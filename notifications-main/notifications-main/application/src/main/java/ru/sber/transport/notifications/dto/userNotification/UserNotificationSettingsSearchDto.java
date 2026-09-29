package ru.sber.transport.notifications.dto.userNotification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.userNotification.UserNotificationSettings;
import ru.sber.transport.notifications.enums.UserNotificationSettingsSortOption;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Builder
@Schema(name = "Параметры поиска пользовательских настроек уведомлений")
public class UserNotificationSettingsSearchDto {

    @Schema(name = "ИД пользователя")
    private UUID userId;

    @Schema(name = "ИД организации или контрактора")
    private UUID parentId;

    @Schema(name = "Класс уведомлений")
    private Set<NotificationClass> notificationClass;

    @Schema(description = "Настройки сортировки")
    private SortSetting sortSetting;

    @Schema(description = "Настройки разделения на страницы")
    private PageSetting pageSetting;

    public static PageRequest getPageRequest(UserNotificationSettingsSearchDto requestSearchDTO) {
        Sort sort = getSort(requestSearchDTO);
        if (requestSearchDTO == null || requestSearchDTO.getPageSetting() == null) {
            return PageRequest.of(0, 10, sort);
        }
        return PageRequest.of(requestSearchDTO.getPageSetting().getPage(), requestSearchDTO.getPageSetting().getSize(), sort);
    }

    @Getter
    @Setter
    @Builder
    @ToString
    public static class SortSetting {
        @Schema(description = "Выбор сортировки")
        private UserNotificationSettingsSortOption property = UserNotificationSettingsSortOption.NAME;

        @Schema(description = "Направление сортировки")
        private boolean directionAsc = true;
    }

    @Getter
    @Setter
    @Builder
    @ToString
    public static class PageSetting {
        @Schema(description = "Номер страницы")
        private int page = 0;

        @Schema(description = "Количество элементов на странице")
        private int size = 20;
    }

    public static Sort getSort(UserNotificationSettingsSearchDto searchDto) {
        if (searchDto == null || searchDto.getSortSetting() == null) {
            return Sort.sort(UserNotificationSettings.class).by(UserNotificationSettings::getName).ascending();
        }

        Sort sort = switch (searchDto.getSortSetting().getProperty()) {
            case NOTIFICATION_CLASS ->
                    Sort.sort(UserNotificationSettings.class).by(UserNotificationSettings::getNotificationClass);
            case PUSH_ACTIVE -> Sort.sort(UserNotificationSettings.class).by(UserNotificationSettings::isPushActive);
            case PUSH_ENABLED -> Sort.sort(UserNotificationSettings.class).by(UserNotificationSettings::isPushEnabled);
            case EMAIL_ACTIVE -> Sort.sort(UserNotificationSettings.class).by(UserNotificationSettings::isEmailActive);
            case EMAIL_ENABLED ->
                    Sort.sort(UserNotificationSettings.class).by(UserNotificationSettings::isEmailEnabled);
            case SMS_ACTIVE -> Sort.sort(UserNotificationSettings.class).by(UserNotificationSettings::isSmsActive);
            case SMS_ENABLED -> Sort.sort(UserNotificationSettings.class).by(UserNotificationSettings::isSmsEnabled);
            default -> Sort.sort(UserNotificationSettings.class).by(UserNotificationSettings::getName);
        };

        return searchDto.getSortSetting().isDirectionAsc() ? sort.ascending() : sort.descending();
    }
}
