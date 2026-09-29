package ru.sber.transport.telemechanic.human_readable_id.service;

import org.springframework.stereotype.Service;
import ru.sber.transport.humanreadableid.model.interfaces.Prefix;
import ru.sber.transport.humanreadableid.service.SQCreator;
import ru.sber.transport.telemechanic.database.human_readable_id.model.CompanySQ;


@Service
public class SQCreatorImpl implements SQCreator<CompanySQ> {
    /**
     * Create concrete instance (CompanySQEmployee) of BaseCompanySQ
     *
     * @param prefix         .
     * @param organizationId ID
     * @return instance
     */
    @Override
    public CompanySQ createCompanySQ(Prefix prefix, Long organizationId, int count) {
        return CompanySQ.builder().prefix(prefix.name()).orgDigitId(organizationId).sq((long) count).build();
    }
}
