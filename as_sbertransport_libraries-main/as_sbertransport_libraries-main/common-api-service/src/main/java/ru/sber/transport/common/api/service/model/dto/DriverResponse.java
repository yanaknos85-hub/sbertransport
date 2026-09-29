package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.Objects;

/**
 * DriverResponse
 */


public class DriverResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("id")
  private Long id;

  @JsonProperty("name")
  private String name;

  @JsonProperty("patronimyc") // так поле пишется в аналитике
  private String patronymic; // намеренно поставленно в правильной грамматике

  @JsonProperty("secName")
  private String secName;

  @JsonProperty("phone")
  private String phone;

  @JsonProperty("imageUrl")
  private String imageUrl;

  @JsonProperty("rating")
  private Double rating;

  @JsonProperty("companyId")
  private Integer companyId;

  @JsonProperty("license")
  private LicenseResponse license;

  @JsonProperty("vehicle")
  private VehicleResponse vehicle;

  public DriverResponse id(Long id) {
    this.id = id;
    return this;
  }

  /**
   * Get id
   * @return id
  */
  
  @Schema(name = "id", required = false)
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public DriverResponse name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
  */
  
  @Schema(name = "name", required = false)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public DriverResponse patronymic(String patronymic) {
    this.patronymic = patronymic;
    return this;
  }

  /**
   * Get patronymic
   * @return patronymic
  */
  
  @Schema(name = "patronymic", required = false)
  public String getPatronymic() {
    return patronymic;
  }

  public void setPatronymic(String patronymic) {
    this.patronymic = patronymic;
  }

  public DriverResponse secName(String secName) {
    this.secName = secName;
    return this;
  }

  /**
   * Get secName
   * @return secName
  */
  
  @Schema(name = "secName", required = false)
  public String getSecName() {
    return secName;
  }

  public void setSecName(String secName) {
    this.secName = secName;
  }

  public DriverResponse phone(String phone) {
    this.phone = phone;
    return this;
  }

  /**
   * Get phone
   * @return phone
  */
  
  @Schema(name = "phone", required = false)
  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public DriverResponse imageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
    return this;
  }

  /**
   * Get imageUrl
   * @return imageUrl
  */
  
  @Schema(name = "imageUrl", required = false)
  public String getImageUrl() {
    return imageUrl;
  }

  public void setImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public DriverResponse rating(Double rating) {
    this.rating = rating;
    return this;
  }

  /**
   * Get rating
   * @return rating
  */
  
  @Schema(name = "rating", required = false)
  public Double getRating() {
    return rating;
  }

  public void setRating(Double rating) {
    this.rating = rating;
  }

  public DriverResponse companyId(Integer companyId) {
    this.companyId = companyId;
    return this;
  }

  /**
   * Get companyId
   * @return companyId
  */
  
  @Schema(name = "companyId", required = false)
  public Integer getCompanyId() {
    return companyId;
  }

  public void setCompanyId(Integer companyId) {
    this.companyId = companyId;
  }

  public DriverResponse license(LicenseResponse license) {
    this.license = license;
    return this;
  }

  /**
   * Get license
   * @return license
  */
  @Valid 
  @Schema(name = "license", required = false)
  public LicenseResponse getLicense() {
    return license;
  }

  public void setLicense(LicenseResponse license) {
    this.license = license;
  }

  public DriverResponse vehicle(VehicleResponse vehicle) {
    this.vehicle = vehicle;
    return this;
  }

  /**
   * Get vehicle
   * @return vehicle
  */
  @Valid 
  @Schema(name = "vehicle", required = false)
  public VehicleResponse getVehicle() {
    return vehicle;
  }

  public void setVehicle(VehicleResponse vehicle) {
    this.vehicle = vehicle;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var driverResponse = (DriverResponse) o;
    return Objects.equals(this.id, driverResponse.id) &&
        Objects.equals(this.name, driverResponse.name) &&
        Objects.equals(this.patronymic, driverResponse.patronymic) &&
        Objects.equals(this.secName, driverResponse.secName) &&
        Objects.equals(this.phone, driverResponse.phone) &&
        Objects.equals(this.imageUrl, driverResponse.imageUrl) &&
        Objects.equals(this.rating, driverResponse.rating) &&
        Objects.equals(this.companyId, driverResponse.companyId) &&
        Objects.equals(this.license, driverResponse.license) &&
        Objects.equals(this.vehicle, driverResponse.vehicle);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, patronymic, secName, phone, imageUrl, rating, companyId, license, vehicle);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class DriverResponse {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    patronymic: ").append(toIndentedString(patronymic)).append("\n");
    sb.append("    secName: ").append(toIndentedString(secName)).append("\n");
    sb.append("    phone: ").append(toIndentedString(phone)).append("\n");
    sb.append("    imageUrl: ").append(toIndentedString(imageUrl)).append("\n");
    sb.append("    rating: ").append(toIndentedString(rating)).append("\n");
    sb.append("    companyId: ").append(toIndentedString(companyId)).append("\n");
    sb.append("    license: ").append(toIndentedString(license)).append("\n");
    sb.append("    vehicle: ").append(toIndentedString(vehicle)).append("\n");
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

  public String toInfoString() {
    var sb = new StringBuilder();
    sb.append(name).append(" ");
    sb.append(patronymic).append(" ");
    sb.append(secName).append(" ");
    sb.append(phone).append(" ");
    sb.append(vehicle);
    return sb.toString();
  }
}

