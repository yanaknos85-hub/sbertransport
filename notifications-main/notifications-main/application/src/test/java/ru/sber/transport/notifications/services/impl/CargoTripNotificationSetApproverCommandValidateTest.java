package ru.sber.transport.notifications.services.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.services.impl.commands.cargo.CargoTripNotificationSetApproverCommand;
import ru.sber.transport.notifications.services.impl.commands.cargo.NotificationHelper;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CargoTripNotificationSetApproverCommandValidateTest {

    @InjectMocks
    private CargoTripNotificationSetApproverCommand command;

    private final UUID userId = UUID.randomUUID();

    @DisplayName("validate() должен вернуть false, если authorId == approvalId")
    @Test
    void validate_WhenAuthorIdEqualsApprovalId_ShouldReturnFalse() {
        // Arrange
        TripRequest previous = new TripRequest();
        TripRequest current = new TripRequest();
        current.setAuthorId(userId);
        current.setApprovalId(userId);

        // Act
        boolean result = command.validate(previous, current);

        // Assert
        assertThat(result).isFalse();
    }

    @DisplayName("validate() должен вернуть true, если authorId != approvalId")
    @Test
    void validate_WhenAuthorIdNotEqualsApprovalId_ShouldReturnTrue() {
        // Arrange
        TripRequest previous = new TripRequest();
        TripRequest current = new TripRequest();

        previous.setTransportType(TransportTypeEnum.DEDICATED);
        current.setTransportType(TransportTypeEnum.DEDICATED);

        previous.setStatus("TripRequest");
        current.setStatus(TripRequestStatus.CARGO_AWAITING_APPROVAL.name());

        current.setAuthorId(userId);
        current.setApprovalId(UUID.randomUUID()); // другой ID

        // Act
        boolean result = command.validate(previous, current);

        // Assert
        assertThat(result).isTrue();
    }

    @DisplayName("validate() должен вернуть false, если authorId !.isEquals approvalId")
    @Test
    void validate_WhenAuthorIdEqualsApprovalId_ShouldReturnTrue() {
        // Arrange
        TripRequest previous = new TripRequest();
        TripRequest current = new TripRequest();

        previous.setTransportType(TransportTypeEnum.DEDICATED);
        current.setTransportType(TransportTypeEnum.DEDICATED);

        previous.setStatus("TripRequest");
        current.setStatus(TripRequestStatus.CARGO_AWAITING_APPROVAL.name());

        current.setAuthorId(userId);
        current.setApprovalId(UUID.fromString(userId.toString())); // другой ID

        // Act
        boolean result = command.validate(previous, current);

        // Assert
        assertThat(result).isFalse();
    }

    @DisplayName("commonValidation: должен вернуть false, если статусы разные и тип — CARGO_TRANSPORTATION")
    @Test
    void commonValidation_WhenStatusesDifferAndIsCargoTransportation_ShouldReturnFalse() {
        // Arrange
        TripRequest previous = new TripRequest();
        TripRequest current = new TripRequest();

        previous.setTransportType(TransportTypeEnum.DEDICATED);
        current.setTransportType(TransportTypeEnum.DEDICATED);

        previous.setStatus("TripRequest");
        current.setStatus("TripRequest2"); // статус изменился

        // Act
        boolean result = NotificationHelper.commonValidation(previous, current);

        // Assert
        assertThat(result).isFalse();
    }
}