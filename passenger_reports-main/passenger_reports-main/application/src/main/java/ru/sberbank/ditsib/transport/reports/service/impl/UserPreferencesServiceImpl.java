package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.reports.dao.UserPropertiesRepository;
import ru.sberbank.ditsib.transport.reports.dao.UserControlRepository;
import ru.sberbank.ditsib.transport.reports.dao.UserPreferencesRepository;
import ru.sberbank.ditsib.transport.reports.dto.user.UserPreferencesRequestDTO;
import ru.sberbank.ditsib.transport.reports.dto.user.UserPreferencesResponseDTO;
import ru.sberbank.ditsib.transport.reports.mappers.UserPreferencesMapper;
import ru.sberbank.ditsib.transport.reports.model.user.UserControls;
import ru.sberbank.ditsib.transport.reports.model.user.UserPreferences;
import ru.sberbank.ditsib.transport.reports.model.user.UserProperty;
import ru.sberbank.ditsib.transport.reports.service.UserPreferencesService;

import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class UserPreferencesServiceImpl implements UserPreferencesService {
    
    private final UserPreferencesRepository userPreferencesRepository;
    private final UserControlRepository userControlRepository;
    private final UserPropertiesRepository userPropertiesRepository;
    private final UserPreferencesMapper mapper;
    
    @Override
    @Transactional
    public UserPreferencesResponseDTO getPreferencesByIdAndNameForm(UUID userID, String nameForm) {
        var userPreferencesOptional = userPreferencesRepository.findAByUserIdAndNameForm(userID, nameForm);
        if (userPreferencesOptional.isPresent()) {
            return buildSuccessResponse(userPreferencesOptional.get());
        } else {
            return buildErrorResponse(new UserPreferences());
        }
    }
    
    
    @Override
    @Transactional
    public void updateUserPreferences(UserPreferencesRequestDTO requestDTO) {
        var mappedUserPreferences = mapper.toUserPreferences(requestDTO);
        var preferences = userPreferencesRepository.findAByUserIdAndNameForm(mappedUserPreferences.getUserId(), mappedUserPreferences.getNameForm());
        var mappedUserControlsList = mappedUserPreferences.getUserControlsList();
        preferences.ifPresent(userPreferencesRepository::delete);
        mappedUserControlsList.forEach(control -> saveUserControls(control, createUserPreferences(mappedUserPreferences)));
    }
    
    private void saveUserControls(UserControls control, UserPreferences actualUserPreferences) {
        control.setUserPreferencesId(actualUserPreferences);
        var userPropertyList = control.getUserPropertyList();
        control.setUserPropertyList(null);
        UserControls savedUserControl = userControlRepository.save(control);
        
        savedUserControl.setUserPropertyList(userPropertyList);
        userPropertyList.forEach(userProperty -> saveUserProperty(userProperty, savedUserControl));
    }
    
    private void saveUserProperty(UserProperty userProperty, UserControls savedUserControl) {
        userProperty.setUserControlsId(savedUserControl);
        userPropertiesRepository.save(userProperty);
    }
    
    private UserPreferences createUserPreferences(UserPreferences mappedUserPreferences) {
        mappedUserPreferences.setUserControlsList(null);
        return userPreferencesRepository.save(mappedUserPreferences);
    }
    
    private UserPreferencesResponseDTO buildSuccessResponse(UserPreferences userPreferences) {
        return mapper.toUserPreferencesResponseSuccessResult(userPreferences);
    }
    
    private UserPreferencesResponseDTO buildErrorResponse(UserPreferences userPreferences) {
        return mapper.toUserPreferencesResponseErrorResult(userPreferences);
    }
}
