package conectaseguros.co.news_service.descuentos.domain.port.output;

import conectaseguros.co.news_service.descuentos.domain.model.DescuentoValidado;
import java.util.List;
import java.util.Optional;

public interface DescuentoRepository {

  /**
   * Guarda un descuento validado
   *
   * @param descuento Descuento a guardar
   * @return Descuento guardado con ID asignado
   */
  DescuentoValidado guardar(DescuentoValidado descuento);

  /**
   * Guarda múltiples descuentos en lote
   *
   * @param descuentos Lista de descuentos a guardar
   * @return Lista de descuentos guardados
   */
  List<DescuentoValidado> guardarLote(List<DescuentoValidado> descuentos);

  /**
   * Busca un descuento por ID
   *
   * @param id ID del descuento
   * @return Descuento si existe
   */
  Optional<DescuentoValidado> buscarPorId(Long id);

  /**
   * Verifica si ya existe un descuento para el cliente en el período
   *
   * @param cedulaCliente Cédula del cliente
   * @param periodo Período del descuento
   * @param codigoConcepto Código del concepto
   * @return true si ya existe
   */
  boolean existeDescuentoPeriodo(String cedulaCliente, String periodo, String codigoConcepto);

  /**
   * Busca descuentos por archivo
   *
   * @param archivoId ID del archivo
   * @return Lista de descuentos del archivo
   */
  List<DescuentoValidado> buscarPorArchivo(Long archivoId);

  /**
   * Busca descuentos por cliente y período
   *
   * @param cedulaCliente Cédula del cliente
   * @param periodo Período del descuento
   * @return Lista de descuentos del cliente en el período
   */
  List<DescuentoValidado> buscarPorClienteYPeriodo(String cedulaCliente, String periodo);

  /**
   * Elimina los descuentos asociados a un archivo
   *
   * @param archivoId ID del archivo
   */
  void eliminarPorArchivo(Long archivoId);
}
