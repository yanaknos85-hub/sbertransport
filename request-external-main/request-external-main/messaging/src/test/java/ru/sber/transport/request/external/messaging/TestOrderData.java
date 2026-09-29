package ru.sber.transport.request.external.messaging;

import java.math.BigDecimal;
import java.time.Duration;
import ru.sber.transport.request.external.model.OrderData;

public record TestOrderData(
        BigDecimal getCost,
        Duration getDuration,
        long getDistance
) implements OrderData {
}
