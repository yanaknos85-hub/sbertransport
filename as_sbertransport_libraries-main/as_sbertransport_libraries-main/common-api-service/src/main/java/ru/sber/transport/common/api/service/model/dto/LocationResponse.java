package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.Objects;

/**
 * LocationResponse
 */


public class LocationResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("isSuccess")
  private Boolean isSuccess;

  @JsonProperty("orderLocation")
  private OrderLocationResponse orderLocation;

  public LocationResponse isSuccess(Boolean isSuccess) {
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

  public LocationResponse orderLocation(OrderLocationResponse orderLocation) {
    this.orderLocation = orderLocation;
    return this;
  }

  /**
   * Get orderLocation
   * @return orderLocation
  */
  @Valid 
  @Schema(name = "orderLocation", required = false)
  public OrderLocationResponse getOrderLocation() {
    return orderLocation;
  }

  public void setOrderLocation(OrderLocationResponse orderLocation) {
    this.orderLocation = orderLocation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var locationResponse = (LocationResponse) o;
    return Objects.equals(this.isSuccess, locationResponse.isSuccess) &&
        Objects.equals(this.orderLocation, locationResponse.orderLocation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isSuccess, orderLocation);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class LocationResponse {\n");
    sb.append("    isSuccess: ").append(toIndentedString(isSuccess)).append("\n");
    sb.append("    orderLocation: ").append(toIndentedString(orderLocation)).append("\n");
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

