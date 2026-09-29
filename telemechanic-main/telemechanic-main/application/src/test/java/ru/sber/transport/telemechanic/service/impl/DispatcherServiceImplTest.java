package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.instancio.junit.InstancioExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.sber.transport.telemechanic.config.properties.EwbSberProperties;
import ru.sber.transport.telemechanic.database.dao.DispatcherRepository;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.dispatcher.*;
import ru.sber.transport.telemechanic.exception.dispatcher.AttorneyException;
import ru.sber.transport.telemechanic.exception.dispatcher.DispatcherException;
import ru.sber.transport.telemechanic.exception.dispatcher.FleetOwnerDepartmentException;
import ru.sber.transport.telemechanic.exception.dispatcher.FleetOwnerOrganizationException;
import ru.sber.transport.telemechanic.helper.CentralOrganizationHelper;
import ru.sber.transport.telemechanic.mapper.DispatcherMapper;
import ru.sber.transport.telemechanic.messaging.listener.message.DispatcherMessage;
import ru.sber.transport.telemechanic.service.DepartmentService;
import ru.sber.transport.telemechanic.service.EwbTariffService;
import ru.sber.transport.telemechanic.service.OrganizationAddressService;
import ru.sber.transport.telemechanic.service.OrganizationService;

import java.time.*;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.instancio.Select.field;
import static org.mockito.Mockito.*;
import static ru.sber.transport.telemechanic.exception.dispatcher.AttorneyException.NOT_UNIQUE_ATTORNEY_MSG;
import static ru.sber.transport.telemechanic.exception.dispatcher.AttorneyException.NOT_UNIQUE_EMPLOYEE_MSG;
import static ru.sber.transport.telemechanic.exception.dispatcher.DispatcherException.NOT_FOUND_BY_ATTORNEY_NUMBER_MSG;

@ExtendWith(value = { MockitoExtension.class, InstancioExtension.class })
class DispatcherServiceImplTest {
    
    @Mock
    private OrganizationService organizationService;
    
    @Mock
    private OrganizationAddressService organizationAddressService;
    
    @Mock
    private EwbTariffService ewbTariffService;
    
    @Mock
    private DepartmentService departmentService;
    
    @Mock
    private EmployeeServiceImpl employeeService;
    
    @Mock
    private DispatcherRepository dispatcherRepository;
    
    @Mock
    private DispatcherMapper dispatcherMapper;
    
    @Mock
    private CentralOrganizationHelper centralOrganizationHelper;
    
    @Mock
    private Clock clock;
    private final LocalDateTime NOW = LocalDateTime.of(2024, 9, 17, 16, 10);
    private final Clock fixedClock = Clock.fixed(NOW.toInstant(ZoneOffset.UTC), ZoneId.of(ZoneOffset.UTC.getId()));
    
    @InjectMocks
    private DispatcherServiceImpl dispatcherService;
    
