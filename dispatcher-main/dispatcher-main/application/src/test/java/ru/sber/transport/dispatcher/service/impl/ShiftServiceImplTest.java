package ru.sber.transport.dispatcher.service.impl;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.dispatcher.database.dao.*;
import ru.sber.transport.dispatcher.database.model.*;
import ru.sber.transport.dispatcher.messages.ShiftFromMaisMessage;
import ru.sber.transport.dispatcher.messages.Source;
import ru.sber.transport.dispatcher.service.ShiftService;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.messaging.kafka.test.KafkaTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EmbeddedPostgres
@DisplayName("Проверка сервиса для работы со сменами")
@Transactional
class ShiftServiceImplTest extends KafkaTest {

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ShiftConflictRepository shiftConflictRepository;

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private AutoparkRepository autoparkRepository;

    @Autowired
    private ContractorRepository contractorRepository;

    @Test
    @DisplayName("Получение списка смен по ID водителя и дате")
    void findAllByDriverIdAndDate() {
        var autopark = createAutopark();
        var driver = createDriver(autopark);
        var vehicle = createVehicle(autopark);

        var startDate = LocalDateTime.now().plusHours(1);
        var endDate = LocalDateTime.now().plusHours(2);

        var shift = createShift(driver, vehicle, startDate, endDate);
        shiftRepository.save(shift);

        var result = shiftService.findAllByDriverIdAndDate(driver.getId(), startDate.minusHours(1), endDate.plusHours(1));

        assertEquals(1, result.size());
        assertEquals(shift.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Получение списка смен по ID транспортного средства и дате")
    void findAllByVehicleIdAndDate() {
        var autopark = createAutopark();
        var driver = createDriver(autopark);
        var vehicle = createVehicle(autopark);

        var startDate = LocalDateTime.now().plusHours(1);
        var endDate = LocalDateTime.now().plusHours(2);

        var shift = createShift(driver, vehicle, startDate, endDate);
        shiftRepository.save(shift);

        var result = shiftService.findAllByVehicleIdAndDate(vehicle.getId(), startDate.minusHours(1), endDate.plusHours(1));

        assertEquals(1, result.size());
        assertEquals(shift.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Сохранение смены")
    void save() {
        var autopark = createAutopark();
        var driver = createDriver(autopark);
        var vehicle = createVehicle(autopark);

        var startDate = LocalDateTime.now().plusHours(1);
        var endDate = LocalDateTime.now().plusHours(2);

        var shift = createShift(driver, vehicle, startDate, endDate);

        var savedShift = shiftService.save(shift, Source.CONTRACTOR);

        assertNotNull(savedShift.getId());
        assertEquals(shift.getDriver().getId(), savedShift.getDriver().getId());
        assertEquals(shift.getVehicle().getId(), savedShift.getVehicle().getId());
    }

    @Test
    @DisplayName("Получение смены по ID")
    void get() {
        var autopark = createAutopark();
        var driver = createDriver(autopark);
        var vehicle = createVehicle(autopark);

        var startDate = LocalDateTime.now().plusHours(1);
        var endDate = LocalDateTime.now().plusHours(2);

        var shift = createShift(driver, vehicle, startDate, endDate);
        shiftRepository.save(shift);

        var result = shiftService.get(shift.getId());

        assertTrue(result.isPresent());
        assertEquals(shift.getId(), result.get().getId());
    }

    @Test
    @DisplayName("Получение смены по null ID")
    void get_NullId() {
        var result = shiftService.get(null);

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Получение смен по водителю и текущей дате")
    void getShiftByDriverIdAndCurrentDate() {
        var autopark = createAutopark();
        var driver = createDriver(autopark);
        var vehicle = createVehicle(autopark);

        var startDate = LocalDateTime.now().minusHours(1);
        var endDate = LocalDateTime.now().plusHours(1);

        var shift = createShift(driver, vehicle, startDate, endDate);
        shiftRepository.save(shift);

        var result = shiftService.getShiftByDriverIdAndCurrentDate(driver, LocalDateTime.now());

        assertEquals(1, result.size());
        assertEquals(shift.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Получение списка смен по ID контрагента")
    void findAllByContractorId() {
        var autopark = createAutopark();

        var driver = createDriver(autoparkRepository.findAll().get(0));
        var vehicle = createVehicle(autoparkRepository.findAll().get(0));

        var startDate = LocalDateTime.now().plusHours(1);
        var endDate = LocalDateTime.now().plusHours(2);

        var shift = createShift(driver, vehicle, startDate, endDate);
        shiftRepository.save(shift);

        var result = shiftService.findAllByContractorId(autopark.getContractor().getId());

        assertEquals(1, result.size());
        assertEquals(shift.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Получение списка смен по дате начала и ID ряда")
    void getShiftIdsByStartDateAfterAndRowId() {
        var autopark = createAutopark();
        var driver = createDriver(autopark);
        var vehicle = createVehicle(autopark);

        var startDate = LocalDateTime.now().plusHours(1);
        var endDate = LocalDateTime.now().plusHours(2);
        var rowId = UUID.randomUUID();

        var shift = createShift(driver, vehicle, startDate, endDate);
        shift.setRowId(rowId);
        shiftRepository.save(shift);

        var result = shiftService.getShiftIdsByStartDateAfterAndRowId(startDate.toLocalDate(), rowId);

        assertEquals(1, result.size());
        assertEquals(shift.getId(), result.get(0).getId());
    }

    @Test
    @DisplayName("Обработка смены из МАИС: создание - успешно")
    void handleShiftFromMais_Create_Success() {
        var autopark = createAutopark();
        var driver = createDriver(autopark);
        var vehicle = createVehicle(autopark);
        driver.setPersonnelNumber("test-driver-001");
        vehicle.setStateNumber("A123BC777");
        driverRepository.save(driver);
        vehicleRepository.save(vehicle);

        var message = new ShiftFromMaisMessage(
                UUID.randomUUID(),
                "route-123",
                "test-driver-001",
                "A123BC777",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                ShiftFromMaisMessage.ActionType.CREATE
        );

        shiftService.handleShiftFromMais(message);

        var result = shiftRepository.findByRouteId("route-123");
        assertTrue(result.isPresent());
        assertEquals("route-123", result.get().getRouteId());
        assertEquals(driver.getId(), result.get().getDriver().getId());
        assertEquals(vehicle.getId(), result.get().getVehicle().getId());
    }

    @Test
    @DisplayName("Обработка смены из МАИС: создание - конфликт валидации")
    void handleShiftFromMais_Create_Conflict() {
        var autopark = createAutopark();
        var driver = createDriver(autopark);
        driver.setPersonnelNumber("test-driver-001");
        driver.setActive(false);
        driverRepository.save(driver);

        var message = new ShiftFromMaisMessage(
                UUID.randomUUID(),
                "route-123",
                "test-driver-001",
                "A123BC777",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                ShiftFromMaisMessage.ActionType.CREATE
        );

        shiftService.handleShiftFromMais(message);

        var conflict = shiftConflictRepository.findById("route-123");
        assertTrue(conflict.isPresent());
        assertEquals(ConflictReason.DRIVER_NOT_FOUND, conflict.get().getConflictReason());
    }

    @Test
    @DisplayName("Обработка смены из МАИС: обновление")
    void handleShiftFromMais_Update() {
        var autopark = createAutopark();
        var driver = createDriver(autopark);
        var vehicle = createVehicle(autopark);
        driver.setPersonnelNumber("test-driver-001");
        vehicle.setStateNumber("A123BC777");
        driverRepository.save(driver);
        vehicleRepository.save(vehicle);

        var existingShift = new Shift();
        existingShift.setRouteId("route-123");
        existingShift.setDriver(driver);
        existingShift.setVehicle(vehicle);
        existingShift.setStartDate(LocalDateTime.now().plusHours(1));
        existingShift.setEndDate(LocalDateTime.now().plusHours(2));
        shiftRepository.save(existingShift);

        var message = new ShiftFromMaisMessage(
                UUID.randomUUID(),
                "route-123",
                "test-driver-001",
                "A123BC777",
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(3),
                ShiftFromMaisMessage.ActionType.UPDATE
        );

        shiftService.handleShiftFromMais(message);

        var result = shiftRepository.findByRouteId("route-123");
        assertTrue(result.isPresent());
        assertEquals(message.startDate(), result.get().getStartDate());
        assertEquals(message.endDate(), result.get().getEndDate());
    }

    @Test
    @DisplayName("Обработка смены из МАИС: удаление - найдена смена")
    void handleShiftFromMais_Delete_Success() {
        var autopark = createAutopark();
        var driver = createDriver(autopark);
        var vehicle = createVehicle(autopark);

        var existingShift = new Shift();
        existingShift.setRouteId("route-123");
        existingShift.setDriver(driver);
        existingShift.setVehicle(vehicle);
        existingShift.setStartDate(LocalDateTime.now().plusHours(1));
        existingShift.setEndDate(LocalDateTime.now().plusHours(2));
        existingShift.setDeleted(false);
        existingShift.setActive(true);
        shiftRepository.save(existingShift);

        var message = new ShiftFromMaisMessage(
                UUID.randomUUID(),
                "route-123",
                "test-driver-001",
                "A123BC777",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                ShiftFromMaisMessage.ActionType.DELETE
        );

        shiftService.handleShiftFromMais(message);

        var result = shiftRepository.findByRouteId("route-123");
        assertTrue(result.isPresent());
        assertTrue(result.get().isDeleted());
        assertFalse(result.get().isActive());
    }

    @Test
    @DisplayName("Обработка смены из МАИС: удаление - смена не найдена")
    void handleShiftFromMais_Delete_NotFound() {
        var message = new ShiftFromMaisMessage(
                UUID.randomUUID(),
                "route-123",
                "test-driver-001",
                "A123BC777",
                LocalDateTime.now().plusHours(1),
                LocalDateTime.now().plusHours(2),
                ShiftFromMaisMessage.ActionType.DELETE
        );

        shiftService.handleShiftFromMais(message);

        var result = shiftRepository.findByRouteId("route-123");
        assertTrue(result.isEmpty());
    }

    private Autopark createAutopark() {
        var contractor = Instancio.of(Contractor.class)
                .ignore(Select.field(Contractor::getId))
                .ignore(Select.field(Contractor::getDigitId))
                .ignore(Select.field(Contractor::getMainDispatcher))
                .create();
        contractorRepository.save(contractor);

        var autopark = Instancio.of(Autopark.class)
                .ignore(Select.field(Autopark::getId))
                .set(Select.field(Autopark::getContractor), contractor)
                .create();
        return autoparkRepository.save(autopark);
    }

    private Driver createDriver(Autopark autopark) {
        var driver = Instancio.of(Driver.class)
                .ignore(Select.field(Driver::getId))
                .set(Select.field(Driver::getContractor), autopark.getContractor())
                .set(Select.field(Driver::getAutopark), autopark)
                .set(Select.field(Driver::isActive), true)
                .ignore(Select.field(Driver::getAttributes))
                .create();
        return driverRepository.save(driver);
    }

    private Vehicle createVehicle(Autopark autopark) {
        var vehicle = Instancio.of(Vehicle.class)
                .ignore(Select.field(Vehicle::getId))
                .set(Select.field(Vehicle::getAutopark), autopark)
                .set(Select.field(Vehicle::isActive), true)
                .set(Select.field(Vehicle::isInExploitation), true)
                .set(Select.field(Vehicle::getManufactureYear), 2020)
                .ignore(Select.field(Vehicle::getVehicleAdditional))
                .create();
        return vehicleRepository.save(vehicle);
    }

    private Shift createShift(Driver driver, Vehicle vehicle, LocalDateTime startDate, LocalDateTime endDate) {
        return Instancio.of(Shift.class)
                .ignore(Select.field(Shift::getId))
                .set(Select.field(Shift::getContractorId), driver.getContractor().getId())
                .set(Select.field(Shift::getDriver), driver)
                .set(Select.field(Shift::getVehicle), vehicle)
                .set(Select.field(Shift::getStartDate), startDate)
                .set(Select.field(Shift::getEndDate), endDate)
                .set(Select.field(Shift::isDeleted), false)
                .set(Select.field(Shift::isActive), true)
                .create();
    }
}
