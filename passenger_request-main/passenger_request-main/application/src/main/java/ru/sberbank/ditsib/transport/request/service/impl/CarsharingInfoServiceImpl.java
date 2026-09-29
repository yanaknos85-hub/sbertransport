package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.request.database.dao.CarsharingInfoRepository;
import ru.sberbank.ditsib.transport.request.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingInfo;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.carsharing.CarsharingInfoRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.carsharing.CarsharingInfoResponseDTO;
import ru.sberbank.ditsib.transport.request.dto.mapper.CarsharingDTOMapper;
import ru.sberbank.ditsib.transport.request.service.CarsharingInfoService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CarsharingInfoServiceImpl implements CarsharingInfoService {
    
    private final CarsharingInfoRepository repository;
    private final EmployeeRepository employeeRepository;
    private final CarsharingDTOMapper carsharingMapper;
    
    @Value("${carsharing.belkaCar.deepLink:https://belkacar.ru/deeplink}")
    private String carsharingDeepLink;
    
    @Value("${carsharing.sberfriendDeepLink:https://sberfriend.sbrf.ru/sberfriend/#/interaction/new?elementId=12}")
    private String sberfriendDeepLink;
    
    @Override
    public CarsharingInfoResponseDTO get() {
        var carsharingInfo = createOrGet();
        var deepLink = getDeepLink(carsharingInfo);
        return carsharingMapper.getCarsharingResponseDTO(carsharingInfo, deepLink);
    }
    
    @Override
    public CarsharingInfoResponseDTO update(CarsharingInfoRequestDTO dto) {
        var carsharingInfo = createOrGet();
        carsharingInfo.setConsent(dto.isConsent());
        carsharingInfo.setPreviouslyUsed(dto.isPreviouslyUsed());
        carsharingInfo = repository.save(carsharingInfo);
        var deepLink = getDeepLink(carsharingInfo);
        return carsharingMapper.getCarsharingResponseDTO(carsharingInfo, deepLink);
    }
    
    private CarsharingInfo createOrGet() {
        var employeeId = ControllerUtils.currentUser();
        var employee = employeeRepository.findByUserId(employeeId).orElseThrow(() -> new EntityNotFoundException(Employee.class, employeeId));
        return repository.getByEmployee(employee).orElseGet(() -> {
            var newCarsharingInfo = CarsharingInfo
                    .builder()
                    .employee(employee)
                    .build();
            return repository.save(newCarsharingInfo);
        });
    }
    
    @Override
    public @NonNull
    String getDeeplinkByRequestId(@NonNull UUID requestId) {
        return carsharingDeepLink;
    }
    
    private String getDeepLink(CarsharingInfo carsharingInfo) {
        return carsharingInfo.isPreviouslyUsed() ? carsharingDeepLink : sberfriendDeepLink;
    }
    
}

