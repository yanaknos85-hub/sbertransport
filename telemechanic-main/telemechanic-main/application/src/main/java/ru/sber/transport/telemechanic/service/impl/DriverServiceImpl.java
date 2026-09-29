package ru.sber.transport.telemechanic.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.telemechanic.database.dao.DriverRepository;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.driver.*;
import ru.sber.transport.telemechanic.enumerate.TransportStatus;
import ru.sber.transport.telemechanic.exception.ConflictException;
import ru.sber.transport.telemechanic.exception.DepartmentNotActiveException;
import ru.sber.transport.telemechanic.exception.TransportNotInUse;
import ru.sber.transport.telemechanic.exception.UserNotFoundException;
import ru.sber.transport.telemechanic.exception.dispatcher.FleetOwnerOrganizationException;
import ru.sber.transport.telemechanic.exception.driver.*;
import ru.sber.transport.telemechanic.helper.SnilsHelper;
import ru.sber.transport.telemechanic.mapper.DriverMapper;
import ru.sber.transport.telemechanic.mapper.DrivingLicenseMapper;
import ru.sber.transport.telemechanic.messaging.listener.message.DriverMessage;
import ru.sber.transport.telemechanic.service.*;

import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

import static ru.sber.transport.telemechanic.exception.driver.DriverAlreadyExistsException.SERIES_NUMBER_EXISTS;
import static ru.sber.transport.telemechanic.exception.driver.DriverAlreadyExistsException.TIN_SNILS_EXISTS;
import static ru.sber.transport.telemechanic.exception.driver.DriverNotFoundException.DRIVER_BY_USER_ID_MSG_FORMAT;
import static ru.sber.transport.telemechanic.helper.DriverLicenseHelper.validateNumber;
import static ru.sber.transport.telemechanic.helper.DriverLicenseHelper.validateSeries;

@Service
@RequiredArgsConstructor
@Slf4j
public class DriverServiceImpl implements DriverService {
    
    private final DriverRepository driverRepository;
    private final DrivingLicenseService drivingLicenseService;
    private final EwbTariffService ewbTariffService;
    private final EmployeeService employeeService;
    private final DriverMapper driverMapper;
    private final DrivingLicenseMapper drivingLicenseMapper;
    private final TransportService transportService;
    private final TransportValidationService transportValidationService;
    private static final String LIKE_FORMAT = "%%%s%%";
    
    @Override
    @Transactional
    public void addDriver(AddDriverRequest addDriverRequest) {
        validateDriver(addDriverRequest);
        var optionalDriver = driverRepository.findWithLicenseByEmployeeId(addDriverRequest.driver().employeeId());
        if (optionalDriver.isPresent()) {
            var driver = optionalDriver.get();
            var drivingLicense = driver.getDrivingLicense();
            drivingLicense.setActive(false);
            drivingLicenseService.saveOrUpdate(drivingLicense);
            var newDrivingLicense = drivingLicenseMapper.drivingLicenseInfoToDrivingLicense(addDriverRequest.drivingLicense());
            newDrivingLicense.setPreviousId(drivingLicense.getId());
            
            driverRepository.save(driver.setDrivingLicense(newDrivingLicense));
        } else {
            driverRepository.save(driverMapper.addDriverRequestToDriver(addDriverRequest));
        }
    }
    
    @Override
    @Transactional
    public void editDriver(UUID id, EditDriverRequest editDriverRequest, UUID userId) {
        var driver = driverRepository.findById(id)
                                     .orElseThrow(() -> new DriverNotFoundException(id));
        var initiator = employeeService.get(userId).orElseThrow(() -> new UserNotFoundException(userId));
        if (initiator.getOrganization().getId() != driver.getEmployee().getOrganization().getId()) {
            throw new NotSameOrganizationException(initiator.getOrganization().getId(), driver.getEmployee().getOrganization().getId());
        }
        
        if (editDriverRequest.snils() != null) {
            checkSnils(editDriverRequest.snils());
            driver.setSnils(editDriverRequest.snils());
        }
        
        if (editDriverRequest.tin() != null) {
            driver.setTin(editDriverRequest.tin());
        }
        
        if (editDriverRequest.drivingLicense() != null) {
            validateSeries(editDriverRequest.drivingLicense().series());
            validateNumber(editDriverRequest.drivingLicense().number());
            
            if (driverRepository.existsByEmployeeIdAndDrivingLicenseSeriesAndDrivingLicenseNumberAndDrivingLicenseActiveIsTrue(
                    driver.getEmployee().getId(),
                    editDriverRequest.drivingLicense().series(),
                    editDriverRequest.drivingLicense().number())) {
                throw new DrivingLicenseAlreadyExistsException();
            }
            driver.getDrivingLicense()
                  .setSeries(editDriverRequest.drivingLicense().series())
                  .setNumber(editDriverRequest.drivingLicense().number())
                  .setIssueDate(editDriverRequest.drivingLicense().issueDate())
                  .setExpiryDate(editDriverRequest.drivingLicense().expiryDate())
                  .setCategories(editDriverRequest.drivingLicense().categoryIds().stream()
                                                  .map(el -> new Category().setId(el))
                                                  .collect(Collectors.toSet()));
        }
        driverRepository.save(driver);
    }
    
