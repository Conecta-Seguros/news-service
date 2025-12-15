package conectaseguros.co.news_service.descuentos.application.service;

import conectaseguros.co.news_service.descuentos.domain.model.DescuentoValidado;
import conectaseguros.co.news_service.descuentos.domain.port.output.ProductoService;
import conectaseguros.co.news_service.descuentos.infrastructure.output.payment.PagoNominaRequestDTO;
import conectaseguros.co.news_service.descuentos.infrastructure.output.payment.PagoResponseDTO;
import conectaseguros.co.news_service.descuentos.infrastructure.output.payment.PaymentApiResponse;
import conectaseguros.co.news_service.descuentos.infrastructure.output.payment.PaymentsServiceClient;
import conectaseguros.co.news_service.descuentos.infrastructure.output.product.ProductoInfo;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Servicio para registrar pagos de nómina en el sistema de pagos. Orquesta la obtención de datos
 * desde products-service y el registro en payments-service.
 */
@Service
public class RegistrarPagoNominaService {

  private static final Logger logger = LoggerFactory.getLogger(RegistrarPagoNominaService.class);

  private final PaymentsServiceClient paymentsServiceClient;
  private final ProductoService productoService;

  public RegistrarPagoNominaService(
      PaymentsServiceClient paymentsServiceClient, ProductoService productoService) {
    this.paymentsServiceClient = paymentsServiceClient;
    this.productoService = productoService;
  }

  /**
   * Registra pagos de nómina de forma asíncrona para una lista de descuentos validados.
   *
   * @param descuentos Lista de descuentos validados
   * @param archivoId ID del archivo de nómina
   * @return CompletableFuture con resumen del procesamiento
   */
  @Async
  public CompletableFuture<ResumenRegistroPagos> registrarPagosAsync(
      List<DescuentoValidado> descuentos, Long archivoId) {
    logger.info(
        "=== INICIANDO REGISTRO ASÍNCRONO DE PAGOS PARA {} DESCUENTOS - ARCHIVO {} ===",
        descuentos.size(),
        archivoId);

    ResumenRegistroPagos resumen = new ResumenRegistroPagos();
    resumen.totalProcesados = descuentos.size();

    for (DescuentoValidado descuento : descuentos) {
      try {
        registrarPagoNomina(descuento, archivoId);
        resumen.exitosos++;
        logger.debug(
            "✅ Pago registrado exitosamente - Cliente: {}, Periodo: {}",
            descuento.getCedulaCliente(),
            descuento.getPeriodo());
      } catch (Exception e) {
        resumen.fallidos++;
        resumen.errores.add(
            String.format(
                "Cliente %s (Periodo %s): %s",
                descuento.getCedulaCliente(), descuento.getPeriodo(), e.getMessage()));
        logger.error(
            "❌ Error al registrar pago para cliente {} periodo {}: {}",
            descuento.getCedulaCliente(),
            descuento.getPeriodo(),
            e.getMessage(),
            e);
      }

      // Log de progreso cada 100 pagos
      if ((resumen.exitosos + resumen.fallidos) % 100 == 0) {
        logger.info(
            "Progreso registro de pagos: {}/{} procesados ({} exitosos, {} fallidos)",
            resumen.exitosos + resumen.fallidos,
            descuentos.size(),
            resumen.exitosos,
            resumen.fallidos);
      }
    }

    logger.info(
        "=== REGISTRO DE PAGOS COMPLETADO - Exitosos: {}, Fallidos: {}, Total: {} ===",
        resumen.exitosos,
        resumen.fallidos,
        resumen.totalProcesados);

    return CompletableFuture.completedFuture(resumen);
  }

