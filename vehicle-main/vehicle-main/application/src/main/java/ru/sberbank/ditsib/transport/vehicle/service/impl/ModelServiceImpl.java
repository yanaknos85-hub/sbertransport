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
import ru.sberbank.ditsib.transport.vehicle.database.dao.BrandRepository;
import ru.sberbank.ditsib.transport.vehicle.database.dao.ModelRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Brand;
import ru.sberbank.ditsib.transport.vehicle.database.model.Brand_;
import ru.sberbank.ditsib.transport.vehicle.database.model.Model;
import ru.sberbank.ditsib.transport.vehicle.database.model.Model_;
import ru.sberbank.ditsib.transport.vehicle.dto.model.ModelDto;
import ru.sberbank.ditsib.transport.vehicle.dto.model.ModelRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.ModelMapper;
import ru.sberbank.ditsib.transport.vehicle.service.ModelService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ModelServiceImpl implements ModelService {
    private final ModelRepository repository;
    private final ModelMapper mapper;
    private final BrandRepository brandRepository;
    
    @Override
    public ModelDto get(UUID id) {
        return mapper.modelToModelDto(getOrThrow(id));
    }
    
    @Override
    @Transactional
    public ModelDto create(ModelRequestDto requestDto) {
        var title = requestDto.title().trim();
        checkIfModelAlreadyExists(title);
        var brand = getBrand(requestDto.brandId());
        var saved = repository.saveAndFlush(Model.builder()
                                          .title(title)
                                          .brand(brand)
                                          .build());
        return mapper.modelToModelDto(saved);
    }
    
    @Override
    @Transactional
    public ModelDto update(UUID id, ModelRequestDto requestDto) {
        var foundModel = getOrThrow(id);
        var title = requestDto.title().trim();
        repository.findByTitle(title)
                  .filter(model -> !model.getId().equals(foundModel.getId()))
                  .ifPresent(model -> {
                      throw new EntityAlreadyExistsException(model.getTitle(), model.getId());
                  });
        var brand = getBrand(requestDto.brandId());
        foundModel.setTitle(title);
        foundModel.setBrand(brand);
        return mapper.modelToModelDto(foundModel);
    }
    
    @Override
    @Transactional
    public void delete(UUID id) {
        var foundModel = getOrThrow(id);
        repository.delete(foundModel);
    }
    
    @Override
    public Page<ModelDto> findAll(PaginationCommonRequestDto paginationRequest) {
        var brandSorting = Model_.BRAND.concat(".").concat(Brand_.TITLE);
        var sorting = Sort.by(Sort.Direction.ASC, brandSorting, Model_.TITLE);
        PageImpl<ModelDto> result;
        var request = Optional.ofNullable(paginationRequest);
        var pageSetting = request.map(PaginationCommonRequestDto::pageSetting).orElse(null);
        if (pageSetting != null) {
            var pageable = PageRequest.of(pageSetting.page(), pageSetting.size(), sorting);
            Page<Model> foundPageable;
            var search = request.map(PaginationCommonRequestDto::search).orElse(null);
            if (search != null && StringUtils.isNotBlank(search.title())) {
                foundPageable = repository.findAllByTitleContainingIgnoreCase(search.title(), pageable);
            } else {
                foundPageable = repository.findAll(pageable);
            }
            var allEntries = mapper.listModelToListModelDto(foundPageable.getContent());
            result = new PageImpl<>(allEntries, foundPageable.getPageable(), foundPageable.getTotalElements());
        } else {
            var foundEntries = repository.findAll(sorting);
            var allEntries = mapper.listModelToListModelDto(foundEntries);
            result = new PageImpl<>(allEntries, PageRequest.of(0, allEntries.size()), allEntries.size());
        }
        return result;
    }
    
    private Brand getBrand(UUID typeId) {
        return brandRepository.findById(typeId)
                              .orElseThrow(() -> new EntityNotFoundException(Brand.class, typeId));
    }
    
    private Model getOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(Model.class, id));
    }
    
    private void checkIfModelAlreadyExists(String title) {
        repository.findByTitle(title)
                .ifPresent(entity -> {
                    throw new EntityAlreadyExistsException(entity.getTitle(), entity.getId());
                });
    }
}
