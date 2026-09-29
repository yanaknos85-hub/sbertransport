package ru.sberbank.ditsib.transport.request.shared;

import ru.sberbank.ditsib.transport.constants.CarsharingJoinRequestStatus;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.messaging.message.DeadlineSettingsMessage;
import ru.sberbank.ditsib.transport.request.database.model.deadline.DeadlineSettings;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Экземпляры и методы для тестирования настроек КС
 */
public class DeadlineSettingsSharedData {
    
    /**
     * Вернуть тестовый экземпляр DeadlineSettingsMessage.RequestDeadlineSettingsItem
     *
     * @param transportType тип транспорта
     * @param requestStatus статус заявки
     *
     * @return DeadlineSettingsMessage.RequestDeadlineSettingsItem
     */
    public DeadlineSettingsMessage.RequestDeadlineSettingsItem createTestRequestDeadlineSettingsItem(
            TransportTypeEnum transportType, TripRequestStatus requestStatus
                                                                                                    ) {
        return DeadlineSettingsMessage.RequestDeadlineSettingsItem.builder()
                                                                  .id(UUID.randomUUID())
                                                                  .requestStatus(requestStatus.name())
                                                                  .transportType(transportType.name())
                                                                  .unit(ChronoUnit.DAYS)
                                                                  .value(1)
                                                                  .build();
    }
    
    
    /**
     * Вернуть тестовый экземпляр DeadlineSettingsMessage.CarsharingJoinDeadlineSettingsItem
     *
     * @param transportType тип транспорта
     * @param joinRequestStatus
     *
     * @return DeadlineSettingsMessage.CarsharingJoinDeadlineSettingsItem
     */
    public DeadlineSettingsMessage.CarsharingJoinDeadlineSettingsItem createTestCarsharingJoinDeadlineSettingsItem(
            TransportTypeEnum transportType, CarsharingJoinRequestStatus joinRequestStatus
                                                                                                                  ) {
        return DeadlineSettingsMessage.CarsharingJoinDeadlineSettingsItem.builder()
                                                                         .id(UUID.randomUUID())
                                                                         .carsharingJoinStatus(joinRequestStatus.name())
                                                                         .transportType(transportType.name())
                                                                         .unit(ChronoUnit.DAYS)
                                                                         .value(1)
                                                                         .build();
    }
    
