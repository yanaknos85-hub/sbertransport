package ru.sberbank.transport.oto.cargo.mappers;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.transport.oto.cargo.database.model.template.TemplateForCargo;
import ru.sberbank.ditsib.transport.request.messaging.TemplateForCargoMessage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@UnitTest
@Isolated
@Feature("app_passenger_oto")
@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class TemplateForCargoMapperTest {
    
    private final TemplateMapper mapper = new TemplateMapperImpl();
    
    @Test
    void entityToTemplateForOtoDtoTest() {
        var waypoint1 = Instancio.of(TemplateForCargoMessage.Waypoint.class)
                                 .set(Select.field("orderingIndex"), 1)
                                 .create();
        var waypoint2 = Instancio.of(TemplateForCargoMessage.Waypoint.class)
                                 .set(Select.field("orderingIndex"), 2)
                                 .create();
        var actual = Instancio.of(TemplateForCargo.class)
                              .supply(
                                      Select.field(TemplateForCargoMessage.TemplateInfo.class, "waypoints"),
                                      () -> List.of(waypoint1, waypoint2)
                                     )
                              .set(Select.field("cronExpression"), "0 0 0 * * 1,2,3").create();
        
        String expectedCronFormatted = """
                                       Еженедельно:
                                       Дни: Пн / Вт / Ср
                                       """;
        
        var expected = mapper.entityToTemplateForOtoDto(actual);
        
        var senderWaypoint = actual.getTemplate().getWaypoints().getFirst();
        var recipientWaypoint = actual.getTemplate().getWaypoints().getLast();
        var nextRequestDate = actual.getRequestsDateDelivery().stream()
                                    .min(Comparator.comparing(
                                            date -> Duration.between(LocalDateTime.now(), date).abs())).orElse(null);
        var lastRequestDate = actual.getRequestsDateDelivery().stream()
                                    .max(Comparator.naturalOrder()).orElse(null);
        
        assertAll(
                () -> assertEquals(expected.getHumanReadableId(), actual.getHumanReadableId()),
                () -> assertEquals(expected.getStatus(), actual.getStatus().getDescription()),
                () -> assertEquals(expected.getTariffType(), actual.getTransportType()),
                () -> assertEquals(expected.getNextRequestDate(), nextRequestDate),
                () -> assertEquals(expected.getLastRequestDate(), lastRequestDate),
                () -> assertEquals(expected.getAuthor(), actual.getTemplate().getAuthorFIO()),
                () -> assertEquals(expected.getAuthorMobilePhone(), actual.getTemplate().getAuthorPhone()),
                () -> assertEquals(expected.getCarrier(), actual.getTemplate().getContragent()),
                () -> assertEquals(expected.getSender(), actual.getSenderName()),
                () -> assertEquals(expected.getRecipient(), actual.getRecipientName()),
                () -> assertEquals(expected.getSenderAddress(), actual.getSenderAddress()),
                () -> assertEquals(expected.getRecipientAddress(), actual.getRecipientAddress()),
                () -> assertEquals(expected.getSenderPhone(), senderWaypoint.getContact().getPhone()),
                () -> assertEquals(expected.getSenderOrganization(), senderWaypoint.getOrganization()),
                () -> assertEquals(expected.getRecipientPhone(), recipientWaypoint.getContact().getPhone()),
                () -> assertEquals(expected.getRecipientOrganization(), recipientWaypoint.getOrganization()),
                () -> assertEquals(expected.getPlannedRange(), actual.getTemplate().getDistance(), 0.001d),
                () -> assertEquals(expected.getPlannedPrice(), actual.getTotalCost()),
                () -> assertEquals(expected.getCargoType(), actual.getTemplate().getCargoName()),
                () -> assertEquals(expected.getLoaders(), actual.getTemplate().getLoaders()),
                () -> assertEquals(expected.getWeight(), actual.getTemplate().getWeight()),
                () -> assertEquals(expected.getVolume(), actual.getTemplate().getVolume()),
                () -> assertEquals(expected.getCreationTime(), actual.getCreationTime()),
                () -> assertEquals(expected.getComment(), actual.getTemplate().getComment()),
                () -> assertEquals(expected.getCountRequests(), actual.getCountRequests()),
                () -> assertEquals(expected.getCountRequestsInRoute(), actual.getCountRequestsInRoute()),
                () -> assertEquals(expectedCronFormatted, mapper.cargoPeriodDtoToString(actual.getCronExpression())));
    }
    
    @ParameterizedTest
    @CsvSource({
            "'0 0 0 * * 1,2,3', 'Еженедельно:\nДни: Пн / Вт / Ср\n'",
            "'0 0 0 ? * 1#1,1#3', 'Ежемесячно:\nНеделя: 1 / 3\nДни: Пн\n'",
            "'0 0 0 * 1,2,3 1,2#1,2', 'Ежеквартально:\nМесяц: 1 / 2 / 3\nНеделя: 1\nДни: Пн / Вт\n'"
    })
    void cargoPeriodDtoToStringTest(String cronExpression, String expected) {
        String result = mapper.cargoPeriodDtoToString(cronExpression);
        assertEquals(expected, result);
    }
}