  /**
   * Registra un pago de nómina individual en el sistema de pagos.
   *
   * @param descuento Descuento validado
   * @param archivoId ID del archivo de nómina
   * @return Respuesta del registro de pago
   */
  public PagoResponseDTO registrarPagoNomina(DescuentoValidado descuento, Long archivoId) {
    logger.debug(
        "Registrando pago para cliente {} - Periodo: {}",
        descuento.getCedulaCliente(),
        descuento.getPeriodo());

    // 1. Obtener productos activos del cliente con plan de pagos
    List<ProductoInfo> productosActivos =
        productoService.obtenerProductosActivosPorCedula(descuento.getCedulaCliente());

    if (productosActivos.isEmpty()) {
      throw new PagoRegistroException(
          "No se encontraron productos activos para el cliente: "
              + descuento.getCedulaCliente());
    }

    // 2. Parsear el periodo (formato "YYYYMM" a "YYYY-MM")
    String periodoFormateado = formatearPeriodo(descuento.getPeriodo());
    Integer periodoYear = extraerYear(descuento.getPeriodo());
    Integer periodoMonth = extraerMonth(descuento.getPeriodo());

    logger.debug(
        "Periodo parseado: {} -> {} (Year: {}, Month: {})",
        descuento.getPeriodo(),
        periodoFormateado,
        periodoYear,
        periodoMonth);

    // 3. Buscar el producto que tiene pago para este periodo
    Optional<ProductoInfo> productoConPago =
        productosActivos.stream()
            .filter(p -> p.tienePagoValidoParaPeriodo(periodoFormateado))
            .findFirst();

    if (productoConPago.isEmpty()) {
      logger.warn(
          "No se encontró producto con pago válido para periodo {} - Cliente: {}. Productos disponibles: {}",
          periodoFormateado,
          descuento.getCedulaCliente(),
          productosActivos.stream()
              .map(p -> "ID:" + p.getId() + " Periodos:" + p.getPeriodosDisponibles())
              .collect(Collectors.joining(", ")));

      throw new PagoRegistroException(
          String.format(
              "No se encontró producto con pago válido para el periodo %s del cliente %s",
              periodoFormateado, descuento.getCedulaCliente()));
    }

    ProductoInfo producto = productoConPago.get();

    // 4. Calcular número de cuota desde el plan de pagos
    Integer numeroCuota = calcularNumeroCuota(producto.getPlanDePagos(), periodoFormateado);

    logger.debug(
        "Producto encontrado - ID: {}, PolizaID: {}, NumeroCuota: {}, Valor: {}",
        producto.getId(),
        producto.getPolizaId(),
        numeroCuota,
        descuento.getValor());

    // 5. Construir request para payments-service
    PagoNominaRequestDTO request =
        PagoNominaRequestDTO.builder()
            .polizaId(producto.getPolizaId().longValue())
            .productoClienteId(producto.getId().longValue())
            .clienteId(descuento.getClienteId().longValue())
            .archivoNominaId(archivoId)
            .valorEsperado(descuento.getValorEsperado())
            .valorPagado(descuento.getValor())
            .periodoYear(periodoYear)
            .periodoMonth(periodoMonth)
            .numeroCuota(numeroCuota)
            .fechaPago(descuento.getFecha())
            .observaciones(generarObservaciones(descuento))
            .build();

    logger.debug("Request para payments-service: {}", request);

    // 6. Llamar a payments-service para registrar el pago
    try {
      PaymentApiResponse<PagoResponseDTO> response =
          paymentsServiceClient.registrarPagoNomina(request);

      logger.info(
          "✅ Pago registrado exitosamente - ID: {}, Cliente: {}, Periodo: {}",
          response.getData().getId(),
          descuento.getCedulaCliente(),
          descuento.getPeriodo());

      return response.getData();

    } catch (Exception e) {
      logger.error(
          "❌ Error al llamar a payments-service para cliente {} periodo {}: {}",
          descuento.getCedulaCliente(),
          descuento.getPeriodo(),
          e.getMessage());
      throw new PagoRegistroException(
          "Error al registrar pago en payments-service: " + e.getMessage(), e);
    }
  }

  /**
   * Formatea un periodo de formato "YYYYMM" a "YYYY-MM".
   *
   * @param periodo Periodo en formato "YYYYMM" (ej: "202512")
   * @return Periodo en formato "YYYY-MM" (ej: "2025-12")
   */
  private String formatearPeriodo(String periodo) {
    if (periodo == null || periodo.length() != 6) {
      throw new IllegalArgumentException(
          "Periodo inválido. Debe tener formato YYYYMM: " + periodo);
    }

    String year = periodo.substring(0, 4);
    String month = periodo.substring(4, 6);

    return year + "-" + month;
  }