    @Test
    void addDispatcher() {
        var request = Instancio.of(AddDispatcherRequest.class).create();
        
        doReturn(Optional.empty()).when(organizationService).get(request.organizationId());
        assertThatExceptionOfType(FleetOwnerOrganizationException.class)
                .isThrownBy(() -> dispatcherService.addDispatcher(request))
                .withMessage("Не найдена организация владельца автопарка %s".formatted(request.organizationId()));
        
        doReturn(Optional.of(
                Instancio.of(Organization.class)
                         .supply(field("active"), () -> false)
                         .create()
                            )).when(organizationService).get(request.organizationId());
        assertThatExceptionOfType(FleetOwnerOrganizationException.class)
                .isThrownBy(() -> dispatcherService.addDispatcher(request))
                .withMessage("Не активна организация владельца автопарка %s".formatted(request.organizationId()));
        
        doReturn(Optional.of(
                Instancio.of(Organization.class)
                         .supply(field("active"), () -> true)
                         .create()
                            )).when(organizationService).get(request.organizationId());
        doReturn(false).when(ewbTariffService).existsActiveTariffByOrganizationId(request.organizationId());
        assertThatExceptionOfType(FleetOwnerOrganizationException.class)
                .isThrownBy(() -> dispatcherService.addDispatcher(request))
                .withMessage("Организация не является владельцем автопарка. ИД организации %s".formatted(request.organizationId()));
        
        doReturn(true).when(ewbTariffService).existsActiveTariffByOrganizationId(request.organizationId());
        doReturn(Optional.empty()).when(departmentService).get(request.departmentId());
        assertThatExceptionOfType(FleetOwnerDepartmentException.class)
                .isThrownBy(() -> dispatcherService.addDispatcher(request))
                .withMessage("Не найдено подразделение владельца автопарка %s".formatted(request.departmentId()));
        
        doReturn(Optional.of(
                Instancio.of(Department.class)
                         .supply(field("active"), () -> false)
                         .create()
                            )).when(departmentService).get(request.departmentId());
        assertThatExceptionOfType(FleetOwnerDepartmentException.class)
                .isThrownBy(() -> dispatcherService.addDispatcher(request))
                .withMessage("Не активно подразделение владельца автопарка %s".formatted(request.departmentId()));
        
        doReturn(Optional.of(
                Instancio.of(Department.class)
                         .supply(field("active"), () -> true)
                         .create()
                            )).when(departmentService).get(request.departmentId());
        assertThatExceptionOfType(FleetOwnerDepartmentException.class)
                .isThrownBy(() -> dispatcherService.addDispatcher(request))
                .withMessage("Подразделение %s не найдено в организации с id:%s".formatted(request.departmentId(), request.organizationId()));
        
        doReturn(Optional.of(
                Instancio.of(Department.class)
                         .supply(field("active"), () -> true)
                         .supply(field("organization"), () -> Instancio.of(Organization.class)
                                                                       .supply(field("id"), request::organizationId)
                                                                       .create())
                         .create()
                            )).when(departmentService).get(request.departmentId());
        doReturn(Optional.empty()).when(employeeService).get(request.employeeId());
        assertThatExceptionOfType(DispatcherException.class)
                .isThrownBy(() -> dispatcherService.addDispatcher(request))
                .withMessage("Не найден диспетчер ID %s".formatted(request.employeeId()));
        
        doReturn(Optional.of(
                Instancio.of(Employee.class).create()
                            )).when(employeeService).get(request.employeeId());
        assertThatExceptionOfType(DispatcherException.class)
                .isThrownBy(() -> dispatcherService.addDispatcher(request))
                .withMessage("Сотрудник %s не найден в подразделении с id:%s".formatted(request.employeeId(), request.departmentId()));
        
        doReturn(Optional.of(
                Instancio.of(Employee.class)
                         .supply(field("department"), () -> Instancio.of(Department.class)
                                                                     .supply(field("id"), request::departmentId)
                                                                     .create())
                         .create()
                            )).when(employeeService).get(request.employeeId());
        
        doReturn(true).when(dispatcherRepository)
                      .existsByEmployee_IdAndOrganization_IdAndActiveIsTrue(request.employeeId(), request.organizationId());
        assertThatExceptionOfType(AttorneyException.class)
                .isThrownBy(() -> dispatcherService.addDispatcher(request))
                .withMessage(NOT_UNIQUE_EMPLOYEE_MSG.formatted(request.employeeId(), request.organizationId()));
        
        doReturn(false).when(dispatcherRepository)
                       .existsByEmployee_IdAndOrganization_IdAndActiveIsTrue(request.employeeId(), request.organizationId());
        doReturn(true).when(dispatcherRepository)
                      .existsByAttorney_NumberAndOrganization_IdAndActiveIsTrue(request.attorneyNumber(), request.organizationId());
        assertThatExceptionOfType(AttorneyException.class)
                .isThrownBy(() -> dispatcherService.addDispatcher(request))
                .withMessage(NOT_UNIQUE_ATTORNEY_MSG.formatted(request.attorneyNumber(), request.organizationId()));
        
        var savedDispatcher = Instancio.of(Dispatcher.class).create();
        doReturn(false).when(dispatcherRepository)
                       .existsByAttorney_NumberAndOrganization_IdAndActiveIsTrue(request.attorneyNumber(), request.organizationId());
        doReturn(savedDispatcher).when(dispatcherMapper).addDispatcherRequestToDispatcher(request);
        assertThatNoException().isThrownBy(() -> dispatcherService.addDispatcher(request));
        verify(dispatcherRepository, times(1)).save(savedDispatcher);
    }
    
