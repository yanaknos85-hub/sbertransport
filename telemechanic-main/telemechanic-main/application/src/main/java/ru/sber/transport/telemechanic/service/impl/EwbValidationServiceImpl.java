package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.telemechanic.database.dao.EwbRepository;
import ru.sber.transport.telemechanic.database.model.Driver;
import ru.sber.transport.telemechanic.database.model.Organization;
import ru.sber.transport.telemechanic.dto.ewb.first_title.FirstTitleRequest;
import ru.sber.transport.telemechanic.exception.*;
import ru.sber.transport.telemechanic.service.DriverService;
import ru.sber.transport.telemechanic.service.EwbValidationService;


import java.time.LocalDate;
import java.util.UUID;

import static ru.sber.transport.telemechanic.service.impl.EwbServiceImpl.DATE_FORMAT;

@Service
@RequiredArgsConstructor
public class EwbValidationServiceImpl implements EwbValidationService {
    
    private final EwbRepository ewbRepository;
    private final DriverService driverService;
    
    @Override
    public void validateEwb(FirstTitleRequest request, Driver driver) {
        if (ewbRepository.existsByDriverIdAndStartDateAndStatusIn(driver.getId(), request.startDate(), IN_PROGRESS_STATUSES)) {
            throw new EwbNotClosedException(driver.getEmployee().getPersonnelNumber(), request.startDate().format(DATE_FORMAT));
        }
    }
    
    @Override
    public void validateEwbQrCode(UUID ewbId, UUID userId) {
        var ewb = ewbRepository.findById(ewbId).orElseThrow(() -> new EwbNotFoundException(ewbId));
        if (!ewb.getDriver().getEmployee().getId().equals(userId)) {
            throw new BadRequestException("Только водитель ЭПЛ может получить QR-код");
        }
        if (!ewb.isQrCode()) {
            throw new BadRequestException("Невозможно получить QR-код! Титул 4 не сформирован.");
        }
    }
    
    @Override
    public void validateDriver(UUID driverId, LocalDate ewbStartDate, LocalDate ewbEndDate) {
        ewbRepository.findByDriverIdAndNewEwbDatesBetweenEwbDatesAndActiveStatuses(
                             driverId,
                             ewbStartDate,
                             ewbEndDate,
                             IN_PROGRESS_STATUSES)
                     .ifPresent(ewb -> {
                         var driver = driverService.getActiveDriverById(driverId);
                         throw new EwbNotClosedException(driver.getEmployee().getPersonnelNumber(), ewbStartDate.format(DATE_FORMAT));
                     });
    }
    
    @Override
    public void validateDriverForSecondTitle(Driver driver, Organization organization) {
            if (!organization.getId().equals(driver.getEmployee().getOrganization().getId())) {
                throw new DriverDoesNotAssociatedWithOrganizationException("Водитель не принадлежит организации: %s".formatted(organization.getId()));
            }
    }
    
}
