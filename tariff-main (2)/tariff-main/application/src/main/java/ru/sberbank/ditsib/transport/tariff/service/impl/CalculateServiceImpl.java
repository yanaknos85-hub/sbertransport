package ru.sberbank.ditsib.transport.tariff.service.impl;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.limits.grpc.dto.LimitReservationModel;
import ru.sber.transport.limits.grpc.service.LimitReservationServiceGrpc;
import ru.sber.transport.tariff.model.CalculatedDto;
import ru.sber.transport.tariff.model.SuburbTripDataDTO;
import ru.sber.transport.tariff.model.TripDto;
import ru.sber.transport.tariff.model.WaypointDTO;
import ru.sberbank.ditsib.transport.constants.*;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.tariff.client.ContractorClient;
import ru.sberbank.ditsib.transport.tariff.database.dao.ConnectionRestrictionRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.GroupTransferTariffRepository;
import ru.sberbank.ditsib.transport.tariff.database.dao.PositionRepository;
import ru.sberbank.ditsib.transport.tariff.database.model.*;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Employee;
import ru.sberbank.ditsib.transport.tariff.database.model.messages.Position;
import ru.sberbank.ditsib.transport.tariff.dto.RegionDto;
import ru.sberbank.ditsib.transport.tariff.dto.TransportWithCalculateDTO;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.TaskDTO;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.TransportPageDTO;
import ru.sberbank.ditsib.transport.tariff.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.tariff.exceptions.RegionResolvingFailedException;
import ru.sberbank.ditsib.transport.tariff.mappers.TransportMapper;
import ru.sberbank.ditsib.transport.tariff.service.CalculateService;
import ru.sberbank.ditsib.transport.tariff.service.ContractorService;
import ru.sberbank.ditsib.transport.tariff.service.RegionDataResolver;
import ru.sberbank.ditsib.transport.tariff.service.TariffService;
import ru.sberbank.ditsib.transport.tariff.util.ConvertUtils;

import jakarta.validation.constraints.NotNull;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.sberbank.ditsib.transport.dto.tariff.CalculatedDto.ENGINE_VOLUME_COEFFICIENT;

/**
 * Base service for calculation.
 */
@RequiredArgsConstructor
@Component
@Slf4j
@Transactional
class CalculateServiceImpl implements CalculateService {
    
    @Value("${traffic.threshold:7}")
    private Integer trafficThreshold;
    
    @GrpcClient(value = "grpc-limits")
    private LimitReservationServiceGrpc.LimitReservationServiceBlockingStub stub;
    
    private final TariffService tariffService;
    
    private final ContractorService contractorService;
    
    private final RegionDataResolver regionDataResolver;
    
    private final EntityDTOMapper mapper;
    
    private final PositionRepository positionRepository;
    
    private final GroupTransferTariffRepository repository;
    
    private final ContractorClient contractorClient;
    
    private final TransportMapper mapperTransport;
    
    private final ConnectionRestrictionRepository connectionRestrictionRepository;
    
    private final Set<TaxiClass> defaultAvailableTaxiClasses = new HashSet<>(List.of(TaxiClass.ECONOMY));
    
    @Override
    public CalculatedDto calculate(TransportTypeEnum tariffType, UUID tariffId, TripDto tripData, Employee employee) {
        var tariff = tariffService.get(tariffType, tariffId)
                                  .filter(BaseTariff::isActive)
                                  .orElseThrow(() -> new EntityNotFoundException(BaseTariff.class, tariffId));
        CalculatedDto calculatedDto = calculateData(tariff, tripData);
        fillLimitsAvailable(List.of(calculatedDto), tripData, employee);
        var outcomeTariffList = tariffService.getAll(tariff.getTransportType(), List.of(tariff.getRegionId()), null, true, ContractType.OUTCOME);
        var outcomeTariffContractList = outcomeTariffList
                .stream()
                .filter(q->BaseTariffWithContract.class.isAssignableFrom(q.getClass()))
                .map(q->(BaseTariffWithContract)q)
                .toList();
        findOutcomeTariff(tripData, calculatedDto, tariff, outcomeTariffContractList, employee);
        return calculatedDto;
    }
    
    @Override
    public Collection<? extends CalculatedDto> calculate(TripDto tripData, Employee employee) {
        return calculate(tripData, employee, true, Collections.emptyList());
    }
    
    @Override
    public Collection<? extends CalculatedDto> calculate(
            TripDto tripData, Employee employee, boolean checkLimits, List<TransportTypeEnum> transportTypeList
                                                        ) {
        var regionBranch = getRegionBranch(tripData);
        formatTripDateBasedOnRegionTimeZone(tripData, regionBranch);
        
        if (isNightTrip(tripData)) {
            var result = calculateCommon(tripData, employee, regionBranch, true, checkLimits, transportTypeList);
            if (hasTaxiTariffs(result)) {
                return result;
            }
        }
        log.debug("Using calculateCommon, transportTypes - " + transportTypeList.stream().map(TransportTypeEnum::name).collect(
                Collectors.joining(",")));
        return calculateCommon(tripData, employee, regionBranch, false, checkLimits, transportTypeList);
    }
    
    @Override
    public TransportPageDTO calculateTransports(
            TripDto tripData, String search, Boolean availableOnly,
            Employee employee, String token
                                               ) {
        var regionBranch = getRegionBranch(tripData);
        formatTripDateBasedOnRegionTimeZone(tripData, regionBranch);
        var waitTime = tripData.getWaypoints()
                               .stream().map(WaypointDTO::getWaitTime)
                               .filter(Objects::nonNull)
                               .map(Duration::toSeconds)
                               .map(d -> Duration.of(d / 1000, ChronoUnit.SECONDS))
                               .map(wt -> Math.max(wt.toMinutes(), 0))
                               .mapToLong(Long::longValue).sum();
        var startDate = tripData.getTripDate();
        var endDate = startDate.plusMinutes(tripData.getTime().toMinutes()).plusMinutes(30L).plusMinutes(waitTime);
        
        var regions = regionBranch.stream().map(RegionDto::getId).collect(Collectors.toList());
        var rawTariffs = repository.findAllTariff(tripData.getTripDate().toLocalDate(),
                                               tripData.getOrganizationId(),
                                               ConvertUtils.setToStringArray(regions));
        var filteredTariffs = rawTariffs.stream().filter(q -> q.getContract().getContractType() == ContractType.INCOME).toList();
        if (filteredTariffs.isEmpty()) {
            filteredTariffs = rawTariffs.stream().filter(q -> q.getContract().getContractType() == ContractType.TRANSITIONAL).toList();
        }
        log.info("Поиск тарифов для автомобилей ");
        if (filteredTariffs.isEmpty()) {
            throw new EntityNotFoundException(GroupTransferTariff.class, "");
        }
        var contractorId = filteredTariffs.getFirst().getContractorId();
        var response = contractorClient.getTransports(0,
                                                      Integer.MAX_VALUE,
                                                      toOffsetDateTime(startDate, tripData.getTimeZone()),
                                                      toOffsetDateTime(endDate, tripData.getTimeZone()),
                                                      search,
                                                      null,
                                                      null,
                                                      null,
                                                      contractorId,
                                                      token);
        var tariffMap = toTransportRegionMap(filteredTariffs);
        var enrichTransport = response
                .getContent()
                .stream()
                .map(t ->
                     {
                         var newDTO = mapperTransport.toCalculateDto(t);
                         var tariffOpt = getGroupTransferTariff(t.getId(), regions, tariffMap);
                         var cost = tariffOpt.map(_t -> calculateGroupTransfer(_t, tripData)).orElse(0L);
                         newDTO.setCalculated(CalculatedDto.builder()
                                                           .id(tariffOpt.map(BaseTariff::getId).orElse(null))
                                                           .cost(cost)
                                                           .build());
                         newDTO.setAvailableOnly(t.getTrips().isEmpty());
                         
                         if(tariffOpt.isPresent()){
                             var tariff = tariffOpt.get();
                             var outcomeTariffList = tariffService.getAll(tariff.getTransportType(), List.of(tariff.getRegionId()), null, true, ContractType.OUTCOME);
                             var outcomeTariffContractList = outcomeTariffList
                                     .stream()
                                     .filter(q->BaseTariffWithContract.class.isAssignableFrom(q.getClass()))
                                     .map(q->(BaseTariffWithContract)q)
                                     .toList();
                             findOutcomeTariff(tripData, newDTO.getCalculated(), tariffOpt.orElse(null), outcomeTariffContractList, employee);
                         }
                         return newDTO;
                     });
        enrichTransport = enrichTransport.filter(t -> t.getCalculated().getId() != null);
        if (availableOnly != null) {
            enrichTransport = enrichTransport
                    .filter(t -> availableOnly.equals(t.getAvailableOnly()));
        }
        response.setContent(enrichTransport.sorted(Comparator.comparingLong(t -> t.getCalculated().getCost())).toList());
        return response;
    }
    
