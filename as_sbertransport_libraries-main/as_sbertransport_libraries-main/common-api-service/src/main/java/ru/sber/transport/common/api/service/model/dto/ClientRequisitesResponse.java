package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

/**
 * ClientRequisitesResponse
 */


public class ClientRequisitesResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("address")
  private String address;

  @JsonProperty("addressFiz")
  private String addressFiz;

  @JsonProperty("bank")
  private String bank;

  @JsonProperty("bik")
  private String bik;

  @JsonProperty("inn")
  private String inn;

  @JsonProperty("kpp")
  private String kpp;

  @JsonProperty("rs")
  private String rs;

  public ClientRequisitesResponse address(String address) {
    this.address = address;
    return this;
  }

  /**
   * Get address
   * @return address
  */
  
  @Schema(name = "address", required = false)
  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public ClientRequisitesResponse addressFiz(String addressFiz) {
    this.addressFiz = addressFiz;
    return this;
  }

  /**
   * Get addressFiz
   * @return addressFiz
  */
  
  @Schema(name = "addressFiz", required = false)
  public String getAddressFiz() {
    return addressFiz;
  }

  public void setAddressFiz(String addressFiz) {
    this.addressFiz = addressFiz;
  }

  public ClientRequisitesResponse bank(String bank) {
    this.bank = bank;
    return this;
  }

  /**
   * Get bank
   * @return bank
  */
  
  @Schema(name = "bank", required = false)
  public String getBank() {
    return bank;
  }

  public void setBank(String bank) {
    this.bank = bank;
  }

  public ClientRequisitesResponse bik(String bik) {
    this.bik = bik;
    return this;
  }

  /**
   * Get bik
   * @return bik
  */
  
  @Schema(name = "bik", required = false)
  public String getBik() {
    return bik;
  }

  public void setBik(String bik) {
    this.bik = bik;
  }

  public ClientRequisitesResponse inn(String inn) {
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

  public ClientRequisitesResponse kpp(String kpp) {
    this.kpp = kpp;
    return this;
  }

  /**
   * Get kpp
   * @return kpp
  */
  
  @Schema(name = "kpp", required = false)
  public String getKpp() {
    return kpp;
  }

  public void setKpp(String kpp) {
    this.kpp = kpp;
  }

  public ClientRequisitesResponse rs(String rs) {
    this.rs = rs;
    return this;
  }

  /**
   * Get rs
   * @return rs
  */
  
  @Schema(name = "rs", required = false)
  public String getRs() {
    return rs;
  }

  public void setRs(String rs) {
    this.rs = rs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var clientRequisitesResponse = (ClientRequisitesResponse) o;
    return Objects.equals(this.address, clientRequisitesResponse.address) &&
        Objects.equals(this.addressFiz, clientRequisitesResponse.addressFiz) &&
        Objects.equals(this.bank, clientRequisitesResponse.bank) &&
        Objects.equals(this.bik, clientRequisitesResponse.bik) &&
        Objects.equals(this.inn, clientRequisitesResponse.inn) &&
        Objects.equals(this.kpp, clientRequisitesResponse.kpp) &&
        Objects.equals(this.rs, clientRequisitesResponse.rs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(address, addressFiz, bank, bik, inn, kpp, rs);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class ClientRequisitesResponse {\n");
    sb.append("    address: ").append(toIndentedString(address)).append("\n");
    sb.append("    addressFiz: ").append(toIndentedString(addressFiz)).append("\n");
    sb.append("    bank: ").append(toIndentedString(bank)).append("\n");
    sb.append("    bik: ").append(toIndentedString(bik)).append("\n");
    sb.append("    inn: ").append(toIndentedString(inn)).append("\n");
    sb.append("    kpp: ").append(toIndentedString(kpp)).append("\n");
    sb.append("    rs: ").append(toIndentedString(rs)).append("\n");
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

