package conectaseguros.co.news_service.descuentos.application.service;

import conectaseguros.co.news_service.descuentos.domain.model.DescuentoProcesado;
import conectaseguros.co.news_service.descuentos.domain.port.output.ConsultaDescuentosRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Servicio de aplicación para consultar descuentos procesados */
@Service
@Transactional(readOnly = true)
public class ConsultaDescuentosProcesadosService {

  private static final Logger logger =
      LoggerFactory.getLogger(ConsultaDescuentosProcesadosService.class);

  private final ConsultaDescuentosRepository consultaRepository;

  public ConsultaDescuentosProcesadosService(ConsultaDescuentosRepository consultaRepository) {
    this.consultaRepository = consultaRepository;
  }

  /** Obtiene descuentos procesados por periodo y seccional con paginación */
  public Page<DescuentoProcesado> obtenerDescuentosProcesados(
      String periodo, Integer seccionalId, Pageable pageable) {
    logger.info(
        "Consultando descuentos procesados - Período: {}, Seccional: {}, Página: {}",
        periodo,
        seccionalId,
        pageable.getPageNumber());

    validarParametros(periodo, seccionalId);

    // Convertir formato del período de YYYY/MM a YYYY-MM
    String periodoNormalizado = normalizarPeriodo(periodo);
    logger.debug("Período normalizado de {} a {}", periodo, periodoNormalizado);

    Page<DescuentoProcesado> descuentos =
        consultaRepository.buscarPorPeriodoYSeccional(periodoNormalizado, seccionalId, pageable);

    logger.info(
        "Encontrados {} descuentos procesados de {} totales para período {} y seccional {}",
        descuentos.getNumberOfElements(),
        descuentos.getTotalElements(),
        periodoNormalizado,
        seccionalId);

    return descuentos;
  }

  /** Obtiene todos los descuentos procesados por periodo y seccional sin paginación */
  public List<DescuentoProcesado> obtenerTodosDescuentosProcesados(
      String periodo, Integer seccionalId) {
    logger.info(
        "Consultando todos los descuentos procesados - Período: {}, Seccional: {}",
        periodo,
        seccionalId);

    validarParametros(periodo, seccionalId);

    // Convertir formato del período de YYYY/MM a YYYY-MM
    String periodoNormalizado = normalizarPeriodo(periodo);
    logger.debug("Período normalizado de {} a {}", periodo, periodoNormalizado);

    List<DescuentoProcesado> descuentos =
        consultaRepository.buscarPorPeriodoYSeccional(periodoNormalizado, seccionalId);

    logger.info(
        "Encontrados {} descuentos procesados para período {} y seccional {}",
        descuentos.size(),
        periodoNormalizado,
        seccionalId);

    return descuentos;
  }

  /** Cuenta el total de descuentos procesados por periodo y seccional */
  public long contarDescuentosProcesados(String periodo, Integer seccionalId) {
    logger.info(
        "Contando descuentos procesados - Período: {}, Seccional: {}", periodo, seccionalId);

    validarParametros(periodo, seccionalId);

    // Convertir formato del período de YYYY/MM a YYYY-MM
    String periodoNormalizado = normalizarPeriodo(periodo);
    logger.debug("Período normalizado de {} a {}", periodo, periodoNormalizado);

    long total = consultaRepository.contarPorPeriodoYSeccional(periodoNormalizado, seccionalId);

    logger.info(
        "Total de descuentos procesados: {} para período {} y seccional {}",
        total,
        periodoNormalizado,
        seccionalId);

    return total;
  }

  /** Obtiene todos los descuentos procesados por cédula de cliente */
  public List<DescuentoProcesado> obtenerDescuentosPorCedula(String cedulaCliente) {
    logger.info("Consultando descuentos procesados por cédula: {}", cedulaCliente);

    if (cedulaCliente == null || cedulaCliente.trim().isEmpty()) {
      throw new IllegalArgumentException("La cédula del cliente es requerida");
    }

    List<DescuentoProcesado> descuentos = consultaRepository.buscarPorCedulaCliente(cedulaCliente);

    logger.info(
        "Encontrados {} descuentos procesados para el cliente con cédula {}",
        descuentos.size(),
        cedulaCliente);

    return descuentos;
  }

  /** Normaliza el formato del período de YYYY/MM o YYYY-MM a YYYY-MM (formato de BD) */
  private String normalizarPeriodo(String periodo) {
    if (periodo == null) {
      return null;
    }

    // Si ya está en formato YYYY-MM, no hacer nada
    if (periodo.matches("\\d{4}-\\d{2}")) {
      return periodo;
    }

    // Si está en formato YYYY/MM, convertir a YYYY-MM
    if (periodo.matches("\\d{4}/\\d{2}")) {
      return periodo.replace("/", "-");
    }

    // Si no coincide con ningún formato esperado, lanzar excepción
    throw new IllegalArgumentException(
        "Formato de período inválido: " + periodo + ". Formatos soportados: YYYY/MM o YYYY-MM");
  }

  /** Valida los parámetros de entrada */
  private void validarParametros(String periodo, Integer seccionalId) {
    if (periodo == null || periodo.trim().isEmpty()) {
      throw new IllegalArgumentException("El período es requerido");
    }

    if (seccionalId == null || seccionalId <= 0) {
      throw new IllegalArgumentException("El ID de seccional debe ser un número positivo");
    }

    // Validar formato básico del período
    if (!periodo.matches("\\d{4}[/-]\\d{2}")) {
      throw new IllegalArgumentException(
          "Formato de período inválido: " + periodo + ". Formatos soportados: YYYY/MM o YYYY-MM");
    }
  }
}
