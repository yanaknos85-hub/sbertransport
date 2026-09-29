package ru.sber.transport.etrn.service;

import org.springframework.data.domain.Page;
import ru.sber.transport.etrn.dto.*;

import java.util.UUID;

public interface EtrnService {

    EtrnDto create(EtrnCreateRequest request);

    EtrnDetailDto getById(UUID id);

    Page<EtrnJournalDto> list(SearchEtrnDto request);
}