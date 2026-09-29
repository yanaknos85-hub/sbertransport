package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.dispatcher.database.model.ActiveStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Признак водителя", description = "Новый признак водителя")
public class NewAttributeDTO {

    @NotBlank
    @Size(max = 128, min = 1)
    @Schema(description = "Наименование признака", minLength = 1, maxLength = 128)
    private String name;

    @Schema(description = "Информация о контрагенте")
    private ContractorDTO contractor;

    @Schema(description = "Признак активности")
    @Builder.Default
    private ActiveStatus status = ActiveStatus.ACTIVE;
}
