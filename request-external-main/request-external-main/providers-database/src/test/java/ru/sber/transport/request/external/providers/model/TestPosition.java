package ru.sber.transport.request.external.providers.model;

import java.util.List;
import java.util.UUID;
import ru.sber.transport.request.external.model.Position;

public record TestPosition(UUID getId, List<String> getAvailableClasses) implements Position {
}
