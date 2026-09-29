package ru.sber.transport.request.external.messaging;

import ru.sber.transport.request.external.model.Assessment;

public record TestAssessment(String getComment, byte getRating) implements Assessment {
}
