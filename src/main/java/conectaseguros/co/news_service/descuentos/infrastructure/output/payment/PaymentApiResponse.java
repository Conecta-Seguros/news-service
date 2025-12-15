package conectaseguros.co.news_service.descuentos.infrastructure.output.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Wrapper para las respuestas del API de payments-service. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentApiResponse<T> {

  private String message;
  private T data;
}
