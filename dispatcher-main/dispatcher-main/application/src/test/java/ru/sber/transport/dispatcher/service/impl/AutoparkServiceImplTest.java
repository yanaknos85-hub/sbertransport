package ru.sber.transport.dispatcher.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.dispatcher.database.dao.AutoparkRepository;
import ru.sber.transport.dispatcher.database.dao.ShiftRepository;
import ru.sber.transport.dispatcher.database.dao.VehicleRepository;
import ru.sber.transport.dispatcher.database.model.Autopark;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.dto.NewAutoparkDTO;
import ru.sber.transport.dispatcher.exceptions.ConflictException;
import ru.sber.transport.dispatcher.messaging.senders.AutoparkSender;
import ru.sber.transport.dispatcher.service.ContractorService;
import ru.sber.transport.dispatcher.service.VerificationService;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.util.List;
import java.util.Optional;

import static java.util.UUID.randomUUID;
import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@ExtendWith(MockitoExtension.class)
@Feature("app_platform_dispatcher")
public class AutoparkServiceImplTest {

    @Mock
    private ContractorService contractorService;
    @Mock
    private AutoparkRepository autoparkRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private ShiftRepository shiftRepository;
    @Mock
    private VerificationService verificationService;
    @Mock
    private AutoparkSender autoparkSender;

    @InjectMocks
    private AutoparkServiceImpl autoparkService;

    @Test
    @DisplayName("Добавление автопарка передав норму автомобилей меньше допустимой")
    void shouldSave_whenCreating_ifVehicleCountNormInDtoLtLimit() {
        var dto = Instancio.of(NewAutoparkDTO.class)
                .set(field(NewAutoparkDTO::getVehicleCountNorm), 1)
                .create();
        var existingAutopark = Instancio.of(Autopark.class)
                .set(field(Autopark::getVehicleCountNorm), 10)
                .create();
        var savedAutopark = Instancio.of(Autopark.class).create();
        var existingContractor = Instancio.of(Contractor.class)
                .set(field(Contractor::getVehicleCountNorm), 100)
                .create();

        when(contractorService.get(existingContractor.getId())).thenReturn(Optional.of(existingContractor));
        when(autoparkRepository.findAllByContractorIdAndActiveIsTrue(existingContractor.getId()))
                .thenReturn(List.of(existingAutopark));
        when(autoparkRepository.save(any())).thenReturn(savedAutopark);

        var result = autoparkService.add(existingContractor.getId(), dto);
        verify(autoparkRepository, times(1)).save(any());
        verify(autoparkSender, times(1)).send(any());
        assertEquals(savedAutopark.getId(), result.id());
        assertEquals(savedAutopark.getName(), result.name());
        assertNull(result.contractor());
        assertEquals(savedAutopark.isActive(), result.active());
        assertEquals(savedAutopark.getRoutingId(), result.routingId());
    }

    @Test
    @DisplayName("Добавление автопарка передав норму автомобилей больше допустимой")
    void shouldThrowException_whenCreating_ifVehicleCountNormInDtoGtLimit() {
        var dto = Instancio.of(NewAutoparkDTO.class)
                .set(field(NewAutoparkDTO::getVehicleCountNorm), 50)
                .create();
        var existingAutopark = Instancio.of(Autopark.class)
                .set(field(Autopark::getVehicleCountNorm), 10)
                .create();
        var existingContractor = Instancio.of(Contractor.class)
                .set(field(Contractor::getVehicleCountNorm), 1)
                .create();

        when(contractorService.get(existingContractor.getId())).thenReturn(Optional.of(existingContractor));
        when(autoparkRepository.findAllByContractorIdAndActiveIsTrue(existingContractor.getId()))
                .thenReturn(List.of(existingAutopark));

        var exception = assertThrows(ConflictException.class, () -> autoparkService.add(existingContractor.getId(), dto));
        assertEquals("Нормативное количество автомобилей превышает допустимое значение", exception.getMessage());
    }

    @Test
    @DisplayName("Добавление автопарка с отсутствием нормы")
    void shouldNotSave_whenCreating_ifVehicleCountNormIsNull() {
        var dto = Instancio.of(NewAutoparkDTO.class)
                .ignore(field(NewAutoparkDTO::getVehicleCountNorm))
                .create();
        var savedAutopark = Instancio.of(Autopark.class).create();
        var existingContractor = Instancio.of(Contractor.class)
                .set(field(Contractor::getVehicleCountNorm), 100)
                .create();

        when(contractorService.get(existingContractor.getId())).thenReturn(Optional.of(existingContractor));
        when(autoparkRepository.save(any())).thenReturn(savedAutopark);

        var result = autoparkService.add(existingContractor.getId(), dto);
        verify(autoparkRepository, times(0)).findAllByContractorIdAndActiveIsTrue(any());
        verify(autoparkRepository, times(1)).save(any());
        verify(autoparkSender, times(1)).send(any());
        assertEquals(savedAutopark.getId(), result.id());
        assertEquals(savedAutopark.getName(), result.name());
        assertNull(result.contractor());
        assertEquals(savedAutopark.isActive(), result.active());
        assertEquals(savedAutopark.getRoutingId(), result.routingId());
    }

    @Test
    @DisplayName("Создание дубля автопарка")
    void shouldThrowException_whenCreating_ifAutoparkAlreadyExists() {
        var dto = Instancio.of(NewAutoparkDTO.class).create();
        var contractorId = randomUUID();
        var existingAutopark = Instancio.of(Autopark.class)
                .set(field(Autopark::getVehicleCountNorm), 10)
                .create();

        when(autoparkRepository.findByContractorIdAndNameAndActiveIsTrue(contractorId, dto.getName()))
                .thenReturn(Optional.ofNullable(existingAutopark));

        var exception = assertThrows(DuplicateDataException.class, () -> autoparkService.add(contractorId, dto));
        assertTrue(exception.getMessage().contains("Conflict data on entity Autopark"));
    }

    @Test
    @DisplayName("Добавление автопарка для contractor с vehicleCountNorm == null")
    void shouldThrowException_whenCreating_ifContractorVehicleCountNumIsNull() {
        var dto = Instancio.of(NewAutoparkDTO.class).create();
        var existingContractor = Instancio.of(Contractor.class)
                .set(field(Contractor::getVehicleCountNorm), null)
                .create();

        when(contractorService.get(existingContractor.getId())).thenReturn(Optional.of(existingContractor));

        var exception = assertThrows(ConflictException.class, () -> autoparkService.add(existingContractor.getId(), dto));
        assertEquals("Необходимо задать нормативное количество автомобилей для внутреннего автопарка", exception.getMessage());
    }

}
