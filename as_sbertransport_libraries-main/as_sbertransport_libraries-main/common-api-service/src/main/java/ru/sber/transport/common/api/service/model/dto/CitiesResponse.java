package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Справочник городов
 */

@Schema(name = "CitiesResponse", description = "Справочник городов")

public class CitiesResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("isSuccess")
  private Boolean isSuccess;

  @JsonProperty("cities")
  @Valid
  private List<CityInfo> cities = null;

  public CitiesResponse isSuccess(Boolean isSuccess) {
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

  public CitiesResponse cities(List<CityInfo> cities) {
    this.cities = cities;
    return this;
  }

  public CitiesResponse addCitiesItem(CityInfo citiesItem) {
    if (this.cities == null) {
      this.cities = new ArrayList<>();
    }
    this.cities.add(citiesItem);
    return this;
  }

  /**
   * Get cities
   * @return cities
  */
  @Valid 
  @Schema(name = "cities", required = false)
  public List<CityInfo> getCities() {
    return cities;
  }

  public void setCities(List<CityInfo> cities) {
    this.cities = cities;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var citiesResponse = (CitiesResponse) o;
    return Objects.equals(this.isSuccess, citiesResponse.isSuccess) &&
        Objects.equals(this.cities, citiesResponse.cities);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isSuccess, cities);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class CitiesResponse {\n");
    sb.append("    isSuccess: ").append(toIndentedString(isSuccess)).append("\n");
    sb.append("    cities: ").append(toIndentedString(cities)).append("\n");
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

