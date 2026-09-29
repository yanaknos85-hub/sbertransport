package ru.sberbank.ditsib.transport.vehicle.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.database.dao.DriveRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Drive;
import ru.sberbank.ditsib.transport.vehicle.database.model.Drive_;
import ru.sberbank.ditsib.transport.vehicle.dto.drive.DriveDto;
import ru.sberbank.ditsib.transport.vehicle.dto.drive.DriveRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.DriveMapper;
import ru.sberbank.ditsib.transport.vehicle.service.DriveService;

import java.util.UUID;

/**
 * @author skakun-a
 */
@Service
@RequiredArgsConstructor
public class DriveServiceImpl implements DriveService {
    
    private final DriveRepository repository;
    private final DriveMapper mapper;
    
    @Override
    public DriveDto get(UUID id) {
        return mapper.driveToDriveDto(getOrThrow(id));
    }
    
    @Override
    @Transactional
    public DriveDto create(DriveRequestDto requestDto) {
        var title = requestDto.title().trim();
        checkIfDriveExists(title);
        var saved = repository.save(Drive.builder()
                                         .title(title)
                                         .build());
        return mapper.driveToDriveDto(saved);
        
    }
    
    @Override
    @Transactional
    public DriveDto update(UUID id, DriveRequestDto requestDto) {
        var frountDrive = getOrThrow(id);
        var title = requestDto.title().trim();
        repository.findByTitle(title)
                  .filter(drive -> !drive.getId().equals(frountDrive.getId()))
                  .ifPresent(drive -> {
                      throw new EntityAlreadyExistsException(drive.getTitle(), drive.getId());
                  });
        frountDrive.setTitle(title);
        return mapper.driveToDriveDto(frountDrive);
    }
    
    private Drive getOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(Drive.class, id));
    }
    
    @Override
    @Transactional
    public void delete(UUID id) {
        repository.delete(getOrThrow(id));
    }
    
    @Override
    public Page<DriveDto> findAll(PageSettingDto pageSetting) {
        var sorting = Sort.by(Drive_.TITLE).ascending();
        PageImpl<DriveDto> result;
        if (pageSetting != null) {
            var foundPageable = repository.findAll(PageRequest.of(pageSetting.page(), pageSetting.size(), sorting));
            var allEntries = mapper.listDriveToListDriveDto(foundPageable.getContent());
            result = new PageImpl<>(allEntries, foundPageable.getPageable(), foundPageable.getTotalElements());
        } else {
            var foundEntries = repository.findAll(sorting);
            var allEntries = mapper.listDriveToListDriveDto(foundEntries);
            result = new PageImpl<>(allEntries, PageRequest.of(0, allEntries.size()), allEntries.size());
        }
        return result;
    }
    
    private void checkIfDriveExists(String title) {
        repository.findByTitle(title)
                  .ifPresent(entity -> {
                      throw new EntityAlreadyExistsException(entity.getTitle(), entity.getId());
                  });
    }
    
}
