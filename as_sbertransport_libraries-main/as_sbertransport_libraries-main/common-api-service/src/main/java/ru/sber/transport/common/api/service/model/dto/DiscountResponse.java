package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.Objects;

/**
 * DiscountResponse
 */


public class DiscountResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("type")
  private String type;

  @JsonProperty("absolutePrice")
  private Double absolutePrice;

  @JsonProperty("percent")
  private Integer percent;

  @JsonProperty("coupon")
  private CouponResponse coupon;

  public DiscountResponse type(String type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   * @return type
  */
  
  @Schema(name = "type", required = false)
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public DiscountResponse absolutePrice(Double absolutePrice) {
    this.absolutePrice = absolutePrice;
    return this;
  }

  /**
   * Get absolutePrice
   * @return absolutePrice
  */
  
  @Schema(name = "absolutePrice", required = false)
  public Double getAbsolutePrice() {
    return absolutePrice;
  }

  public void setAbsolutePrice(Double absolutePrice) {
    this.absolutePrice = absolutePrice;
  }

  public DiscountResponse percent(Integer percent) {
    this.percent = percent;
    return this;
  }

  /**
   * Get percent
   * @return percent
  */
  
  @Schema(name = "percent", required = false)
  public Integer getPercent() {
    return percent;
  }

  public void setPercent(Integer percent) {
    this.percent = percent;
  }

  public DiscountResponse coupon(CouponResponse coupon) {
    this.coupon = coupon;
    return this;
  }

  /**
   * Get coupon
   * @return coupon
  */
  @Valid 
  @Schema(name = "coupon", required = false)
  public CouponResponse getCoupon() {
    return coupon;
  }

  public void setCoupon(CouponResponse coupon) {
    this.coupon = coupon;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var discountResponse = (DiscountResponse) o;
    return Objects.equals(this.type, discountResponse.type) &&
        Objects.equals(this.absolutePrice, discountResponse.absolutePrice) &&
        Objects.equals(this.percent, discountResponse.percent) &&
        Objects.equals(this.coupon, discountResponse.coupon);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, absolutePrice, percent, coupon);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class DiscountResponse {\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    absolutePrice: ").append(toIndentedString(absolutePrice)).append("\n");
    sb.append("    percent: ").append(toIndentedString(percent)).append("\n");
    sb.append("    coupon: ").append(toIndentedString(coupon)).append("\n");
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

