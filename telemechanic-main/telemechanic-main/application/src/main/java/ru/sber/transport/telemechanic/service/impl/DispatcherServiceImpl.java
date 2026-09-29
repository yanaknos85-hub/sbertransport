package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ru.sber.transport.telemechanic.database.dao.DispatcherRepository;
import ru.sber.transport.telemechanic.database.model.Attorney;
import ru.sber.transport.telemechanic.database.model.Dispatcher;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.dto.dispatcher.*;
import ru.sber.transport.telemechanic.exception.OrganizationEmptyPropertyException;
import ru.sber.transport.telemechanic.exception.dispatcher.AttorneyException;
import ru.sber.transport.telemechanic.exception.dispatcher.DispatcherException;
import ru.sber.transport.telemechanic.exception.dispatcher.FleetOwnerDepartmentException;
import ru.sber.transport.telemechanic.exception.dispatcher.FleetOwnerOrganizationException;
import ru.sber.transport.telemechanic.helper.CentralOrganizationHelper;
import ru.sber.transport.telemechanic.mapper.DispatcherMapper;
import ru.sber.transport.telemechanic.messaging.listener.message.DispatcherMessage;
import ru.sber.transport.telemechanic.service.*;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import static ru.sber.transport.telemechanic.exception.dispatcher.AttorneyException.*;

@Service
@RequiredArgsConstructor
public class DispatcherServiceImpl implements DispatcherService {
    private final OrganizationService organizationService;
    private final OrganizationAddressService organizationAddressService;
    private final DepartmentService departmentService;
    private final EmployeeService employeeService;
    private final EwbTariffService ewbTariffService;
    private final DispatcherRepository dispatcherRepository;
    private final DispatcherMapper dispatcherMapper;
    private final Clock clock;
    private final CentralOrganizationHelper centralOrganizationHelper;
    
    @Override
    @Transactional
    public void addDispatcher(AddDispatcherRequest addDispatcherRequest) {
        validateDispatcher(addDispatcherRequest);
        dispatcherRepository.save(dispatcherMapper.addDispatcherRequestToDispatcher(addDispatcherRequest));
    }
    
    @Override
    @Transactional
    public void editDispatcher(UUID dispatcherId, EditDispatcherRequest editDispatcherRequest) {
        var dispatcher = dispatcherRepository.findById(dispatcherId)
                                             .orElseThrow(() -> new DispatcherException(DispatcherException.NOT_FOUND_MSG.formatted(dispatcherId)));
        if (!dispatcher.isActive()) {
            throw new DispatcherException(DispatcherException.EDIT_NOT_ACTIVE_MSG.formatted(dispatcherId));
        }
        if (!dispatcher.getAttorney().getNumber().equals(editDispatcherRequest.attorneyNumber()) &&
                dispatcherRepository.existsByAttorney_NumberAndOrganization_IdAndActiveIsTrue(editDispatcherRequest.attorneyNumber(), dispatcher.getOrganization().getId())) {
            throw new AttorneyException(
                    NOT_UNIQUE_ATTORNEY_MSG.formatted(editDispatcherRequest.attorneyNumber(), dispatcher.getOrganization().getId())
            );
        }
        dispatcherRepository.save(dispatcher.setAttorney(
                                          dispatcher.getAttorney()
                                                    .setNumber(editDispatcherRequest.attorneyNumber())
                                                    .setIssueDate(editDispatcherRequest.issueDate())
                                                    .setExpiryDate(editDispatcherRequest.expiryDate())
                                                    .setCreationSystem(editDispatcherRequest.creationSystem())
                                                        )
                                 );
    }
    
    @Override
    public GetDispatcherResponse getDispatcher(UUID dispatcherId) {
        var dispatcher = dispatcherRepository.findById(dispatcherId)
                                             .orElseThrow(() -> new DispatcherException(DispatcherException.NOT_FOUND_MSG.formatted(dispatcherId)));
        return dispatcherMapper.dispatcherToGetDispatcherResponse(dispatcher);
    }
    
    
    @Override
    @Transactional
    public void deactivateDispatcher(UUID id) {
        var dispatcher = dispatcherRepository.findById(id)
                                             .orElseThrow(() -> new DispatcherException(DispatcherException.NOT_FOUND_MSG.formatted(id)));
        if (!dispatcher.isActive()) {
            throw new DispatcherException(DispatcherException.NOT_ACTIVE_MSG.formatted(id));
        }
        dispatcherRepository.save(dispatcher.setActive(false));
    }
    
