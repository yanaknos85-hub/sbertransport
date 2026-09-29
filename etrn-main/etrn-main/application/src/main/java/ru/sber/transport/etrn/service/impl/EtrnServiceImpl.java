package ru.sber.transport.etrn.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.etrn.database.dao.EtrnAuditRepository;
import ru.sber.transport.etrn.database.dao.EtrnRepository;
import ru.sber.transport.etrn.database.model.Etrn;
import ru.sber.transport.etrn.database.model.EtrnAudit;
import ru.sber.transport.etrn.dto.*;
import ru.sber.transport.etrn.exceptions.BadRequestException;
import ru.sber.transport.etrn.exceptions.EtrnNotFoundException;
import ru.sber.transport.etrn.mapper.EtrnMapper;
import ru.sber.transport.etrn.service.EtrnService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EtrnServiceImpl implements EtrnService {

    private final EtrnRepository etrnRepository;
    private final EtrnAuditRepository auditRepository;
    private final EtrnMapper mapper;

    @Override
    @Transactional
    public EtrnDto create(EtrnCreateRequest request) {
        log.debug("Создание ЭТрН: humanReadableId={}", request.humanReadableId());

        // Проверяем идемпотентность по humanReadableId
        if (etrnRepository.existsByHumanReadableId(request.humanReadableId())) {
            log.warn("ЭТрН с humanReadableId={} уже существует, идемпотентный возврат", request.humanReadableId());
            Etrn existing = etrnRepository.findByHumanReadableId(request.humanReadableId())
                    .orElseThrow();
            return mapper.toDto(existing);
        }

        Etrn entity = mapper.toEntity(request);
        log.debug("Подготовка сущности ЭТрН: статус={}, timeZone={}", entity.getStatus(), entity.getTimeZone());

        Etrn saved = etrnRepository.save(entity);
        log.debug("Сущность ЭТрН сохранена в БД: id={}", saved.getId());

        EtrnAudit audit = EtrnAudit.builder()
                .etrnId(saved.getId())
                .action("ЭТрН создана")
                .details("humanReadableId=" + saved.getHumanReadableId())
                .build();
        auditRepository.save(audit);
        log.debug("Запись в аудит сохранена: etrnId={}", saved.getId());

        log.info("ЭТрН {} создана (id={})", saved.getHumanReadableId(), saved.getId());
        return mapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EtrnDetailDto getById(UUID id) {
        log.debug("Получение ЭТрН по id={}", id);

        Etrn entity = etrnRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("ЭТрН с id={} не найдена", id);
                    return new EtrnNotFoundException(id.toString());
                });

        log.debug("ЭТрН найдена: humanReadableId={}", entity.getHumanReadableId());
        return mapper.toDetailDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EtrnJournalDto> list(SearchEtrnDto request) {
        log.debug("Поиск ЭТрН: humanReadableId={}, statusFilter={}, lockedByMe={}, page={}, size={}",
                request.humanReadableId(), request.statusFilter(), request.lockedByMe(),
                request.pageSetting().page(), request.pageSetting().size());

        var spec = (Specification<Etrn>) (root, query, cb) -> null;

        if (request.humanReadableId() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("humanReadableId"), request.humanReadableId()));
            log.debug("Фильтр по humanReadableId применён");
        }
        if (request.statusFilter() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), request.statusFilter()));
            log.debug("Фильтр по status={} применён", request.statusFilter());
        }
        if (request.lockedByMe() != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(
                            cb.function("jsonb_extract_path_text", String.class,
                                    root.get("lockInfo"), cb.literal("userId")),
                            request.lockedByMe().toString()
                    ));
            log.debug("Фильтр по lockedByMe={} применён", request.lockedByMe());
        }

        // Сортировка (если задана)
        Sort sort = Sort.unsorted();
        if (request.sortSetting() != null) {
            Sort.Direction direction = request.sortSetting().directionAsc()
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, request.sortSetting().property());
            log.debug("Сортировка: {} {}", direction, request.sortSetting().property());
        }

        Pageable pageable = PageRequest.of(
                request.pageSetting().page(),
                request.pageSetting().size(),
                sort
        );

        Page<EtrnJournalDto> result = etrnRepository.findAll(spec, pageable).map(mapper::toJournalDto);
        log.debug("Найдено {} записей, страница {}/{}",
                result.getTotalElements(),
                request.pageSetting().page(),
                result.getTotalPages());
        return result;
    }
}
