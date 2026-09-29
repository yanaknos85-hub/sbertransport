package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.telemechanic.database.dao.TransportRepository;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.transport.GetTransportRequest;
import ru.sber.transport.telemechanic.dto.transport.GetTransportResponse;
import ru.sber.transport.telemechanic.dto.transport.TransportSearchDto;
import ru.sber.transport.telemechanic.enumerate.TransportStatus;
import ru.sber.transport.telemechanic.exception.TransportNotFound;
import ru.sber.transport.telemechanic.exception.TransportNotInUse;
import ru.sber.transport.telemechanic.mapper.TransportMapper;
import ru.sber.transport.telemechanic.service.DepartmentService;
import ru.sber.transport.telemechanic.service.EmployeeService;
import ru.sber.transport.telemechanic.service.OrganizationService;
import ru.sber.transport.telemechanic.service.TransportService;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class TransportServiceImpl implements TransportService {
    
    private final TransportRepository transportRepository;
    
    private final TransportMapper transportMapper;
    
    private final EmployeeService employeeService;
    
    private final OrganizationService organizationService;
    
    private final DepartmentService departmentService;
    
    private static final String LIKE_FORMAT = "%%%s%%";
    
    @Override
    public Transport getTransportById(UUID transportId) {
        return transportRepository
                .findById(transportId)
                .orElseThrow(() -> new TransportNotFound(transportId));
    }
    
    @Override
    public Transport getTransportByIdWithOrganizations(UUID transportId) {
        return transportRepository.findByIdWithOrganizations(transportId)
                                  .orElseThrow(() -> new TransportNotFound(transportId));
    }
    
    @Override
    public Optional<Transport> get(UUID id) {
        return transportRepository.findById(id);
    }
    
    @Override
    public void deactivate(Transport entity) {
        transportRepository.save(entity.setStatus(TransportStatus.NOT_IN_USE));
    }
    
    @Override
    public void saveOrUpdate(Transport entity) {
        var dbEntityOptional = transportRepository.findById(entity.getId());
        if (dbEntityOptional.isPresent()) {
            transportRepository.save(dbEntityOptional.get()
                                                     .setStateNumber(entity.getStateNumber())
                                                     .setBrand(entity.getBrand())
                                                     .setModel(entity.getModel())
                                                     .setStatus(entity.getStatus())
                                                     .setMileage(entity.getMileage())
                                                     .setType(entity.getType())
                                                     .setSubtype(entity.getSubtype())
                                                     .setFuelTankVolume(entity.getFuelTankVolume())
                                                     .setOrganizations(entity.getOrganizations())
                                                     .setContractorId(entity.getContractorId())
                                                     .setAutoparkId(entity.getAutoparkId()));
        } else {
            transportRepository.save(entity);
        }
    }
    
    @Override
    public Page<GetTransportResponse> getTransportByStateNumberSelfOrganization(GetTransportRequest request, UUID employeeId) {
        var organizationId = employeeService.getByUserId(employeeId).getOrganization().getId();
        return getTransportByStateNumber(request.stateNumber(), organizationId, request.preparePageRequest());
    }
    
    @Override
    public Page<GetTransportResponse> getTransportByStateNumberAllOrganizations(GetTransportRequest request) {
        return getTransportByStateNumber(request.stateNumber(), null, request.preparePageRequest());
    }
    
    @Override
    @Transactional
    public Transport getTransportInUseById(UUID transportId) {
        var transport = transportRepository.findById(transportId)
                                           .orElseThrow(() -> new TransportNotFound(transportId));
        if (!transport.getStatus().equals(TransportStatus.IN_USE)) {
            throw new TransportNotInUse("Автомобиль не в эксплуатации. transport id:%s".formatted(transportId));
        }
        return transport;
    }
    
    @Override
    public Page<GetTransportResponse> getTransportByFilters(TransportSearchDto searchDto, Authentication authentication) {
        var pageable = PageRequest.of(searchDto.getPage(), searchDto.getSize());
        var contractorId = getExternalId(searchDto.getOrganizationId(), Organization.class);
        var autoparkId = getExternalId(searchDto.getDepartmentId(), Department.class);
        var spec = createSpec(contractorId, autoparkId, searchDto.getStateNumber());
        return transportRepository.findAll(spec, pageable).map(transportMapper::transportToGetTransportResponse);
        
    }
    
    private Specification<Transport> createSpec(UUID contractorId, UUID autoparkId, String stateNumber) {
        return (root, q, cb) -> {
            var predicate = cb.and(cb.isNotNull(root.get(Transport_.CONTRACTOR_ID)), cb.isNotNull(root.get(Transport_.AUTOPARK_ID)));
            if (contractorId != null) {
                predicate = cb.and(predicate, cb.equal(root.get(Transport_.CONTRACTOR_ID), contractorId));
            }
            if (autoparkId != null) {
                predicate = cb.and(predicate, cb.equal(root.get(Transport_.AUTOPARK_ID), autoparkId));
            }
            if (stateNumber != null) {
                predicate = cb.and(predicate, cb.like(cb.lower(root.get(Transport_.STATE_NUMBER)),
                                                      LIKE_FORMAT.formatted(stateNumber.toLowerCase(Locale.ROOT))));
            }
            return predicate;
        };
    }
    
    private Page<GetTransportResponse> getTransportByStateNumber(String stateNumber, UUID organizationId, Pageable pageRequest) {
        var transports = transportRepository.findAllByStateNumber(stateNumber, organizationId, pageRequest);
        return new PageImpl<>(transports.getContent().stream()
                                        .map(transportMapper::transportToGetTransportResponse)
                                        .toList(),
                              transports.getPageable(),
                              transports.getTotalElements());
    }
    
    private UUID getExternalId(UUID entityId, Class<?> clazz) {
        if (entityId != null) {
            if (Organization.class.equals(clazz)) {
                var orgOpt = organizationService.get(entityId);
                return orgOpt.map(Organization::getContractorExternalId).orElseThrow(() -> new EntityNotFoundException(clazz, entityId));
            }
            if (Department.class.equals(clazz)) {
                var depOpt = departmentService.get(entityId);
                return depOpt.map(Department::getAutoparkId).orElseThrow(() -> new EntityNotFoundException(clazz, entityId));
            }
        }
        return null;
    }
    
}
