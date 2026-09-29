package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.Objects;

/**
 * ShortOrderInfoResponseExtra
 */


public class ShortOrderInfoResponseExtra  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("orderPartnerId")
  private String orderPartnerId;

  @JsonProperty("tariff")
  private Integer tariff;

  @JsonProperty("createOrderTime")
  private String createOrderTime;

  @JsonProperty("collectionTime")
  private String collectionTime;

  @JsonProperty("routePoints")
  private OrderRoutePoints routePoints;

  @JsonProperty("price")
  private Double price;

  @JsonProperty("distance")
  private Double distance;

  @JsonProperty("statusCode")
  private Integer statusCode;

  @JsonProperty("finishTime")
  private String finishTime;

  public ShortOrderInfoResponseExtra orderPartnerId(String orderPartnerId) {
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

  public ShortOrderInfoResponseExtra tariff(Integer tariff) {
    this.tariff = tariff;
    return this;
  }

  /**
   * Get tariff
   * @return tariff
  */
  
  @Schema(name = "tariff", required = false)
  public Integer getTariff() {
    return tariff;
  }

  public void setTariff(Integer tariff) {
    this.tariff = tariff;
  }

  public ShortOrderInfoResponseExtra createOrderTime(String createOrderTime) {
    this.createOrderTime = createOrderTime;
    return this;
  }

  /**
   * Get createOrderTime
   * @return createOrderTime
  */
  
  @Schema(name = "createOrderTime", required = false)
  public String getCreateOrderTime() {
    return createOrderTime;
  }

  public void setCreateOrderTime(String createOrderTime) {
    this.createOrderTime = createOrderTime;
  }

  public ShortOrderInfoResponseExtra collectionTime(String collectionTime) {
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

  public ShortOrderInfoResponseExtra routePoints(OrderRoutePoints routePoints) {
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

  public ShortOrderInfoResponseExtra price(Double price) {
    this.price = price;
    return this;
  }

  /**
   * Get price
   * @return price
  */
  
  @Schema(name = "price", required = false)
  public Double getPrice() {
    return price;
  }

  public void setPrice(Double price) {
    this.price = price;
  }

  public ShortOrderInfoResponseExtra distance(Double distance) {
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

  public ShortOrderInfoResponseExtra statusCode(Integer statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  /**
   * Get statusCode
   * @return statusCode
  */
  
  @Schema(name = "statusCode", required = false)
  public Integer getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(Integer statusCode) {
    this.statusCode = statusCode;
  }

  public ShortOrderInfoResponseExtra finishTime(String finishTime) {
    this.finishTime = finishTime;
    return this;
  }

  /**
   * Get finishTime
   * @return finishTime
  */
  
  @Schema(name = "finishTime", required = false)
  public String getFinishTime() {
    return finishTime;
  }

  public void setFinishTime(String finishTime) {
    this.finishTime = finishTime;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var shortOrderInfoResponseExtra = (ShortOrderInfoResponseExtra) o;
    return Objects.equals(this.orderPartnerId, shortOrderInfoResponseExtra.orderPartnerId) &&
        Objects.equals(this.tariff, shortOrderInfoResponseExtra.tariff) &&
        Objects.equals(this.createOrderTime, shortOrderInfoResponseExtra.createOrderTime) &&
        Objects.equals(this.collectionTime, shortOrderInfoResponseExtra.collectionTime) &&
        Objects.equals(this.routePoints, shortOrderInfoResponseExtra.routePoints) &&
        Objects.equals(this.price, shortOrderInfoResponseExtra.price) &&
        Objects.equals(this.distance, shortOrderInfoResponseExtra.distance) &&
        Objects.equals(this.statusCode, shortOrderInfoResponseExtra.statusCode) &&
        Objects.equals(this.finishTime, shortOrderInfoResponseExtra.finishTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderPartnerId, tariff, createOrderTime, collectionTime, routePoints, price, distance, statusCode, finishTime);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class ShortOrderInfoResponseExtra {\n");
    sb.append("    orderPartnerId: ").append(toIndentedString(orderPartnerId)).append("\n");
    sb.append("    tariff: ").append(toIndentedString(tariff)).append("\n");
    sb.append("    createOrderTime: ").append(toIndentedString(createOrderTime)).append("\n");
    sb.append("    collectionTime: ").append(toIndentedString(collectionTime)).append("\n");
    sb.append("    routePoints: ").append(toIndentedString(routePoints)).append("\n");
    sb.append("    price: ").append(toIndentedString(price)).append("\n");
    sb.append("    distance: ").append(toIndentedString(distance)).append("\n");
    sb.append("    statusCode: ").append(toIndentedString(statusCode)).append("\n");
    sb.append("    finishTime: ").append(toIndentedString(finishTime)).append("\n");
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

