package conectaseguros.co.news_service.descuentos.infrastructure.output.product;

import conectaseguros.co.news_service.descuentos.domain.model.ResultadoValidacion;
import conectaseguros.co.news_service.descuentos.domain.model.enums.TipoError;
import conectaseguros.co.news_service.descuentos.domain.port.output.ProductoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ProductoServiceAdapter implements ProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoServiceAdapter.class);

    private final WebClient.Builder webClientBuilder;
    private final String serviceName;
    private final Duration timeout;
    // ✅ Cache para evitar múltiples llamadas al mismo cliente
    private final Map<Integer, List<ProductoInfo>> productosCache = new ConcurrentHashMap<>();

    public ProductoServiceAdapter(WebClient.Builder webClientBuilder,
                                  @Value("${app.services.products-service.name}") String serviceName,
                                  @Value("${app.services.products-service.timeout:5}") int timeoutSeconds) {
        this.webClientBuilder = webClientBuilder;
        this.serviceName = serviceName;
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        logger.info("✅ ProductoServiceAdapter configurado con Eureka y cache:");
        logger.info("   - Servicio: {}", serviceName);
    }

    /**
     * Busca productos activos por clienteId, usando cache si está disponible
     */
    public Optional<List<ProductoInfo>> buscarProductosActivosPorClienteId(Integer clienteId) {
        List<ProductoInfo> productosEnCache = productosCache.get(clienteId);
        if (productosEnCache != null) {
            logger.debug("✅ Productos de cliente {} encontrados en cache", clienteId);
            return Optional.of(productosEnCache);
        }
        try {
            logger.debug("=== BUSCANDO PRODUCTOS ACTIVOS CON EUREKA ===");
            logger.debug("ClienteId: {}", clienteId);
            String url = "lb://" + serviceName + "/api/v1/products/productos-cliente/cliente/" + clienteId + "/activos";
            logger.debug("URL con Eureka Discovery: {}", url);
            String token = getBearerToken();
            WebClient.RequestHeadersSpec<?> request = webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("X-Service-Name", "news-service")
                    .header("X-Request-Source", "internal");
            if (token != null) {
                request = request.header("Authorization", "Bearer " + token);
            }
            Map<String, Object> response = request
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(timeout)
                    .block();
            logger.debug("=== RESPONSE DEBUGGING ===");
            logger.debug("Response Body: {}", response);
            if (response != null) {
                Optional<List<ProductoInfo>> productosOpt = parseProductoResponse(response);
                if (productosOpt.isPresent()) {
                    productosCache.put(clienteId, productosOpt.get());
                    logger.debug("✅ Productos de cliente {} guardados en cache", clienteId);
                }
                return productosOpt;
            }
            logger.debug("❌ Response nulo - productos no encontrados");
            return Optional.empty();
        } catch (WebClientResponseException e) {
            logger.error("Error WebClient {} al buscar productos cliente {}: {}",
                    e.getStatusCode(), clienteId, e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 404) {
                logger.debug("Productos de cliente {} no encontrados (404)", clienteId);
                return Optional.empty();
            }
            throw new ProductoServiceException("Error HTTP: " + e.getStatusCode(), e);
        } catch (Exception e) {
            logger.error("Error al buscar productos cliente {}: {}", clienteId, e.getMessage(), e);
            throw new ProductoServiceException("Error de comunicación: " + e.getMessage(), e);
        }
    }

    /**
     * Busca productos activos por cédula del cliente, usando cache si está disponible
     */
    public Optional<List<ProductoInfo>> buscarProductosActivosPorClienteCedula(String clienteCedula) {
        // Para cache usamos hash de la cédula como key
        Integer cacheKey = clienteCedula.hashCode();
        List<ProductoInfo> productosEnCache = productosCache.get(cacheKey);
        if (productosEnCache != null) {
            logger.debug("✅ Productos de cliente cedula {} encontrados en cache", clienteCedula);
            return Optional.of(productosEnCache);
        }
        try {
            logger.debug("=== BUSCANDO PRODUCTOS ACTIVOS POR CEDULA CON EUREKA ===");
            logger.debug("ClienteCedula: {}", clienteCedula);
            String url = "lb://" + serviceName + "/api/v1/products/productos-cliente/cliente/" + clienteCedula + "/activos";
            logger.debug("URL con Eureka Discovery: {}", url);
            String token = getBearerToken();
            WebClient.RequestHeadersSpec<?> request = webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("X-Service-Name", "news-service")
                    .header("X-Request-Source", "internal");
            if (token != null) {
                request = request.header("Authorization", "Bearer " + token);
            }
            Map<String, Object> response = request
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .timeout(timeout)
                    .block();
            logger.debug("=== RESPONSE DEBUGGING ===");
            logger.debug("Response Body: {}", response);
            if (response != null) {
                Optional<List<ProductoInfo>> productosOpt = parseProductoResponse(response);
                if (productosOpt.isPresent()) {
                    productosCache.put(cacheKey, productosOpt.get());
                    logger.debug("✅ Productos de cliente cedula {} guardados en cache", clienteCedula);
                }
                return productosOpt;
            }
            logger.debug("❌ Response nulo - productos no encontrados");
            return Optional.empty();
        } catch (WebClientResponseException e) {
            logger.error("Error WebClient {} al buscar productos cliente cedula {}: {}",
                    e.getStatusCode(), clienteCedula, e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 404) {
                logger.debug("Productos de cliente cedula {} no encontrados (404)", clienteCedula);
                return Optional.empty();
            }
            throw new ProductoServiceException("Error HTTP: " + e.getStatusCode(), e);
        } catch (Exception e) {
            logger.error("Error al buscar productos cliente cedula {}: {}", clienteCedula, e.getMessage(), e);
            throw new ProductoServiceException("Error de comunicación: " + e.getMessage(), e);
        }
    }

    @Override
    public ResultadoValidacion validarProductosActivos(Integer clienteId) {
        try {
            logger.debug("=== VALIDANDO PRODUCTOS ACTIVOS (USANDO CACHE) ===");
            logger.debug("ClienteId: {}", clienteId);
            List<ProductoInfo> productosEnCache = productosCache.get(clienteId);
            Optional<List<ProductoInfo>> productosOpt;
            if (productosEnCache != null) {
                logger.debug("✅ Usando productos desde cache - evitando segunda llamada HTTP");
                productosOpt = Optional.of(productosEnCache);
            } else {
                logger.debug("⚠️ Productos no en cache, haciendo llamada HTTP");
                productosOpt = buscarProductosActivosPorClienteId(clienteId);
            }
            if (productosOpt.isEmpty() || productosOpt.get().isEmpty()) {
                logger.debug("❌ Cliente {} no tiene productos activos", clienteId);
                return ResultadoValidacion.conError(
                        TipoError.SIN_PRODUCTOS_ACTIVOS,
                        String.format("Cliente %d no tiene productos activos", clienteId)
                );
            }
            logger.debug("✅ Cliente {} tiene productos activos: {}", clienteId, productosOpt.get().size());
            return ResultadoValidacion.exitoso(productosOpt.get());
        } catch (ProductoServiceException e) {
            logger.error("❌ Error en validación productos activos: {}", e.getMessage());
            return ResultadoValidacion.conError(
                    TipoError.ERROR_COMUNICACION_PRODUCTOS,
                    "Error al validar productos: " + e.getMessage()
            );
        }
    }

    @Override
    public ResultadoValidacion validarProductosActivosPorCedula(String clienteCedula) {
        try {
            logger.debug("=== VALIDANDO PRODUCTOS ACTIVOS POR CEDULA (USANDO CACHE) ===");
            logger.debug("ClienteCedula: {}", clienteCedula);
            Integer cacheKey = clienteCedula.hashCode();
            List<ProductoInfo> productosEnCache = productosCache.get(cacheKey);
            Optional<List<ProductoInfo>> productosOpt;
            if (productosEnCache != null) {
                logger.debug("✅ Usando productos desde cache - evitando segunda llamada HTTP");
                productosOpt = Optional.of(productosEnCache);
            } else {
                logger.debug("⚠️ Productos no en cache, haciendo llamada HTTP");
                productosOpt = buscarProductosActivosPorClienteCedula(clienteCedula);
            }
            if (productosOpt.isEmpty() || productosOpt.get().isEmpty()) {
                logger.debug("❌ Cliente cedula {} no tiene productos activos", clienteCedula);
                return ResultadoValidacion.conError(
                        TipoError.SIN_PRODUCTOS_ACTIVOS,
                        String.format("Cliente con cédula %s no tiene productos activos", clienteCedula)
                );
            }
            logger.debug("✅ Cliente cedula {} tiene productos activos: {}", clienteCedula, productosOpt.get().size());
            return ResultadoValidacion.exitoso(productosOpt.get());
        } catch (ProductoServiceException e) {
            logger.error("❌ Error en validación productos activos por cédula: {}", e.getMessage());
            return ResultadoValidacion.conError(
                    TipoError.ERROR_COMUNICACION_PRODUCTOS,
                    "Error al validar productos: " + e.getMessage()
            );
        }
    }

    /**
     * Limpiar cache (útil para testing o cuando sea necesario)
     */
    public void limpiarCache() {
        productosCache.clear();
        logger.debug("✅ Cache de productos limpiado");
    }

    /**
     * Parsea la respuesta del microservicio de productos
     */
    private Optional<List<ProductoInfo>> parseProductoResponse(Map<String, Object> responseBody) {
        try {
            logger.debug("=== PARSEANDO RESPUESTA PRODUCTOS ===");
            logger.debug("Response completo: {}", responseBody);
            Object dataObj = responseBody.get("data");
            logger.debug("Campo 'data': {}", dataObj);
            if (!(dataObj instanceof List)) {
                logger.warn("❌ Campo 'data' no es una lista: {}", dataObj);
                return Optional.empty();
            }
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> productosList = (List<Map<String, Object>>) dataObj;
            List<ProductoInfo> productos = productosList.stream()
                    .map(this::mapToProductoInfo)
                    .toList();
            logger.debug("✅ Productos activos parseados: {}", productos.size());
            return Optional.of(productos);
        } catch (Exception e) {
            logger.error("❌ Error al parsear productos del cliente: {}", e.getMessage(), e);
            throw new ProductoServiceException("Error al parsear productos: " + e.getMessage(), e);
        }
    }

    private ProductoInfo mapToProductoInfo(Map<String, Object> productoMap) {
        logger.debug("Mapeando producto desde datos: {}", productoMap);

        try {
            // Parsear campos obligatorios
            Integer id = productoMap.get("id") instanceof Number ?
                ((Number) productoMap.get("id")).intValue() : null;

            Integer polizaId = productoMap.get("polizaId") instanceof Number ?
                ((Number) productoMap.get("polizaId")).intValue() : null;

            String aseguradoCedula = productoMap.get("aseguradoCedula") instanceof String ?
                (String) productoMap.get("aseguradoCedula") : null;

            String estado = productoMap.get("estado") instanceof String ?
                (String) productoMap.get("estado") : null;

            // Parsear fechas
            LocalDateTime fechaCreacion = parseDateTime(productoMap.get("fechaCreacion"));
            LocalDateTime fechaCancelacion = parseDateTime(productoMap.get("fechaCancelacion"));

            // Parsear campos de texto opcionales
            String motivoCancelacion = productoMap.get("motivoCancelacion") instanceof String ?
                (String) productoMap.get("motivoCancelacion") : null;

            String usuarioCreacion = productoMap.get("usuarioCreacion") instanceof String ?
                (String) productoMap.get("usuarioCreacion") : null;

            String usuarioModificacion = productoMap.get("usuarioModificacion") instanceof String ?
                (String) productoMap.get("usuarioModificacion") : null;

            // Verificar si tiene la nueva estructura (valorTotalPeriodo y planDePagos)
            boolean tieneNuevaEstructura = productoMap.containsKey("valorTotalPeriodo") &&
                                         productoMap.containsKey("planDePagos");

            if (tieneNuevaEstructura) {
                // Usar la nueva estructura
                BigDecimal valorTotalPeriodo = parseBigDecimal(productoMap.get("valorTotalPeriodo"));
                Map<String, BigDecimal> planDePagos = parsePlanDePagos(productoMap.get("planDePagos"));

                logger.debug("Producto parseado (nueva estructura) - ID: {}, PolizaID: {}, Cedula: {}, Estado: {}, ValorTotalPeriodo: {}, PlanPagos: {}",
                    id, polizaId, aseguradoCedula, estado, valorTotalPeriodo, planDePagos);

                return new ProductoInfo(id, polizaId, aseguradoCedula, estado,
                    fechaCreacion, fechaCancelacion, motivoCancelacion,
                    usuarioCreacion, usuarioModificacion,
                    valorTotalPeriodo, planDePagos);
            } else {
                // Usar la estructura anterior para compatibilidad
                BigDecimal valorTotalAnual = parseBigDecimal(productoMap.get("valorTotalAnual"));
                BigDecimal cuotaMensual = parseBigDecimal(productoMap.get("cuotaMensual"));

                logger.debug("Producto parseado (estructura anterior) - ID: {}, PolizaID: {}, Cedula: {}, Estado: {}, CuotaMensual: {}",
                    id, polizaId, aseguradoCedula, estado, cuotaMensual);

                return new ProductoInfo(id, polizaId, aseguradoCedula, estado,
                    fechaCreacion, fechaCancelacion, motivoCancelacion,
                    usuarioCreacion, usuarioModificacion,
                    valorTotalAnual, cuotaMensual);
            }

        } catch (Exception e) {
            logger.error("Error al mapear producto: {} - Datos: {}", e.getMessage(), productoMap);
            throw new ProductoServiceException("Error al parsear producto: " + e.getMessage(), e);
        }
    }

    /**
     * Parsea el plan de pagos desde la respuesta del endpoint
     */
    @SuppressWarnings("unchecked")
    private Map<String, BigDecimal> parsePlanDePagos(Object planDePagosObj) {
        if (planDePagosObj == null) {
            return Map.of();
        }

        try {
            if (planDePagosObj instanceof Map) {
                Map<String, Object> planMap = (Map<String, Object>) planDePagosObj;
                Map<String, BigDecimal> resultado = new java.util.HashMap<>();

                for (Map.Entry<String, Object> entry : planMap.entrySet()) {
                    String periodo = entry.getKey();
                    BigDecimal valor = parseBigDecimal(entry.getValue());
                    if (valor.compareTo(BigDecimal.ZERO) > 0) {
                        resultado.put(periodo, valor);
                    }
                }

                logger.debug("Plan de pagos parseado: {}", resultado);
                return resultado;
            }
        } catch (Exception e) {
            logger.warn("Error al parsear plan de pagos, retornando map vacío: {}", e.getMessage());
        }

        return Map.of();
    }

    /**
     * Parsea un BigDecimal de forma segura desde diferentes tipos de objetos
     */
    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                logger.warn("No se pudo parsear BigDecimal desde String: {}", value);
                return BigDecimal.ZERO;
            }
        }
        logger.warn("Tipo no soportado para BigDecimal: {} - {}", value.getClass().getSimpleName(), value);
        return BigDecimal.ZERO;
    }

    /**
     * Parsea un LocalDateTime de forma segura desde diferentes tipos de objetos
     */
    private LocalDateTime parseDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            try {
                return LocalDateTime.parse((String) value);
            } catch (Exception e) {
                logger.warn("No se pudo parsear LocalDateTime desde String: {}", value);
                return null;
            }
        }
        logger.warn("Tipo no soportado para LocalDateTime: {} - {}", value.getClass().getSimpleName(), value);
        return null;
    }

    /**
     * Convierte un objeto a Integer de forma segura
     */
    private Integer convertToInteger(Object obj, String fieldName) {
        if (obj == null) {
            throw new IllegalArgumentException("Campo " + fieldName + " es nulo");
        }
        if (obj instanceof Integer) {
            return (Integer) obj;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        if (obj instanceof String) {
            try {
                return Integer.valueOf((String) obj);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("No se puede convertir " + fieldName + " a Integer: " + obj);
            }
        }
        throw new IllegalArgumentException("Tipo no soportado para " + fieldName + ": " + obj.getClass().getSimpleName());
    }

    /**
     * Valida si el cliente tiene productos usando una petición HEAD al endpoint externo
     */
    public boolean clienteTieneProductos(Integer clienteId) {
        try {
            String url = "lb://" + serviceName + "/api/v1/products/productos-cliente/cliente/" + clienteId + "/activos";
            logger.debug("HEAD URL con Eureka Discovery: {}", url);
            String token = getBearerToken();
            WebClient.RequestHeadersSpec<?> request = webClientBuilder.build()
                .head()
                .uri(url)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("X-Service-Name", "news-service")
                .header("X-Request-Source", "internal");
            if (token != null) {
                request = request.header("Authorization", "Bearer " + token);
            }
            request
                .retrieve()
                .toBodilessEntity()
                .timeout(timeout)
                .block();
            return true; // Si no lanza excepción, el cliente tiene productos
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                logger.debug("Cliente {} no tiene productos (HEAD 404)", clienteId);
                return false;
            }
            if (e.getStatusCode().value() == 503) {
                logger.error("Servicio de productos no disponible (503) al validar cliente {}.", clienteId);
                throw new ProductoServiceException("Servicio de productos no disponible (503)", e);
            }
            logger.error("Error HEAD al validar productos cliente {}: {}", clienteId, e.getMessage());
            throw new ProductoServiceException("Error HEAD: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error de comunicación HEAD productos cliente {}: {}", clienteId, e.getMessage());
            throw new ProductoServiceException("Error de comunicación HEAD: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean clienteTieneProductosPorCedula(String clienteCedula) {
        try {
            String url = "lb://" + serviceName + "/api/v1/products/productos-cliente/cliente/" + clienteCedula + "/activos";
            logger.debug("HEAD URL por cédula con Eureka Discovery: {}", url);
            String token = getBearerToken();
            WebClient.RequestHeadersSpec<?> request = webClientBuilder.build()
                .head()
                .uri(url)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("X-Service-Name", "news-service")
                .header("X-Request-Source", "internal");
            if (token != null) {
                request = request.header("Authorization", "Bearer " + token);
            }
            request
                .retrieve()
                .toBodilessEntity()
                .timeout(timeout)
                .block();
            return true; // Si no lanza excepción, el cliente tiene productos
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                logger.debug("Cliente cédula {} no tiene productos (HEAD 404)", clienteCedula);
                return false;
            }
            if (e.getStatusCode().value() == 503) {
                logger.error("Servicio de productos no disponible (503) al validar cliente cédula {}.", clienteCedula);
                throw new ProductoServiceException("Servicio de productos no disponible (503)", e);
            }
            logger.error("Error HEAD al validar productos cliente cédula {}: {}", clienteCedula, e.getMessage());
            throw new ProductoServiceException("Error HEAD: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error de comunicación HEAD productos cliente cédula {}: {}", clienteCedula, e.getMessage());
            throw new ProductoServiceException("Error de comunicación HEAD: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ProductoInfo> obtenerProductosActivos(Integer clienteId) {
        return buscarProductosActivosPorClienteId(clienteId).orElse(List.of());
    }

    @Override
    public List<ProductoInfo> obtenerProductosActivosPorCedula(String clienteCedula) {
        return buscarProductosActivosPorClienteCedula(clienteCedula).orElse(List.of());
    }

    /**
     * Obtiene el token JWT actual del contexto de seguridad
     */
    private String getBearerToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String token) {
            return token;
        }
        return null;
    }

    /**
     * Excepción personalizada para errores del servicio de productos
     */
    public static class ProductoServiceException extends RuntimeException {
        public ProductoServiceException(String mensaje) {
            super(mensaje);
        }
        public ProductoServiceException(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }
}
