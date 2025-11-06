package conectaseguros.co.news_service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import conectaseguros.co.news_service.descuentos.application.service.ValidadorClientesFaltantes;
import conectaseguros.co.news_service.descuentos.domain.model.ErrorProcesamiento;
import conectaseguros.co.news_service.descuentos.domain.model.enums.TipoError;
import conectaseguros.co.news_service.descuentos.domain.port.output.ClienteService;
import conectaseguros.co.news_service.descuentos.domain.port.output.ErrorProcesamientoRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * ✅ Tests para ValidadorClientesFaltantes
 *
 * <p>Este test valida la lógica de validación bidireccional: BD → Archivo (detectar clientes que no
 * vinieron en el archivo)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ValidadorClientesFaltantes - Validación Bidireccional")
public class ValidadorClientesFaltantesTest {

  @Mock private ClienteService clienteService;

  @Mock private ErrorProcesamientoRepository errorProcesamientoRepository;

  private ValidadorClientesFaltantes validador;

  @BeforeEach
  void setUp() {
    validador = new ValidadorClientesFaltantes(clienteService, errorProcesamientoRepository);
  }

  @Test
  @DisplayName("Debe detectar clientes faltantes cuando existen en BD pero no en archivo")
  void testDetectarClientesFaltantes() {
    // ARRANGE
    Long archivoId = 1L;
    Integer seccionalId = 5;
    String periodo = "2024-10";

    // Clientes en BD
    List<ClienteService.ClienteInfo> clientesBD =
        List.of(
            new ClienteService.ClienteInfo(1, "1001001", "Cliente 1", seccionalId, true),
            new ClienteService.ClienteInfo(2, "1001002", "Cliente 2", seccionalId, true),
            new ClienteService.ClienteInfo(3, "1001003", "Cliente 3", seccionalId, true),
            new ClienteService.ClienteInfo(4, "1001004", "Cliente 4", seccionalId, true),
            new ClienteService.ClienteInfo(5, "1001005", "Cliente 5", seccionalId, true));

    // Clientes procesados del archivo (faltaría 1001004 y 1001005)
    Set<String> clientesArchivoProcesados = new HashSet<>();
    clientesArchivoProcesados.add("1001001");
    clientesArchivoProcesados.add("1001002");
    clientesArchivoProcesados.add("1001003");

    when(clienteService.obtenerClientesActivosPorSeccional(seccionalId)).thenReturn(clientesBD);

    when(errorProcesamientoRepository.guardar(any(ErrorProcesamiento.class)))
        .thenReturn(new ErrorProcesamiento());

    // ACT
    int clientesFaltantes =
        validador.validarClientesFaltantes(
            archivoId, seccionalId, clientesArchivoProcesados, periodo);

    // ASSERT
    assertEquals(2, clientesFaltantes, "Debería encontrar 2 clientes faltantes");

    // Verificar que se llamó a guardar dos veces (una por cada cliente faltante)
    ArgumentCaptor<ErrorProcesamiento> captor = ArgumentCaptor.forClass(ErrorProcesamiento.class);
    verify(errorProcesamientoRepository, times(2)).guardar(captor.capture());

    List<ErrorProcesamiento> erroresGuardados = captor.getAllValues();
    erroresGuardados.forEach(
        error ->
            assertEquals(
                TipoError.CLIENTE_FALTANTE_EN_ARCHIVO,
                error.getTipoError(),
                "Los errores registrados deben ser de tipo CLIENTE_FALTANTE_EN_ARCHIVO"));
  }

  @Test
  @DisplayName("Debe retornar 0 cuando todos los clientes fueron procesados")
  void testTodosLosClientesFueronProcesados() {
    // ARRANGE
    Long archivoId = 1L;
    Integer seccionalId = 5;
    String periodo = "2024-10";

    List<ClienteService.ClienteInfo> clientesBD =
        List.of(
            new ClienteService.ClienteInfo(1, "1001001", "Cliente 1", seccionalId, true),
            new ClienteService.ClienteInfo(2, "1001002", "Cliente 2", seccionalId, true));

    // Todos los clientes fueron procesados
    Set<String> clientesArchivoProcesados = new HashSet<>();
    clientesArchivoProcesados.add("1001001");
    clientesArchivoProcesados.add("1001002");

    when(clienteService.obtenerClientesActivosPorSeccional(seccionalId)).thenReturn(clientesBD);

    // ACT
    int clientesFaltantes =
        validador.validarClientesFaltantes(
            archivoId, seccionalId, clientesArchivoProcesados, periodo);

    // ASSERT
    assertEquals(0, clientesFaltantes, "No debería haber clientes faltantes");
    verify(errorProcesamientoRepository, never()).guardar(any());
  }

