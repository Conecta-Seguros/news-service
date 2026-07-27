package conectaseguros.co.news_service.archivo.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pruebas puras (sin POI, sin Spring) de la heuristica de deteccion de "rango usado fantasma" en
 * archivos Excel XLSX.
 */
class RangoUsadoExcelTest {

  @Test
  @DisplayName("filasDeclaradas parsea un rango tipo A1:M1042782")
  void filasDeclaradas_parseaRangoCompleto() {
    assertThat(RangoUsadoExcel.filasDeclaradas("A1:M1042782")).isEqualTo(1_042_782L);
  }

  @Test
  @DisplayName("filasDeclaradas parsea una referencia de una sola celda")
  void filasDeclaradas_parseaCeldaUnica() {
    assertThat(RangoUsadoExcel.filasDeclaradas("A1")).isEqualTo(1L);
  }

  @Test
  @DisplayName("filasDeclaradas parsea un rango corto")
  void filasDeclaradas_parseaRangoCorto() {
    assertThat(RangoUsadoExcel.filasDeclaradas("A1:C40")).isEqualTo(40L);
  }

  @Test
  @DisplayName("filasDeclaradas retorna un valor seguro (0) para referencia nula")
  void filasDeclaradas_referenciaNulaRetornaValorSeguro() {
    assertThat(RangoUsadoExcel.filasDeclaradas(null)).isZero();
  }

  @Test
  @DisplayName("filasDeclaradas retorna un valor seguro (0) para referencia vacia")
  void filasDeclaradas_referenciaVaciaRetornaValorSeguro() {
    assertThat(RangoUsadoExcel.filasDeclaradas("  ")).isZero();
  }

  @Test
  @DisplayName("filasDeclaradas retorna un valor seguro (0) para referencia malformada")
  void filasDeclaradas_referenciaMalformadaRetornaValorSeguro() {
    assertThat(RangoUsadoExcel.filasDeclaradas("no-es-un-rango")).isZero();
  }

  @Test
  @DisplayName("filasDeclaradas retorna un valor seguro (0) para referencia de solo dos puntos")
  void filasDeclaradas_referenciaSoloDosPuntosRetornaValorSeguro() {
    assertThat(RangoUsadoExcel.filasDeclaradas(":")).isZero();
  }

  @Test
  @DisplayName(
      "filasDeclaradas retorna un valor seguro (0) para referencia de multiples dos puntos")
  void filasDeclaradas_referenciaMultiplesDosPuntosRetornaValorSeguro() {
    assertThat(RangoUsadoExcel.filasDeclaradas("::")).isZero();
  }

  @Test
  @DisplayName(
      "filasDeclaradas retorna Long.MAX_VALUE cuando el numero de fila desborda long, para no"
          + " debilitar el ceiling")
  void filasDeclaradas_desbordeNumericoRetornaMaxValue() {
    assertThat(RangoUsadoExcel.filasDeclaradas("A1:A99999999999999999999"))
        .isEqualTo(Long.MAX_VALUE);
  }

  @Test
  @DisplayName("excedeCeiling es verdadero cuando filasDeclaradas desbordo por overflow numerico")
  void excedeCeiling_verdaderoParaDesbordeNumerico() {
    long filasDeclaradas = RangoUsadoExcel.filasDeclaradas("A1:A99999999999999999999");
    assertThat(RangoUsadoExcel.excedeCeiling(filasDeclaradas)).isTrue();
  }

  @Test
  @DisplayName("esFantasma es verdadero cuando filasDeclaradas desbordo por overflow numerico")
  void esFantasma_verdaderoParaDesbordeNumerico() {
    long filasDeclaradas = RangoUsadoExcel.filasDeclaradas("A1:A99999999999999999999");
    assertThat(RangoUsadoExcel.esFantasma(filasDeclaradas, 1L)).isTrue();
  }

  @Test
  @DisplayName("excedeCeiling es verdadero para la firma fantasma reportada en produccion")
  void excedeCeiling_verdaderoParaFirmaFantasmaReal() {
    assertThat(RangoUsadoExcel.excedeCeiling(1_042_782L)).isTrue();
  }

