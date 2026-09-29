package ru.sberbank.ditsib.transport.request.human_readable_id.dao;

import org.springframework.stereotype.Repository;
import ru.sber.transport.humanreadableid.dao.AbstractRepository;
import ru.sberbank.ditsib.transport.request.human_readable_id.model.CompanySQRequest;


/**
 * CompanySQ repository
 */
@Repository
public interface CompanySQRepositoryRequest extends AbstractRepository<CompanySQRequest> {

}
