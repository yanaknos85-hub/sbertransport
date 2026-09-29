package ru.sber.transport.request.external.messaging.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.request.external.messaging.message.ReceiptMessage;
import ru.sber.transport.request.external.model.BaseTripOrderData;
import ru.sber.transport.request.external.model.OrderData;
import ru.sber.transport.request.external.model.TripOrderData;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static ru.sber.transport.request.external.messaging.message.ReceiptMessage.FileData;

/**
 * Реализация маппера данных чека
 */
@Component
@RequiredArgsConstructor
public class ReceiptMapper {

    private static final String NPE_MAPPING_ERROR_MSG = "Ошибка получения параметра [%s], параметр должен быть заполнен";

    private static final String YANDEX_TRANSPORT_TYPE = "YANDEX";

    /**
     * Маппинг данных
     *
     * @param order заявка
     * @return данные чека
     * @throws IllegalArgumentException в случае отсутствия ключевых полей
     */
    public ReceiptMessage toReceiptMessage(TripOrderData order) throws IllegalArgumentException {
        var requestId = getRequestId(order);
        var date = getDate(order);
        var cost = getCost(order);
        var files = getFiles(order);

        return ReceiptMessage.builder()
                .requestId(requestId)
                .transportType(YANDEX_TRANSPORT_TYPE)
                .desiredDate(date)
                .cost(cost)
                .files(files)
                .build();
    }

    private LocalDateTime getDate(TripOrderData order) {
        return ofNullable(order)
                .map(BaseTripOrderData::getDate)
                .map(OffsetDateTime::toLocalDateTime)
                .orElseThrow(() -> new IllegalArgumentException(format(NPE_MAPPING_ERROR_MSG, "order.date")));
    }

    private List<FileData> getFiles(TripOrderData order) {
        var cost = getCost(order);
        var folderId = getRequestId(order);
        var fileName = getReceipt(order);
        var fileData = FileData.builder()
                .ticketCost(cost)
                .folderId(folderId)
                .fileName(fileName)
                .build();

        return List.of(fileData);
    }

    private UUID getRequestId(TripOrderData order) {
        return ofNullable(order)
                .map(TripOrderData::getId)
                .orElseThrow(() -> new IllegalArgumentException(format(NPE_MAPPING_ERROR_MSG, "order.id")));
    }

    private String getReceipt(TripOrderData order) {
        return ofNullable(order)
                .map(TripOrderData::getReceipt)
                .orElseThrow(() -> new IllegalArgumentException(format(NPE_MAPPING_ERROR_MSG, "order.receipt")));
    }

    private Long getCost(TripOrderData order) {
        return ofNullable(order)
                .map(TripOrderData::getActual)
                .map(OrderData::getCost)
                .map(BigDecimal::longValue)
                .orElseThrow(() -> new IllegalArgumentException(format(NPE_MAPPING_ERROR_MSG, "order.actual.cost")));
    }
}


