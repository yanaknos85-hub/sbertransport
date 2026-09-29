package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Objects;

/**
 * Contact
 */


public class Contact  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("phone")
  private String phone;

  @JsonProperty("name")
  private String name;

  public Contact phone(String phone) {
    this.phone = phone;
    return this;
  }

  /**
   * Get phone
   * @return phone
  */
  
  @Schema(name = "phone", required = false)
  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public Contact name(String name) {
    this.name = name;
    return this;
  }

  /**
   * Get name
   * @return name
  */
  
  @Schema(name = "name", required = false)
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var contact = (Contact) o;
    return Objects.equals(this.phone, contact.phone) &&
        Objects.equals(this.name, contact.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(phone, name);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class Contact {\n");
    sb.append("    phone: ").append(toIndentedString(phone)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
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

