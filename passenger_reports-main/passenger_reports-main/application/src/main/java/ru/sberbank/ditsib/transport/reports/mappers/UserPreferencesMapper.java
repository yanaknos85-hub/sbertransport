package ru.sberbank.ditsib.transport.reports.mappers;

import org.mapstruct.*;
import ru.sberbank.ditsib.transport.reports.dto.user.Control;
import ru.sberbank.ditsib.transport.reports.dto.user.Settings;
import ru.sberbank.ditsib.transport.reports.dto.user.UserPreferencesRequestDTO;
import ru.sberbank.ditsib.transport.reports.dto.user.UserPreferencesResponseDTO;
import ru.sberbank.ditsib.transport.reports.model.user.UserControls;
import ru.sberbank.ditsib.transport.reports.model.user.UserPreferences;
import ru.sberbank.ditsib.transport.reports.model.user.UserProperty;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserPreferencesMapper {

    @Mapping(target = "statusCode", constant = "0")
    @Mapping(target = "statusDescription", constant = "ошибок нет")
    @Mapping(source = "userControlsList", target = "controls")
    @Mapping(source = "userId", target = "userID")
    UserPreferencesResponseDTO toUserPreferencesResponseSuccessResult(UserPreferences userPreferences);

    @Mapping(target = "statusCode", constant = "1")
    @Mapping(target = "statusDescription", constant = "данные для userID и nameForm отсутствуют")
    @Mapping(source = "userControlsList", target = "controls")
    UserPreferencesResponseDTO toUserPreferencesResponseErrorResult(UserPreferences userPreferences);

    @Mapping(source = "controls", target = "userControlsList")
    @Mapping(source = "userID", target = "userId")
    UserPreferences toUserPreferences(UserPreferencesRequestDTO userPreferencesRequestDTO);

    @Mapping(source = "userPropertyList", target = "settings")
    Control toControl(UserControls userControls);
    
    Settings toSettings(UserProperty userProperty);

    @Mapping(source = "settings", target = "userPropertyList")
    UserControls toUserControls(Control control);
    
    UserProperty toUserProperty(Settings settings);
}
