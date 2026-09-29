package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

/**
 * CouponResponse
 */


public class CouponResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("promocode")
  private String promocode;

  @JsonProperty("description")
  private String description;

  public CouponResponse promocode(String promocode) {
    this.promocode = promocode;
    return this;
  }

  /**
   * Get promocode
   * @return promocode
  */
  
  @Schema(name = "promocode", required = false)
  public String getPromocode() {
    return promocode;
  }

  public void setPromocode(String promocode) {
    this.promocode = promocode;
  }

  public CouponResponse description(String description) {
    this.description = description;
    return this;
  }

  /**
   * Get description
   * @return description
  */
  
  @Schema(name = "description", required = false)
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var couponResponse = (CouponResponse) o;
    return Objects.equals(this.promocode, couponResponse.promocode) &&
        Objects.equals(this.description, couponResponse.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(promocode, description);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class CouponResponse {\n");
    sb.append("    promocode: ").append(toIndentedString(promocode)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

