package ru.sberbank.ditsib.transport.vehicle.service;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import ru.sberbank.ditsib.transport.vehicle.database.dao.AttorneyRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Attorney;
import ru.sberbank.ditsib.transport.vehicle.database.model.Employee;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyCreateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyUpdateDto;
import ru.sberbank.ditsib.transport.vehicle.exception.AttorneyNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.exception.AttorneyWrongDatesException;
import ru.sberbank.ditsib.transport.vehicle.exception.NonUniqueAttorneyException;
import ru.sberbank.ditsib.transport.vehicle.mapper.AttorneyMapper;
import ru.sberbank.ditsib.transport.vehicle.mapper.AttorneyMapperImpl;
import ru.sberbank.ditsib.transport.vehicle.mapper.EmployeeMapper;
import ru.sberbank.ditsib.transport.vehicle.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.vehicle.service.impl.AttorneyServiceImpl;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.sberbank.ditsib.transport.vehicle.exception.AttorneyNotFoundException.MSG_FORMAT;

@ExtendWith(MockitoExtension.class)
class AttorneyServiceTest {

    @Spy
    private final EmployeeMapper employeeMapper = Mappers.getMapper(EmployeeMapper.class);

    @Spy
    private final AttorneyMapper mapper = new AttorneyMapperImpl(employeeMapper);

    @Mock
    private AttorneyRepository attorneyRepository;

    @Mock
    private EmployeeService employeeService;
    
    @Mock
    private Clock clock;
    
    @InjectMocks
    private AttorneyServiceImpl attorneyService;
    
    private final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
    private final Clock fixedClock = Clock.fixed(LOCAL_DATE_TIME.toInstant(ZoneOffset.UTC), ZoneOffset.of(ZoneOffset.UTC.getId()));
    
    @Test
    @DisplayName("Добавление доверенности сотрудником из одной организации должна быть позволена")
    void createAttorneySuccess() {
        var telemechanic = Instancio.create(Employee.class);
        var user = Instancio.create(Employee.class).toBuilder().organization(telemechanic.getOrganization()).build();
        var attorney = Instancio.create(Attorney.class);

        doReturn(Optional.empty()).when(attorneyRepository).findByAttorneyId(any());
        doReturn(telemechanic).when(employeeService).getByUserId(telemechanic.getId());
        doReturn(user).when(employeeService).getByUserId(user.getUserId());
        doReturn(attorney).when(attorneyRepository).saveAndFlush(any(Attorney.class));

        var request = new AttorneyCreateDto(telemechanic.getId(),
                UUID.randomUUID(),
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                "Notorious B.I.G.");

        attorneyService.create(request, user.getUserId());

        verify(attorneyRepository, times(1)).saveAndFlush(any());
    }

