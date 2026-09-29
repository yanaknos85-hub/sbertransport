package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.sberbank.ditsib.transport.constants.ItinerantType;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;

import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(title = "Поиск заявки", description = "Фильтры для поиска заявок общественного транспорта")
public class RequestPublicSearchDTO extends RequestSearchDTO {

    @Schema(description = "Список идентификаторов организаций")
    private Set<UUID> organizationSet;
/*
    @Schema(description = "Список идентификаторов структурных подразделений")
    private Set<UUID> set;*/

    @Schema(description = "Список идентификаторов подразделений ")
    private Set<UUID> departmentSet;

    @Schema(description = "Место возникновения затрат, МВЗ", maxLength = 20, minLength = 3)
    @Size(max = 20, min = 3)
    private String mvz;

    @Schema(description = "Список характеров деятельности сотрудника")
    private Set<ItinerantType> employeeItinerantTypeSet;

    @Schema(description = "Тип компенсации за поездку на общественном транспорте")
    private PublicCompensationType compensationType;

    @Schema(description = "Дата утверждения поездки")
    @Valid
    private DateRange approvalDate;

}