    @Override
    public TransportWithCalculateDTO calculateTransport(
            TripDto tripData, UUID transportId, long startDate, long endDate, Employee employee,
            String token
                                                       ) {
        
        var regionBranch = getRegionBranch(tripData);
        formatTripDateBasedOnRegionTimeZone(tripData, regionBranch);
        var regions = regionBranch.stream().map(RegionDto::getId).collect(Collectors.toList());
        var rawTariffs = repository.findAllTariff(tripData.getTripDate().toLocalDate(),
                                               tripData.getOrganizationId(),
                                               ConvertUtils.setToStringArray(regions),
                                               ConvertUtils.setToStringArray(Set.of(transportId)));
        var filteredTariffs = rawTariffs.stream().filter(q -> q.getContract().getContractType() == ContractType.INCOME).toList();
        if (filteredTariffs.isEmpty()) {
            filteredTariffs = rawTariffs.stream().filter(q -> q.getContract().getContractType() == ContractType.TRANSITIONAL).toList();
        }
        if (filteredTariffs.isEmpty()) {
            throw new EntityNotFoundException(GroupTransferTariff.class, transportId);
        }
        var contractorId = filteredTariffs.getFirst().getContractorId();
        var response = contractorClient.getTransport(toOffsetDateTime(startDate, tripData.getTimeZone()),
                                                     toOffsetDateTime(endDate, tripData.getTimeZone()),
                                                     contractorId,
                                                     transportId,
                                                     token)
                                       .stream().map(t -> toClientTimeZone(t, tripData.getClientTimeZone())).toList();
        HashMap<TransportRegionPair, GroupTransferTariff> tariffMap = toTransportRegionMap(filteredTariffs);
        GroupTransferTariff tariff = getGroupTransferTariff(transportId, regions, tariffMap).orElse(null);
        if (tariff == null) {
            throw new RuntimeException("Tariff for transfer transport not found");
        }
        var cost = calculateGroupTransfer(tariff, tripData);
        var newDTO = TransportWithCalculateDTO.builder().trips(response).build();
        newDTO.setCalculated(CalculatedDto.builder()
                                          .id(tariff.getId())
                                          .cost(cost)
                                          .build());
        var outcomeTariffList = tariffService.getAll(tariff.getTransportType(), List.of(tariff.getRegionId()), null, true, ContractType.OUTCOME);
        var outcomeTariffContractList = outcomeTariffList
                .stream()
                .filter(q->BaseTariffWithContract.class.isAssignableFrom(q.getClass()))
                .map(q->(BaseTariffWithContract)q)
                .toList();
        findOutcomeTariff(tripData, newDTO.getCalculated(), tariff, outcomeTariffContractList, employee);
        return newDTO;
    }
    
    private TaskDTO toClientTimeZone(TaskDTO dto, String clientTimeZone) {
        return TaskDTO.builder()
                      .start(dto.getStart().withOffsetSameLocal(ConvertUtils.toOffset(ZoneId.of(clientTimeZone))))
                      .end(dto.getEnd().withOffsetSameLocal(ConvertUtils.toOffset(ZoneId.of(clientTimeZone))))
                      .build();
    }
    
    private HashMap<TransportRegionPair, GroupTransferTariff> toTransportRegionMap(List<GroupTransferTariff> tariffs) {
        var tariffMap = new HashMap<TransportRegionPair, GroupTransferTariff>();
        for (var tariff : tariffs) {
            for (var _regionId : tariff.getRegionIds()) {
                for (var _transportId : tariff.getTransportIds()) {
                    tariffMap.put(TransportRegionPair.builder().regionId(_regionId).transportId(_transportId).build(), tariff);
                }
            }
        }
        return tariffMap;
    }
    
    private Optional<GroupTransferTariff> getGroupTransferTariff(
            UUID transportId, List<UUID> regions, HashMap<TransportRegionPair, GroupTransferTariff> tariffMap
                                                                ) {
        GroupTransferTariff tariff = null;
        for (var region : regions) {
            tariff = tariffMap.get(TransportRegionPair.builder().regionId(region).transportId(transportId).build());
            if (tariff != null) {
                break;
            }
        }
        return Optional.ofNullable(tariff);
    }
    
    private OffsetDateTime toOffsetDateTime(LocalDateTime date, String timezone) {
        
        return OffsetDateTime.of(date, ZoneOffset.of(timezone))
                             .withOffsetSameLocal(ZoneOffset.UTC)
                             .withOffsetSameInstant(ZoneOffset.of(timezone));
    }
    
