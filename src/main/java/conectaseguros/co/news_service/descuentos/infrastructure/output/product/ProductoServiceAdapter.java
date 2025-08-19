package conectaseguros.co.news_service.descuentos.infrastructure.output.product;

import conectaseguros.co.news_service.descuentos.domain.model.ResultadoValidacion;
import conectaseguros.co.news_service.descuentos.domain.model.enums.TipoError;
import conectaseguros.co.news_service.descuentos.domain.port.output.ProductoService;
import conectaseguros.co.news_service.descuentos.infrastructure.input.dto.ProductoClienteResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductoServiceAdapter implements ProductoService {

    private static final Logger logger = LoggerFactory.getLogger(ProductoServiceAdapter.class);

    private final WebClient webClient;
    private final String baseUrl;
    private final Duration timeout;

    public ProductoServiceAdapter(WebClient.Builder webClientBuilder,
                                  @Value("${app.services.productos.base-url:http://localhost:8083}") String baseUrl,
                                  @Value("${app.services.productos.timeout:5}") int timeoutSeconds) {
        this.baseUrl = baseUrl;
        this.timeout = Duration.ofSeconds(timeoutSeconds);
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public ResultadoValidacion validarProductosActivos(Integer clienteId) {
        try {
            logger.debug("Validando productos activos para cliente: {}", clienteId);

            ProductoClienteResponseDTO response = webClient
                    .get()
                    .uri("/api/v1/productos/cliente/{clienteId}/activos", clienteId)
                    .retrieve()
                    .bodyToMono(ProductoClienteResponseDTO.class)
                    .timeout(timeout)
                    .block();

            if (response == null || response.getTotalProductosActivos() == null) {
                return ResultadoValidacion.conError(
                        TipoError.SIN_PRODUCTOS_ACTIVOS,
                        String.format("No se pudo determinar productos activos para cliente %d", clienteId)
                );
            }

            if (response.getTotalProductosActivos() == 0) {
                return ResultadoValidacion.conError(
                        TipoError.SIN_PRODUCTOS_ACTIVOS,
                        String.format("Cliente %d no tiene productos activos", clienteId)
                );
            }

            // Convertir a ProductoInfo
            List<ProductoInfo> productos = response.getProductos().stream()
                    .filter(p -> Boolean.TRUE.equals(p.getVigente()))
                    .map(p -> new ProductoInfo(p.getId(), p.getCodigo(), p.getNombre(), p.getVigente()))
                    .collect(Collectors.toList());

            logger.debug("Cliente {} tiene {} productos activos", clienteId, productos.size());
            return ResultadoValidacion.exitoso(productos);

        } catch (WebClientResponseException.NotFound e) {
            logger.debug("No se encontraron productos para cliente: {}", clienteId);
            return ResultadoValidacion.conError(
                    TipoError.SIN_PRODUCTOS_ACTIVOS,
                    String.format("Cliente %d no tiene productos registrados", clienteId)
            );

        } catch (WebClientResponseException e) {
            logger.error("Error HTTP al consultar productos del cliente {}: {} - {}",
                    clienteId, e.getStatusCode(), e.getResponseBodyAsString());
            return ResultadoValidacion.conError(
                    TipoError.ERROR_COMUNICACION_PRODUCTOS,
                    String.format("Error HTTP al consultar productos: %s", e.getStatusCode())
            );

        } catch (Exception e) {
            logger.error("Error inesperado al consultar productos del cliente {}", clienteId, e);
            return ResultadoValidacion.conError(
                    TipoError.ERROR_COMUNICACION_PRODUCTOS,
                    String.format("Error de comunicación con servicio de productos: %s", e.getMessage())
            );
        }
    }

    @Override
    public List<ProductoInfo> obtenerProductosActivos(Integer clienteId) {
        ResultadoValidacion resultado = validarProductosActivos(clienteId);

        if (resultado.esValido()) {
            @SuppressWarnings("unchecked")
            List<ProductoInfo> productos = resultado.getDatosAdicionales(List.class);
            return productos != null ? productos : Collections.emptyList();
        }

        return Collections.emptyList();
    }
}