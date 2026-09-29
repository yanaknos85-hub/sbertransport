package ru.sberbank.ditsib.transport.request.dto.personal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PersonalCarDTO {
    /**
     * Identifier
     */
    @NotNull
    @Schema(description = "Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;

    /**
     * Bran name
     */
    @Schema(description = "Марка")
    private String brandName;

    /**
     * Model name
     */
    @Schema(description = "Модель")
    private String model;

    /**
     * Registrationnumber
     */
    @NotBlank
    @Schema(description = "Гос. номер", requiredMode = Schema.RequiredMode.REQUIRED)
    private String registrationNumber;

    /**
     * registration certificate
     */
    @NotBlank
    @Schema(description = "Номер свидетельства о регистрации", requiredMode = Schema.RequiredMode.REQUIRED)
    private String registrationCertificate;

    /**
     * Engine volume
     */
    @Schema(description = "Объем двигателя, см^3")
    private int engineVolume;

    /**
     * Insurance number
     */
    @Schema(description = "Номер ОСАГО")
    private String insuranceNumber;

    /**
     * Corporate user
     */
    @JsonIgnore
    private UUID employeeId;

    /**
     * Owner info
     */
    @Schema(description = "Информация о владельце")
    private String ownerInfo;

}
