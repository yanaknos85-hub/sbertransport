package ru.sberbank.ditsib.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.ReportingPolicy;
import ru.sberbank.ditsib.database.model.Employee;
import ru.sberbank.ditsib.database.model.Lead;
import ru.sberbank.ditsib.dto.lead.LeadExcelDto;
import ru.sberbank.ditsib.dto.lead.LeadRequestDto;
import ru.sberbank.ditsib.dto.lead.LeadResponseDto;
import ru.sberbank.ditsib.dto.point.PointLeadRequestDto;

/**
 * Маппер для преобразования пользовательских лидов
 */
@Mapper(uses = {PointLeadMapper.class, EmployeeMapper.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS)
public interface LeadMapper {

    /**
     * Преобразует Lead entity в доменную модель
     *
     * @param lead сущность из базы данных
     * @return доменная модель заявки
     */
    LeadResponseDto toDto(Lead lead);

    /**
     * Конвертирует одну Excel строку в LeadRequestDto.
     */
    @Mapping(target = "comment", ignore = true)
    @Mapping(target = "isDriver", constant = "false")
    @Mapping(target = "departureTime", expression = "java(java.time.LocalDateTime.of(row.getOrderDate(), row.getOrderTime()))")
    @Mapping(target = "points", expression = "java(java.util.List.of(from, to))")
    LeadRequestDto toLeadRequest(LeadExcelDto row, PointLeadRequestDto from, PointLeadRequestDto to);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainLeadId", ignore = true)
    @Mapping(target = "cost", ignore = true)
    @Mapping(target = "points", ignore = true)
    @Mapping(target = "employee", source = "employee")
    @Mapping(target = "status", constant = "PROCESSING")
    @Mapping(target = "createDateTime", expression = "java(java.time.LocalDateTime.now())")
    Lead toEntity(LeadRequestDto request, Employee employee);
}