package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.Objects;

/**
 * OrderRequest
 */


public class OrderRequest  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("requestId")
  private String requestId;

  @JsonProperty("humanReadableId")
  private String humanReadableId;

  @JsonProperty("inn")
  private String inn;

  @JsonProperty("statusCode")
  private StatusCode statusCode;

  @JsonProperty("workGroup")
  private String workGroup;

  @JsonProperty("tariff")
  private Integer tariff;

  @JsonProperty("planStartTime")
  private String planStartTime;

  @JsonProperty("routePoints")
  private OrderRoutePoints routePoints;

  @JsonProperty("class")
  private OrderClass propertyClass;

  @JsonProperty("comment")
  private String comment;

  public OrderRequest requestId(String requestId) {
    this.requestId = requestId;
    return this;
  }

  /**
   * ID заявки в АС СберТранспорт
   * @return requestId
  */
  @Schema(name = "requestId", description = "ID заявки в АС СберТранспорт", required = false)
  public String getRequestId() {
    return requestId;
  }

  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }

  public OrderRequest inn(String inn) {
    this.inn = inn;
    return this;
  }

  /**
   * Человекочитаемый ID заявки в АС СберТранспорт
   * @return humanReadableId
   */
  @Schema(name = "humanReadableId", description = "Человекочитаемый ID заявки в АС СберТранспорт", required = false)
  public String getHumanReadableId() { return humanReadableId; }

  public void setHumanReadableId(String humanReadableId) { this.humanReadableId = humanReadableId; }

  public OrderRequest humanReadableId(String humanReadableId) {
    this.humanReadableId = humanReadableId;
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

  public OrderRequest statusCode(StatusCode statusCode) {
    this.statusCode = statusCode;
    return this;
  }

  /**
   * Get statusCode
   * @return statusCode
  */
  @Valid 
  @Schema(name = "statusCode", required = false)
  public StatusCode getStatusCode() {
    return statusCode;
  }

  public void setStatusCode(StatusCode statusCode) {
    this.statusCode = statusCode;
  }

  public OrderRequest workGroup(String workGroup) {
    this.workGroup = workGroup;
    return this;
  }

  /**
   * Get workGroup
   * @return workGroup
  */
  
  @Schema(name = "workGroup", required = false)
  public String getWorkGroup() {
    return workGroup;
  }

  public void setWorkGroup(String workGroup) {
    this.workGroup = workGroup;
  }

  public OrderRequest tariff(Integer tariff) {
    this.tariff = tariff;
    return this;
  }

  /**
   * Идентификатор тарифа
   * @return tariff
  */
  
  @Schema(name = "tariff", description = "Идентификатор тарифа", required = false)
  public Integer getTariff() {
    return tariff;
  }

  public void setTariff(Integer tariff) {
    this.tariff = tariff;
  }

  public OrderRequest planStartTime(String planStartTime) {
    this.planStartTime = planStartTime;
    return this;
  }

  /**
   * Плановое время начала поездки
   * @return planStartTime
  */
  
  @Schema(name = "planStartTime", description = "Плановое время начала поездки", required = false)
  public String getPlanStartTime() {
    return planStartTime;
  }

  public void setPlanStartTime(String planStartTime) {
    this.planStartTime = planStartTime;
  }

  public OrderRequest routePoints(OrderRoutePoints routePoints) {
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

  public OrderRequest propertyClass(OrderClass propertyClass) {
    this.propertyClass = propertyClass;
    return this;
  }

  /**
   * Get propertyClass
   * @return propertyClass
  */
  @Valid 
  @Schema(name = "class", required = false)
  public OrderClass getPropertyClass() {
    return propertyClass;
  }

  public void setPropertyClass(OrderClass propertyClass) {
    this.propertyClass = propertyClass;
  }

  public OrderRequest comment(String comment) {
    this.comment = comment;
    return this;
  }

  /**
   * Дополнительная информация
   * @return comment
  */
  
  @Schema(name = "comment", description = "Дополнительная информация", required = false)
  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var orderRequest = (OrderRequest) o;
    return Objects.equals(this.requestId, orderRequest.requestId) &&
        Objects.equals(this.inn, orderRequest.inn) &&
        Objects.equals(this.statusCode, orderRequest.statusCode) &&
        Objects.equals(this.workGroup, orderRequest.workGroup) &&
        Objects.equals(this.tariff, orderRequest.tariff) &&
        Objects.equals(this.planStartTime, orderRequest.planStartTime) &&
        Objects.equals(this.routePoints, orderRequest.routePoints) &&
        Objects.equals(this.propertyClass, orderRequest.propertyClass) &&
        Objects.equals(this.comment, orderRequest.comment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestId, inn, statusCode, workGroup, tariff, planStartTime, routePoints, propertyClass, comment);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class OrderRequest {\n");
    sb.append("    requestId: ").append(toIndentedString(requestId)).append("\n");
    sb.append("    inn: ").append(toIndentedString(inn)).append("\n");
    sb.append("    statusCode: ").append(toIndentedString(statusCode)).append("\n");
    sb.append("    workGroup: ").append(toIndentedString(workGroup)).append("\n");
    sb.append("    tariff: ").append(toIndentedString(tariff)).append("\n");
    sb.append("    planStartTime: ").append(toIndentedString(planStartTime)).append("\n");
    sb.append("    routePoints: ").append(toIndentedString(routePoints)).append("\n");
    sb.append("    propertyClass: ").append(toIndentedString(propertyClass)).append("\n");
    sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
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

