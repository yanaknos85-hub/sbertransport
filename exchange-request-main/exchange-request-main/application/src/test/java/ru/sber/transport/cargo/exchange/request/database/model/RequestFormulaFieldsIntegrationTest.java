package ru.sber.transport.cargo.exchange.request.database.model;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.cargo.exchange.request.RequestApplication;
import ru.sber.transport.cargo.exchange.request.database.dao.RequestRepository;
import ru.sber.transport.cargo.exchange.request.database.dao.WaypointRepository;
import ru.sber.transport.cargo.exchange.request.dto.AddressInfoDto;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;
import ru.sber.transport.cargo.exchange.request.enums.WaypointType;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = RequestApplication.class)
@ActiveProfiles("test")
@EmbeddedPostgres
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RequestFormulaFieldsIntegrationTest {

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private WaypointRepository waypointRepository;

    @Autowired
    private EntityManager entityManager;

    private Request request;

    @BeforeEach
    void setUp() {
        request = Request.builder()
                .ownerId(UUID.randomUUID())
                .organizationId(UUID.randomUUID())
                .humanReadableId("ОП-202601-0000001")
                .status(RequestStatus.DRAFT)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        request = requestRepository.saveAndFlush(request);

        entityManager.clear();
    }

    @Test
    @DisplayName("loadingDate должен быть равен дате первого LOAD waypoint по orderingIndex")
    void loadingDate_ShouldBeFirstLoadWaypointDate() {
        Waypoint load1 = Waypoint.builder()
                .request(request)
                .type(WaypointType.LOAD)
                .date(LocalDate.of(2026, 1, 5))
                .orderingIndex(0)
                .radius(100)
                .addressInfo(new AddressInfoDto())
                .from(LocalTime.of(9, 0))
                .to(LocalTime.of(18, 0))
                .build();

        Waypoint load2 = Waypoint.builder()
                .request(request)
                .type(WaypointType.LOAD)
                .date(LocalDate.of(2026, 1, 10))
                .orderingIndex(1)
                .radius(100)
                .addressInfo(new AddressInfoDto())
                .from(LocalTime.of(9, 0))
                .to(LocalTime.of(18, 0))
                .build();

        waypointRepository.save(load1);
        waypointRepository.save(load2);
        entityManager.flush();
        entityManager.clear();

        Request found = requestRepository.findById(request.getId()).orElseThrow();

        assertThat(found.getLoadingDate()).isEqualTo(load1.getDate());
    }

    @Test
    @DisplayName("deliveryDate должен быть равен дате последнего UNLOAD waypoint по orderingIndex")
    void deliveryDate_ShouldBeLastUnloadWaypointDate() {
        Waypoint unload1 = Waypoint.builder()
                .request(request)
                .type(WaypointType.UNLOAD)
                .date(LocalDate.of(2026, 1, 7))
                .orderingIndex(0)
                .radius(100)
                .addressInfo(new AddressInfoDto())
                .from(LocalTime.of(9, 0))
                .to(LocalTime.of(18, 0))
                .build();

        Waypoint unload2 = Waypoint.builder()
                .request(request)
                .type(WaypointType.UNLOAD)
                .date(LocalDate.of(2026, 1, 12))
                .orderingIndex(1)
                .radius(100)
                .addressInfo(new AddressInfoDto())
                .from(LocalTime.of(9, 0))
                .to(LocalTime.of(18, 0))
                .build();

        waypointRepository.save(unload1);
        waypointRepository.save(unload2);
        entityManager.flush();
        entityManager.clear();

        Request found = requestRepository.findById(request.getId()).orElseThrow();

        assertThat(found.getDeliveryDate()).isEqualTo(LocalDate.of(2026, 1, 12));
    }

    @Test
    @DisplayName("loadingDate должен быть null, если нет LOAD-пунктов")
    void loadingDate_ShouldBeNull_WhenNoLoadWaypoints() {
        Waypoint unload = Waypoint.builder()
                .request(request)
                .type(WaypointType.UNLOAD)
                .date(LocalDate.of(2026, 1, 10))
                .orderingIndex(0)
                .radius(100)
                .addressInfo(new AddressInfoDto())
                .from(LocalTime.of(9, 0))
                .to(LocalTime.of(18, 0))
                .build();

        waypointRepository.save(unload);
        entityManager.flush();
        entityManager.clear();

        Request found = requestRepository.findById(request.getId()).orElseThrow();

        assertThat(found.getLoadingDate()).isNull();
    }

    @Test
    @DisplayName("deliveryDate должен быть null, если нет UNLOAD-пунктов")
    void deliveryDate_ShouldBeNull_WhenNoUnloadWaypoints() {
        Waypoint load = Waypoint.builder()
                .request(request)
                .type(WaypointType.LOAD)
                .date(LocalDate.of(2026, 1, 5))
                .orderingIndex(0)
                .radius(100)
                .addressInfo(new AddressInfoDto())
                .from(LocalTime.of(9, 0))
                .to(LocalTime.of(18, 0))
                .build();

        waypointRepository.save(load);
        entityManager.flush();
        entityManager.clear();

        Request found = requestRepository.findById(request.getId()).orElseThrow();

        assertThat(found.getDeliveryDate()).isNull();
    }
}