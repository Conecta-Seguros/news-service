package conectaseguros.co.news_service.archivo.domain.model.enums;

public enum TipoArchivo {
  NOVEDADES("NOVEDADES"),
  DESCUENTOS("DESCUENTOS");

  private final String valor;

  TipoArchivo(String valor) {
    this.valor = valor;
  }

  public static TipoArchivo fromString(String valor) {
    for (TipoArchivo tipo : TipoArchivo.values()) {
      if (tipo.valor.equalsIgnoreCase(valor)) {
        return tipo;
      }
    }
    throw new IllegalArgumentException("Tipo de archivo no válido: " + valor);
  }

  public String getValor() {
    return valor;
  }
}
