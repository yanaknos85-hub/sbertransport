package ru.sber.transport.notifications.messaging.listeners.mappers;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.model.ContactWithEmail;
import ru.sber.transport.notifications.database.model.ContactWithPhone;
import ru.sber.transport.notifications.database.model.ContactWithPhoneAndEmail;
import ru.sber.transport.notifications.database.model.NotificationContact;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@DisplayName("Проверка маппера контактов")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class NotificationContactMapperTest {

    private final NotificationContactMapper notificationContactMapper = new NotificationContactMapperImpl();

    @ParameterizedTest
    @MethodSource("dataSource")
    @DisplayName("Проверка маппера контактов")
    void testMapper(Class<?> clz, NotificationContact data) {
        // when
        var mapped = notificationContactMapper.toContactData(data);

        // then
        if (clz != null) {
            assertThat(mapped)
                    .isPresent();

            assertThat(clz.isAssignableFrom(mapped.get().getClass()))
                    .isTrue();

        } else {
            assertThat(mapped)
                    .isNotPresent();
        }
    }

    private static Stream<Arguments> dataSource() {
        return Stream.of(
                Arguments.of(ContactWithEmail.class, Instancio.of(NotificationContact.class)
                        .ignore(field(NotificationContact::getPhone))
                        .ignore(field(NotificationContact::isPhoneConfirmed))
                        .create()),
                Arguments.of(ContactWithPhone.class, Instancio.of(NotificationContact.class)
                        .ignore(field(NotificationContact::getEmail))
                        .create()),
                Arguments.of(ContactWithPhoneAndEmail.class, Instancio.create(NotificationContact.class)),
                Arguments.of(null, Instancio.of(NotificationContact.class)
                        .ignore(field(NotificationContact::getPhone))
                        .ignore(field(NotificationContact::isPhoneConfirmed))
                        .ignore(field(NotificationContact::getEmail))
                        .create())
        );
    }

}