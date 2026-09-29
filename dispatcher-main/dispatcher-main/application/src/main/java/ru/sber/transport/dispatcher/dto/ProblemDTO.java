package ru.sber.transport.dispatcher.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@Schema(title = "Данные о проблемах в конфликтующей сущности", description = "Данные о проблемах в конфликтующей сущности")
public class ProblemDTO {

    @Schema(description = "Список ID существующих сущностей, которые вызывают конфликт")
    private List<UUID> conflictEntitiesIds;

    @Schema(description = "Наименование поля конфликтующей сущности")
    private String field;

    @Schema(description = "Значение поля конфликтующей сущности")
    private String value;

}
