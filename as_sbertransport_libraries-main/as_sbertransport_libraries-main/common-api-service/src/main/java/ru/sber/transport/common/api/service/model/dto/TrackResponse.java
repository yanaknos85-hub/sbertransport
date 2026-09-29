package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

/**
 * TrackResponse
 */


public class TrackResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("distance")
  private Double distance;

  @JsonProperty("duration")
  private Double duration;

  public TrackResponse distance(Double distance) {
    this.distance = distance;
    return this;
  }

  /**
   * Get distance
   * @return distance
  */
  
  @Schema(name = "distance", required = false)
  public Double getDistance() {
    return distance;
  }

  public void setDistance(Double distance) {
    this.distance = distance;
  }

  public TrackResponse duration(Double duration) {
    this.duration = duration;
    return this;
  }

  /**
   * Get duration
   * @return duration
  */
  
  @Schema(name = "duration", required = false)
  public Double getDuration() {
    return duration;
  }

  public void setDuration(Double duration) {
    this.duration = duration;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var trackResponse = (TrackResponse) o;
    return Objects.equals(this.distance, trackResponse.distance) &&
        Objects.equals(this.duration, trackResponse.duration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(distance, duration);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class TrackResponse {\n");
    sb.append("    distance: ").append(toIndentedString(distance)).append("\n");
    sb.append("    duration: ").append(toIndentedString(duration)).append("\n");
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

