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
import ru.sberbank.ditsib.transport.vehicle.database.dao.TypeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Type;
import ru.sberbank.ditsib.transport.vehicle.database.model.Type_;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.type.TypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.type.TypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.TypeMapper;
import ru.sberbank.ditsib.transport.vehicle.service.TypeService;

import java.util.Optional;
import java.util.UUID;

/**
 * @author skakun-a
 */
@Service
@RequiredArgsConstructor
public class TypeServiceImpl implements TypeService {
    
    private final TypeRepository repository;
    private final TypeMapper mapper;
    
    @Override
    public TypeDto get(UUID id) {
        return mapper.typeToTypeDto(getOrThrow(id));
    }
    
    @Override
    @Transactional
    public TypeDto create(TypeRequestDto requestDto) {
        var title = requestDto.title().trim();
        checkIfBrandExists(title);
        var saved = repository.save(Type.builder()
                                        .title(title)
                                        .build());
        return mapper.typeToTypeDto(saved);
        
    }
    
    @Override
    @Transactional
    public TypeDto update(UUID id, TypeRequestDto requestDto) {
        var foundBrand = getOrThrow(id);
        var title = requestDto.title().trim();
        repository.findByTitle(title)
                  .filter(brand -> !brand.getId().equals(foundBrand.getId()))
                  .ifPresent(brand -> {
                      throw new EntityAlreadyExistsException(brand.getTitle(), brand.getId());
                  });
        foundBrand.setTitle(title);
        return mapper.typeToTypeDto(foundBrand);
    }
    
    @Override
    @Transactional
    public void delete(UUID id) {
        var type = getOrThrow(id);
        repository.delete(type);
    }
    
    private Type getOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(Type.class, id));
    }
    
    @Override
    public Page<TypeDto> findAll(PaginationCommonRequestDto paginationRequest) {
        var sorting = Sort.by(Type_.TITLE).ascending();
        var request = Optional.ofNullable(paginationRequest);
        var pageSetting = request.map(PaginationCommonRequestDto::pageSetting).orElse(null);
        if (pageSetting == null) {
            pageSetting = new PageSettingDto(0,20);
        }
        var pageRequest = PageRequest.of(pageSetting.page(), pageSetting.size(), sorting);
        Page<Type> foundPageable;
        var search = request.map(PaginationCommonRequestDto::search).orElse(null);
        if (search != null && StringUtils.isNotBlank(search.title())) {
            foundPageable = repository.findAllByTitleContainingIgnoreCase(search.title(), pageRequest);
        } else {
            foundPageable = repository.findAll(pageRequest);
        }
        var allEntries = mapper.listTypeToListTypeDto(foundPageable.getContent());
        return new PageImpl<>(allEntries, foundPageable.getPageable(), foundPageable.getTotalElements());
    }
    
    private void checkIfBrandExists(String title) {
        repository.findByTitle(title)
                  .ifPresent(entity -> {
                      throw new EntityAlreadyExistsException(entity.getTitle(), entity.getId());
                  });
    }
    
}
