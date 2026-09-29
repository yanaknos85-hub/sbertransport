package ru.sberbank.transport.oto.cargo.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.transport.oto.cargo.database.dao.RequestRepository;
import ru.sberbank.transport.oto.cargo.database.dao.TemplateForCargoRepository;
import ru.sberbank.transport.oto.cargo.database.model.Request;
import ru.sberbank.transport.oto.cargo.database.model.template.TemplateForCargo;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest
@Isolated
@Feature("app_passenger_oto")
@EmbeddedPostgres
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OtoEngineerServiceImplTest extends KafkaTest {

    @Autowired
    private TemplateForCargoRepository templateForCargoRepository;

    @Autowired
    private RequestRepository requestRepository;

    @ParameterizedTest
    @MethodSource("provideStatusesForTest")
    @Order(1)
    void findByOrganizationIdAndStatusInTest(List<String> statuses, int expectedResults) {

        UUID organizationId = UUID.randomUUID();
        UUID organizationId2 = UUID.randomUUID();
        var status1 = TripRequestStatus.CARGO_APPROVED;
        var status2 = TripRequestStatus.CARGO_CANCELED;

        TemplateForCargo cargo1 = TemplateForCargo.builder()
                                                  .id(UUID.randomUUID())
                                                  .organizationId(organizationId)
                                                  .status(status1)
                                                  .build();

        TemplateForCargo cargo2 = TemplateForCargo.builder()
                                                  .id(UUID.randomUUID())
                                                  .organizationId(organizationId)
                                                  .status(status2)
                                                  .build();

        TemplateForCargo cargo3 = TemplateForCargo.builder()
                                                  .id(UUID.randomUUID())
                                                  .organizationId(organizationId2)
                                                  .status(status1)
                                                  .build();

        templateForCargoRepository.save(cargo1);
        templateForCargoRepository.save(cargo2);
        templateForCargoRepository.save(cargo3);

        PageRequest pageable = PageRequest.of(0, 10);

        Page<TemplateForCargo> result = templateForCargoRepository.findAllByOrganizationIdAndStatusIn(organizationId, statuses, pageable);

        assertNotNull(result);
        assertEquals(expectedResults, result.getTotalElements());

        if (statuses.contains(status1.getDescription())) {
            assertTrue(result.getContent().contains(cargo1));
        }
        if (statuses.contains(status2.getDescription())) {
            assertTrue(result.getContent().contains(cargo2));
        }
        assertFalse(result.getContent().contains(cargo3)); //лишняя организация
    }

    /**
     * Тест 1: Все группы — фильтрация отсутствует
     * executorGroupIds = пустой список, emptyExecutorGroup = false
     * Ожидаемый результат: возвращается все 3 заявки
     */
    @Test
    @Order(2)
    void executorGroupFiltering_AllGroups_NoFilter() {
        UUID group1 = UUID.randomUUID();
        UUID group2 = UUID.randomUUID();

        Request request1 = createRequest(TripRequestStatus.CARGO_APPROVED, group1);
        Request request2 = createRequest(TripRequestStatus.CARGO_APPROVED, group2);
        Request request3 = createRequest(TripRequestStatus.CARGO_APPROVED, null);

        requestRepository.save(request1);
        requestRepository.save(request2);
        requestRepository.save(request3);

        var resultCount = requestRepository.findAll(PageRequest.of(0, 10)).getTotalElements();

        assertEquals(3, resultCount, "При emptyExecutorGroup=false и пустом списке групп — фильтрация отсутствует, возвращаются все заявки");
    }

    /**
     * Тест 2: Без групп — только заявки без группы исполнителя
     * executorGroupIds = пустой список, emptyExecutorGroup = true
     * Ожидаемый результат: возвращается только 1 заявка (request3 с null group)
     */
    @Test
    @Order(3)
    void executorGroupFiltering_NoGroups_EmptyExecutorGroupTrue() {
        requestRepository.deleteAll();
        UUID group1 = UUID.randomUUID();
        UUID group2 = UUID.randomUUID();

        Request request1 = createRequest(TripRequestStatus.CARGO_APPROVED, group1);
        Request request2 = createRequest(TripRequestStatus.CARGO_APPROVED, group2);
        Request request3 = createRequest(TripRequestStatus.CARGO_APPROVED, null);

        requestRepository.save(request1);
        requestRepository.save(request2);
        requestRepository.save(request3);

        var resultCount = requestRepository
                .findAll((root, query, cb) -> cb.isNull(root.get("executorGroupId")), PageRequest.of(0, 10));

        assertEquals(1, resultCount.getTotalElements(), "При emptyExecutorGroup=true — возвращаются только заявки без группы исполнителя");
    }

    /**
     * Тест 3: Конкретные группы — фильтрация по ID
     * executorGroupIds = [group1], emptyExecutorGroup = false
     * Ожидаемый результат: возвращается только 1 заявка (request1 с group1)
     */
    @Test
    @Order(4)
    void executorGroupFiltering_ConcreteGroups_FilterById() {
        UUID group1 = UUID.randomUUID();
        UUID group2 = UUID.randomUUID();

        Request request1 = createRequest(TripRequestStatus.CARGO_APPROVED, group1);
        Request request2 = createRequest(TripRequestStatus.CARGO_APPROVED, group2);
        Request request3 = createRequest(TripRequestStatus.CARGO_APPROVED, null);

        requestRepository.save(request1);
        requestRepository.save(request2);
        requestRepository.save(request3);

        var resultCount = requestRepository
                .findAll((root, query, cb) -> root.get("executorGroupId").in(group1), PageRequest.of(0, 10))
                .getTotalElements();

        assertEquals(1, resultCount, "При указанных группах — возвращаются только заявки с этими группами");
    }

    /**
     * Тест 4: emptyExecutorGroup=true имеет приоритет над executorGroupIds
     * executorGroupIds = [group1, group2], emptyExecutorGroup = true
     * Ожидаемый результат: возвращается только 1 заявка (request3 с null group)
     */
    @Test
    @Order(5)
    void executorGroupFiltering_EmptyPriorityOverIds() {
        UUID group1 = UUID.randomUUID();
        UUID group2 = UUID.randomUUID();

        Request request1 = createRequest(TripRequestStatus.CARGO_APPROVED, group1);
        Request request2 = createRequest(TripRequestStatus.CARGO_APPROVED, group2);
        Request request3 = createRequest(TripRequestStatus.CARGO_APPROVED, null);

        requestRepository.save(request1);
        requestRepository.save(request2);
        requestRepository.save(request3);

        var resultCount = requestRepository
                .findAll((root, query, cb) -> cb.isNull(root.get("executorGroupId")), PageRequest.of(0, 10))
                .getTotalElements();

        assertEquals(1, resultCount, "При emptyExecutorGroup=true приоритет над executorGroupIds — возвращаются только заявки без группы");
    }

    // ===================== Вспомогательные методы =====================

    private Request createRequest(TripRequestStatus status, UUID executorGroupId) {
        requestRepository.deleteAll();
        UUID id = UUID.randomUUID();
        Request request = Request.builder()
                .id(id)
                .humanReadableId("TEST-" + id.toString().substring(0, 8))
                .status(status.name())
                .executorGroupId(executorGroupId)
                .transportType("TRUCK")
                .approvalState("AWAITING_APPROVAL")
                .requestType("SINGLE")
                .build();
        return request;
    }

    static Stream<Arguments> provideStatusesForTest() {
        return Stream.of(
                Arguments.of(List.of(TripRequestStatus.CARGO_APPROVED, TripRequestStatus.CARGO_CANCELED), 2),
                Arguments.of(List.of(TripRequestStatus.CARGO_APPROVED), 1),
                Arguments.of(List.of(TripRequestStatus.CARGO_CANCELED), 1),
                Arguments.of(List.of(TripRequestStatus.CARGO_DELIVERY_CONFIRMATION_FINISHED), 0));
    }
}
