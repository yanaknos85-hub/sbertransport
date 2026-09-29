package ru.sber.transport.request_checks.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sber.transport.request_checks.util.Constants.TIMEZONE_UTC;
import static ru.sber.transport.request_checks.util.Constants.YANDEX_TRANSPORT_TYPE;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request_checks.RequestChecksApplication;
import ru.sber.transport.request_checks.entity.TripRequestEntity;
import ru.sber.transport.request_checks.entity.WaypointEntity;

@SpringBootTest(classes = RequestChecksApplication.class)
@MockitoBean(types = JwtDecoder.class)
@EmbeddedPostgres
@ActiveProfiles({"test"})
@DisplayName("Проверка работы TripRequestRepository")
class TripRequestRepositoryTest {

    @Autowired
    private TripRequestRepository tripRequestRepository;

    @Autowired
    private WaypointRepository waypointRepository;

    @Test
    @DisplayName("Сохранение новой заявки")
    void testSaveNewTripRequest() {
        val entity = createTripRequest();

        tripRequestRepository.save(entity);

        val saved = tripRequestRepository.findById(entity.getId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getId()).isEqualTo(entity.getId());
        assertThat(saved.get().getHumanReadableId()).isEqualTo(entity.getHumanReadableId());
        assertThat(saved.get().getTransportType()).isEqualTo(entity.getTransportType());
        assertThat(saved.get().getPassengerId()).isEqualTo(entity.getPassengerId());
        assertThat(saved.get().getDesiredDate().toEpochSecond()).isEqualTo(entity.getDesiredDate().toEpochSecond());
        assertThat(saved.get().getTimeZone()).isEqualTo(entity.getTimeZone());
        assertThat(saved.get().getDistance()).isEqualTo(entity.getDistance());
        assertThat(saved.get().getDuration()).isEqualTo(entity.getDuration());
    }

    @Test
    @DisplayName("Обновление существующей заявки")
    void testUpdateTripRequest() {
        val initialEntity = createTripRequest();
        tripRequestRepository.save(initialEntity);

        val updatedEntity = TripRequestEntity.builder()
            .id(initialEntity.getId())
            .transportType(YANDEX_TRANSPORT_TYPE)
            .humanReadableId("updated-" + initialEntity.getHumanReadableId())
            .passengerId(initialEntity.getPassengerId())
            .desiredDate(initialEntity.getDesiredDate())
            .timeZone(initialEntity.getTimeZone())
            .distance(initialEntity.getDistance() + 10000)
            .duration(initialEntity.getDuration() + 100000)
            .status("APPROVED")
            .build();

        tripRequestRepository.save(updatedEntity);

        val saved = tripRequestRepository.findById(updatedEntity.getId());
        assertThat(saved).isPresent();
        assertThat(saved.get().getTransportType()).isEqualTo(updatedEntity.getTransportType());
        assertThat(saved.get().getHumanReadableId()).isEqualTo(updatedEntity.getHumanReadableId());
        assertThat(saved.get().getDistance()).isEqualTo(updatedEntity.getDistance());
        assertThat(saved.get().getDuration()).isEqualTo(updatedEntity.getDuration());
    }

    @Test
    @DisplayName("Поиск несуществующей заявки")
    void testFindByIdNotFound() {
        val nonExistentId = UUID.randomUUID();

        val result = tripRequestRepository.findById(nonExistentId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Подсчет многоточечных поездок (более 2 точек)")
    void testCountMultipointRequests() {
        val passengerId = UUID.randomUUID();
        val dateTime = LocalDateTime.of(2026, 5, 21, 0, 0);

        val trip1 = createTripRequest(passengerId, dateTime).build();
        tripRequestRepository.save(trip1);
        waypointRepository.saveAll(List.of(
            createWaypoint(trip1.getId(), 0),
            createWaypoint(trip1.getId(), 1),
            createWaypoint(trip1.getId(), 2)
        ));

        val trip2 = createTripRequest(passengerId, dateTime).build();
        tripRequestRepository.save(trip2);
        waypointRepository.saveAll(List.of(
            createWaypoint(trip2.getId(), 0),
            createWaypoint(trip2.getId(), 1)
        ));

        val trip3 = createTripRequest(passengerId, dateTime).build();
        tripRequestRepository.save(trip3);
        waypointRepository.saveAll(List.of(
            createWaypoint(trip3.getId(), 0),
            createWaypoint(trip3.getId(), 1),
            createWaypoint(trip3.getId(), 2),
            createWaypoint(trip3.getId(), 3)
        ));

        val otherDate = LocalDateTime.of(2026, 5, 22, 0, 0);
        val trip4 = createTripRequest(passengerId, otherDate).build();
        tripRequestRepository.save(trip4);
        waypointRepository.saveAll(List.of(
            createWaypoint(trip4.getId(), 0),
            createWaypoint(trip4.getId(), 1),
            createWaypoint(trip4.getId(), 2)
        ));

        val count = tripRequestRepository.countMultipointRequests(passengerId, dateTime, TIMEZONE_UTC);

        assertThat(count).isEqualTo(2);
    }

    @Test
    @DisplayName("Подсчет многоточечных поездок - пустой результат")
    void testCountMultipointRequestsEmpty() {
        val passengerId = UUID.randomUUID();
        val date = LocalDateTime.of(2026, 5, 21, 0, 0);

        val count = tripRequestRepository.countMultipointRequests(passengerId, date, TIMEZONE_UTC);

        assertThat(count).isZero();
    }

    private static TripRequestEntity createTripRequest() {
        return TripRequestEntity.builder()
            .id(UUID.randomUUID())
            .humanReadableId("YA-" + System.currentTimeMillis())
            .transportType(YANDEX_TRANSPORT_TYPE)
            .passengerId(UUID.randomUUID())
            .desiredDate(OffsetDateTime.now())
            .timeZone(TIMEZONE_UTC)
            .distance(15500)
            .duration(1800L)
            .status("APPROVED")
            .build();
    }

    private static TripRequestEntity.TripRequestEntityBuilder createTripRequest(UUID passengerId, LocalDateTime date) {
        return TripRequestEntity.builder()
            .id(UUID.randomUUID())
            .humanReadableId("YA-" + System.currentTimeMillis())
            .transportType(YANDEX_TRANSPORT_TYPE)
            .passengerId(passengerId)
            .desiredDate(date.atOffset(ZoneOffset.UTC))
            .timeZone(TIMEZONE_UTC)
            .distance(15500)
            .duration(1800L)
            .status("APPROVED");
    }

    private static WaypointEntity createWaypoint(UUID tripRequestId, int index) {
        return WaypointEntity.builder()
            .id(UUID.randomUUID())
            .tripRequestId(tripRequestId)
            .orderingIndex(index)
            .latitude(55.751244 + index * 0.001)
            .longitude(37.618423 + index * 0.001)
            .build();
    }

}




