package ru.sberbank.ditsib.transport.request.messaging.listeners;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.ItinerantType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.messaging.message.ContractMessage;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.CarsharingContractRepository;
import ru.sberbank.ditsib.transport.request.database.dao.CorporateCarsharingRepository;
import ru.sberbank.ditsib.transport.request.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingContract;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CorporateCarsharing;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.service.corp.DepartmentService;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@EmbeddedPostgres
@MockitoBean(types = JwtDecoder.class)
@SpringBootTest(classes = RequestApplication.class)
@DisplayName("Тест слушателя Контрактов и автоматической генерации сущностей Корп.Каршеринга")
@Transactional
class CarsharingContractListenerTest extends KafkaTest {
    
    @Autowired
    private CarsharingContractRepository contractRepository;
    @Autowired
    private CorporateCarsharingRepository carsharingRepository;
    @Autowired
    private EmployeeRepository employeeRepository;
    @Autowired
    @Qualifier("contractInput")
    private Consumer<Message<ContractMessage>> contractInput;
    @Autowired
    private DepartmentService departmentService;
    
    private final UUID ORG1_ID = UUID.fromString("2c2eb25b-b2d0-428a-bf5e-aaaaaaaaaaa1");
    private final UUID ORG2_ID = UUID.fromString("cc2eb25c-b2d1-438b-bf2e-aaaaaaaaaaa2");
    private final UUID ORG3_ID = UUID.fromString("cc2eb25d-b2d2-448c-bf1e-aaaaaaaaaaa3");
    private final UUID ORG4_ID = UUID.fromString("cc2eb25e-b2d3-458d-bf2e-aaaaaaaaaaa4");
    private final UUID ORG5_ID = UUID.fromString("cc2eb25f-b2d4-429e-bf3e-aaaaaaaaaaa5");
    private final String REGION1 = "Region1";
    private final String REGION2 = "Region2";
    private final String REGION3 = "Region3";
    private final UUID CONTRACT1_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-bbbbbbbbbbb1");
    private final UUID CONTRACT2_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-bbbbbbbbbbb2");
    private final UUID CONTRACTOR1_ID = UUID.fromString("cc2eb26b-b2d1-438b-bf4e-ccccccccccc1");
    private final UUID CONTRACTOR2_ID = UUID.fromString("cc2eb27b-b2d2-478b-bf5e-ccccccccccc2");
    private final UUID CONTRACTOR3_ID = UUID.fromString("cc2eb17b-b2d4-479b-bf6e-ccccccccccc3");
    
    @AfterEach
    void clear() {
        carsharingRepository.deleteAll();
        contractRepository.deleteAll();
        employeeRepository.deleteAll();
    }
    
    @Test
    @DisplayName("Тест записи Контракта и Корп.Каршерингов - успех")
    void handleCreateTest() {
        ContractMessage message = getNewContractMessage(CONTRACT1_ID, REGION1, CONTRACTOR1_ID,
                                                        Set.of(ORG1_ID, ORG2_ID), true, false);
        contractInput.accept(MessageBuilder.withPayload(message).build());
        
        assertThat(contractRepository.count()).isEqualTo(1);
        CarsharingContract contract = contractRepository.findAll().get(0);
        assertThat(contract.getId()).isEqualTo(message.getId());
        assertThat(contract.getContractorId()).isEqualTo(message.getContractorId());
        assertThat(contract.getOrganizations().size()).isEqualTo(message.getOrganizations().size());
        assertThat(contract.getTransportType().name()).isEqualTo(message.getTransportType());
        assertThat(contract.getRegion()).isEqualTo(message.getRegion());
        assertThat(contract.getServiceType().name()).isEqualTo(message.getServiceType());
        assertThat(contract.isActive()).isEqualTo(message.isActive());
        assertThat(contract.isDeleted()).isEqualTo(message.isDeleted());
        
        assertThat(carsharingRepository.count()).isEqualTo(message.getOrganizations().size());
        assertThat(carsharingRepository.findAll().stream()
                                       .map(CorporateCarsharing::getOrganizationId)
                                       .collect(Collectors.toSet()).containsAll(message.getOrganizations())).isTrue();
    }
    