    @Test
    @DisplayName("Добавление доверенности сотрудником из иной организации должна приводить к ошибке")
    void createAttorneyFail() {
        var telemechanic = Instancio.create(Employee.class);
        var user = Instancio.create(Employee.class);

        doReturn(telemechanic).when(employeeService).getByUserId(telemechanic.getId());
        doReturn(user).when(employeeService).getByUserId(user.getUserId());

        var request = new AttorneyCreateDto(telemechanic.getId(),
                UUID.randomUUID(),
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                "Notorious B.I.G.");

        var userId = user.getUserId();
        assertThatThrownBy(() -> attorneyService.create(request, userId))
                .isInstanceOf(AccessDeniedException.class);

        verify(attorneyRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Добавление доверенности с датой окончания доверенности раньше чем датой выдачи должна приводить к ошибке")
    void createAttorneyWrongDatesFail() {
        var telemechanic = Instancio.create(Employee.class);
        var user = Instancio.create(Employee.class).toBuilder().organization(telemechanic.getOrganization()).build();

        var request = new AttorneyCreateDto(telemechanic.getId(),
                UUID.randomUUID(),
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().minusDays(10),
                "Notorious B.I.G.");

        var userId = user.getUserId();
        assertThatThrownBy(() -> attorneyService.create(request, userId))
                .isInstanceOf(AttorneyWrongDatesException.class);

        verify(attorneyRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Добавление доверенности для одного телемеханика с совпадающими номерами должна приводить к ошибке")
    void createAttorneyNotUniqueFail() {
        var telemechanic = Instancio.create(Employee.class);
        var user = Instancio.create(Employee.class).toBuilder().organization(telemechanic.getOrganization()).build();

        doReturn(Optional.of(Instancio.create(Attorney.class))).when(attorneyRepository).findByAttorneyId(any());
        doReturn(telemechanic).when(employeeService).getByUserId(telemechanic.getId());
        doReturn(user).when(employeeService).getByUserId(user.getUserId());

        var request = new AttorneyCreateDto(telemechanic.getId(),
                UUID.randomUUID(),
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                "Notorious B.I.G.");

        var userId = user.getUserId();
        assertThatThrownBy(() -> attorneyService.create(request, userId))
                .isInstanceOf(NonUniqueAttorneyException.class);

        verify(attorneyRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Обновление доверенности для одного телемеханика с совпадающими номерами должна приводить к ошибке")
    void updateAttorneyNotUniqueFail() {
        var telemechanic = Instancio.create(Employee.class);
        var user = Instancio.create(Employee.class).toBuilder().organization(telemechanic.getOrganization()).build();

        var id = UUID.randomUUID();
        doReturn(Optional.of(Instancio.create(Attorney.class).toBuilder().telemechanic(telemechanic).build())).when(attorneyRepository).findById(id);
        doReturn(Optional.of(Instancio.create(Attorney.class).toBuilder().telemechanic(telemechanic).build()))
                .when(attorneyRepository).findByAttorneyId(any());
        doReturn(user).when(employeeService).getByUserId(user.getUserId());

        var request = new AttorneyUpdateDto(
                UUID.randomUUID(),
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                "Notorious B.I.G.");

        var userId = user.getUserId();
        assertThatThrownBy(() -> attorneyService.update(request, id, userId))
                .isInstanceOf(NonUniqueAttorneyException.class);

        verify(attorneyRepository, never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Обновление доверенности существующей доверенности должно происходить без ошибки")
    void updateAttorneySuccess() {
        var telemechanic = Instancio.create(Employee.class);
        var user = Instancio.create(Employee.class).toBuilder().organization(telemechanic.getOrganization()).build();

        var attorney = Instancio.create(Attorney.class).toBuilder().telemechanic(telemechanic).build();
        doReturn(Optional.of(attorney)).when(attorneyRepository).findById(attorney.getId());
        doReturn(Optional.of(attorney))
                .when(attorneyRepository).findByAttorneyId(any());
        doReturn(user).when(employeeService).getByUserId(user.getUserId());

        var request = new AttorneyUpdateDto(
                UUID.randomUUID(),
                LocalDateTime.now().minusDays(10),
                LocalDateTime.now().plusDays(10),
                "Notorious B.I.G.");

        var userId = user.getUserId();
        var updated = attorneyService.update(request, attorney.getId(), userId);

        assertThat(updated).isNotNull();
    }
    
    @Test
    @DisplayName("Поиск доверенности по идентификатору телемеханика")
    void getAttorneyByTelemechanicId() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        
        var telemechanicId = UUID.randomUUID();
        var firstAttorney = Instancio.create(Attorney.class);
        firstAttorney.setExpiryDate(LocalDateTime.now().plusMonths(10));
        var secondAttorney = Instancio.create(Attorney.class);
        secondAttorney.setExpiryDate(firstAttorney.getExpiryDate().plusMonths(15));
        when(attorneyRepository.getAllByTelemechanicId(telemechanicId))
                .thenReturn(List.of(firstAttorney, secondAttorney));
        var actual = attorneyService.getAttorneyByTelemechanicId(telemechanicId);
        assertEquals(secondAttorney.getId(), actual.id());
    }
    
    @Test
    @DisplayName("Доверенность просроченна")
    void getAttorneyNotFound() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        
        var telemechanicId = UUID.randomUUID();
        var firstAttorney = Instancio.create(Attorney.class);
        firstAttorney.setExpiryDate(LocalDateTime.now().minusYears(10));
        when(attorneyRepository.getAllByTelemechanicId(telemechanicId))
                .thenReturn(List.of(firstAttorney));
        var exception = assertThrows(AttorneyNotFoundException.class,
                                                () -> attorneyService.getAttorneyByTelemechanicId(telemechanicId));
        assertEquals(String.format(MSG_FORMAT, telemechanicId), exception.getMessage());
        
    }

}
