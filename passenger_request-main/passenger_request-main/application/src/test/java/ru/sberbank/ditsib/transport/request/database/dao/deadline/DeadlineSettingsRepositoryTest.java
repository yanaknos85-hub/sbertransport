package ru.sberbank.ditsib.transport.request.database.dao.deadline;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.CarsharingJoinRequestStatus;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.RequestForTaxiRepository;
import ru.sberbank.ditsib.transport.request.database.model.deadline.CarsharingJoinDeadlineSettingsItem;
import ru.sberbank.ditsib.transport.request.database.model.deadline.DeadlineSettings;
import ru.sberbank.ditsib.transport.request.database.model.deadline.RequestDeadlineSettingsItem;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

// todo удалить тест, когда заработает тест на контроллер
@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@MockitoBean(types = JwtDecoder.class)
class DeadlineSettingsRepositoryTest extends KafkaTest {
    
    @Autowired
    private RequestForTaxiRepository taxiRepository;
    
    @Autowired
    private DeadlineSettingsRepository repository;
    
    @Test
    void create() {
        DeadlineSettings deadline = getTestDeadlineSettings();
        repository.save(deadline);
        assertEquals(repository.count(), 1);
    }
    
    @Test
    void checkTimedSql() {
        taxiRepository.findNewReadyToSend(LocalDate.now().minusDays(1).atStartOfDay());
    }
    
    /**
     * Вернуть тестовый экземпляр RequestDeadlineSettingsItem
     *
     * @param transportType тип транспорта
     * @param requestStatus статус заявки
     *
     * @return RequestDeadlineSettingsItem
     */
    public RequestDeadlineSettingsItem createTestRequestDeadlineSettingsItem(
            TransportTypeEnum transportType, TripRequestStatus requestStatus
                                                                            ) {
        return RequestDeadlineSettingsItem.builder()
                                          .id(UUID.randomUUID())
                                          .requestStatus(requestStatus)
                                          .transportType(transportType)
                                          .unit(ChronoUnit.DAYS)
                                          .value(1)
                                          .build();
    }
    
    private CarsharingJoinDeadlineSettingsItem createTestCarsharingJoinDeadlineSettingsItem() {
        return CarsharingJoinDeadlineSettingsItem.builder()
                                                 .id(UUID.randomUUID())
                                                 .carsharingJoinStatus(CarsharingJoinRequestStatus.UNDER_CONSIDERATION)
                                                 .transportType(TransportTypeEnum.CARSHARING)
                                                 .unit(ChronoUnit.DAYS)
                                                 .value(1)
                                                 .build();
    }
    
    /**
     * Вернуть тестовый экземпляр настроек КС
     * @return тестовый экземпляр DeadlineSettings
     */
    private DeadlineSettings getTestDeadlineSettings() {
        
        DeadlineSettings settings = new DeadlineSettings();
        settings.setId(UUID.randomUUID());
        settings.setOrganizationId(UUID.randomUUID());
        settings.setTaxiAwaitingApprovalDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.TAXI, TripRequestStatus.TAXI_AWAITING_APPROVAL));
        settings.setTaxiAwaitingSearchDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.TAXI, TripRequestStatus.TAXI_DRIVER_SEARCH));
        settings.setTaxiTripFinishedDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.TAXI, TripRequestStatus.TAXI_TRIP_FINISHED));
        settings.setPersonalAwaitingApprovalDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.PERSONAL, TripRequestStatus.PERSONAL_AWAITING_APPROVAL));
        settings.setPersonalAwaitingSharedRideApprovalDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.PERSONAL, TripRequestStatus.PERSONAL_AWAITING_SHARED_RIDE_APPROVAL));
        settings.setPersonalTripInProgressDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.PERSONAL, TripRequestStatus.PERSONAL_TRIP_IN_PROGRESS));
        settings.setPersonalAwaitingTripApprovalDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.PERSONAL, TripRequestStatus.PERSONAL_AWAITING_TRIP_APPROVAL));
        settings.setPublicAwaitingApprovalDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.PUBLIC, TripRequestStatus.PUBLIC_AWAITING_APPROVAL));
        settings.setPublicTripConfirmationDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.PUBLIC, TripRequestStatus.PUBLIC_TRIP_CONFIRMATION));
        settings.setPublicAwaitingAffirmativeDeadline(createTestRequestDeadlineSettingsItem(
                TransportTypeEnum.PUBLIC, TripRequestStatus.PUBLIC_AWAITING_AFFIRMATIVE));
        settings.setCarsharingJoinDeadline(createTestCarsharingJoinDeadlineSettingsItem(
                                                                                       ));
        
        return settings;
    }
}