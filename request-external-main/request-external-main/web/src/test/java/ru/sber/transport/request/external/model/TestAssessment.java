package ru.sber.transport.request.external.model;

public record TestAssessment(String getComment, byte getRating) implements Assessment {
}
