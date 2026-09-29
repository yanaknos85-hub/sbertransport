package ru.sberbank.ditsib.transport.request.service.impl;

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
import ru.sber.transport.tariff.messaging.CarSharingTariffMessage;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.CarsharingTariffRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingTariff;
import ru.sberbank.ditsib.transport.request.mappers.TariffMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка работы с тарифами на каршеринг")
class CarsharingTariffServiceImplTest extends KafkaTest {
    
    @Autowired
    private CarsharingTariffRepository carsharingTariffRepository;
    
    @Autowired
    private TariffMapper tariffMapper;
    
    @Test
    void save() {
        var cut = new CarsharingTariffServiceImpl(carsharingTariffRepository, tariffMapper);
        
        UUID id = UUID.randomUUID();
        String humanReadableId = "humanReadableId";
        UUID departmentId = UUID.randomUUID();
        String serviceType = "EMPLOYEE_TRANSPORTATION";
        UUID organizationId = UUID.randomUUID();
        UUID regionId = UUID.randomUUID();
        String region = "region";
        String transportType = "CARSHARING";
        boolean active = true;
        UUID contractId = UUID.randomUUID();
        int rideCostPerKm = 10;
        int rideCostPerMin = 20;
        int waitCostPerMin = 30;
        double coefWorkDayMorning = 1.0d;
        double coefWorkDayNoon = 1.0d;
        double coefWorkDayEvening = 1.0d;
        double coefWorkDayNight = 1.0d;
        double coefDayOff = 1.0d;
        double coefTraffic = 1.0d;
        double coefChildSeat = 1.0d;
        double coefPetTransport = 1.0d;
        double coefCasko = 1.0d;
        boolean deleted = false;
        
        CarSharingTariffMessage message = new CarSharingTariffMessage(
                id,
                humanReadableId,
                departmentId,
                serviceType,
                organizationId,
                regionId,
                region,
                transportType,
                active,
                contractId,
                rideCostPerKm,
                rideCostPerMin,
                waitCostPerMin,
                coefWorkDayMorning,
                coefWorkDayNoon,
                coefWorkDayEvening,
                coefWorkDayNight,
                coefDayOff,
                coefTraffic,
                coefChildSeat,
                coefPetTransport,
                coefCasko,
                deleted
        );
        cut.save(message.id(), message);
        
        var result = carsharingTariffRepository.findById(message.getId()).get();
        
        assertEquals(message.getId(), result.getId());
    }
    
    @Test
    void saveTariff() {
        
        var cut = new CarsharingTariffServiceImpl(carsharingTariffRepository, tariffMapper);
        
        var tariffId = UUID.randomUUID();
        CarsharingTariff tariff = CarsharingTariff.builder().id(tariffId).build();
        var result = cut.save(tariff);
        
        assertTrue(carsharingTariffRepository.findById(tariffId).isPresent());
    }
    
    @Test
    void deleteById() {
        
        var cut = new CarsharingTariffServiceImpl(carsharingTariffRepository, tariffMapper);
        
        var tariffId = UUID.randomUUID();
        CarsharingTariff tariff = CarsharingTariff.builder().id(tariffId).build();
        carsharingTariffRepository.save(tariff);
        cut.deleteById(tariffId);
        
        assertFalse(carsharingTariffRepository.findById(tariffId).isPresent());
    }
}