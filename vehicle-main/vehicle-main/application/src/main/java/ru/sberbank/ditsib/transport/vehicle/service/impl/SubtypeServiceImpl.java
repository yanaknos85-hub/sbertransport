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
import ru.sberbank.ditsib.transport.vehicle.database.dao.SubtypeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.dao.TypeRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Subtype;
import ru.sberbank.ditsib.transport.vehicle.database.model.Subtype_;
import ru.sberbank.ditsib.transport.vehicle.database.model.Type;
import ru.sberbank.ditsib.transport.vehicle.database.model.Type_;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.subtype.SubtypeDto;
import ru.sberbank.ditsib.transport.vehicle.dto.subtype.SubtypeRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.SubtypeMapper;
import ru.sberbank.ditsib.transport.vehicle.service.SubtypeService;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubtypeServiceImpl implements SubtypeService {
    private final SubtypeRepository repository;
    private final SubtypeMapper mapper;
    private final TypeRepository typeRepository;
    
    @Override
    public SubtypeDto get(UUID id) {
        return mapper.subtypeToSubtypeDto(getOrThrow(id));
    }
    
    @Override
    @Transactional
    public SubtypeDto create(SubtypeRequestDto requestDto) {
        var title = requestDto.title().trim();
        checkIfSubtypeAlreadyExists(title);
        var type = getType(requestDto.typeId());
        var saved = repository.saveAndFlush(Subtype.builder()
                                                   .title(title)
                                                   .type(type)
                                                   .build());
        return mapper.subtypeToSubtypeDto(saved);
    }
    
    @Override
    @Transactional
    public SubtypeDto update(UUID id, SubtypeRequestDto requestDto) {
        var foundSubtype = getOrThrow(id);
        var title = requestDto.title().trim();
        repository.findByTitle(title)
                  .filter(subtype -> !subtype.getId().equals(foundSubtype.getId()))
                  .ifPresent(subtype -> {
                      throw new EntityAlreadyExistsException(subtype.getTitle(), subtype.getId());
                  });
        var type = getType(requestDto.typeId());
        foundSubtype.setTitle(title);
        foundSubtype.setType(type);
        return mapper.subtypeToSubtypeDto(foundSubtype);
    }
    
    @Override
    @Transactional
    public void delete(UUID id) {
        var foundSubtype = getOrThrow(id);
        repository.delete(foundSubtype);
    }
    
    @Override
    public Page<SubtypeDto> findAll(PaginationCommonRequestDto paginationRequest) {
        var typeSoring = Subtype_.TYPE.concat(".").concat(Type_.TITLE);
        var sorting = Sort.by(Sort.Direction.ASC, typeSoring, Subtype_.TITLE);
        var request = Optional.ofNullable(paginationRequest);
        var pageSetting = request.map(PaginationCommonRequestDto::pageSetting).orElse(null);
        if (pageSetting == null) {
            pageSetting = new PageSettingDto(0, 20);
        }
        var pageRequest = PageRequest.of(pageSetting.page(), pageSetting.size(), sorting);
        Page<Subtype> foundPageable;
        var search = request.map(PaginationCommonRequestDto::search).orElse(null);
        if (search != null && StringUtils.isNotBlank(search.title())) {
            foundPageable = repository.findAllByTitleContainingIgnoreCase(search.title(), pageRequest);
        } else {
            foundPageable = repository.findAll(pageRequest);
        }
        var allEntries = mapper.listSubtypeToListSubtypeDto(foundPageable.getContent());
        return new PageImpl<>(allEntries, foundPageable.getPageable(), foundPageable.getTotalElements());
    }
    
    private Type getType(UUID typeId) {
        return typeRepository.findById(typeId)
                             .orElseThrow(() -> new EntityNotFoundException(Type.class, typeId));
    }
    
    private Subtype getOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(Subtype.class, id));
    }
    
    private void checkIfSubtypeAlreadyExists(String title) {
        repository.findByTitle(title)
                  .ifPresent(entity -> {
                      throw new EntityAlreadyExistsException(entity.getTitle(), entity.getId());
                  });
    }
}
