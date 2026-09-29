package ru.sber.transport.request.external.providers.model;

import java.util.UUID;
import ru.sber.transport.request.external.model.Fraud;

public record TestFraud(UUID getId, String getComment, String getType) implements Fraud {
}
