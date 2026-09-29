package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TaxiExternalIntegrationType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.exceptions.IllegalCallerResponseException;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.RequestForTaxi;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.GeoDriverDTO;
import ru.sberbank.ditsib.transport.request.exceptions.UserNotFoundException;
import ru.sberbank.ditsib.transport.request.service.ContractorService;
import ru.sberbank.ditsib.transport.request.service.GeoDriverResolver;
import ru.sberbank.ditsib.transport.request.service.GeoService;
import ru.sberbank.ditsib.transport.request.service.RequestValidationService;
import ru.sberbank.ditsib.transport.request.service.corp.EmployeeService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Service
@Transactional
public class GeoServiceImpl implements GeoService {
    
    private final EmployeeService employeeService;
    private final RequestValidationService requestValidationService;
    private final ContractorService contractorService;
    private final List<GeoDriverResolver> geoDriverResolvers;
    
    @Override
    public GeoDriverDTO getGeoDriverByRequestId(UUID requestId, UUID userId) {
        
        log.debug("getGeoDriverByRequestId: requestId = {}, userId = {}", requestId, userId);
        
        var authenticatedEmployee = getEmployee(userId);
        var request = (RequestForTaxi) requestValidationService.validateAndGetRequest(requestId, TransportTypeEnum.TAXI);
        
        checkAuthorization(request, authenticatedEmployee);
        log.debug("getGeoDriverByRequestId: checkAuthorization() success");
        
        var integrationType = getIntegrationType(request);
        log.debug("getGeoDriverByRequestId: integrationType = {}", integrationType);
        
        return geoDriverResolvers.stream()
                                 .filter(r -> r.getIntegrationType() != null && r.getIntegrationType().equals(integrationType))
                                 .findFirst()
                                 .orElse(getGeoDriverResolverDefault())
                                 .getGeoDriverByRequest(request);
    }
    
    private GeoDriverResolver getGeoDriverResolverDefault() {
        return geoDriverResolvers.stream()
                .filter(r -> r.getIntegrationType() == null)
                .findFirst()
                .orElseThrow();
    }
    
    private void checkAuthorization(Request request, Employee authenticatedEmployee) {
        log.debug("checkAuthorization: request = {}, authenticatedEmployee = {}", request, authenticatedEmployee);
        if (request == null || authenticatedEmployee == null) {
            log.debug("checkAuthorization: not enough info, wrong request's or user's data");
            throw new IllegalCallerResponseException();
        }
        if (!(authenticatedEmployee.getId() != null && request.getAuthor() != null && authenticatedEmployee.getId().equals(request.getAuthor().getId()) ||
              authenticatedEmployee.getId() != null && request.getPassenger() != null && authenticatedEmployee.getId().equals(request.getPassenger().getId()))) {
            throw new IllegalCallerResponseException();
        }
    }
    
    private Employee getEmployee(UUID userId) {
        Objects.requireNonNull(userId, "UserId is null");
        return employeeService.getByUserId(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }
    
    private TaxiExternalIntegrationType getIntegrationType(RequestForTaxi request) {
        return Optional.ofNullable(request)
                       .map(RequestForTaxi::getContractorId)
                       .flatMap(contractorService::getOptional)
                       .map(Contractor::getIntegrationType)
                       .orElse(TaxiExternalIntegrationType.EMAIL_XML_API);
    }
}
