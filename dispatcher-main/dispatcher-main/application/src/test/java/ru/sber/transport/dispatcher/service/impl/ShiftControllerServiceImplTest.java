package ru.sber.transport.dispatcher.service.impl;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.dispatcher.database.dao.*;
import ru.sber.transport.dispatcher.database.model.Autopark;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.database.model.Driver;
import ru.sber.transport.dispatcher.database.model.Shift;
import ru.sber.transport.dispatcher.database.model.Vehicle;
import ru.sber.transport.dispatcher.messages.EwbMessage;
import ru.sber.transport.dispatcher.messaging.senders.ShiftSender;
import ru.sber.transport.dispatcher.service.ShiftControllerService;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка сервиса для работы со сменами")
@Transactional
public class ShiftControllerServiceImplTest extends KafkaTest {

    @Autowired
    private ShiftControllerService shiftControllerService;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Autowired
    private AutoparkRepository autoparkRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private DriverRepository driverRepository;

    @MockitoBean
    private ShiftSender shiftSender;

    @BeforeEach
    void setupContractor() {
        contractorRepository.saveAndFlush(Instancio.of(Contractor.class)
                .ignore(Select.field(Contractor::getMainDispatcher))
                .ignore(Select.field(Contractor::getId))
                .create()
        );
    }

    @Test
    @DisplayName("Отправка данных по эпл по сокету (успех создания эпл)")
    void sendEwbBySocket() {
        var contractor = contractorRepository.findAll().get(0);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::isActive), true)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .set(Select.field(Driver::getAutopark), null)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var startDate = LocalDateTime.now();
        var endDate = LocalDateTime.now().plusHours(2);
        var shift = shiftRepository.save(Instancio.of(Shift.class)
                .ignore(Select.field(Shift::getId))
                .set(Select.field(Shift::getVehicle), vehicle)
                .set(Select.field(Shift::getDriver), driver)
                .set(Select.field(Shift::getContractorId), contractor.getId())
                .set(Select.field(Shift::isDeleted), false)
                .set(Select.field(Shift::isActive), true)
                .set(Select.field(Shift::getStartDate), startDate)
                .set(Select.field(Shift::getEndDate), endDate)
                .set(Select.field(Shift::getRouteId), "ROUTE-001")
                .set(Select.field(Shift::getEwbId), UUID.randomUUID())
                .create());

        shiftControllerService.sendSocketForEwb(new EwbMessage(shift.getId(), ""));
        var existingShift = shiftRepository.findById(shift.getId());
        Assertions.assertTrue(existingShift.isPresent());
        Assertions.assertEquals(existingShift.get().getEwbId(), shift.getEwbId());
    }

    @Test
    @DisplayName("Отправка данных по эпл по сокету (ошибка создания эпл)")
    void sendEwbBySocketFail() {
        var contractor = contractorRepository.findAll().get(0);

        var autopark = autoparkRepository.save(Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .set(Select.field(Autopark::isActive), true)
                .ignore(Select.field(Autopark::getVehicles))
                .create()
        );

        var vehicle = vehicleRepository.save(Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::getManufactureYear), 2000)
                .set(Select.field(Vehicle::getVehicleAdditional), null)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .create());

        var driver = driverRepository.save(Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), contractor)
                .set(Select.field(Driver::isActive), true)
                .set(Select.field(Driver::getAutopark), null)
                .ignore(Select.field(Driver::getAttributes))
                .create());

        var startDate = LocalDateTime.now();
        var endDate = LocalDateTime.now().plusHours(2);
        var shift = shiftRepository.save(Instancio.of(Shift.class)
                .ignore(Select.field(Shift::getId))
                .set(Select.field(Shift::getVehicle), vehicle)
                .set(Select.field(Shift::getDriver), driver)
                .set(Select.field(Shift::getContractorId), contractor.getId())
                .set(Select.field(Shift::isDeleted), false)
                .set(Select.field(Shift::isActive), true)
                .set(Select.field(Shift::getStartDate), startDate)
                .set(Select.field(Shift::getEndDate), endDate)
                .set(Select.field(Shift::getRouteId), "ROUTE-001")
                .set(Select.field(Shift::getEwbId), UUID.randomUUID())
                .create());

        shiftControllerService.sendSocketForEwb(new EwbMessage(shift.getId(), "Ошибка"));
        var existingShift = shiftRepository.findById(shift.getId());
        Assertions.assertTrue(existingShift.isPresent());
        Assertions.assertNull(existingShift.get().getEwbId());
    }

}