    @Override
    @Transactional
    public void deactivateDriver(UUID id) {
        var driver = driverRepository.findById(id)
                                     .orElseThrow(() -> new DriverNotFoundException(id));
        if (!driver.getDrivingLicense().isActive()) {
            throw new DrivingLicenseNotActiveException(driver.getId());
        }
        driver.getDrivingLicense().setActive(false);
        driverRepository.save(driver);
    }
    
    @Override
    public GetDriverResponse getDriverById(UUID id) {
        return driverRepository.findById(id)
                               .map(driverMapper::driverToGetDriverResponse)
                               .orElseThrow(() -> new DriverNotFoundException(id));
    }
    
    @Override
    public Page<DriverByFioResponse> getDriversByFio(DriverByFioRequest request, UUID userId) {
        var transport = transportService.getTransportByIdWithOrganizations(request.transportId());
        
        if (transport.getStatus() != TransportStatus.IN_USE) {
            throw new TransportNotInUse(String.format("Транспортное средство %s выведено из эксплуатации", transport.getId()));
        }
        
        var dispatcherOrganizationId = employeeService.getByUserId(userId).getOrganization().getId();
        var organizationIds = transport.getOrganizations().stream().map(Organization::getId).collect(Collectors.toSet());
        transportValidationService.validateTransportOrgsContainsDispatcherOrg(organizationIds, dispatcherOrganizationId, transport.getId());
        
        return driverRepository.findByFio(dispatcherOrganizationId, request.searchText(), transport.getId(), request.preparePageRequest())
                               .map(driverMapper::driverByFioToDriverByFioResponse);
    }
    
    @Override
    public Page<DriverSearchResponse> search(DriverSearchRequest searchDriverRequest) {
        var pageRequest = searchDriverRequest.preparePageRequest();
        var result = driverRepository.search(searchDriverRequest, pageRequest);
        return new PageImpl<>(
                result.getContent().stream()
                      .map(driverMapper::driverProjectionToDriverSearchResponseDto)
                      .toList(),
                result.getPageable(),
                result.getTotalElements()
        );
    }
    
    @Transactional
    @Override
    public void deactivateDrivers() {
        driverRepository.setActiveFalseWhereExpiryDateBeforeNow();
    }
    
    @Override
    @Transactional
    public Driver getActiveDriverById(UUID id) {
        var driver = driverRepository.findById(id)
                                     .orElseThrow(() -> new DriverNotFoundException(id));
        if (!driver.getDrivingLicense().isActive()) {
            throw new DrivingLicenseNotActiveException(driver.getDrivingLicense().getId());
        }
        return driver;
    }
    
    @Override
    public Driver getByEmployeeId(UUID employeeId) {
        return driverRepository.findWithEmployeeAndLicenseByEmployeeIdAndDrivingLicense_ActiveTrue(employeeId)
                               .orElseThrow(() -> new DriverNotFoundException(employeeId, DRIVER_BY_USER_ID_MSG_FORMAT));
    }
    
    @Override
    @Transactional
    public void saveMessage(DriverMessage driverMessage) {
        var driver = driverRepository.findWithLicenseByEmployeeId(driverMessage.oauthId())
                                     .orElseGet(() -> {
                                         var newDriver = new Driver();
                                         employeeService.get(driverMessage.oauthId()).ifPresent(newDriver::setEmployee);
                                         newDriver.setDrivingLicense(new DrivingLicense());
                                         return newDriver;
                                     });
        driver.setAutoparkId(driverMessage.autoparkId());
        driver.setContractorId(driverMessage.contractorId());
        driver.setTin(driverMessage.tin());
        driver.setSnils(driverMessage.snils());
        var driverLicense = driver.getDrivingLicense();
        driverLicense.setActive(driverMessage.active());
        driverLicense.setExpiryDate(driverMessage.expiryDate());
        driverLicense.setIssueDate(driverMessage.issueDate());
        if (StringUtils.hasText(driverMessage.driverLicenseNumber())) {
            var trimmedDriveLicenseNumber = driverMessage.driverLicenseNumber();
            int firstSpacePos = trimmedDriveLicenseNumber.indexOf(' ');
            if (firstSpacePos == 6) { // 321211 02 85
                driverLicense.setNumber(trimmedDriveLicenseNumber.substring(0, 6));
                driverLicense.setSeries(trimmedDriveLicenseNumber.substring(7));
            } else if (firstSpacePos == 2) { // 34 73 473473
                driverLicense.setNumber(trimmedDriveLicenseNumber.substring(6));
                driverLicense.setSeries(trimmedDriveLicenseNumber.substring(0, 5));
            }
        }
        driverRepository.save(driver);
    }
    
