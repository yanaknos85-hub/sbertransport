package ru.sber.transport.etrn.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import ru.sber.transport.etrn.database.model.Organization;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganizationMapper {

    Organization fromMessage(OrganizationMessage message);

    @Mapping(target = "id", ignore = true)
    Organization update(Organization source, @MappingTarget Organization target);
}