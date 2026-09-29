package ru.sberbank.ditsib.transport.request.mappers;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.NullValueMappingStrategy;
import ru.sberbank.ditsib.transport.request.messaging.message.DepartmentTripRequestApproversMessage;
import ru.sberbank.ditsib.transport.request.database.model.Approver;

import java.util.Collection;

/**
 * Маппер согласующих подразделения.
 */
@Mapper
public interface DepartmentTripRequestApproversMapper {
    
    Approver toEntity(DepartmentTripRequestApproversMessage.Approver approver);
    
    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    Collection<Approver> toEntities(Collection<DepartmentTripRequestApproversMessage.Approver> approvers);
    
}
