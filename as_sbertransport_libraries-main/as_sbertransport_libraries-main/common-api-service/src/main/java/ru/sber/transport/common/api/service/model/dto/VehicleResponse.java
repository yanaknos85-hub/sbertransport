package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

/**
 * VehicleResponse
 */


public class VehicleResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("mark")
  private String mark;

  @JsonProperty("model")
  private String model;

  @JsonProperty("color")
  private String color;

  @JsonProperty("registrationNumber")
  private String registrationNumber;

  public VehicleResponse mark(String mark) {
    this.mark = mark;
    return this;
  }

  /**
   * Get mark
   * @return mark
  */
  
  @Schema(name = "mark", required = false)
  public String getMark() {
    return mark;
  }

  public void setMark(String mark) {
    this.mark = mark;
  }

  public VehicleResponse model(String model) {
    this.model = model;
    return this;
  }

  /**
   * Get model
   * @return model
  */
  
  @Schema(name = "model", required = false)
  public String getModel() {
    return model;
  }

  public void setModel(String model) {
    this.model = model;
  }

  public VehicleResponse color(String color) {
    this.color = color;
    return this;
  }

  /**
   * Get color
   * @return color
  */
  
  @Schema(name = "color", required = false)
  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public VehicleResponse registrationNumber(String registrationNumber) {
    this.registrationNumber = registrationNumber;
    return this;
  }

  /**
   * Get registrationNumber
   * @return registrationNumber
  */
  
  @Schema(name = "registrationNumber", required = false)
  public String getRegistrationNumber() {
    return registrationNumber;
  }

  public void setRegistrationNumber(String registrationNumber) {
    this.registrationNumber = registrationNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var vehicleResponse = (VehicleResponse) o;
    return Objects.equals(this.mark, vehicleResponse.mark) &&
        Objects.equals(this.model, vehicleResponse.model) &&
        Objects.equals(this.color, vehicleResponse.color) &&
        Objects.equals(this.registrationNumber, vehicleResponse.registrationNumber);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mark, model, color, registrationNumber);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class VehicleResponse {\n");
    sb.append("    mark: ").append(toIndentedString(mark)).append("\n");
    sb.append("    model: ").append(toIndentedString(model)).append("\n");
    sb.append("    color: ").append(toIndentedString(color)).append("\n");
    sb.append("    registrationNumber: ").append(toIndentedString(registrationNumber)).append("\n");
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

