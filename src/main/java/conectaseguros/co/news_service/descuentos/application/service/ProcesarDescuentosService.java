package conectaseguros.co.news_service.descuentos.application.service;

import conectaseguros.co.news_service.archivo.domain.exceptions.ArchivoNoEncontradoException;
import conectaseguros.co.news_service.archivo.domain.model.ArchivoDescuentos;
import conectaseguros.co.news_service.archivo.domain.model.enums.EstadoArchivo;
import conectaseguros.co.news_service.archivo.domain.model.enums.TipoArchivo;
import conectaseguros.co.news_service.archivo.domain.port.output.ArchivoDescuentosRepository;
import conectaseguros.co.news_service.descuentos.domain.model.*;
import conectaseguros.co.news_service.descuentos.domain.model.enums.TipoError;
import conectaseguros.co.news_service.descuentos.domain.port.input.ValidadorLinea;
import conectaseguros.co.news_service.descuentos.domain.port.output.ClienteService;
import conectaseguros.co.news_service.descuentos.domain.port.output.DescuentoRepository;
import conectaseguros.co.news_service.descuentos.domain.port.output.ErrorProcesamientoRepository;
import conectaseguros.co.news_service.descuentos.infrastructure.output.product.ProductoInfo;
import java.io.BufferedReader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProcesarDescuentosService {

  private static final Logger logger = LoggerFactory.getLogger(ProcesarDescuentosService.class);

  private final ArchivoDescuentosRepository archivoRepository;
  private final DescuentoRepository descuentoRepository;
  private final ErrorProcesamientoRepository errorRepository;
  private final ClienteService clienteService;
  private final ValidadorFormatoLinea validadorFormato;
  private final ValidadorCliente validadorCliente;
  private final ValidadorProductos validadorProductos;
  private final ValidadorClientesFaltantes validadorClientesFaltantes;
  private final RegistrarPagoNominaService registrarPagoNominaService;

  @Value("${app.procesamiento.lote-tamano:1000}")
  private int tamanoLote;

  public ProcesarDescuentosService(
      ArchivoDescuentosRepository archivoRepository,
      DescuentoRepository descuentoRepository,
      ErrorProcesamientoRepository errorRepository,
      ClienteService clienteService,
      ValidadorFormatoLinea validadorFormato,
      ValidadorCliente validadorCliente,
      ValidadorProductos validadorProductos,
      ValidadorClientesFaltantes validadorClientesFaltantes,
      RegistrarPagoNominaService registrarPagoNominaService) {
    this.archivoRepository = archivoRepository;
    this.descuentoRepository = descuentoRepository;
    this.errorRepository = errorRepository;
    this.clienteService = clienteService;
    this.validadorFormato = validadorFormato;
    this.validadorCliente = validadorCliente;
    this.validadorProductos = validadorProductos;
    this.validadorClientesFaltantes = validadorClientesFaltantes;
    this.registrarPagoNominaService = registrarPagoNominaService;
  }

  /** Procesa un archivo de descuentos de forma asíncrona */
  @Async
  public CompletableFuture<EstadoProcesamiento> procesarArchivo(Long archivoId) {
    logger.info("=== INICIANDO PROCESAMIENTO ASÍNCRONO DE ARCHIVO {} ===", archivoId);

    return CompletableFuture.supplyAsync(
        () -> {
          try {
            logger.info("Ejecutando procesamiento en hilo: {}", Thread.currentThread().getName());
            EstadoProcesamiento resultado = procesarArchivoSincrono(archivoId);
            logger.info("=== PROCESAMIENTO COMPLETADO PARA ARCHIVO {} ===", archivoId);
            return resultado;
          } catch (Exception e) {
            logger.error("=== ERROR EN PROCESAMIENTO ASÍNCRONO DE ARCHIVO {} ===", archivoId, e);

            // Marcar archivo como ERROR
            try {
              marcarArchivoConError(archivoId, e.getMessage());
            } catch (Exception ex) {
              logger.error("Error al marcar archivo {} con error", archivoId, ex);
            }

            return EstadoProcesamiento.conError(
                "Error inesperado: " + e.getMessage(), java.time.LocalDateTime.now());
          }
        });
  }

  /** Valida la estructura básica del archivo */
  private ResultadoValidacion validarEstructuraArchivo(List<String> lineas, Long archivoId) {
    logger.debug("Validando estructura del archivo {}", archivoId);

    if (lineas.isEmpty()) {
      return ResultadoValidacion.conError(TipoError.ARCHIVO_VACIO, "El archivo está vacío");
    }

    if (lineas.size() < 2) {
      return ResultadoValidacion.conError(
          TipoError.ARCHIVO_VACIO,
          "El archivo debe contener al menos una línea de datos además del header");
    }

    // Validar headers
    String header = lineas.get(0);
    String[] headersArchivo = parsearLinea(header);
    // Normalizar headers: eliminar espacios y convertir a mayúsculas
    for (int h = 0; h < headersArchivo.length; h++) {
      headersArchivo[h] = headersArchivo[h].replace("\"", "").trim().toUpperCase();
    }
    boolean tieneCodigoEmpleado = false;
    boolean tieneValor = false;
    for (String h : headersArchivo) {
      if (h.equals("CODEMPLEADO")) {
        tieneCodigoEmpleado = true;
      }
      if (h.equals("VALOR")) {
        tieneValor = true;
      }
    }
    if (!tieneCodigoEmpleado || !tieneValor) {
      return ResultadoValidacion.conError(
          TipoError.HEADERS_INCORRECTOS,
          "El archivo debe contener los headers CODEMPLEADO y VALOR");
    }
    logger.debug("Estructura del archivo {} validada correctamente", archivoId);
    return ResultadoValidacion.exitoso();
  }

  /** Procesa un archivo de descuentos de forma síncrona */
  public EstadoProcesamiento procesarArchivoSincrono(Long archivoId) {
    logger.info("=== INICIANDO PROCESAMIENTO SÍNCRONO DE ARCHIVO {} ===", archivoId);

    // Obtener archivo
    ArchivoDescuentos archivo =
        archivoRepository
            .buscarPorId(archivoId)
            .orElseThrow(
                () -> new ArchivoNoEncontradoException("Archivo no encontrado: " + archivoId));

    logger.info(
        "Archivo encontrado: {} - Estado: {}", archivo.getNombreArchivo(), archivo.getEstado());

    // ✅ NUEVO: Cambiar estado a PROCESANDO al inicio
    try {
      archivo.setEstado(EstadoArchivo.PROCESANDO);
      archivoRepository.guardar(archivo);
      logger.info("Estado del archivo {} cambiado a PROCESANDO", archivoId);
    } catch (Exception e) {
      logger.error("Error al cambiar estado a PROCESANDO para archivo {}", archivoId, e);
      throw new RuntimeException("Error al inicializar procesamiento", e);
    }

    ProcesamientoDescuentos procesamiento =
        new ProcesamientoDescuentos(archivoId, archivo.getPeriodo(), archivo.getSeccionalId());

    try {
      // Configurar cadena de validadores
      ValidadorLinea cadenaValidadores = configurarCadenaValidadores();
      logger.info("Cadena de validadores configurada para archivo: {}", archivoId);

      // Convertir contenido a líneas
      String contenidoTexto = new String(archivo.getContenido());
      List<String> lineas = obtenerLineasArchivo(contenidoTexto);
      logger.info("Archivo {} contiene {} líneas totales", archivoId, lineas.size());

      // Validar estructura del archivo
      ResultadoValidacion validacionEstructura = validarEstructuraArchivo(lineas, archivoId);
      if (validacionEstructura.tieneErrores()) {
        logger.error(
            "Error de estructura en archivo {}: {}",
            archivoId,
            validacionEstructura.getDescripcionErrores());
        procesamiento.marcarConError(
            "Error de estructura: " + validacionEstructura.getDescripcionErrores());
        guardarErroresEstructura(validacionEstructura.getErrores(), archivoId);

        // Marcar archivo con error
        archivo.setEstado(EstadoArchivo.ERROR);
        archivoRepository.guardar(archivo);

        return procesamiento.toEstadoProcesamiento();
      }

      // Obtener índices de los headers relevantes
      String[] headersArchivo = parsearLinea(lineas.get(0));
      int idxCodigoEmpleado = -1;
      int idxValor = -1;
      for (int i = 0; i < headersArchivo.length; i++) {
        if (headersArchivo[i].trim().equalsIgnoreCase("CODEMPLEADO")) {
          idxCodigoEmpleado = i;
        }
        if (headersArchivo[i].trim().equalsIgnoreCase("VALOR")) {
          idxValor = i;
        }
      }
      if (idxCodigoEmpleado == -1 || idxValor == -1) {
        throw new RuntimeException("No se encontraron los headers CODEMPLEADO y VALOR");
      }

      // Iniciar procesamiento
      int totalLineasDatos = lineas.size() - 1; // Excluir header
      procesamiento.iniciarProcesamiento(totalLineasDatos);

      logger.info("Procesando {} líneas de datos para archivo {}", totalLineasDatos, archivoId);

      // ✅ NUEVA VALIDACIÓN FASE 1: BD → Archivo
      // Crear Set vacío que se poblará mientras procesamos las líneas
      Set<String> clientesArchivoProcesados = new HashSet<>();

      // Procesar líneas en lotes
      List<DescuentoValidado> descuentosLote = new ArrayList<>();
      List<ErrorProcesamiento> erroresLote = new ArrayList<>();
      // Set para evitar duplicados en el lote
      java.util.Set<String> clavesUnicasLote = new java.util.HashSet<>();

      for (int i = 1; i < lineas.size(); i++) { // Empezar desde 1 para saltar header
        try {
          String lineaTexto = lineas.get(i);
          if (lineaTexto.trim().isEmpty()) {
            logger.debug("Saltando línea vacía en posición {}", i + 1);
            continue; // Saltar líneas vacías
          }

          String[] campos = parsearLinea(lineaTexto);
          Map<String, String> camposPorNombre = new java.util.HashMap<>();
          for (int j = 0; j < headersArchivo.length && j < campos.length; j++) {
            camposPorNombre.put(headersArchivo[j].trim(), campos[j].trim());
          }
          String codigoEmpleado = camposPorNombre.get("CODEMPLEADO");
          String valorStr = camposPorNombre.get("VALOR");
          // Validar presencia de campos obligatorios antes de crear LineaDescuento
          boolean faltanCamposObligatorios = false;
          StringBuilder camposFaltantes = new StringBuilder();
          // Solo exigir CODEMPLEADO y VALOR
          String[] obligatorios = {"CODEMPLEADO", "VALOR"};
          for (String campo : obligatorios) {
            if (camposPorNombre.get(campo) == null || camposPorNombre.get(campo).trim().isEmpty()) {
              faltanCamposObligatorios = true;
              if (camposFaltantes.length() > 0) camposFaltantes.append(", ");
              camposFaltantes.append(campo);
            }
          }
          if (faltanCamposObligatorios) {
            logger.debug("Línea {} faltan campos obligatorios: {}", i + 1, camposFaltantes);
            ErrorProcesamiento error =
                new ErrorProcesamiento(
                    archivoId,
                    TipoArchivo.DESCUENTOS,
                    i + 1,
                    null,
                    archivo.getPeriodo(),
                    archivo.getSeccionalId(),
                    TipoError.CAMPO_OBLIGATORIO_FALTANTE,
                    "Faltan campos obligatorios: " + camposFaltantes,
                    lineaTexto);
            erroresLote.add(error);
            procesamiento.registrarLineaProcesada(false);
            continue;
          }
          // Crear LineaDescuento con el Map y el número de línea
          LineaDescuento linea = new LineaDescuento(camposPorNombre, i + 1);

          // ✅ AGREGAR CLIENTE AL CONJUNTO DE PROCESADOS (independiente de validaciones)
          // Esto debe ocurrir ANTES de las validaciones para capturar todos los clientes del
          // archivo
          if (linea.getCodigoEmpleado() != null && !linea.getCodigoEmpleado().trim().isEmpty()) {
            clientesArchivoProcesados.add(linea.getCodigoEmpleado());
            logger.debug(
                "✅ Cliente {} agregado al conjunto de clientes procesados (línea {})",
                linea.getCodigoEmpleado(),
                i + 1);
          }

          // ✅ CORRECCIÓN: Validar línea pasando el período del archivo
          ResultadoValidacion resultado =
              configurarCadenaValidadores()
                  .validar(linea, archivo.getSeccionalId(), archivo.getPeriodo());
          logger.debug(
              "Validación de línea {}: {}",
              i + 1,
              resultado.esValido() ? "EXITOSA" : "CON ERRORES");

          if (resultado.esValido()) {
            // ✅ OBTENER VALOR ESPERADO DESDE EL RESULTADO DE VALIDACIÓN
            BigDecimal valorEsperado = resultado.getValorEsperado();
            if (valorEsperado == null) {
              // Fallback: calcular valor esperado usando el período específico
              ClienteService.ClienteInfo clienteInfo =
                  resultado.getDatosAdicionales(ClienteService.ClienteInfo.class);
              Integer clienteId =
                  clienteInfo != null
                      ? clienteInfo.getId()
                      : obtenerClienteIdFallback(linea.getCodigoEmpleado());

              // ✅ CORRECCIÓN: Usar ProductoService en lugar de ValidadorProductos directamente
              List<ProductoInfo> productosActivos =
                  validadorProductos
                      .getProductoService()
                      .obtenerProductosActivosPorCedula(linea.getCodigoEmpleado());
              valorEsperado =
                  productosActivos.stream()
                      .map(
                          p ->
                              p.getValorParaPeriodo(
                                  archivo.getPeriodo())) // ✅ Usar período específico
                      .filter(valor -> valor.compareTo(BigDecimal.ZERO) > 0)
                      .reduce(BigDecimal.ZERO, BigDecimal::add);

              logger.debug(
                  "Valor esperado calculado como fallback para cliente {} período {}: {}",
                  linea.getCodigoEmpleado(),
                  archivo.getPeriodo(),
                  valorEsperado);
            } else {
              logger.debug(
                  "Valor esperado obtenido desde validación para cliente {} período {}: {}",
                  linea.getCodigoEmpleado(),
                  archivo.getPeriodo(),
                  valorEsperado);
            }

            // Generar clave única para el lote
            String claveUnica =
                linea.getCodigoEmpleado()
                    + "|"
                    + archivo.getSeccionalId()
                    + "|"
                    + archivo.getPeriodo()
                    + "|"
                    + linea.getCodigoConcepto()
                    + "|"
                    + archivoId;
            if (clavesUnicasLote.contains(claveUnica)) {
              logger.debug("Línea {} es duplicada en el lote para clave {}", i + 1, claveUnica);
              ErrorProcesamiento error =
                  ErrorProcesamiento.deLineaDescuento(
                      archivoId,
                      linea,
                      new ErrorValidacion(
                          TipoError.DUPLICADO_PERIODO,
                          String.format(
                              "Descuento duplicado en el lote para cliente %s en período %s, concepto %s",
                              linea.getCodigoEmpleado(),
                              archivo.getPeriodo(),
                              linea.getCodigoConcepto())));
              erroresLote.add(error);
              procesamiento.registrarLineaProcesada(false);
              continue;
            }
            clavesUnicasLote.add(claveUnica);

            // Verificar duplicados en la base de datos
            if (!descuentoRepository.existeDescuentoPeriodo(
                linea.getCodigoEmpleado(), archivo.getPeriodo(), linea.getCodigoConcepto())) {
              ClienteService.ClienteInfo clienteInfo =
                  resultado.getDatosAdicionales(ClienteService.ClienteInfo.class);
              Integer clienteId =
                  clienteInfo != null
                      ? clienteInfo.getId()
                      : obtenerClienteIdFallback(linea.getCodigoEmpleado());

              // ✅ CORRECCIÓN: Crear DescuentoValidado con valor esperado correcto
              DescuentoValidado descuento =
                  DescuentoValidado.fromLineaDescuento(
                      linea,
                      clienteId,
                      archivo.getSeccionalId(),
                      archivo.getPeriodo(),
                      archivoId,
                      valorEsperado,
                      linea.getValor() // valor del archivo
                      );

              descuentosLote.add(descuento);
              procesamiento.registrarLineaProcesada(true);

              logger.debug(
                  "Línea {} agregada como descuento válido con valor esperado: {}",
                  i + 1,
                  valorEsperado);
            } else {
              logger.debug(
                  "Línea {} es duplicada en BD para cliente {} en período {}",
                  i + 1,
                  linea.getCodigoEmpleado(),
                  archivo.getPeriodo());
              ErrorProcesamiento error =
                  ErrorProcesamiento.deLineaDescuento(
                      archivoId,
                      linea,
                      new ErrorValidacion(
                          TipoError.DUPLICADO_PERIODO,
                          String.format(
                              "Descuento duplicado en BD para cliente %s en período %s, concepto %s",
                              linea.getCodigoEmpleado(),
                              archivo.getPeriodo(),
                              linea.getCodigoConcepto())));
              erroresLote.add(error);
              procesamiento.registrarLineaProcesada(false);
            }
          } else {
            logger.debug(
                "Línea {} tiene {} errores de validación", i + 1, resultado.getErrores().size());
            for (ErrorValidacion errorValidacion : resultado.getErrores()) {
              ErrorProcesamiento error =
                  ErrorProcesamiento.deLineaDescuento(archivoId, linea, errorValidacion);
              erroresLote.add(error);
            }
            procesamiento.registrarLineaProcesada(false);
          }

          if (descuentosLote.size() >= tamanoLote || erroresLote.size() >= tamanoLote) {
            logger.debug(
                "Guardando lote: {} descuentos, {} errores",
                descuentosLote.size(),
                erroresLote.size());
            guardarLotes(descuentosLote, erroresLote);
            descuentosLote.clear();
            erroresLote.clear();
            clavesUnicasLote.clear();
          }

        } catch (Exception e) {
          logger.error(
              "Error procesando línea {} del archivo {}: {}", i + 1, archivoId, e.getMessage());
          ErrorProcesamiento error =
              new ErrorProcesamiento(
                  archivoId,
                  TipoArchivo.DESCUENTOS,
                  i + 1,
                  null,
                  archivo.getPeriodo(),
                  archivo.getSeccionalId(),
                  TipoError.ERROR_PROCESAMIENTO,
                  "Error inesperado: " + e.getMessage(),
                  lineas.get(i));
          erroresLote.add(error);
          procesamiento.registrarLineaProcesada(false);
        }

        // Log de progreso cada 100 líneas
        if (i % 100 == 0) {
          logger.info(
              "Procesadas {} de {} líneas ({}%)",
              procesamiento.getLineasProcesadas(),
              totalLineasDatos,
              String.format("%.1f", procesamiento.getPorcentajeProgreso()));
        }
      }

      // Guardar lote final
      logger.info(
          "Guardando lote final: {} descuentos, {} errores",
          descuentosLote.size(),
          erroresLote.size());
      guardarLotes(descuentosLote, erroresLote);

      // ✅ NUEVA VALIDACIÓN FASE 2: Validar clientes faltantes (BD → Archivo)
      logger.info("=== INICIANDO VALIDACIÓN DE CLIENTES FALTANTES ===");
      logger.info("Clientes procesados del archivo: {}", clientesArchivoProcesados.size());

      try {
        int clientesFaltantes =
            validadorClientesFaltantes.validarClientesFaltantes(
                archivoId,
                archivo.getSeccionalId(),
                clientesArchivoProcesados,
                archivo.getPeriodo());

        logger.info(
            "✅ Validación de clientes faltantes completada: {} clientes no procesados",
            clientesFaltantes);
        procesamiento.agregarClientesFaltantes(clientesFaltantes);

      } catch (Exception e) {
        logger.error(
            "⚠️ Error en validación de clientes faltantes (continuando procesamiento): {}",
            e.getMessage(),
            e);
        // NO es error crítico, continuamos con el procesamiento
      }

      // ✅ REGISTRAR PAGOS EN PAYMENTS-SERVICE
      logger.info("=== INICIANDO REGISTRO DE PAGOS EN PAYMENTS-SERVICE ===");
      try {
        // Obtener todos los descuentos validados del archivo
        List<DescuentoValidado> descuentosParaRegistrar =
            descuentoRepository.buscarPorArchivo(archivoId);

        if (!descuentosParaRegistrar.isEmpty()) {
          logger.info(
              "Encontrados {} descuentos validados para registrar como pagos",
              descuentosParaRegistrar.size());

          // Llamar al servicio de registro de pagos de forma asíncrona
          registrarPagoNominaService.registrarPagosAsync(descuentosParaRegistrar, archivoId);

          logger.info(
              "✅ Proceso de registro de pagos iniciado para {} descuentos del archivo {}",
              descuentosParaRegistrar.size(),
              archivoId);
        } else {
          logger.warn("No se encontraron descuentos validados para registrar como pagos");
        }
      } catch (Exception e) {
        logger.error(
            "⚠️ Error al iniciar registro de pagos (continuando procesamiento): {}",
            e.getMessage(),
            e);
        // NO es error crítico del procesamiento del archivo, continuamos
      }

      // Completar procesamiento
      procesamiento.completarProcesamiento();

      // Actualizar estado del archivo
      actualizarEstadoArchivo(archivo, procesamiento);

      logger.info(
          "Procesamiento completado para archivo {}. Exitosas: {}, Errores: {}",
          archivoId,
          procesamiento.getLineasExitosas(),
          procesamiento.getLineasConError());

      return procesamiento.toEstadoProcesamiento();

    } catch (Exception e) {
      logger.error("Error durante procesamiento de archivo {}", archivoId, e);

      // Marcar archivo con error
      try {
        archivo.setEstado(EstadoArchivo.ERROR);
        archivoRepository.guardar(archivo);
      } catch (Exception ex) {
        logger.error("Error al cambiar estado a ERROR para archivo {}", archivoId, ex);
      }

      procesamiento.marcarConError("Error durante procesamiento: " + e.getMessage());
      return procesamiento.toEstadoProcesamiento();
    }
  }

  /**
   * Procesa un archivo de descuentos a partir de filas (List<String[]>) para soportar XLSX nativo
   */
  public EstadoProcesamiento procesarArchivoDesdeFilas(Long archivoId, List<String[]> filas) {
    logger.info("=== INICIANDO PROCESAMIENTO DESDE FILAS PARA ARCHIVO {} ===", archivoId);

    ArchivoDescuentos archivo =
        archivoRepository
            .buscarPorId(archivoId)
            .orElseThrow(
                () -> new ArchivoNoEncontradoException("Archivo no encontrado: " + archivoId));

    // Cambiar estado a PROCESANDO al inicio
    try {
      archivo.setEstado(EstadoArchivo.PROCESANDO);
      archivoRepository.guardar(archivo);
      logger.info("Estado del archivo {} cambiado a PROCESANDO", archivoId);
    } catch (Exception e) {
      logger.error("Error al cambiar estado a PROCESANDO para archivo {}", archivoId, e);
      throw new RuntimeException("Error al inicializar procesamiento", e);
    }

    ProcesamientoDescuentos procesamiento =
        new ProcesamientoDescuentos(archivoId, archivo.getPeriodo(), archivo.getSeccionalId());

    try {
      ValidadorLinea cadenaValidadores = configurarCadenaValidadores();
      logger.info("Cadena de validadores configurada para archivo: {}", archivoId);

      if (filas == null || filas.isEmpty()) {
        procesamiento.marcarConError("El archivo está vacío");
        archivo.setEstado(EstadoArchivo.ERROR);
        archivoRepository.guardar(archivo);
        return procesamiento.toEstadoProcesamiento();
      }
      if (filas.size() < 2) {
        procesamiento.marcarConError(
            "El archivo debe contener al menos una línea de datos además del header");
        archivo.setEstado(EstadoArchivo.ERROR);
        archivoRepository.guardar(archivo);
        return procesamiento.toEstadoProcesamiento();
      }

      // Normalizar headers
      String[] headersArchivo = filas.get(0);
      for (int h = 0; h < headersArchivo.length; h++) {
        headersArchivo[h] = headersArchivo[h].replace("\"", "").trim().toUpperCase();
      }
      boolean tieneCodigoEmpleado = false;
      boolean tieneValor = false;
      for (String h : headersArchivo) {
        if (h.equals("CODEMPLEADO")) tieneCodigoEmpleado = true;
        if (h.equals("VALOR")) tieneValor = true;
      }
      if (!tieneCodigoEmpleado || !tieneValor) {
        procesamiento.marcarConError("El archivo debe contener los headers CODEMPLEADO y VALOR");
        archivo.setEstado(EstadoArchivo.ERROR);
        archivoRepository.guardar(archivo);
        return procesamiento.toEstadoProcesamiento();
      }

      int totalLineasDatos = filas.size() - 1;
      procesamiento.iniciarProcesamiento(totalLineasDatos);
      logger.info("Procesando {} líneas de datos para archivo {}", totalLineasDatos, archivoId);

      // ✅ Crear Set para recolectar clientes procesados del archivo
      Set<String> clientesArchivoProcesados = new HashSet<>();

      // Agrupar descuentos por cliente/concepto/período/seccional/archivo y sumar valores
      Map<String, List<LineaDescuento>> descuentosPorClave = new java.util.HashMap<>();
      List<ErrorProcesamiento> erroresLote = new ArrayList<>();
      for (int i = 1; i < filas.size(); i++) { // Empezar desde 1 para saltar header
        try {
          String[] campos = filas.get(i);
          // Filtrar líneas vacías o con todos los campos vacíos
          boolean todosVacios = true;
          for (String campo : campos) {
            if (campo != null && !campo.trim().isEmpty()) {
              todosVacios = false;
              break;
            }
          }
          if (campos.length == 0 || todosVacios) {
            continue; // Saltar líneas vacías
          }
          for (int c = 0; c < campos.length; c++) {
            campos[c] = campos[c].replace("\"", "").trim();
          }
          Map<String, String> camposPorNombre = new java.util.HashMap<>();
          for (int j = 0; j < headersArchivo.length && j < campos.length; j++) {
            camposPorNombre.put(headersArchivo[j], campos[j]);
          }
          boolean faltanCamposObligatorios = false;
          StringBuilder camposFaltantes = new StringBuilder();
          String[] obligatorios = {"CODEMPLEADO", "VALOR"};
          for (String campo : obligatorios) {
            if (camposPorNombre.get(campo) == null || camposPorNombre.get(campo).trim().isEmpty()) {
              faltanCamposObligatorios = true;
              if (!camposFaltantes.isEmpty()) camposFaltantes.append(", ");
              camposFaltantes.append(campo);
            }
          }
          if (faltanCamposObligatorios) {
            ErrorProcesamiento error =
                new ErrorProcesamiento(
                    archivoId,
                    TipoArchivo.DESCUENTOS,
                    i + 1,
                    null,
                    archivo.getPeriodo(),
                    archivo.getSeccionalId(),
                    TipoError.CAMPO_OBLIGATORIO_FALTANTE,
                    "Faltan campos obligatorios: " + camposFaltantes,
                    String.join(",", campos));
            erroresLote.add(error);
            procesamiento.registrarLineaProcesada(false);
            continue;
          }
          LineaDescuento linea = new LineaDescuento(camposPorNombre, i + 1);

          // ✅ AGREGAR CLIENTE AL CONJUNTO DE PROCESADOS (independiente de validaciones)
          if (linea.getCodigoEmpleado() != null && !linea.getCodigoEmpleado().trim().isEmpty()) {
            clientesArchivoProcesados.add(linea.getCodigoEmpleado());
            logger.debug(
                "✅ Cliente {} agregado al conjunto de clientes procesados (línea {})",
                linea.getCodigoEmpleado(),
                i + 1);
          }

          String clave =
              linea.getCodigoEmpleado()
                  + "|"
                  + linea.getCodigoConcepto()
                  + "|"
                  + archivo.getPeriodo()
                  + "|"
                  + archivo.getSeccionalId()
                  + "|"
                  + archivoId;
          descuentosPorClave.computeIfAbsent(clave, k -> new ArrayList<>()).add(linea);
          procesamiento.registrarLineaProcesada(true);
        } catch (Exception e) {
          ErrorProcesamiento error =
              new ErrorProcesamiento(
                  archivoId,
                  TipoArchivo.DESCUENTOS,
                  i + 1,
                  null,
                  archivo.getPeriodo(),
                  archivo.getSeccionalId(),
                  TipoError.ERROR_PROCESAMIENTO,
                  "Error inesperado: " + e.getMessage(),
                  String.join(",", filas.get(i)));
          erroresLote.add(error);
          procesamiento.registrarLineaProcesada(false);
        }
      }

      // Procesar descuentos agrupados
      List<DescuentoValidado> descuentosLote = new ArrayList<>();
      for (Map.Entry<String, List<LineaDescuento>> entry : descuentosPorClave.entrySet()) {
        List<LineaDescuento> grupo = entry.getValue();
        BigDecimal totalDescuento =
            grupo.stream().map(LineaDescuento::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
        LineaDescuento base = grupo.get(0);

        // ✅ CORRECCIÓN: Agregar el parámetro periodo faltante
        ResultadoValidacion resultado =
            cadenaValidadores.validar(base, archivo.getSeccionalId(), archivo.getPeriodo());

        if (resultado.esValido()) {
          if (!descuentoRepository.existeDescuentoPeriodo(
              base.getCodigoEmpleado(), archivo.getPeriodo(), base.getCodigoConcepto())) {
            ClienteService.ClienteInfo clienteInfo =
                resultado.getDatosAdicionales(ClienteService.ClienteInfo.class);
            Integer clienteId =
                clienteInfo != null
                    ? clienteInfo.getId()
                    : obtenerClienteIdFallback(base.getCodigoEmpleado());

            // ✅ CORRECCIÓN: Usar el método correcto del ProductoService con período específico
            List<ProductoInfo> productosActivos =
                validadorProductos
                    .getProductoService()
                    .obtenerProductosActivosPorCedula(base.getCodigoEmpleado());
            BigDecimal valorEsperado =
                productosActivos.stream()
                    .map(
                        p ->
                            p.getValorParaPeriodo(
                                archivo.getPeriodo())) // ✅ Usar período específico
                    .filter(valor -> valor.compareTo(BigDecimal.ZERO) > 0)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            DescuentoValidado descuento =
                DescuentoValidado.fromLineaDescuento(
                    base,
                    clienteId,
                    archivo.getSeccionalId(),
                    archivo.getPeriodo(),
                    archivoId,
                    valorEsperado,
                    totalDescuento);
            descuentosLote.add(descuento);
          } else {
            ErrorProcesamiento error =
                ErrorProcesamiento.deLineaDescuento(
                    archivoId,
                    base,
                    new ErrorValidacion(
                        TipoError.DUPLICADO_PERIODO,
                        String.format(
                            "Descuento duplicado en BD para cliente %s en período %s, concepto %s",
                            base.getCodigoEmpleado(),
                            archivo.getPeriodo(),
                            base.getCodigoConcepto())));
            erroresLote.add(error);
          }
        } else {
          for (ErrorValidacion errorValidacion : resultado.getErrores()) {
            ErrorProcesamiento error =
                ErrorProcesamiento.deLineaDescuento(archivoId, base, errorValidacion);
            erroresLote.add(error);
          }
        }
      }

      // Guardar lote final
      guardarLotes(descuentosLote, erroresLote);

      // ✅ VALIDACIÓN DE CLIENTES FALTANTES (BD → Archivo)
      logger.info("=== INICIANDO VALIDACIÓN DE CLIENTES FALTANTES (XLSX) ===");
      logger.info("Clientes procesados del archivo: {}", clientesArchivoProcesados.size());

      try {
        int clientesFaltantes =
            validadorClientesFaltantes.validarClientesFaltantes(
                archivoId,
                archivo.getSeccionalId(),
                clientesArchivoProcesados,
                archivo.getPeriodo());

        logger.info(
            "✅ Validación de clientes faltantes completada: {} clientes no procesados",
            clientesFaltantes);
        procesamiento.agregarClientesFaltantes(clientesFaltantes);

      } catch (Exception e) {
        logger.error(
            "⚠️ Error en validación de clientes faltantes (continuando procesamiento): {}",
            e.getMessage(),
            e);
        // NO es error crítico, continuamos con el procesamiento
      }

      // ✅ REGISTRAR PAGOS EN PAYMENTS-SERVICE
      logger.info("=== INICIANDO REGISTRO DE PAGOS EN PAYMENTS-SERVICE (XLSX) ===");
      try {
        // Obtener todos los descuentos validados del archivo
        List<DescuentoValidado> descuentosParaRegistrar =
            descuentoRepository.buscarPorArchivo(archivoId);

        if (!descuentosParaRegistrar.isEmpty()) {
          logger.info(
              "Encontrados {} descuentos validados para registrar como pagos",
              descuentosParaRegistrar.size());

          // Llamar al servicio de registro de pagos de forma asíncrona
          registrarPagoNominaService.registrarPagosAsync(descuentosParaRegistrar, archivoId);

          logger.info(
              "✅ Proceso de registro de pagos iniciado para {} descuentos del archivo {}",
              descuentosParaRegistrar.size(),
              archivoId);
        } else {
          logger.warn("No se encontraron descuentos validados para registrar como pagos");
        }
      } catch (Exception e) {
        logger.error(
            "⚠️ Error al iniciar registro de pagos (continuando procesamiento): {}",
            e.getMessage(),
            e);
        // NO es error crítico del procesamiento del archivo, continuamos
      }

      procesamiento.completarProcesamiento();
      actualizarEstadoArchivo(archivo, procesamiento);
      return procesamiento.toEstadoProcesamiento();

    } catch (Exception e) {
      archivo.setEstado(EstadoArchivo.ERROR);
      archivoRepository.guardar(archivo);
      procesamiento.marcarConError("Error durante procesamiento: " + e.getMessage());
      return procesamiento.toEstadoProcesamiento();
    }
  }

  /** Configura la cadena de validadores */
  private ValidadorLinea configurarCadenaValidadores() {
    logger.debug("Configurando cadena de validadores...");

    // Configurar cadena de responsabilidad
    ValidadorLinea cadena =
        validadorFormato.setSiguiente(validadorCliente.setSiguiente(validadorProductos));

    logger.debug(
        "Cadena de validadores configurada: ValidadorFormato -> ValidadorCliente -> ValidadorProductos");
    return cadena;
  }

  /** Convierte el contenido del archivo en lista de líneas */
  private List<String> obtenerLineasArchivo(String contenido) {
    List<String> lineas = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new StringReader(contenido))) {
      String linea;
      while ((linea = reader.readLine()) != null) {
        lineas.add(linea);
      }
      logger.debug("Archivo convertido a {} líneas", lineas.size());
    } catch (Exception e) {
      logger.error("Error al leer líneas del archivo", e);
      throw new RuntimeException("Error al leer líneas del archivo", e);
    }
    return lineas;
  }

  /** Guarda lotes de descuentos y errores */
  private void guardarLotes(List<DescuentoValidado> descuentos, List<ErrorProcesamiento> errores) {
    try {
      if (!descuentos.isEmpty()) {
        descuentoRepository.guardarLote(descuentos);
        logger.debug("Guardado lote de {} descuentos", descuentos.size());
      }

      if (!errores.isEmpty()) {
        errorRepository.guardarLote(errores);
        logger.debug("Guardado lote de {} errores", errores.size());
      }
    } catch (Exception e) {
      logger.error(
          "Error al guardar lotes: {} descuentos, {} errores",
          descuentos.size(),
          errores.size(),
          e);
      throw new RuntimeException("Error al guardar lotes en base de datos", e);
    }
  }

  /** Guarda errores de estructura del archivo */
  private void guardarErroresEstructura(List<ErrorValidacion> errores, Long archivoId) {
    try {
      List<ErrorProcesamiento> erroresProcesamiento =
          errores.stream()
              .map(
                  error ->
                      ErrorProcesamiento.deEstructuraArchivo(
                          archivoId, error.getTipoError(), error.getDescripcion()))
              .toList();

      errorRepository.guardarLote(erroresProcesamiento);
      logger.debug(
          "Guardados {} errores de estructura para archivo {}",
          erroresProcesamiento.size(),
          archivoId);
    } catch (Exception e) {
      logger.error("Error al guardar errores de estructura para archivo {}", archivoId, e);
      throw new RuntimeException("Error al guardar errores de estructura", e);
    }
  }

  /** Obtiene ID de cliente como fallback */
  private Integer obtenerClienteIdFallback(String cedula) {
    try {
      return clienteService
          .buscarPorCedula(cedula)
          .map(ClienteService.ClienteInfo::getId)
          .orElse(null);
    } catch (Exception e) {
      logger.warn("No se pudo obtener ID de cliente para {}: {}", cedula, e.getMessage());
      return null;
    }
  }

  /** Actualiza el estado del archivo con las estadísticas del procesamiento */
  private void actualizarEstadoArchivo(
      ArchivoDescuentos archivo, ProcesamientoDescuentos procesamiento) {
    try {
      // Actualizar estadísticas del archivo
      archivo.setTotalRegistros(procesamiento.getTotalLineas());
      archivo.setRegistrosProcesados(procesamiento.getLineasProcesadas());
      archivo.setDiferenciasEncontradas(procesamiento.getLineasConError());
      archivo.setEstado(procesamiento.getEstado());

      archivoRepository.guardar(archivo);
      logger.info(
          "Estado del archivo {} actualizado: {} registros procesados, {} errores",
          archivo.getId(),
          procesamiento.getLineasProcesadas(),
          procesamiento.getLineasConError());
    } catch (Exception e) {
      logger.error("Error al actualizar estado del archivo {}", archivo.getId(), e);
      throw new RuntimeException("Error al actualizar estado del archivo", e);
    }
  }

  /** Marca un archivo con estado de error */
  private void marcarArchivoConError(Long archivoId, String mensajeError) {
    try {
      ArchivoDescuentos archivo = archivoRepository.buscarPorId(archivoId).orElse(null);
      if (archivo != null) {
        archivo.setEstado(EstadoArchivo.ERROR);
        archivoRepository.guardar(archivo);
        logger.info("Archivo {} marcado con estado ERROR: {}", archivoId, mensajeError);
      }
    } catch (Exception e) {
      logger.error("Error al marcar archivo {} con error", archivoId, e);
    }
  }

  /** Método de utilidad para obtener estadísticas de procesamiento */
  public EstadisticasProcesamiento obtenerEstadisticas(Long archivoId) {
    try {
      ArchivoDescuentos archivo = archivoRepository.buscarPorId(archivoId).orElse(null);
      if (archivo == null) {
        return null;
      }

      return new EstadisticasProcesamiento(
          archivoId,
          archivo.getTotalRegistros() != null ? archivo.getTotalRegistros() : 0,
          archivo.getRegistrosProcesados() != null ? archivo.getRegistrosProcesados() : 0,
          archivo.getDiferenciasEncontradas() != null ? archivo.getDiferenciasEncontradas() : 0,
          archivo.getEstado());
    } catch (Exception e) {
      logger.error("Error al obtener estadísticas para archivo {}", archivoId, e);
      return null;
    }
  }

  /**
   * Parsea una línea de texto en campos usando el delimitador CSV (coma por defecto). Si el archivo
   * usa otro delimitador (por ejemplo, punto y coma), ajusta aquí. Maneja comillas para campos con
   * delimitadores internos.
   */
  private String[] parsearLinea(String linea) {
    // Usar una librería robusta para CSV si el formato es complejo (OpenCSV, Apache Commons CSV)
    // Aquí una versión simple para CSV estándar:
    List<String> campos = new ArrayList<>();
    boolean enComillas = false;
    StringBuilder campo = new StringBuilder();
    for (int i = 0; i < linea.length(); i++) {
      char c = linea.charAt(i);
      if (c == '"') {
        enComillas = !enComillas;
      } else if (c == ',' && !enComillas) {
        campos.add(campo.toString().trim());
        campo.setLength(0);
      } else {
        campo.append(c);
      }
    }
    campos.add(campo.toString().trim());
    return campos.toArray(new String[0]);
  }

  /** Clase interna para estadísticas */
  public static class EstadisticasProcesamiento {
    private final Long archivoId;
    private final Integer totalRegistros;
    private final Integer registrosProcesados;
    private final Integer errores;
    private final EstadoArchivo estado;

    public EstadisticasProcesamiento(
        Long archivoId,
        Integer totalRegistros,
        Integer registrosProcesados,
        Integer errores,
        EstadoArchivo estado) {
      this.archivoId = archivoId;
      this.totalRegistros = totalRegistros;
      this.registrosProcesados = registrosProcesados;
      this.errores = errores;
      this.estado = estado;
    }

    // Getters
    public Long getArchivoId() {
      return archivoId;
    }

    public Integer getTotalRegistros() {
      return totalRegistros;
    }

    public Integer getRegistrosProcesados() {
      return registrosProcesados;
    }

    public Integer getErrores() {
      return errores;
    }

    public EstadoArchivo getEstado() {
      return estado;
    }

    public double getPorcentajeProgreso() {
      return totalRegistros > 0 ? (double) registrosProcesados / totalRegistros * 100.0 : 0.0;
    }
  }
}
