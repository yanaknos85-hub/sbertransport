package ru.sberbank.ditsib.transport.vehicle.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.vehicle.constants.ReportType;
import ru.sberbank.ditsib.transport.vehicle.constants.TransportStatus;
import ru.sberbank.ditsib.transport.vehicle.database.dao.*;
import ru.sberbank.ditsib.transport.vehicle.database.model.*;
import ru.sberbank.ditsib.transport.vehicle.dto.PageSettingDto;
import ru.sberbank.ditsib.transport.vehicle.dto.StateNumberSearchRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.files.ReportDto;
import ru.sberbank.ditsib.transport.vehicle.dto.files.ReportQueryParametersDto;
import ru.sberbank.ditsib.transport.vehicle.dto.files.TransportReportDto;
import ru.sberbank.ditsib.transport.vehicle.dto.indicator.GetIndicatorValueDto;
import ru.sberbank.ditsib.transport.vehicle.dto.indicator.IndicatorDateInfo;
import ru.sberbank.ditsib.transport.vehicle.dto.indicator.Indicators;
import ru.sberbank.ditsib.transport.vehicle.dto.transmissiontype.TransportInfoDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.*;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.create.TransportCreateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.update.TransportUpdateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.TransportResponseDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.TransportSearchResponseDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.TransportSearchResponseDtoV2;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.TransportSearchWithStructureResponseDto;
import ru.sberbank.ditsib.transport.vehicle.exception.*;
import ru.sberbank.ditsib.transport.vehicle.mapper.IndicatorMapper;
import ru.sberbank.ditsib.transport.vehicle.mapper.OdometerHistoryMapper;
import ru.sberbank.ditsib.transport.vehicle.mapper.ReportMapper;
import ru.sberbank.ditsib.transport.vehicle.mapper.TransportMapper;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.OdometerHistoryValueMessage;
import ru.sberbank.ditsib.transport.vehicle.messaging.sender.TransportSender;
import ru.sberbank.ditsib.transport.vehicle.service.TransportService;
import ru.sberbank.ditsib.transport.vehicle.service.corp.DepartmentService;
import ru.sberbank.ditsib.transport.vehicle.service.corp.EmployeeService;
import ru.sberbank.ditsib.transport.vehicle.service.corp.OrganizationService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransportServiceImpl implements TransportService {
    public static final String ORG_DEP_RELATION_VALIDATION_FORMAT = "Подразделение %s не принадлежит организации %s";

    private final Clock clock;
    private final DepartmentRepository departmentRepository;
    private final TransportRepository transportRepository;
    private final TransportMapper transportMapper;
    private final EmployeeService employeeService;
    private final OrganizationService organizationService;
    private final DepartmentService departmentService;
    private final VehicleRepository vehicleRepository;
    private final SubtypeRepository subtypeRepository;
    private final TelematicsRepository telematicsRepository;
    private final FuelConsumptionRepository fuelConsumptionRepository;
    private final OdometerValueRepository odometerValueRepository;
    private final OdometerHistoryRepository odometerHistoryRepository;
    private final AccessiblePositionRepository accessiblePositionRepository;
    private final OdometerHistoryMapper odometerHistoryMapper;
    private final ReportMapper reportMapper;
    private final IndicatorMapper indicatorMapper;
    private final TransportSender transportSender;

    @Override
    public TransportResponseDto getById(UUID transportId) {
        var transport = getTransport(transportId);
        return transportMapper.transportToTransportResponseDto(transport);
    }

    @Override
    @Transactional
    public void create(TransportCreateDto createDto) {
        checkUniqueConstraints(createDto.vehicle().vinCode(),
                createDto.vehicle().assetNumber(),
                createDto.vehicle().stateNumber(),
                null);
        var affiliationToOrganization = checkAffiliationToOrganization(createDto.organizations(), createDto.contractorId(), createDto.autoparkId());
        var newTransport = transportMapper.transportCreateDtoToTransport(createDto);
        var vehicleId = createDto.vehicle().id();
        enrichVehicle(vehicleId, newTransport);
        var subtypeId = createDto.vehicle().subtypeId();
        enrichSubtype(subtypeId, newTransport);
        var telematicsId = createDto.vehicle().telematicsId();
        enrichTelematics(telematicsId, newTransport);
        if (affiliationToOrganization) {
            validateOrgDepRelations(createDto.organizations());
            validateAccessiblePosition(createDto.accessiblePositionId());
            var organizationIds = Objects.requireNonNull(createDto.organizations())
                    .stream()
                    .map(OrganizationRequestDto::organizationId)
                    .collect(Collectors.toSet());
            enrichOrganization(organizationIds, newTransport);
            var departmentIds = Objects.requireNonNull(createDto.organizations())
                    .stream()
                    .map(OrganizationRequestDto::departmentId)
                    .collect(Collectors.toSet());
            enrichDepartment(departmentIds, newTransport);
        }
        var result = transportRepository.saveAndFlush(newTransport);
        transportSender.send(result, false);
    }

    @Override
    @Transactional
    public TransportResponseDto update(UUID transportId, TransportUpdateDto updateDto) {
        checkUniqueConstraints(updateDto.vehicle().vinCode(),
                updateDto.vehicle().assetNumber(),
                updateDto.vehicle().stateNumber(),
                transportId);
        var transport = getTransport(transportId);
        var oldAffiliationToOrganization = transport.getContractorId() == null && transport.getAutoparkId() == null;
        var newAffiliationToOrganization = checkAffiliationToOrganization(updateDto.organizations(), updateDto.contractorId(), updateDto.autoparkId());
        if (oldAffiliationToOrganization != newAffiliationToOrganization) {
            throw new ConflictException("Нельзя передать транспорт от организации к автопарку и наоборот");
        }
        if (Objects.nonNull(transport.getExploitationEnd())) {
            verifyExploitationDate(
                    updateDto.location().exploitationStart().toLocalDate(),
                    transport.getExploitationEnd());
        }
        verifyMileage(transport.getCurrentMileage(), updateDto.vehicle().currentMileage());
        if (newAffiliationToOrganization) {
            validateOrgDepRelations(updateDto.organizations());
            validateAccessiblePosition(updateDto.accessiblePositionId());
        }
        if (updateDto.vehicle().subtypeId() != null) {
            enrichSubtype(updateDto.vehicle().subtypeId(), transport);
        }
        enrichNewData(transport, updateDto, newAffiliationToOrganization);
        var result = transportRepository.saveAndFlush(transport);
        transportSender.send(result, false);
        return transportMapper.transportToTransportResponseDto(transport);
    }

    @Override
    public Page<TransportSearchResponseDto> searchAllOrganizations(TransportSearchingRequestDto searchingRequestDto) {
        return search(searchingRequestDto);
    }

    @Override
    public Page<TransportSearchResponseDto> searchSelfOrganizations(TransportSelfSearchingRequestDto selfSearchingRequestDto, UUID userId) {
        var employee = employeeService.getByUserId(userId);
        var employeeOrganizationId = employee.getOrganization().getId();
        if (selfSearchingRequestDto.departmentId() != null) {
            var department = departmentService.get(selfSearchingRequestDto.departmentId()).orElseThrow(
                    () -> new DepartmentNotFoundException(selfSearchingRequestDto.departmentId()));
            if (!department.getOrganization().getId().equals(employeeOrganizationId)) {
                throw new OrgDepRelationValidationException(
                        ORG_DEP_RELATION_VALIDATION_FORMAT.formatted(selfSearchingRequestDto.departmentId(), employeeOrganizationId));
            }
        }
        return search(transportMapper.transportSelfSearchingRequestDtoToTransportSearchingRequestDto(selfSearchingRequestDto, employeeOrganizationId));
    }

    @Override
    public Page<TransportSearchResponseDto> search(TransportSearchingRequestDto searchingRequestDto) {
        var fetchedResult = findTransportsBySearchParams(searchingRequestDto);
        var transportDtoList = transportMapper.transportToTransportResponseDtoList(fetchedResult.getContent());
        return new PageImpl<>(transportDtoList, fetchedResult.getPageable(), fetchedResult.getTotalElements());
    }

    @Override
    public Page<TransportSearchResponseDtoV2> searchWithBrandAndModel(TransportSearchingRequestDto searchingRequestDto) {
        var fetchedResult = findTransportsBySearchParams(searchingRequestDto);
        var transportDtoList = fetchedResult.getContent().stream()
                .map(transportMapper::transportToTransportSearchWithBrandAndModelResponseDto)
                .toList();
        return new PageImpl<>(transportDtoList, fetchedResult.getPageable(), fetchedResult.getTotalElements());
    }

    @NotNull
    private Page<Transport> findTransportsBySearchParams(TransportSearchingRequestDto searchingRequestDto) {
        var specification = prepareSearchingSpecification(searchingRequestDto);
        var page = preparePageRequest(searchingRequestDto.page());
        return transportRepository.findAll(specification, page);
    }

    @NotNull
    private static PageRequest preparePageRequest(PageSettingDto pageSetting) {
        var sorting = Sort.by(Transport_.STATUS).and(Sort.by(Transport_.STATE_NUMBER));
        return PageRequest.of(pageSetting.page(), pageSetting.size(), sorting);
    }

    public void checkUniqueConstraints(String vin, String assetNumber, String stateNumber, UUID transportId) {
        var specification = (Specification<Transport>) (root, query, builder) -> {
            var predicate = builder.isNotNull(root.get(Transport_.ID));
            if (Objects.nonNull(transportId)) {
                predicate = builder.and(predicate, builder.notEqual(root.get(Transport_.ID), transportId));
            }
            predicate = builder.and(predicate, builder.or(
                    builder.equal(builder.upper(root.get(Transport_.VIN_CODE)), vin.toUpperCase()),
                    builder.equal(builder.upper(root.get(Transport_.ASSET_NUMBER)), assetNumber.toUpperCase()),
                    builder.and(builder.equal(builder.upper(root.get(Transport_.STATE_NUMBER)), stateNumber.toUpperCase()),
                            builder.equal(root.get(Transport_.STATUS), TransportStatus.IN_USE.name()))));
            return predicate;
        };

        var transportList = transportRepository.findAll(specification);
        if (!transportList.isEmpty()) {
            throw new EntityAlreadyExistsException(errorMessage(transportList, vin, assetNumber, stateNumber));
        }
    }

    @Override
    @Transactional
    public void deactivate(UUID transportId, DeactivationDto deactivationDto) {
        var transport = getTransport(transportId);
        var exploitationEnd = deactivationDto.exploitationEnd().toLocalDate();
        var exploitationStart = transport.getExploitationStart();
        verifyExploitationDate(exploitationStart, exploitationEnd);
        if (exploitationEnd.isAfter(LocalDate.now(clock))) {
            throw new ExploitationDateException("Дата не может быть больше текущей даты");
        }
        transport.setStatus(TransportStatus.NOT_IN_USE);
        transport.setExploitationEnd(exploitationEnd);
        transportSender.send(transport, true);
    }

    @Override
    @Transactional
    public void updateIndicators(UUID transportId, Indicators indicators, UUID userId) {
        var transport = getTransport(transportId);
        var employee = employeeService.getByUserId(userId);
        validateOrganization(transport, employee);
        validateStatusForUpdateIndicators(transport);
        odometerValueRepository.findByTransportId(transportId).stream()
                .max(Comparator.comparing(OdometerValue::getYear)
                        .thenComparing(OdometerValue::getMonth))
                .ifPresentOrElse(odometerValue -> updateIndicatorsForUsedTransport(odometerValue, indicators, transport, userId),
                        () -> checkDateAndUpdateIndicators(transport.getExploitationStart().getYear(),
                                transport.getExploitationStart().getMonth(),
                                indicators,
                                transport, userId));
    }

    @Override
    @Transactional
    public IndicatorDateInfo getIndicatorsDateInfo(UUID transportId, UUID userId) {
        var transport = getTransport(transportId);
        var employee = employeeService.getByUserId(userId);
        validateOrganization(transport, employee);
        validateStatusForUpdateIndicators(transport);
        return odometerValueRepository.findByTransportId(transportId).stream()
                .max(Comparator.comparing(OdometerValue::getYear)
                        .thenComparing(OdometerValue::getMonth))
                .map(this::createIndicatorDateInfo)
                .orElseGet(() -> new IndicatorDateInfo(transport.getExploitationStart().getYear(),
                        transport.getExploitationStart().getMonth()));
    }

    @Override
    public List<ReportDto> createReport(ReportQueryParametersDto mapFilterParameterToQueryParameters) {
        return switch (mapFilterParameterToQueryParameters.reportType()) {
            case ODOMETER -> reportMapper.reportProjectionListToReportDtoList(transportRepository.createOdometerReport(
                    mapFilterParameterToQueryParameters.organizationId(),
                    mapFilterParameterToQueryParameters.year(),
                    ReportType.ODOMETER.getDescription()));
            case FUEL -> reportMapper.reportProjectionListToReportDtoList(transportRepository.createFuelReport(
                    mapFilterParameterToQueryParameters.organizationId(),
                    mapFilterParameterToQueryParameters.year(),
                    ReportType.FUEL.getDescription()));
            case MILEAGE -> reportMapper.reportProjectionListToReportDtoList(transportRepository.createMileageReport(
                    mapFilterParameterToQueryParameters.organizationId(),
                    mapFilterParameterToQueryParameters.year(),
                    ReportType.MILEAGE.getDescription()));
        };
    }

    @Override
    public List<TransportReportDto> getInfoForTransportReport(UUID organizationId) {
        return transportMapper.listTransportReportProjectionToTransportReportDto(transportRepository.findAllByOrganizationId(organizationId));
    }

    @Override
    public List<TransportReportDto> getInfoForTransportReportForContractor(UUID contractorId, UUID autoparkId) {
        return transportMapper.listTransportReportProjectionToTransportReportDto(transportRepository.findAllByContractorIdAndAutoparkId(contractorId, autoparkId));
    }

    @Override
    public Page<TransportInfoDto> searchByStateNumber(StateNumberSearchRequestDto requestDto) {
        var specification = prepareSearchingSpecificationByStateNumber(requestDto.stateNumber());
        var page = preparePageRequest(requestDto.page());
        var fetchedResult = transportRepository.findAll(specification, page);
        var transportDtoList = transportMapper.transportToTransportInfoDto(fetchedResult.getContent());
        return new PageImpl<>(transportDtoList, fetchedResult.getPageable(), fetchedResult.getTotalElements());
    }

    @Override
    @Transactional
    public void addIndicatorsHistory(OdometerHistoryValueMessage message) {
        var transport = transportRepository.findById(message.transportId())
                .orElseThrow(() -> new EntityNotFoundException(Transport.class, message.transportId()));
        var odometerHistory = odometerHistoryMapper.addIndicatorsHistoryDtoToOdometerHistory(message);
        var savedOdometerHistory = odometerHistoryRepository.save(odometerHistory);
        updateIndicatorsValue(savedOdometerHistory.getValue(),
                savedOdometerHistory.getCreationTime(),
                savedOdometerHistory.getTransportId(),
                savedOdometerHistory.getCreatorUserId());
        updateTransportMileage(transport, savedOdometerHistory.getValue());
        transportSender.send(transport, false);
    }

    @Override
    public GetIndicatorValueDto getIndicatorValue(UUID transportId) {
        var odometerValue = odometerValueRepository.findByTransportId(transportId)
                .stream()
                .max(Comparator.comparing(OdometerValue::getYear).thenComparing(OdometerValue::getMonth))
                .orElseThrow(() -> new EntityNotFoundException(OdometerValue.class, transportId));
        return indicatorMapper.odometerValueToGetIndicatorValueDto(odometerValue);
    }

    private Specification<Transport> prepareSearchingSpecification(TransportSearchingRequestDto searchingRequestDto) {
        return (root, query, builder) -> {
            var predicate = builder.isTrue(builder.literal(true));

            predicate = getPredicateBySearchText(
                    root,
                    builder,
                    searchingRequestDto.searchText(),
                    predicate
            );

            predicate = getPredicateByStateNumber(
                    root,
                    builder,
                    searchingRequestDto.stateNumber(),
                    predicate
            );

            predicate = getPredicateByOrganizationId(
                    searchingRequestDto,
                    root,
                    builder,
                    predicate
            );

            predicate = addDepartmentFilter(
                    root,
                    builder,
                    predicate,
                    searchingRequestDto.departmentId()
            );

            predicate = getPredicateByStatus(
                    searchingRequestDto,
                    root,
                    builder,
                    predicate
            );

            predicate = getPredicateByBrandIdAndModelId(
                    root,
                    builder,
                    searchingRequestDto.brand(),
                    predicate,
                    searchingRequestDto.model()
            );

            predicate = getPredicateByYear(
                    searchingRequestDto,
                    root,
                    builder,
                    predicate
            );

            predicate = getPredicateByContractorIds(
                    searchingRequestDto,
                    root,
                    builder,
                    predicate
            );

            predicate = getPredicateByAutoparkId(
                    searchingRequestDto,
                    root,
                    builder,
                    predicate
            );

            return predicate;
        };
    }

    private static Predicate getPredicateBySearchText(Root<Transport> root, CriteriaBuilder builder, String searchText, Predicate predicate) {
        if (Strings.isNotBlank(searchText)) {
            var searchTextEmbraced = "%" + searchText.toUpperCase() + "%";
            predicate = builder.and(predicate,
                    builder.or(
                            builder.like(builder.upper(root.get(Transport_.STATE_NUMBER)), searchTextEmbraced),
                            builder.like(builder.upper(root.get(Transport_.VIN_CODE)), searchTextEmbraced)));
        }
        return predicate;
    }

    private static Predicate getPredicateByStateNumber(Root<Transport> root, CriteriaBuilder builder, String stateNumber, Predicate predicate) {
        if (Strings.isNotBlank(stateNumber)) {
            var stateNumberEmbraced = "%" + stateNumber.toUpperCase() + "%";
            predicate = builder.and(predicate, builder.like(builder.upper(root.get(Transport_.STATE_NUMBER)), stateNumberEmbraced));
        }
        return predicate;
    }

    private static Predicate getPredicateByAutoparkId(TransportSearchingRequestDto searchingRequestDto, Root<Transport> root, CriteriaBuilder builder, Predicate predicate) {
        var autoparkId = searchingRequestDto.autoparkId();

        if (autoparkId != null) {
            predicate = builder.and(predicate,
                    builder.equal(
                            root.get(Transport_.AUTOPARK_ID), autoparkId
                    ));
        }
        return predicate;
    }

    private static Predicate getPredicateByContractorIds(TransportSearchingRequestDto searchingRequestDto, Root<Transport> root, CriteriaBuilder builder, Predicate predicate) {
        var contractorIds = searchingRequestDto.contractorIds();
        if (contractorIds != null && !contractorIds.isEmpty()) {
            predicate = builder.and(predicate,
                    root.get(Transport_.CONTRACTOR_ID).in(contractorIds));
        }
        return predicate;
    }

    private static Predicate getPredicateByYear(TransportSearchingRequestDto searchingRequestDto, Root<Transport> root, CriteriaBuilder builder, Predicate predicate) {
        var year = searchingRequestDto.year();

        if (year != null) {
            predicate = builder.and(predicate,
                    builder.equal(
                            root.get(Transport_.YEAR), year
                    ));
        }
        return predicate;
    }

    private static Predicate getPredicateByBrandIdAndModelId(Root<Transport> root, CriteriaBuilder builder, UUID brandId, Predicate predicate, UUID modelId) {
        Join<Transport, Vehicle> vehicleJoin = root.join(Transport_.VEHICLE);
        Join<Vehicle, Model> modelJoin = vehicleJoin.join(Vehicle_.MODEL);

        if (brandId != null) {
            Join<Model, Brand> brandJoin = modelJoin.join(Model_.BRAND);
            predicate = builder.and(predicate,
                    builder.equal(brandJoin.get(Brand_.ID), brandId));
        }

        if (modelId != null) {
            predicate = builder.and(predicate,
                    builder.equal(modelJoin.get(Model_.ID), modelId)
            );
        }
        return predicate;
    }

    private static Predicate getPredicateByStatus(TransportSearchingRequestDto searchingRequestDto, Root<Transport> root, CriteriaBuilder builder, Predicate predicate) {
        var status = searchingRequestDto.status();

        if (status != null) {
            predicate = builder.and(predicate,
                    builder.equal(
                            root.get(Transport_.STATUS), status
                    ));
        }
        return predicate;
    }

    private static Predicate getPredicateByOrganizationId(TransportSearchingRequestDto searchingRequestDto, Root<Transport> root, CriteriaBuilder builder, Predicate predicate) {
        var organizationId = searchingRequestDto.organizationId();

        if (organizationId != null) {
            Join<Transport, Organization> orgJoin = root.join(Transport_.ORGANIZATIONS);
            predicate = builder.and(predicate,
                    builder.in(orgJoin.get(Organization_.ID)).value(organizationId)
            );
        }
        return predicate;
    }

    private Specification<Transport> prepareSearchingSpecificationByStateNumber(String stateNumber) {
        return (root, query, builder) -> {
            var predicate = builder.isTrue(builder.literal(true));
            if (Strings.isNotBlank(stateNumber)) {
                var searchTextEmbraced = "%" + stateNumber.toUpperCase() + "%";
                predicate = builder.and(predicate, builder.like(builder.upper(root.get(Transport_.STATE_NUMBER)), searchTextEmbraced));
            }

            return predicate;
        };
    }

    @Override
    public Page<TransportSearchWithStructureResponseDto> searchWithStructure(TransportSearchWithStructureRequestDto requestDto, UUID userId) {
        var employee = employeeService.getByUserId(userId);
        var departmentIds = departmentService.getParentDepartments(employee.getDepartment().getId());
        var pageable = PageRequest.of(requestDto.page().page(), requestDto.page().size());
        var result = transportRepository.findTransportWithStructure(departmentIds, requestDto.searchText(), pageable);
        return new PageImpl<>(
                transportMapper.listTransportShortProjectionToListTransportSearchWithStructureResponseDto(result.getContent()),
                result.getPageable(),
                result.getTotalElements());
    }

    @Override
    @Transactional
    public TransportInfoDto getTransportByStateNumber(String stateNumber) {
        var transport = transportRepository.findTransportByStateNumber(stateNumber)
                .orElseThrow(() -> new TransportNotFoundException(String.format("Транспорт с гос.номером '%s' не найден", stateNumber)));
        return transportMapper.transportToTransportInfoDto(transport);
    }

    @Override
    @Transactional
    public int getMileage(UUID odometerHistoryId) {
        var odometerHistory = odometerHistoryRepository.findById(odometerHistoryId)
                .orElseThrow(() -> new EntityNotFoundException(OdometerHistory.class, odometerHistoryId));
        return odometerHistory.getValue();
    }

    private void validateAccessiblePosition(UUID accessiblePositionId) {
        if (accessiblePositionId != null && accessiblePositionRepository.findById(accessiblePositionId).isEmpty()) {
            throw new AccessiblePositionNotFoundException(accessiblePositionId);
        }
    }

    private void validateOrgDepRelations(List<OrganizationRequestDto> organizations) {
        if (organizations == null || organizations.isEmpty()) {
            return;
        }

        var depIds = organizations.stream().map(OrganizationRequestDto::departmentId).toList();

        var depToOrgPairs = departmentRepository.findDepOrgPairsByIds(depIds);
        var depToOrgMap = depToOrgPairs.stream()
                .collect(Collectors.toMap(
                        pair -> (UUID) pair[0],
                        pair -> (UUID) pair[1])
                );

        for (OrganizationRequestDto dto : organizations) {
            var orgIdFromDb = depToOrgMap.get(dto.departmentId());

            if (orgIdFromDb == null || !orgIdFromDb.equals(dto.organizationId())) {
                throw new OrgDepRelationValidationException(ORG_DEP_RELATION_VALIDATION_FORMAT.formatted(dto.departmentId(), dto.organizationId()));
            }
        }
    }

    private void verifyMileage(int currentMileage, int updatedMileage) {
        if (currentMileage > updatedMileage) {
            throw new MileageNotIncrementedException("Текущий пробег не может быть меньше предыдущего");
        }
    }

    private void validateOrganization(Transport transport, Employee employee) {
        var orgIds = transport.getOrganizations().stream().map(Organization::getId).collect(Collectors.toSet());
        if (!orgIds.contains(employee.getOrganization().getId())) {
            throw new NotAllowedUserException(orgIds, employee.getUserId(), employee.getOrganization().getId());
        }
    }

    private IndicatorDateInfo createIndicatorDateInfo(OdometerValue value) {
        var nextYear = value.getYear();
        var nextMonth = value.getMonth().plus(1);
        if (nextMonth.equals(Month.JANUARY)) {
            nextYear++;
        }
        return new IndicatorDateInfo(nextYear, nextMonth);
    }

    private void checkDateAndUpdateIndicators(int year, Month month, Indicators indicators, Transport transport, UUID userId) {
        if (indicators.year() != year || !indicators.month().equals(month)) {
            throw new IndicatorsException(String.format("Необходимо внести данные за год:%s, месяц:%s",
                    year,
                    month.getValue()));
        }
        saveIndicatorsAndCurrentMileage(indicators.consumption(), indicators.value(), indicators.year(), indicators.month(), transport, userId);
    }

    private void updateIndicatorsForUsedTransport(OdometerValue value, Indicators indicators, Transport transport, UUID userId) {
        if (transport.getCurrentMileage() > indicators.value()) {
            throw new IndicatorsException("Показания одометра не могут быть меньше, чем ранее внесенные в систему");
        }
        var nextYear = value.getYear();
        var nextMonth = value.getMonth().plus(1);
        if (nextMonth.equals(Month.JANUARY)) {
            nextYear++;
        }
        checkDateAndUpdateIndicators(nextYear,
                nextMonth,
                indicators,
                transport,
                userId);
    }

    private void validateStatusForUpdateIndicators(Transport transport) {
        if (transport.getStatus().equals(TransportStatus.NOT_IN_USE)) {
            throw new StatusException("Транспорт в статусе:" + TransportStatus.NOT_IN_USE.getDescription());
        }
    }

    private void saveIndicatorsAndCurrentMileage(int consumption, int value, int year, Month month, Transport transport, UUID userId) {
        fuelConsumptionRepository.save(FuelConsumption.builder()
                .consumption(consumption)
                .transportId(transport.getId())
                .year(year)
                .month(month)
                .build());
        odometerValueRepository.save(OdometerValue.builder()
                .value(value)
                .transportId(transport.getId())
                .year(year)
                .month(month)
                .creatorUserId(userId)
                .build());
        transport.setCurrentMileage(value);
        transportRepository.save(transport);
        transportSender.send(transport, false);
    }

    private static void verifyExploitationDate(LocalDate exploitationStart, LocalDate exploitationEnd) {
        if (exploitationStart.isAfter(exploitationEnd)) {
            throw new ExploitationDateException("Дата не может быть меньше даты начала эксплуатации");
        }
    }

    private void enrichNewData(Transport transport, TransportUpdateDto updateDto, boolean affiliationToOrganization) {
        if (affiliationToOrganization) {
            var organizations = updateDto.organizations();
            var organizationIds = Objects.requireNonNull(organizations).stream().map(OrganizationRequestDto::organizationId).collect(Collectors.toSet());
            var departmentIds = organizations.stream().map(OrganizationRequestDto::departmentId).collect(Collectors.toSet());
            enrichOrganization(organizationIds, transport);
            enrichDepartment(departmentIds, transport);
        } else {
            transport.setContractorId(updateDto.contractorId());
            transport.setAutoparkId(updateDto.autoparkId());
        }

        var location = updateDto.location();
        transport.setExploitationStart(location.exploitationStart().toLocalDate());
        transport.setLocationAddress(updateDto.location().locationAddress());
        transport.setParkingAddress(updateDto.location().parkingAddress());
        transport.setComment(updateDto.comment());
        enrichAccessiblePosition(updateDto.accessiblePositionId(), transport);

        var vehicle = updateDto.vehicle();
        enrichVehicle(vehicle.id(), transport);
        transport.setStateNumber(vehicle.stateNumber());
        transport.setVinCode(vehicle.vinCode());
        transport.setAssetNumber(vehicle.assetNumber());
        transport.setInventoryNumber(vehicle.inventoryNumber());
        transport.setBodyNumber(vehicle.bodyNumber());
        transport.setChassisNumber(vehicle.chassisNumber());
        transport.setCurrentMileage(vehicle.currentMileage());
        enrichTelematics(vehicle.telematicsId(), transport);

        var documents = updateDto.documents();
        transport.setCertificateNumber(documents.certificateNumber());
        transport.setCertificateIssuedDate(documents.certificateIssuedDate().toLocalDate());
        transport.setVehicleType(documents.vehicleType());
        transport.setBalanceUnitNumber(updateDto.balanceUnitNumber());
        transport.setFacility(updateDto.facility());
        transport.setEquipmentUnitSystemNumber(updateDto.equipmentUnitSystemNumber());
    }

    private Transport getTransport(UUID transportId) {
        return transportRepository.findById(transportId)
                .orElseThrow(() -> new TransportNotFoundException(String.format("Транспортное средство с идентификатором %s не найдено", transportId)));
    }

    private void enrichTelematics(UUID telematicsId, Transport newTransport) {
        if (Objects.nonNull(telematicsId)) {
            var telematics = telematicsRepository.findById(telematicsId)
                    .orElseThrow(() -> new EntityNotFoundException(Telematics.class, telematicsId));
            newTransport.setTelematics(telematics);
        } else {
            newTransport.setTelematics(null);
        }
    }

    private void enrichSubtype(UUID subtypeId, Transport transport) {
        var subtype = subtypeRepository.findById(subtypeId).orElseThrow(() -> new EntityNotFoundException(Subtype.class, subtypeId));
        transport.setSubtype(subtype);
    }

    private void enrichDepartment(Set<UUID> departmentIds, Transport newTransport) {
        if (departmentIds.isEmpty()) {
            return;
        }

        var departments = departmentService.getAllById(departmentIds);

        if (departments.size() != departmentIds.size()) {
            throw new EntityNotFoundException(
                    Department.class,
                    String.format("Not all departments found in the list: %s", departmentIds)
            );
        }

        newTransport.setDepartments(new HashSet<>(departments));
    }

    private void enrichOrganization(Set<UUID> organizationIds, Transport newTransport) {
        if (organizationIds.isEmpty()) {
            return;
        }

        var organizations = organizationService.getAllByIds(organizationIds);

        if (organizations.size() != organizationIds.size()) {
            throw new EntityNotFoundException(
                    Organization.class,
                    String.format("Not all organizations found in the list: %s", organizationIds)
            );
        }

        newTransport.setOrganizations(new HashSet<>(organizations));
    }

    private void enrichVehicle(UUID vehicleId, Transport newTransport) {
        var vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException(Vehicle.class, vehicleId));
        newTransport.setVehicle(vehicle);
    }

    private void enrichAccessiblePosition(UUID accessiblePositionId, Transport transport) {
        var accessiblePosition = accessiblePositionRepository.findById(accessiblePositionId)
                .orElseThrow(() -> new AccessiblePositionNotFoundException(accessiblePositionId));
        transport.setAccessiblePosition(accessiblePosition);
    }

    private void updateIndicatorsValue(int value, LocalDateTime creationTime, UUID transportId, UUID creatorUserId) {
        var odometerValue = odometerValueRepository.findByTransportId(transportId)
                .stream()
                .max(Comparator.comparing(OdometerValue::getYear).thenComparing(OdometerValue::getMonth))
                .orElse(null);
        var year = LocalDate.now(clock).getYear();
        var month = LocalDate.now(clock).getMonth();
        if (Objects.isNull(odometerValue) ||
                odometerValue.getYear() != year ||
                odometerValue.getMonth() != month) {
            var newOdometerValue = OdometerValue.builder()
                    .value(value)
                    .transportId(transportId)
                    .year(year)
                    .month(month)
                    .creationDate(creationTime)
                    .creatorUserId(creatorUserId)
                    .build();
            odometerValueRepository.save(newOdometerValue);
        } else {
            odometerValue.setValue(value);
            odometerValueRepository.save(odometerValue);
        }
    }

    private void updateTransportMileage(Transport transport, int value) {
        transport.setCurrentMileage(value);
        transportRepository.save(transport);
    }

    /**
     * Добавляет критерий фильтрации транспорта по департаменту, включая дочерние департаменты.
     *
     * @param root         корневая сущность запроса
     * @param builder      CriteriaBuilder для построения условий
     * @param predicate    текущий набор условий
     * @param departmentId идентификатор департамента
     * @return Predicate с добавленным фильтром по департаменту
     */
    private Predicate addDepartmentFilter(
            Root<Transport> root,
            CriteriaBuilder builder,
            Predicate predicate,
            UUID departmentId
    ) {
        if (departmentId == null) {
            return predicate;
        }

        // 1. Основной департамент
        Join<Transport, Department> departmentJoin = root.join(Transport_.DEPARTMENTS);
        predicate = builder.and(predicate,
                builder.in(departmentJoin.get(Department_.ID)).value(departmentId)
        );

        // 2. Дочерние департаменты: где parent.id = deptId
        Join<Transport, Department> childDepartmentJoin = root.join(Transport_.DEPARTMENTS);
        Join<Department, Department> parentJoin = childDepartmentJoin.join(Department_.PARENT);
        predicate = builder.or(
                predicate,
                builder.in(parentJoin.get(Department_.ID)).value(departmentId)
        );

        return predicate;
    }

    private boolean checkAffiliationToOrganization(List<OrganizationRequestDto> organizations, UUID contractorId, UUID autoparkId) {
        if (organizations == null || organizations.isEmpty()) {
            if (contractorId == null && autoparkId == null) {
                throw new ConflictException("Транспортное средство должно принадлежать либо организации, либо автопарку");
            } else return false;
        } else if (contractorId != null && autoparkId != null) {
            throw new ConflictException("Транспортное средство не должно принадлежать одновременно организации и автопарку");
        } else return true;
    }

    private String errorMessage(List<Transport> transportList, String vin, String assetNumber, String stateNumber) {
        var vinCodeAdded = false;
        var assetNumberAdded = false;
        var stateNumberAdded = false;
        List<String> matches = new ArrayList<>();
        for (var transport : transportList) {
            if (!vinCodeAdded && vin.equals(transport.getVinCode())) {
                matches.add("VIN номером %s".formatted(vin));
            }
            if (!assetNumberAdded && assetNumber.equals(transport.getAssetNumber())) {
                matches.add("номером основного средства %s".formatted(assetNumber));
            }
            if (!stateNumberAdded && stateNumber.equals(transport.getStateNumber()) && TransportStatus.IN_USE.equals(transport.getStatus())) {
                matches.add("гос. номером %s".formatted(stateNumber));
            }
        }
        return "Транспортное средство с " + String.join(", ", matches) + " уже существует";
    }
}
