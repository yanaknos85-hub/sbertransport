package ru.sber.transport.common.api.service.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Cправочник тарифов
 */

@Schema(name = "TariffsResponse", description = "Cправочник тарифов")

public class TariffsResponse  implements Serializable {

  private static final long serialVersionUID = 1L;

  @JsonProperty("isSuccess")
  private Boolean isSuccess;

  @JsonProperty("tariffs")
  @Valid
  private List<TariffInfo> tariffs = null;

  public TariffsResponse isSuccess(Boolean isSuccess) {
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

  public TariffsResponse tariffs(List<TariffInfo> tariffs) {
    this.tariffs = tariffs;
    return this;
  }

  public TariffsResponse addTariffsItem(TariffInfo tariffsItem) {
    if (this.tariffs == null) {
      this.tariffs = new ArrayList<>();
    }
    this.tariffs.add(tariffsItem);
    return this;
  }

  /**
   * Get tariffs
   * @return tariffs
  */
  @Valid 
  @Schema(name = "tariffs", required = false)
  public List<TariffInfo> getTariffs() {
    return tariffs;
  }

  public void setTariffs(List<TariffInfo> tariffs) {
    this.tariffs = tariffs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    var tariffsResponse = (TariffsResponse) o;
    return Objects.equals(this.isSuccess, tariffsResponse.isSuccess) &&
        Objects.equals(this.tariffs, tariffsResponse.tariffs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isSuccess, tariffs);
  }

  @Override
  public String toString() {
    var sb = new StringBuilder();
    sb.append("class TariffsResponse {\n");
    sb.append("    isSuccess: ").append(toIndentedString(isSuccess)).append("\n");
    sb.append("    tariffs: ").append(toIndentedString(tariffs)).append("\n");
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

