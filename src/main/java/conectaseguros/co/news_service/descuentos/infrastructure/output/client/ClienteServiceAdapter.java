package conectaseguros.co.news_service.descuentos.infrastructure.output.client;

import conectaseguros.co.news_service.descuentos.domain.model.ResultadoValidacion;
import conectaseguros.co.news_service.descuentos.domain.model.enums.TipoError;
import conectaseguros.co.news_service.descuentos.domain.port.output.ClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ClienteServiceAdapter implements ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteServiceAdapter.class);

    private final WebClient.Builder webClientBuilder;
    private final String serviceName;

    // ✅ Cache para evitar múltiples llamadas al mismo cliente
    private final Map<String, ClienteInfo> clienteCache = new ConcurrentHashMap<>();

    public ClienteServiceAdapter(WebClient.Builder webClientBuilder,
                                 @Value("${app.services.clients-service.name}") String serviceName) {
        this.webClientBuilder = webClientBuilder;
        this.serviceName = serviceName;

        logger.info("✅ ClienteServiceAdapter configurado con cache:");
        logger.info("   - Servicio: {}", serviceName);
    }

    @Override
    public Optional<ClienteInfo> buscarPorCedula(String cedula) {
        // ✅ Verificar cache primero
        ClienteInfo clienteEnCache = clienteCache.get(cedula);
        if (clienteEnCache != null) {
            logger.debug("✅ Cliente {} encontrado en cache", cedula);
            return Optional.of(clienteEnCache);
        }

        try {
            logger.debug("=== BUSCANDO CLIENTE CON EUREKA ===");
            logger.debug("Cédula: {}", cedula);

            String url = "lb://" + serviceName + "/api/v1/clients/clientes/cedula/" + cedula + "/exists";
            logger.debug("URL con Eureka Discovery: {}", url);

            Map<String, Object> response = webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .header("X-Service-Name", "news-service")
                    .header("X-Request-Source", "internal")
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            logger.debug("=== RESPONSE DEBUGGING ===");
            logger.debug("Response Body: {}", response);

            if (response != null) {
                Optional<ClienteInfo> clienteOpt = parseClienteResponse(response, cedula);

                // ✅ Guardar en cache si se encontró
                if (clienteOpt.isPresent()) {
                    clienteCache.put(cedula, clienteOpt.get());
                    logger.debug("✅ Cliente {} guardado en cache", cedula);
                }

                return clienteOpt;
            }

            logger.debug("❌ Response nulo - cliente no encontrado");
            return Optional.empty();

        } catch (WebClientResponseException e) {
            logger.error("Error WebClient {} al buscar cliente {}: {}",
                    e.getStatusCode(), cedula, e.getResponseBodyAsString());

            if (e.getStatusCode().value() == 404) {
                logger.debug("Cliente {} no encontrado (404)", cedula);
                return Optional.empty();
            }

            throw new ClienteServiceException("Error HTTP: " + e.getStatusCode(), e);

        } catch (Exception e) {
            logger.error("Error al buscar cliente {} con Eureka: {}", cedula, e.getMessage(), e);
            throw new ClienteServiceException("Error de comunicación: " + e.getMessage(), e);
        }
    }

    @Override
    public ResultadoValidacion validarClienteSeccional(String cedula, Integer seccionalId) {
        try {
            logger.debug("=== VALIDANDO CLIENTE SECCIONAL CON EUREKA ===");
            logger.debug("Cédula: {}, Seccional esperada: {}", cedula, seccionalId);

            Optional<ClienteInfo> clienteOpt = buscarPorCedula(cedula);

            if (clienteOpt.isEmpty()) {
                logger.debug("❌ Cliente no encontrado por buscarPorCedula()");
                return ResultadoValidacion.conError(
                        TipoError.CLIENTE_NO_EXISTE,
                        String.format("Cliente con cédula %s no existe", cedula)
                );
            }

            ClienteInfo cliente = clienteOpt.get();
            logger.debug("✅ Cliente encontrado: {}", cliente);
            logger.debug("Seccional del cliente: {}, Seccional esperada: {}", cliente.getSeccionalId(), seccionalId);

            if (!seccionalId.equals(cliente.getSeccionalId())) {
                logger.debug("❌ Seccional no coincide");
                return ResultadoValidacion.conError(
                        TipoError.CLIENTE_SECCIONAL_INCORRECTA,
                        String.format("Cliente %s pertenece a seccional %d, esperada %d",
                                cedula, cliente.getSeccionalId(), seccionalId)
                );
            }

            logger.debug("✅ Validación exitosa");
            return ResultadoValidacion.exitoso(cliente);

        } catch (ClienteServiceException e) {
            logger.error("❌ Error en validación cliente-seccional: {}", e.getMessage());
            return ResultadoValidacion.conError(
                    TipoError.ERROR_COMUNICACION_CLIENTES,
                    "Error al validar cliente: " + e.getMessage()
            );
        }
    }

    @Override
    public ResultadoValidacion validarClienteActivo(String cedula) {
        try {
            logger.debug("=== VALIDANDO CLIENTE ACTIVO (USANDO CACHE) ===");
            logger.debug("Cédula: {}", cedula);

            // ✅ Intentar obtener del cache primero (ya debería estar por validarClienteSeccional)
            ClienteInfo clienteEnCache = clienteCache.get(cedula);
            Optional<ClienteInfo> clienteOpt;

            if (clienteEnCache != null) {
                logger.debug("✅ Usando cliente desde cache - evitando segunda llamada HTTP");
                clienteOpt = Optional.of(clienteEnCache);
            } else {
                logger.debug("⚠️ Cliente no en cache, haciendo llamada HTTP");
                clienteOpt = buscarPorCedula(cedula);
            }

            if (clienteOpt.isEmpty()) {
                logger.debug("❌ Cliente no encontrado");
                return ResultadoValidacion.conError(
                        TipoError.CLIENTE_NO_EXISTE,
                        String.format("Cliente con cédula %s no existe", cedula)
                );
            }

            ClienteInfo cliente = clienteOpt.get();
            logger.debug("Cliente encontrado: {}, Activo: {}", cliente.getNombreCompleto(), cliente.isActivo());

            if (!cliente.isActivo()) {
                logger.debug("❌ Cliente inactivo");
                return ResultadoValidacion.conError(
                        TipoError.CLIENTE_INACTIVO,
                        String.format("Cliente %s está inactivo", cedula)
                );
            }

            logger.debug("✅ Cliente activo - validación exitosa");
            return ResultadoValidacion.exitoso(cliente);

        } catch (ClienteServiceException e) {
            logger.error("❌ Error en validación cliente activo: {}", e.getMessage());
            return ResultadoValidacion.conError(
                    TipoError.ERROR_COMUNICACION_CLIENTES,
                    "Error al validar cliente: " + e.getMessage()
            );
        }
    }

    /**
     * ✅ Método combinado para validar cliente completo en una sola llamada
     */
    public ResultadoValidacion validarClienteCompleto(String cedula, Integer seccionalId) {
        try {
            logger.debug("=== VALIDACIÓN COMPLETA DE CLIENTE ===");
            logger.debug("Cédula: {}, Seccional esperada: {}", cedula, seccionalId);

            Optional<ClienteInfo> clienteOpt = buscarPorCedula(cedula);

            if (clienteOpt.isEmpty()) {
                logger.debug("❌ Cliente no encontrado");
                return ResultadoValidacion.conError(
                        TipoError.CLIENTE_NO_EXISTE,
                        String.format("Cliente con cédula %s no existe", cedula)
                );
            }

            ClienteInfo cliente = clienteOpt.get();
            logger.debug("✅ Cliente encontrado: {}", cliente);

            // ✅ Validar seccional
            if (!seccionalId.equals(cliente.getSeccionalId())) {
                logger.debug("❌ Seccional no coincide");
                return ResultadoValidacion.conError(
                        TipoError.CLIENTE_SECCIONAL_INCORRECTA,
                        String.format("Cliente %s pertenece a seccional %d, esperada %d",
                                cedula, cliente.getSeccionalId(), seccionalId)
                );
            }

            // ✅ Validar activo
            if (!cliente.isActivo()) {
                logger.debug("❌ Cliente inactivo");
                return ResultadoValidacion.conError(
                        TipoError.CLIENTE_INACTIVO,
                        String.format("Cliente %s está inactivo", cedula)
                );
            }

            logger.debug("✅ Validación completa exitosa - cliente válido");
            return ResultadoValidacion.exitoso(cliente);

        } catch (ClienteServiceException e) {
            logger.error("❌ Error en validación completa: {}", e.getMessage());
            return ResultadoValidacion.conError(
                    TipoError.ERROR_COMUNICACION_CLIENTES,
                    "Error al validar cliente: " + e.getMessage()
            );
        }
    }

    /**
     * Limpiar cache (útil para testing o cuando sea necesario)
     */
    public void limpiarCache() {
        clienteCache.clear();
        logger.debug("✅ Cache de clientes limpiado");
    }

    /**
     * Parsea la respuesta del microservicio de clientes
     */
    private Optional<ClienteInfo> parseClienteResponse(Map<String, Object> responseBody, String cedula) {
        try {
            logger.debug("=== PARSEANDO RESPUESTA ===");
            logger.debug("Response completo: {}", responseBody);

            Object dataObj = responseBody.get("data");
            logger.debug("Campo 'data': {}", dataObj);

            if (!(dataObj instanceof Map)) {
                logger.warn("❌ Campo 'data' no es un Map: {}", dataObj);
                return Optional.empty();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) dataObj;
            logger.debug("Data como Map: {}", data);

            Object existsObj = data.get("exists");
            logger.debug("Campo 'exists': {}", existsObj);

            Boolean exists = (Boolean) existsObj;
            if (!Boolean.TRUE.equals(exists)) {
                logger.debug("❌ Cliente existe = false o null");
                return Optional.empty();
            }

            logger.debug("✅ Cliente existe = true, extrayendo clienteInfo...");

            Object clienteInfoObj = data.get("clienteInfo");
            logger.debug("Campo 'clienteInfo': {}", clienteInfoObj);

            if (!(clienteInfoObj instanceof Map)) {
                logger.warn("❌ clienteInfo no es un Map: {}", clienteInfoObj);
                return Optional.empty();
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> clienteInfo = (Map<String, Object>) clienteInfoObj;
            logger.debug("ClienteInfo como Map: {}", clienteInfo);

            // ✅ Extraer campos con validación
            Object idObj = clienteInfo.get("id");
            Object nombreCompletoObj = clienteInfo.get("nombreCompleto");
            Object seccionalIdObj = clienteInfo.get("seccionalId");
            Object activoObj = clienteInfo.get("activo");

            logger.debug("Campos extraídos:");
            logger.debug("  - ID: {} ({})", idObj, idObj != null ? idObj.getClass().getSimpleName() : "NULL");
            logger.debug("  - NombreCompleto: {} ({})", nombreCompletoObj, nombreCompletoObj != null ? nombreCompletoObj.getClass().getSimpleName() : "NULL");
            logger.debug("  - SeccionalId: {} ({})", seccionalIdObj, seccionalIdObj != null ? seccionalIdObj.getClass().getSimpleName() : "NULL");
            logger.debug("  - Activo: {} ({})", activoObj, activoObj != null ? activoObj.getClass().getSimpleName() : "NULL");

            // ✅ Crear ClienteInfo con conversiones seguras
            ClienteInfo cliente = new ClienteInfo(
                    convertToInteger(idObj, "id"),
                    cedula,
                    (String) nombreCompletoObj,
                    convertToInteger(seccionalIdObj, "seccionalId"),
                    (Boolean) activoObj
            );

            logger.debug("✅ Cliente creado exitosamente: {}", cliente);
            return Optional.of(cliente);

        } catch (Exception e) {
            logger.error("❌ Error al parsear información del cliente: {}", e.getMessage(), e);
            throw new ClienteServiceException("Error al parsear información del cliente: " + e.getMessage(), e);
        }
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
     * Excepción personalizada para errores del servicio de clientes
     */
    public static class ClienteServiceException extends RuntimeException {
        public ClienteServiceException(String mensaje) {
            super(mensaje);
        }

        public ClienteServiceException(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }
}