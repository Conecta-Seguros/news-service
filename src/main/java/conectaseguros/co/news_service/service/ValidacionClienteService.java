package conectaseguros.co.news_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ValidacionClienteService {
    
    private final RestTemplate restTemplate;
    
    @Value("${productos.api.base-url}")
    private String productosApiBaseUrl;
    
    private final List<String> clientesValidos = Arrays.asList(
            "12345678", "87654321", "98765432", "11111111", "22222222"
    );

    private final List<String> polizasActivas = Arrays.asList(
            "POL-001", "POL-002", "POL-003", "POL-004", "POL-005"
    );

    public boolean validarClienteExiste(String cedula) {
        // Simulación: cliente existe si está en la lista o empieza por "1"
        return clientesValidos.contains(cedula) || cedula.startsWith("1");
    }
    /**
     * Validar que el cliente tiene productos activos
     * Consulta la API de productos usando el endpoint /api/v1/products/productos-cliente/{id}
     */
    public boolean clienteTieneProductosActivos(String cedula) {
        try {
            // Obtener el ID del cliente basado en la cédula
            Long clienteId = obtenerClienteId(cedula);
            
            // Construir la URL del endpoint de productos
            String url = productosApiBaseUrl + "/api/v1/products/productos-cliente/" + clienteId;
            
            log.debug("Verificando productos activos para cliente ID: {} en URL: {}", clienteId, url);
            
            // Realizar petición HEAD al endpoint de productos
            ResponseEntity<Void> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.HEAD,
                null,
                Void.class
            );
            
            // Si el status es 200, el cliente tiene productos activos
            boolean tieneProductos = response.getStatusCode() == HttpStatus.OK;
            log.debug("Cliente ID: {} {} productos activos", clienteId, tieneProductos ? "tiene" : "no tiene");
            
            return tieneProductos;
            
        } catch (RestClientException e) {
            log.warn("Error al consultar productos para cliente con cédula {}: {}", cedula, e.getMessage());
            // En caso de error, aplicar fallback con la lógica anterior
            return cedula.startsWith("1") || cedula.startsWith("2");
        } catch (Exception e) {
            log.error("Error inesperado al validar productos del cliente {}: {}", cedula, e.getMessage());
            // En caso de error, aplicar fallback con la lógica anterior
            return cedula.startsWith("1") || cedula.startsWith("2");
        }
    }

    /**
     * Validar que la póliza específica existe y está activa para el cliente
     * En producción: consultar base de datos o API de pólizas
     */
    public boolean validarPolizaActivaParaCliente(String cedula, String numeroPoliza) {
        // Simulación: si el cliente existe y la póliza está en la lista
        return validarClienteExiste(cedula) &&
                (polizasActivas.contains(numeroPoliza) || numeroPoliza.startsWith("POL-"));
    }

    /**
     * Validar seccional
     * En producción: implementar según tu lógica de negocio
     */
    public boolean validarSeccional(String cedula, String seccionalRequerida) {
        // Simulación: todos los clientes válidos pertenecen a la seccional
        return validarClienteExiste(cedula);
    }

    /**
     * Obtener información del cliente para mensajes
     * En producción: consultar base de datos o API
     */
    public String obtenerNombreCompletoCliente(String cedula) {
        // Simulación de nombres
        switch (cedula) {
            case "12345678": return "Juan Pérez García";
            case "87654321": return "María López Rodríguez";
            case "98765432": return "Carlos Martínez Silva";
            case "11111111": return "Ana González Morales";
            case "22222222": return "Luis Hernández Castro";
            default: return "Cliente " + cedula;
        }
    }

    /**
     * Obtener nombre del producto para mensajes
     * En producción: consultar base de datos o API
     */
    public String obtenerNombreProducto(String numeroPoliza) {
        // Simulación de productos
        if (numeroPoliza.contains("AUTO")) return "Seguro Automóvil";
        if (numeroPoliza.contains("VIDA")) return "Seguro de Vida";
        if (numeroPoliza.contains("HOGAR")) return "Seguro Hogar";
        return "Seguro General";
    }

    /**
     * Obtener ID del cliente (simulado)
     * En producción: consultar base de datos
     */
    public Long obtenerClienteId(String cedula) {
        // Simulación: convertir cédula a ID
        return Long.parseLong(cedula.replaceAll("[^0-9]", "").substring(0, Math.min(8, cedula.length())));
    }
}