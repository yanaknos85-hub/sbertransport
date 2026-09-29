package ru.sber.transport.contractor.dto.internal;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchResponseDto {

    @JsonAlias(value = "id")
    @Schema(description = "Идентификатор филиала")
    private UUID externalId;

    @Schema(description = "Название автопарка")
    private String name;

    @JsonAlias(value = "routingId")
    @Schema(description = "Идентификатор подразделения")
    private UUID departmentId;

    @Schema(description = "Нормативное количество автомобилей в филиале")
    private Integer vehicleCountNorm;
}
