package ru.sber.transport.request.external.providers.order.model;

import static ru.sber.transport.database.external_request.Tables.ASSESSMENTS;

import org.jooq.Record;
import ru.sber.transport.database.external_request.enums.AssessmentType;
import ru.sber.transport.request.external.model.Assessment;
import ru.sber.transport.request.external.model.Assessments;

public class AssessmentsModel implements Assessments {

    private final Record source;

    public AssessmentsModel(Record source) {
        this.source = source;
    }

    @Override
    public Assessment getService() {
        final var type = source.get(ASSESSMENTS.TYPE);
        if (!AssessmentType.SERVICE.equals(type)) {
            return null;
        }
        return new AssessmentModel(source);
    }
}
