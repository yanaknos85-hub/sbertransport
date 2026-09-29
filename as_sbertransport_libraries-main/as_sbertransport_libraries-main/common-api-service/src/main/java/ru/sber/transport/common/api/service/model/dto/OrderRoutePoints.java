package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * OrderRoutePoints
 */


public class OrderRoutePoints  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("source")
  private Source source;

  @JsonProperty("destination")
  private Destination destination;

  @JsonProperty("waypoints")
  @Valid
  private List<Waypoint> waypoints = null;

  public OrderRoutePoints source(Source source) {
    this.source = source;
    return this;
  }

  /**
   * Get source
   * @return source
  */
  @Valid 
  @Schema(name = "source", required = false)
  public Source getSource() {
    return source;
  }

  public void setSource(Source source) {
    this.source = source;
  }

  public OrderRoutePoints destination(Destination destination) {
    this.destination = destination;
    return this;
  }

  /**
   * Get destination
   * @return destination
  */
  @Valid 
  @Schema(name = "destination", required = false)
  public Destination getDestination() {
    return destination;
  }

  public void setDestination(Destination destination) {
    this.destination = destination;
  }

  public OrderRoutePoints waypoints(List<Waypoint> waypoints) {
    this.waypoints = waypoints;
    return this;
  }

  public OrderRoutePoints addWaypointsItem(Waypoint waypointsItem) {
    if (this.waypoints == null) {
      this.waypoints = new ArrayList<>();
    }
    this.waypoints.add(waypointsItem);
    return this;
  }

  /**
   * Get waypoints
   * @return waypoints
  */
  @Valid 
  @Schema(name = "waypoints", required = false)
  public List<Waypoint> getWaypoints() {
    return waypoints;
  }

  public void setWaypoints(List<Waypoint> waypoints) {
    this.waypoints = waypoints;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var orderRoutePoints = (OrderRoutePoints) o;
    return Objects.equals(this.source, orderRoutePoints.source) &&
        Objects.equals(this.destination, orderRoutePoints.destination) &&
        Objects.equals(this.waypoints, orderRoutePoints.waypoints);
  }

  @Override
  public int hashCode() {
    return Objects.hash(source, destination, waypoints);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class OrderRoutePoints {\n");
    sb.append("    source: ").append(toIndentedString(source)).append("\n");
    sb.append("    destination: ").append(toIndentedString(destination)).append("\n");
    sb.append("    waypoints: ").append(toIndentedString(waypoints)).append("\n");
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