    @Override
    public Page<GetDispatcherResponse> search(SearchDispatcherRequest searchDispatcherRequest) {
        var pageRequest = searchDispatcherRequest.preparePageRequest();
        var result = dispatcherRepository.findAllBySearchFilters(searchDispatcherRequest, pageRequest);
        return new PageImpl<>(
                result.getContent().stream()
                      .map(dispatcherMapper::dispatcherToGetDispatcherResponse)
                      .toList(),
                result.getPageable(),
                result.getTotalElements()
        );
    }
    
    @Transactional
    @Override
    public void deactivateDispatchers() {
        dispatcherRepository.setActiveFalseWhereExpiryDateBeforeNow();
    }
    
    @Override
    public GetOrganizationDispatcherResponse getSelfOrganizationInfo(UUID userId) {
        var userOrganizationId = employeeService.getByUserId(userId).getOrganization().getId();
        var address = organizationAddressService.get(userOrganizationId);
        var dispatcher = getActiveDispatcher(userId);
        if (dispatcher.getAttorney().getExpiryDate().isBefore(LocalDate.now(clock))) {
            throw new AttorneyException(ATTORNEY_EXPIRED.formatted(dispatcher.getAttorney().getId()));
        }
        return createGetOrganizationDispatcherResponse(
                dispatcher.getEmployee().getOrganization(),
                dispatcher,
                address.getRegion().getCode());
    }
    
    @Override
    public Dispatcher getByEmployeeIdAndActive(UUID employeeId) {
        return dispatcherRepository.findByEmployeeUserIdAndActiveIsTrue(employeeId).stream()
                                   .filter(Dispatcher::isActive)
                                   .findFirst()
                                   .orElseThrow(() -> new DispatcherException(DispatcherException.NOT_FOUND_BY_USER_ID_MSG.formatted(employeeId)));
    }
    
    @Override
    @Transactional
    public void saveMessage(DispatcherMessage message) {
        Dispatcher dispatcher;
        if (message.active()) {
            dispatcher = dispatcherRepository.findByEmployeeIdAndActiveIsTrue(message.oauthId())
                                             .orElseGet(Dispatcher::new);
            dispatcher.setActive(true);
        } else {
            dispatcher = dispatcherRepository.findByAttorneyNumber(message.attorneyNumber())
                    .orElseThrow(() -> new DispatcherException(DispatcherException.NOT_FOUND_BY_ATTORNEY_NUMBER_MSG.formatted(message.attorneyNumber())));
            dispatcher.setActive(false);
            dispatcherRepository.save(dispatcher);
            return;
        }
        if (dispatcher.getEmployee() == null) {
            employeeService.get(message.oauthId()).ifPresent(e -> {
                dispatcher.setEmployee(e);
                dispatcher.setOrganization(e.getOrganization());
                dispatcher.setDepartment(e.getDepartment());
            });
        }
        dispatcher.setAutoparkId(message.autoparkId());
        dispatcher.setContractorId(message.contractorId());
        
        Attorney attorney = dispatcher.getAttorney() != null
                            ? dispatcher.getAttorney()
                            : new Attorney();
        attorney.setNumber(message.attorneyNumber());
        attorney.setIssueDate(message.issueDate());
        attorney.setExpiryDate(message.expiryDate());
        attorney.setCreationSystem(message.creationSystem());
        
        dispatcher.setAttorney(attorney);
        dispatcherRepository.save(dispatcher);
    }
    
    
    private Dispatcher getActiveDispatcher(UUID userId) {
        var dispatchers = dispatcherRepository.findByEmployeeUserIdAndActiveIsTrue(userId);
        if (dispatchers.isEmpty()) {
            throw new DispatcherException(DispatcherException.NOT_FOUND_BY_USER_ID_MSG.formatted(userId));
        }
        if (dispatchers.stream()
                       .noneMatch(Dispatcher::isActive)) {
            throw new DispatcherException("Не найдено ни одной активной записи диспетчера. user id сотрудника :%s".formatted(userId));
        }
        var activeDispatchers = dispatchers.stream()
                                           .filter(Dispatcher::isActive)
                                           .toList();
        if (activeDispatchers.size() != 1) {
            throw new DispatcherException("Найдено более чем одна активная запись диспетчера. user id сотрудника :%s".formatted(userId));
        }
        return dispatchers.getFirst();
    }
    
