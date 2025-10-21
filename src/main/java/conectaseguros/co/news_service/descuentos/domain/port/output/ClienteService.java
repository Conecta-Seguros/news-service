package conectaseguros.co.news_service.descuentos.domain.port.output;

import conectaseguros.co.news_service.descuentos.domain.model.ResultadoValidacion;
import java.util.List;
import java.util.Optional;
import lombok.Getter;

public interface ClienteService {

  /**
   * Busca un cliente por su cédula
   *
   * @param cedula Cédula del cliente
   * @return Información del cliente si existe
   */
  Optional<ClienteInfo> buscarPorCedula(String cedula);

  /**
   * Valida que un cliente pertenezca a la seccional especificada
   *
   * @param cedula Cédula del cliente
   * @param seccionalId ID de la seccional
   * @return Resultado de la validación
   */
  ResultadoValidacion validarClienteSeccional(String cedula, Integer seccionalId);

  /**
   * Verifica si un cliente está activo
   *
   * @param cedula Cédula del cliente
   * @return Resultado de la validación
   */
  ResultadoValidacion validarClienteActivo(String cedula);

  /**
   * Obtiene todos los clientes activos de una seccional
   *
   * @param seccionalId ID de la seccional
   * @return Lista de clientes activos de la seccional
   */
  List<ClienteInfo> obtenerClientesActivosPorSeccional(Integer seccionalId);

  /** Información básica del cliente */
  @Getter
  class ClienteInfo {
    // Getters
    private final Integer id;
    private final String cedula;
    private final String nombreCompleto;
    private final Integer seccionalId;
    private final boolean activo;

    public ClienteInfo(
        Integer id, String cedula, String nombreCompleto, Integer seccionalId, boolean activo) {
      this.id = id;
      this.cedula = cedula;
      this.nombreCompleto = nombreCompleto;
      this.seccionalId = seccionalId;
      this.activo = activo;
    }

    @Override
    public String toString() {
      return String.format(
          "ClienteInfo{id=%d, cedula='%s', seccional=%d, activo=%s}",
          id, cedula, seccionalId, activo);
    }
  }
}
