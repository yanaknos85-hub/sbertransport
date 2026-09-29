package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dao.UserAttributesRepository;
import ru.sberbank.ditsib.transport.reports.dto.*;
import ru.sberbank.ditsib.transport.reports.mappers.UserAttributesMapper;
import ru.sberbank.ditsib.transport.reports.model.attributes.*;
import ru.sberbank.ditsib.transport.reports.service.UserAttributesService;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserAttributesServiceImpl implements UserAttributesService {
    
    private final UserAttributesRepository userAttributesRepository;
    private final UserAttributesMapper mapper;
    
    @Override
    public UserAttributesDTO get(@NotNull UUID userId) {
        return mapper.userAttributesToDTO(
                userAttributesRepository
                        .findByUserId(userId)
                        .orElseGet(() -> UserAttributes.builder()
                                                       .personalUIVisibility(new PersonalUIVisibility())
                                                       .publicUIVisibility(new PublicUIVisibility())
                                                       .taxiUIVisibility(new TaxiUIVisibility())
                                                       .carsharingUIVisibility(new CarsharingUIVisibility()).build()));
    }
    
    @Override
    public UserAttributesDTO getDefaultAttributes() {
        return UserAttributesDTO.builder().taxiUIVisibility(mapper.taxiUIVisibilityToDTO(new TaxiUIVisibility()))
                                .personalUIVisibility(mapper.personalUIVisibilityToDTO(new PersonalUIVisibility()))
                                .publicUIVisibility(mapper.publicUIVisibilityToDTO(new PublicUIVisibility()))
                                .carsharingUIVisibility(mapper.carsharingUIVisibilityToDTO(new CarsharingUIVisibility())).build();
    }
    
    @Override
    public UserAttributesDTO update(@NotNull UUID userId, TaxiUIVisibilityDTO taxiUIVisibilityDTO) {
        var userAttributes = userAttributesRepository.findByUserId(userId).orElse(null);
        if (userAttributes == null) {
            userAttributes = UserAttributes.builder()
                                           .personalUIVisibility(new PersonalUIVisibility())
                                           .publicUIVisibility(new PublicUIVisibility())
                                           .taxiUIVisibility(mapper.dtoToTaxiUIVisibility(taxiUIVisibilityDTO))
                                           .carsharingUIVisibility(new CarsharingUIVisibility()).build();
        } else {
            userAttributes =
                    userAttributes.toBuilder().taxiUIVisibility(mapper.dtoToTaxiUIVisibility(taxiUIVisibilityDTO)).build();
        }
        return mapper.userAttributesToDTO(userAttributesRepository.save(userAttributes));
    }
    
    @Override
    public UserAttributesDTO update(@NotNull UUID userId, PersonalUIVisibilityDTO personalUIVisibilityDTO) {
        var userAttributes = userAttributesRepository.findByUserId(userId).orElse(null);
        if (userAttributes == null) {
            userAttributes = UserAttributes.builder()
                                           .personalUIVisibility(mapper.dtoToPersonalUIVisibility(personalUIVisibilityDTO))
                                           .publicUIVisibility(new PublicUIVisibility())
                                           .taxiUIVisibility(new TaxiUIVisibility())
                                           .carsharingUIVisibility(new CarsharingUIVisibility()).build();
        } else {
            userAttributes =
                    userAttributes.toBuilder().personalUIVisibility(mapper.dtoToPersonalUIVisibility(personalUIVisibilityDTO)).build();
        }
        return mapper.userAttributesToDTO(userAttributesRepository.save(userAttributes));
    }
    
    @Override
    public UserAttributesDTO update(@NotNull UUID userId, PublicUIVisibilityDTO publicUIVisibilityDTO) {
        var userAttributes = userAttributesRepository.findByUserId(userId).orElse(null);
        if (userAttributes == null) {
            userAttributes = UserAttributes.builder()
                                           .personalUIVisibility(new PersonalUIVisibility())
                                           .publicUIVisibility(mapper.dtoToPublicUIVisibility(publicUIVisibilityDTO))
                                           .taxiUIVisibility(new TaxiUIVisibility())
                                           .carsharingUIVisibility(new CarsharingUIVisibility()).build();
        } else {
            userAttributes =
                    userAttributes.toBuilder().publicUIVisibility(mapper.dtoToPublicUIVisibility(publicUIVisibilityDTO)).build();
        }
        return mapper.userAttributesToDTO(userAttributesRepository.save(userAttributes));
    }
    
    @Override
    public UserAttributesDTO update(@NotNull UUID userId, CarsharingUIVisibilityDTO carsharingUIVisibilityDTO) {
        var userAttributes = userAttributesRepository.findByUserId(userId).orElse(null);
        if (userAttributes == null) {
            userAttributes = UserAttributes.builder()
                                           .personalUIVisibility(new PersonalUIVisibility())
                                           .publicUIVisibility(new PublicUIVisibility())
                                           .taxiUIVisibility(new TaxiUIVisibility())
                                           .carsharingUIVisibility(mapper.dtoToCarsharingUIVisibility(carsharingUIVisibilityDTO)).build();
        } else {
            userAttributes =
                    userAttributes.toBuilder().carsharingUIVisibility(mapper.dtoToCarsharingUIVisibility(carsharingUIVisibilityDTO)).build();
        }
        return mapper.userAttributesToDTO(userAttributesRepository.save(userAttributes));
    }
    
}
