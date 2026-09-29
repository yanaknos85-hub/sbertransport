package ru.sber.transport.request.external.model;

import java.util.UUID;

public record TestFraud(UUID getId, String getComment, String getType) implements Fraud {
}