    @Test
    @DisplayName("Тест редактирования Контракта и Корп.Каршерингов - успех")
    void handleUpdateTest() {
        // наполним БД
        ContractMessage message1 = getNewContractMessage(CONTRACT1_ID, REGION1, CONTRACTOR1_ID,
                                                         Set.of(ORG1_ID, ORG2_ID), true, false);
        ContractMessage message2 = getNewContractMessage(CONTRACT2_ID, REGION1, CONTRACTOR2_ID,
                                                         Set.of(ORG1_ID, ORG2_ID, ORG5_ID), true, false);
        contractInput.accept(MessageBuilder.withPayload(message1).build());
        contractInput.accept(MessageBuilder.withPayload(message2).build());
        
        assertThat(contractRepository.count()).isEqualTo(2);
        assertThat(carsharingRepository.count()).isEqualTo(message1.getOrganizations().size() +
                                                           message2.getOrganizations().size());
        assertThat(carsharingRepository.findAll().stream()
                                       .map(CorporateCarsharing::getOrganizationId)
                                       .collect(Collectors.toSet()).containsAll(message1.getOrganizations())).isTrue();
        assertThat(carsharingRepository.findAll().stream()
                                       .map(CorporateCarsharing::getOrganizationId)
                                       .collect(Collectors.toSet()).containsAll(message2.getOrganizations())).isTrue();
        Department department = departmentService.findOrCreateById(UUID.randomUUID());
        // добавим сотрудника в список подключенных, чтобы при изменении каршеринг был только деактивирован
        Employee employee = employeeRepository.save(Employee.builder()
                                                            .id(UUID.randomUUID())
                                                            .humanReadableId("EM-0001")
                                                            .firstName("Name")
                                                            .lastName("Surname")
                                                            .userId(UUID.randomUUID())
                                                            .personnelNumber("1")
                                                            .department(department)
                                                            .positionId(UUID.randomUUID())
                                                            .itinerantType(ItinerantType.FULL)
                                                            .marriageCertificateNumber("sdf3454")
                                                            .costCenter("Что это такое?")
                                                            .mobilePhone("322-223-33-22")
                                                            .build());
        CorporateCarsharing carsharing = getCarsharingFromDb(CONTRACTOR1_ID, ORG2_ID, REGION1);
        carsharing.getJoinedEmployees().add(employee);
        carsharingRepository.save(carsharing);
        
        // Редактируем первый контракт. Каршеринг ORG1 д.б. удален и записан заново, каршеринг ORG2 д.б. деактивирован.
        // Должны добавится каршеринги для ORG3, ORG4
        message1 = getNewContractMessage(CONTRACT1_ID, REGION2, CONTRACTOR1_ID,
                                         Set.of(ORG1_ID, ORG3_ID, ORG4_ID), true, false);
        contractInput.accept(MessageBuilder.withPayload(message1).build());
        
        assertThat(contractRepository.count()).isEqualTo(2);
        assertThat(carsharingRepository.count()).isEqualTo(7);
        assertThat(carsharingRepository.findByActive(false).size()).isEqualTo(1);
        checkCarsharing(true, CONTRACTOR1_ID, ORG1_ID, REGION2);    //перезаписанный
        checkCarsharing(false, CONTRACTOR1_ID, ORG2_ID, REGION2);    //деактивированный, контракт перезаписывает регион
        checkCarsharing(true, CONTRACTOR2_ID, ORG1_ID, REGION1);
        checkCarsharing(true, CONTRACTOR2_ID, ORG2_ID, REGION1);
        checkCarsharing(true, CONTRACTOR2_ID, ORG5_ID, REGION1);
        checkCarsharing(true, CONTRACTOR1_ID, ORG3_ID, REGION2);    //две строки добавлены крайним сообщением
        checkCarsharing(true, CONTRACTOR1_ID, ORG4_ID, REGION2);
        //проверим наличие сотрудника у деактивированного шеринга
        carsharing = getCarsharingFromDb(CONTRACTOR1_ID, ORG2_ID, REGION2);
        assertThat(carsharing.getJoinedEmployees().size()).isEqualTo(1);
        
        // еще раз добавим сотрудника в спиок подключенных другого каршеринга
        carsharing = getCarsharingFromDb(CONTRACTOR2_ID, ORG5_ID, REGION1);
        carsharing.getJoinedEmployees().add(employee);
        carsharingRepository.save(carsharing);
        
        // Редактируем второй контракт. Каршеринг ORG1 д.б. удален, ORG2 перезаписан, ORG5 д.б. деактивирован и
        // перезаписан активированным
        message2 = getNewContractMessage(CONTRACT2_ID, REGION3, CONTRACTOR3_ID,
                                         Set.of(ORG2_ID, ORG5_ID), true, false);
        contractInput.accept(MessageBuilder.withPayload(message2).build());
        
        assertThat(contractRepository.count()).isEqualTo(2);
        assertThat(carsharingRepository.count()).isEqualTo(6);
        assertThat(carsharingRepository.findByActive(false).size()).isEqualTo(1);
        checkCarsharing(true, CONTRACTOR1_ID, ORG1_ID, REGION2);
        checkCarsharing(false, CONTRACTOR1_ID, ORG2_ID, REGION2);
        checkCarsharing(true, CONTRACTOR3_ID, ORG2_ID, REGION3);   //перезаписан
        checkCarsharing(true, CONTRACTOR3_ID, ORG5_ID, REGION3);   //деактивирован и снова активирован
        checkCarsharing(true, CONTRACTOR1_ID, ORG3_ID, REGION2);
        checkCarsharing(true, CONTRACTOR1_ID, ORG4_ID, REGION2);
        //проверим наличие сотрудника у деактивированного шеринга
        carsharing = getCarsharingFromDb(CONTRACTOR3_ID, ORG5_ID, REGION3);
        assertThat(carsharing.getJoinedEmployees().size()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Тест деактивации Контракта и удаления / деактивации Корп.Каршерингов - успех")
    void handleDeleteDeactivateTest() {
        // наполним БД
        ContractMessage message1 = getNewContractMessage(CONTRACT1_ID, REGION1, CONTRACTOR1_ID,
                                                         Set.of(ORG1_ID, ORG2_ID), true, false);
        ContractMessage message2 = getNewContractMessage(CONTRACT2_ID, REGION1, CONTRACTOR2_ID,
                                                         Set.of(ORG1_ID, ORG2_ID, ORG5_ID), true, false);
        contractInput.accept(MessageBuilder.withPayload(message1).build());
        contractInput.accept(MessageBuilder.withPayload(message2).build());
        assertThat(contractRepository.count()).isEqualTo(2);
        
        Department department = departmentService.findOrCreateById(UUID.randomUUID());
        // добавим сотрудника в спиок подключенных, чтобы при изменении каршеринг был только деактивирован
        Employee employee = employeeRepository.save(Employee.builder()
                                                            .id(UUID.randomUUID())
                                                            .humanReadableId("EM-0001")
                                                            .firstName("Name")
                                                            .lastName("Surname")
                                                            .userId(UUID.randomUUID())
                                                            .personnelNumber("1")
                                                            .department(department)
                                                            .positionId(UUID.randomUUID())
                                                            .itinerantType(ItinerantType.FULL)
                                                            .marriageCertificateNumber("sdf3454")
                                                            .costCenter("Что это такое?")
                                                            .mobilePhone("322-223-33-22")
                                                            .build());
        CorporateCarsharing carsharing = getCarsharingFromDb(CONTRACTOR1_ID, ORG1_ID, REGION1);
        carsharing.getJoinedEmployees().add(employee);
        carsharingRepository.save(carsharing);
        carsharing = getCarsharingFromDb(CONTRACTOR2_ID, ORG1_ID, REGION1);
        carsharing.getJoinedEmployees().add(employee);
        carsharingRepository.save(carsharing);
        
        // отправим сообщения на деактивацию контракта 1 и удаление контракта 2
        message1 = getNewContractMessage(CONTRACT1_ID, REGION1, CONTRACTOR1_ID,
                                         Set.of(ORG1_ID, ORG2_ID), false, false);
        message2 = getNewContractMessage(CONTRACT2_ID, REGION1, CONTRACTOR2_ID,
                                         Set.of(ORG1_ID, ORG2_ID, ORG5_ID), true, true);
        contractInput.accept(MessageBuilder.withPayload(message1).build());
        contractInput.accept(MessageBuilder.withPayload(message2).build());
        assertThat(contractRepository.count()).isEqualTo(2);
        
        // убедимся, что осталось только два неактивных каршеринга, где есть подключенные сотрудники
        assertThat(carsharingRepository.count()).isEqualTo(2);
        assertThat(carsharingRepository.findByActive(false).size()).isEqualTo(2);
        checkCarsharing(false, CONTRACTOR1_ID, ORG1_ID, REGION1);
        checkCarsharing(false, CONTRACTOR2_ID, ORG1_ID, REGION1);
        
        //проверим наличие сотрудника у деактивированных шерингов
        carsharing = getCarsharingFromDb(CONTRACTOR1_ID, ORG1_ID, REGION1);
        assertThat(carsharing.getJoinedEmployees().size()).isEqualTo(1);
        carsharing = getCarsharingFromDb(CONTRACTOR2_ID, ORG1_ID, REGION1);
        assertThat(carsharing.getJoinedEmployees().size()).isEqualTo(1);
    }
    
    @Test
    @DisplayName("Тест игнора сообщения с другими типами транспорта")
    void handleOtherTransportTypeMessageIgnoreTest() {
        ContractMessage message = getNewContractMessage(CONTRACT1_ID, REGION1, CONTRACTOR1_ID,
                                                        Set.of(ORG1_ID, ORG2_ID), true, false);
        message.setTransportType(TransportTypeEnum.TAXI.getName());
        contractInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(contractRepository.count()).isEqualTo(0);
        message.setTransportType(TransportTypeEnum.PERSONAL.getName());
        contractInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(contractRepository.count()).isEqualTo(0);
        message.setTransportType(TransportTypeEnum.PUBLIC.getName());
        contractInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(contractRepository.count()).isEqualTo(0);
    }
    
    /**
     * Сгенерировать сообщение о Контракте
     *
     * @param contractId id контракта
     * @param region регион действия контракта
     * @param contractorId id контрагента
     * @param organizationsIds id корп.клиентов, охваченных контрактом
     *
     * @return сообщение о Контракте
     */
    private ContractMessage getNewContractMessage(
            UUID contractId, String region, UUID contractorId,
            Set<UUID> organizationsIds, boolean active, boolean deleted
                                                 ) {
        return ContractMessage.builder()
                              .id(contractId)
                              .serviceType("EMPLOYEE_TRANSPORTATION")
                              .transportType(TransportTypeEnum.CARSHARING.getName())
                              .region(region)
                              .contractorId(contractorId)
                              .sum(1000L)
                              .startDate(LocalDate.of(2021, 4, 20))
                              .endDate(LocalDate.of(2021, 4, 21))
                              .userId(UUID.randomUUID())
                              .creationTime(LocalDateTime.now())
                              .organizations(organizationsIds)
                              .active(active)
                              .deleted(deleted)
                              .build();
    }
    
    /**
     * Проверить наличие каршеринга по заданным условиям
     *
     * @param contractorId id контрагента
     * @param organizationId id корп.клиента
     * @param status статус каршеринга
     * @param region регион
     */
    private void checkCarsharing(boolean status, UUID contractorId, UUID organizationId, String region) {
        assertThat(carsharingRepository.findByActive(status).stream()
                                       .filter(c -> c.getContract().getContractorId().equals(contractorId) &&
                                                    c.getOrganizationId().equals(organizationId) &&
                                                    c.getContract().getRegion().equals(region)).count()).isEqualTo(1);
    }
    
    /**
     * Вернуть каршеринг из БД или null
     *
     * @param contractorId id контрагента
     * @param organizationId id корп.клиента
     * @param region регион
     *
     * @return каршеринг из БД или null
     */
    private CorporateCarsharing getCarsharingFromDb(UUID contractorId, UUID organizationId, String region) {
        return carsharingRepository.findByContractContractorIdAndContractRegionAndOrganizationId(
                contractorId, region, organizationId).orElse(null);
    }
}