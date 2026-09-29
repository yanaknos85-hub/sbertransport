package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.dao.userNotificationSettings.UserNotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.HasContactData;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelSettings;
import ru.sber.transport.notifications.database.model.settings.channel.ChannelType;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.userNotification.UserNotificationSettings;
import ru.sber.transport.notifications.dto.userNotification.UserNotificationSettingsDto;
import ru.sber.transport.notifications.dto.userNotification.UserNotificationSettingsSearchDto;
import ru.sber.transport.notifications.mapper.userNotificationSettings.UserNotificationSettingsMapper;
import ru.sber.transport.notifications.services.UserNotificationSettingsService;
import ru.sber.transport.notifications.services.UserNotificationSettingsSpecService;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.sber.transport.notifications.database.model.settings.channel.ChannelType.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserNotificationSettingsServiceImpl implements UserNotificationSettingsService {

    private final UserNotificationSettingsMapper mapper;
    private final UserNotificationSettingsRepository repository;
    private final UserNotificationSettingsSpecService<UserNotificationSettingsSearchDto> specService;
    private final NotificationSettingsRepository notificationSettingsRepository;
    private final Random random = new SecureRandom();

    @Override
    @Transactional
    public Page<UserNotificationSettingsDto> get(UserNotificationSettingsSearchDto searchDto) {
        var notificationClass = searchDto.getNotificationClass().stream().findFirst()
                .orElseThrow(() -> new IllegalArgumentException("NotificationClass not found"));

        repository.findMissedSettings(searchDto.getUserId(), searchDto.getParentId(), notificationClass.name()).stream()
                .map(settings -> mapToNewUserNotificationSettings(settings, searchDto.getUserId()))
                .forEach(repository::save);

        return search(searchDto);
    }

    @Override
    public UserNotificationSettingsDto updateByUser(UUID id, UserNotificationSettingsDto userNotificationSettings) {
        var result = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Settings notification not found"));

        result.setPushActive(userNotificationSettings.isPushActive());
        result.setEmailActive(userNotificationSettings.isEmailActive());
        result.setSmsActive(userNotificationSettings.isSmsActive());

        return mapper.toDto(repository.save(result));
    }

    @Override
    public Page<UserNotificationSettingsDto> search(UserNotificationSettingsSearchDto searchDto) {
        var spec = specService.getSpec(searchDto);

        PageRequest pageRequest = UserNotificationSettingsSearchDto.getPageRequest(searchDto);
        return repository.findAll(spec, pageRequest).map(mapper::toDto);
    }

    @Override
    public void saveByEngineerCorpClient(NotificationSettings newSettings) {
        long start = System.currentTimeMillis();
        // todo заменить на библиотеку логирование когда она будет готова
        var logId = random.nextInt(1001) + 1000;
        log.debug("CREATE_USER_SETTINGS: id = {}, началось создание пользовательских настроек параллельном потоке, {}", logId, start);
        Thread thread = new Thread(() -> saveNotificationSettingsAsUserNotificationSettings(newSettings));
        thread.start();
        long finish = System.currentTimeMillis();
        long elapsed = finish - start;
        log.debug("CREATE_USER_SETTINGS: id = {}, создание завершено в параллельном потоке, время потребовавшееся на запуск {}, длительность запуска elapsed {}", logId, finish, elapsed);
    }

    @Override
    public void updateChannelMap(HasContactData receiver, Map<ChannelType, String> messages, UUID notificationId) {
        if (receiver instanceof Employee employee) {
            repository.findByUserIdAndNotificationId(employee.getId(), notificationId)
                    .ifPresent(userSettings-> doUpdateChannelMap(messages, userSettings));
        }
    }

    private void doUpdateChannelMap(Map<ChannelType, String> messages, UserNotificationSettings userSettings) {
        if (!userSettings.isPushActive()) {
            messages.remove(PUSH);
        }
        if (!userSettings.isEmailActive()) {
            messages.remove(EMAIL);
        }
        if (!userSettings.isSmsActive()) {
            messages.remove(SMS);
        }
    }

    private void saveNotificationSettingsAsUserNotificationSettings(NotificationSettings newSettings) {
        var result = repository.findByParentIdAndNotificationId(newSettings.getParentId(), newSettings.getId()).stream()
                .map(userNotification -> mapCurrentUserNotificationSettings(newSettings, userNotification))
                .toList();

        if (result.isEmpty()) {
            result = repository.findByParentId(newSettings.getParentId()).stream()
                    .map(userId -> mapToNewUserNotificationSettings(newSettings, userId))
                    .toList();
        }

        repository.saveAll(result);
    }

    private UserNotificationSettings mapToNewUserNotificationSettings(NotificationSettings settings, UUID userId) {
        var map = settings.getChannels().stream().collect(Collectors.groupingBy(ChannelSettings::getChannel));

        var disabledChanel = new ChannelSettings(null, null, null, false, null);

        return UserNotificationSettings.builder()
                .userId(userId)
                .notificationId(settings.getId())
                .notificationClass(settings.getNotificationClass())
                .name(settings.getName())
                .pushActive(true)
                .emailActive(true)
                .smsActive(true)
                .emailEnabled(map.get(ChannelType.EMAIL).stream().findFirst().orElse(disabledChanel).isActive())
                .pushEnabled(map.get(ChannelType.PUSH).stream().findFirst().orElse(disabledChanel).isActive())
                .smsEnabled(map.get(ChannelType.SMS).stream().findFirst().orElse(disabledChanel).isActive())
                .parentId(settings.getParentId())
                .build();
    }

    private UserNotificationSettings mapCurrentUserNotificationSettings(NotificationSettings settings, UserNotificationSettings current) {
        var map = settings.getChannels().stream().collect(Collectors.groupingBy(ChannelSettings::getChannel));

        var disabledChanel = new ChannelSettings(null, null, null, false, null);

        current.setEmailEnabled(map.get(ChannelType.EMAIL).stream().findFirst().orElse(disabledChanel).isActive());
        current.setPushEnabled(map.get(ChannelType.PUSH).stream().findFirst().orElse(disabledChanel).isActive());
        current.setSmsEnabled(map.get(ChannelType.SMS).stream().findFirst().orElse(disabledChanel).isActive());

        return current;
    }

}
