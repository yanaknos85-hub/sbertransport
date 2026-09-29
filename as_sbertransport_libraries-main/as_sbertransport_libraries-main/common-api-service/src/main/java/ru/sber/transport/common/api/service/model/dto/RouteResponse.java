package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.Objects;

/**
 * RouteResponse
 */


public class RouteResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("isSuccess")
  private Boolean isSuccess;

  @JsonProperty("orderTrack")
  private OrderTrackResponse orderTrack;

  public RouteResponse isSuccess(Boolean isSuccess) {
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

  public RouteResponse orderTrack(OrderTrackResponse orderTrack) {
    this.orderTrack = orderTrack;
    return this;
  }

  /**
   * Get orderTrack
   * @return orderTrack
  */
  @Valid 
  @Schema(name = "orderTrack", required = false)
  public OrderTrackResponse getOrderTrack() {
    return orderTrack;
  }

  public void setOrderTrack(OrderTrackResponse orderTrack) {
    this.orderTrack = orderTrack;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var routeResponse = (RouteResponse) o;
    return Objects.equals(this.isSuccess, routeResponse.isSuccess) &&
        Objects.equals(this.orderTrack, routeResponse.orderTrack);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isSuccess, orderTrack);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class RouteResponse {\n");
    sb.append("    isSuccess: ").append(toIndentedString(isSuccess)).append("\n");
    sb.append("    orderTrack: ").append(toIndentedString(orderTrack)).append("\n");
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

