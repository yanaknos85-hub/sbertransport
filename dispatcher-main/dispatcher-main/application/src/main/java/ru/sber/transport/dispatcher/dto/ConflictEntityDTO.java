package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@Schema(title = "Данные о конфликтующей сущности", description = "Данные о конфликтующей сущности")
public class ConflictEntityDTO {

    @Schema(description = "Идентификатор конфликтующей сущности")
    private UUID id;

    @Schema(description = "Наименование конфликтующей сущности")
    private String name;

}
