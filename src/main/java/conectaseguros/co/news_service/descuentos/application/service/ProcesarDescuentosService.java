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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

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

    @Value("${app.procesamiento.lote-tamaño:1000}")
    private int tamañoLote;

    @Value("${app.procesamiento.headers-esperados:TERCERO,NIT,CODCONCEPTO,CONCEPTO,CODEMPLEADO,Nombre1,Nombre2,Apellido1,Apellido2,COD_UNIDAD_EJECUTORA,VALOR,NOMINA}")
    private String headersEsperados;

    public ProcesarDescuentosService(ArchivoDescuentosRepository archivoRepository,
                                     DescuentoRepository descuentoRepository,
                                     ErrorProcesamientoRepository errorRepository,
                                     ClienteService clienteService,
                                     ValidadorFormatoLinea validadorFormato,
                                     ValidadorCliente validadorCliente,
                                     ValidadorProductos validadorProductos) {
        this.archivoRepository = archivoRepository;
        this.descuentoRepository = descuentoRepository;
        this.errorRepository = errorRepository;
        this.clienteService = clienteService;
        this.validadorFormato = validadorFormato;
        this.validadorCliente = validadorCliente;
        this.validadorProductos = validadorProductos;
    }

    /**
     * Procesa un archivo de descuentos de forma asíncrona
     */
    @Async
    public CompletableFuture<EstadoProcesamiento> procesarArchivo(Long archivoId) {
        logger.info("=== INICIANDO PROCESAMIENTO ASÍNCRONO DE ARCHIVO {} ===", archivoId);

        return CompletableFuture.supplyAsync(() -> {
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
                        "Error inesperado: " + e.getMessage(),
                        java.time.LocalDateTime.now()
                );
            }
        });
    }

    /**
     * Procesa un archivo de descuentos de forma síncrona
     */
    public EstadoProcesamiento procesarArchivoSincrono(Long archivoId) {
        logger.info("=== INICIANDO PROCESAMIENTO SÍNCRONO DE ARCHIVO {} ===", archivoId);

        // Obtener archivo
        ArchivoDescuentos archivo = archivoRepository.buscarPorId(archivoId)
                .orElseThrow(() -> new ArchivoNoEncontradoException("Archivo no encontrado: " + archivoId));

        logger.info("Archivo encontrado: {} - Estado: {}", archivo.getNombreArchivo(), archivo.getEstado());

        // ✅ NUEVO: Cambiar estado a PROCESANDO al inicio
        try {
            archivo.setEstado(EstadoArchivo.PROCESANDO);
            archivoRepository.guardar(archivo);
            logger.info("Estado del archivo {} cambiado a PROCESANDO", archivoId);
        } catch (Exception e) {
            logger.error("Error al cambiar estado a PROCESANDO para archivo {}", archivoId, e);
            throw new RuntimeException("Error al inicializar procesamiento", e);
        }

        ProcesamientoDescuentos procesamiento = new ProcesamientoDescuentos(
                archivoId, archivo.getPeriodo(), archivo.getSeccionalId()
        );

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
                logger.error("Error de estructura en archivo {}: {}", archivoId, validacionEstructura.getDescripcionErrores());
                procesamiento.marcarConError("Error de estructura: " + validacionEstructura.getDescripcionErrores());
                guardarErroresEstructura(validacionEstructura.getErrores(), archivoId);

                // Marcar archivo con error
                archivo.setEstado(EstadoArchivo.ERROR);
                archivoRepository.guardar(archivo);

                return procesamiento.toEstadoProcesamiento();
            }

            // Iniciar procesamiento
            int totalLineasDatos = lineas.size() - 1; // Excluir header
            procesamiento.iniciarProcesamiento(totalLineasDatos);

            logger.info("Procesando {} líneas de datos para archivo {}", totalLineasDatos, archivoId);

            // Procesar líneas en lotes
            List<DescuentoValidado> descuentosLote = new ArrayList<>();
            List<ErrorProcesamiento> erroresLote = new ArrayList<>();

            for (int i = 1; i < lineas.size(); i++) { // Empezar desde 1 para saltar header
                try {
                    String lineaTexto = lineas.get(i);
                    if (lineaTexto.trim().isEmpty()) {
                        logger.debug("Saltando línea vacía en posición {}", i + 1);
                        continue; // Saltar líneas vacías
                    }

                    String[] campos = parsearLinea(lineaTexto);
                    logger.debug("Procesando línea {} con {} campos", i + 1, campos.length);

                    LineaDescuento linea = new LineaDescuento(campos, i + 1);

                    // Validar línea
                    ResultadoValidacion resultado = cadenaValidadores.validar(linea, archivo.getSeccionalId());
                    logger.debug("Validación de línea {}: {}", i + 1, resultado.esValido() ? "EXITOSA" : "CON ERRORES");

                    if (resultado.esValido()) {
                        // Verificar duplicados
                        if (!descuentoRepository.existeDescuentoPeriodo(
                                linea.getCodigoEmpleado(), archivo.getPeriodo(), linea.getCodigoConcepto())) {

                            // Obtener información del cliente
                            ClienteService.ClienteInfo clienteInfo = resultado.getDatosAdicionales(ClienteService.ClienteInfo.class);
                            Integer clienteId = clienteInfo != null ? clienteInfo.getId() :
                                    obtenerClienteIdFallback(linea.getCodigoEmpleado());

                            DescuentoValidado descuento = DescuentoValidado.fromLineaDescuento(
                                    linea, clienteId, archivo.getSeccionalId(), archivo.getPeriodo(), archivoId
                            );
                            descuentosLote.add(descuento);
                            procesamiento.registrarLineaProcesada(true);
                            logger.debug("Línea {} agregada como descuento válido", i + 1);
                        } else {
                            // Duplicado
                            logger.debug("Línea {} es duplicada para cliente {} en período {}",
                                    i + 1, linea.getCodigoEmpleado(), archivo.getPeriodo());
                            ErrorProcesamiento error = ErrorProcesamiento.deLineaDescuento(
                                    archivoId, linea, new ErrorValidacion(
                                            TipoError.DUPLICADO_PERIODO,
                                            String.format("Descuento duplicado para cliente %s en período %s",
                                                    linea.getCodigoEmpleado(), archivo.getPeriodo())
                                    )
                            );
                            erroresLote.add(error);
                            procesamiento.registrarLineaProcesada(false);
                        }
                    } else {
                        // Línea con errores
                        logger.debug("Línea {} tiene {} errores de validación", i + 1, resultado.getErrores().size());
                        for (ErrorValidacion errorValidacion : resultado.getErrores()) {
                            ErrorProcesamiento error = ErrorProcesamiento.deLineaDescuento(
                                    archivoId, linea, errorValidacion
                            );
                            erroresLote.add(error);
                        }
                        procesamiento.registrarLineaProcesada(false);
                    }

                    // Guardar lote si alcanzó el tamaño configurado
                    if (descuentosLote.size() >= tamañoLote || erroresLote.size() >= tamañoLote) {
                        logger.debug("Guardando lote: {} descuentos, {} errores", descuentosLote.size(), erroresLote.size());
                        guardarLotes(descuentosLote, erroresLote);
                        descuentosLote.clear();
                        erroresLote.clear();
                    }

                } catch (Exception e) {
                    logger.error("Error procesando línea {} del archivo {}", i + 1, archivoId, e);
                    ErrorProcesamiento error = new ErrorProcesamiento(
                            archivoId, TipoArchivo.DESCUENTOS, i + 1, null,
                            archivo.getPeriodo(), archivo.getSeccionalId(),
                            TipoError.ERROR_PROCESAMIENTO,
                            "Error inesperado: " + e.getMessage(),
                            lineas.get(i)
                    );
                    erroresLote.add(error);
                    procesamiento.registrarLineaProcesada(false);
                }

                // Log de progreso cada 100 líneas
                if (i % 100 == 0) {
                    logger.info("Procesadas {} de {} líneas ({}%)",
                            procesamiento.getLineasProcesadas(),
                            totalLineasDatos,
                            String.format("%.1f", procesamiento.getPorcentajeProgreso()));
                }
            }

            // Guardar lote final
            logger.info("Guardando lote final: {} descuentos, {} errores", descuentosLote.size(), erroresLote.size());
            guardarLotes(descuentosLote, erroresLote);

            // Completar procesamiento
            procesamiento.completarProcesamiento();

            // Actualizar estado del archivo
            actualizarEstadoArchivo(archivo, procesamiento);

            logger.info("Procesamiento completado para archivo {}. Exitosas: {}, Errores: {}",
                    archivoId, procesamiento.getLineasExitosas(), procesamiento.getLineasConError());

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
     * Configura la cadena de validadores
     */
    private ValidadorLinea configurarCadenaValidadores() {
        logger.debug("Configurando cadena de validadores...");

        // Configurar cadena de responsabilidad
        ValidadorLinea cadena = validadorFormato
                .setSiguiente(validadorCliente
                        .setSiguiente(validadorProductos));

        logger.debug("Cadena de validadores configurada: ValidadorFormato -> ValidadorCliente -> ValidadorProductos");
        return cadena;
    }

    /**
     * Convierte el contenido del archivo en lista de líneas
     */
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

    /**
     * Valida la estructura básica del archivo
     */
    private ResultadoValidacion validarEstructuraArchivo(List<String> lineas, Long archivoId) {
        logger.debug("Validando estructura del archivo {}", archivoId);

        if (lineas.isEmpty()) {
            return ResultadoValidacion.conError(
                    TipoError.ARCHIVO_VACIO,
                    "El archivo está vacío"
            );
        }

        if (lineas.size() < 2) {
            return ResultadoValidacion.conError(
                    TipoError.ARCHIVO_VACIO,
                    "El archivo debe contener al menos una línea de datos además del header"
            );
        }

        // Validar headers
        String header = lineas.get(0);
        String[] headersArchivo = parsearLinea(header);
        String[] headersEsperadosArray = headersEsperados.split(",");

        logger.debug("Headers del archivo: {}", (Object) headersArchivo);
        logger.debug("Headers esperados: {}", (Object) headersEsperadosArray);

        if (headersArchivo.length != headersEsperadosArray.length) {
            return ResultadoValidacion.conError(
                    TipoError.HEADERS_INCORRECTOS,
                    String.format("Se esperan %d columnas, se encontraron %d",
                            headersEsperadosArray.length, headersArchivo.length)
            );
        }

        // Validar nombres de headers (case-insensitive)
        for (int i = 0; i < headersEsperadosArray.length; i++) {
            if (!headersArchivo[i].trim().equalsIgnoreCase(headersEsperadosArray[i].trim())) {
                return ResultadoValidacion.conError(
                        TipoError.HEADERS_INCORRECTOS,
                        String.format("Header en posición %d: se esperaba '%s', se encontró '%s'",
                                i + 1, headersEsperadosArray[i], headersArchivo[i])
                );
            }
        }

        logger.debug("Estructura del archivo {} validada correctamente", archivoId);
        return ResultadoValidacion.exitoso();
    }

    /**
     * Parsea una línea CSV simple
     */
    private String[] parsearLinea(String linea) {
        // Parser simple para CSV - en producción usar librería especializada como OpenCSV
        String[] campos = linea.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

        // Limpiar comillas de los campos
        for (int i = 0; i < campos.length; i++) {
            campos[i] = campos[i].trim();
            if (campos[i].startsWith("\"") && campos[i].endsWith("\"")) {
                campos[i] = campos[i].substring(1, campos[i].length() - 1);
            }
        }

        return campos;
    }

    /**
     * Guarda lotes de descuentos y errores
     */
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
            logger.error("Error al guardar lotes: {} descuentos, {} errores", descuentos.size(), errores.size(), e);
            throw new RuntimeException("Error al guardar lotes en base de datos", e);
        }
    }

    /**
     * Guarda errores de estructura del archivo
     */
    private void guardarErroresEstructura(List<ErrorValidacion> errores, Long archivoId) {
        try {
            List<ErrorProcesamiento> erroresProcesamiento = errores.stream()
                    .map(error -> ErrorProcesamiento.deEstructuraArchivo(archivoId, error.getTipoError(), error.getDescripcion()))
                    .toList();

            errorRepository.guardarLote(erroresProcesamiento);
            logger.debug("Guardados {} errores de estructura para archivo {}", erroresProcesamiento.size(), archivoId);
        } catch (Exception e) {
            logger.error("Error al guardar errores de estructura para archivo {}", archivoId, e);
            throw new RuntimeException("Error al guardar errores de estructura", e);
        }
    }

    /**
     * Obtiene ID de cliente como fallback
     */
    private Integer obtenerClienteIdFallback(String cedula) {
        try {
            return clienteService.buscarPorCedula(cedula)
                    .map(ClienteService.ClienteInfo::getId)
                    .orElse(null);
        } catch (Exception e) {
            logger.warn("No se pudo obtener ID de cliente para {}: {}", cedula, e.getMessage());
            return null;
        }
    }

    /**
     * Actualiza el estado del archivo con las estadísticas del procesamiento
     */
    private void actualizarEstadoArchivo(ArchivoDescuentos archivo, ProcesamientoDescuentos procesamiento) {
        try {
            // Actualizar estadísticas del archivo
            archivo.setTotalRegistros(procesamiento.getTotalLineas());
            archivo.setRegistrosProcesados(procesamiento.getLineasProcesadas());
            archivo.setDiferenciasEncontradas(procesamiento.getLineasConError());
            archivo.setEstado(procesamiento.getEstado());

            archivoRepository.guardar(archivo);
            logger.info("Estado del archivo {} actualizado: {} registros procesados, {} errores",
                    archivo.getId(), procesamiento.getLineasProcesadas(), procesamiento.getLineasConError());
        } catch (Exception e) {
            logger.error("Error al actualizar estado del archivo {}", archivo.getId(), e);
            throw new RuntimeException("Error al actualizar estado del archivo", e);
        }
    }

    /**
     * Marca un archivo con estado de error
     */
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

    /**
     * Método de utilidad para obtener estadísticas de procesamiento
     */
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
                    archivo.getEstado()
            );
        } catch (Exception e) {
            logger.error("Error al obtener estadísticas para archivo {}", archivoId, e);
            return null;
        }
    }

    /**
     * Clase interna para estadísticas
     */
    public static class EstadisticasProcesamiento {
        private final Long archivoId;
        private final Integer totalRegistros;
        private final Integer registrosProcesados;
        private final Integer errores;
        private final EstadoArchivo estado;

        public EstadisticasProcesamiento(Long archivoId, Integer totalRegistros,
                                         Integer registrosProcesados, Integer errores, EstadoArchivo estado) {
            this.archivoId = archivoId;
            this.totalRegistros = totalRegistros;
            this.registrosProcesados = registrosProcesados;
            this.errores = errores;
            this.estado = estado;
        }

        // Getters
        public Long getArchivoId() { return archivoId; }
        public Integer getTotalRegistros() { return totalRegistros; }
        public Integer getRegistrosProcesados() { return registrosProcesados; }
        public Integer getErrores() { return errores; }
        public EstadoArchivo getEstado() { return estado; }

        public double getPorcentajeProgreso() {
            return totalRegistros > 0 ? (double) registrosProcesados / totalRegistros * 100.0 : 0.0;
        }
    }
}