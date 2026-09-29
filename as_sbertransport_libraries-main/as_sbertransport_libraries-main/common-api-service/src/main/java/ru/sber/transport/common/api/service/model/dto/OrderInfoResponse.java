package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.Objects;

/**
 * OrderInfoResponse
 */


public class OrderInfoResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("isSuccess")
  private Boolean isSuccess;

  @JsonProperty("order")
  private OrderInfoResponseExtra order;

  public OrderInfoResponse isSuccess(Boolean isSuccess) {
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

  public OrderInfoResponse order(OrderInfoResponseExtra order) {
    this.order = order;
    return this;
  }

  /**
   * Get order
   * @return order
  */
  @Valid 
  @Schema(name = "order", required = false)
  public OrderInfoResponseExtra getOrder() {
    return order;
  }

  public void setOrder(OrderInfoResponseExtra order) {
    this.order = order;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var orderInfoResponse = (OrderInfoResponse) o;
    return Objects.equals(this.isSuccess, orderInfoResponse.isSuccess) &&
        Objects.equals(this.order, orderInfoResponse.order);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isSuccess, order);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class OrderInfoResponse {\n");
    sb.append("    isSuccess: ").append(toIndentedString(isSuccess)).append("\n");
    sb.append("    order: ").append(toIndentedString(order)).append("\n");
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

