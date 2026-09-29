package ru.sber.transport.request.external.messaging;

import ru.sber.transport.request.external.model.Assessments;

public record TestAssessments(TestAssessment getService) implements Assessments {
}
