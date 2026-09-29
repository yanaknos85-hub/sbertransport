package ru.sber.transport.request.external.providers.order.model;

import static ru.sber.transport.database.external_request.Tables.ASSESSMENTS;

import org.jooq.Record;
import ru.sber.transport.request.external.model.Assessment;

/**
 * Модель оценки
 */
public class AssessmentModel implements Assessment {

    private final Record source;

    public AssessmentModel(Record source) {
        this.source = source;
    }

    @Override
    public String getComment() {
        return source.get(ASSESSMENTS.COMMENT);
    }

    @Override
    public byte getRating() {
        return source.get(ASSESSMENTS.RATING, Byte.class);
    }
}