    @Override
    public Page<DriverByFioResponse> getDrivers(DriverFilters filters) {
        var transport = transportService.get(filters.getTransportId())
                                        .orElseThrow(() -> new EntityNotFoundException(Transport.class, filters.getTransportId()));
        if (transport.getStatus() != TransportStatus.IN_USE) {
            throw new TransportNotInUse(String.format("Транспортное средство %s выведено из эксплуатации", transport.getId()));
        }
        if (transport.getContractorId() == null || transport.getAutoparkId() == null) {
            throw new ConflictException("Транспортное средство должно принадлежать внутреннему автопарку");
        }
        var pageable = PageRequest.of(filters.getPage(), filters.getSize());
        return driverRepository.findAll(createSpec(transport.getContractorId(), transport.getAutoparkId(), filters.getSearchText()), pageable)
                               .map(driverMapper::driverToDriverByFioResponse);
        
    }
    
    private Specification<Driver> createSpec(UUID contractorId, UUID autoparkId, String fio) {
        return (root, q, cb) -> {
            var employee = root.join(Driver_.EMPLOYEE);
            var predicate = cb.and(cb.equal(root.get(Driver_.CONTRACTOR_ID), contractorId),
                                   cb.or(
                                           cb.equal(root.get(Driver_.AUTOPARK_ID), autoparkId),
                                           cb.isNull(root.get(Driver_.AUTOPARK_ID))
                                        ));
            if (fio != null) {
                predicate = cb.and(predicate, cb.like(cb.lower(employee.get(Employee_.FULL_NAME_INDEX)),
                                                      LIKE_FORMAT.formatted(fio.toLowerCase(Locale.ROOT))));
            }
            return predicate;
        };
    }
    
    private void validateDriver(AddDriverRequest addDriverRequest) {
        checkDepartment(addDriverRequest.driver().employeeId());
        checkSnils(addDriverRequest.driver().snils());
        checkDriver(addDriverRequest);
    }
    
    private void checkDepartment(UUID employeeId) {
        var employee = employeeService.get(employeeId)
                                      .orElseThrow(() -> new UserNotFoundException(employeeId));
        var department = employee.getDepartment();
        if (!department.isActive()) {
            throw new DepartmentNotActiveException(department.getId());
        }
        var organizationId = department.getOrganization().getId();
        if (!ewbTariffService.existsActiveTariffByOrganizationId(organizationId)) {
            throw new FleetOwnerOrganizationException(FleetOwnerOrganizationException.NOT_FLEET_OWNER_MSG.formatted(organizationId));
        }
    }
    
    private void checkSnils(String snils) {
        if (!SnilsHelper.isValidSnils(snils)) {
            throw new SnilsNotFormattedException(snils);
        }
    }
    
    private void checkDriver(AddDriverRequest addDriverRequest) {
        validateSeries(addDriverRequest.drivingLicense().series());
        validateNumber(addDriverRequest.drivingLicense().number());
        
        if (driverRepository.existsByEmployeeIdAndDrivingLicenseSeriesAndDrivingLicenseNumberAndDrivingLicenseActiveIsTrue(
                addDriverRequest.driver().employeeId(),
                addDriverRequest.drivingLicense().series(),
                addDriverRequest.drivingLicense().number()
                                                                                                                          )) {
            throw new DrivingLicenseAlreadyExistsException();
        }
        if (driverRepository.existsByDrivingLicenseSeriesAndDrivingLicenseNumberAndDrivingLicenseActiveIsTrueAndEmployeeIdNot(
                addDriverRequest.drivingLicense().series(),
                addDriverRequest.drivingLicense().number(),
                addDriverRequest.driver().employeeId()
                                                                                                                             )) {
            throw new DriverAlreadyExistsException(SERIES_NUMBER_EXISTS.formatted(
                    addDriverRequest.drivingLicense().series(),
                    addDriverRequest.drivingLicense().number()
                                                                                 )
            );
        }
        if (driverRepository.existsByTinAndSnilsAndActiveEmployeeNot(
                addDriverRequest.driver().tin(),
                addDriverRequest.driver().snils(),
                addDriverRequest.driver().employeeId()
                                                                )) {
            throw new DriverAlreadyExistsException(
                    TIN_SNILS_EXISTS.formatted(
                            addDriverRequest.driver().tin(),
                            addDriverRequest.driver().snils()
                                              )
            );
        }
    }
}