    private OffsetDateTime toOffsetDateTime(long time, String timezone) {
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.of(timezone));
    }
    
    private void formatTripDateBasedOnRegionTimeZone(TripDto tripData, List<RegionDto> regionBranch) {
        var regionTimeZone = regionBranch.stream().map(RegionDto::getTimeZone).filter(Objects::nonNull).findFirst().orElse(null);
        if (regionTimeZone == null) {
            throw new RegionResolvingFailedException("Не удалось получить таймзону для региона");
        }
        //Запоминаем клиентскую таймзону
        tripData.setClientTimeZone(tripData.getTimeZone());
        /*
         * Приводим время относительно региона
         * C фронта приходит UTC 07:30
         * Приводим ко времени пользователя 10:30 для GMT+3
         * Меняем на зону для региона (например GMT+10), получаем 10:30 GMT+10
         * Приводим к UTC и получаем 00:30
         */
        var utcTripDateBasedOnRegionTimeZone = tripData.getTripDate()
                                                       .atZone(ZoneId.of("UTC"))
                                                       .withZoneSameInstant(ZoneId.of(tripData.getTimeZone()))
                                                       .withZoneSameLocal(ZoneId.of(regionTimeZone))
                                                       .withZoneSameInstant(ZoneId.of("UTC"))
                                                       .toLocalDateTime();
        
        log.debug("tripTime: {}, client time zone: {}, region time zone: {}, final time {}", tripData.getTripDate(), tripData.getTimeZone(),
                  regionTimeZone, utcTripDateBasedOnRegionTimeZone);
        
        tripData.setTripDate(utcTripDateBasedOnRegionTimeZone);
        tripData.setTimeZone(regionTimeZone);
    }
    
    private boolean isNightTrip(TripDto tripData) {
        if (tripData == null || tripData.getTripDate() == null) {
            log.debug("Дата/время поездки не определены => не можем утверждать, что поездка является ночной");
            return false;
        }
        var tripTime = getTripTime(tripData);
        return (tripTime.getHour() >= 22 || tripTime.getHour() < 6);
    }
    
    private LocalDateTime getTripTime(TripDto tripData) {
        if (tripData == null || tripData.getTripDate() == null) {
            throw new IllegalArgumentException("Дата/время поездки не определены => рассчитать время поездки с учетом тайм-зоны невозможно");
        }
        String timeZone = tripData.getTimeZone();
        if (timeZone == null) {
            log.debug("Тайм-зона не определена => расчет времени поездки сделан по GMT+03");
            timeZone = "GMT+03";
        }
        return ZonedDateTime.of(tripData.getTripDate(), ZoneId.of("UTC"))
                            .withZoneSameInstant(ZoneId.of(timeZone))
                            .toLocalDateTime();
    }
    
    private boolean hasTaxiTariffs(Collection<? extends CalculatedDto> tariffs) {
        return tariffs.stream().anyMatch(t -> t.getTransportType().getId().equals(TransportTypeEnum.TAXI.getId()));
    }
    
    public Collection<? extends CalculatedDto> calculateCommon(
            TripDto tripData, Employee employee, List<RegionDto> regionBranch,
            Boolean deleteNonNightTariffTaxi, boolean checkLimits, List<TransportTypeEnum> transportTypeList
                                                              ) {
        // по адресу начала маршрута определить иерархию гео-зон
        // произвести поиск всех действующих тарифов для перевозки сотрудников доступных для наиболее детальной гео-зоны для каждого вида транспорта
        log.debug("Using findAllTariffs, transportTypes - " + transportTypeList.stream().map(TransportTypeEnum::name).collect(
                Collectors.joining(",")));
        var allTariffList = findAllTariffs(tripData, employee, regionBranch, deleteNonNightTariffTaxi, transportTypeList, false);
        
        // среди найденных тарифов такси выбрать один контракт в зависимости от веса контракта
        // собрать тарифы такси относящиеся к этому контракту и все остальные тарифы и отправить на фронт
        List<? extends BaseTariff> baseTariffs = processTaxiTariffs(allTariffList, true);
        baseTariffs = processGroupTransferTariffs(baseTariffs, tripData, true);
        
        List<CalculatedDto> calculatedTariffs = baseTariffs
                .stream()
                .map(tariff -> calculateData(tariff, tripData))
                .toList();
        
        calculatedTariffs = filterByAvailableTaxiClasses(calculatedTariffs, employee);
        
        if (checkLimits) {
            fillLimitsAvailable(calculatedTariffs, tripData, employee);
        }
        
        var outcomeTariffList = findAllTariffs(tripData, employee, regionBranch, deleteNonNightTariffTaxi, transportTypeList, true)
                .stream()
                .filter(q->BaseTariffWithContract.class.isAssignableFrom(q.getClass()))
                .map(q->(BaseTariffWithContract)q)
                .filter(q->q.getContract().getContractType() == ContractType.OUTCOME)
                .toList();
        findOutcomeTariff(tripData, calculatedTariffs, baseTariffs, outcomeTariffList, employee);
        return calculatedTariffs;
    }
    
    private void findOutcomeTariff(
            TripDto tripData,
            List<CalculatedDto> calculatedIncomeTariffs,
            List<? extends BaseTariff> incomeTariffs,
            List<BaseTariffWithContract> outcomeTariffs,
            Employee employee
                                  ) {
        var incomeTariffsMap = incomeTariffs.stream().collect(Collectors.toMap(BaseTariff::getId, Function.identity()));
        for (var calculatedIncomeTariff : calculatedIncomeTariffs) {
            var incomeTariff = incomeTariffsMap.get(calculatedIncomeTariff.getId());
            findOutcomeTariff(tripData, calculatedIncomeTariff, incomeTariff, outcomeTariffs, employee);
        }
    }
    
    private void findOutcomeTariff(
            TripDto tripData,
            CalculatedDto calculatedIncomeTariff,
            BaseTariff incomeTariff,
            List<BaseTariffWithContract> outcomeTariffs,
            Employee employee
                                  ) {
        
        if (!List.of(TransportTypeEnum.TAXI, TransportTypeEnum.CARSHARING).contains(incomeTariff.getTransportType())) {
            calculatedIncomeTariff.setOutcomeTariffId(incomeTariff.getId());
            calculatedIncomeTariff.setOutcomeCost(calculatedIncomeTariff.getCost());
            return;
        }
        
        var baseTariffWithContract = (BaseTariffWithContract) incomeTariff;
        if (baseTariffWithContract.getContract().getContractType() == ContractType.OUTCOME) {
            throw new RuntimeException("Нельзя подбирать расходный тариф под расходный");
        }
        if (baseTariffWithContract.getContract().getContractType() == ContractType.TRANSITIONAL) {
            calculatedIncomeTariff.setOutcomeTariffId(incomeTariff.getId());
            calculatedIncomeTariff.setOutcomeCost(calculatedIncomeTariff.getCost());
            return;
        }
        
        var organizationIds = baseTariffWithContract.getContract().getOrganizations().stream().map(Organization::getId).toList();
        var restrictedContractors = connectionRestrictionRepository.findByOrganizationIdIn(organizationIds)
                                                          .stream()
                                                          .map(ConnectionRestriction::getContractor)
                                                          .map(Contractor::getId)
                                                          .collect(Collectors.toSet());
        
        var filteredOutcomeTariffs = outcomeTariffs
                .stream()
                .filter(q -> List.of(TransportTypeEnum.TAXI, TransportTypeEnum.CARSHARING).contains(q.getTransportType()))
                .filter(q -> calculateData(q, tripData).getCost() <= calculatedIncomeTariff.getCost())
                .filter(q -> q.getContract().getContractType() == ContractType.OUTCOME)
                .filter(q -> !restrictedContractors.contains(q.getContractorId()))
                .toList();
        
        var baseTariffs = processTaxiTariffs(filteredOutcomeTariffs, false);
        baseTariffs = processGroupTransferTariffs(filteredOutcomeTariffs, tripData, false);
        
        List<CalculatedDto> calculatedTariffs = baseTariffs
                .stream()
                .map(tariff -> calculateData(tariff, tripData))
                .toList();
        
        calculatedTariffs = filterByAvailableTaxiClasses(calculatedTariffs, employee);
        
        if (incomeTariff.getTransportType() == TransportTypeEnum.TAXI) {
            var taxiOutcomeTariffs = calculatedTariffs
                    .stream()
                    .filter(q -> q.getTransportType().getName().equals(TransportTypeEnum.TAXI.getName()))
                    .filter(q -> q.getTaxiClass() == calculatedIncomeTariff.getTaxiClass())
                    .toList();
            
            if (taxiOutcomeTariffs.isEmpty()) {
                throw new RuntimeException("Не найден расходный тариф");
            }
            
            if (taxiOutcomeTariffs.size() != 1) {
                throw new RuntimeException("Должен быть только один найденный расходный тариф");
            }
            
            var taxiOutcomeTariff = taxiOutcomeTariffs.getFirst();
            calculatedIncomeTariff.setOutcomeTariffId(taxiOutcomeTariff.getId());
            calculatedIncomeTariff.setOutcomeCost(taxiOutcomeTariff.getCost());
        } else if (incomeTariff.getTransportType() == TransportTypeEnum.CARSHARING) {
            var carsharingOutcomeTariffs = calculatedTariffs
                    .stream()
                    .filter(q -> q.getTransportType().getName().equals(TransportTypeEnum.CARSHARING.getName()))
                    .filter(q -> Objects.equals(q.getGroupTransferClass(), calculatedIncomeTariff.getGroupTransferClass()))
                    .toList();
            
            if (carsharingOutcomeTariffs.isEmpty()) {
                throw new RuntimeException("Не найден расходный тариф");
            }
            
            if (carsharingOutcomeTariffs.size() != 1) {
                throw new RuntimeException("Должен быть только один найденный расходный тариф");
            }
            
            var carsharingOutcomeTariff = carsharingOutcomeTariffs.getFirst();
            calculatedIncomeTariff.setOutcomeTariffId(carsharingOutcomeTariff.getId());
            calculatedIncomeTariff.setOutcomeCost(carsharingOutcomeTariff.getCost());
        }
    }
    
    private List<CalculatedDto> filterByAvailableTaxiClasses(List<CalculatedDto> calculatedTariffs, Employee employee) {
        Set<TaxiClass> availableTaxiClasses = getAvailableTaxiClasses(employee);
        return calculatedTariffs.stream()
                                .filter(calculatedDto -> (!calculatedDto.getTransportType().getId().equals(TransportTypeEnum.TAXI.getId())) ||
                                                         (calculatedDto.getTransportType().getId().equals(TransportTypeEnum.TAXI.getId()) &&
                                                          availableTaxiClasses.contains(calculatedDto.getTaxiClass())
                                                         )
                                       )
                                .toList();
    }
    
    private Set<TaxiClass> getAvailableTaxiClasses(Employee employee) {
        
        Set<TaxiClass> noAvailableTaxiClasses = new HashSet<>();
        
        if (employee == null) {
            log.error("Сотрудник не определен. В качестве доступных классов такси принимается значение по умолчанию: {}",
                      defaultAvailableTaxiClasses);
            return defaultAvailableTaxiClasses;
        }
        if (employee.getPositionId() == null) {
            log.error("Должность сотрудника c lastName={}, firstName={} не определена. В качестве доступных классов такси принимается значение по " +
                      "умолчанию: {}", employee.getLastName(), employee.getFirstName(), defaultAvailableTaxiClasses);
            return defaultAvailableTaxiClasses;
        }
        Position position = null;
        try {
            position = positionRepository.getReferenceById(employee.getPositionId());
        } catch (jakarta.persistence.EntityNotFoundException ex) {
            log.error(ex.getMessage());
        }
        if (position == null) {
            log.error("Должность сотрудника c lastName={}, firstName={} по указанному positionId={} не найдена. В качестве доступных классов " +
                      "такси принимается значение по умолчанию: {}",
                      employee.getLastName(), employee.getFirstName(), employee.getPositionId(), defaultAvailableTaxiClasses);
            return defaultAvailableTaxiClasses;
        }
        if (position.getAvailableClasses() == null || position.getAvailableClasses().isEmpty()) {
            log.warn("Для должности сотрудника lastName={}, firstName={} с positionId={} не найдено доступных классов такси",
                     employee.getLastName(), employee.getFirstName(), employee.getPositionId());
            return noAvailableTaxiClasses;
        }
        
        return position.getAvailableClasses();
    }
    
    public List<? extends BaseTariff> findAllTariffs(
            @NotNull TripDto tripDto, Employee employee, List<RegionDto> regionBranch, Boolean isNightTrip
                                                    ) {
        return findAllTariffs(tripDto, employee, regionBranch, isNightTrip, Collections.emptyList(), false);
    }
    
    public List<? extends BaseTariff> findAllTariffs(
            @NotNull TripDto tripDto, Employee employee, List<RegionDto> regionBranch, Boolean isNightTrip, List<TransportTypeEnum> transportTypeList,
            boolean outcome
                                                    ) {
        if (regionBranch.isEmpty()) {
            return Collections.emptyList();
        }
        
        //Разбиваем тарифы на тип транспорта/класс
        var foundTariffs = new EnumMap<TransportTypeEnum, Map<String, List<BaseTariff>>>(TransportTypeEnum.class);
        
        for (RegionDto selectedRegion : regionBranch) {
            //Ищем все активные тарифы на организацию и регион
            log.debug("Using getAll tariffs, transportTypes - {}", transportTypeList.stream().map(TransportTypeEnum::name).collect(
                    Collectors.joining(",")));
            List<? extends BaseTariff> allTariffList;
            if (outcome) {
                allTariffList = tariffService.getAll(selectedRegion.getId(), null,
                                                     true, TransportServiceType.EMPLOYEE_TRANSPORTATION, transportTypeList, ContractType.OUTCOME);
            } else {
                allTariffList = tariffService.getAll(selectedRegion.getId(), tripDto.getOrganizationId(),
                                                     true, TransportServiceType.EMPLOYEE_TRANSPORTATION, transportTypeList, null);
            }
            
            //Исключаем тарифы других департаментов
            removeAnotherDepartmentTariffs(allTariffList, employee.getDepartmentId());
            //Исключаем тарифы такси по признаку ночного тарифа
            removeUnnecessaryTariffs(allTariffList, isNightTrip);
            
            allTariffList.forEach(baseTariff -> {
                var foundMapByTransportType = foundTariffs.getOrDefault(baseTariff.getTransportType(), new HashMap<>());
                foundTariffs.putIfAbsent(baseTariff.getTransportType(), foundMapByTransportType);
                
                var foundTariffsByTransportClass = foundMapByTransportType.getOrDefault(getClassName(baseTariff),
                                                                                        new ArrayList<>());
                foundMapByTransportType.putIfAbsent(getClassName(baseTariff), foundTariffsByTransportClass);
                
                // Если список пустой, то добавляем тариф
                if (foundTariffsByTransportClass.isEmpty()) {
                    foundTariffsByTransportClass.add(baseTariff);
                    return;
                }
                
                //Если в списке уже есть тарифы
                //Берём первый тариф из списка
                var firstTariff = foundTariffsByTransportClass.getFirst();
                
                // Если тариф на департамент и на ту же гео зону - добавляем список
                if (firstTariff.getDepartment() != null && baseTariff.getDepartment() != null &&
                    firstTariff.getRegionId().equals(baseTariff.getRegionId())) {
                    foundTariffsByTransportClass.add(baseTariff);
                    return;
                }
                
                // Если тариф для организации и новый тариф так же для организации и у них одинаковая геозона - добавляем
                if (firstTariff.getDepartment() == null && baseTariff.getDepartment() == null &&
                    firstTariff.getRegionId().equals(baseTariff.getRegionId())) {
                    foundTariffsByTransportClass.add(baseTariff);
                    return;
                }
                
                // Если тариф на организацию, а новый тариф на департамент - чистим список и добавляем тариф для департамента
                if (firstTariff.getDepartment() == null && baseTariff.getDepartment() != null) {
                    foundTariffsByTransportClass.clear();
                    foundTariffsByTransportClass.add(baseTariff);
                }
            });
        }
        return foundTariffs.values().stream().map(Map::values).flatMap(Collection::stream).flatMap(List::stream).toList();
    }

    private String getClassName(BaseTariff baseTariff) {
        return switch (baseTariff.getTransportType()) {
            case TAXI -> ((TaxiTariff) baseTariff).getTaxiClass().name();
            case GROUP_TRANSFER -> ((GroupTransferTariff) baseTariff).getGroupTransferClass().name();
            default -> null;
        };
    }

    /**
     * @param tariffList список тарифов
     * @param departmentId id подразделения пассажира
     */
    private void removeAnotherDepartmentTariffs(List<? extends BaseTariff> tariffList, UUID departmentId) {
        if (departmentId != null) {
            tariffList.removeIf(tariff -> tariff.getDepartment() != null &&
                                          !departmentId.equals(tariff.getDepartment().getId()));
        }
    }
    
    private void removeUnnecessaryTariffs(List<? extends BaseTariff> tariffList, boolean isNightTrip) {
        tariffList.removeIf(tariff -> tariff instanceof TaxiTariff taxiTariff &&
                                      ((!taxiTariff.getIsNightTariff() && isNightTrip) || (taxiTariff.getIsNightTariff() && !isNightTrip)));
    }
    
    @Override
    public Collection<? extends CalculatedDto> calculate(TransportTypeEnum transportTypeEnum, TripDto tripData, Employee employee) {
        return calculateDefault(tripData, getRegionId(tripData.getStartPoint()), transportTypeEnum, employee);
    }
    
    private Collection<? extends CalculatedDto> calculateDefault(
            TripDto tripData, UUID regionId, TransportTypeEnum transportTypeEnum,
            Employee employee
                                                                ) {
        var tariffs = tariffService.getAll(transportTypeEnum, regionId, tripData.getOrganizationId(), true, null);
        List<CalculatedDto> calculatedDtoList = tariffs
                .stream()
                .map(tariff -> calculateData(tariff, tripData)).toList();
        
        fillLimitsAvailable(calculatedDtoList, tripData, employee);
        var outcomeTariffList = tariffService.getAll(transportTypeEnum, List.of(regionId), null, true, ContractType.OUTCOME);
        var outcomeTariffContractList = outcomeTariffList
                .stream()
                .filter(q->BaseTariffWithContract.class.isAssignableFrom(q.getClass()))
                .map(q->(BaseTariffWithContract)q)
                .toList();
        findOutcomeTariff(tripData, calculatedDtoList, tariffs, outcomeTariffContractList, employee);
        return calculatedDtoList;
    }
    
    /**
     * Calculate trip data with tariff.
     *
     * @param tariff tariff.
     * @param tripData trip data.
     *
     * @return calculated data.
     */
    private CalculatedDto calculateData(BaseTariff tariff, TripDto tripData) {
        var transportType = tariff.getTransportType();
        
        long calculated = calculateByTransportType(tariff, tripData);
        
        TaxiClass taxiClass = null;
        Integer minCancelTimeMin = null;
        if (tariff instanceof TaxiTariff taxiTariff) {
            taxiClass = taxiTariff.getTaxiClass();
            minCancelTimeMin = taxiTariff.getCoopTariffParams() == null ?
                               CoopTariffParams.DEFAULT_MIN_CANCEL_TIME :
                               taxiTariff.getCoopTariffParams().getMinCancelTimeMin();
            
        }
        Double engineCoefficient = null;
        if (tariff instanceof PersonalTariff personalTariff) {
            engineCoefficient = getEngineCoefficient(personalTariff.getEngineTariffParams(), tripData);
        }
        
        String carSharingCompName = null;
        UUID carSharingCompId = null;
        if (tariff instanceof CarSharingTariff carSharingTariff) {
            UUID contractorId = carSharingTariff.getContract().getContractorId();
            Contractor contractor =
                    contractorService.get(contractorId).orElseThrow(() -> new EntityNotFoundException(Contractor.class, contractorId));
            carSharingCompName = contractor.getName();
            carSharingCompId = contractorId;
        }
        String groupTransferClass = null;
        if (tariff instanceof GroupTransferTariff groupTransferTariff) {
            groupTransferClass = groupTransferTariff.getGroupTransferClass().name();
        }
        return CalculatedDto.builder()
                            .cost(calculated)
                            .id(tariff.getId())
                            .transportType(mapper.mapTransportTypeToDTO(transportType))
                            .priceDetail("taxiClass", taxiClass)
                            .priceDetail("minCancelTimeMin", minCancelTimeMin)
                            .priceDetail(ENGINE_VOLUME_COEFFICIENT, engineCoefficient)
                            .priceDetail("carSharingCompName", carSharingCompName)
                            .priceDetail("carSharingCompId", carSharingCompId)
                            .taxiClass(taxiClass)
                            .groupTransferClass(groupTransferClass)
                            .build();
    }
    
    /**
     * @param tariff тариф для расчета
     * @param tripDto данные поездки для расчёта
     *
     * @return стоимость поездки
     */
    protected long calculateByTransportType(BaseTariff tariff, TripDto tripDto) {
        var calculated = switch (tariff.getTransportType()) {
            case TAXI -> calculateTaxi((TaxiTariff) tariff, tripDto);
            case PERSONAL -> calculatePersonal((PersonalTariff) tariff, tripDto);
            case CARSHARING -> calculateCarSharing((CarSharingTariff) tariff, tripDto);
            case BICYCLE -> calculateBicycle((BicycleTariff) tariff, tripDto);
            case SCOOTER -> calculateScooter((ScooterTariff) tariff, tripDto);
            case PUBLIC -> calculatePublic((PublicTariff) tariff, tripDto);
            case GROUP_TRANSFER -> calculateGroupTransfer((GroupTransferTariff) tariff, tripDto);
            default -> 0;
        };
        log.debug("Price before rounding: %s".formatted(calculated));
        calculated = Math.round(Math.round(calculated / 100.0)) * 100L;
        log.debug("Price after rounding: %s".formatted(calculated));
        return calculated;
    }
    
    
    private void fillLimitsAvailable(List<CalculatedDto> tariffs, TripDto tripDto, Employee employee) {
        
        if (tariffs.isEmpty()) {
            return;
        }
        
        List<LimitReservationModel.LimitReservationRequest> limitReservationRequests = tariffs.stream().map(tariff ->
                                                                                                                    LimitReservationModel.LimitReservationRequest
                                                                                                                            .newBuilder()
                                                                                                                            .setOrganizationId(tripDto
                                                                                                                                                       .getOrganizationId()
                                                                                                                                                       .toString())
                                                                                                                            .setDepartmentId(employee
                                                                                                                                                     .getDepartmentId()
                                                                                                                                                     .toString())
                                                                                                                            .setEmployeeId(
                                                                                                                                    employee.getId()
                                                                                                                                            .toString())
                                                                                                                            .setTransportType(tariff
                                                                                                                                                      .getTransportType()
                                                                                                                                                      .getName())
                                                                                                                            .setSum(tariff.getCost())
                                                                                                                            .setPlannedDate(
                                                                                                                                    convertLocalDateTimeToGoogleTimestamp(
                                                                                                                                            tripDto.getTripDate()))
                                                                                                                            .setCheckLimit(true)
                                                                                                                            .setRequestId(
                                                                                                                                    tariff.getId()
                                                                                                                                          .toString())
                                                                                                                            .build()
                                                                                                           ).toList();
        LimitReservationModel.LimitReservationRequestList request = LimitReservationModel.LimitReservationRequestList.newBuilder()
                                                                                                                     .setUserId(employee.getId()
                                                                                                                                        .toString())
                                                                                                                     .addAllListLimitReservations(
                                                                                                                             limitReservationRequests)
                                                                                                                     .build();
        
        var secContext = SecurityContextHolder.getContext();
        var authentication = secContext.getAuthentication();
        secContext.setAuthentication(null);
        LimitReservationModel.LimitReservationResponseList limitReservationResponseList = stub.limitReservation(request);
        processLimitReservationResponse(limitReservationResponseList, tariffs);
        secContext.setAuthentication(authentication);
    }
    
    private com.google.protobuf.Timestamp convertLocalDateTimeToGoogleTimestamp(LocalDateTime localDateTime) {
        Instant instant = localDateTime.toInstant(ZoneOffset.UTC);
        
        return com.google.protobuf.Timestamp.newBuilder()
                                            .setSeconds(instant.getEpochSecond())
                                            .setNanos(instant.getNano())
                                            .build();
    }
    
    private void processLimitReservationResponse(
            LimitReservationModel.LimitReservationResponseList limitReservationResponseList,
            List<CalculatedDto> tariffs
                                                ) {
        Map<String, String> collect = limitReservationResponseList.getLimitReservationResponseList().stream()
                                                                  .collect(Collectors.toMap(
                                                                          LimitReservationModel.LimitReservationResponse::getTripRequestId,
                                                                          LimitReservationModel.LimitReservationResponse::getLimitReservationStatus));
        
        tariffs.forEach(tariff ->
                                tariff.setLimitAvailable("RESERVED_FROM_EMPLOYEE".equals(collect.get(tariff.getId().toString())) ||
                                                         "RESERVED_FROM_DEPARTMENT".equals(collect.get(tariff.getId().toString())) ||
                                                         tariff.getTransportType().getName().equals(TransportTypeEnum.GROUP_TRANSFER.getName()))
                       );
    }
    
    private long calculateTaxi(TaxiTariff tariff, TripDto tripData) {
        return (long) ((calculateTaxiBasicCost(tariff, tripData)
                        + calculateTaxiSuburbCost(tariff.getSuburbTariffParams(), tripData.getSuburbTripData()))
                       * calculateTotalTaxiCoefficient(tariff, tripData));
    }
    
    protected long calculatePersonal(PersonalTariff tariff, TripDto tripDto) {
        long calculatePersonal = (long) ((calculatePersonalBasicCost(tariff, tripDto) +
                                          calculatePersonalSuburbCost(tariff.getSuburbTariffParams(), tripDto.getSuburbTripData()))
                                         * calculateTotalPersonalCoefficient(tariff, tripDto));
        long minCost = Long.max(tariff.getMinRideDistanceCost(),
                                tariff.getMinRideTimeCost());
        calculatePersonal = Math.max(calculatePersonal, minCost);
        return calculatePersonal;
    }
    
    private long calculateCarSharing(CarSharingTariff tariff, TripDto tripDto) {
        return (long) (calculateCarSharingBasicCost(tariff, tripDto) * calculateCarSharingCoefficient(tariff, tripDto));
    }
    
    private long calculateBicycle(BicycleTariff tariff, TripDto tripDto) {
        return (long) (calculateBicycleBasicCost(tariff, tripDto) * calculateBicycleCoefficient(tariff, tripDto));
    }
    
    private long calculateScooter(ScooterTariff tariff, TripDto tripDto) {
        return (long) (calculateScooterBasicCost(tariff, tripDto) * calculateScooterCoefficient(tariff, tripDto));
    }
    
    private long calculatePublic(PublicTariff tariff, TripDto tripDto) {
        var publicTripData = tripDto.getPublicTripData();
        
        var busAvailability = tariff.isBusAvailability();
        var tramAvailability = tariff.isTramAvailability();
        var trolleybusAvailability = tariff.isTrolleybusAvailability();
        var metroAvailability = tariff.isMetroAvailability();
        
        var busTicketCost = tariff.getBusTicketCost();
        var tramTicketCost = tariff.getTramTicketCost();
        var trolleybusTicketCost = tariff.getTrolleybusTicketCost();
        var metroTicketCost = tariff.getMetroTicketCost();
        
        var minCost = getMinCost(busAvailability, busTicketCost, 0);
        minCost = getMinCost(tramAvailability, tramTicketCost, minCost);
        minCost = getMinCost(trolleybusAvailability, trolleybusTicketCost, minCost);
        minCost = getMinCost(metroAvailability, metroTicketCost, minCost);
        
        if (publicTripData != null) {
            var metroTicketsQuantity = publicTripData.getMetroTicketsQuantity();
            var tramTicketsQuantity = publicTripData.getTramTicketsQuantity();
            var trolleybusTicketsQuantity = publicTripData.getTrolleybusTicketsQuantity();
            var busTicketsQuantity = publicTripData.getBusTicketsQuantity();
            
            var cost = (metroAvailability && metroTicketsQuantity != null ?
                        (long) metroTicketsQuantity * metroTicketCost : 0L) +
                       (tramAvailability && tramTicketsQuantity != null ?
                        (long) tramTicketsQuantity * tramTicketCost : 0L) +
                       (trolleybusAvailability && trolleybusTicketsQuantity != null ?
                        (long) trolleybusTicketsQuantity * trolleybusTicketCost : 0L) +
                       (busAvailability && busTicketsQuantity != null ?
                        (long) busTicketsQuantity * busTicketCost : 0L);
            
            return cost == 0 ? minCost : cost;
        }
        return minCost;
    }
    
    private long calculateGroupTransfer(GroupTransferTariff tariff, TripDto tripDto) {
        
        if (tariff.getMinKm() > Math.round(tripDto.getDistance()) || tariff.getMinMin() > tripDto.getTime().toMinutes()) {
            return Optional.ofNullable(tariff.getMinRideCost()).orElse(0);
        }
        
        var isSuburb = tripDto.getWaypoints().stream().map(WaypointDTO::getCity).collect(Collectors.toSet()).size() > 1;
        var costWaitTime = tripDto.getWaypoints()
                                  .stream().map(WaypointDTO::getWaitTime)
                                  .map(Duration::toSeconds)
                                  .map(d -> Duration.of(d / 1000, ChronoUnit.SECONDS))
                                  .map(wt -> Math.max(wt.toMinutes() - tariff.getFreeWaitingTime(), 0))
                                  .mapToLong(Long::longValue).sum() * tariff.getWaitCostPerMin();
        var costPerKm = 0;
        var costPerMin = 0;
        
        if (isSuburb) {
            costPerKm = tariff.getCostPerKmSuburb() == 0 ? tariff.getRideCostPerKm() : tariff.getCostPerKmSuburb();
            costPerMin = tariff.getCostPerMinSuburb() == 0 ? tariff.getRideCostPerMin() : tariff.getCostPerMinSuburb();
        } else {
            costPerKm = tariff.getCostPerKmCity() == 0 ? tariff.getRideCostPerKm() : tariff.getCostPerKmCity();
            costPerMin = tariff.getCostPerMinCity() == 0 ? tariff.getRideCostPerMin() : tariff.getCostPerMinCity();
        }
        
        return (costPerMin * Math.max(tripDto.getTime().toMinutes() - tariff.getMinMin(), 0)) +
               (long) (costPerKm * Math.max(tripDto.getDistance() - tariff.getMinKm(), 0)) +
               costWaitTime +
               tariff.getMinRideCost();
    }
    
    private List<? extends BaseTariff> processTaxiTariffs(List<? extends BaseTariff> allTariffList, boolean income) {
        var taxiTariffList = allTariffList.stream()
                                          .filter(tariff -> TransportTypeEnum.TAXI.equals(tariff.getTransportType()))
                                          .map(TaxiTariff.class::cast)
                                          .filter(e -> Objects.nonNull(e.getContract()))
                                          .toList();
        
        if (income) {
            taxiTariffList = taxiTariffList.stream().filter(e -> e.getContract().getContractType() != ContractType.OUTCOME).toList();
            var incomeTariffs = taxiTariffList.stream().filter(q -> q.getContract().getContractType() == ContractType.INCOME).toList();
            if (!incomeTariffs.isEmpty()) {
                taxiTariffList = incomeTariffs;
            }
        } else {
            taxiTariffList = taxiTariffList.stream().filter(e -> e.getContract().getContractType() == ContractType.OUTCOME).toList();
        }
        
        var tariffListNew = new ArrayList<>(allTariffList.stream()
                                                         .filter(tariff -> !TransportTypeEnum.TAXI.equals(tariff.getTransportType()))
                                                         .map(BaseTariff.class::cast)
                                                         .toList());
        
        if (taxiTariffList.isEmpty()) {
            log.info("No taxi tariffs");
            return tariffListNew;
        }
        
        Map<TaxiClass, List<TaxiTariff>> taxiTariffMap = taxiTariffList.stream().collect(Collectors.groupingBy(TaxiTariff::getTaxiClass));
        List<Contract> contracts = new ArrayList<>(Collections.emptyList());
        contracts.addAll(
                taxiTariffList.stream().map(BaseTariffWithContract::getContract).filter(Contract::isActive).distinct().toList());
        // Выбираем контракт в рамках каждого класса такси
        for (var tariffEntry : taxiTariffMap.entrySet()) {
            var contract = chooseContract(contracts, tariffEntry.getValue());
            tariffEntry.getValue().stream()
                       .filter(e -> e.getContract().equals(contract))
                       .findAny()
                       .ifPresent(tariffListNew::add);
        }
        
        return tariffListNew;
    }
    
    private List<? extends BaseTariff> processGroupTransferTariffs(List<? extends BaseTariff> allTariffList, TripDto tripDto, boolean income) {
        var groupTransferTariffList = allTariffList.stream()
                                                   .filter(tariff -> TransportTypeEnum.GROUP_TRANSFER.equals(tariff.getTransportType()))
                                                   .map(GroupTransferTariff.class::cast)
                                                   .filter(e -> e.isVip() == tripDto.isVip())
                                                   .filter(e -> Objects.equals(tripDto.getInformation().isAnimal(), e.getAnimal()))
                                                   .filter(e -> Objects.equals(tripDto.getInformation().isAnimal(), e.getBugOversized()))
                                                   .filter(e -> Objects.equals(tripDto.getInformation().isChildSeat(), e.getChildSeat()))
                                                   .filter(e -> Objects.nonNull(e.getContract()))
                                                   .toList();
        
        if (income) {
            groupTransferTariffList = groupTransferTariffList.stream().filter(e -> e.getContract().getContractType() != ContractType.OUTCOME).toList();
            var incomeTariffs = groupTransferTariffList.stream().filter(q -> q.getContract().getContractType() == ContractType.INCOME).toList();
            if (!incomeTariffs.isEmpty()) {
                groupTransferTariffList = incomeTariffs;
            }
        } else {
            groupTransferTariffList = groupTransferTariffList.stream().filter(e -> e.getContract().getContractType() == ContractType.OUTCOME).toList();
        }
        
        var tariffListNew = new ArrayList<>(allTariffList.stream()
                                                         .filter(tariff -> !TransportTypeEnum.GROUP_TRANSFER.equals(tariff.getTransportType()))
                                                         .map(BaseTariff.class::cast)
                                                         .toList());
        
        if (groupTransferTariffList.isEmpty() || tripDto.getWaypoints() == null) {
            log.info("No group transfer tariffs");
            return tariffListNew;
        }
        
        final var groupTransferTariffMap = groupTransferTariffList.stream().collect(Collectors.groupingBy(GroupTransferTariff::getGroupTransferClass));
        List<Contract> contracts = new ArrayList<>(Collections.emptyList());
        contracts.addAll(
                groupTransferTariffList.stream().map(BaseTariffWithContract::getContract).filter(Contract::isActive).distinct().toList());
        // Выбираем контракт в рамках каждого класса такси
        for (var tariffEntry : groupTransferTariffMap.entrySet()) {
            var contract = chooseContract(contracts, tariffEntry.getValue());
            tariffEntry.getValue().stream()
                       .filter(e -> e.getContract().equals(contract))
                       .findAny()
                       .ifPresent(tariffListNew::add);
        }
        
        return tariffListNew;
    }
    
    //Получение временного коэффициента, соответствующего планируемому времени заказа
    protected double getTimeCoefficient(@NotNull TimedTariffParams params, @NotNull LocalDateTime time) {
        if (time.getDayOfWeek() == DayOfWeek.SATURDAY || time.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return params.getCoefDayOff();
        }
        var localTime = time.toLocalTime();
        if (!localTime.isBefore(TimedTariffParams.MORNING_START) &&
                localTime.isBefore(TimedTariffParams.NOON_START)) {
            return params.getCoefWorkDayMorning();
        }
        if (!localTime.isBefore(TimedTariffParams.NOON_START) &&
                localTime.isBefore(TimedTariffParams.EVENING_START)) {
            return params.getCoefWorkDayNoon();
        }
        if (!localTime.isBefore(TimedTariffParams.EVENING_START) &&
                localTime.isBefore(TimedTariffParams.NIGHT_START)) {
            return params.getCoefWorkDayEvening();
        }
        if (!localTime.isBefore(TimedTariffParams.NIGHT_START) ||
                localTime.isBefore(TimedTariffParams.MORNING_START)) {
            return params.getCoefWorkDayNight();
        }
        return 1d;
    }
    
    //Расчет базовой стоимости поездки на такси
    protected long calculateTaxiBasicCost(@NotNull TaxiTariff tariff, @NotNull TripDto tripData) {
        long minRideDistanceCost = tariff.getMinRideDistanceCost();
        long distanceCost = (long) (minRideDistanceCost + Math.max(0,
                                                                   tripData.getDistance() -
                                                                   tariff.getDistanceIncluded()) *
                                                          tariff.getRideCostPerKm());
        long minRideTimeCost = tariff.getMinRideTimeCost();
        long timeCost = (minRideTimeCost + Math.max(0, ((tripData.getTime()
                                                                 .toMinutes()) - tariff.getTimeIncluded()) *
                                                       tariff.getRideCostPerMin()));
        var totalWaitingTime = tripData.getWaypoints().stream()
                                       .map(WaypointDTO::getWaitTime)
                                       .map(Duration::toSeconds)
                                       .map(d -> Duration.of(d / 1000, ChronoUnit.SECONDS))
                                       .reduce(Duration.ZERO, Duration::plus);
        long waitingPrice = Math.max(0, (tripData.getWaitingTime().toMinutes()) - tariff.getFreeWaitingTime()) * tariff.getWaitCostPerMin()
                            + tariff.getWaitCostPerMinIntermediate() * (totalWaitingTime.toMinutes());
        return distanceCost + timeCost + waitingPrice;
    }
    
    
    //Расчет стоимости поездки по области и между регионами
    protected long calculateTaxiSuburbCost(
            @NotNull SuburbTariffParams suburbTariffParams,
            @NotNull SuburbTripDataDTO suburbTripData
                                          ) {
        var suburbServiceCost = suburbTripData.getSuburbServiceDistance() * suburbTariffParams.getSuburbServiceCostPerKm()
                                + suburbTripData.getSuburbServiceTime() *
                                  (suburbTariffParams.getSuburbServiceCostPerMin());
        var suburbCost = calculateSuburbanTimeAndDistanceCost(suburbTariffParams, suburbTripData);
        return (long) suburbServiceCost + suburbCost;
    }
    
    //Получение общего коэффициента поездки на такси
    protected double calculateTotalTaxiCoefficient(@NotNull TaxiTariff taxiTariff, @NotNull TripDto tripDto) {
        var coefficient = 1d;
        LocalDateTime tripTime = getTripTime(tripDto);
        coefficient *= getTimeCoefficient(taxiTariff.getTimedTariffParams(), tripTime);
        if (tripDto.getTrafficJamScore() >= trafficThreshold) {
            coefficient *= (1 + taxiTariff.getCoefTraffic() *
                                (tripDto.getTrafficJamScore() - trafficThreshold));
        }
        Set<RequestOptions> options = tripDto.getOptions();
        if (options.contains(RequestOptions.BICYCLE_SKI_TRANSPORTATION)) {
            coefficient *= taxiTariff.getCoefBicycle();
        }
        if (options.contains(RequestOptions.CHILD_SEAT)) {
            coefficient *= taxiTariff.getCoefChildSeat();
        }
        if (options.contains(RequestOptions.PET_TRANSPORTATION)) {
            coefficient *= taxiTariff.getCoefPetTransport();
        }
        coefficient *= taxiTariff.getCoefOrg();
        return coefficient;
    }
    
    //Расчет базовой стоимости поездки
    protected long calculatePersonalBasicCost(@NotNull PersonalTariff tariff, @NotNull TripDto tripData) {
        long distanceCost = (long) tripData.getDistance() * tariff.getRideCostPerKm();
        long timeCost = tripData.getTime().toMinutes() * tariff.getRideCostPerMin();
        long waitingPrice = (tripData.getIntermediateWaitingTime().toMinutes()) * tariff.getWaitCostPerMin();
        return distanceCost + timeCost + waitingPrice;
    }
    
    //Расчет стоимости поездки по области и между регионами
    protected long calculatePersonalSuburbCost(
            @NotNull SuburbTariffParams suburbTariffParams,
            @NotNull SuburbTripDataDTO suburbTripData
                                              ) {
        return calculateSuburbanTimeAndDistanceCost(suburbTariffParams, suburbTripData);
    }
    
    protected double getEngineCoefficient(@NotNull EngineTariffParams engineTariffParams, @NotNull TripDto tripDto) {
        int engineVolume = tripDto.getEngineVolume();
        if (engineVolume < EngineTariffParams.BORDER_1_6) {
            return engineTariffParams.getCoefEngine1_6();
        }
        if (engineVolume < EngineTariffParams.BORDER_2_0) {
            return engineTariffParams.getCoefEngine1_6_to_2_0();
        }
        return engineTariffParams.getCoefEngine2_0_to_2_5();
    }
    
    protected double calculateTotalPersonalCoefficient(@NotNull PersonalTariff tariff, @NotNull TripDto tripDto) {
        var tripTime = getTripTime(tripDto);
        
        var coefficient = getSeasonalCoefficient(tariff, tripTime);
        coefficient *= getTimeCoefficient(tariff.getTimedTariffParams(), tripTime);
        if (tripDto.getTrafficJamScore() >= 7) {
            coefficient *= (1 + tariff.getCoefTraffic() * (tripDto.getTrafficJamScore() - trafficThreshold));
        }
        Set<RequestOptions> options = tripDto.getOptions();
        if (options.contains(RequestOptions.MATERIAL_ASSETS_TRANSPORTATION)) {
            coefficient *= tariff.getCoefMaterialAssets();
        }
        coefficient *= getEngineCoefficient(tariff.getEngineTariffParams(), tripDto);
        return coefficient;
    }
    
    protected double getSeasonalCoefficient(@NotNull PersonalTariff tariff, LocalDateTime tripTime) {
        if (tariff.getSeasonStart() == null || tariff.getSeasonEnd() == null) {
            return 1;
        }
        //используется високосный год, чтобы не было проблем с 29 февраля
        tariff = tariff.toBuilder().seasonStart(tariff.getSeasonStart().withYear(2000))
                       .seasonEnd(tariff.getSeasonEnd().withYear(2000)).build();
        if (!tariff.getSeasonStart().isAfter(tariff.getSeasonEnd())) {
            if (!tripTime.toLocalDate().withYear(2000).isBefore(tariff.getSeasonStart()) &&
                    tripTime.toLocalDate().withYear(2000).isBefore(tariff.getSeasonEnd())) {
                return tariff.getSeasonalCoefficient();
            }
        } else if (!tripTime.toLocalDate().withYear(2000).isBefore(tariff.getSeasonStart()) ||
                tripTime.toLocalDate().withYear(2000).isBefore(tariff.getSeasonEnd())) {
            return tariff.getSeasonalCoefficient();
        }
        return 1d;
    }
    
    private long calculateSuburbanTimeAndDistanceCost(
            @NotNull SuburbTariffParams suburbTariffParams, @NotNull SuburbTripDataDTO suburbTripData
                                                     ) {
        return (long) suburbTripData.getSuburbDistance() * suburbTariffParams.getCostPerKmSuburb()
               + (long) suburbTripData.getSuburbTime() * suburbTariffParams.getCostPerMinSuburb()
               + (long) suburbTripData.getInterRegionDistance() * suburbTariffParams.getCostPerKmInterRegion()
               + (long) suburbTripData.getInterRegionTime() * suburbTariffParams.getCostPerMinInterRegion();
    }
    
    protected long calculateCarSharingBasicCost(@NotNull CarSharingTariff tariff, @NotNull TripDto tripData) {
        return (long) (tariff.getRideCostPerKm() * tripData.getDistance() +
                       tariff.getRideCostPerMin() * (tripData.getTime()
                                                             .toMinutes())
                       + tariff.getWaitCostPerMin() * (tripData.getIntermediateWaitingTime()
                                                               .toMinutes()));
    }
    
    protected double calculateCarSharingCoefficient(@NotNull CarSharingTariff tariff, @NotNull TripDto tripData) {
        var coefficient = 1d;
        Set<RequestOptions> options = tripData.getOptions();
        if (options.contains(RequestOptions.CHILD_SEAT)) {
            coefficient *= tariff.getCoefChildSeat();
        }
        if (options.contains(RequestOptions.PET_TRANSPORTATION)) {
            coefficient *= tariff.getCoefPetTransport();
        }
        if (tripData.getTrafficJamScore() >= 7) {
            coefficient *= (1 + tariff.getCoefTraffic() * (tripData.getTrafficJamScore() - trafficThreshold));
        }
        LocalDateTime tripTime = getTripTime(tripData);
        coefficient *= getTimeCoefficient(tariff.getTimedTariffParams(), tripTime);
        return coefficient;
    }
    
    protected long calculateBicycleBasicCost(@NotNull BicycleTariff tariff, @NotNull TripDto tripData) {
        return (long) (tariff.getRideCostPerKm() * tripData.getDistance() +
                       tariff.getRideCostPerMin() * tripData.getTime().toMinutes()) + tariff.getBookingCost();
    }
    
    protected double calculateBicycleCoefficient(@NotNull BicycleTariff tariff, @NotNull TripDto tripData) {
        var coefficient = 1d;
        LocalDateTime tripTime = getTripTime(tripData);
        coefficient *= getTimeCoefficient(tariff.getTimedTariffParams(), tripTime);
        return coefficient;
    }
    
    protected long calculateScooterBasicCost(@NotNull ScooterTariff tariff, @NotNull TripDto tripData) {
        return (long) (tariff.getRideCostPerKm() * tripData.getDistance() +
                       tariff.getRideCostPerMin() * (tripData.getTime().toMinutes())) + tariff.getBookingCost();
    }
    
    protected double calculateScooterCoefficient(@NotNull ScooterTariff tariff, @NotNull TripDto tripData) {
        var coefficient = 1d;
        LocalDateTime tripTime = getTripTime(tripData);
        coefficient *= getTimeCoefficient(tariff.getTimedTariffParams(), tripTime);
        return coefficient;
    }
    
    /**
     * Выбор исполняющего контрагента в зависимости от веса контракта.
     *
     * @param contracts Список активных контрактов в регионе
     * @param tariffList список тарифов.
     *
     * @return договор.
     */
    private Contract chooseContract(List<Contract> contracts, List<? extends BaseTariffWithContract> tariffList) {
        
        var contractList = contracts.stream()
                                    .filter(e -> tariffList.stream().anyMatch(t -> t.getContract().equals(e)))
                                    .toList();
        String transportClass = "";
        if (tariffList.getFirst() instanceof TaxiTariff taxiTariff) {
            transportClass = taxiTariff.getTaxiClass().getValue();
        } else if (tariffList.getFirst() instanceof GroupTransferTariff groupTransferTariff) {
            transportClass = groupTransferTariff.getGroupTransferClass().name();
        }
        log.info("found second relevant contracts = " + contractList.size() + " for class: " + transportClass);
        // выбрать в одного в зависимости от суммы контракта
        // функция распределения: сложи все суммы контрактов и запиши интервалы в массив. возьми рандом от суммы и
        // посмотри в какой интервал попадает этот рандом.
        // в хешмап ключ это вершина интервала. значение айди контракта.
        // типа если суммы контрактов 700, 60, 20 то ключи 700, 760, 780 и рандом надо брать от 1 до 780
        // (int) math.random * 780
        
        var upperIntervalValue = 0L;
        var list = new ArrayList<Pair<Long, Contract>>();
        for (var contract : contractList) {
            upperIntervalValue += contract.getSum();
            list.add(Pair.of(upperIntervalValue, contract));
        }
        var random = (long) (Math.random() * upperIntervalValue);
        
        var lowerInterval = 0L;
        for (var pair : list) {
            if (random >= lowerInterval && random < pair.getFirst()) {
                var contract = pair.getSecond();
                log.debug("Chosen contract number = {} for  class: {}", contract.getContractNumber(), transportClass);
                return contract;
            } else {
                lowerInterval += pair.getFirst();
            }
        }
        log.debug("There is not contract for requested data");
        return null;
    }
    
    private RegionDto getRegion(WaypointDTO waypoint) {
        try {
            log.info("getGeoZone for: {}", waypoint);
            var regionData = regionDataResolver.getRegion(waypoint);
            if (regionData != null) {
                log.info("getGeoZone found region: {} with id: {}", regionData.getName(), regionData.getId());
                return regionData;
            }
        } catch (Exception e) {
            throw new RegionResolvingFailedException(e);
        }
        
        log.warn("getGeoZone: failed to find geo zone!!!");
        return null;
    }
    
    private UUID getRegionId(WaypointDTO waypoint) {
        return Optional.ofNullable(getRegion(waypoint)).map(RegionDto::getId).orElse(null);
    }
    
    private List<RegionDto> getRegionBranch(TripDto tripData) {
        try {
            log.info("getGeoZone for: {}", tripData.getStartPoint());
            var regionBranch = regionDataResolver.getRegionBranch(tripData.getStartPoint());
            if (regionBranch == null || regionBranch.isEmpty()) {
                log.debug("getRegionBranch didn`t find elements");
                return Collections.emptyList();
            }
            log.debug("getRegionBranch found region list of {} elements", regionBranch.size());
            return regionBranch;
        } catch (Exception e) {
            throw new RegionResolvingFailedException(e);
        }
    }
    
    private int getMinCost(boolean available, int currentCost, int minCost) {
        if (available) {
            return minCost == 0 ? currentCost : Math.min(currentCost, minCost);
        }
        return minCost;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    private static class TransportRegionPair {
        private UUID transportId;
        private UUID regionId;
    }
}