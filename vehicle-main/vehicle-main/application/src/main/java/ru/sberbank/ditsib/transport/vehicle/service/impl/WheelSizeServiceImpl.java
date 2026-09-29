package ru.sberbank.ditsib.transport.vehicle.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.database.dao.WheelSizeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.WheelSize;
import ru.sberbank.ditsib.transport.vehicle.database.model.WheelSize_;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.wheelsize.WheelSizeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.wheelsize.WheelSizeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.WheelSizeMapper;
import ru.sberbank.ditsib.transport.vehicle.service.WheelSizeService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WheelSizeServiceImpl implements WheelSizeService {
    private final WheelSizeRepository repository;
    private final WheelSizeMapper mapper;

    @Override
    public WheelSizeDto get(UUID id) {
        return mapper.wheeSizeToWheelSizeDto(getOrThrow(id));
    }

    @Override
    @Transactional
    public WheelSizeDto create(WheelSizeRequestDto requestDto) {
        var title = requestDto.title().trim();
        checkIfAlreadyExists(title);
        var saved = repository.save(WheelSize.builder()
                .title(title)
                .build());
        return mapper.wheeSizeToWheelSizeDto(saved);

    }

    @Override
    @Transactional
    public WheelSizeDto update(UUID id, WheelSizeRequestDto requestDto) {
        var foundEntity = getOrThrow(id);
        var title = requestDto.title().trim();
        repository.findByTitle(title)
                .filter(entity -> !entity.getId().equals(foundEntity.getId()))
                .ifPresent(entity -> {
                    throw new EntityAlreadyExistsException(entity.getTitle(), entity.getId());
                });
        foundEntity.setTitle(title);
        return mapper.wheeSizeToWheelSizeDto(foundEntity);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        var foundEntity = getOrThrow(id);
        repository.delete(foundEntity);
    }

    private WheelSize getOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(WheelSize.class, id));
    }

    @Override
    public Page<WheelSizeDto> findAll(PaginationCommonRequestDto paginationRequest) {
        var sorting = Sort.by(WheelSize_.TITLE).ascending();
        var request = Optional.ofNullable(paginationRequest);
        var pageSetting = request.map(PaginationCommonRequestDto::pageSetting).orElse(null);
        if (pageSetting == null) {
            pageSetting = new PageSettingDto(0,20);
        }
        var pageRequest = PageRequest.of(pageSetting.page(), pageSetting.size(), sorting);
        Page<WheelSize> foundPageable;
        var search = request.map(PaginationCommonRequestDto::search).orElse(null);
        if (search != null && StringUtils.isNotBlank(search.title())) {
            foundPageable = repository.findAllByTitleContainingIgnoreCase(search.title(), pageRequest);
        } else {
            foundPageable = repository.findAll(pageRequest);
        }
        var allEntries = mapper.listWheelSizeToListWheelSizeDto(foundPageable.getContent());
        return new PageImpl<>(allEntries, foundPageable.getPageable(), foundPageable.getTotalElements());
    }

    private void checkIfAlreadyExists(String title) {
        repository.findByTitle(title)
                .ifPresent(entity -> {
                    throw new EntityAlreadyExistsException(entity.getTitle(), entity.getId());
                });
    }

}
