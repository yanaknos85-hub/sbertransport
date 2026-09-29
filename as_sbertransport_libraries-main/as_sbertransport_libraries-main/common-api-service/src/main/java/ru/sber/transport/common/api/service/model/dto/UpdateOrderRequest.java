package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * UpdateOrderRequest
 */


public class UpdateOrderRequest  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("orderPartnerId")
  private String orderPartnerId;

  @JsonProperty("calculationHash")
  private String calculationHash;

  @JsonProperty("collectionTime")
  private String collectionTime;

  @JsonProperty("routePoints")
  private OrderRoutePoints routePoints;

  @JsonProperty("options")
  @Valid
  private List<String> options = null;

  @JsonProperty("comment")
  private String comment;

  @JsonProperty("purpose")
  private String purpose;

  public UpdateOrderRequest orderPartnerId(String orderPartnerId) {
    this.orderPartnerId = orderPartnerId;
    return this;
  }

  /**
   * Get orderPartnerId
   * @return orderPartnerId
  */
  
  @Schema(name = "orderPartnerId", required = false)
  public String getOrderPartnerId() {
    return orderPartnerId;
  }

  public void setOrderPartnerId(String orderPartnerId) {
    this.orderPartnerId = orderPartnerId;
  }

  public UpdateOrderRequest calculationHash(String calculationHash) {
    this.calculationHash = calculationHash;
    return this;
  }

  /**
   * Get calculationHash
   * @return calculationHash
  */
  
  @Schema(name = "calculationHash", required = false)
  public String getCalculationHash() {
    return calculationHash;
  }

  public void setCalculationHash(String calculationHash) {
    this.calculationHash = calculationHash;
  }

  public UpdateOrderRequest collectionTime(String collectionTime) {
    this.collectionTime = collectionTime;
    return this;
  }

  /**
   * Get collectionTime
   * @return collectionTime
  */
  
  @Schema(name = "collectionTime", required = false)
  public String getCollectionTime() {
    return collectionTime;
  }

  public void setCollectionTime(String collectionTime) {
    this.collectionTime = collectionTime;
  }

  public UpdateOrderRequest routePoints(OrderRoutePoints routePoints) {
    this.routePoints = routePoints;
    return this;
  }

  /**
   * Get routePoints
   * @return routePoints
  */
  @Valid 
  @Schema(name = "routePoints", required = false)
  public OrderRoutePoints getRoutePoints() {
    return routePoints;
  }

  public void setRoutePoints(OrderRoutePoints routePoints) {
    this.routePoints = routePoints;
  }

  public UpdateOrderRequest options(List<String> options) {
    this.options = options;
    return this;
  }

  public UpdateOrderRequest addOptionsItem(String optionsItem) {
    if (this.options == null) {
      this.options = new ArrayList<>();
    }
    this.options.add(optionsItem);
    return this;
  }

  /**
   * Get options
   * @return options
  */
  
  @Schema(name = "options", required = false)
  public List<String> getOptions() {
    return options;
  }

  public void setOptions(List<String> options) {
    this.options = options;
  }

  public UpdateOrderRequest comment(String comment) {
    this.comment = comment;
    return this;
  }

  /**
   * Get comment
   * @return comment
  */
  
  @Schema(name = "comment", required = false)
  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public UpdateOrderRequest purpose(String purpose) {
    this.purpose = purpose;
    return this;
  }

  /**
   * Get purpose
   * @return purpose
  */
  
  @Schema(name = "purpose", required = false)
  public String getPurpose() {
    return purpose;
  }

  public void setPurpose(String purpose) {
    this.purpose = purpose;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var updateOrderRequest = (UpdateOrderRequest) o;
    return Objects.equals(this.orderPartnerId, updateOrderRequest.orderPartnerId) &&
        Objects.equals(this.calculationHash, updateOrderRequest.calculationHash) &&
        Objects.equals(this.collectionTime, updateOrderRequest.collectionTime) &&
        Objects.equals(this.routePoints, updateOrderRequest.routePoints) &&
        Objects.equals(this.options, updateOrderRequest.options) &&
        Objects.equals(this.comment, updateOrderRequest.comment) &&
        Objects.equals(this.purpose, updateOrderRequest.purpose);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderPartnerId, calculationHash, collectionTime, routePoints, options, comment, purpose);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class UpdateOrderRequest {\n");
    sb.append("    orderPartnerId: ").append(toIndentedString(orderPartnerId)).append("\n");
    sb.append("    calculationHash: ").append(toIndentedString(calculationHash)).append("\n");
    sb.append("    collectionTime: ").append(toIndentedString(collectionTime)).append("\n");
    sb.append("    routePoints: ").append(toIndentedString(routePoints)).append("\n");
    sb.append("    options: ").append(toIndentedString(options)).append("\n");
    sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
    sb.append("    purpose: ").append(toIndentedString(purpose)).append("\n");
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

