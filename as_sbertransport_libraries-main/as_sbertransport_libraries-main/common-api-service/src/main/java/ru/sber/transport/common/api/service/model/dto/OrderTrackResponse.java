package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OrderTrackResponse
 */


public class OrderTrackResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("orderPartnerId")
  private String orderPartnerId;

  @JsonProperty("track")
  @Valid
  private List<RouteTrackResponse> track = null;

  public OrderTrackResponse orderPartnerId(String orderPartnerId) {
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

  public OrderTrackResponse track(List<RouteTrackResponse> track) {
    this.track = track;
    return this;
  }

  public OrderTrackResponse addTrackItem(RouteTrackResponse trackItem) {
    if (this.track == null) {
      this.track = new ArrayList<>();
    }
    this.track.add(trackItem);
    return this;
  }

  /**
   * Get track
   * @return track
  */
  @Valid 
  @Schema(name = "track", required = false)
  public List<RouteTrackResponse> getTrack() {
    return track;
  }

  public void setTrack(List<RouteTrackResponse> track) {
    this.track = track;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var orderTrackResponse = (OrderTrackResponse) o;
    return Objects.equals(this.orderPartnerId, orderTrackResponse.orderPartnerId) &&
        Objects.equals(this.track, orderTrackResponse.track);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderPartnerId, track);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class OrderTrackResponse {\n");
    sb.append("    orderPartnerId: ").append(toIndentedString(orderPartnerId)).append("\n");
    sb.append("    track: ").append(toIndentedString(track)).append("\n");
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

