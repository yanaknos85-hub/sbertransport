package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.PersonalCarOwnerInfo;
import ru.sberbank.ditsib.transport.messaging.messages.PersonalCarMessage;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.PersonalCarListener;
import ru.sberbank.ditsib.transport.reports.model.Employee;
import ru.sberbank.ditsib.transport.reports.model.PersonalCar;
import ru.sberbank.ditsib.transport.reports.service.EmployeeService;
import ru.sberbank.ditsib.transport.reports.service.PersonalCarService;

import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component("personalCarsInput")
@Slf4j
public class PersonalCarListenerImpl implements PersonalCarListener   {
    
    private final PersonalCarService personalCarService;
    private final EmployeeService employeeService;
    
    @Transactional
    @Override
    public void handlePersonalCar(PersonalCarMessage message) {
        if (!message.isDeleted()) {
            log.info("Got new personal car information for   {}", message.getEmployeeId());
            Employee employee = employeeService.findOrCreateEmployeeById(message.getEmployeeId());
            PersonalCar personalCar =
                    personalCarService.findById(message.getId()).orElse(PersonalCar.builder().id(message.getId()).build());
            personalCar = personalCar.toBuilder()
                                     .brandName(message.getBrandName())
                                     .engineVolume(message.getEngineVolume())
                                     .insuranceNumber(message.getInsuranceNumber())
                                     .model(message.getModel())
                                     .ownerInfo(message.getOwnerInfo() == null ? null :
                                                PersonalCarOwnerInfo.valueOf(message.getOwnerInfo()))
                                     .registrationCertificate(message.getRegistrationCertificate())
                                     .registrationNumber(message.getRegistrationNumber())
                                     .employee(employee)
                                     .build();
            personalCarService.save(personalCar);
        }
    }
}
