package ru.sberbank.ditsib.transport.reports.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.reports.controller.UserPreferencesController;
import ru.sberbank.ditsib.transport.reports.dto.user.UserPreferencesRequestDTO;
import ru.sberbank.ditsib.transport.reports.dto.user.UserPreferencesResponseDTO;
import ru.sberbank.ditsib.transport.reports.service.UserPreferencesService;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@E2EController
public class UserPreferencesControllerImpl implements UserPreferencesController {

    private final UserPreferencesService userPreferencesService;

    @Override
    public UserPreferencesResponseDTO getPreferencesByUserId(UUID userID, String nameForm) {
        return userPreferencesService.getPreferencesByIdAndNameForm(userID, nameForm);
    }

    @Override
    public void updateUserPreferences(UserPreferencesRequestDTO requestDTO) {
        userPreferencesService.updateUserPreferences(requestDTO);
    }
}
