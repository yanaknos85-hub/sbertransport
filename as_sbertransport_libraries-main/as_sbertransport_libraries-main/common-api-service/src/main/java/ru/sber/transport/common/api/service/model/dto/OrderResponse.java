package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

/**
 * OrderResponse
 */


public class OrderResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("isSuccess")
  private Boolean isSuccess;

  @JsonProperty("orderParthnerId")
  private String orderParthnerId;

  @JsonProperty("orderSbertransportId")
  private String orderSbertransportId;

  public OrderResponse isSuccess(Boolean isSuccess) {
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

  public OrderResponse orderParthnerId(String orderParthnerId) {
    this.orderParthnerId = orderParthnerId;
    return this;
  }

  /**
   * Идентификатор созданного заказа ответа
   * @return orderParthnerId
  */
  
  @Schema(name = "orderParthnerId", description = "Идентификатор созданного заказа ответа", required = false)
  public String getOrderParthnerId() {
    return orderParthnerId;
  }

  public void setOrderParthnerId(String orderParthnerId) {
    this.orderParthnerId = orderParthnerId;
  }

  public OrderResponse orderSbertransportId(String orderSbertransportId) {
    this.orderSbertransportId = orderSbertransportId;
    return this;
  }

  /**
   * Get orderSbertransportId
   * @return orderSbertransportId
  */
  
  @Schema(name = "orderSbertransportId", required = false)
  public String getOrderSbertransportId() {
    return orderSbertransportId;
  }

  public void setOrderSbertransportId(String orderSbertransportId) {
    this.orderSbertransportId = orderSbertransportId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var orderResponse = (OrderResponse) o;
    return Objects.equals(this.isSuccess, orderResponse.isSuccess) &&
        Objects.equals(this.orderParthnerId, orderResponse.orderParthnerId) &&
        Objects.equals(this.orderSbertransportId, orderResponse.orderSbertransportId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isSuccess, orderParthnerId, orderSbertransportId);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class OrderResponse {\n");
    sb.append("    isSuccess: ").append(toIndentedString(isSuccess)).append("\n");
    sb.append("    orderParthnerId: ").append(toIndentedString(orderParthnerId)).append("\n");
    sb.append("    orderSbertransportId: ").append(toIndentedString(orderSbertransportId)).append("\n");
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

