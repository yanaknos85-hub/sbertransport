package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.Objects;

/**
 * OrderInfoResponseExtra
 */


public class OrderInfoResponseExtra  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("orderPartnerId")
  private String orderPartnerId;

  @JsonProperty("orderSbertransportId")
  private String orderSbertransportId;

  @JsonProperty("inn")
  private String inn;

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

  @JsonProperty("calculation")
  private CalculationResponse calculation;

  @JsonProperty("passenger")
  private Contact passenger;

  @JsonProperty("performerArrivalTime")
  private String performerArrivalTime;

  @JsonProperty("driver")
  private DriverResponse driver;

  @JsonProperty("comment")
  private String comment;

  @JsonProperty("purpose")
  private String purpose;

  @JsonProperty("webViewLink")
  private String webViewLink;

  @JsonProperty("eta")
  private Integer eta;

  @JsonProperty("isTest")
  private Boolean isTest = false;

  @JsonProperty("waitTime")
  private Integer waitTime;

  @JsonProperty("waitTimeOW")
  private Integer waitTimeOW;

  public OrderInfoResponseExtra orderPartnerId(String orderPartnerId) {
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

  public OrderInfoResponseExtra orderSbertransportId(String orderSbertransportId) {
    this.orderSbertransportId = orderSbertransportId;
    return this;
  }

  /**
   * Get orderSbertransportId
   * @return orderSbertransportId
  */
  
  @Schema(name = "orderSbertransportId", required = false)
  public String getOrderSbertransportId() {
    return orderSbertransportId;
  }

  public void setOrderSbertransportId(String orderSbertransportId) {
    this.orderSbertransportId = orderSbertransportId;
  }

  public OrderInfoResponseExtra inn(String inn) {
    this.inn = inn;
    return this;
  }

  /**
   * Get inn
   * @return inn
  */
  
  @Schema(name = "inn", required = false)
  public String getInn() {
    return inn;
  }

  public void setInn(String inn) {
    this.inn = inn;
  }

  public OrderInfoResponseExtra tariff(Integer tariff) {
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

  public OrderInfoResponseExtra createOrderTime(String createOrderTime) {
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

  public OrderInfoResponseExtra collectionTime(String collectionTime) {
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

  public OrderInfoResponseExtra routePoints(OrderRoutePoints routePoints) {
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

  public OrderInfoResponseExtra price(Double price) {
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

  public OrderInfoResponseExtra distance(Double distance) {
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

  public OrderInfoResponseExtra statusCode(Integer statusCode) {
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

  public OrderInfoResponseExtra finishTime(String finishTime) {
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

  public OrderInfoResponseExtra calculation(CalculationResponse calculation) {
    this.calculation = calculation;
    return this;
  }

  /**
   * Get calculation
   * @return calculation
  */
  @Valid 
  @Schema(name = "calculation", required = false)
  public CalculationResponse getCalculation() {
    return calculation;
  }

  public void setCalculation(CalculationResponse calculation) {
    this.calculation = calculation;
  }

  public OrderInfoResponseExtra passenger(Contact passenger) {
    this.passenger = passenger;
    return this;
  }

  /**
   * Get passenger
   * @return passenger
  */
  @Valid 
  @Schema(name = "passenger", required = false)
  public Contact getPassenger() {
    return passenger;
  }

  public void setPassenger(Contact passenger) {
    this.passenger = passenger;
  }

  public OrderInfoResponseExtra performerArrivalTime(String performerArrivalTime) {
    this.performerArrivalTime = performerArrivalTime;
    return this;
  }

  /**
   * Get performerArrivalTime
   * @return performerArrivalTime
  */
  
  @Schema(name = "performerArrivalTime", required = false)
  public String getPerformerArrivalTime() {
    return performerArrivalTime;
  }

  public void setPerformerArrivalTime(String performerArrivalTime) {
    this.performerArrivalTime = performerArrivalTime;
  }

  public OrderInfoResponseExtra driver(DriverResponse driver) {
    this.driver = driver;
    return this;
  }

  /**
   * Get driver
   * @return driver
  */
  @Valid 
  @Schema(name = "driver", required = false)
  public DriverResponse getDriver() {
    return driver;
  }

  public void setDriver(DriverResponse driver) {
    this.driver = driver;
  }

  public OrderInfoResponseExtra comment(String comment) {
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

  public OrderInfoResponseExtra purpose(String purpose) {
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

  public OrderInfoResponseExtra webViewLink(String webViewLink) {
    this.webViewLink = webViewLink;
    return this;
  }

  /**
   * Get webViewLink
   * @return webViewLink
  */
  
  @Schema(name = "webViewLink", required = false)
  public String getWebViewLink() {
    return webViewLink;
  }

  public void setWebViewLink(String webViewLink) {
    this.webViewLink = webViewLink;
  }

  public OrderInfoResponseExtra eta(Integer eta) {
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

  public OrderInfoResponseExtra isTest(Boolean isTest) {
    this.isTest = isTest;
    return this;
  }

  /**
   * Get isTest
   * @return isTest
  */
  
  @Schema(name = "isTest", required = false)
  public Boolean isIsTest() {
    return isTest;
  }

  public void setIsTest(Boolean isTest) {
    this.isTest = isTest;
  }

  public OrderInfoResponseExtra waitTime(Integer waitTime) {
    this.waitTime = waitTime;
    return this;
  }

  /**
   * Get waitTime
   * @return waitTime
  */
  
  @Schema(name = "waitTime", required = false)
  public Integer getWaitTime() {
    return waitTime;
  }

  public void setWaitTime(Integer waitTime) {
    this.waitTime = waitTime;
  }

  public OrderInfoResponseExtra waitTimeOW(Integer waitTimeOW) {
    this.waitTimeOW = waitTimeOW;
    return this;
  }

  /**
   * Get waitTimeOW
   * @return waitTimeOW
  */
  
  @Schema(name = "waitTimeOW", required = false)
  public Integer getWaitTimeOW() {
    return waitTimeOW;
  }

  public void setWaitTimeOW(Integer waitTimeOW) {
    this.waitTimeOW = waitTimeOW;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var orderInfoResponseExtra = (OrderInfoResponseExtra) o;
    return Objects.equals(this.orderPartnerId, orderInfoResponseExtra.orderPartnerId) &&
        Objects.equals(this.orderSbertransportId, orderInfoResponseExtra.orderSbertransportId) &&
        Objects.equals(this.inn, orderInfoResponseExtra.inn) &&
        Objects.equals(this.tariff, orderInfoResponseExtra.tariff) &&
        Objects.equals(this.createOrderTime, orderInfoResponseExtra.createOrderTime) &&
        Objects.equals(this.collectionTime, orderInfoResponseExtra.collectionTime) &&
        Objects.equals(this.routePoints, orderInfoResponseExtra.routePoints) &&
        Objects.equals(this.price, orderInfoResponseExtra.price) &&
        Objects.equals(this.distance, orderInfoResponseExtra.distance) &&
        Objects.equals(this.statusCode, orderInfoResponseExtra.statusCode) &&
        Objects.equals(this.finishTime, orderInfoResponseExtra.finishTime) &&
        Objects.equals(this.calculation, orderInfoResponseExtra.calculation) &&
        Objects.equals(this.passenger, orderInfoResponseExtra.passenger) &&
        Objects.equals(this.performerArrivalTime, orderInfoResponseExtra.performerArrivalTime) &&
        Objects.equals(this.driver, orderInfoResponseExtra.driver) &&
        Objects.equals(this.comment, orderInfoResponseExtra.comment) &&
        Objects.equals(this.purpose, orderInfoResponseExtra.purpose) &&
        Objects.equals(this.webViewLink, orderInfoResponseExtra.webViewLink) &&
        Objects.equals(this.eta, orderInfoResponseExtra.eta) &&
        Objects.equals(this.isTest, orderInfoResponseExtra.isTest) &&
        Objects.equals(this.waitTime, orderInfoResponseExtra.waitTime) &&
        Objects.equals(this.waitTimeOW, orderInfoResponseExtra.waitTimeOW);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orderPartnerId, orderSbertransportId, inn, tariff, createOrderTime, collectionTime, routePoints, price, distance, statusCode, finishTime, calculation, passenger, performerArrivalTime, driver, comment, purpose, webViewLink, eta, isTest, waitTime, waitTimeOW);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class OrderInfoResponseExtra {\n");
    sb.append("    orderPartnerId: ").append(toIndentedString(orderPartnerId)).append("\n");
    sb.append("    orderSbertransportId: ").append(toIndentedString(orderSbertransportId)).append("\n");
    sb.append("    inn: ").append(toIndentedString(inn)).append("\n");
    sb.append("    tariff: ").append(toIndentedString(tariff)).append("\n");
    sb.append("    createOrderTime: ").append(toIndentedString(createOrderTime)).append("\n");
    sb.append("    collectionTime: ").append(toIndentedString(collectionTime)).append("\n");
    sb.append("    routePoints: ").append(toIndentedString(routePoints)).append("\n");
    sb.append("    price: ").append(toIndentedString(price)).append("\n");
    sb.append("    distance: ").append(toIndentedString(distance)).append("\n");
    sb.append("    statusCode: ").append(toIndentedString(statusCode)).append("\n");
    sb.append("    finishTime: ").append(toIndentedString(finishTime)).append("\n");
    sb.append("    calculation: ").append(toIndentedString(calculation)).append("\n");
    sb.append("    passenger: ").append(toIndentedString(passenger)).append("\n");
    sb.append("    performerArrivalTime: ").append(toIndentedString(performerArrivalTime)).append("\n");
    sb.append("    driver: ").append(toIndentedString(driver)).append("\n");
    sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
    sb.append("    purpose: ").append(toIndentedString(purpose)).append("\n");
    sb.append("    webViewLink: ").append(toIndentedString(webViewLink)).append("\n");
    sb.append("    eta: ").append(toIndentedString(eta)).append("\n");
    sb.append("    isTest: ").append(toIndentedString(isTest)).append("\n");
    sb.append("    waitTime: ").append(toIndentedString(waitTime)).append("\n");
    sb.append("    waitTimeOW: ").append(toIndentedString(waitTimeOW)).append("\n");
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

