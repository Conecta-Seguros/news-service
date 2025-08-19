package conectaseguros.co.news_service.descuentos.infrastructure.output.client;

import conectaseguros.co.news_service.descuentos.domain.model.ResultadoValidacion;
import conectaseguros.co.news_service.descuentos.domain.model.enums.TipoError;
import conectaseguros.co.news_service.descuentos.domain.port.output.ClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Component
public class ClienteServiceAdapter implements ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteServiceAdapter.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ClienteServiceAdapter(RestTemplate restTemplate,
                                 @Value("${app.services.api-gateway.base-url:http://localhost:8080}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    @Override
    public Optional<ClienteInfo> buscarPorCedula(String cedula) {
        try {
            logger.debug("Buscando cliente con cédula: {} en URL: {}", cedula, baseUrl);

            // ✅ Construir la URL correcta
            String url = baseUrl + "/api/v1/clientes/cedula/" + cedula + "/exists";
            logger.debug("URL completa: {}", url);

            // ✅ Usar Map para manejar la respuesta JSON dinámicamente
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                logger.debug("Respuesta recibida: {}", responseBody);

                // Extraer el campo 'data' de la respuesta
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");

                if (data != null) {
                    Boolean exists = (Boolean) data.get("exists");

                    if (Boolean.TRUE.equals(exists)) {
                        // Extraer información del cliente
                        @SuppressWarnings("unchecked")
                        Map<String, Object> clienteInfo = (Map<String, Object>) data.get("clienteInfo");

                        if (clienteInfo != null) {
                            ClienteInfo cliente = new ClienteInfo(
                                    Math.toIntExact(Long.parseLong(clienteInfo.get("id").toString())),
                                    cedula,
                                    (String) clienteInfo.get("nombreCompleto"),
                                    Integer.valueOf(clienteInfo.get("seccionalId").toString()),
                                    (Boolean) clienteInfo.get("activo")
                            );

                            logger.debug("Cliente encontrado: {}", cliente);
                            return Optional.of(cliente);
                        }
                    } else {
                        logger.debug("Cliente con cédula {} no existe según el servicio", cedula);
                        return Optional.empty();
                    }
                }
            }

            logger.debug("No se encontró información válida para la cédula: {}", cedula);
            return Optional.empty();

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                logger.debug("Cliente con cédula {} no encontrado (404)", cedula);
                return Optional.empty();
            }

            logger.error("Error HTTP al buscar cliente {}: {} - {}", cedula, e.getStatusCode(), e.getResponseBodyAsString());
            throw new ClienteServiceException("Error HTTP al consultar cliente: " + e.getStatusCode(), e);

        } catch (Exception e) {
            logger.error("Error inesperado al buscar cliente {}", cedula, e);
            throw new ClienteServiceException("Error de comunicación con servicio de clientes: " + e.getMessage(), e);
        }
    }

    @Override
    public ResultadoValidacion validarClienteSeccional(String cedula, Integer seccionalId) {
        try {
            Optional<ClienteInfo> clienteOpt = buscarPorCedula(cedula);

            if (clienteOpt.isEmpty()) {
                return ResultadoValidacion.conError(
                        TipoError.CLIENTE_NO_EXISTE,
                        String.format("Cliente con cédula %s no existe en el sistema", cedula)
                );
            }

            ClienteInfo cliente = clienteOpt.get();

            if (!seccionalId.equals(cliente.getSeccionalId())) {
                return ResultadoValidacion.conError(
                        TipoError.CLIENTE_SECCIONAL_INCORRECTA,
                        String.format("Cliente %s pertenece a seccional %d, se esperaba %d",
                                cedula, cliente.getSeccionalId(), seccionalId)
                );
            }

            return ResultadoValidacion.exitoso(cliente);

        } catch (ClienteServiceException e) {
            logger.error("Error al validar cliente-seccional para {}", cedula, e);
            return ResultadoValidacion.conError(
                    TipoError.ERROR_COMUNICACION_CLIENTES,
                    String.format("Error al validar cliente %s: %s", cedula, e.getMessage())
            );
        }
    }

    @Override
    public ResultadoValidacion validarClienteActivo(String cedula) {
        try {
            Optional<ClienteInfo> clienteOpt = buscarPorCedula(cedula);

            if (clienteOpt.isEmpty()) {
                return ResultadoValidacion.conError(
                        TipoError.CLIENTE_NO_EXISTE,
                        String.format("Cliente con cédula %s no existe en el sistema", cedula)
                );
            }

            ClienteInfo cliente = clienteOpt.get();

            if (!cliente.isActivo()) {
                return ResultadoValidacion.conError(
                        TipoError.CLIENTE_INACTIVO,
                        String.format("Cliente %s está inactivo", cedula)
                );
            }

            return ResultadoValidacion.exitoso(cliente);

        } catch (ClienteServiceException e) {
            logger.error("Error al validar estado activo del cliente {}", cedula, e);
            return ResultadoValidacion.conError(
                    TipoError.ERROR_COMUNICACION_CLIENTES,
                    String.format("Error al validar estado del cliente %s: %s", cedula, e.getMessage())
            );
        }
    }

    public static class ClienteServiceException extends RuntimeException {
        public ClienteServiceException(String mensaje) {
            super(mensaje);
        }

        public ClienteServiceException(String mensaje, Throwable causa) {
            super(mensaje, causa);
        }
    }
}
