package ru.sberbank.ditsib.transport.reports.dto.personalTransport;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PersonalCarDTO implements Serializable {
    @NotNull
    @Schema(description = "Идентификатор")
    private UUID id;

    @Schema(description = "Марка")
    private String brandName;

    @Schema(description = "Модель")
    private String model;

    @NotBlank
    @Schema(description = "Гос. номер")
    private String registrationNumber;

    @NotBlank
    @Schema(description = "Номер свидетельства о регистрации")
    private String registrationCertificate;

    @Schema(description = "Объем двигателя, см^3")
    private int engineVolume;

    @Schema(description = "Номер ОСАГО")
    private String insuranceNumber;

    @JsonIgnore
    private UUID employeeId;

    @Schema(description = "Информация о владельце")
    private String ownerInfo;

}