package ru.sberbank.ditsib.transport.request.messaging.listeners.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.DriverRepository;
import ru.sberbank.ditsib.transport.request.database.dao.VehicleRepository;
import ru.sberbank.ditsib.transport.request.database.model.driversData.Driver;
import ru.sberbank.ditsib.transport.request.database.model.driversData.Vehicle;

import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@SpringBootTest(classes = RequestApplication.class)
@EmbeddedPostgres
@Transactional
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка обновления данных поездки по сообщению из Kafka")
class InContractorTaxiTripInProgressListenerImplTest extends KafkaTest {
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Autowired
    private VehicleRepository vehicleRepository;
    
    @Test
    @DisplayName("Проверка поиска водителя по ФИО и номеру телефона")
    void test_findDriverByFIOAndContactPhone() {
        var driverWrong1 =
                Driver.builder()
                      .id(UUID.randomUUID()).firstName("firstName").lastName("lastName").patronymic("patronymic").contactPhone("89251234567")
                      .active(true)
                      .build();
        driverRepository.save(driverWrong1);
        
        var driverWrong2 =
                Driver.builder()
                      .id(UUID.randomUUID()).firstName("wrongName").lastName("wrongName").patronymic("wrongPatronymic").contactPhone("89251111111")
                      .active(true)
                      .build();
        driverRepository.save(driverWrong2);
        
        var driverWrong3 =
                Driver.builder()
                      .id(UUID.randomUUID()).firstName("firstName").lastName("lastName").patronymic("patronymic").contactPhone("89252222222")
                      .active(false)
                      .build();
        driverRepository.save(driverWrong3);
        
        var driverRight =
                Driver.builder()
                      .id(UUID.randomUUID()).firstName("firstName").lastName("lastName").patronymic("patronymic").contactPhone("89252222222")
                      .active(true)
                      .build();
        driverRight = driverRepository.save(driverRight);
        
        var drivers = driverRepository.findByLastNameAndFirstNameAndPatronymicAndContactPhoneAndActive("lastName", "firstName", "patronymic",
                                                                                                       "89252222222", true);
        
        assertFalse(drivers.isEmpty());
        assertEquals(drivers.get(0).getId(), driverRight.getId());
    }
    
    
    @Test
    @DisplayName("Проверка поиска автомобиля по бренду/модели/гос.номеру/цвету")
    void test_findVehicleByBrandModelNumberColor() {
        var vehicleWrong1 = Vehicle.builder()
                                   .id(UUID.randomUUID()).brand("brand").model("model").stateNumber("stateNumber").color("color").active(false)
                                   .build();
        vehicleRepository.save(vehicleWrong1);
        
        var vehicleWrong2 = Vehicle.builder()
                                   .id(UUID.randomUUID()).brand("wrongBrand").model("wrongModel").stateNumber("wrongStateNumber").color("wrongColor").active(true)
                                   .build();
        vehicleRepository.save(vehicleWrong2);
        
        var vehicleRight = Vehicle.builder()
                                  .id(UUID.randomUUID()).brand("brand").model("model").stateNumber("stateNumber").color("color").active(true)
                                  .build();
        vehicleRight = vehicleRepository.save(vehicleRight);
        
        var vehicles = vehicleRepository.findFirstByBrandAndModelAndStateNumberAndColorAndActive("brand", "model", "stateNumber", "color", true);
        
        assertFalse(vehicles.isEmpty());
        assertEquals(vehicles.get(0).getId(), vehicleRight.getId());
    }
}