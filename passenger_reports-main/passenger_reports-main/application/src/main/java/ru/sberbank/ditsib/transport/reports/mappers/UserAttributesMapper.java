package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.sberbank.ditsib.transport.reports.dto.*;
import ru.sberbank.ditsib.transport.reports.model.attributes.*;

/**
 * Mapper for converting  entity to dto and back
 */
@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL,
        injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface UserAttributesMapper {
    
    UserAttributesDTO userAttributesToDTO(UserAttributes source);
    
    TaxiUIVisibilityDTO taxiUIVisibilityToDTO(TaxiUIVisibility source);
    
    PersonalUIVisibilityDTO personalUIVisibilityToDTO(PersonalUIVisibility source);
    
    PublicUIVisibilityDTO publicUIVisibilityToDTO(PublicUIVisibility source);
    
    CarsharingUIVisibilityDTO carsharingUIVisibilityToDTO(CarsharingUIVisibility source);
    
    
    TaxiUIVisibility dtoToTaxiUIVisibility(TaxiUIVisibilityDTO source);
    
    PublicUIVisibility dtoToPublicUIVisibility(PublicUIVisibilityDTO source);
    
    PersonalUIVisibility dtoToPersonalUIVisibility(PersonalUIVisibilityDTO source);
    
    CarsharingUIVisibility dtoToCarsharingUIVisibility(CarsharingUIVisibilityDTO source);
    
}
