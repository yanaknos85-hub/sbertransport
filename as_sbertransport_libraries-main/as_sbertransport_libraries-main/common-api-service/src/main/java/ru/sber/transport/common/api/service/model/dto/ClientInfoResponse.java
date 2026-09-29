package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.Objects;

/**
 * Информация о Контрагенте
 */

@Schema(name = "ClientInfoResponse", description = "Информация о Контрагенте")

public class ClientInfoResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("isSuccess")
  private Boolean isSuccess;

  @JsonProperty("corporation")
  private ClientInfo corporation;

  public ClientInfoResponse isSuccess(Boolean isSuccess) {
    this.isSuccess = isSuccess;
    return this;
  }

  /**
   * Обработка завершена успешно
   * @return isSuccess
  */
  
  @Schema(name = "isSuccess", description = "Обработка завершена успешно", required = false)
  public Boolean isIsSuccess() {
    return isSuccess;
  }

  public void setIsSuccess(Boolean isSuccess) {
    this.isSuccess = isSuccess;
  }

  public ClientInfoResponse corporation(ClientInfo corporation) {
    this.corporation = corporation;
    return this;
  }

  /**
   * Get corporation
   * @return corporation
  */
  @Valid 
  @Schema(name = "corporation", required = false)
  public ClientInfo getCorporation() {
    return corporation;
  }

  public void setCorporation(ClientInfo corporation) {
    this.corporation = corporation;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var clientInfoResponse = (ClientInfoResponse) o;
    return Objects.equals(this.isSuccess, clientInfoResponse.isSuccess) &&
        Objects.equals(this.corporation, clientInfoResponse.corporation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isSuccess, corporation);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class ClientInfoResponse {\n");
    sb.append("    isSuccess: ").append(toIndentedString(isSuccess)).append("\n");
    sb.append("    corporation: ").append(toIndentedString(corporation)).append("\n");
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

