package ru.sber.transport.notifications.services.impl.commands.groupTransfer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Проверка сервиса всех настройок уведомлений GroupTransferApproved")
@UnitTest
class GroupTransferAwaitingSearchCommandTest {

    private final GroupTransferAwaitingSearchCommand command =
            new GroupTransferAwaitingSearchCommand(null, null, null,null);

    @Test
    void getNotificationType() {
        assertThat(command.getNotificationType()).isEqualTo(NotificationType.GROUP_TRANSFER_AWAITING_SEARCH);
    }

    @Test
    void getCommandName() {
        assertThat(command.getCommandName()).isEqualTo(command.getClass().getSimpleName());
    }

    @Test
    void getDescription() {
        assertThat(command.getDescription()).isEqualTo("notice_4004");
    }

    @Test
    void getExpectedStatus() {
        assertThat(command.getExpectedStatus()).isEqualTo(NotificationType.GROUP_TRANSFER_AWAITING_SEARCH.name());
    }

    @Test
    void getReceiverIds() {
        UUID passengerId = UUID.randomUUID();
        TripRequest tripRequest = TripRequest.builder()
                .passengerId(passengerId)
                .build();

        assertThat(command.getReceiverIds(tripRequest)).contains(passengerId);
    }
}