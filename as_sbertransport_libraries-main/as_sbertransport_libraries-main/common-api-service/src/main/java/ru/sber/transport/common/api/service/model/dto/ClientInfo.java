package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.Objects;

/**
 * Информация о Контрагенте
 */

@Schema(name = "ClientInfo", description = "Информация о Контрагенте")

public class ClientInfo  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("paymentType")
  private String paymentType;

  @JsonProperty("brand")
  private String brand;

  @JsonProperty("fullName")
  private String fullName;

  @JsonProperty("balance")
  private Double balance;

  @JsonProperty("limit")
  private Double limit;

  @JsonProperty("mail")
  private String mail;

  @JsonProperty("requisites")
  private ClientRequisitesResponse requisites;

  public ClientInfo paymentType(String paymentType) {
    this.paymentType = paymentType;
    return this;
  }

  /**
   * Get paymentType
   * @return paymentType
  */
  
  @Schema(name = "paymentType", required = false)
  public String getPaymentType() {
    return paymentType;
  }

  public void setPaymentType(String paymentType) {
    this.paymentType = paymentType;
  }

  public ClientInfo brand(String brand) {
    this.brand = brand;
    return this;
  }

  /**
   * Get brand
   * @return brand
  */
  
  @Schema(name = "brand", required = false)
  public String getBrand() {
    return brand;
  }

  public void setBrand(String brand) {
    this.brand = brand;
  }

  public ClientInfo fullName(String fullName) {
    this.fullName = fullName;
    return this;
  }

  /**
   * Get fullName
   * @return fullName
  */
  
  @Schema(name = "fullName", required = false)
  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public ClientInfo balance(Double balance) {
    this.balance = balance;
    return this;
  }

  /**
   * Get balance
   * @return balance
  */
  
  @Schema(name = "balance", required = false)
  public Double getBalance() {
    return balance;
  }

  public void setBalance(Double balance) {
    this.balance = balance;
  }

  public ClientInfo limit(Double limit) {
    this.limit = limit;
    return this;
  }

  /**
   * Get limit
   * @return limit
  */
  
  @Schema(name = "limit", required = false)
  public Double getLimit() {
    return limit;
  }

  public void setLimit(Double limit) {
    this.limit = limit;
  }

  public ClientInfo mail(String mail) {
    this.mail = mail;
    return this;
  }

  /**
   * Get mail
   * @return mail
  */
  
  @Schema(name = "mail", required = false)
  public String getMail() {
    return mail;
  }

  public void setMail(String mail) {
    this.mail = mail;
  }

  public ClientInfo requisites(ClientRequisitesResponse requisites) {
    this.requisites = requisites;
    return this;
  }

  /**
   * Get requisites
   * @return requisites
  */
  @Valid 
  @Schema(name = "requisites", required = false)
  public ClientRequisitesResponse getRequisites() {
    return requisites;
  }

  public void setRequisites(ClientRequisitesResponse requisites) {
    this.requisites = requisites;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var clientInfo = (ClientInfo) o;
    return Objects.equals(this.paymentType, clientInfo.paymentType) &&
        Objects.equals(this.brand, clientInfo.brand) &&
        Objects.equals(this.fullName, clientInfo.fullName) &&
        Objects.equals(this.balance, clientInfo.balance) &&
        Objects.equals(this.limit, clientInfo.limit) &&
        Objects.equals(this.mail, clientInfo.mail) &&
        Objects.equals(this.requisites, clientInfo.requisites);
  }

  @Override
  public int hashCode() {
    return Objects.hash(paymentType, brand, fullName, balance, limit, mail, requisites);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class ClientInfo {\n");
    sb.append("    paymentType: ").append(toIndentedString(paymentType)).append("\n");
    sb.append("    brand: ").append(toIndentedString(brand)).append("\n");
    sb.append("    fullName: ").append(toIndentedString(fullName)).append("\n");
    sb.append("    balance: ").append(toIndentedString(balance)).append("\n");
    sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
    sb.append("    mail: ").append(toIndentedString(mail)).append("\n");
    sb.append("    requisites: ").append(toIndentedString(requisites)).append("\n");
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