    @Test
    void editDispatcher() {
        var request = Instancio.of(EditDispatcherRequest.class).create();
        var dispatcherId = UUID.randomUUID();
        
        doReturn(Optional.empty()).when(dispatcherRepository).findById(dispatcherId);
        assertThatExceptionOfType(DispatcherException.class)
                .isThrownBy(() -> dispatcherService.editDispatcher(dispatcherId, request))
                .withMessage("Не найден диспетчер ID %s".formatted(dispatcherId));
        
        doReturn(Optional.of(
                Instancio.of(Dispatcher.class)
                         .supply(field("active"), () -> false)
                         .create()
                            )).when(dispatcherRepository).findById(dispatcherId);
        assertThatExceptionOfType(DispatcherException.class)
                .isThrownBy(() -> dispatcherService.editDispatcher(dispatcherId, request))
                .withMessage("Нельзя отредактировать. Неактивна запись для диспетчера ID:%s".formatted(dispatcherId));
        
        var dispatcher = Instancio.of(Dispatcher.class)
                                  .supply(field("active"), () -> true)
                                  .create();
        
        doReturn(Optional.of(dispatcher)).when(dispatcherRepository).findById(dispatcherId);
        doReturn(true).when(dispatcherRepository)
                      .existsByAttorney_NumberAndOrganization_IdAndActiveIsTrue(request.attorneyNumber(), dispatcher.getOrganization().getId());
        assertThatExceptionOfType(AttorneyException.class)
                .isThrownBy(() -> dispatcherService.editDispatcher(dispatcherId, request))
                .withMessage(NOT_UNIQUE_ATTORNEY_MSG.formatted(request.attorneyNumber(), dispatcher.getOrganization().getId()));
        
        doReturn(false).when(dispatcherRepository)
                       .existsByAttorney_NumberAndOrganization_IdAndActiveIsTrue(request.attorneyNumber(), dispatcher.getOrganization().getId());
        assertThatNoException().isThrownBy(() -> dispatcherService.editDispatcher(dispatcherId, request));
        verify(dispatcherRepository, times(1)).save(any());
        
        doReturn(Optional.of(
                Instancio.of(Dispatcher.class)
                         .supply(field("active"), () -> true)
                         .supply(field("attorney"), () -> new Attorney().setNumber(request.attorneyNumber()))
                         .create()
                            )).when(dispatcherRepository).findById(dispatcherId);
        assertThatNoException().isThrownBy(() -> dispatcherService.editDispatcher(dispatcherId, request));
        verify(dispatcherRepository, times(2)).save(any());
    }
    
    @Test
    void getDispatcher() {
        var dispatcherId = UUID.randomUUID();
        var dispatcher = Instancio.of(Dispatcher.class).create();
        var mappedDispatcher = Instancio.of(GetDispatcherResponse.class).create();
        
        doReturn(Optional.empty()).when(dispatcherRepository).findById(dispatcherId);
        assertThatExceptionOfType(DispatcherException.class)
                .isThrownBy(() -> dispatcherService.getDispatcher(dispatcherId))
                .withMessage("Не найден диспетчер ID %s".formatted(dispatcherId));
        
        doReturn(Optional.of(dispatcher)).when(dispatcherRepository).findById(dispatcherId);
        doReturn(mappedDispatcher).when(dispatcherMapper).dispatcherToGetDispatcherResponse(dispatcher);
        var actual = dispatcherService.getDispatcher(dispatcherId);
        assertThat(actual).isEqualTo(mappedDispatcher);
    }
    
    @Test
    void deactivateDispatcher() {
        var dispatcherId = UUID.randomUUID();
        
        doReturn(Optional.empty()).when(dispatcherRepository).findById(dispatcherId);
        assertThatExceptionOfType(DispatcherException.class)
                .isThrownBy(() -> dispatcherService.deactivateDispatcher(dispatcherId))
                .withMessage("Не найден диспетчер ID %s".formatted(dispatcherId));
        
        doReturn(Optional.of(
                Instancio.of(Dispatcher.class)
                         .supply(field("active"), () -> false)
                         .create()
                            )).when(dispatcherRepository).findById(dispatcherId);
        assertThatExceptionOfType(DispatcherException.class)
                .isThrownBy(() -> dispatcherService.deactivateDispatcher(dispatcherId))
                .withMessage("Неактивна запись для диспетчера ID:%s".formatted(dispatcherId));
        
        doReturn(Optional.of(
                Instancio.of(Dispatcher.class)
                         .supply(field("active"), () -> true)
                         .create()
                            )).when(dispatcherRepository).findById(dispatcherId);
        assertThatNoException().isThrownBy(() -> dispatcherService.deactivateDispatcher(dispatcherId));
        verify(dispatcherRepository, times(1)).save(any());
    }
    
