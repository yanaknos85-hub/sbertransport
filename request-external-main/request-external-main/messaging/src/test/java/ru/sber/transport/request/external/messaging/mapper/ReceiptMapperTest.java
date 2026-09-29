package ru.sber.transport.request.external.messaging.mapper;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.request.external.messaging.message.ReceiptMessage;
import ru.sber.transport.request.external.messaging.message.ReceiptMessage.FileData;
import ru.sber.transport.request.external.model.OrderData;
import ru.sber.transport.request.external.model.TripOrderData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTest
@IsolatedTest
@Feature("app_platform_request_external")
class ReceiptMapperTest {

    private static final String YANDEX_TRANSPORT_TYPE = "YANDEX";

    private static final UUID SOME_REQUEST_ID = UUID.randomUUID();
    private static final OffsetDateTime SOME_DATE = OffsetDateTime.of(2025, 1, 15, 10, 30, 0, 0, UTC);
    private static final BigDecimal SOME_COST = new BigDecimal(1500);
    private static final String SOME_RECEIPT = "TRIP-123";

    private final ReceiptMapper mapper = new ReceiptMapper();

    private static TripOrderData createOrder(UUID id,
                                             OffsetDateTime date,
                                             BigDecimal cost,
                                             String receipt) {
        var order = mock(TripOrderData.class);
        when(order.getId()).thenReturn(id);
        when(order.getDate()).thenReturn(date);
        when(order.getReceipt()).thenReturn(receipt);
        var actual = mock(OrderData.class);
        when(order.getActual()).thenReturn(actual);
        when(actual.getCost()).thenReturn(cost);
        return order;
    }

    private static ReceiptMessage createReceipt(UUID requestId,
                                                LocalDateTime date,
                                                Long cost,
                                                String receipt) {
        var fileData = FileData.builder()
                .ticketCost(cost)
                .folderId(requestId)
                .fileName(receipt)
                .build();

        return ReceiptMessage.builder()
                .requestId(requestId)
                .transportType(YANDEX_TRANSPORT_TYPE)
                .desiredDate(date)
                .cost(cost)
                .files(List.of(fileData))
                .build();
    }

    static Stream<Arguments> getArgumentsWithNullFields() {
        return Stream.of(
                Arguments.of("order.id", createOrder(null, SOME_DATE, SOME_COST, SOME_RECEIPT)),
                Arguments.of("order.date", createOrder(SOME_REQUEST_ID, null, SOME_COST, SOME_RECEIPT)),
                Arguments.of("order.actual.cost", createOrder(SOME_REQUEST_ID, SOME_DATE, null, SOME_RECEIPT)),
                Arguments.of("order.receipt", createOrder(SOME_REQUEST_ID, SOME_DATE, SOME_COST, null))
        );
    }

    @DisplayName("Маппинг при отсутствии обязательного поля: выбрасывается IllegalArgumentException с именем поля")
    @ParameterizedTest
    @MethodSource("getArgumentsWithNullFields")
    void toReceiptMessage_nullField_throwsException(String expectedFieldName, final TripOrderData order) {
        assertThatThrownBy(() -> mapper.toReceiptMessage(order))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(expectedFieldName);
    }

    @Test
    @DisplayName("Маппинг заявки в чек при полностью заполненных обязательных полях")
    void toReceiptMessage_allFieldsPresent_success() {
        var order = createOrder(SOME_REQUEST_ID, SOME_DATE, SOME_COST, SOME_RECEIPT);
        var receipt = createReceipt(SOME_REQUEST_ID, SOME_DATE.toLocalDateTime(), SOME_COST.longValue(), SOME_RECEIPT);

        var expected = mapper.toReceiptMessage(order);

        assertEquals(expected, receipt);
    }
}