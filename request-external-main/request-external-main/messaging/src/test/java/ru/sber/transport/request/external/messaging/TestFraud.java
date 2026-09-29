package ru.sber.transport.request.external.messaging;

import java.util.UUID;
import ru.sber.transport.request.external.model.Fraud;

public record TestFraud(UUID getId, String getComment, String getType) implements Fraud {
}
