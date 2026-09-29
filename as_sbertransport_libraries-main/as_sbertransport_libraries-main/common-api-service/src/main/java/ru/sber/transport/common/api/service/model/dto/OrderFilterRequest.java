package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

/**
 * OrderFilterRequest
 */


public class OrderFilterRequest  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("beginDate")
  private String beginDate;

  @JsonProperty("endDate")
  private String endDate;

  @JsonProperty("orderStatus")
  private Integer orderStatus;

  @JsonProperty("passengerPhone")
  private String passengerPhone;

  @JsonProperty("driverId")
  private Integer driverId;

  @JsonProperty("driverPhone")
  private String driverPhone;

  public OrderFilterRequest beginDate(String beginDate) {
    this.beginDate = beginDate;
    return this;
  }

  /**
   * Get beginDate
   * @return beginDate
  */
  
  @Schema(name = "beginDate", required = false)
  public String getBeginDate() {
    return beginDate;
  }

  public void setBeginDate(String beginDate) {
    this.beginDate = beginDate;
  }

  public OrderFilterRequest endDate(String endDate) {
    this.endDate = endDate;
    return this;
  }

  /**
   * Get endDate
   * @return endDate
  */
  
  @Schema(name = "endDate", required = false)
  public String getEndDate() {
    return endDate;
  }

  public void setEndDate(String endDate) {
    this.endDate = endDate;
  }

  public OrderFilterRequest orderStatus(Integer orderStatus) {
    this.orderStatus = orderStatus;
    return this;
  }

  /**
   * Get orderStatus
   * @return orderStatus
  */
  
  @Schema(name = "orderStatus", required = false)
  public Integer getOrderStatus() {
    return orderStatus;
  }

  public void setOrderStatus(Integer orderStatus) {
    this.orderStatus = orderStatus;
  }

  public OrderFilterRequest passengerPhone(String passengerPhone) {
    this.passengerPhone = passengerPhone;
    return this;
  }

  /**
   * Get passengerPhone
   * @return passengerPhone
  */
  
  @Schema(name = "passengerPhone", required = false)
  public String getPassengerPhone() {
    return passengerPhone;
  }

  public void setPassengerPhone(String passengerPhone) {
    this.passengerPhone = passengerPhone;
  }

  public OrderFilterRequest driverId(Integer driverId) {
    this.driverId = driverId;
    return this;
  }

  /**
   * Get driverId
   * @return driverId
  */
  
  @Schema(name = "driverId", required = false)
  public Integer getDriverId() {
    return driverId;
  }

  public void setDriverId(Integer driverId) {
    this.driverId = driverId;
  }

  public OrderFilterRequest driverPhone(String driverPhone) {
    this.driverPhone = driverPhone;
    return this;
  }

  /**
   * Get driverPhone
   * @return driverPhone
  */
  
  @Schema(name = "driverPhone", required = false)
  public String getDriverPhone() {
    return driverPhone;
  }

  public void setDriverPhone(String driverPhone) {
    this.driverPhone = driverPhone;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var orderFilterRequest = (OrderFilterRequest) o;
    return Objects.equals(this.beginDate, orderFilterRequest.beginDate) &&
        Objects.equals(this.endDate, orderFilterRequest.endDate) &&
        Objects.equals(this.orderStatus, orderFilterRequest.orderStatus) &&
        Objects.equals(this.passengerPhone, orderFilterRequest.passengerPhone) &&
        Objects.equals(this.driverId, orderFilterRequest.driverId) &&
        Objects.equals(this.driverPhone, orderFilterRequest.driverPhone);
  }

  @Override
  public int hashCode() {
    return Objects.hash(beginDate, endDate, orderStatus, passengerPhone, driverId, driverPhone);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class OrderFilterRequest {\n");
    sb.append("    beginDate: ").append(toIndentedString(beginDate)).append("\n");
    sb.append("    endDate: ").append(toIndentedString(endDate)).append("\n");
    sb.append("    orderStatus: ").append(toIndentedString(orderStatus)).append("\n");
    sb.append("    passengerPhone: ").append(toIndentedString(passengerPhone)).append("\n");
    sb.append("    driverId: ").append(toIndentedString(driverId)).append("\n");
    sb.append("    driverPhone: ").append(toIndentedString(driverPhone)).append("\n");
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

