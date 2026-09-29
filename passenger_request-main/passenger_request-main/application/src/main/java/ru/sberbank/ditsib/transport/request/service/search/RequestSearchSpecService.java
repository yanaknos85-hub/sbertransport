package ru.sberbank.ditsib.transport.request.service.search;

import org.springframework.data.jpa.domain.Specification;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.dto.RequestSearchDTO;

public interface RequestSearchSpecService<T extends Request> {

    <N extends RequestSearchDTO> Specification<T> getSpec(N requestSearchDTO);
}