    private GetOrganizationDispatcherResponse createGetOrganizationDispatcherResponse(
            Organization organization,
            Dispatcher dispatcher,
            String regionCode
                                                                                     ) {
        return centralOrganizationHelper.map(
                organization.getOrganizationGroupId(),
                () -> dispatcherMapper.dispatcherToGetOrganizationDispatcherResponse(dispatcher,
                                                                                     getOrganizationProperty(organization.getOfficialName(),
                                                                                                             "Не заполнено наименование организации. ID:%s",
                                                                                                             organization.getId()),
                                                                                     organization.getMsrn(),
                                                                                     organization.getTin(),
                                                                                     getOrganizationProperty(organizationService.getFirstContactPhone(
                                                                                                                     organization.getId()),
                                                                                                             "Не найден телефон организации. ID:%s",
                                                                                                             organization.getId()),
                                                                                     regionCode),
                () -> dispatcherMapper.dispatcherToGetOrganizationDispatcherResponse(dispatcher,
                                                                                     centralOrganizationHelper.getCentralName(),
                                                                                     centralOrganizationHelper.getCentralMsrn(),
                                                                                     centralOrganizationHelper.getCentralTin(),
                                                                                     centralOrganizationHelper.getCentralPhone(),
                                                                                     regionCode));
    }
    
    private String getOrganizationProperty(String value, String text, UUID organizationId) {
        if (StringUtils.hasText(value)) {
            return value;
        } else {
            throw new OrganizationEmptyPropertyException(text.formatted(organizationId));
        }
    }
    
    private void validateDispatcher(AddDispatcherRequest addDispatcherRequest) {
        checkFleetOwnerOrganizationId(addDispatcherRequest.organizationId());
        checkFleetOwnerDepartmentId(addDispatcherRequest.organizationId(), addDispatcherRequest.departmentId());
        checkDispatcher(addDispatcherRequest.employeeId(), addDispatcherRequest.departmentId());
        checkAttorney(addDispatcherRequest.employeeId(), addDispatcherRequest.attorneyNumber(), addDispatcherRequest.organizationId());
    }

    private void checkFleetOwnerOrganizationId(UUID organizationId) {
        var organization = organizationService.get(organizationId)
                                              .orElseThrow(
                                                      () -> new FleetOwnerOrganizationException(
                                                              FleetOwnerOrganizationException.NOT_FOUND_MSG.formatted(organizationId))
                                                          );
        if (!organization.isActive()) {
            throw new FleetOwnerOrganizationException(FleetOwnerOrganizationException.NOT_ACTIVE_MSG.formatted(organizationId));
        }
        if (!ewbTariffService.existsActiveTariffByOrganizationId(organizationId)) {
            throw new FleetOwnerOrganizationException(FleetOwnerOrganizationException.NOT_FLEET_OWNER_MSG.formatted(organizationId));
        }
    }
    
    private void checkFleetOwnerDepartmentId(UUID organizationId, UUID departmentId) {
        var department = departmentService.get(departmentId)
                                          .orElseThrow(
                                                  () -> new FleetOwnerDepartmentException(
                                                          FleetOwnerDepartmentException.NOT_FOUND_MSG.formatted(departmentId))
                                                      );
        if (!department.isActive()) {
            throw new FleetOwnerDepartmentException(FleetOwnerDepartmentException.NOT_ACTIVE_MSG.formatted(departmentId));
        }
        if (department.getOrganization().getId() != organizationId) {
            throw new FleetOwnerDepartmentException(FleetOwnerDepartmentException.NOT_IN_ORGANIZATION_MSG.formatted(departmentId, organizationId));
        }
    }
    
    private void checkDispatcher(UUID employeeId, UUID departmentId) {
        var employee = employeeService.get(employeeId)
                                      .orElseThrow(
                                              () -> new DispatcherException(DispatcherException.NOT_FOUND_MSG.formatted(employeeId))
                                                  );
        if (employee.getDepartment().getId() != departmentId) {
            throw new DispatcherException(DispatcherException.NOT_IN_DEPARTMENT_MSG.formatted(employeeId, departmentId));
        }
    }
    
    private void checkAttorney(UUID employeeId, UUID attorneyNumber, UUID organizationId) {
        if (dispatcherRepository.existsByAttorney_NumberAndOrganization_IdAndActiveIsTrue(attorneyNumber, organizationId)) {
            throw new AttorneyException(
                    NOT_UNIQUE_ATTORNEY_MSG.formatted(attorneyNumber, organizationId)
            );
        }

        if (dispatcherRepository.existsByEmployee_IdAndOrganization_IdAndActiveIsTrue(employeeId, organizationId)) {
            throw new AttorneyException(NOT_UNIQUE_EMPLOYEE_MSG.formatted(employeeId, organizationId));
        }
    }
}
