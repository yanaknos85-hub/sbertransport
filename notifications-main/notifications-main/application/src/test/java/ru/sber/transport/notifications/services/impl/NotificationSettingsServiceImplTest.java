package ru.sber.transport.notifications.services.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.dao.messages.corp.OrganizationRepository;
import ru.sber.transport.notifications.database.dao.settings.DefaultNotificationSettingsRepository;
import ru.sber.transport.notifications.database.dao.settings.NotificationSettingsRepository;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.mapper.RoleMapperImpl;
import ru.sber.transport.notifications.mapper.settings.*;
import ru.sber.transport.notifications.services.DefaultNotificationSettingsService;
import ru.sber.transport.notifications.services.NotificationService;
import ru.sber.transport.notifications.services.NotificationSettingsService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@DisplayName("Проверка сервиса настроек уведомлений")
class NotificationSettingsServiceImplTest {

    private final NotificationSettingsRepository repository = mock(NotificationSettingsRepository.class);
    private final NotificationService notificationService = mock(NotificationService.class);
    private final DefaultNotificationSettingsService defaultNotificationService = mock(DefaultNotificationSettingsService.class);
    private final ChannelSettingsMapper channelSettingsMapper = new ChannelSettingsMapperImpl();
    private final CountSettingsMapper countingSettingsMapper = new CountSettingsMapperImpl();
    private final RestrictionSettingsMapper restrictionsMapper = new RestrictionSettingsMapperImpl(new RoleMapperImpl());
    private final TimingSettingsMapper timingsMapper = new TimingSettingsMapperImpl();
    private final NotificationSettingsMapper mapper = new NotificationSettingsMapperImpl(channelSettingsMapper, countingSettingsMapper, restrictionsMapper, timingsMapper);
    private final OrganizationRepository organizationRepository = mock(OrganizationRepository.class);
    private final DefaultNotificationSettingsRepository defaultNotificaitonSettingsRepository = mock(DefaultNotificationSettingsRepository.class);
    private final NotificationSettingsService service = new NotificationSettingsServiceImpl(repository, notificationService, defaultNotificationService, mapper, organizationRepository, defaultNotificaitonSettingsRepository);

    @Test
    @DisplayName("Проверка добавления настроек для нового организации")
    void test_addSettingsForNewOrganization() {
        final var organizationId = UUID.randomUUID();
        final var defaultNotificationSettings = Instancio.ofList(NotificationSettings.class).create();

        when(defaultNotificationService.getDefaultSettings()).thenReturn(defaultNotificationSettings);

        service.addSettingsForNewOrganization(organizationId);

        final var settingsCaptor = ArgumentCaptor.forClass(NotificationSettings.class);

        verify(repository, times(defaultNotificationSettings.size())).save(settingsCaptor.capture());

        assertThat(defaultNotificationSettings).hasSameSizeAs(settingsCaptor.getAllValues());

        for (int i = 0; i < settingsCaptor.getAllValues().size(); i++) {
            final var actual = settingsCaptor.getAllValues().get(i);
            final var expected = defaultNotificationSettings.get(i);

            assertSoftly(it -> {
                it.assertThat(actual.getText()).isEqualTo(expected.getText());
                it.assertThat(actual.getName()).isEqualTo(expected.getName());
                it.assertThat(actual.getDescription()).isEqualTo(expected.getDescription());
                it.assertThat(actual.getNotificationClass()).isEqualTo(expected.getNotificationClass());
                it.assertThat(actual.getOwnerId()).isNull();
                it.assertThat(actual.getParentId()).isEqualTo(organizationId);
                it.assertThat(actual.getParentType()).isEqualTo(expected.getParentType());
                it.assertThat(actual.getRestrictions().getRestrictType()).isEqualTo(expected.getRestrictions().getRestrictType());
            });
        }
    }

}