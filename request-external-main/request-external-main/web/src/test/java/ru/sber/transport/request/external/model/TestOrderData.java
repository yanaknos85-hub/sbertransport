package ru.sber.transport.request.external.model;

import java.math.BigDecimal;
import java.time.Duration;

public record TestOrderData(BigDecimal getCost, Duration getDuration, long getDistance) implements OrderData {
}
