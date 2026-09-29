package ru.sberbank.ditsib.transport.vehicle.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.database.dao.EngineTypeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.dao.FuelTypeNameRepository;
import ru.sberbank.ditsib.transport.vehicle.database.dao.FuelTypeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.*;
import ru.sberbank.ditsib.transport.vehicle.database.model.EngineType_;
import ru.sberbank.ditsib.transport.vehicle.database.model.FuelType_;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.fueltype.FuelTypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.fueltype.FuelTypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.FuelTypeMapper;
import ru.sberbank.ditsib.transport.vehicle.messaging.sender.FuelTypeSender;
import ru.sberbank.ditsib.transport.vehicle.service.FuelTypeService;
import ru.sberbank.ditsib.transport.vehicle.service.validation.FuelTypeValidationService;

import java.util.List;
import java.util.UUID;

import static java.util.Collections.emptyList;
import static org.springframework.util.CollectionUtils.isEmpty;

@Service
@RequiredArgsConstructor
public class FuelTypeServiceImpl implements FuelTypeService {
    private final FuelTypeRepository repository;
    private final FuelTypeNameRepository fuelTypeNameRepository;
    private final FuelTypeMapper mapper;
    private final FuelTypeValidationService validationService;
    private final EngineTypeRepository engineTypeRepository;
    private final FuelTypeSender sender;

    @Override
    public FuelTypeDto get(UUID id) {
        return mapper.fueltTypeToFuelTypeDto(getOrThrow(id));
    }

    @Override
    @Transactional
    public void create(FuelTypeRequestDto requestDto) {
        var title = requestDto.title().trim();
        var engineType = getEngineType(requestDto.engineTypeId());
        validationService.checkIfFuelTypeAlreadyExists(title, engineType);
        var saved = repository.save(FuelType.builder()
                .title(title)
                .engineType(engineType)
                .build());
        saved.setFuelTypeNames(saveFuelTypeNames(requestDto, saved));
        sender.send(mapper.fuelTypeToFuelTypeMessage(saved, false));
    }

    @Override
    @Transactional
    public FuelTypeDto update(UUID id, FuelTypeRequestDto requestDto) {
        var foundFuelType = getOrThrow(id);
        var title = requestDto.title().trim();
        var engineType = getEngineType(requestDto.engineTypeId());
        repository.findByTitleAndEngineType(title, engineType)
                .filter(fuelType -> !fuelType.getId().equals(foundFuelType.getId()))
                .ifPresent(fuelType -> {
                    throw new EntityAlreadyExistsException(fuelType.getTitle(), fuelType.getId());
                });

        foundFuelType.setTitle(title);
        foundFuelType.setEngineType(engineType);
        foundFuelType.getFuelTypeNames().clear();
        foundFuelType.getFuelTypeNames().addAll(saveFuelTypeNames(requestDto, foundFuelType));
        sender.send(mapper.fuelTypeToFuelTypeMessage(foundFuelType, false));
        return mapper.fueltTypeToFuelTypeDto(foundFuelType);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        var foundFuelType = getOrThrow(id);
        repository.delete(foundFuelType);
        sender.send(mapper.fuelTypeToFuelTypeMessage(foundFuelType, true));
    }

    @Override
    public Page<FuelTypeDto> findAll(PageSettingDto pageSetting) {
        var typeSoring = FuelType_.ENGINE_TYPE.concat(".").concat(EngineType_.TITLE);
        var sorting = Sort.by(Sort.Direction.ASC, typeSoring).and(Sort.by(Sort.Direction.ASC, FuelType_.TITLE));
        PageImpl<FuelTypeDto> result;
        if (pageSetting != null) {
            var foundPageable = repository.findAllWithNamesAndEngineTypes(PageRequest.of(pageSetting.page(), pageSetting.size(), sorting));
            var allEntries = mapper.listFuelTypeToListFuelTypeDto(foundPageable.getContent());
            result = new PageImpl<>(allEntries, foundPageable.getPageable(), foundPageable.getTotalElements());
        } else {
            var foundEntries = repository.findAllWithNamesAndEngineTypes(sorting);
            var allEntries = mapper.listFuelTypeToListFuelTypeDto(foundEntries);
            result = new PageImpl<>(allEntries, PageRequest.of(0, allEntries.size()), allEntries.size());
        }
        return result;
    }

    private EngineType getEngineType(UUID engineTypeId) {
        return engineTypeRepository.findById(engineTypeId)
                .orElseThrow(() -> new EntityNotFoundException(EngineType.class, engineTypeId));
    }

    private FuelType getOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(FuelType.class, id));
    }

    private List<FuelTypeName> saveFuelTypeNames(FuelTypeRequestDto requestDto, FuelType saved) {
        if (!isEmpty(requestDto.possibleTitles())) {
            var processedNames = requestDto.possibleTitles().stream()
                    .filter(StringUtils::hasLength)
                    .map(String::trim)
                    .map(String::toUpperCase)
                    .distinct()
                    .toList();
            validationService.checkFuelTypeNamesAlreadyInUse(processedNames, saved.getId());
            var fuelTypeNames = processedNames.stream()
                    .map(name -> FuelTypeName.builder()
                            .fuelTypeId(saved.getId())
                            .name(name)
                            .build())
                    .toList();
            return fuelTypeNameRepository.saveAll(fuelTypeNames);
        }
        return emptyList();
    }
}