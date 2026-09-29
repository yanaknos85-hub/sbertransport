package ru.sberbank.ditsib.transport.request.messaging;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.messaging.message.DepartmentTripRequestApproversMessage;
import ru.sberbank.ditsib.transport.request.RequestApplication;
import ru.sberbank.ditsib.transport.request.database.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.request.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.request.database.model.Approver;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.mappers.DepartmentTripRequestApproversMapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@SuppressWarnings("OptionalGetWithoutIsPresent")
@EmbeddedPostgres
@SpringBootTest(classes = RequestApplication.class)
@DisplayName("Проверка получения согласующих")
@MockitoBean(types = JwtDecoder.class)
public class DepartmentTripRequestApproversListenerTest extends KafkaTest {
    @Autowired
    @Qualifier("departmentTripRequestApproversInput")
    private Consumer<Message<DepartmentTripRequestApproversMessage>> departmentTripRequestApproversInput;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private DepartmentTripRequestApproversMapper mapper;
    
    private Department department;
    private DepartmentTripRequestApproversMessage.Approver approver1;
    private DepartmentTripRequestApproversMessage.Approver approver2;
    private DepartmentTripRequestApproversMessage.Approver approver3;
    
    @BeforeEach
    void fillRepository() {
        var organization = organizationRepository.save(Organization.builder().id(UUID.randomUUID()).build());
        department = departmentRepository
                .save(Department.builder()
                                .id(UUID.randomUUID())
                                .humanReadableId("asd")
                                .organization(organization)
                                .departmentName("Depname")
                                .build());
        approver1 = DepartmentTripRequestApproversMessage.Approver.builder().employeeId(UUID.randomUUID()).build();
        approver2 = DepartmentTripRequestApproversMessage.Approver.builder().employeeId(UUID.randomUUID())
                                                                  .transportType(TransportTypeEnum.TAXI.name()).build();
        approver3 = DepartmentTripRequestApproversMessage.Approver.builder().employeeId(UUID.randomUUID())
                                                                  .transportType(TransportTypeEnum.PUBLIC.name()).build();
    }
    
    @AfterEach
    void dropRepository() {
        departmentRepository.deleteAll();
    }
    
    @DisplayName("До прихода сообщения у подразделения не было согласований")
    @Test
    public void test_new() {
        assertThat(departmentRepository.getDepartmentWithApprovers(department.getId()).get().getApprovers()).containsExactlyInAnyOrder();
        DepartmentTripRequestApproversMessage message =
                DepartmentTripRequestApproversMessage.builder()
                                                     .departmentId(department.getId())
                                                     .approvers(Arrays.asList(approver1, approver2, approver3))
                                                     .build();
        departmentTripRequestApproversInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(departmentRepository.getDepartmentWithApprovers(department.getId()).get().getApprovers())
                .containsExactlyInAnyOrder(entity(approver1), entity(approver2), entity(approver3));
        
    }
    
    @DisplayName("Изменение списка согласующих")
    @Test
    public void test_update() {
        test_new();
        DepartmentTripRequestApproversMessage message =
                DepartmentTripRequestApproversMessage.builder()
                                                     .departmentId(department.getId())
                                                     .approvers(Arrays.asList(approver1, approver2))
                                                     .build();
        departmentTripRequestApproversInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(departmentRepository.getDepartmentWithApprovers(department.getId()).get().getApprovers())
                .containsExactlyInAnyOrder(entity(approver1), entity(approver2));
        
    }
    
    
    @DisplayName("Изменение списка согласующих до пустого")
    @Test
    public void test_update_empty() {
        test_new();
        DepartmentTripRequestApproversMessage message =
                DepartmentTripRequestApproversMessage.builder()
                                                     .departmentId(department.getId())
                                                     .approvers(Collections.emptyList())
                                                     .build();
        departmentTripRequestApproversInput.accept(MessageBuilder.withPayload(message).build());
        assertThat(departmentRepository.getDepartmentWithApprovers(department.getId()).get().getApprovers())
                .containsExactlyInAnyOrder();
        
    }
    
    
    private Approver entity(DepartmentTripRequestApproversMessage.Approver approver) {
        return mapper.toEntity(approver);
    }
    
    
}
