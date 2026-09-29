package ru.sber.transport.request_checks.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Проверка DurationCheckRequestDTO")
class DurationCheckRequestDtoTest {

    @Test
    void testDurationCheckRequestDTOConstructor() {
        val passengerId = UUID.randomUUID();
        val desiredDate = OffsetDateTime.now();
        val expectedDuration = 3600000L;
        val timeZone = "+03:00";

        val request = new DurationCheckRequestDto(passengerId, desiredDate, expectedDuration, timeZone);

        assertThat(request.passengerId()).isEqualTo(passengerId);
        assertThat(request.desiredDate()).isEqualTo(desiredDate);
        assertThat(request.expectedDuration()).isEqualTo(expectedDuration);
        assertThat(request.timeZone()).isEqualTo(timeZone);
    }

    @Test
    void testDurationCheckRequestDTOWithDifferentValues() {
        val passengerId = UUID.randomUUID();
        val desiredDate = OffsetDateTime.now();
        val expectedDuration = 7200000L;
        val timeZone = "+05:00";

        val request = new DurationCheckRequestDto(passengerId, desiredDate, expectedDuration, timeZone);

        assertThat(request.passengerId()).isEqualTo(passengerId);
        assertThat(request.desiredDate()).isEqualTo(desiredDate);
        assertThat(request.expectedDuration()).isEqualTo(expectedDuration);
        assertThat(request.timeZone()).isEqualTo(timeZone);
    }

    @Test
    void testDurationCheckRequestDTOWithDifferentTimezones() {
        val passengerId = UUID.randomUUID();
        val desiredDate = OffsetDateTime.now();
        val expectedDuration = 3600000L;

        val requestUtc = new DurationCheckRequestDto(passengerId, desiredDate, expectedDuration, "UTC");
        val requestMoscow = new DurationCheckRequestDto(passengerId, desiredDate, expectedDuration, "+03:00");

        assertThat(requestUtc.timeZone()).isEqualTo("UTC");
        assertThat(requestMoscow.timeZone()).isEqualTo("+03:00");
    }

}
