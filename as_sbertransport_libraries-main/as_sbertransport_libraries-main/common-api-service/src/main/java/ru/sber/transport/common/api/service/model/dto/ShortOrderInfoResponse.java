package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ShortOrderInfoResponse
 */


public class ShortOrderInfoResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("isSuccess")
  private Boolean isSuccess;

  @JsonProperty("orders")
  @Valid
  private List<ShortOrderInfoResponseExtra> orders = null;

  public ShortOrderInfoResponse isSuccess(Boolean isSuccess) {
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

  public ShortOrderInfoResponse orders(List<ShortOrderInfoResponseExtra> orders) {
    this.orders = orders;
    return this;
  }

  public ShortOrderInfoResponse addOrdersItem(ShortOrderInfoResponseExtra ordersItem) {
    if (this.orders == null) {
      this.orders = new ArrayList<>();
    }
    this.orders.add(ordersItem);
    return this;
  }

  /**
   * Get orders
   * @return orders
  */
  @Valid 
  @Schema(name = "orders", required = false)
  public List<ShortOrderInfoResponseExtra> getOrders() {
    return orders;
  }

  public void setOrders(List<ShortOrderInfoResponseExtra> orders) {
    this.orders = orders;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var shortOrderInfoResponse = (ShortOrderInfoResponse) o;
    return Objects.equals(this.isSuccess, shortOrderInfoResponse.isSuccess) &&
        Objects.equals(this.orders, shortOrderInfoResponse.orders);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isSuccess, orders);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class ShortOrderInfoResponse {\n");
    sb.append("    isSuccess: ").append(toIndentedString(isSuccess)).append("\n");
    sb.append("    orders: ").append(toIndentedString(orders)).append("\n");
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

