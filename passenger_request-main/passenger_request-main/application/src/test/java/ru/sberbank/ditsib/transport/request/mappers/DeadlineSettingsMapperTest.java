package ru.sberbank.ditsib.transport.request.mappers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mapstruct.factory.Mappers;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.constants.CarsharingJoinRequestStatus;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.messaging.message.DeadlineSettingsMessage;
import ru.sberbank.ditsib.transport.request.shared.DeadlineSettingsSharedData;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
class DeadlineSettingsMapperTest {
    
    DeadlineSettingsMapper mapper = Mappers.getMapper(DeadlineSettingsMapper.class);
    DeadlineSettingsSharedData sharedData = new DeadlineSettingsSharedData();
    
    @Test
    void messageToRequestItem() {
        var messageItem = DeadlineSettingsMessage.RequestDeadlineSettingsItem
                .builder()
                .id(UUID.randomUUID())
                .unit(ChronoUnit.HOURS)
                .value(5)
                .transportType(TransportTypeEnum.TAXI.name())
                .requestStatus(TripRequestStatus.TAXI_APPROVED.name())
                .build();
        var item = mapper.messageToRequestItem(messageItem);
    
        assertThat(item).isNotNull();
        assertThat(item.getRequestStatus().name()).isEqualTo(messageItem.getRequestStatus());
        assertThat(item.getTransportType().name()).isEqualTo(messageItem.getTransportType());
        assertThat(item.getId()).isEqualTo(messageItem.getId());
        assertThat(item.getUnit()).isEqualTo(messageItem.getUnit());
        assertThat(item.getValue()).isEqualTo(messageItem.getValue());
    }
    
    @Test
    void messageToCarsharingJoinItem() {
        var messageItem = DeadlineSettingsMessage.CarsharingJoinDeadlineSettingsItem
                .builder()
                .id(UUID.randomUUID())
                .unit(ChronoUnit.HOURS)
                .value(5)
                .transportType(TransportTypeEnum.CARSHARING.name())
                .carsharingJoinStatus(CarsharingJoinRequestStatus.UNDER_CONSIDERATION.name())
                .build();
        var item = mapper.messageToCarsharingJoinItem(messageItem);
    
        assertThat(item).isNotNull();
        assertThat(item.getCarsharingJoinStatus().name()).isEqualTo(messageItem.getCarsharingJoinStatus());
        assertThat(item.getTransportType().name()).isEqualTo(messageItem.getTransportType());
        assertThat(item.getId()).isEqualTo(messageItem.getId());
        assertThat(item.getUnit()).isEqualTo(messageItem.getUnit());
        assertThat(item.getValue()).isEqualTo(messageItem.getValue());
    }
    
    @Test
    void fromMessage() {
        var testMessage = sharedData.getTestDeadlineSettingsMessage(UUID.randomUUID());
        var settings = mapper.fromMessage(testMessage);
        sharedData.checkSettingsByMessage(testMessage, settings);
    }
}