    @Test
    void search() {
        var request = Instancio.of(SearchDispatcherRequest.class).create();
        var dispatcher = Instancio.of(Dispatcher.class).create();
        var mappedDispatcher = Instancio.of(GetDispatcherResponse.class).create();
        
        doReturn(new PageImpl<>(List.of(dispatcher), PageRequest.of(0, 10), 1L))
                .when(dispatcherRepository)
                .findAllBySearchFilters(any(SearchDispatcherRequest.class), any(PageRequest.class));
        doReturn(mappedDispatcher).when(dispatcherMapper).dispatcherToGetDispatcherResponse(dispatcher);
        var actual = dispatcherService.search(request);
        assertThat(actual.getContent()).hasSize(1);
        assertThat(actual.getTotalElements()).isEqualTo(1);
        assertThat(actual.getPageable())
                .extracting(Pageable::getPageNumber, Pageable::getPageSize)
                .containsExactly(0, 10);
    }
    
    @Test
    void deactivateDispatchers() {
        doNothing().when(dispatcherRepository).setActiveFalseWhereExpiryDateBeforeNow();
        dispatcherService.deactivateDispatchers();
        verify(dispatcherRepository).setActiveFalseWhereExpiryDateBeforeNow();
    }
    
    @Test
    void getSelfOrganizationInfo() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        
        var userId = UUID.randomUUID();
        var organization = Instancio.create(Organization.class);
        var user = Instancio.of(Employee.class)
                            .set(field(Employee::getId), userId)
                            .set(field(Employee::getOrganization), organization)
                            .create();
        var dispatcher = Instancio.of(Dispatcher.class)
                                  .set(field(Dispatcher::isActive), true)
                                  .set(
                                          field(Dispatcher::getAttorney),
                                          Instancio.of(Attorney.class)
                                                   .set(field(Attorney::getExpiryDate), NOW.toLocalDate())
                                                   .create()
                                      )
                                  .create();
        var expected = Instancio.of(GetOrganizationDispatcherResponse.class).create();
        
        doReturn(user).when(employeeService).getByUserId(userId);
        doReturn(organization.getAddress()).when(organizationAddressService).get(user.getOrganization().getId());
        doReturn(List.of(dispatcher)).when(dispatcherRepository).findByEmployeeUserIdAndActiveIsTrue(userId);
        doReturn(expected).when(centralOrganizationHelper).map(any(UUID.class), any(), any());
        
