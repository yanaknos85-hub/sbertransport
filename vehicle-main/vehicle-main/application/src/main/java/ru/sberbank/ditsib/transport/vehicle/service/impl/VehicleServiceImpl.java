package ru.sberbank.ditsib.transport.vehicle.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.database.dao.*;
import ru.sberbank.ditsib.transport.vehicle.database.model.*;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PageVehicleWithFilters;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.*;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.exception.FuelEngineTypesRelationValidationException;
import ru.sberbank.ditsib.transport.vehicle.helper.VehicleSpecificationHelper;
import ru.sberbank.ditsib.transport.vehicle.mapper.VehicleFilterMapper;
import ru.sberbank.ditsib.transport.vehicle.mapper.VehicleMapper;
import ru.sberbank.ditsib.transport.vehicle.service.VehicleService;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {
    private final VehicleRepository vehicleRepository;
    private final ModelRepository modelRepository;
    private final FuelTypeRepository fuelTypeRepository;
    private final CategoryRepository categoryRepository;
    private final DriveRepository driveRepository;
    private final BodyTypeRepository bodyTypeRepository;
    private final TransmissionTypeRepository transmissionTypeRepository;
    private final WheelSizeRepository wheelSizeRepository;
    private final EngineTypeRepository engineTypeRepository;
    private final VehicleMapper mapper;
    private final VehicleFilterMapper vehicleFilterMapper;

    @Override
    @Transactional
    public void create(VehicleRequestDto requestDto) {
        var vehicle = mapper.vehicleRequestDtoToVehicle(requestDto);

        var model = getModel(requestDto.modelId());
        vehicle.setModel(model);

        var fuelTypes = getFuelTypes(requestDto.fuelTypeIds());
        vehicle.setFuelTypes(fuelTypes);

        checkConstraintEngineTypeToFuelType(fuelTypes, requestDto.engineTypeId());

        var engineType = getEngineType(requestDto.engineTypeId());
        vehicle.setEngineType(engineType);

        var category = getCategory(requestDto.categoryId());
        vehicle.setCategory(category);

        var drive = getDrive(requestDto.driveId());
        vehicle.setDrive(drive);

        var transmissionType = getTransmissionType(requestDto.transmissionTypeId());
        vehicle.setTransmissionType(transmissionType);

        var bodyType = getBodyType(requestDto.bodyTypeId());
        vehicle.setBodyType(bodyType);

        var frontWheelSize = getWheelSize(requestDto.frontWheelSizeId());
        vehicle.setFrontWheelSize(frontWheelSize);

        var rearWheelSize = getWheelSize(requestDto.rearWheelSizeId());
        vehicle.setRearWheelSize(rearWheelSize);

        if (vehicleRepository.existsByAllFields(vehicle)) {
            throw new EntityAlreadyExistsException(Vehicle.class);
        }

        vehicleRepository.saveAndFlush(vehicle);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        var vehicle = getOrThrow(id);
        vehicleRepository.delete(vehicle);
    }

    @Override
    public Page<VehicleShortDto> findAll(PageSettingDto pageSetting) {
        var pageableRequest = Optional.ofNullable(pageSetting)
                .map(ps -> PageRequest.of(ps.page(), ps.size()))
                .orElseGet(() -> PageRequest.of(0, 20));
        var pageableResult = vehicleRepository.getAllProjections(pageableRequest);
        var mappedDtos = mapper.listVehicleProjectionToListVehicleShortDto(pageableResult.getContent());
        return new PageImpl<>(mappedDtos, pageableResult.getPageable(), pageableResult.getTotalElements());
    }

    @Override
    public PageVehicleWithFilters search(VehicleSearchDto searchDto) {
        var pageSetting = searchDto.pageSetting();
        var pageRequest = Objects.nonNull(pageSetting) ?
                PageRequest.of(pageSetting.page(), pageSetting.size()) :
                PageRequest.of(0, 20);
        var specificationRequest = VehicleSpecificationHelper.prepareSearchingRequest(searchDto);
        var pageableResult = vehicleRepository.findAll(specificationRequest, pageRequest);
        var mappedEntries = mapper.listVehicleToListVehicleShortDto(pageableResult.getContent());
        var filters = getFiltersValues(searchDto);
        return PageVehicleWithFilters.builder()
                .content(mappedEntries)
                .totalElements(pageableResult.getTotalElements())
                .number(pageableResult.getNumber())
                .totalPages(pageableResult.getTotalPages())
                .numberOfElements(pageableResult.getNumberOfElements())
                .size(pageableResult.getSize())
                .filters(filters)
                .build();
    }

    private VehicleSearchFilters getFiltersValues(VehicleSearchDto searchDto) {
        return vehicleFilterMapper.vehicleSearchFiltersProjectionToVehicleSearchFilters(vehicleRepository.getFilters(searchDto));
    }

    private Vehicle getOrThrow(UUID id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Vehicle.class, id));
    }

    private Set<FuelType> getFuelTypes(Set<UUID> fuelTypeIds) {
        return new HashSet<>(fuelTypeRepository.findAllById(fuelTypeIds));
    }

    private EngineType getEngineType(UUID engineTypeId) {
        return engineTypeRepository.findById(engineTypeId)
                .orElseThrow(() -> new EntityNotFoundException(EngineType.class, engineTypeId));
    }

    private Drive getDrive(UUID driveId) {
        return driveRepository.findById(driveId)
                .orElseThrow(() -> new EntityNotFoundException(Drive.class, driveId));
    }

    private Category getCategory(UUID categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException(Category.class, categoryId));
    }

    private FuelType getFuelType(UUID fuelTypeId) {
        return fuelTypeRepository.findById(fuelTypeId)
                .orElseThrow(() -> new EntityNotFoundException(FuelType.class, fuelTypeId));
    }

    private Model getModel(UUID modelId) {
        return modelRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException(Model.class, modelId));
    }

    private TransmissionType getTransmissionType(UUID uuid) {
        return transmissionTypeRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException(TransmissionType.class, uuid));
    }

    private BodyType getBodyType(UUID uuid) {
        return bodyTypeRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException(BodyType.class, uuid));
    }

    private WheelSize getWheelSize(UUID uuid) {
        return wheelSizeRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException(WheelSize.class, uuid));
    }

    private void checkConstraintEngineTypeToFuelType(Set<FuelType> fuelTypes, UUID engineTypeId) {
        var engineTypes = fuelTypes.stream().map(fuelType -> fuelType.getEngineType().getId()).collect(Collectors.toSet());

        if (engineTypes.size() > 1 || !engineTypes.contains(engineTypeId)) {
            throw new FuelEngineTypesRelationValidationException("Переданные типы топлива не соответствуют типу двигателя");
        }
    }
}
