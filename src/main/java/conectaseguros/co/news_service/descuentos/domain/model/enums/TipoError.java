package conectaseguros.co.news_service.descuentos.domain.model.enums;

public enum TipoError {

  // Errores de estructura (críticos - detienen procesamiento)
  ESTRUCTURA_INVALIDA("Estructura de archivo inválida", true),
  HEADERS_INCORRECTOS("Headers del archivo incorrectos", true),
  ARCHIVO_VACIO("Archivo vacío o sin datos", true),

  // Errores de formato de línea (no críticos)
  FORMATO_LINEA("Error de formato en línea", false),
  CAMPO_OBLIGATORIO_FALTANTE("Campo obligatorio faltante", false),
  FORMATO_NUMERO_INVALIDO("Formato numérico inválido", false),
  LONGITUD_CAMPO_INVALIDA("Longitud de campo inválida", false),

  // Errores de negocio (no críticos)
  CLIENTE_NO_EXISTE("Cliente no existe en el sistema", false),
  CLIENTE_SECCIONAL_INCORRECTA("Cliente no pertenece a la seccional", false),
  CLIENTE_INACTIVO("Cliente inactivo", false),
  CLIENTE_FALTANTE_EN_ARCHIVO("Cliente esperado no se procesó en el archivo", false),
  SIN_PRODUCTOS_ACTIVOS("Cliente sin productos activos", false),
  DUPLICADO_PERIODO("Descuento duplicado en el período", false),
  VALOR_NEGATIVO_O_CERO("Valor debe ser positivo", false),

  ERROR_DESCUENTO_EXCEDE_MAXIMO("Valor de descuento excede el máximo permitido", false),

  // Errores de conectividad (no críticos - pueden ser reintentados)
  ERROR_COMUNICACION_CLIENTES("Error al consultar servicio de clientes", false),
  ERROR_COMUNICACION_PRODUCTOS("Error al consultar servicio de productos", false),
  TIMEOUT_SERVICIO_EXTERNO("Timeout en servicio externo", false),

  // Errores del sistema (críticos)
  ERROR_BASE_DATOS("Error en base de datos", true),
  ERROR_PROCESAMIENTO("Error interno de procesamiento", true);

  private final String descripcion;
  private final boolean critico;

  TipoError(String descripcion, boolean critico) {
    this.descripcion = descripcion;
    this.critico = critico;
  }

  public String getDescripcion() {
    return descripcion;
  }

  public boolean esCritico() {
    return critico;
  }

  public boolean esReintentable() {
    return this == ERROR_COMUNICACION_CLIENTES
        || this == ERROR_COMUNICACION_PRODUCTOS
        || this == TIMEOUT_SERVICIO_EXTERNO;
  }

  @Override
  public String toString() {
    return String.format("%s (%s)", name(), descripcion);
  }
}
