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
import ru.sberbank.ditsib.transport.vehicle.database.dao.TransmissionTypeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.TransmissionType;
import ru.sberbank.ditsib.transport.vehicle.database.model.TransmissionType_;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transmissiontype.TransmissionTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transmissiontype.TransmissionTypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.TransmissionTypeMapper;
import ru.sberbank.ditsib.transport.vehicle.service.TransmissionTypeService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransmissionTypeServiceImpl implements TransmissionTypeService {
    private final TransmissionTypeRepository repository;
    private final TransmissionTypeMapper mapper;

    @Override
    public TransmissionTypeDto get(UUID id) {
        return mapper.transmissionTypeToTransmissionTypeDto(getOrThrow(id));
    }

    @Override
    @Transactional
    public TransmissionTypeDto create(TransmissionTypeRequestDto requestDto) {
        var title = requestDto.title().trim();
        checkIfAlreadyExists(title);
        var saved = repository.save(TransmissionType.builder()
                .title(title)
                .build());
        return mapper.transmissionTypeToTransmissionTypeDto(saved);

    }

    @Override
    @Transactional
    public TransmissionTypeDto update(UUID id, TransmissionTypeRequestDto requestDto) {
        var foundEntity = getOrThrow(id);
        var title = requestDto.title().trim();
        repository.findByTitle(title)
                .filter(entity -> !entity.getId().equals(foundEntity.getId()))
                .ifPresent(entity -> {
                    throw new EntityAlreadyExistsException(entity.getTitle(), entity.getId());
                });
        foundEntity.setTitle(title);
        return mapper.transmissionTypeToTransmissionTypeDto(foundEntity);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        var foundEntity = getOrThrow(id);
        repository.delete(foundEntity);
    }

    private TransmissionType getOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(TransmissionType.class, id));
    }

    @Override
    public Page<TransmissionTypeDto> findAll(PaginationCommonRequestDto paginationRequest) {
        var sorting = Sort.by(TransmissionType_.TITLE).ascending();
        var request = Optional.ofNullable(paginationRequest);
        var pageSetting = request.map(PaginationCommonRequestDto::pageSetting).orElse(null);
        if (pageSetting == null) {
            pageSetting = new PageSettingDto(0,20);
        }
        var pageRequest = PageRequest.of(pageSetting.page(), pageSetting.size(), sorting);
        Page<TransmissionType> foundPageable;
        var search = request.map(PaginationCommonRequestDto::search).orElse(null);
        if (search != null && StringUtils.isNotBlank(search.title())) {
            foundPageable = repository.findAllByTitleContainingIgnoreCase(search.title(), pageRequest);
        } else {
            foundPageable = repository.findAll(pageRequest);
        }
        var allEntries = mapper.listTransmissionTypeToListTransmissionTypeDto(foundPageable.getContent());
        return new PageImpl<>(allEntries, foundPageable.getPageable(), foundPageable.getTotalElements());
    }

    private void checkIfAlreadyExists(String title) {
        repository.findByTitle(title)
                .ifPresent(entity -> {
                    throw new EntityAlreadyExistsException(entity.getTitle(), entity.getId());
                });
    }
    
}
