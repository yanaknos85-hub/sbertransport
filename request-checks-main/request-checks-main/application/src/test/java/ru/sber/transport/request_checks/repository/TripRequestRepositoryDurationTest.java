package ru.sber.transport.request_checks.repository;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.request_checks.RequestChecksApplication;
import ru.sber.transport.request_checks.entity.TripRequestEntity;

@SpringBootTest(classes = RequestChecksApplication.class)
@MockitoBean(types = JwtDecoder.class)
@EmbeddedPostgres
@ActiveProfiles({"test"})
@DisplayName("Проверка работы TripRequestRepository - Duration")
class TripRequestRepositoryDurationTest {

    private static final int DAY_START_HOUR = 22;

    @Autowired
    private TripRequestRepository tripRequestRepository;

    @Test
    @DisplayName("Сумма длительности поездок за день")
    void testSumDurationByDate() {
        val passengerId = UUID.randomUUID();
        val zoneId = "+03:00";
        val date = OffsetDateTime.now();
        
        val trip1 = createTripRequest(passengerId, date, 1800L); // 30 минут
        tripRequestRepository.save(trip1);
        
        val trip2 = createTripRequest(passengerId, date, 3600L); // 1 час
        tripRequestRepository.save(trip2);
        
        val totalDuration = tripRequestRepository.sumDurationByDate(passengerId, date, zoneId, DAY_START_HOUR, "Europe/Moscow");
        
        assertThat(totalDuration).isEqualTo(5400L); // 30 мин + 1 час = 5400 сек
    }

    @Test
    @DisplayName("Сумма длительности поездок - пустой результат")
    void testSumDurationByDateEmpty() {
        val passengerId = UUID.randomUUID();
        val date = OffsetDateTime.now();
        val zoneId = "+03:00";
        
        val totalDuration = tripRequestRepository.sumDurationByDate(passengerId, date, zoneId, DAY_START_HOUR, "Europe/Moscow");
        
        assertThat(totalDuration).isZero();
    }

    @Test
    @DisplayName("Сумма длительности поездок за день - граница даты с 22:00")
    void testSumDurationByDateEdgeCase() {
        val passengerId = UUID.randomUUID();
        val zoneId = "+03:00";
        // 21 мая 23:00 (день заканчивается в 22:00, так что это еще 21 мая)
        val baseDate = OffsetDateTime.of(2026, 5, 21, 23, 0, 0, 0, java.time.ZoneOffset.of("+03:00"));
        
        // Поездка в 23:00 (это все еще день 21 мая, так как день начинается в 22:00)
        val trip1 = createTripRequest(passengerId, baseDate, 3600L);
        tripRequestRepository.save(trip1);
        
        // Поездка на 22 мая в 00:01 (это уже день 22 мая)
        val nextDay = baseDate.withDayOfMonth(22).withHour(0).withMinute(1);
        val trip2 = createTripRequest(passengerId, nextDay, 1800L);
        tripRequestRepository.save(trip2);
        
        // Проверяем только поездки за день, начинающийся в 22:00 от 21 мая 23:00
        val totalDuration = tripRequestRepository.sumDurationByDate(passengerId, baseDate, zoneId, DAY_START_HOUR, "Europe/Moscow");
        
        // Обе поездки должны быть включены, так как обе в пределах одного "дня" (22:00-22:00)
        assertThat(totalDuration).isEqualTo(5400L);
    }

    @Test
    @DisplayName("Сумма длительности поездок за день - нулевые значения")
    void testSumDurationByDateWithZeroDuration() {
        val passengerId = UUID.randomUUID();
        val zoneId = "+03:00";
        val date = OffsetDateTime.now();
        
        val trip1 = createTripRequest(passengerId, date, 0L);
        tripRequestRepository.save(trip1);
        
        val totalDuration = tripRequestRepository.sumDurationByDate(passengerId, date, zoneId, DAY_START_HOUR, "Europe/Moscow");
        
        assertThat(totalDuration).isZero();
    }

    @Test
    @DisplayName("Сумма длительности поездок за день - с учетом 22:00")
    void testSumDurationByDateWithDayStartTwentyTwo() {
        val passengerId = UUID.randomUUID();
        val zoneId = "+03:00";
        
        // 21 мая 23:00 - эта поездка будет включена в "день" 21 мая (так как день начинается в 22:00)
        val trip1 = createTripRequest(passengerId, OffsetDateTime.of(2026, 5, 21, 23, 0, 0, 0, java.time.ZoneOffset.of("+03:00")), 3600L);
        tripRequestRepository.save(trip1);
        
        // 22 мая 01:00 - эта поездка будет включена в "день" 21 мая (так как день начинается в 22:00)
        val trip2 = createTripRequest(passengerId, OffsetDateTime.of(2026, 5, 22, 1, 0, 0, 0, java.time.ZoneOffset.of("+03:00")), 7200L);
        tripRequestRepository.save(trip2);
        
        // Проверяем для 22 мая 01:00 - обе поездки должны быть включены (день начинается в 22:00 21 мая)
        val requestDate = OffsetDateTime.of(2026, 5, 22, 1, 0, 0, 0, java.time.ZoneOffset.of("+03:00"));
        val totalDuration = tripRequestRepository.sumDurationByDate(passengerId, requestDate, zoneId, DAY_START_HOUR, "Europe/Moscow");
        
        // 3600 + 7200 = 10800 сек = 3 часа
        assertThat(totalDuration).isEqualTo(10800L);
    }

    @ParameterizedTest
    @CsvSource({
        "TAXI_CANCELLED, 1800",
        "GROUP_TRANSFER_CANCELLED, 1200",
        "BUS_CANCELLED, 600",
        "CANCELLED, 900"
    })
    @DisplayName("Сумма длительности — поездки с отмененными статусами исключаются")
    void testSumDurationByDateExcludesCancelledStatuses(String cancelledStatus, long expectedDuration) {
        val passengerId = UUID.randomUUID();
        val zoneId = "+03:00";
        val date = OffsetDateTime.now();

        val trip1 = createTripRequest(passengerId, date, 3600L, cancelledStatus);
        tripRequestRepository.save(trip1);
        val trip2 = createTripRequest(passengerId, date, expectedDuration, "APPROVED");
        tripRequestRepository.save(trip2);
        val totalDuration = tripRequestRepository.sumDurationByDate(passengerId, date, zoneId, DAY_START_HOUR, "Europe/Moscow");

        assertThat(totalDuration).isEqualTo(expectedDuration);
    }

    private TripRequestEntity createTripRequest(UUID passengerId, OffsetDateTime date, Long duration) {
        return createTripRequest(passengerId, date, duration, "APPROVED");
    }

    private TripRequestEntity createTripRequest(UUID passengerId, OffsetDateTime date, Long duration, String status) {
        return TripRequestEntity.builder()
            .id(UUID.randomUUID())
            .humanReadableId("YA-" + System.currentTimeMillis())
            .transportType("YANDEX")
            .passengerId(passengerId)
            .desiredDate(date)
            .timeZone("+03:00")
            .distance(15500)
            .duration(duration)
            .status(status)
            .build();
    }

}
