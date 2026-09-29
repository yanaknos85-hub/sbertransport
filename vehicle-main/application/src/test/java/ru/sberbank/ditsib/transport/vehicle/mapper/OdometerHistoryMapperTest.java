package ru.sberbank.ditsib.transport.vehicle.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.vehicle.database.model.OdometerHistory;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.OdometerHistoryValueMessage;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class OdometerHistoryMapperTest {

    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
    private static final Clock fixedClock = Clock.fixed(LOCAL_DATE_TIME.toInstant(ZoneOffset.UTC), ZoneOffset.of(ZoneOffset.UTC.getId()));
    private final OdometerHistoryMapper mapper = new OdometerHistoryMapperImpl();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Mock
    private Clock clock;

    @BeforeEach
    void beforeEach() {
        doReturn(fixedClock.instant()).when(clock).instant();
        doReturn(fixedClock.getZone()).when(clock).getZone();
        mapper.setObjectMapper(objectMapper);
    }

    @Test
    @SneakyThrows
    void addIndicatorsHistoryDtoToOdometerHistory() {
        var transportId = UUID.randomUUID();
        var value = 10250;
        var creatorUserId = UUID.randomUUID();
        var creationTime = LocalDateTime.now(clock);
        var attributes = Map.<String, Object>of("EWB_ID", "422579f1-501a-40c3-bdb0-ee0f7f396a1c", "DIRECTION", "OUT");

        var message = new OdometerHistoryValueMessage(transportId, value, creatorUserId, creationTime, attributes);
        var odometerHistory = mapper.addIndicatorsHistoryDtoToOdometerHistory(message);

        assertThat(odometerHistory)
                .isNotNull()
                .extracting(
                        OdometerHistory::getTransportId,
                        OdometerHistory::getValue,
                        OdometerHistory::getCreatorUserId,
                        OdometerHistory::getCreationTime,
                        OdometerHistory::getMetaAttributes)
                .containsExactly(transportId,
                        value,
                        creatorUserId,
                        creationTime,
                        objectMapper.writeValueAsString(attributes));
    }

    @Test
    @SneakyThrows
    void addIndicatorsHistoryDtoToOdometerHistoryEmptyAttributes() {
        var transportId = UUID.randomUUID();
        var value = 10250;
        var creatorUserId = UUID.randomUUID();
        var creationTime = LocalDateTime.now(clock);

        var message = new OdometerHistoryValueMessage(transportId, value, creatorUserId, creationTime, null);
        var odometerHistory = mapper.addIndicatorsHistoryDtoToOdometerHistory(message);

        assertThat(odometerHistory)
                .isNotNull()
                .extracting(
                        OdometerHistory::getTransportId,
                        OdometerHistory::getValue,
                        OdometerHistory::getCreatorUserId,
                        OdometerHistory::getCreationTime,
                        OdometerHistory::getMetaAttributes)
                .containsExactly(transportId,
                        value,
                        creatorUserId,
                        creationTime,
                        null);
    }

}