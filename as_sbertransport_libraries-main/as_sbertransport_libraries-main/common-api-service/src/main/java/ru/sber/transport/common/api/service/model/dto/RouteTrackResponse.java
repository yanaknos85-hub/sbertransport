package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

/**
 * RouteTrackResponse
 */


public class RouteTrackResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("latitude")
  private Double latitude;

  @JsonProperty("longitude")
  private Double longitude;

  @JsonProperty("time")
  private String time;

  @JsonProperty("speed")
  private String speed;

  @JsonProperty("direction")
  private String direction;

  @JsonProperty("accuracy")
  private String accuracy;

  public RouteTrackResponse latitude(Double latitude) {
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

  public RouteTrackResponse longitude(Double longitude) {
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

  public RouteTrackResponse time(String time) {
    this.time = time;
    return this;
  }

  /**
   * Get time
   * @return time
  */
  
  @Schema(name = "time", required = false)
  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public RouteTrackResponse speed(String speed) {
    this.speed = speed;
    return this;
  }

  /**
   * Get speed
   * @return speed
  */
  
  @Schema(name = "speed", required = false)
  public String getSpeed() {
    return speed;
  }

  public void setSpeed(String speed) {
    this.speed = speed;
  }

  public RouteTrackResponse direction(String direction) {
    this.direction = direction;
    return this;
  }

  /**
   * Get direction
   * @return direction
  */
  
  @Schema(name = "direction", required = false)
  public String getDirection() {
    return direction;
  }

  public void setDirection(String direction) {
    this.direction = direction;
  }

  public RouteTrackResponse accuracy(String accuracy) {
    this.accuracy = accuracy;
    return this;
  }

  /**
   * Get accuracy
   * @return accuracy
  */
  
  @Schema(name = "accuracy", required = false)
  public String getAccuracy() {
    return accuracy;
  }

  public void setAccuracy(String accuracy) {
    this.accuracy = accuracy;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var routeTrackResponse = (RouteTrackResponse) o;
    return Objects.equals(this.latitude, routeTrackResponse.latitude) &&
        Objects.equals(this.longitude, routeTrackResponse.longitude) &&
        Objects.equals(this.time, routeTrackResponse.time) &&
        Objects.equals(this.speed, routeTrackResponse.speed) &&
        Objects.equals(this.direction, routeTrackResponse.direction) &&
        Objects.equals(this.accuracy, routeTrackResponse.accuracy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(latitude, longitude, time, speed, direction, accuracy);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class RouteTrackResponse {\n");
    sb.append("    latitude: ").append(toIndentedString(latitude)).append("\n");
    sb.append("    longitude: ").append(toIndentedString(longitude)).append("\n");
    sb.append("    time: ").append(toIndentedString(time)).append("\n");
    sb.append("    speed: ").append(toIndentedString(speed)).append("\n");
    sb.append("    direction: ").append(toIndentedString(direction)).append("\n");
    sb.append("    accuracy: ").append(toIndentedString(accuracy)).append("\n");
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

