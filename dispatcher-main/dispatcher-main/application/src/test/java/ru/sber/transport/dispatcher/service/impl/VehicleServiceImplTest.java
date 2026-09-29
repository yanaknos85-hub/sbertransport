package ru.sber.transport.dispatcher.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.dispatcher.database.dao.AutoparkRepository;
import ru.sber.transport.dispatcher.database.dao.VehicleRepository;
import ru.sber.transport.dispatcher.database.model.Autopark;
import ru.sber.transport.dispatcher.database.model.Vehicle;
import ru.sber.transport.dispatcher.messages.TransportMessage;
import ru.sber.transport.dispatcher.service.VehicleService;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка сервиса для работы с транспортными средствами")
@Transactional
class VehicleServiceImplTest extends KafkaTest {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private VehicleService vehicleService;

    @Autowired
    private AutoparkRepository autoparkRepository;

    @AfterEach
    void clear() {
        vehicleRepository.deleteAll();
        autoparkRepository.deleteAll();
    }

    @Test
    @DisplayName("Обновление транспортного средства")
    void updateVehicle() {
        var autopark = Instancio.of(Autopark.class)
                .ignore(field(Autopark::getId))
                .ignore(field(Autopark::getVehicles))
                .ignore(field(Autopark::getContractor))
                .create();
        autoparkRepository.save(autopark);

        var vehicle = Instancio.of(Vehicle.class)
                .ignore(field(Vehicle::getId))
                .ignore(field(Vehicle::getEcoClass))
                .ignore(field(Vehicle::getVehicleType))
                .ignore(field(Vehicle::getVehicleAdditional))
                .set(field(Vehicle::getAutopark), autopark)
                .set(field(Vehicle::isActive), true)
                .set(field(Vehicle::isInExploitation), true)
                .set(field(Vehicle::getManufactureYear), 2000)
                .create();
        vehicleRepository.save(vehicle);

        var message = Instancio.of(TransportMessage.class)
                .set(field(TransportMessage::subtype), "Легковой")
                .set(field(TransportMessage::status), "NOT_IN_USE")
                .set(field(TransportMessage::autoparkId), autopark.getId())
                .set(field(TransportMessage::id), vehicle.getTransportId())
                .create();
        vehicleService.upsert(message);

        var result = vehicleRepository.findAll().get(0);
        assertEquals(1, vehicleRepository.findAll().size());
        assertEquals(message.getId(), result.getTransportId());
        assertFalse(result.isActive());
        assertFalse(result.isInExploitation());
    }

    @Test
    @DisplayName("Создание нового транспортного средства")
    void createVehicle() {
        var autopark = Instancio.of(Autopark.class)
                .ignore(field(Autopark::getId))
                .ignore(field(Autopark::getVehicles))
                .ignore(field(Autopark::getContractor))
                .create();
        autoparkRepository.save(autopark);

        var message = Instancio.of(TransportMessage.class)
                .set(field(TransportMessage::subtype), "Легковой")
                .set(field(TransportMessage::status), "NOT_IN_USE")
                .set(field(TransportMessage::autoparkId), autopark.getId())
                .create();
        vehicleService.upsert(message);

        var result = vehicleRepository.findAll().get(0);
        assertEquals(1, vehicleRepository.findAll().size());
        assertEquals(message.getId(), result.getTransportId());
        assertFalse(result.isActive());
        assertFalse(result.isInExploitation());
    }

    @Test
    @DisplayName("Несохранение тс при отсутствии указанного автопарка в базе данных")
    void upsert_autopark_not_found() {
        var message = Instancio.of(TransportMessage.class)
                .set(field(TransportMessage::subtype), "Легковой")
                .create();
        vehicleService.upsert(message);
        var result = vehicleRepository.findAll();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Несохранение тс при отсутствии автопарка")
    void upsert_autopark_is_null() {
        var message = Instancio.of(TransportMessage.class)
                .set(field(TransportMessage::subtype), "Легковой")
                .set(field(TransportMessage::autoparkId), null)
                .create();
        vehicleService.upsert(message);
        var result = vehicleRepository.findAll();
        assertTrue(result.isEmpty());
    }

}