  @Test
  @DisplayName("Debe manejar seccional vacía correctamente")
  void testSeccionalVacia() {
    // ARRANGE
    Long archivoId = 1L;
    Integer seccionalId = 99;
    String periodo = "2024-10";

    List<ClienteService.ClienteInfo> clientesBD = new ArrayList<>(); // Vacía

    Set<String> clientesArchivoProcesados = new HashSet<>();

    when(clienteService.obtenerClientesActivosPorSeccional(seccionalId)).thenReturn(clientesBD);

    // ACT
    int clientesFaltantes =
        validador.validarClientesFaltantes(
            archivoId, seccionalId, clientesArchivoProcesados, periodo);

    // ASSERT
    assertEquals(0, clientesFaltantes, "Seccional vacía no debería generar clientes faltantes");
    verify(errorProcesamientoRepository, never()).guardar(any());
  }

  @Test
  @DisplayName("Debe retornar cantidad correcta con archivo parcial")
  void testArchivoConClientesParcialesProcesados() {
    // ARRANGE
    Long archivoId = 1L;
    Integer seccionalId = 10;
    String periodo = "2024-10";

    List<ClienteService.ClienteInfo> clientesBD =
        List.of(
            new ClienteService.ClienteInfo(1, "1001001", "Cliente 1", seccionalId, true),
            new ClienteService.ClienteInfo(2, "1001002", "Cliente 2", seccionalId, true),
            new ClienteService.ClienteInfo(3, "1001003", "Cliente 3", seccionalId, true),
            new ClienteService.ClienteInfo(4, "1001004", "Cliente 4", seccionalId, true),
            new ClienteService.ClienteInfo(5, "1001005", "Cliente 5", seccionalId, true),
            new ClienteService.ClienteInfo(6, "1001006", "Cliente 6", seccionalId, true));

    Set<String> clientesArchivoProcesados = new HashSet<>();
    clientesArchivoProcesados.add("1001001"); // Procesado
    clientesArchivoProcesados.add("1001003"); // Procesado
    // 1001002, 1001004, 1001005, 1001006 faltantes

    when(clienteService.obtenerClientesActivosPorSeccional(seccionalId)).thenReturn(clientesBD);

    when(errorProcesamientoRepository.guardar(any(ErrorProcesamiento.class)))
        .thenReturn(new ErrorProcesamiento());

    // ACT
    int clientesFaltantes =
        validador.validarClientesFaltantes(
            archivoId, seccionalId, clientesArchivoProcesados, periodo);

    // ASSERT
    assertEquals(4, clientesFaltantes, "Debería encontrar 4 clientes faltantes");
    verify(errorProcesamientoRepository, times(4)).guardar(any());
  }

  @Test
  @DisplayName("Debe registrar correctamente los datos del error de cliente faltante")
  void testDatosErrorClienteFaltante() {
    // ARRANGE
    Long archivoId = 1L;
    Integer seccionalId = 5;
    String periodo = "2024-10";
    String cedulaFaltante = "1001004";

    List<ClienteService.ClienteInfo> clientesBD =
        List.of(
            new ClienteService.ClienteInfo(1, "1001001", "Cliente 1", seccionalId, true),
            new ClienteService.ClienteInfo(4, "1001004", "Cliente 4", seccionalId, true));

    Set<String> clientesArchivoProcesados = new HashSet<>();
    clientesArchivoProcesados.add("1001001");
    // 1001004 faltante

    when(clienteService.obtenerClientesActivosPorSeccional(seccionalId)).thenReturn(clientesBD);

    when(errorProcesamientoRepository.guardar(any(ErrorProcesamiento.class)))
        .thenReturn(new ErrorProcesamiento());

    // ACT
    validador.validarClientesFaltantes(archivoId, seccionalId, clientesArchivoProcesados, periodo);

    // ASSERT
    ArgumentCaptor<ErrorProcesamiento> captor = ArgumentCaptor.forClass(ErrorProcesamiento.class);
    verify(errorProcesamientoRepository).guardar(captor.capture());

    ErrorProcesamiento errorCapturado = captor.getValue();
    assertEquals(archivoId, errorCapturado.getArchivoId(), "ID de archivo debe coincidir");
    assertEquals(
        cedulaFaltante, errorCapturado.getCodigoCliente(), "Código de cliente debe coincidir");
    assertEquals(periodo, errorCapturado.getPeriodo(), "Período debe coincidir");
    assertEquals(seccionalId, errorCapturado.getSeccionalId(), "Seccional debe coincidir");
    assertEquals(
        TipoError.CLIENTE_FALTANTE_EN_ARCHIVO,
        errorCapturado.getTipoError(),
        "Tipo error debe ser CLIENTE_FALTANTE_EN_ARCHIVO");
  }
}
