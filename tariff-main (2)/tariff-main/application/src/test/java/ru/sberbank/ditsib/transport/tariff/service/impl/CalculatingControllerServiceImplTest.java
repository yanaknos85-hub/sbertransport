package ru.sberbank.ditsib.transport.tariff.service.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.tariff.model.TripDto;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sberbank.ditsib.transport.tariff.database.dao.ContractRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.TariffRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Department;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;
import ru.sberbank.ditsib.transport.tariff.dto.RegionDto;
import ru.sberbank.ditsib.transport.tariff.service.CalculateService;

import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.junit.jupiter.api.Assertions.*;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@SpringBootTest
@EmbeddedPostgres
@Transactional
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка подбора доступных тарифов")
@ActiveProfiles({"test", "kafka"})
class CalculatingControllerServiceImplTest extends KafkaTest {
    
    @Autowired
    CalculateService service;
    
    @Autowired
    OrganizationRepository organizationRepository;
    
    @Autowired
    DepartmentRepository departmentRepository;
    
    @Autowired
    TariffRepository<TaxiTariff> tariffRepository;
    
    @Autowired
    ContractRepository contractRepository;
    
    @Test
    @DisplayName("Есть тариф с пустым department и с department1. Запрос на подбор тарифов от сотрудника с department2")
    void calculate_employee_with_another_department_test() {
        var organization = Instancio.of(Organization.class)
                .set(field(Organization::getDigitId), 1234L)
                .create();
        organization = organizationRepository.save(organization);
        
        var department1 = departmentRepository.save(Department.builder()
                                    .id(UUID.randomUUID())
                                    .organizationId(organization.getId())
                                    .build());
        
        var department2 = departmentRepository.save(Department.builder()
                                    .id(UUID.randomUUID())
                                    .organizationId(organization.getId())
                                    .build());
        
        Employee employee = Employee.builder()
                                    .id(UUID.randomUUID())
                                    .organizationId(organization.getId())
                                    .departmentId(department2.getId())
                                    .build();
        
        TripDto tripData = TripDto.builder()
                                  .organizationId(organization.getId())
                                  .employeeId(employee.getId())
                                  .distance(10.0)
                                  .time(Duration.of(10, ChronoUnit.MINUTES))
                                  .build();
        
        var regionId = UUID.randomUUID();
        List<RegionDto> regionBranch = new ArrayList<>();
        regionBranch.add(RegionDto.builder().id(regionId).build());
        
        Contract contract = Contract.builder()
                                    .sum(1L)
                                    .startDate(LocalDate.now())
                                    .creationTime(LocalDateTime.now())
                                    .contractNumber("contract number")
                                    .contractorId(UUID.randomUUID())
                                    .transportType(TransportTypeEnum.TAXI)
                                    .userId(UUID.randomUUID())
                                    .contractType(ContractType.TRANSITIONAL)
                                    .restrictionType(RestrictionType.NONE)
                                    .build();
        contract = contractRepository.save(contract);
        
        TaxiTariff tariffForDepartment1 = TaxiTariff.builder()
                                                    .humanReadableId(UUID.randomUUID().toString())
                                                    .organization(organization)
                                                    .department(department1)
                                                    .transportType(TransportTypeEnum.TAXI)
                                                    .rideCostPerKm(1)
                                                    .rideCostPerMin(1)
                                                    .taxiClass(TaxiClass.ECONOMY)
                                                    .waitCostPerMin(1)
                                                    .regionId(regionId)
                                                    .contract(contract)
                                                    .build();
        tariffRepository.save(tariffForDepartment1);
        
        TaxiTariff tariff = TaxiTariff.builder()
                                      .humanReadableId(UUID.randomUUID().toString())
                                      .organization(organization)
                                      .department(null)
                                      .transportType(TransportTypeEnum.TAXI)
                                      .rideCostPerKm(1)
                                      .rideCostPerMin(1)
                                      .taxiClass(TaxiClass.ECONOMY)
                                      .waitCostPerMin(1)
                                      .regionId(regionId)
                                      .contract(contract)
                                      .build();
        tariffRepository.save(tariff);
        
        var result = service.findAllTariffs(tripData, employee, regionBranch, false);
        
        assertEquals(1, result.size(), "В результат подбора тарифов не должны попадать тарифы с чужим подразделением");
        
        System.out.println(result.size());
    }
}