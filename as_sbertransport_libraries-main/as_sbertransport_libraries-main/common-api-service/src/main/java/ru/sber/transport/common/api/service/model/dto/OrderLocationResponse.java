package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

/**
 * OrderLocationResponse
 */


public class OrderLocationResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("orderPartnerId")
  private String orderPartnerId;

  @JsonProperty("latitude")
  private Double latitude;

  @JsonProperty("longitude")
  private Double longitude;

  public OrderLocationResponse orderPartnerId(String orderPartnerId) {
    this.orderPartnerId = orderPartnerId;
    return this;
  }

  /**
   * Get orderPartnerId
   * @return orderPartnerId
  */
  
  @Schema(name = "orderPartnerId", required = false)
  public String getOrderPartnerId() {
    return orderPartnerId;
  }

  public void setOrderPartnerId(String orderPartnerId) {
    this.orderPartnerId = orderPartnerId;
  }

  public OrderLocationResponse latitude(Double latitude) {
    this.latitude = latitude;
    return this;
  }

  /**
   * Get latitude
   * @return latitude
  */
  
  @Schema(name = "latitude", required = false)
  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public OrderLocationResponse longitude(Double longitude) {
    this.longitude = longitude;
    return this;
  }

  /**
   * Get longitude
   * @return longitude
  */
  
  @Schema(name = "longitude", required = false)
  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var orderLocationResponse = (OrderLocationResponse) o;
    return Objects.equals(this.orderPartnerId, orderLocationResponse.orderPartnerId) &&
        Objects.equals(this.latitude, orderLocationResponse.latitude) &&
        Objects.equals(this.longitude, orderLocationResponse.longitude);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderPartnerId, latitude, longitude);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class OrderLocationResponse {\n");
    sb.append("    orderPartnerId: ").append(toIndentedString(orderPartnerId)).append("\n");
    sb.append("    latitude: ").append(toIndentedString(latitude)).append("\n");
    sb.append("    longitude: ").append(toIndentedString(longitude)).append("\n");
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

