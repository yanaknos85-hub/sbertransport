package ru.sber.transport.contractor.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetBranchDto {

    @NotNull
    @Schema(description = "Идентификатор организации")
    private UUID organizationId;

    @Schema(description = "Наименование филиала")
    private String name;

    @Schema(description = "Номер страницы")
    private int page = 0;

    @Schema(description = "Размер страницы")
    private int size = 20;

    @Schema(description = "Идентификатор отдела")
    private UUID departmentId;

}
