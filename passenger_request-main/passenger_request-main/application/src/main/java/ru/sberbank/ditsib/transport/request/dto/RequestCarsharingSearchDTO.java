package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "Поиск заявки", description = "Фильтры для поиска заявок по каршерингу")
public class RequestCarsharingSearchDTO extends RequestSearchDTO {

    @Schema(description = "Список идентификаторов подразделений")
    private Set<UUID> departmentSet;

    @Schema(description = "Тип поездки")
    private Boolean coopTrip;

}
