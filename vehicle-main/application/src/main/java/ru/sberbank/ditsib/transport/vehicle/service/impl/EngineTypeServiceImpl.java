package ru.sberbank.ditsib.transport.vehicle.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.database.dao.EngineTypeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.EngineType;
import ru.sberbank.ditsib.transport.vehicle.database.model.EngineType_;
import ru.sberbank.ditsib.transport.vehicle.dto.enginetype.EngineTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.enginetype.EngineTypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.EngineTypeMapper;
import ru.sberbank.ditsib.transport.vehicle.messaging.sender.EngineTypeSender;
import ru.sberbank.ditsib.transport.vehicle.service.EngineTypeService;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Service
@RequiredArgsConstructor
public class EngineTypeServiceImpl implements EngineTypeService {

    private final EngineTypeRepository repository;
    private final EngineTypeSender sender;
    private final EngineTypeMapper mapper;

    @Override
    public EngineTypeDto get(UUID id) {
        return mapper.engineTypeToEngineTypeDto(getOrThrow(id));
    }

    @Override
    @Transactional
    public EngineTypeDto create(EngineTypeRequestDto requestDto) {
        var title = requestDto.title().trim();
        checkIfEngineTypeExists(title);
        var saved = repository.save(EngineType.builder()
                .title(title)
                .build());
        sender.send(mapper.engineTypeToEngineTypeMessage(saved, false));
        return mapper.engineTypeToEngineTypeDto(saved);

    }

    @Override
    @Transactional
    public EngineTypeDto update(UUID id, EngineTypeRequestDto requestDto) {
        var frountEngineType = getOrThrow(id);
        var title = requestDto.title().trim();
        repository.findByTitle(title)
                .filter(engineType -> !engineType.getId().equals(frountEngineType.getId()))
                .ifPresent(engineType -> {
                    throw new EntityAlreadyExistsException(engineType.getTitle(), engineType.getId());
                });
        frountEngineType.setTitle(title);
        sender.send(mapper.engineTypeToEngineTypeMessage(frountEngineType, false));
        return mapper.engineTypeToEngineTypeDto(frountEngineType);
    }

    private EngineType getOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(EngineType.class, id));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        var engineType = getOrThrow(id);
        repository.delete(engineType);
        sender.send(mapper.engineTypeToEngineTypeMessage(engineType, true));
    }

    @Override
    public Page<EngineTypeDto> findAll(PageSettingDto pageSetting) {
        var sorting = Sort.by(EngineType_.TITLE).ascending();
        PageImpl<EngineTypeDto> result;
        if (pageSetting != null) {
            var foundPageable = repository.findAll(PageRequest.of(pageSetting.page(), pageSetting.size(), sorting));
            var allEntries = mapper.listEngineTypeToListEngineTypeDro(foundPageable.getContent());
            result = new PageImpl<>(allEntries, foundPageable.getPageable(), foundPageable.getTotalElements());
        } else {
            var foundEntries = repository.findAll(sorting);
            var allEntries = mapper.listEngineTypeToListEngineTypeDro(foundEntries);
            result = new PageImpl<>(allEntries, PageRequest.of(0, allEntries.size()), allEntries.size());
        }
        return result;
    }

    private void checkIfEngineTypeExists(String title) {
        repository.findByTitle(title)
                .ifPresent(entity -> {
                    throw new EntityAlreadyExistsException(entity.getTitle(), entity.getId());
                });
    }

}
