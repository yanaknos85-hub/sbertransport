package ru.sberbank.ditsib.transport.vehicle.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.database.dao.BodyTypeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.BodyType;
import ru.sberbank.ditsib.transport.vehicle.database.model.BodyType_;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.bodytype.BodyTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.bodytype.BodyTypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.BodyTypeMapper;
import ru.sberbank.ditsib.transport.vehicle.service.BodyTypeService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BodyTypeServiceImpl implements BodyTypeService {

    private final BodyTypeRepository repository;
    private final BodyTypeMapper mapper;

    @Override
    public BodyTypeDto get(UUID id) {
        return mapper.bodyTypeToBodyTypeDto(getOrThrow(id));
    }

    @Override
    @Transactional
    public BodyTypeDto create(BodyTypeRequestDto requestDto) {
        var title = requestDto.title().trim();
        checkIfAlreadyExists(title);
        var saved = repository.save(BodyType.builder()
                .title(title)
                .build());
        return mapper.bodyTypeToBodyTypeDto(saved);

    }

    @Override
    @Transactional
    public BodyTypeDto update(UUID id, BodyTypeRequestDto requestDto) {
        var foundEntity = getOrThrow(id);
        var title = requestDto.title().trim();
        repository.findByTitle(title)
                .filter(entity -> !entity.getId().equals(foundEntity.getId()))
                .ifPresent(entity -> {
                    throw new EntityAlreadyExistsException(entity.getTitle(), entity.getId());
                });
        foundEntity.setTitle(title);
        return mapper.bodyTypeToBodyTypeDto(foundEntity);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        var foundEntity = getOrThrow(id);
        repository.delete(foundEntity);
    }

    private BodyType getOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(BodyType.class, id));
    }

    @Override
    public Page<BodyTypeDto> findAll(PaginationCommonRequestDto paginationRequest) {
        var sorting = Sort.by(BodyType_.TITLE).ascending();
        var request = Optional.ofNullable(paginationRequest);
        var pageSetting = request.map(PaginationCommonRequestDto::pageSetting).orElse(null);
        if (pageSetting == null) {
            pageSetting = new PageSettingDto(0,20);
        }
        var pageRequest = PageRequest.of(pageSetting.page(), pageSetting.size(), sorting);
        Page<BodyType> foundPageable;
        var search = request.map(PaginationCommonRequestDto::search).orElse(null);
        if (search != null && StringUtils.isNotBlank(search.title())) {
            foundPageable = repository.findAllByTitleContainingIgnoreCase(search.title(), pageRequest);
        } else {
            foundPageable = repository.findAll(pageRequest);
        }
        var allEntries = mapper.listBodyTypeToBodyListBodyTypeDto(foundPageable.getContent());
        return new PageImpl<>(allEntries, foundPageable.getPageable(), foundPageable.getTotalElements());
    }

    private void checkIfAlreadyExists(String title) {
        repository.findByTitle(title)
                .ifPresent(entity -> {
                    throw new EntityAlreadyExistsException(entity.getTitle(), entity.getId());
                });
    }

}
