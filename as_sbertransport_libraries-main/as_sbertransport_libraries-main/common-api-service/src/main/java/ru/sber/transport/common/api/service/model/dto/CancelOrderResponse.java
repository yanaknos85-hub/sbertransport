package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

/**
 * CancelOrderResponse
 */


public class CancelOrderResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("isSuccess")
  private Boolean isSuccess;

  @JsonProperty("orderParthnerID")
  private String orderParthnerID;

  public CancelOrderResponse isSuccess(Boolean isSuccess) {
    this.isSuccess = isSuccess;
    return this;
  }

  /**
   * Обработка завершена успешно
   * @return isSuccess
  */
  
  @Schema(name = "isSuccess", description = "Обработка завершена успешно", required = false)
  public Boolean isIsSuccess() {
    return isSuccess;
  }

  public void setIsSuccess(Boolean isSuccess) {
    this.isSuccess = isSuccess;
  }

  public CancelOrderResponse orderParthnerID(String orderParthnerID) {
    this.orderParthnerID = orderParthnerID;
    return this;
  }

  /**
   * Номер заказа
   * @return orderParthnerID
  */
  
  @Schema(name = "orderParthnerID", description = "Номер заказа", required = false)
  public String getOrderParthnerID() {
    return orderParthnerID;
  }

  public void setOrderParthnerID(String orderParthnerID) {
    this.orderParthnerID = orderParthnerID;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var cancelOrderResponse = (CancelOrderResponse) o;
    return Objects.equals(this.isSuccess, cancelOrderResponse.isSuccess) &&
        Objects.equals(this.orderParthnerID, cancelOrderResponse.orderParthnerID);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isSuccess, orderParthnerID);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class CancelOrderResponse {\n");
    sb.append("    isSuccess: ").append(toIndentedString(isSuccess)).append("\n");
    sb.append("    orderParthnerID: ").append(toIndentedString(orderParthnerID)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

