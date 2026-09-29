package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.Objects;

/**
 * TariffInfo
 */


public class TariffInfo  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("id")
  private Long id;

  @JsonProperty("name")
  private String name;

  @JsonProperty("startPrice")
  private Double startPrice;

  @JsonProperty("oneKmPrice")
  private Double oneKmPrice;

  @JsonProperty("oneMinPrice")
  private Double oneMinPrice;

  @JsonProperty("freeWaitMinutes")
  private Integer freeWaitMinutes;

  @JsonProperty("waitTimePrice")
  private Double waitTimePrice;

  @JsonProperty("cancellationPrice")
  private Double cancellationPrice;

  @JsonProperty("options")
  private OptionsTariffResponse options;

  public TariffInfo id(Long id) {
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

  public TariffInfo name(String name) {
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

  public TariffInfo startPrice(Double startPrice) {
    this.startPrice = startPrice;
    return this;
  }

  /**
   * Get startPrice
   * @return startPrice
  */
  
  @Schema(name = "startPrice", required = false)
  public Double getStartPrice() {
    return startPrice;
  }

  public void setStartPrice(Double startPrice) {
    this.startPrice = startPrice;
  }

  public TariffInfo oneKmPrice(Double oneKmPrice) {
    this.oneKmPrice = oneKmPrice;
    return this;
  }

  /**
   * Get oneKmPrice
   * @return oneKmPrice
  */
  
  @Schema(name = "oneKmPrice", required = false)
  public Double getOneKmPrice() {
    return oneKmPrice;
  }

  public void setOneKmPrice(Double oneKmPrice) {
    this.oneKmPrice = oneKmPrice;
  }

  public TariffInfo oneMinPrice(Double oneMinPrice) {
    this.oneMinPrice = oneMinPrice;
    return this;
  }

  /**
   * Get oneMinPrice
   * @return oneMinPrice
  */
  
  @Schema(name = "oneMinPrice", required = false)
  public Double getOneMinPrice() {
    return oneMinPrice;
  }

  public void setOneMinPrice(Double oneMinPrice) {
    this.oneMinPrice = oneMinPrice;
  }

  public TariffInfo freeWaitMinutes(Integer freeWaitMinutes) {
    this.freeWaitMinutes = freeWaitMinutes;
    return this;
  }

  /**
   * Get freeWaitMinutes
   * @return freeWaitMinutes
  */
  
  @Schema(name = "freeWaitMinutes", required = false)
  public Integer getFreeWaitMinutes() {
    return freeWaitMinutes;
  }

  public void setFreeWaitMinutes(Integer freeWaitMinutes) {
    this.freeWaitMinutes = freeWaitMinutes;
  }

  public TariffInfo waitTimePrice(Double waitTimePrice) {
    this.waitTimePrice = waitTimePrice;
    return this;
  }

  /**
   * Get waitTimePrice
   * @return waitTimePrice
  */
  
  @Schema(name = "waitTimePrice", required = false)
  public Double getWaitTimePrice() {
    return waitTimePrice;
  }

  public void setWaitTimePrice(Double waitTimePrice) {
    this.waitTimePrice = waitTimePrice;
  }

  public TariffInfo cancellationPrice(Double cancellationPrice) {
    this.cancellationPrice = cancellationPrice;
    return this;
  }

  /**
   * Get cancellationPrice
   * @return cancellationPrice
  */
  
  @Schema(name = "cancellationPrice", required = false)
  public Double getCancellationPrice() {
    return cancellationPrice;
  }

  public void setCancellationPrice(Double cancellationPrice) {
    this.cancellationPrice = cancellationPrice;
  }

  public TariffInfo options(OptionsTariffResponse options) {
    this.options = options;
    return this;
  }

  /**
   * Get options
   * @return options
  */
  @Valid 
  @Schema(name = "options", required = false)
  public OptionsTariffResponse getOptions() {
    return options;
  }

  public void setOptions(OptionsTariffResponse options) {
    this.options = options;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var tariffInfo = (TariffInfo) o;
    return Objects.equals(this.id, tariffInfo.id) &&
        Objects.equals(this.name, tariffInfo.name) &&
        Objects.equals(this.startPrice, tariffInfo.startPrice) &&
        Objects.equals(this.oneKmPrice, tariffInfo.oneKmPrice) &&
        Objects.equals(this.oneMinPrice, tariffInfo.oneMinPrice) &&
        Objects.equals(this.freeWaitMinutes, tariffInfo.freeWaitMinutes) &&
        Objects.equals(this.waitTimePrice, tariffInfo.waitTimePrice) &&
        Objects.equals(this.cancellationPrice, tariffInfo.cancellationPrice) &&
        Objects.equals(this.options, tariffInfo.options);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, startPrice, oneKmPrice, oneMinPrice, freeWaitMinutes, waitTimePrice, cancellationPrice, options);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class TariffInfo {\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    startPrice: ").append(toIndentedString(startPrice)).append("\n");
    sb.append("    oneKmPrice: ").append(toIndentedString(oneKmPrice)).append("\n");
    sb.append("    oneMinPrice: ").append(toIndentedString(oneMinPrice)).append("\n");
    sb.append("    freeWaitMinutes: ").append(toIndentedString(freeWaitMinutes)).append("\n");
    sb.append("    waitTimePrice: ").append(toIndentedString(waitTimePrice)).append("\n");
    sb.append("    cancellationPrice: ").append(toIndentedString(cancellationPrice)).append("\n");
    sb.append("    options: ").append(toIndentedString(options)).append("\n");
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

