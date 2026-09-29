package ru.sber.transport.request_checks.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.ArrayList;
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
import ru.sber.transport.request_checks.entity.WaypointEntity;

@SpringBootTest(classes = RequestChecksApplication.class)
@ActiveProfiles({"test"})
@EmbeddedPostgres
@MockitoBean(types = JwtDecoder.class)
@DisplayName("Проверка работы WaypointRepository")
class WaypointRepositoryTest {

    @Autowired
    private WaypointRepository waypointRepository;

    @Test
    @DisplayName("Сохранение новой путевой точки")
    void testSaveNewWaypoint() {
        val tripRequestId = UUID.randomUUID();
        val entity = createWaypoint(tripRequestId);

        waypointRepository.save(entity);

        val saved = waypointRepository.findByTripRequestId(tripRequestId);
        assertThat(saved).hasSize(1);
        assertThat(saved.getFirst()).isEqualTo(entity);
    }

    @Test
    @DisplayName("Поиск путевых точек по несуществующей заявке")
    void testFindByTripRequestIdNotFound() {
        val nonExistentId = UUID.randomUUID();

        val result = waypointRepository.findByTripRequestId(nonExistentId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Сохранение путевых точек batch")
    void testSaveAllWaypoints() {
        val tripRequestId = UUID.randomUUID();
        val waypoints = List.of(
            createWaypoint(tripRequestId),
            createWaypoint(tripRequestId)
        );

        waypointRepository.saveAll(waypoints);

        val saved = waypointRepository.findByTripRequestId(tripRequestId);
        assertThat(saved).hasSize(2);
    }

    @Test
    @DisplayName("Сохранение пустого списка путевых точек")
    void testSaveAllEmptyWaypoints() {
        val waypoints = new ArrayList<WaypointEntity>();

        waypointRepository.saveAll(waypoints);

        assertDoesNotThrow(() -> waypointRepository.saveAll(waypoints));
    }

    private static WaypointEntity createWaypoint(UUID tripRequestId) {
        return WaypointEntity.builder()
            .id(UUID.randomUUID())
            .tripRequestId(tripRequestId)
            .orderingIndex(0)
            .latitude(37.618423)
            .longitude(55.751244)
            .build();
    }

}
