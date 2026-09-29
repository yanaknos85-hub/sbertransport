package ru.sber.transport.request.external.providers.order;

import static ru.sber.transport.database.external_request.Tables.ASSESSMENTS;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.business.providers.AssessmnentProvider;
import ru.sber.transport.database.external_request.tables.Assessments;
import ru.sber.transport.database.external_request.tables.records.AssessmentsRecord;

@Slf4j
@RequiredArgsConstructor
public class AssessmentDataProviderImpl implements AssessmnentProvider,
    JooqRepository<Assessments, AssessmentsRecord, UUID> {

    @Override
    @Transactional
    public void deleteForRequest(UUID requestId) {
        context().delete(table())
            .where(table().ORDER_ID.eq(requestId))
            .execute();
    }

    @Override
    @Transactional
    public Assessments table() {
        return ASSESSMENTS;
    }

}
