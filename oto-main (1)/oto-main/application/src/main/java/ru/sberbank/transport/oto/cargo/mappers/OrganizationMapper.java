package ru.sberbank.transport.oto.cargo.mappers;

import org.mapstruct.*;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.transport.oto.cargo.database.model.Organization;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganizationMapper {

    Organization fromMessage(OrganizationMessage message);

    @Mapping(target = "id", ignore = true)
    Organization update(Organization source, @MappingTarget Organization target);

}
