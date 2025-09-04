package conectaseguros.co.news_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidacionClienteServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ValidacionClienteService validacionClienteService;

    @BeforeEach
    void setUp() {
        // Set the productos API base URL for testing
        ReflectionTestUtils.setField(validacionClienteService, "productosApiBaseUrl", "http://localhost:8083");
    }

    @Test
    void clienteTieneProductosActivos_cuandoAPIResponde200_deberiaRetornarTrue() {
        // Given
        String cedula = "12345678";
        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.OK);
        
        when(restTemplate.exchange(
                eq("http://localhost:8083/api/v1/products/productos-cliente/12345678"),
                eq(HttpMethod.HEAD),
                isNull(),
                eq(Void.class)
        )).thenReturn(responseEntity);

        // When
        boolean resultado = validacionClienteService.clienteTieneProductosActivos(cedula);

        // Then
        assertTrue(resultado);
        verify(restTemplate).exchange(
                eq("http://localhost:8083/api/v1/products/productos-cliente/12345678"),
                eq(HttpMethod.HEAD),
                isNull(),
                eq(Void.class)
        );
    }

    @Test
    void clienteTieneProductosActivos_cuandoAPIResponde404_deberiaRetornarFalse() {
        // Given
        String cedula = "87654321";
        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        
        when(restTemplate.exchange(
                eq("http://localhost:8083/api/v1/products/productos-cliente/87654321"),
                eq(HttpMethod.HEAD),
                isNull(),
                eq(Void.class)
        )).thenReturn(responseEntity);

        // When
        boolean resultado = validacionClienteService.clienteTieneProductosActivos(cedula);

        // Then
        assertFalse(resultado);
    }

    @Test
    void clienteTieneProductosActivos_cuandoAPILanzaExcepcion_deberiaUsarFallback() {
        // Given
        String cedula = "12345678"; // Starts with "1", so fallback should return true
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.HEAD),
                isNull(),
                eq(Void.class)
        )).thenThrow(new RestClientException("Connection failed"));

        // When
        boolean resultado = validacionClienteService.clienteTieneProductosActivos(cedula);

        // Then
        assertTrue(resultado); // Should return true because cedula starts with "1"
    }

    @Test
    void clienteTieneProductosActivos_cuandoAPILanzaExcepcion_deberiaUsarFallbackConCedulaNo1o2() {
        // Given
        String cedula = "87654321"; // Starts with "8", so fallback should return false
        
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.HEAD),
                isNull(),
                eq(Void.class)
        )).thenThrow(new RestClientException("Connection failed"));

        // When
        boolean resultado = validacionClienteService.clienteTieneProductosActivos(cedula);

        // Then
        assertFalse(resultado); // Should return false because cedula doesn't start with "1" or "2"
    }

    @Test
    void obtenerClienteId_deberiaConvertirCedulaAId() {
        // Given
        String cedula = "12345678";

        // When
        Long clienteId = validacionClienteService.obtenerClienteId(cedula);

        // Then
        assertEquals(12345678L, clienteId);
    }

    @Test
    void validarClienteExiste_deberiaValidarCorrectamente() {
        // Given/When/Then
        assertTrue(validacionClienteService.validarClienteExiste("12345678"));
        assertTrue(validacionClienteService.validarClienteExiste("18765432"));
        assertFalse(validacionClienteService.validarClienteExiste("99999999"));
    }
}