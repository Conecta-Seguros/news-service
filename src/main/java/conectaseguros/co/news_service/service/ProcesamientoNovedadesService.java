package conectaseguros.co.news_service.service;

import conectaseguros.co.news_service.dto.MensajeProcesamientoDTO;
import conectaseguros.co.news_service.dto.NovedadArchivoDTO;
import conectaseguros.co.news_service.dto.ProcesamientoArchivoDTO;
import conectaseguros.co.news_service.dto.ResultadoProcesamientoDTO;
import conectaseguros.co.news_service.model.Novedad;
import conectaseguros.co.news_service.model.ProcesamientoArchivo;
import conectaseguros.co.news_service.repository.NovedadRepository;
import conectaseguros.co.news_service.repository.ProcesamientoArchivoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProcesamientoNovedadesService {

    private final NovedadRepository novedadRepository;
    private final ProcesamientoArchivoRepository procesamientoRepository;
    private final ValidacionClienteService validacionService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Historia de Usuario: Procesar archivo de novedades con validaciones completas
     */
    public ResultadoProcesamientoDTO procesarArchivoNovedades(MultipartFile archivo, String usuarioProcesamiento) {
        // Crear registro de procesamiento
        ProcesamientoArchivo procesamiento = ProcesamientoArchivo.builder()
                .nombreArchivo(archivo.getOriginalFilename())
                .estado("PROCESANDO")
                .usuarioProcesamiento(usuarioProcesamiento)
                .totalRegistros(0)
                .registrosExitosos(0)
                .registrosErrores(0)
                .build();

        procesamiento = procesamientoRepository.save(procesamiento);

        List<MensajeProcesamientoDTO> mensajesExito = new ArrayList<>();
        List<MensajeProcesamientoDTO> mensajesError = new ArrayList<>();

        int exitosos = 0;
        int errores = 0;
        try {
            // Leer y procesar archivo
            List<NovedadArchivoDTO> novedades = leerArchivoCSV(archivo);
            procesamiento.setTotalRegistros(novedades.size());

            exitosos = 0;
            errores = 0;

            for (NovedadArchivoDTO novedadDto : novedades) {
                try {
                    // Validar y procesar cada registro
                    MensajeProcesamientoDTO resultado = procesarRegistroNovedad(novedadDto, archivo.getOriginalFilename(), usuarioProcesamiento);

                    if ("EXITO".equals(resultado.getTipoMensaje())) {
                        mensajesExito.add(resultado);
                        exitosos++;
                    } else {
                        mensajesError.add(resultado);
                        errores++;
                    }

                } catch (Exception e) {
                    mensajesError.add(MensajeProcesamientoDTO.builder()
                            .numeroLinea(novedadDto.getNumeroLinea())
                            .cedulaCliente(novedadDto.getCedulaCliente())
                            .numeroPoliza(novedadDto.getNumeroPoliza())
                            .tipoMensaje("ERROR")
                            .mensaje("Error inesperado al procesar registro")
                            .detalle(e.getMessage())
                            .build());
                    errores++;
                }
            }

            // Actualizar registro de procesamiento
            procesamiento.setRegistrosExitosos(exitosos);
            procesamiento.setRegistrosErrores(errores);
            procesamiento.setEstado("COMPLETADO");
            procesamiento.setFechaFin(LocalDateTime.now());

        } catch (Exception e) {
            procesamiento.setEstado("ERROR");
            procesamiento.setFechaFin(LocalDateTime.now());
            procesamiento.setObservaciones("Error general: " + e.getMessage());

            mensajesError.add(MensajeProcesamientoDTO.builder()
                    .numeroLinea(0)
                    .tipoMensaje("ERROR")
                    .mensaje("Error al procesar archivo")
                    .detalle(e.getMessage())
                    .build());
        }

        procesamientoRepository.save(procesamiento);

        return ResultadoProcesamientoDTO.builder()
                .nombreArchivo(procesamiento.getNombreArchivo())
                .totalRegistros(procesamiento.getTotalRegistros())
                .registrosExitosos(procesamiento.getRegistrosExitosos())
                .registrosErrores(procesamiento.getRegistrosErrores())
                .estado(procesamiento.getEstado())
                .fechaInicio(procesamiento.getFechaInicio())
                .fechaFin(procesamiento.getFechaFin())
                .duracionProcesamiento(calcularDuracion(procesamiento.getFechaInicio(), procesamiento.getFechaFin()))
                .mensajesExito(mensajesExito)
                .mensajesError(mensajesError)
                .resumenProcesamiento(generarResumenProcesamiento(procesamiento, exitosos, errores))
                .build();
    }

    /**
     * Procesar un registro individual con todas las validaciones
     */
    private MensajeProcesamientoDTO procesarRegistroNovedad(NovedadArchivoDTO dto, String archivoOrigen, String usuario) {
        try {
            // VALIDACIÓN 1: Cliente existe
            if (!validacionService.validarClienteExiste(dto.getCedulaCliente())) {
                return MensajeProcesamientoDTO.builder()
                        .numeroLinea(dto.getNumeroLinea())
                        .cedulaCliente(dto.getCedulaCliente())
                        .numeroPoliza(dto.getNumeroPoliza())
                        .tipoMensaje("ERROR")
                        .mensaje("Cliente no existe")
                        .detalle("No se encontró un cliente con la cédula: " + dto.getCedulaCliente())
                        .build();
            }

            String nombreCliente = validacionService.obtenerNombreCompletoCliente(dto.getCedulaCliente());

            // VALIDACIÓN 2: Cliente tiene productos activos
            if (!validacionService.clienteTieneProductosActivos(dto.getCedulaCliente())) {
                return MensajeProcesamientoDTO.builder()
                        .numeroLinea(dto.getNumeroLinea())
                        .cedulaCliente(dto.getCedulaCliente())
                        .numeroPoliza(dto.getNumeroPoliza())
                        .tipoMensaje("ERROR")
                        .mensaje("Cliente sin productos activos")
                        .detalle("El cliente " + nombreCliente + " no tiene productos activos")
                        .nombreCliente(nombreCliente)
                        .build();
            }

            // VALIDACIÓN 3: Producto específico existe y está activo
            if (!validacionService.validarPolizaActivaParaCliente(dto.getCedulaCliente(), dto.getNumeroPoliza())) {
                return MensajeProcesamientoDTO.builder()
                        .numeroLinea(dto.getNumeroLinea())
                        .cedulaCliente(dto.getCedulaCliente())
                        .numeroPoliza(dto.getNumeroPoliza())
                        .tipoMensaje("ERROR")
                        .mensaje("Póliza no encontrada o inactiva")
                        .detalle("No se encontró la póliza " + dto.getNumeroPoliza() +
                                " activa para el cliente " + nombreCliente)
                        .nombreCliente(nombreCliente)
                        .build();
            }

            // VALIDACIÓN 4: Cliente pertenece a la seccional
            if (!validacionService.validarSeccional(dto.getCedulaCliente(), "SECCIONAL_DEFAULT")) {
                return MensajeProcesamientoDTO.builder()
                        .numeroLinea(dto.getNumeroLinea())
                        .cedulaCliente(dto.getCedulaCliente())
                        .numeroPoliza(dto.getNumeroPoliza())
                        .tipoMensaje("ERROR")
                        .mensaje("Cliente no pertenece a la seccional")
                        .detalle("El cliente no pertenece a la seccional requerida")
                        .nombreCliente(nombreCliente)
                        .build();
            }

            // VALIDACIÓN 5: Datos de la novedad son válidos
            String validacionDatos = validarDatosNovedad(dto);
            if (validacionDatos != null) {
                return MensajeProcesamientoDTO.builder()
                        .numeroLinea(dto.getNumeroLinea())
                        .cedulaCliente(dto.getCedulaCliente())
                        .numeroPoliza(dto.getNumeroPoliza())
                        .tipoMensaje("ERROR")
                        .mensaje("Datos de novedad inválidos")
                        .detalle(validacionDatos)
                        .nombreCliente(nombreCliente)
                        .build();
            }

            // VALIDACIÓN 6: No existe novedad duplicada
            if (novedadRepository.existsByCedulaClienteAndNumeroPolizaAndTipoNovedad(
                    dto.getCedulaCliente(), dto.getNumeroPoliza(), dto.getTipoNovedad())) {
                return MensajeProcesamientoDTO.builder()
                        .numeroLinea(dto.getNumeroLinea())
                        .cedulaCliente(dto.getCedulaCliente())
                        .numeroPoliza(dto.getNumeroPoliza())
                        .tipoMensaje("ERROR")
                        .mensaje("Novedad duplicada")
                        .detalle("Ya existe una novedad del tipo " + dto.getTipoNovedad() +
                                " para este cliente y póliza")
                        .nombreCliente(nombreCliente)
                        .build();
            }

            // CREAR LA NOVEDAD
            Long clienteId = validacionService.obtenerClienteId(dto.getCedulaCliente());
            String nombreProducto = validacionService.obtenerNombreProducto(dto.getNumeroPoliza());

            Novedad novedad = Novedad.builder()
                    .clienteId(clienteId)
                    .cedulaCliente(dto.getCedulaCliente())
                    .numeroPoliza(dto.getNumeroPoliza())
                    .tipoNovedad(dto.getTipoNovedad())
                    .valor(dto.getValor())
                    .porcentaje(dto.getPorcentaje())
                    .descripcion(dto.getDescripcion())
                    .fechaAplicacion(dto.getFechaAplicacion())
                    .fechaVencimiento(dto.getFechaVencimiento())
                    .estado("ACTIVO")
                    .archivoOrigen(archivoOrigen)
                    .numeroLinea(dto.getNumeroLinea())
                    .usuarioCreacion(usuario)
                    .build();

            novedadRepository.save(novedad);

            // MENSAJE DE ÉXITO
            return MensajeProcesamientoDTO.builder()
                    .numeroLinea(dto.getNumeroLinea())
                    .cedulaCliente(dto.getCedulaCliente())
                    .numeroPoliza(dto.getNumeroPoliza())
                    .tipoMensaje("EXITO")
                    .mensaje("Novedad procesada exitosamente")
                    .detalle("Tipo: " + dto.getTipoNovedad() + ", Valor: $" + dto.getValor() +
                            (dto.getPorcentaje() != null ? ", Porcentaje: " + dto.getPorcentaje() + "%" : ""))
                    .nombreCliente(nombreCliente)
                    .productoNombre(nombreProducto)
                    .build();

        } catch (Exception e) {
            return MensajeProcesamientoDTO.builder()
                    .numeroLinea(dto.getNumeroLinea())
                    .cedulaCliente(dto.getCedulaCliente())
                    .numeroPoliza(dto.getNumeroPoliza())
                    .tipoMensaje("ERROR")
                    .mensaje("Error al procesar novedad")
                    .detalle(e.getMessage())
                    .build();
        }
    }

    /**
     * Leer archivo CSV y convertir a DTOs
     */
    private List<NovedadArchivoDTO> leerArchivoCSV(MultipartFile archivo) throws Exception {
        List<NovedadArchivoDTO> novedades = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(archivo.getInputStream()))) {
            String linea;
            int numeroLinea = 0;

            // Saltar header si existe
            reader.readLine();
            numeroLinea++;

            while ((linea = reader.readLine()) != null) {
                numeroLinea++;

                if (linea.trim().isEmpty()) continue;

                String[] campos = linea.split(",");

                if (campos.length < 7) {
                    throw new IllegalArgumentException("Línea " + numeroLinea + ": Formato inválido, se esperan al menos 7 campos");
                }

                try {
                    NovedadArchivoDTO novedad = NovedadArchivoDTO.builder()
                            .cedulaCliente(campos[0].trim())
                            .numeroPoliza(campos[1].trim())
                            .tipoNovedad(campos[2].trim())
                            .valor(new BigDecimal(campos[3].trim()))
                            .porcentaje(campos[4].trim().isEmpty() ? null : new BigDecimal(campos[4].trim()))
                            .descripcion(campos[5].trim())
                            .fechaAplicacion(LocalDate.parse(campos[6].trim(), DATE_FORMATTER))
                            .fechaVencimiento(campos.length > 7 && !campos[7].trim().isEmpty() ?
                                    LocalDate.parse(campos[7].trim(), DATE_FORMATTER) : null)
                            .numeroLinea(numeroLinea)
                            .build();

                    novedades.add(novedad);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Línea " + numeroLinea + ": Error al parsear datos - " + e.getMessage());
                }
            }
        }

        return novedades;
    }

    /**
     * Validar datos específicos de la novedad
     */
    private String validarDatosNovedad(NovedadArchivoDTO dto) {
        if (dto.getTipoNovedad() == null || dto.getTipoNovedad().trim().isEmpty()) {
            return "Tipo de novedad es obligatorio";
        }

        if (dto.getValor() == null || dto.getValor().compareTo(BigDecimal.ZERO) < 0) {
            return "Valor debe ser mayor o igual a cero";
        }

        if (dto.getPorcentaje() != null &&
                (dto.getPorcentaje().compareTo(BigDecimal.ZERO) < 0 || dto.getPorcentaje().compareTo(new BigDecimal("100")) > 0)) {
            return "Porcentaje debe estar entre 0 y 100";
        }

        if (dto.getFechaAplicacion() == null) {
            return "Fecha de aplicación es obligatoria";
        }

        if (dto.getFechaAplicacion().isBefore(LocalDate.now())) {
            return "Fecha de aplicación no puede ser anterior a hoy";
        }

        if (dto.getFechaVencimiento() != null && dto.getFechaVencimiento().isBefore(dto.getFechaAplicacion())) {
            return "Fecha de vencimiento no puede ser anterior a la fecha de aplicación";
        }

        return null; // Sin errores
    }

    /**
     * Generar resumen del procesamiento
     */
    private String generarResumenProcesamiento(ProcesamientoArchivo procesamiento, int exitosos, int errores) {
        StringBuilder resumen = new StringBuilder();
        resumen.append("RESUMEN DE PROCESAMIENTO\n");
        resumen.append("========================\n");
        resumen.append("Archivo: ").append(procesamiento.getNombreArchivo()).append("\n");
        resumen.append("Total registros: ").append(procesamiento.getTotalRegistros()).append("\n");
        resumen.append("Procesados exitosamente: ").append(exitosos).append("\n");
        resumen.append("Errores: ").append(errores).append("\n");
        resumen.append("Estado: ").append(procesamiento.getEstado()).append("\n");
        resumen.append("Usuario: ").append(procesamiento.getUsuarioProcesamiento()).append("\n");
        resumen.append("Duración: ").append(calcularDuracion(procesamiento.getFechaInicio(), procesamiento.getFechaFin()));

        return resumen.toString();
    }

    private String calcularDuracion(LocalDateTime inicio, LocalDateTime fin) {
        if (fin == null) return "En proceso...";

        Duration duracion = Duration.between(inicio, fin);
        long segundos = duracion.getSeconds();

        if (segundos < 60) {
            return segundos + " segundos";
        } else if (segundos < 3600) {
            return (segundos / 60) + " minutos " + (segundos % 60) + " segundos";
        } else {
            return (segundos / 3600) + " horas " + ((segundos % 3600) / 60) + " minutos";
        }
    }

    /**
     * Obtener historial de procesamientos
     */
    public List<ProcesamientoArchivoDTO> obtenerHistorialProcesamientos() {
        return procesamientoRepository.findTop10ByOrderByFechaInicioDesc()
                .stream()
                .map(this::toProcesamientoDTO)
                .collect(Collectors.toList());
    }

    private ProcesamientoArchivoDTO toProcesamientoDTO(ProcesamientoArchivo p) {
        return ProcesamientoArchivoDTO.builder()
                .id(p.getId())
                .nombreArchivo(p.getNombreArchivo())
                .totalRegistros(p.getTotalRegistros())
                .registrosExitosos(p.getRegistrosExitosos())
                .registrosErrores(p.getRegistrosErrores())
                .estado(p.getEstado())
                .fechaInicio(p.getFechaInicio())
                .fechaFin(p.getFechaFin())
                .usuarioProcesamiento(p.getUsuarioProcesamiento())
                .observaciones(p.getObservaciones())
                .duracionProcesamiento(calcularDuracion(p.getFechaInicio(), p.getFechaFin()))
                .build();
    }
}