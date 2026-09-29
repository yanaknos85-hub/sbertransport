package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * CalculationResponse
 */


public class CalculationResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("hash")
  private String hash;

  @JsonProperty("tariff")
  private TariffResponse tariff;

  @JsonProperty("track")
  private TrackResponse track;

  @JsonProperty("precalculatedPrice")
  private Double precalculatedPrice;

  @JsonProperty("priceBeforeDiscount")
  private Double priceBeforeDiscount;

  @JsonProperty("discounts")
  @Valid
  private List<DiscountResponse> discounts = null;

  @JsonProperty("route")
  @Valid
  private List<List<Double>> route = null;

  @JsonProperty("eta")
  private Integer eta;

  @JsonProperty("totalPrice")
  private BigDecimal totalPrice;

  public CalculationResponse hash(String hash) {
    this.hash = hash;
    return this;
  }

  /**
   * Get hash
   * @return hash
  */
  
  @Schema(name = "hash", required = false)
  public String getHash() {
    return hash;
  }

  public void setHash(String hash) {
    this.hash = hash;
  }

  public CalculationResponse tariff(TariffResponse tariff) {
    this.tariff = tariff;
    return this;
  }

  /**
   * Get tariff
   * @return tariff
  */
  @Valid 
  @Schema(name = "tariff", required = false)
  public TariffResponse getTariff() {
    return tariff;
  }

  public void setTariff(TariffResponse tariff) {
    this.tariff = tariff;
  }

  public CalculationResponse track(TrackResponse track) {
    this.track = track;
    return this;
  }

  /**
   * Get track
   * @return track
  */
  @Valid 
  @Schema(name = "track", required = false)
  public TrackResponse getTrack() {
    return track;
  }

  public void setTrack(TrackResponse track) {
    this.track = track;
  }

  public CalculationResponse precalculatedPrice(Double precalculatedPrice) {
    this.precalculatedPrice = precalculatedPrice;
    return this;
  }

  /**
   * Get precalculatedPrice
   * @return precalculatedPrice
  */
  
  @Schema(name = "precalculatedPrice", required = false)
  public Double getPrecalculatedPrice() {
    return precalculatedPrice;
  }

  public void setPrecalculatedPrice(Double precalculatedPrice) {
    this.precalculatedPrice = precalculatedPrice;
  }

  public CalculationResponse priceBeforeDiscount(Double priceBeforeDiscount) {
    this.priceBeforeDiscount = priceBeforeDiscount;
    return this;
  }

  /**
   * Get priceBeforeDiscount
   * @return priceBeforeDiscount
  */
  
  @Schema(name = "priceBeforeDiscount", required = false)
  public Double getPriceBeforeDiscount() {
    return priceBeforeDiscount;
  }

  public void setPriceBeforeDiscount(Double priceBeforeDiscount) {
    this.priceBeforeDiscount = priceBeforeDiscount;
  }

  public CalculationResponse discounts(List<DiscountResponse> discounts) {
    this.discounts = discounts;
    return this;
  }

  public CalculationResponse addDiscountsItem(DiscountResponse discountsItem) {
    if (this.discounts == null) {
      this.discounts = new ArrayList<>();
    }
    this.discounts.add(discountsItem);
    return this;
  }

  /**
   * Get discounts
   * @return discounts
  */
  @Valid 
  @Schema(name = "discounts", required = false)
  public List<DiscountResponse> getDiscounts() {
    return discounts;
  }

  public void setDiscounts(List<DiscountResponse> discounts) {
    this.discounts = discounts;
  }

  public CalculationResponse route(List<List<Double>> route) {
    this.route = route;
    return this;
  }

  public CalculationResponse addRouteItem(List<Double> routeItem) {
    if (this.route == null) {
      this.route = new ArrayList<>();
    }
    this.route.add(routeItem);
    return this;
  }

  /**
   * Get route
   * @return route
  */
  @Valid 
  @Schema(name = "route", required = false)
  public List<List<Double>> getRoute() {
    return route;
  }

  public void setRoute(List<List<Double>> route) {
    this.route = route;
  }

  public CalculationResponse eta(Integer eta) {
    this.eta = eta;
    return this;
  }

  /**
   * Get eta
   * @return eta
  */
  
  @Schema(name = "eta", required = false)
  public Integer getEta() {
    return eta;
  }

  public void setEta(Integer eta) {
    this.eta = eta;
  }

  public CalculationResponse totalPrice(BigDecimal totalPrice) {
    this.totalPrice = totalPrice;
    return this;
  }

  /**
   * Get totalPrice
   * @return totalPrice
  */
  @Valid 
  @Schema(name = "totalPrice", required = false)
  public BigDecimal getTotalPrice() {
    return totalPrice;
  }

  public void setTotalPrice(BigDecimal totalPrice) {
    this.totalPrice = totalPrice;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var calculationResponse = (CalculationResponse) o;
    return Objects.equals(this.hash, calculationResponse.hash) &&
        Objects.equals(this.tariff, calculationResponse.tariff) &&
        Objects.equals(this.track, calculationResponse.track) &&
        Objects.equals(this.precalculatedPrice, calculationResponse.precalculatedPrice) &&
        Objects.equals(this.priceBeforeDiscount, calculationResponse.priceBeforeDiscount) &&
        Objects.equals(this.discounts, calculationResponse.discounts) &&
        Objects.equals(this.route, calculationResponse.route) &&
        Objects.equals(this.eta, calculationResponse.eta) &&
        Objects.equals(this.totalPrice, calculationResponse.totalPrice);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hash, tariff, track, precalculatedPrice, priceBeforeDiscount, discounts, route, eta, totalPrice);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class CalculationResponse {\n");
    sb.append("    hash: ").append(toIndentedString(hash)).append("\n");
    sb.append("    tariff: ").append(toIndentedString(tariff)).append("\n");
    sb.append("    track: ").append(toIndentedString(track)).append("\n");
    sb.append("    precalculatedPrice: ").append(toIndentedString(precalculatedPrice)).append("\n");
    sb.append("    priceBeforeDiscount: ").append(toIndentedString(priceBeforeDiscount)).append("\n");
    sb.append("    discounts: ").append(toIndentedString(discounts)).append("\n");
    sb.append("    route: ").append(toIndentedString(route)).append("\n");
    sb.append("    eta: ").append(toIndentedString(eta)).append("\n");
    sb.append("    totalPrice: ").append(toIndentedString(totalPrice)).append("\n");
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