        assertThat(dispatcherService.getSelfOrganizationInfo(userId)).usingRecursiveComparison().isEqualTo(expected);
        verify(dispatcherRepository).findByEmployeeUserIdAndActiveIsTrue(any(UUID.class));
        verify(centralOrganizationHelper).map(any(UUID.class), any(), any());
    }
    
    @Test
    void getSelfOrganizationInfoNoActiveDispatcher() {
        var userId = UUID.randomUUID();
        var organization = Instancio.create(Organization.class);
        var user = Instancio.of(Employee.class)
                            .set(field(Employee::getId), userId)
                            .set(field(Employee::getOrganization), organization)
                            .create();
        var dispatcher = Instancio.of(Dispatcher.class)
                                  .set(field(Dispatcher::isActive), false)
                                  .create();
        
        doReturn(user).when(employeeService).getByUserId(userId);
        doReturn(organization.getAddress()).when(organizationAddressService).get(user.getOrganization().getId());
        doReturn(List.of(dispatcher)).when(dispatcherRepository).findByEmployeeUserIdAndActiveIsTrue(userId);
        assertThatExceptionOfType(DispatcherException.class)
                .isThrownBy(() -> dispatcherService.getSelfOrganizationInfo(userId))
                .withMessage("Не найдено ни одной активной записи диспетчера. user id сотрудника :%s", userId);
        verify(dispatcherRepository).findByEmployeeUserIdAndActiveIsTrue(any(UUID.class));
    }
    
    @Test
    void getSelfOrganizationInfoAttorneyExpired() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        
        var userId = UUID.randomUUID();
        var organization = Instancio.create(Organization.class);
        var user = Instancio.of(Employee.class)
                            .set(field(Employee::getId), userId)
                            .set(field(Employee::getOrganization), organization)
                            .create();
        var dispatcher = Instancio.of(Dispatcher.class)
                                  .set(field(Dispatcher::isActive), true)
                                  .set(
                                          field(Dispatcher::getAttorney),
                                          Instancio.of(Attorney.class)
                                                   .set(field(Attorney::getExpiryDate), NOW.toLocalDate().minusMonths(2))
                                                   .create()
                                      )
                                  .create();
        
        doReturn(user).when(employeeService).getByUserId(userId);
        doReturn(organization.getAddress()).when(organizationAddressService).get(user.getOrganization().getId());
        doReturn(List.of(dispatcher)).when(dispatcherRepository).findByEmployeeUserIdAndActiveIsTrue(userId);
        assertThatExceptionOfType(AttorneyException.class)
                .isThrownBy(() -> dispatcherService.getSelfOrganizationInfo(userId))
                .withMessage(AttorneyException.ATTORNEY_EXPIRED, dispatcher.getAttorney().getId());
        verify(dispatcherRepository).findByEmployeeUserIdAndActiveIsTrue(any(UUID.class));
    }
    
    @Test
    void getSelfOrganizationInfoDispatcherNotFound() {
        var userId = UUID.randomUUID();
        var organization = Instancio.create(Organization.class);
        var user = Instancio.of(Employee.class)
                            .set(field(Employee::getId), userId)
                            .set(field(Employee::getOrganization), organization)
                            .create();
        
        doReturn(user).when(employeeService).getByUserId(userId);
        doReturn(organization.getAddress()).when(organizationAddressService).get(user.getOrganization().getId());
        doReturn(Collections.emptyList()).when(dispatcherRepository).findByEmployeeUserIdAndActiveIsTrue(userId);
        assertThatExceptionOfType(DispatcherException.class)
                .isThrownBy(() -> dispatcherService.getSelfOrganizationInfo(userId))
                .withMessage(DispatcherException.NOT_FOUND_BY_USER_ID_MSG, userId);
        verify(dispatcherRepository).findByEmployeeUserIdAndActiveIsTrue(any(UUID.class));
    }
    
    @Test
    void getSelfOrganizationInfoMultipleActiveDispatchers() {
        var userId = UUID.randomUUID();
        var organization = Instancio.create(Organization.class);
        var user = Instancio.of(Employee.class)
                            .set(field(Employee::getId), userId)
                            .set(field(Employee::getOrganization), organization)
                            .create();
        var dispatcher1 = Instancio.of(Dispatcher.class)
                                  .set(field(Dispatcher::isActive), true)
                                  .create();
        var dispatcher2 = Instancio.of(Dispatcher.class)
                                  .set(field(Dispatcher::isActive), true)
                                  .create();
        
        doReturn(user).when(employeeService).getByUserId(userId);
        doReturn(organization.getAddress()).when(organizationAddressService).get(user.getOrganization().getId());
        doReturn(List.of(dispatcher1, dispatcher2)).when(dispatcherRepository).findByEmployeeUserIdAndActiveIsTrue(userId);
        assertThatExceptionOfType(DispatcherException.class)
                .isThrownBy(() -> dispatcherService.getSelfOrganizationInfo(userId))
                .withMessage("Найдено более чем одна активная запись диспетчера. user id сотрудника :%s", userId);
        verify(dispatcherRepository).findByEmployeeUserIdAndActiveIsTrue(any(UUID.class));
    }
    
    @Test
    void getSelfOrganizationInfoCentralOrganization() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        
        var userId = UUID.randomUUID();
        var organization = Instancio.create(Organization.class);
        var user = Instancio.of(Employee.class)
                            .set(field(Employee::getId), userId)
                            .set(field(Employee::getOrganization), organization)
                            .create();
        var dispatcher = Instancio.of(Dispatcher.class)
                                  .set(field(Dispatcher::isActive), true)
                                  .set(
                                          field(Dispatcher::getAttorney),
                                          Instancio.of(Attorney.class)
                                                   .set(field(Attorney::getExpiryDate), NOW.toLocalDate())
                                                   .create()
                                      )
                                  .create();
        var expected = Instancio.of(GetOrganizationDispatcherResponse.class).create();
        
        doReturn(user).when(employeeService).getByUserId(userId);
        doReturn(organization.getAddress()).when(organizationAddressService).get(user.getOrganization().getId());
        doReturn(List.of(dispatcher)).when(dispatcherRepository).findByEmployeeUserIdAndActiveIsTrue(userId);
        doReturn(expected).when(centralOrganizationHelper).map(any(UUID.class), any(), any());
        
        assertThat(dispatcherService.getSelfOrganizationInfo(userId)).usingRecursiveComparison().isEqualTo(expected);
        verify(dispatcherRepository).findByEmployeeUserIdAndActiveIsTrue(any(UUID.class));
        verify(organizationService, never()).getFirstContactPhone(any(UUID.class));
        verify(centralOrganizationHelper).map(any(UUID.class), any(), any());
    }
    
    @Test
    void testSaveMessageWhenActiveAndNewDispatcherCreated() {
        var message = new DispatcherMessage(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                "Иванов",
                "Иван",
                "Иванович",
                "+79991234567",
                true,
                "ivanov@example.com",
                UUID.randomUUID(),
                true,
                true,
                UUID.randomUUID(),
                UUID.randomUUID(),
                true,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                "TEST_SYSTEM",
                UUID.randomUUID()
        );
        
        when(dispatcherRepository.findByEmployeeIdAndActiveIsTrue(any()))
                .thenReturn(Optional.empty());
        
        dispatcherService.saveMessage(message);
        
        verify(employeeService).get(message.oauthId());
        verify(dispatcherRepository).save(any());
    }
    
    @Test
    void testSaveMessageWhenActiveAndExistingDispatcherUpdated() {
        var message = new DispatcherMessage(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                "Иванов",
                "Иван",
                "Иванович",
                "+79991234567",
                true,
                "ivanov@example.com",
                UUID.randomUUID(),
                true,
                true,
                UUID.randomUUID(),
                UUID.randomUUID(),
                true,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                "TEST_SYSTEM",
                UUID.randomUUID()
        );
        var existingDispatcher = Instancio.create(Dispatcher.class);
        existingDispatcher.setActive(true);
        
        when(dispatcherRepository.findByEmployeeIdAndActiveIsTrue(any()))
                .thenReturn(Optional.of(existingDispatcher));
        
        dispatcherService.saveMessage(message);
        
        assertThat(existingDispatcher.isActive()).isTrue();
        verify(dispatcherRepository).save(existingDispatcher);
    }
    
    // Тестирование неактивного сообщения
    @Test
    void testSaveMessageWhenInactiveAndNoDispatcherFoundThrowsException() {
        var message = new DispatcherMessage(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                "Иванов",
                "Иван",
                "Иванович",
                "+79991234567",
                true,
                "ivanov@example.com",
                UUID.randomUUID(),
                false,
                true,
                UUID.randomUUID(),
                UUID.randomUUID(),
                true,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                "TEST_SYSTEM",
                UUID.randomUUID()
        );
        
        when(dispatcherRepository.findByAttorneyNumber(any()))
                .thenReturn(Optional.empty()); // Не находим никакого диспетчера
        
        Throwable exception = catchThrowable(() -> dispatcherService.saveMessage(message));
        
        assertThat(exception)
                .isInstanceOf(DispatcherException.class)
                .hasMessageContaining(String.format(NOT_FOUND_BY_ATTORNEY_NUMBER_MSG, message.attorneyNumber()));
    }
    
    @Test
    void testSaveMessageWhenInactiveAndDispatcherDeactivated() {
        var message = new DispatcherMessage(
                UUID.randomUUID(),
                UUID.randomUUID().toString(),
                "Иванов",
                "Иван",
                "Иванович",
                "+79991234567",
                true,
                "ivanov@example.com",
                UUID.randomUUID(),
                false,
                true,
                UUID.randomUUID(),
                UUID.randomUUID(),
                true,
                LocalDate.now(),
                LocalDate.now().plusDays(7),
                "TEST_SYSTEM",
                UUID.randomUUID()
        );
        var existingDispatcher = Instancio.create(Dispatcher.class);
        existingDispatcher.setActive(true);
        
        when(dispatcherRepository.findByAttorneyNumber(any()))
                .thenReturn(Optional.of(existingDispatcher));
        
        dispatcherService.saveMessage(message);
        
        assertThat(existingDispatcher.isActive()).isFalse();
        verify(dispatcherRepository).save(existingDispatcher);
    }
    
}