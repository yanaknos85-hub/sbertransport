package ru.sber.transport.notifications.services.impl.commands.groupTransfer;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest(properties = {"spring.main.lazy-initialization=true",
        "logging.level.ru.sber.transport.notifications=DEBUG"} )
@DisplayName("Проверка сервиса всех настройок уведомлений GroupTransferApproved")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
public class GroupTransferApprovedCommandTest extends GroupTransferCommon {

    @Autowired
    private GroupTransferApprovedCommand command;

    @DisplayName("Проверка отправки уведомления после наступления времени")
    @Test
    void validate() {

        assertThat(command.validate(
                TripRequest.builder()
                        .transportType(TransportTypeEnum.GROUP_TRANSFER)
                        .status(TripRequestStatus.GROUP_TRANSFER_AWAITING_APPROVAL.name())
                        .build(),
                TripRequest.builder()
                        .transportType(TransportTypeEnum.GROUP_TRANSFER)
                        .status(TripRequestStatus.GROUP_TRANSFER_APPROVED.name())
                        .build())).isFalse();


        assertThat(command.validate(
                TripRequest.builder()
                        .transportType(TransportTypeEnum.GROUP_TRANSFER)
                        .status(TripRequestStatus.GROUP_TRANSFER_AWAITING_APPROVAL.name())
                        .build(),
                TripRequest.builder()
                        .transportType(TransportTypeEnum.GROUP_TRANSFER)
                        .status(TripRequestStatus.GROUP_TRANSFER_APPROVED.name())
                        .approvalId(UUID.randomUUID())
                        .authorId(UUID.randomUUID())
                        .build())).isTrue();
    }

    @DisplayName("Проверка реализации методов")
    @Test
    void checkMethods() {
        assertThat(command.getExpectedStatus()).isEqualTo(TripRequestStatus.GROUP_TRANSFER_APPROVED.name());
        assertThat(command.getCommandName()).isEqualTo(command.getClass().getSimpleName());
        assertThat(command.getNotificationType()).isEqualTo(NotificationType.GROUP_TRANSFER_APPROVED);
        assertThat(command.getDescription()).isEqualTo("notice_4003");
    }
}
