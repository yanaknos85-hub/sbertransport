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
import ru.sberbank.ditsib.transport.vehicle.database.model.Brand;
import ru.sberbank.ditsib.transport.vehicle.database.model.Brand_;
import ru.sberbank.ditsib.transport.vehicle.dto.brand.BrandDto;
import ru.sberbank.ditsib.transport.vehicle.dto.brand.BrandRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.BrandMapper;
import ru.sberbank.ditsib.transport.vehicle.service.BrandService;

import java.util.Optional;
import java.util.UUID;

/**
 * @author skakun-a
 */
@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {
    
    private final BrandRepository repository;
    private final BrandMapper mapper;
    
    @Override
    public BrandDto get(UUID id) {
        return mapper.brandToBrandDto(getOrThrow(id));
    }
    
    @Override
    @Transactional
    public BrandDto create(BrandRequestDto requestDto) {
        var title = requestDto.title().trim();
        checkIfBrandExists(title);
        var saved = repository.save(Brand.builder()
                                         .title(title)
                                         .build());
        return mapper.brandToBrandDto(saved);
        
    }
    
    @Override
    @Transactional
    public BrandDto update(UUID id, BrandRequestDto requestDto) {
        var foundBrand = getOrThrow(id);
        var title = requestDto.title().trim();
        repository.findByTitle(title)
                  .filter(brand -> !brand.getId().equals(foundBrand.getId()))
                  .ifPresent(brand -> {
                      throw new EntityAlreadyExistsException(brand.getTitle(), brand.getId());
                  });
        foundBrand.setTitle(title);
        return mapper.brandToBrandDto(foundBrand);
    }
    
    private Brand getOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(Brand.class, id));
    }
    
    @Override
    @Transactional
    public void delete(UUID id) {
        var brand = getOrThrow(id);
        repository.delete(brand);
    }
    
    @Override
    public Page<BrandDto> findAll(PaginationCommonRequestDto paginationRequest) {
        var sorting = Sort.by(Brand_.TITLE).ascending();
        var request = Optional.ofNullable(paginationRequest);
        var pageSetting = request.map(PaginationCommonRequestDto::pageSetting).orElse(null);
        if (pageSetting == null) {
            pageSetting = new PageSettingDto(0, 20);
        }
        Page<Brand> foundPageable;
        var search = request.map(PaginationCommonRequestDto::search).orElse(null);
        var pageRequest = PageRequest.of(pageSetting.page(), pageSetting.size(), sorting);
        if (search != null && StringUtils.isNotBlank(search.title())) {
            foundPageable = repository.findAllByTitleContainingIgnoreCase(search.title(), pageRequest);
        } else {
            foundPageable = repository.findAll(pageRequest);
        }
        
        var allEntries = mapper.listBrandToBrandDto(foundPageable.getContent());
        return new PageImpl<>(allEntries, foundPageable.getPageable(), foundPageable.getTotalElements());
    }
    
    private void checkIfBrandExists(String title) {
        repository.findByTitle(title)
                  .ifPresent(entity -> {
                      throw new EntityAlreadyExistsException(entity.getTitle(), entity.getId());
                  });
    }
    
}
