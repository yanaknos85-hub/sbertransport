package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.*;
import ru.sberbank.ditsib.transport.constants.LimitType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.limits.LimitServiceType;
import ru.sberbank.ditsib.transport.constants.limits.LimitSharingType;
import ru.sberbank.ditsib.transport.constants.limits.LimitStatus;
import ru.sberbank.ditsib.transport.messaging.messages.LimitMessage;
import ru.sberbank.ditsib.transport.messaging.messages.OrganizationMessage;
import ru.sberbank.ditsib.transport.reports.dto.OrganizationShortDTO;
import ru.sberbank.ditsib.transport.reports.model.Limit;
import ru.sberbank.ditsib.transport.reports.model.Organization;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrganizationMapper {

    Organization fromMessage(OrganizationMessage message);

    @Mapping(target = "id", ignore = true)
    Organization update(Organization source, @MappingTarget Organization target);
    
    OrganizationShortDTO toOrganizationShortDTO(Organization organization);
    
    List<OrganizationShortDTO> toOrganizationShortDTOList(List<Organization> organizationList);

}
