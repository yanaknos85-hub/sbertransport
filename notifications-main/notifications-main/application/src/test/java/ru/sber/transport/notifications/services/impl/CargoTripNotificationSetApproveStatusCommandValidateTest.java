package ru.sber.transport.notifications.services.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.notifications.services.impl.commands.cargo.CargoTripNotificationSetApproveStatusCommand;
import ru.sberbank.ditsib.transport.constants.ApprovalState;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CargoTripNotificationSetApproveStatusCommandValidateTest {

    @InjectMocks
    private CargoTripNotificationSetApproveStatusCommand command;

    private final UUID userId = UUID.randomUUID();
    private final UUID anotherUserId = UUID.randomUUID();

    @DisplayName("validate() должен вернуть false, если authorId == approvalId")
    @Test
    void validate_WhenAuthorIdEqualsApprovalId_ShouldReturnFalse() {
        // Arrange
        TripRequest previous = new TripRequest();
        TripRequest current = new TripRequest();
        current.setAuthorId(userId);
        current.setApprovalId(userId); // тот же пользователь

        // Act
        boolean result = command.validate(previous, current);

        // Assert
        assertThat(result).isFalse();
    }

    @DisplayName("validate() должен вернуть true, если authorId != approvalId и статус изменился на APPROVED")
    @Test
    void validate_WhenAuthorIdNotEqualsApprovalIdAndApproved_ShouldReturnTrue() {
        // Arrange
        TripRequest previous = new TripRequest();
        TripRequest current = new TripRequest();

        previous.setTransportType(TransportTypeEnum.DEDICATED);
        current.setTransportType(TransportTypeEnum.DEDICATED);

        current.setAuthorId(userId);
        current.setApprovalId(anotherUserId);
        current.setStatusCode(200);
        current.setStatus(TripRequestStatus.CARGO_APPROVED.name());
        current.setApprovalState(ApprovalState.APPROVED.name());

        // Имитируем изменение статуса
        previous.setStatusCode(100);
        previous.setStatus(TripRequestStatus.CARGO_AWAITING_TRANSFER.name());

        // Act
        boolean result = command.validate(previous, current);

        // Assert
        assertThat(result).isTrue();
    }

    @DisplayName("validate() должен вернуть true, если authorId != approvalId и статус изменился на APPROVED")
    @Test
    void validate_WhenAuthorIdEqualsApprovalIdAndApproved_ShouldReturnTrue() {
        // Arrange
        TripRequest previous = new TripRequest();
        TripRequest current = new TripRequest();

        previous.setTransportType(TransportTypeEnum.DEDICATED);
        current.setTransportType(TransportTypeEnum.DEDICATED);

        current.setAuthorId(userId);
        current.setApprovalId(UUID.fromString(userId.toString()));
        current.setStatusCode(200);
        current.setStatus(TripRequestStatus.CARGO_APPROVED.name());
        current.setApprovalState(ApprovalState.APPROVED.name());

        // Имитируем изменение статуса
        previous.setStatusCode(100);
        previous.setStatus(TripRequestStatus.CARGO_AWAITING_TRANSFER.name());

        // Act
        boolean result = command.validate(previous, current);

        // Assert
        assertThat(result).isFalse();
    }
}