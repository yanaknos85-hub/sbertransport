package ru.sberbank.ditsib.transport.vehicle.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.database.dao.AttorneyRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.*;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyCreateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyDto;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyPaginationRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyUpdateDto;
import ru.sberbank.ditsib.transport.vehicle.exception.AttorneyNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.exception.AttorneyWrongDatesException;
import ru.sberbank.ditsib.transport.vehicle.exception.NonUniqueAttorneyException;
import ru.sberbank.ditsib.transport.vehicle.mapper.AttorneyMapper;
import ru.sberbank.ditsib.transport.vehicle.service.AttorneyService;
import ru.sberbank.ditsib.transport.vehicle.service.corp.EmployeeService;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AttorneyServiceImpl implements AttorneyService {

    private final AttorneyRepository attorneyRepository;
    private final AttorneyMapper attorneyMapper;
    private final EmployeeService employeeService;
    private final Clock clock;
    
    @Override
    public Page<AttorneyDto> getAll(AttorneyPaginationRequestDto paginationRequest, UUID userId) {
        var employee = employeeService.getByUserId(userId);
        var specification = (Specification<Attorney>) (root, query, builder) ->
                builder.equal(root.get(Attorney_.telemechanic).get(Employee_.organization).get(Organization_.ID),
                        employee.getOrganization().getId());

        var soring = Sort.by(Attorney_.TELEMECHANIC.concat(".").concat(Employee_.LAST_NAME));
        var pageRequest = Optional.ofNullable(paginationRequest.pageSetting())
                .map(pageSetting -> PageRequest.of(pageSetting.page(), pageSetting.size(), soring))
                .orElseGet(() -> PageRequest.of(0, 20, soring));

        var pageableResult = attorneyRepository.findAll(specification, pageRequest);
        var mappedEntries = attorneyMapper.mapAttorneyListToAttorneyDtoList(pageableResult.getContent());
        return new PageImpl<>(mappedEntries, pageableResult.getPageable(), pageableResult.getTotalElements());
    }

    @Override
    @Transactional
    public AttorneyDto create(AttorneyCreateDto requestDto, UUID userId) {
        checkAttorneyDates(requestDto.issueDate(), requestDto.expiryDate());

        var telemechanic = employeeService.getByUserId(requestDto.telemechanicId());
        var user = employeeService.getByUserId(userId);
        checkUsersOrganizations(telemechanic, user);

        attorneyRepository.findByAttorneyId(requestDto.attorneyId())
                .ifPresent(exists -> { throw new NonUniqueAttorneyException(); });

        var attorney = Attorney.builder()
                .attorneyId(requestDto.attorneyId())
                .telemechanic(telemechanic)
                .issueDate(requestDto.issueDate())
                .expiryDate(requestDto.expiryDate())
                .creationSystem(requestDto.creationSystem())
                .build();
        var savedAttorney = attorneyRepository.saveAndFlush(attorney);
        return attorneyMapper.mapAttorneyToAttorneyDto(savedAttorney);
    }
    
    @Override
    @Transactional
    public AttorneyDto update(AttorneyUpdateDto requestDto, UUID attorneyId, UUID userId) {
        checkAttorneyDates(requestDto.issueDate(), requestDto.expiryDate());

        var attorney = attorneyRepository.findById(attorneyId)
                .orElseThrow(() -> new EntityNotFoundException(Attorney.class, attorneyId));

        var user = employeeService.getByUserId(userId);
        var telemechanic = attorney.getTelemechanic();
        checkUsersOrganizations(telemechanic, user);

        attorneyRepository.findByAttorneyId(requestDto.attorneyId())
                .filter(exists -> !Objects.equals(exists.getId(), attorneyId))
                .ifPresent(exists -> { throw new NonUniqueAttorneyException(); });

        attorney.setAttorneyId(requestDto.attorneyId());
        attorney.setCreationSystem(requestDto.creationSystem());
        attorney.setIssueDate(requestDto.issueDate());
        attorney.setExpiryDate(requestDto.expiryDate());

        attorneyRepository.saveAndFlush(attorney);
        return attorneyMapper.mapAttorneyToAttorneyDto(attorney);
    }
    
    @Override
    public AttorneyDto getAttorneyByTelemechanicId(UUID telemechanicId) {
        return attorneyRepository.getAllByTelemechanicId(telemechanicId).stream()
                                 .filter(attorney -> !attorney.getExpiryDate().isBefore(LocalDateTime.now(clock)))
                                 .max(Comparator.comparing(Attorney::getExpiryDate))
                                 .map(attorneyMapper::mapAttorneyToAttorneyDto)
                                 .orElseThrow(() -> new AttorneyNotFoundException(telemechanicId));
    }
    
    private void checkAttorneyDates(LocalDateTime issueDate, LocalDateTime expiryDate) {
        if (issueDate.isAfter(expiryDate)) {
            throw new AttorneyWrongDatesException();
        }
    }

    private void checkUsersOrganizations(Employee telemechanic, Employee user) {
        if (!Objects.equals(telemechanic.getOrganization(), user.getOrganization())) {
            throw new AccessDeniedException("Добавление допустимо только для пользователей из одной организации");
        }
    }

}
