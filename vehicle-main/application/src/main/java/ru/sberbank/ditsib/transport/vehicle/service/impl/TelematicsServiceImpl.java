package ru.sberbank.ditsib.transport.vehicle.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.database.dao.TelematicsRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Telematics;
import ru.sberbank.ditsib.transport.vehicle.database.model.Telematics_;
import ru.sberbank.ditsib.transport.vehicle.dto.PaginationCommonRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.telematics.TelematicsDto;
import ru.sberbank.ditsib.transport.vehicle.dto.telematics.TelematicsRequestDto;
import ru.sberbank.ditsib.transport.vehicle.exception.EntityAlreadyExistsException;
import ru.sberbank.ditsib.transport.vehicle.mapper.TelematicsMapper;
import ru.sberbank.ditsib.transport.vehicle.service.TelematicsService;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

@Service
@RequiredArgsConstructor
public class TelematicsServiceImpl implements TelematicsService {

    private final TelematicsMapper mapper;
    private final TelematicsRepository repository;

    @Override
    public TelematicsDto get(UUID id) {
        return mapper.telematicsToTelematicsDto(getOrThrow(id));
    }

    @Override
    @Transactional
    public TelematicsDto create(TelematicsRequestDto requestDto) {
        var imei = requestDto.imei().trim();
        var title = requestDto.title().trim();
        checkIfTelematicsExists(imei, title, t -> true);

        var saved = repository.save(Telematics.builder()
                .imei(imei)
                .title(title)
                .build());
        return mapper.telematicsToTelematicsDto(saved);
    }

    @Override
    @Transactional
    public TelematicsDto update(UUID id, TelematicsRequestDto requestDto) {
        var fountTelematics = getOrThrow(id);
        var title = requestDto.title().trim();
        var imei = requestDto.imei().trim();
        checkIfTelematicsExists(imei, title, foundTelematics -> !id.equals(foundTelematics.getId()));
        fountTelematics.setImei(imei);
        fountTelematics.setTitle(title);
        return mapper.telematicsToTelematicsDto(fountTelematics);
    }

    private Telematics getOrThrow(UUID id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException(Telematics.class, id));
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        repository.delete(getOrThrow(id));
    }

    @Override
    public Page<TelematicsDto> findAll(PaginationCommonRequestDto paginationCommonRequestDto) {
        var pageSetting = Optional.ofNullable(paginationCommonRequestDto)
                .map(PaginationCommonRequestDto::pageSetting)
                .orElse(null);
        var sorting = Sort.by(Telematics_.TITLE).ascending();
        PageImpl<TelematicsDto> result;
        if (pageSetting != null) {
            var foundPageable = repository.findAll(PageRequest.of(pageSetting.page(), pageSetting.size(), sorting));
            var allEntries = mapper.listTelematicsToListTelematicsDto(foundPageable.getContent());
            result = new PageImpl<>(allEntries, foundPageable.getPageable(), foundPageable.getTotalElements());
        } else {
            var foundEntries = repository.findAll(sorting);
            var allEntries = mapper.listTelematicsToListTelematicsDto(foundEntries);
            result = new PageImpl<>(allEntries, PageRequest.of(0, allEntries.size()), allEntries.size());
        }
        return result;
    }

    private void checkIfTelematicsExists(String imei, String title, Predicate<Telematics> predicate) {
        repository.findByImeiOrTitleIgnoreCase(imei, title.toUpperCase())
                .stream()
                .filter(predicate)
                .findFirst()
                .ifPresent(entity -> {
                    throw new EntityAlreadyExistsException(
                            "title: %s, imei: %s".formatted(entity.getImei(), entity.getTitle()),
                            entity.getId());
                });
    }

}
