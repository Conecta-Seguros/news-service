package conectaseguros.co.news_service.descuentos.application.service;

import conectaseguros.co.news_service.archivo.domain.model.enums.TipoArchivo;
import conectaseguros.co.news_service.descuentos.domain.model.ErrorProcesamiento;
import conectaseguros.co.news_service.descuentos.domain.model.enums.TipoError;
import conectaseguros.co.news_service.descuentos.domain.port.output.ClienteService;
import conectaseguros.co.news_service.descuentos.domain.port.output.ErrorProcesamientoRepository;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ✅ Validador Global: Verifica que todos los clientes de BD están en el archivo
 *
 * <p>Este validador NO valida línea por línea como los otros. En su lugar: 1. Obtiene TODOS los
 * clientes activos de la seccional desde BD 2. Recibe el conjunto de clientes que fueron procesados
 * del archivo 3. Calcula la diferencia: clientes BD - clientes procesados 4. Registra un error por
 * cada cliente faltante
 */
@Component
public class ValidadorClientesFaltantes {

  private static final Logger logger = LoggerFactory.getLogger(ValidadorClientesFaltantes.class);

  private final ClienteService clienteService;
  private final ErrorProcesamientoRepository errorProcesamientoRepository;

  public ValidadorClientesFaltantes(
      ClienteService clienteService, ErrorProcesamientoRepository errorProcesamientoRepository) {
    this.clienteService = clienteService;
    this.errorProcesamientoRepository = errorProcesamientoRepository;
  }

  /**
   * ✅ Valida que todos los clientes de la seccional fueron procesados en el archivo
   *
   * @param archivoId ID del archivo siendo procesado
   * @param seccionalId ID de la seccional
   * @param clientesArchivoProcesados Set de cédulas de clientes procesados del archivo
   * @param periodo Período de procesamiento (para el registro de errores)
   * @return Cantidad de clientes faltantes encontrados
   */
  public int validarClientesFaltantes(
      Long archivoId, Integer seccionalId, Set<String> clientesArchivoProcesados, String periodo) {

    logger.info("=== INICIANDO VALIDACIÓN GLOBAL: BD → Archivo ===");
    logger.info(
        "Seccional: {}, Archivo: {}, Clientes procesados: {}",
        seccionalId,
        archivoId,
        clientesArchivoProcesados.size());

    try {
      // PASO 1: Obtener todos los clientes activos de la seccional
      logger.debug("PASO 1: Obteniendo clientes activos de BD para seccional {}", seccionalId);

      List<ClienteService.ClienteInfo> clientesBD =
          clienteService.obtenerClientesActivosPorSeccional(seccionalId);

      Set<String> clientesBDSet =
          clientesBD.stream()
              .map(ClienteService.ClienteInfo::getCedula)
              .collect(Collectors.toSet());

      logger.info(
          "✅ Se obtuvieron {} clientes activos de BD para seccional {}",
          clientesBDSet.size(),
          seccionalId);
      logger.debug("Clientes en BD: {}", clientesBDSet);

      // PASO 2: Calcular diferencia (clientes faltantes)
      logger.debug("PASO 2: Calculando diferencia (BD - Archivo procesado)");

      Set<String> clientesFaltantes = new HashSet<>(clientesBDSet);
      clientesFaltantes.removeAll(clientesArchivoProcesados);

      logger.info("📊 Clientes faltantes encontrados: {}", clientesFaltantes.size());

      if (!clientesFaltantes.isEmpty()) {
        logger.warn("⚠️  Los siguientes clientes NO fueron procesados en el archivo:");
        clientesFaltantes.forEach(cedula -> logger.warn("   - {}", cedula));
      } else {
        logger.info("✅ Todos los clientes de la seccional fueron procesados en el archivo");
        return 0;
      }

      // PASO 3: Registrar error por cada cliente faltante
      logger.debug("PASO 3: Registrando errores para clientes faltantes");

      int erroresRegistrados =
          registrarClientesFaltantes(archivoId, seccionalId, clientesFaltantes, periodo);

      logger.info("✅ Se registraron {} errores de clientes faltantes", erroresRegistrados);

      return erroresRegistrados;

    } catch (Exception e) {
      logger.error("❌ Error durante validación de clientes faltantes: {}", e.getMessage(), e);
      throw new ValidacionClientesFaltantesException(
          "Error al validar clientes faltantes: " + e.getMessage(), e);
    }
  }

  /**
   * ✅ Registra un error por cada cliente faltante en la base de datos
   *
   * @param archivoId ID del archivo
   * @param seccionalId ID de la seccional
   * @param clientesFaltantes Set de cédulas de clientes faltantes
   * @param periodo Período de procesamiento
   * @return Cantidad de errores registrados
   */
  private int registrarClientesFaltantes(
      Long archivoId, Integer seccionalId, Set<String> clientesFaltantes, String periodo) {

    int erroresRegistrados = 0;
    LocalDateTime ahora = LocalDateTime.now();

    for (String cedulaFaltante : clientesFaltantes) {
      try {
        logger.debug("Registrando cliente faltante: {} para archivo {}", cedulaFaltante, archivoId);

        // Crear error de procesamiento
        ErrorProcesamiento error =
            new ErrorProcesamiento(
                archivoId,
                TipoArchivo.DESCUENTOS,
                null, // No aplica - validación global
                cedulaFaltante,
                periodo,
                seccionalId,
                TipoError.CLIENTE_FALTANTE_EN_ARCHIVO,
                "Cliente esperado no se encuentra en el archivo",
                null);

        // Guardar en BD
        errorProcesamientoRepository.guardar(error);

        logger.debug("✅ Error registrado para cliente faltante: {}", cedulaFaltante);
        erroresRegistrados++;

      } catch (Exception e) {
        logger.error(
            "❌ Error al registrar cliente faltante {}: {}", cedulaFaltante, e.getMessage(), e);
        // Continuar registrando los demás clientes aunque uno falle
      }
    }

    logger.info("✅ Total de errores registrados: {}", erroresRegistrados);
    return erroresRegistrados;
  }

  /** Excepción personalizada para errores en validación de clientes faltantes */
  public static class ValidacionClientesFaltantesException extends RuntimeException {
    public ValidacionClientesFaltantesException(String mensaje) {
      super(mensaje);
    }

    public ValidacionClientesFaltantesException(String mensaje, Throwable causa) {
      super(mensaje, causa);
    }
  }
}