    /**
     * Вернуть тестовый экземпляр сообщения о настройках КС
     *
     * @return тестовый экземпляр DeadlineSettingsMessage
     */
    public DeadlineSettingsMessage getTestDeadlineSettingsMessage(UUID organizationId) {
        DeadlineSettingsMessage message = new DeadlineSettingsMessage();
        message.setId(UUID.randomUUID());
        message.setOrganizationId(organizationId);
        message.setTaxiAwaitingApprovalDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.TAXI, TripRequestStatus.TAXI_AWAITING_APPROVAL));
        message.setTaxiAwaitingSearchDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.TAXI, TripRequestStatus.TAXI_DRIVER_SEARCH));
        message.setTaxiTripFinishedDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.TAXI, TripRequestStatus.TAXI_TRIP_FINISHED));
        message.setPersonalAwaitingApprovalDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.PERSONAL, TripRequestStatus.PERSONAL_AWAITING_APPROVAL));
        message.setPersonalAwaitingSharedRideApprovalDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.PERSONAL, TripRequestStatus.PERSONAL_AWAITING_SHARED_RIDE_APPROVAL));
        message.setPersonalTripInProgressDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.PERSONAL, TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS));
        message.setPersonalAwaitingTripApprovalDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.PERSONAL, TripRequestStatus.PERSONAL_AWAITING_TRIP_APPROVAL));
        message.setPersonalOrderPaymentFormationDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.PERSONAL, TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION));
        message.setPersonalPaymentAwaitingDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.PERSONAL, TripRequestStatus.PERSONAL_PAYMENT_AWAITING));
        message.setPublicAwaitingApprovalDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.PUBLIC, TripRequestStatus.PUBLIC_AWAITING_APPROVAL));
        message.setPublicTripConfirmationDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.PUBLIC, TripRequestStatus.PUBLIC_TRIP_CONFIRMATION));
        message.setPublicAwaitingAffirmativeDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.PUBLIC, TripRequestStatus.PUBLIC_AWAITING_AFFIRMATIVE));
        message.setPublicOrderPaymentFormationDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.PUBLIC, TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION));
        message.setPublicPaymentAwaitingDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.PUBLIC, TripRequestStatus.PUBLIC_PAYMENT_AWAITING));
        message.setCarsharingJoinDeadline(createTestCarsharingJoinDeadlineSettingsItem(
                TransportTypeEnum.CARSHARING, CarsharingJoinRequestStatus.UNDER_CONSIDERATION));
        
        return message;
    }
    
    /**
     * Задать все дедлайны у настроек
     *
     * @return тестовый экземпляр DeadlineSettingsMessage
     */
    public DeadlineSettings setAllDeadlinesIntoSettings(
            DeadlineSettings settings, ChronoUnit unit,
            Integer value
                                                       ) {
        settings.getAllItems().forEach(ds -> {
            ds.setUnit(unit);
            ds.setValue(value);
        });
        return settings;
    }
    
    /**
     * Выборочно проверить сообщение о настройках КС по сущности настроек
     *
     * @param message сообщение о настройках КС
     * @param settings сущность настроек КС
     */
    public void checkSettingsByMessage(DeadlineSettingsMessage message, DeadlineSettings settings) {
        assertThat(message).isNotNull();
        assertThat(message.getId()).isEqualTo(settings.getId());
        assertThat(message.getOrganizationId()).isEqualTo(settings.getOrganizationId());
        // выборочно проверим настройки КС
        assertThat(message.getTaxiAwaitingApprovalDeadline().getTransportType()).isEqualTo(
                settings.getTaxiAwaitingApprovalDeadline().getTransportType().name());
        assertThat(message.getTaxiAwaitingApprovalDeadline().getRequestStatus()).isEqualTo(
                settings.getTaxiAwaitingApprovalDeadline().getRequestStatus().name());
        assertThat(message.getPersonalTripInProgressDeadline().getTransportType()).isEqualTo(
                settings.getPersonalTripInProgressDeadline().getTransportType().name());
        assertThat(message.getPersonalTripInProgressDeadline().getRequestStatus()).isEqualTo(
                settings.getPersonalTripInProgressDeadline().getRequestStatus().name());
        assertThat(message.getPublicTripConfirmationDeadline().getTransportType()).isEqualTo(
                settings.getPublicTripConfirmationDeadline().getTransportType().name());
        assertThat(message.getPublicTripConfirmationDeadline().getRequestStatus()).isEqualTo(
                settings.getPublicTripConfirmationDeadline().getRequestStatus().name());
        assertThat(message.getCarsharingJoinDeadline().getTransportType()).isEqualTo(
                settings.getCarsharingJoinDeadline().getTransportType().name());
        assertThat(message.getCarsharingJoinDeadline().getCarsharingJoinStatus()).isEqualTo(
                settings.getCarsharingJoinDeadline().getCarsharingJoinStatus().name());
        assertThat(message.getCarsharingJoinDeadline().getUnit()).isEqualTo(
                settings.getCarsharingJoinDeadline().getUnit());
        assertThat(message.getCarsharingJoinDeadline().getValue()).isEqualTo(
                settings.getCarsharingJoinDeadline().getValue());
        assertThat(message.getCarsharingJoinDeadline().getId()).isEqualTo(
                settings.getCarsharingJoinDeadline().getId());
        assertThat(message.isDeleted()).isFalse();
    }
    
    /**
     * Получить сообщение об удалении настроек КС с заданным ID
     *
     * @param settingsId ID настроек КС
     */
    public DeadlineSettingsMessage getDeletedMessage(UUID settingsId) {
        return DeadlineSettingsMessage.builder().id(settingsId).deleted(true).build();
    }
}
