package conectaseguros.co.news_service.archivo.domain.model;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Heuristica pura para detectar archivos Excel XLSX cuyo rango de celdas usado (declarado en el
 * atributo {@code ref} del elemento {@code <dimension>} de la hoja) es desproporcionado respecto a
 * las filas realmente pobladas ("rango usado fantasma").
 *
 * <p>No depende de Apache POI ni de Spring: recibe valores ya extraidos (la referencia de rango
 * como texto, y conteos de filas como numeros) y solo aplica reglas numericas, por lo que es
 * totalmente unitaria y rapida de probar.
 */
public final class RangoUsadoExcel {

  /** Techo absoluto de filas declaradas: por encima se aborta sin evaluar la proporcion. */
  public static final long FILAS_DECLARADAS_MAXIMAS = 200_000L;

  /** Piso de filas declaradas: por debajo nunca se marca como fantasma. */
  public static final long FILAS_DECLARADAS_MINIMAS_PARA_EVALUAR = 50_000L;

  /** Proporcion maxima permitida entre filas declaradas y filas realmente pobladas. */
  public static final long RATIO_MAXIMO = 50L;

  private static final Pattern REFERENCIA_CON_FILA = Pattern.compile("[A-Za-z]+(\\d+)");

  private RangoUsadoExcel() {}

  /**
   * Extrae el numero de fila declarado en el atributo {@code ref} del elemento {@code <dimension>}
   * (ej. {@code "A1:M1042782"} -> {@code 1042782}, {@code "A1"} -> {@code 1}). Referencias nulas,
   * vacias o malformadas retornan {@code 0}: un valor seguro que nunca dispara la heuristica de
   * forma accidental. Si el numero de fila desborda el rango de {@code long} (entrada extrema y
   * probablemente maliciosa) retorna {@code Long.MAX_VALUE} en su lugar, para que {@link
   * #excedeCeiling(long)} y {@link #esFantasma(long, long)} lo traten como que excede el techo en
   * vez de desactivar la proteccion.
   */
  public static long filasDeclaradas(String ref) {
    if (ref == null || ref.isBlank()) {
      return 0L;
    }
    String[] partes = ref.split(":");
    if (partes.length == 0) {
      return 0L;
    }
    String celdaFinal = partes[partes.length - 1].trim();
    Matcher matcher = REFERENCIA_CON_FILA.matcher(celdaFinal);
    if (!matcher.matches()) {
      return 0L;
    }
    try {
      return Long.parseLong(matcher.group(1));
    } catch (NumberFormatException e) {
      // El regex garantiza que group(1) es puramente numerico, asi que la unica forma de llegar
      // aqui es un desborde numerico genuino (mas de ~19 digitos): tratarlo como que excede el
      // techo, no como un valor seguro de 0, para no desactivar silenciosamente la proteccion.
      return Long.MAX_VALUE;
    }
  }

  /**
   * Indica si el numero de filas declaradas excede el techo absoluto configurado. Pensado para un
   * chequeo fail-fast antes de conocer cuantas filas estan realmente pobladas.
   */
  public static boolean excedeCeiling(long filasDeclaradas) {
    return filasDeclaradas > FILAS_DECLARADAS_MAXIMAS;
  }

  /**
   * Indica si la proporcion entre filas declaradas y filas realmente pobladas constituye un "rango
   * usado fantasma". Nunca evalua por debajo del piso configurado, de modo que archivos pequenos o
   * medianos nunca son marcados por error.
   */
  public static boolean esFantasma(long filasDeclaradas, long filasPobladas) {
    if (filasDeclaradas < FILAS_DECLARADAS_MINIMAS_PARA_EVALUAR) {
      return false;
    }
    long divisor = Math.max(filasPobladas, 1L);
    return filasDeclaradas / divisor >= RATIO_MAXIMO;
  }
}
