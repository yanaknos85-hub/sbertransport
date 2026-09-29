package ru.sberbank.ditsib.transport.vehicle.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.database.dao.CategoryRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Category;
import ru.sberbank.ditsib.transport.vehicle.database.model.Category_;
import ru.sberbank.ditsib.transport.vehicle.dto.category.CategoryDto;
import ru.sberbank.ditsib.transport.vehicle.dto.category.CategoryRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.CategoryMapper;
import ru.sberbank.ditsib.transport.vehicle.service.CategoryService;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository repository;
    private final CategoryMapper mapper;
    
    @Override
    public CategoryDto get(UUID id) {
        return mapper.categoryToCategoryDto(getOrThrow(id));
    }
    
    @Override
    @Transactional
    public CategoryDto create(CategoryRequestDto requestDto) {
        var title = requestDto.title().trim();
        var category = requestDto.category().trim();
        checkIfCategoryExists(category, title);
        var saved = repository.save(Category.builder()
                                            .title(title)
                                            .category(category)
                                            .build());
        return mapper.categoryToCategoryDto(saved);
        
    }
    
    @Override
    @Transactional
    public CategoryDto update(UUID id, CategoryRequestDto requestDto) {
        var foundCategory = getOrThrow(id);
        var title = requestDto.title().trim();
        var category = requestDto.category().trim();
        repository.findByCategoryOrTitle(category, title)
                  .filter(categoryEnt -> !categoryEnt.getId().equals(foundCategory.getId()))
                  .ifPresent(categoryEnt -> {
                      throw new EntityAlreadyExistsException(categoryEnt.getTitle(), categoryEnt.getId());
                  });
        foundCategory.setTitle(title);
        foundCategory.setCategory(category);
        return mapper.categoryToCategoryDto(foundCategory);
    }
    
    private Category getOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(Category.class, id));
    }
    
    @Override
    @Transactional
    public void delete(UUID id) {
        var category = getOrThrow(id);
        repository.delete(category);
    }
    
    @Override
    public Page<CategoryDto> findAll(PageSettingDto pageSetting) {
        PageImpl<CategoryDto> result;
        var sorting = Sort.by(Category_.CATEGORY).ascending();
        if (pageSetting != null) {
            var foundPageable = repository.findAll(PageRequest.of(pageSetting.page(),
                                                                  pageSetting.size(),
                                                                  sorting));
            var allEntries = mapper.listCategoryToCategoryDto(foundPageable.getContent());
            result = new PageImpl<>(allEntries, foundPageable.getPageable(), foundPageable.getTotalElements());
        } else {
            var foundEntries = repository.findAll(sorting);
            var allEntries = mapper.listCategoryToCategoryDto(foundEntries);
            result = new PageImpl<>(allEntries, PageRequest.of(0, allEntries.size()), allEntries.size());
        }
        return result;
        
    }
    
    private void checkIfCategoryExists(String category, String title) {
        repository.findByCategoryOrTitle(category, title)
                  .ifPresent(entity -> {
                      throw new EntityAlreadyExistsException(
                              String.format("Category: %s, title: %s  ", entity.getCategory(), entity.getTitle()), entity.getId());
                  });
    }
    
}