  @Test
  @DisplayName("excedeCeiling es falso justo en el techo configurado")
  void excedeCeiling_falsoEnElTecho() {
    assertThat(RangoUsadoExcel.excedeCeiling(RangoUsadoExcel.FILAS_DECLARADAS_MAXIMAS)).isFalse();
  }

  @Test
  @DisplayName("excedeCeiling es verdadero justo por encima del techo configurado")
  void excedeCeiling_verdaderoPorEncimaDelTecho() {
    assertThat(RangoUsadoExcel.excedeCeiling(RangoUsadoExcel.FILAS_DECLARADAS_MAXIMAS + 1))
        .isTrue();
  }

  @Test
  @DisplayName("esFantasma detecta la firma fantasma real: 1.042.782 declaradas / 36 pobladas")
  void esFantasma_firmaFantasmaReal() {
    assertThat(RangoUsadoExcel.esFantasma(1_042_782L, 36L)).isTrue();
  }

  @Test
  @DisplayName("esFantasma no marca un archivo pequeno normal: 500 declaradas / 40 pobladas")
  void esFantasma_archivoPequenoNormalNoEsFantasma() {
    assertThat(RangoUsadoExcel.esFantasma(500L, 40L)).isFalse();
  }

  @Test
  @DisplayName("esFantasma nunca evalua por debajo del piso: 49.999 declaradas")
  void esFantasma_debajoDelPisoNuncaSeEvalua() {
    assertThat(RangoUsadoExcel.esFantasma(49_999L, 10L)).isFalse();
  }

  @Test
  @DisplayName("esFantasma detecta proporcion subtil por encima del piso: 60.000 / 100")
  void esFantasma_proporcionSubtilPorEncimaDelPiso() {
    assertThat(RangoUsadoExcel.esFantasma(60_000L, 100L)).isTrue();
  }

  @Test
  @DisplayName(
      "esFantasma no marca un archivo grande pero denso: 60.000 declaradas / 59.000 pobladas")
  void esFantasma_archivoGrandeYDensoNoEsFantasma() {
    assertThat(RangoUsadoExcel.esFantasma(60_000L, 59_000L)).isFalse();
  }

  @Test
  @DisplayName(
      "esFantasma evalua exactamente en el piso configurado (no marca si el ratio es bajo)")
  void esFantasma_evaluaExactamenteEnElPiso() {
    assertThat(
            RangoUsadoExcel.esFantasma(
                RangoUsadoExcel.FILAS_DECLARADAS_MINIMAS_PARA_EVALUAR,
                RangoUsadoExcel.FILAS_DECLARADAS_MINIMAS_PARA_EVALUAR))
        .isFalse();
  }

  @Test
  @DisplayName("esFantasma marca justo por encima del piso cuando el ratio tambien excede")
  void esFantasma_marcaJustoPorEncimaDelPisoConRatioAlto() {
    long declaradas = RangoUsadoExcel.FILAS_DECLARADAS_MINIMAS_PARA_EVALUAR + 1;
    assertThat(RangoUsadoExcel.esFantasma(declaradas, 1L)).isTrue();
  }

  @Test
  @DisplayName("esFantasma es verdadero exactamente en el ratio maximo configurado (50x)")
  void esFantasma_verdaderoEnElRatioExacto() {
    long pobladas = 1_001L;
    long declaradas = pobladas * RangoUsadoExcel.RATIO_MAXIMO;
    assertThat(RangoUsadoExcel.esFantasma(declaradas, pobladas)).isTrue();
  }

  @Test
  @DisplayName("esFantasma es falso justo por debajo del ratio maximo configurado")
  void esFantasma_falsoJustoPorDebajoDelRatio() {
    long pobladas = 1_001L;
    long declaradas = pobladas * RangoUsadoExcel.RATIO_MAXIMO - 1;
    assertThat(RangoUsadoExcel.esFantasma(declaradas, pobladas)).isFalse();
  }

  @Test
  @DisplayName("esFantasma es verdadero justo por encima del ratio maximo configurado")
  void esFantasma_verdaderoJustoPorEncimaDelRatio() {
    long pobladas = 1_001L;
    long declaradas = pobladas * RangoUsadoExcel.RATIO_MAXIMO + 1;
    assertThat(RangoUsadoExcel.esFantasma(declaradas, pobladas)).isTrue();
  }
}