  /**
   * Extrae el año de un periodo en formato "YYYYMM".
   *
   * @param periodo Periodo en formato "YYYYMM"
   * @return Año como Integer
   */
  private Integer extraerYear(String periodo) {
    if (periodo == null || periodo.length() != 6) {
      throw new IllegalArgumentException(
          "Periodo inválido. Debe tener formato YYYYMM: " + periodo);
    }
    return Integer.parseInt(periodo.substring(0, 4));
  }

  /**
   * Extrae el mes de un periodo en formato "YYYYMM".
   *
   * @param periodo Periodo en formato "YYYYMM"
   * @return Mes como Integer
   */
  private Integer extraerMonth(String periodo) {
    if (periodo == null || periodo.length() != 6) {
      throw new IllegalArgumentException(
          "Periodo inválido. Debe tener formato YYYYMM: " + periodo);
    }
    return Integer.parseInt(periodo.substring(4, 6));
  }

  /**
   * Calcula el número de cuota basándose en el plan de pagos y el periodo específico.
   *
   * @param planDePagos Mapa con periodos y valores
   * @param periodo Periodo a buscar (formato "YYYY-MM")
   * @return Número de cuota (1-based)
   */
  private Integer calcularNumeroCuota(Map<String, BigDecimal> planDePagos, String periodo) {
    if (planDePagos == null || planDePagos.isEmpty()) {
      logger.warn("Plan de pagos vacío, retornando numeroCuota = 1");
      return 1;
    }

    // Ordenar periodos cronológicamente
    List<String> periodosOrdenados =
        planDePagos.keySet().stream().sorted().collect(Collectors.toList());

    logger.debug("Periodos ordenados: {}", periodosOrdenados);

    // Buscar la posición del periodo (1-based)
    int posicion = periodosOrdenados.indexOf(periodo);

    if (posicion == -1) {
      logger.warn(
          "Periodo {} no encontrado en plan de pagos: {}. Retornando numeroCuota = 1",
          periodo,
          periodosOrdenados);
      return 1;
    }

    Integer numeroCuota = posicion + 1; // 1-based index
    logger.debug("Número de cuota calculado: {} para periodo {}", numeroCuota, periodo);

    return numeroCuota;
  }

  /**
   * Genera observaciones para el pago basándose en el estado del descuento.
   *
   * @param descuento Descuento validado
   * @return Observaciones
   */
  private String generarObservaciones(DescuentoValidado descuento) {
    String estado = descuento.getEstado();
    BigDecimal diferencia = descuento.getDiferencia();

    if ("CONCILIADO".equals(estado)) {
      return String.format(
          "Pago conciliado - Archivo ID: %d, Estado: %s",
          descuento.getArchivoId(), descuento.getEstado());
    } else if ("EXCEDENTE".equals(estado)) {
      return String.format(
          "Pago con excedente de %s - Archivo ID: %d, Estado: %s",
          diferencia, descuento.getArchivoId(), descuento.getEstado());
    } else if ("FALTANTE".equals(estado)) {
      return String.format(
          "Pago con faltante de %s - Archivo ID: %d, Estado: %s",
          diferencia.abs(), descuento.getArchivoId(), descuento.getEstado());
    }

    return String.format(
        "Pago de nómina - Archivo ID: %d, Estado: %s",
        descuento.getArchivoId(), descuento.getEstado());
  }

  /** Clase para resumir el resultado del registro de pagos. */
  public static class ResumenRegistroPagos {
    public int totalProcesados;
    public int exitosos;
    public int fallidos;
    public List<String> errores = new ArrayList<>();

    @Override
    public String toString() {
      return String.format(
          "ResumenRegistroPagos{total=%d, exitosos=%d, fallidos=%d, errores=%d}",
          totalProcesados, exitosos, fallidos, errores.size());
    }
  }

  /** Excepción personalizada para errores en el registro de pagos. */
  public static class PagoRegistroException extends RuntimeException {
    public PagoRegistroException(String mensaje) {
      super(mensaje);
    }

    public PagoRegistroException(String mensaje, Throwable causa) {
      super(mensaje, causa);
    }
  }
}
