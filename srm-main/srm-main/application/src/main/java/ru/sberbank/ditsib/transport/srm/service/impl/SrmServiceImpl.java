package ru.sberbank.ditsib.transport.srm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jetbrains.annotations.NotNull;
import org.jgrapht.Graph;
import org.jgrapht.alg.tour.NearestNeighborHeuristicTSP;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.springframework.stereotype.Service;
import ru.sber.transport.constants.EventType;
import ru.sber.transport.constants.PointMatchingType;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sber.transport.srm.model.SrmWaypointPostDTO;
import ru.sber.transport.srm.model.Waypoint;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.srm.config.FindAlgorithmEnum;
import ru.sberbank.ditsib.transport.srm.config.SrmSettingNames;
import ru.sberbank.ditsib.transport.srm.dao.BaseTariffRepository;
import ru.sberbank.ditsib.transport.srm.dao.SrmRequestKpiRepository;
import ru.sberbank.ditsib.transport.srm.dao.SrmSharedRideRepository;
import ru.sberbank.ditsib.transport.srm.dto.DistanceMatrixResponseDTO;
import ru.sberbank.ditsib.transport.srm.dto.internal.SrmMultipleRequestDTO;
import ru.sberbank.ditsib.transport.srm.dto.internal.SrmSingleRequestDTO;
import ru.sberbank.ditsib.transport.srm.exception.SrmBadRequestException;
import ru.sberbank.ditsib.transport.srm.exception.SrmLogicException;
import ru.sberbank.ditsib.transport.srm.exception.SrmNotFoundException;
import ru.sberbank.ditsib.transport.srm.mapper.EntityCopyConverter;
import ru.sberbank.ditsib.transport.srm.mapper.EntityDtoConverter;
import ru.sberbank.ditsib.transport.srm.model.SrmRequestKpi;
import ru.sberbank.ditsib.transport.srm.model.SrmSharedRide;
import ru.sberbank.ditsib.transport.srm.model.SrmWaypoint;
import ru.sberbank.ditsib.transport.srm.model.tariff.BaseTariff;
import ru.sberbank.ditsib.transport.srm.model.tariff.CoopTariffParams;
import ru.sberbank.ditsib.transport.srm.model.tariff.PersonalTariff;
import ru.sberbank.ditsib.transport.srm.model.tariff.TaxiTariff;
import ru.sberbank.ditsib.transport.srm.service.GeoService;
import ru.sberbank.ditsib.transport.srm.service.SrmService;
import ru.sberbank.ditsib.transport.srm.service.SrmSettingService;
import ru.sberbank.ditsib.transport.srm.service.TariffService;
import ru.sberbank.ditsib.transport.srm.util.SrmMetrics;
import ru.sberbank.ditsib.transport.srm.util.TariffUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с маджента
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SrmServiceImpl implements SrmService {

    private static final double AVERAGE_RADIUS_OF_EARTH_KM = 6371;
    private final SrmSharedRideRepository sharedRideRepository;
    private final SrmRequestKpiRepository requestKpiRepository;

    private final BaseTariffRepository baseTariffRepository;
    private final Map<TransportTypeEnum, TariffService<? extends BaseTariff>> tariffServices;

    private final GeoService geoService;
    private final SrmSettingService settingService;
    private final EntityDtoConverter entityDtoConverter;
    private final EntityCopyConverter entityCopyConverter;

    private static final String OPERATION_OK = "OK";
    private static final int ABSENT_VALUE = -1;
    private static final int DRUG_LOWER_BOUND = 100000000;
    private static final int DRUG_UPPER_BOUND = 2147483647;

    @Override
    public SrmSharedRideDTO addNew(SrmMultipleRequestDTO multipleRequestDTO, UUID bunchId, boolean doSave) {
        try {
            final var requestDTOList = multipleRequestDTO.getRequestDTOList();
            final var transportType = multipleRequestDTO.getTransportType();
            final var tariffId = multipleRequestDTO.getTariffId();
            if (log.isDebugEnabled()) {
                log.debug("SRM: addNew: begin with save = {} called for request {} and tariff {} and pointMatchingType {} and transportType {}",
                        doSave, requestDTOList.get(0).getRequestId(), tariffId, multipleRequestDTO.getPointMatchingType(), transportType);
            }
            checkTransportType(multipleRequestDTO);
            checkPassengers(transportType, requestDTOList);
            checkWaypoints(requestDTOList);
            checkPickupTime(multipleRequestDTO);
            final var tariff = getBaseTariff(tariffId, transportType);

            assert tariff != null;

            checkPointMatching(multipleRequestDTO, tariff);
            final var sharedRide = new SrmSharedRide();
            sharedRide.setActive(true);
            sharedRide.setTariffId(tariff.getId());
            sharedRide.setTransportType(transportType);
            sharedRide.setTimeZone(multipleRequestDTO.getTimeZone());
            sharedRide.setPointMatchingType(multipleRequestDTO.getPointMatchingType());
            sharedRide.setBunchId(bunchId);
            sharedRide.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
            sharedRide.setBunchNumber(0);
            setSharedRideCoopParams(sharedRide, tariff);

            final var unitedSharedRide = uniteRequestWithSharedRide(sharedRide, tariff, multipleRequestDTO); //addNew
            checkUnitedData(multipleRequestDTO, unitedSharedRide);

            var result = unitedSharedRide;
            if (doSave) {
                result = saveData(unitedSharedRide, tariff);
            }
            if (log.isDebugEnabled()) {
                log.debug("SRM: addNew: end with save = {}", doSave);
            }
            return entityDtoConverter.sharedRideToSharedRideDto(result);
        } catch (SrmBadRequestException | SrmLogicException e) {
            log.debug(e.getMessage(), e);
            if(!log.isDebugEnabled()) {
                log.info(e.getMessage());
            }
            return null;
        }
    }

    @Override
    public List<SrmSharedRideDTO> findMatch(SrmMultipleRequestDTO multipleRequestDTO, UUID bunchId, boolean joinCandidate) {
        return findMatchPrivate(multipleRequestDTO, bunchId, joinCandidate);
    }


    @Override
    public SrmSharedRideDTO joinRequest(UUID rideId, SrmMultipleRequestDTO multipleRequestDTO, int bunchNumber) {
        try {
            final var sharedRide = sharedRideRepository.findById(rideId).orElse(null);
            final var tariff = getBaseTariff(multipleRequestDTO.getTariffId(), multipleRequestDTO.getTransportType());
            log.debug("joinRequest: join request {} to sharedRide {}", multipleRequestDTO, sharedRide);
            if (sharedRide == null) {
                throw new SrmNotFoundException("joinRequest: Совместная поездка не найдена");
            }
            checkBasicAttributes(multipleRequestDTO, sharedRide);
            checkHaveTariff(multipleRequestDTO);
            checkMinimalDistance(multipleRequestDTO, tariff);
            checkTimeAndDistance(multipleRequestDTO, sharedRide);
            final var unitedSharedRide = uniteRequestWithSharedRide(sharedRide, tariff, multipleRequestDTO);
            if (unitedSharedRide == null) {
                throw new SrmLogicException("joinRequest: Ошибка при объединении заявок");
            }
            checkPickupTime(multipleRequestDTO, unitedSharedRide);
            checkWaypointsOrder(unitedSharedRide);
            checkCapacity(unitedSharedRide, tariff);
            checkEconomy(unitedSharedRide);
            unitedSharedRide.setBunchNumber(bunchNumber);
            final var result2 = sharedRideRepository.save(unitedSharedRide);
            log.debug("joinRequest: finish: {}", result2);
            return entityDtoConverter.sharedRideToSharedRideDto(result2);
        } catch (SrmBadRequestException | SrmNotFoundException | SrmLogicException e) {
            log.debug(e.getMessage(), e);
            if(!log.isDebugEnabled()) {
                log.info(e.getMessage());
            }
            return null;
        }
    }

    @Override
    public SrmSharedRideDTO cancelRequest(UUID requestId) {
        try {
            final var requestKpi = requestKpiRepository.findByOrgRequestId(requestId).orElseThrow(() -> new SrmNotFoundException("SRM: cancelRequest: заявка не найдена"));
            final var sharedRide = sharedRideRepository.findById(requestKpi.getSharedRide().getId()).orElseThrow(() -> new SrmNotFoundException("SRM: cancelRequest: совместная поездка в заявке не найдена"));
            if (!requestKpi.isActive()) {
                return entityDtoConverter.sharedRideToSharedRideDto(sharedRide);
            }
            if (triggerTimePassed(sharedRide)) {
                throw new SrmLogicException("SRM: cancelRequest: триггерное время уже наступило! Отзыв невозможен!");
            }
            log.debug("SRM: cancelRequest: requestKpi = {}, sharedRide = {}", requestKpi, sharedRide);

            final var transportType = sharedRide.getTransportType();
            final var tariff = getBaseTariff(sharedRide.getTariffId(), sharedRide.getTransportType());
            if (TransportTypeEnum.TAXI.equals(transportType)
                    || TransportTypeEnum.PERSONAL.equals(transportType)) {
                if (sharedRide.getRequestKpiList().get(0).getId().equals(requestKpi.getId())) {
                    removeAllRequestKpiFromSharedRide(sharedRide);
                    sharedRide.setActive(false);
                    final var result2 = sharedRideRepository.save(sharedRide);
                    return entityDtoConverter.sharedRideToSharedRideDto(result2);
                } else { // иначе - выкинуть участника из поездки
                    softRemoveRequestFromSharedRide(sharedRide, requestKpi.getId());
                    calculateDistancesAndTimesForSharedRide(sharedRide, tariff);  // cancelRequest
                    final var result2 = sharedRideRepository.save(sharedRide);
                    return entityDtoConverter.sharedRideToSharedRideDto(result2);
                }
            }
        } catch (SrmBadRequestException | SrmLogicException e) {
            log.debug(e.getMessage(), e);
            if(!log.isDebugEnabled()) {
                log.info(e.getMessage());
            }
            return null;
        } catch (Exception e) {
            log.error("Srm cancel order from shared ride exception", e);
        }
        return null;
    }

    @Override
    public SrmSharedRideDTO get(UUID rideId) {
        final var sharedRide = sharedRideRepository.findById(rideId).orElseThrow(() -> new SrmLogicException("Совместная поездка не найдена"));
        return entityDtoConverter.sharedRideToSharedRideDto(sharedRide);
    }

    @Override
    public SrmSharedRideDTO getByRequestId(UUID requestId) {
        final var requestKpi = requestKpiRepository.findByOrgRequestId(requestId).orElse(null);
        if (requestKpi == null) {
            return null;
        }
        final var sharedRide = sharedRideRepository.findById(requestKpi.getSharedRide().getId()).orElse(null);
        if (sharedRide == null) {
            return null;
        }
        return entityDtoConverter.sharedRideToSharedRideDto(sharedRide);
    }

    @Override
    public List<SrmSharedRideDTO> getAll() {
        return sharedRideRepository.findAll().stream()
                .map(entityDtoConverter::sharedRideToSharedRideDto)
                .toList();
    }

    @Override
    public SrmSharedRideDTO finishSharedRide(UUID rideId) {
        final var sharedRide = sharedRideRepository.findById(rideId).orElseThrow(() -> new SrmLogicException("Совместная поездка не найдена"));
        sharedRide.setActive(false);
        final var result2 = sharedRideRepository.save(sharedRide);
        return entityDtoConverter.sharedRideToSharedRideDto(result2);
    }

    @NotNull
    private SrmSharedRide saveData(SrmSharedRide unitedSharedRide, BaseTariff tariff) {
        if (!filterByPreservationOfWaypointsOrder(unitedSharedRide)) {
            throw new SrmLogicException("SRM: addNew: Заявка не подходит по критериям порядка точек");
        }
        if (!filterByCapacity(unitedSharedRide, tariff)) {
            throw new SrmLogicException("SRM: addNew: Заявка не подходит по метрическим критериям");
        }
        return sharedRideRepository.saveAndFlush(unitedSharedRide);
    }

    private void checkEconomy(SrmSharedRide unitedSharedRide) {
        if (!filterByEconomyProcent(unitedSharedRide)) {
            throw new SrmLogicException("joinRequest: Заявка не подходит по критериям экономии");
        }
    }

    private void checkCapacity(SrmSharedRide unitedSharedRide, BaseTariff tariff) {
        if (!filterByCapacity(unitedSharedRide, tariff)) {
            throw new SrmLogicException("joinRequest: Заявка не подходит по метрическим критериям");
        }
    }

    private void checkWaypointsOrder(SrmSharedRide unitedSharedRide) {
        if (!filterByPreservationOfWaypointsOrder(unitedSharedRide)) {
            throw new SrmLogicException("joinRequest: Заявка не подходит по критериям порядка точек");
        }
    }

    private void checkPickupTime(SrmMultipleRequestDTO multipleRequestDTO, SrmSharedRide unitedSharedRide) {
        if (multipleRequestDTO.getRequestDTOList().size() == 1
                && !filterByDeviationOfPickUpTime(unitedSharedRide, multipleRequestDTO)) {
            throw new SrmLogicException("joinRequest: слишком поздно! Присоединение невозможно!");
        }
    }

    private void checkTimeAndDistance(SrmMultipleRequestDTO multipleRequestDTO, SrmSharedRide sharedRide) {
        if (multipleRequestDTO.getRequestDTOList().size() == 1 && !isMatchingByWaypoints(sharedRide, multipleRequestDTO.getRequestDTOList().get(0))) { // joinRequest
            throw new SrmLogicException("joinRequest: Заявка не подходит по критериям времени и расстояния");
        }
    }

    private void checkMinimalDistance(SrmMultipleRequestDTO multipleRequestDTO, BaseTariff tariff) {
        if (PointMatchingType.MATCH_POINTS_FALSE.equals(multipleRequestDTO.getPointMatchingType())) {
            final var minRadius = 2 * getIncludeRadius(multipleRequestDTO, tariff);
            if (isMinimalDistanceNonReserved(multipleRequestDTO, minRadius)) {
                throw new SrmLogicException("SRM: joinRequest: Растояние между двумя соседними точками меньше минимального: " + minRadius);
            }
        }
    }

    private void checkHaveTariff(SrmMultipleRequestDTO multipleRequestDTO) {
        if (!baseTariffRepository.existsById(multipleRequestDTO.getTariffId())) {
            throw new SrmBadRequestException("SRM: joinRequest: Тариф заявки не найден! requestId = %s".formatted(multipleRequestDTO.getRequestDTOList().get(0).getRequestId()));
        }
    }

    private static void checkBasicAttributes(SrmMultipleRequestDTO multipleRequestDTO, SrmSharedRide sharedRide) {
        if (!isMatchingByBasicAttributes(sharedRide, multipleRequestDTO)) {
            throw new SrmLogicException("joinRequest: Заявка не подходит по базовым атрибутам");
        }
    }

    private static void checkUnitedData(SrmMultipleRequestDTO multipleRequestDTO, SrmSharedRide unitedSharedRide) {
        if (unitedSharedRide == null) {
            throw new SrmLogicException("SRM: addNew: Ошибка при объединении поездок. " + multipleRequestDTO);
        }
    }

    private static void checkWaypoints(List<SrmSingleRequestDTO> requestDTOList) {
        for (final var singleRequestDTO : requestDTOList) {
            if (singleRequestDTO.getWaypoints() == null || singleRequestDTO.getWaypoints().size() < 2) {
                throw new SrmBadRequestException("SRM: addNew: Количество точек меньше 2. requestId = " + singleRequestDTO.getRequestId());
            }
        }
    }

    private static void checkPassengers(TransportTypeEnum transportType, List<SrmSingleRequestDTO> requestDTOList) {
        if (TransportTypeEnum.TAXI.equals(transportType) || TransportTypeEnum.PERSONAL.equals(transportType)) {
            for (final var singleRequestDTO : requestDTOList) {
                if (singleRequestDTO.getRequiredPassengers() == null || singleRequestDTO.getRequiredPassengers() == 0) {
                    throw new SrmBadRequestException("SRM: addNew: Количество пассажиров равно нулю. requestId = " + singleRequestDTO.getRequestId());
                }
            }
        }
    }

    private static void checkTransportType(SrmMultipleRequestDTO multipleRequestDTO) {
        final var transportType = multipleRequestDTO.getTransportType();
        if (!TransportTypeEnum.TAXI.equals(transportType)
                && !TransportTypeEnum.PERSONAL.equals(transportType)) {
            throw new SrmBadRequestException("SRM: addNew: вид транспорта не поддерживается: " + transportType);
        }
    }

    private void checkPointMatching(SrmMultipleRequestDTO multipleRequestDTO, BaseTariff tariff) {
        if (PointMatchingType.MATCH_POINTS_FALSE.equals(multipleRequestDTO.getPointMatchingType())) {
            double minRadius = 2 * getIncludeRadius(multipleRequestDTO, tariff);
            if (isMinimalDistanceNonReserved(multipleRequestDTO, minRadius)) {
                throw new SrmBadRequestException("SRM: addNew: Растояние между двумя соседними точками меньше минимального: " + minRadius);
            }
        }
    }

    private static void checkPickupTime(SrmMultipleRequestDTO multipleRequestDTO) {
        if (ZonedDateTime.now().isAfter(multipleRequestDTO.getPickupTime())) {
            throw new SrmBadRequestException("SRM: addNew: Время начало поездки в прошлом. requestId = %s"
                    .formatted(multipleRequestDTO.getRequestDTOList().get(0).getRequestId()));
        }
    }

    private List<SrmSharedRideDTO> findMatchPrivate(SrmMultipleRequestDTO multipleRequestDTO, UUID bunchId, boolean joinCandidate) {
        final var findAlgorithm = settingService.getByName(SrmSettingNames.FIND_ALGORITHM);
        final var tariff = getBaseTariff(multipleRequestDTO.getTariffId(), multipleRequestDTO.getTransportType());
        if (log.isDebugEnabled()) {
            log.debug("SRM: findMatch called for  multipleRequestDTO {} with algorithm {}", getMultipleRequestDTOShort(multipleRequestDTO), findAlgorithm);
        }
        if (PointMatchingType.MATCH_POINTS_FALSE.equals(multipleRequestDTO.getPointMatchingType())) {
            final var minRadius = 2 * getIncludeRadius(multipleRequestDTO, tariff);
            if (isMinimalDistanceNonReserved(multipleRequestDTO, minRadius)) {
                log.warn("SRM: findMatch: Растояние между двумя соседними точками меньше минимального: {}", minRadius);
                return new ArrayList<>();
            }
        }
        if (joinCandidate) {
            multipleRequestDTO.getRequestDTOList().get(0).setCandidate(true);
        }
        final var sharedRides =
                new ArrayList<>(sharedRideRepository.findByActiveAndTariffIdAndBunchId(true, multipleRequestDTO.getTariffId(), bunchId)
                        .parallelStream()
                        .filter(it -> !it.getId().equals(multipleRequestDTO.getMultipleRequestId()))
                        .toList());
        clearOldRides(sharedRides);
        log.debug("SRM: findMatch found total active rides {}, found = {}", sharedRides.size(), getSharedRideListShort(sharedRides));
        return FindAlgorithmEnum.ONEBYONE.name().equals(findAlgorithm)
                ? findMatchOneByOne(multipleRequestDTO, sharedRides, joinCandidate)
                : findMatchBatch(multipleRequestDTO, sharedRides, joinCandidate);
    }

    private void clearOldRides(List<SrmSharedRide> sharedRides) {
        List<SrmSharedRide> deactivatedList = new ArrayList<>();
        int deactivated = 0;
        ZonedDateTime now = ZonedDateTime.now();
        for (SrmSharedRide sharedRide : sharedRides) {
            if (!sharedRide.getWaypoints().isEmpty()) {
                if (now.isAfter(sharedRide.getWaypoints().get(0).getStartTime())) {
                    deactivatedList.add(sharedRide);
                    log.debug("SRM: clearOldRides: shared ride deactivated1 {}", sharedRide.getId());
                }
            } else {
                deactivatedList.add(sharedRide);
                log.debug("SRM: clearOldRides: shared ride deactivated2 {}", sharedRide.getId());
            }
        }
        deactivatedList.forEach(e -> e.setActive(false));
        sharedRideRepository.saveAll(deactivatedList);
        sharedRides.removeAll(deactivatedList);

        log.info("SRM: clearOldRides: total deactivated {}", deactivated);
    }

    // for every matching ride calculate joined ride without saving
    private List<SrmSharedRideDTO> findMatchBatch(
            SrmMultipleRequestDTO multipleRequestDTO,
            List<SrmSharedRide> sharedRides,
            boolean joinCandidate
    ) {

        log.debug("SRM: findMatch called with BATCH algorithm, tariff = {}", multipleRequestDTO.getTariffId());
        final var sharedRides0 = sharedRides.parallelStream()
                .filter(it -> isMatchingByBasicAttributes(it, multipleRequestDTO))
                .filter(this::isMatchingByWaitingTimes)
                .toList();
        log.trace("SRM: findMatch: after filtering 0 (isMatchingByBasicAttributes): {}", sharedRides0.size());

        final List<SrmSharedRide> sharedRides2;
        final var requestDTOList = multipleRequestDTO.getRequestDTOList();
        if (requestDTOList.size() == 1) {
            sharedRides2 = sharedRides0.parallelStream().filter(it -> isMatchingByWaypoints(it, requestDTOList.get(0))).toList();
        } else {
            sharedRides2 = sharedRides0;
        }
        log.trace("SRM: findMatch: after filtering 2 (isMatchingByWaypoints): {}", sharedRides2.size());

        final var tariffs = baseTariffRepository.findAllById(sharedRides2.parallelStream().map(SrmSharedRide::getTariffId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(BaseTariff::getId, Function.identity()));
        final var sharedRides3 = sharedRides2.stream().map(entityCopyConverter::sharedRideToSharedRide)
                .map(it -> uniteRequestWithSharedRide(it, tariffs.get(it.getTariffId()), multipleRequestDTO))
                .filter(Objects::nonNull).toList();
        log.trace("SRM: findMatch: after filtering 3 (after uniting): {}", sharedRides3.size());

        final List<SrmSharedRide> sharedRides4;
        if (requestDTOList.size() == 1) {
            sharedRides4 = sharedRides3.parallelStream().filter(it -> filterByDeviationOfPickUpTime(it, multipleRequestDTO)).toList();
        } else {
            sharedRides4 = sharedRides3;
        }
        log.trace("SRM: findMatch: after filtering 4 (filterByDeviationTime): {}", sharedRides4.size());

        final var filteredList = sharedRides4.parallelStream()
                .filter(this::filterByPreservationOfWaypointsOrder)
                .filter(it -> filterByCapacity(it, tariffs.get(it.getTariffId())))
                .filter(this::filterByEconomyProcent)
                .toList();
        log.trace("SRM: findMatch: after filtering 7 (filterByEconomyProcent): {}", filteredList.size());

        List<SrmSharedRideDTO> filteredDtoList;
        if (joinCandidate) {
            filteredDtoList = filteredList.stream()
                    .map(entityDtoConverter::sharedRideToSharedRideDto)
                    .collect(Collectors.toList()); // NOSONAR
        } else {
            final var idList = filteredList.stream()
                    .map(SrmSharedRide::getId)
                    .toList();
            final var sharedRidesFilteredOriginal = new ArrayList<SrmSharedRide>();
            for (final var sharedRide : sharedRides) {
                if (idList.contains(sharedRide.getId())) {
                    sharedRidesFilteredOriginal.add(sharedRide);
                }
            }
            if (idList.size() != sharedRidesFilteredOriginal.size()) {
                throw new SrmLogicException("SRM: error in findMatch!");
            }
            filteredDtoList = sharedRidesFilteredOriginal.stream()
                    .map(entityDtoConverter::sharedRideToSharedRideDto)
                    .collect(Collectors.toList());  // NOSONAR
        }
        log.debug("SRM: findMatch final found results: {}", filteredDtoList.size());
        return filteredDtoList;
    }

    private List<SrmSharedRideDTO> findMatchOneByOne(
            SrmMultipleRequestDTO multipleRequestDTO,
            List<SrmSharedRide> sharedRides,
            boolean joinCandidate
    ) {
        log.debug("SRM: findMatch called with ONEBYONE algorithm");
        // find matching rides considering departure time and radius

        final var sharedRidesSelected = new ArrayList<SrmSharedRide>();
        final var tariffs = baseTariffRepository.findAllById(sharedRides.parallelStream().map(SrmSharedRide::getTariffId).collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(BaseTariff::getId, Function.identity()));
        for (final var sharedRide : sharedRides) {
            boolean selected;
            log.debug("SRM: findMatch considering shared ride with id {}, with num of points {}", sharedRide.getId(), sharedRide.getWaypoints().size());
            printPath(sharedRide.getWaypoints(), "findMatch@" + sharedRide.getId());

            selected = checkSelected(multipleRequestDTO, sharedRide);

            SrmSharedRide sharedRideUnited = null;
            if (selected) {
                final var sharedRideNew = entityCopyConverter.sharedRideToSharedRide(sharedRide);
                sharedRideUnited = uniteRequestWithSharedRide(sharedRideNew, tariffs.get(sharedRideNew.getTariffId()), multipleRequestDTO); //findMatchOneByOne
                if (sharedRideUnited == null) {
                    selected = false;
                }
            }

            if (selected
                    && multipleRequestDTO.getRequestDTOList().size() == 1) {
                if (!filterByDeviationOfPickUpTime(sharedRideUnited, multipleRequestDTO)) {
                    selected = false;
                    log.debug("SRM: findMatch: filterByDeviationOfPickUpTime fail");
                } else {
                    log.debug("SRM: findMatch: filterByDeviationOfPickUpTime success");
                }
            }

            if (selected) {
                if (!filterByPreservationOfWaypointsOrder(sharedRideUnited)) {  // findMatch onebyone
                    selected = false;
                    log.debug("SRM: findMatch: filterByPreservationOfWaypointsOrder fail");
                } else {
                    log.debug("SRM: findMatch: filterByPreservationOfWaypointsOrder success");
                }
            }

            if (selected) {
                if (!filterByCapacity(sharedRideUnited, tariffs.get(sharedRideUnited.getTariffId()))) {
                    selected = false;
                    log.debug("SRM: findMatch: filterByCapacity fail");
                } else {
                    log.debug("SRM: findMatch: filterByCapacity success");
                }
            }

            if (selected) {
                if (!filterByEconomyProcent(sharedRideUnited)) {
                    selected = false;
                    log.debug("SRM: findMatch: filterByEconomyProcent fail");
                } else {
                    log.debug("SRM: findMatch: filterByEconomyProcent success");
                }
            }
            log.debug("SRM: findMatch for shared ride with id {} find result is {}", sharedRide.getId(), selected);
            if (selected) {
                sharedRidesSelected.add(sharedRideUnited);
            }
        }

        List<SrmSharedRideDTO> filteredDtoList;
        if (joinCandidate) {
            filteredDtoList = sharedRidesSelected.stream()
                    .map(entityDtoConverter::sharedRideToSharedRideDto)
                    .collect(Collectors.toList()); // NOSONAR
        } else {
            final var idList = sharedRidesSelected.stream()
                    .map(SrmSharedRide::getId)
                    .toList();
            List<SrmSharedRide> sharedRidesFilteredOriginal = new ArrayList<>();
            for (SrmSharedRide sharedRide : sharedRides) {
                if (idList.contains(sharedRide.getId())) {
                    sharedRidesFilteredOriginal.add(sharedRide);
                }
            }
            if (idList.size() != sharedRidesFilteredOriginal.size()) {
                throw new SrmLogicException("SRM: error in findMatch!");
            }
            filteredDtoList = sharedRidesFilteredOriginal.stream()
                    .map(entityDtoConverter::sharedRideToSharedRideDto)
                    .toList();
        }
        log.debug("SRM: findMatch final found results: {}", filteredDtoList.size());
        return filteredDtoList;
    }

    private boolean checkSelected(SrmMultipleRequestDTO multipleRequestDTO, SrmSharedRide sharedRide) {
        boolean selected;
        if (!isMatchingByBasicAttributes(sharedRide, multipleRequestDTO)) {
            selected = false;
        } else {
            log.debug("SRM: findMatch: isMatchingByBasicAttributes success");
            selected = true;
        }
        if (selected) {
            if (!isMatchingByWaitingTimes(sharedRide)) {
                selected = false;
                log.debug("SRM: findMatch: isMatchingByWaitingTimes fail");
            } else {
                log.debug("SRM: findMatch: isMatchingByWaitingTimes success");
            }
        }
        if (selected && multipleRequestDTO.getRequestDTOList().size() == 1) {
            if (!isMatchingByWaypoints(sharedRide, multipleRequestDTO.getRequestDTOList().get(0))) { // findMatchOneByOne
                selected = false;
                log.debug("SRM: findMatch: isMatchingByWaypoints fail");
            } else {
                log.debug("SRM: findMatch: isMatchingByWaypoints success");
            }
        }
        return selected;
    }

    //---------- private methods from here ---------------------

    private boolean isMinimalDistanceNonReserved(SrmMultipleRequestDTO multipleRequestDTO, double includeRadius) {
        return multipleRequestDTO.getRequestDTOList()
                .stream()
                .anyMatch(it -> !isMinimalDistanceReserved(it, includeRadius));
    }

    private boolean isMinimalDistanceReserved(SrmSingleRequestDTO singleRequestDTO, double includeRadius) {
        var waypointFrom = singleRequestDTO.getWaypoints().get(0);
        for (int i = 1; i < singleRequestDTO.getWaypoints().size(); i++) {
            final var waypointTo = singleRequestDTO.getWaypoints().get(i);
            if (isWaypointInsideRadius(waypointFrom, waypointTo, includeRadius)) {
                return false;
            }
            waypointFrom = waypointTo;
        }
        return true;
    }

    private static CoopTariffParams getCoopTariffParams(BaseTariff baseTariff) {
        if (baseTariff == null) {
            return null;
        }
        final var transportType = baseTariff.getTransportType();
        if (TransportTypeEnum.TAXI.equals(transportType)) {
            final var tariff = (TaxiTariff) baseTariff;
            return tariff.getCoopTariffParams();
        }
        if (TransportTypeEnum.PERSONAL.equals(transportType)) {
            final var tariff = (PersonalTariff) baseTariff;
            return tariff.getCoopTariffParams();
        }
        return null;
    }

    private static Integer getMaxCapacity(BaseTariff baseTariff) {
        if (baseTariff == null) {
            return null;
        }
        final var transportType = baseTariff.getTransportType();
        if (TransportTypeEnum.TAXI.equals(transportType)) {
            final var tariff = (TaxiTariff) baseTariff;
            return tariff.getMaxCapacity();
        }
        if (TransportTypeEnum.PERSONAL.equals(transportType)) {
            final var tariff = (PersonalTariff) baseTariff;
            return tariff.getMaxCapacity();
        }
        return null;
    }

    private BaseTariff getBaseTariff(UUID tariffId, TransportTypeEnum transportType) {
        return tariffServices.get(transportType).findById(tariffId).orElseThrow(() -> new EntityNotFoundException(BaseTariff.class, tariffId));
    }

    private Double getIncludeRadius(SrmMultipleRequestDTO requestDTO, BaseTariff tariff) {
        final var coopTariffParams = getCoopTariffParams(tariff);
        final var distanceDeviationKm = coopTariffParams != null && coopTariffParams.getDistanceDeviationKm() != ABSENT_VALUE
                ? coopTariffParams.getDistanceDeviationKm()
                : Double.parseDouble(settingService.get(SrmSettingNames.DISTANCE_DEVIATION_KM).getValue());
        log.trace("SRM: for tariff {} found include radius {}", requestDTO.getTariffId(), distanceDeviationKm);
        return distanceDeviationKm;
    }

    private void setSharedRideCoopParams(SrmSharedRide sharedRide, BaseTariff tariff) {
        final var coopTariffParams = getCoopTariffParams(tariff);
        if (coopTariffParams != null) {
            final var distanceDeviationKm = coopTariffParams.getDistanceDeviationKm().intValue() != ABSENT_VALUE
                    ? coopTariffParams.getDistanceDeviationKm()
                    : Double.parseDouble(settingService.get(SrmSettingNames.DISTANCE_DEVIATION_KM).getValue());

            final var timeDeviationMin = coopTariffParams.getTimeDeviationMin() != ABSENT_VALUE
                    ? coopTariffParams.getTimeDeviationMin()
                    : Integer.parseInt(settingService.get(SrmSettingNames.TIME_DEVIATION_MIN).getValue());

            final var savingsDeviationPct = coopTariffParams.getSavingsDeviationPct().intValue() != ABSENT_VALUE
                    ? coopTariffParams.getSavingsDeviationPct()
                    : Double.parseDouble(settingService.get(SrmSettingNames.SAVING_DEVIATION_PROCENT).getValue());

            final var minCancelTimeMin = coopTariffParams.getMinCancelTimeMin() != ABSENT_VALUE
                    ? coopTariffParams.getMinCancelTimeMin()
                    : Integer.parseInt(settingService.get(SrmSettingNames.MIN_CANCEL_TIME_MIN).getValue());

            final var sharedTariffParams = sharedRide.getCoopTariffParams();
            sharedTariffParams.setDistanceDeviationKm(distanceDeviationKm);
            sharedTariffParams.setTimeDeviationMin(timeDeviationMin);
            sharedTariffParams.setSavingsDeviationPct(savingsDeviationPct);
            sharedTariffParams.setMinCancelTimeMin(minCancelTimeMin);
        }
    }

    private void softRemoveRequestFromSharedRide(SrmSharedRide sharedRide, UUID requestId) {
        final var waypointIterator = sharedRide.getWaypoints().iterator();
        while (waypointIterator.hasNext()) {
            final var waypoint = waypointIterator.next();
            if (waypoint.getRequestKpiId().equals(requestId)) {
                waypoint.setSharedRide(null);
                waypointIterator.remove();
            }
        }
        for (final var requestKpi : sharedRide.getRequestKpiList()) {
            if (requestKpi.getId().equals(requestId)) {
                requestKpi.setActive(false);
                requestKpiRepository.save(requestKpi);
            }
        }
    }

    private void removeAllRequestKpiFromSharedRide(SrmSharedRide sharedRide) {
        for (final var requestKpi : sharedRide.getRequestKpiList()) {
            if (requestKpi != null) {
                requestKpi.setActive(false);
                requestKpiRepository.save(requestKpi);
            }
        }
    }

    private static boolean isMatchingByBasicAttributes(
            SrmSharedRide sharedRide,
            SrmMultipleRequestDTO multipleRequestDTO
    ) {
        if (!multipleRequestDTO.getPointMatchingType().equals(sharedRide.getPointMatchingType())) {
            log.debug("SRM: isMatchingByBasicAttributes: request declined by pointMatchingType: sharedRide = {}, requestDTO = {}", sharedRide.getPointMatchingType(), multipleRequestDTO.getPointMatchingType());
            return false;
        }
        return true;
    }

    private boolean isMatchingByWaitingTimes(SrmSharedRide sharedRide) {
        int maxWaitingTime = 600;
        final var settingMaxWaitingTime = settingService.get(SrmSettingNames.MAX_WAITING_TIME);
        if (settingMaxWaitingTime != null) {
            maxWaitingTime = Integer.parseInt(settingMaxWaitingTime.getValue());
        }
        for (final var waypoint : sharedRide.getWaypoints()) {
            if (waypoint.getWaitingTime() > maxWaitingTime) {
                log.debug("isMatchingByWaitingTimes: {} > maxWaitingTime!", waypoint.getWaitingTime());
                return false;
            }
        }
        return true;
    }

    private SrmWaypoint getFirstWaypoint(
            SrmSharedRide sharedRide,
            SrmSingleRequestDTO singleRequestDTO
    ) {
        final var requestKpi = getRequestKpiByOrgRequestId(sharedRide.getRequestKpiList(), singleRequestDTO.getRequestId());
        if (requestKpi == null) {
            log.error("SRM: getFirstWaypoint: getRequestKpiByOrgRequestId returned null! {}", sharedRide);
            return null;
        }
        for (final var waypoint : sharedRide.getWaypoints()) {
            if (waypoint.getRequestKpiId().equals(requestKpi.getId())) {
                return waypoint;
            }
        }
        return null;
    }

    private boolean filterByDeviationOfPickUpTime(
            SrmSharedRide sharedRide,
            SrmMultipleRequestDTO multipleRequestDTO
    ) {

        // getTimeDeviationMin - должен быть достаточно большой, потому что кто-то может присоединяться не в первой точке СП.
        // нужно определять разницу между желаемым начаом поездки в заявке и реальным после совмещения.
        // и именно оно должно быть меньше TimeDeviationMin и обе даты не должны быть в прошедшем времени
        final var waypoint = getFirstWaypoint(sharedRide, multipleRequestDTO.getRequestDTOList().get(0));
        if (waypoint == null) {
            log.error("SRM: filterByDeviationOfPickUpTime: getFirstWaypoint returned null! {}", sharedRide);
            return false;
        }
        if (waypoint.getStartTime() == null) {
            log.error("SRM: filterByDeviationOfPickUpTime: waypoint start time is null! {}", sharedRide);
            return false;
        }
        final var diff = Math.abs(waypoint.getStartTime().toInstant().toEpochMilli()
                - multipleRequestDTO.getPickupTime().toInstant().toEpochMilli());
        final var maxTimeDeviationMillies = (long) sharedRide.getCoopTariffParams().getTimeDeviationMin() * 60 * 1000;
        if (diff > maxTimeDeviationMillies) {
            log.warn("Time 1 SharedRide = {} ({}), time 2 (request) = {} ({})", waypoint.getStartTime(), waypoint.getStartTime().toInstant().toEpochMilli(), multipleRequestDTO.getPickupTime(), multipleRequestDTO.getPickupTime().toInstant().toEpochMilli());
            log.warn("Diff between two time1 and time2 in milli = {} is greater than {}", diff, maxTimeDeviationMillies);
            log.warn("SRM: isMatchingByStartTime: rejected by difference between start times");
            return false;
        }
        final var now = ZonedDateTime.now();
        if (now.isAfter(waypoint.getStartTime())) {
            log.warn("SRM: isMatchingByStartTime: rejected because shared ride allready started");
            return false;
        }
        if (now.isAfter(multipleRequestDTO.getPickupTime())) {
            log.warn("SRM: isMatchingByStartTime: rejected because request pickup time allready passed");
            return false;
        }
        return true;
    }

    private static boolean triggerTimePassed(SrmSharedRide sharedRide) {
        final var triggerTime = sharedRide.getCoopTariffParams().getMinCancelTimeMin();
        final var start = sharedRide.getWaypoints().get(0).getStartTime();
        final var now = ZonedDateTime.now();
        final var minutes = ChronoUnit.MINUTES.between(now, start);
        final var triggerTimePassed = minutes < triggerTime;
        if (triggerTimePassed) {
            log.debug("SRM: triggerTimePassed: minutes left = {}, triggertime = {}", minutes, triggerTime);
        }
        return triggerTimePassed;
    }

    private boolean filterByPreservationOfWaypointsOrder(SrmSharedRide sharedRide) {
        return filterByPreservationOfAllWaypoints(sharedRide.getActiveRequestKpiList(), sharedRide.getWaypoints());
    }

    private boolean filterByPreservationOfAllWaypoints(List<SrmRequestKpi> requestKpiList, List<SrmWaypoint> waypoints) {
        // проверка на порядок точек после присоединения. все точки должны сохранить порядок.
        for (final var requestKpi : requestKpiList) {
            if (requestKpi.isActive()) {
                final var listOrgOrder = extractRequestWaypointsOrgOrder(waypoints, requestKpi.getId());
                final var listFinalOrder = extractRequestWaypointsSharedOrder(waypoints, requestKpi.getId());
                if (listOrgOrder.size() != listFinalOrder.size()) {
                    log.warn("SRM: filterByPreservationOfAllWaypoints: number of waypoints differs");
                    return false;
                }
                for (int i = 0; i < listOrgOrder.size(); i++) {
                    final var waypointOrg = listOrgOrder.get(i);
                    final var waypointFinal = listFinalOrder.get(i);
                    if (!waypointOrg.getId().equals(waypointFinal.getId())) {
                        log.warn("SRM: filterByPreservationOfAllWaypoints: waypoint misplaced with index {}", i);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // для употребления ПЕРЕД объедиенением, для проверки подходящести маршрута.
    // по факту: неоптимизированные запросы значит isMatchingByAllWaypoints
    // оптимизированные запросы - в деле может быть только 2 точки для грузовых перевозок
    // есть тест на оптимизированный запрос с бОльшим количеством точек.
    private boolean isMatchingByWaypoints(SrmSharedRide sharedRide, SrmSingleRequestDTO singleRequestDTO) {
        if (sharedRide.getPointMatchingType().equals(PointMatchingType.MATCH_FIRST_AND_LAST_POINTS)) {
            return isMatchingByFirstAndLastWaypoints(sharedRide, singleRequestDTO);
        }
        if (sharedRide.getPointMatchingType().equals(PointMatchingType.MATCH_ALL_POINTS)) {
            return isMatchingByAllWaypoints(sharedRide, singleRequestDTO);
        }
        return true;
    }

    // проверка что все точки лежат на маршруте и следуют в заданном порядке.
    // находим группу для каждой точки маршрута - если все точки на маршруте в заданном порядке - поездка подходит
    private boolean isMatchingByAllWaypoints(SrmSharedRide sharedRide, SrmSingleRequestDTO singleRequestDTO) {
        double includeRadius = sharedRide.getCoopTariffParams().getDistanceDeviationKm();
        UUID prevGroupId = null;
        for (SrmWaypointPostDTO waypointPostDTO : singleRequestDTO.getWaypoints()) {
            final var groupId = getGroupId(sharedRide, waypointPostDTO, prevGroupId, includeRadius);
            if (groupId == null) {
                log.debug("isMatchingByAllWaypoints: for singleRequestDTO {} returning false", singleRequestDTO.getRequestId());
                return false;
            }
            prevGroupId = groupId;
        }
        return true;
    }

    private UUID getGroupId(SrmSharedRide sharedRide, SrmWaypointPostDTO targetWaypoint, UUID prevGroupId, double includeRadius) {
        int prevIndex = 0;
        if (prevGroupId != null) {
            prevIndex = getGroupLastElementIndexPlusOne(sharedRide.getWaypoints(), prevGroupId);
        }
        if (prevIndex == sharedRide.getWaypoints().size()) {
            return null;
        }
        for (int i = prevIndex; i < sharedRide.getWaypoints().size(); i++) {
            final var waypoint = sharedRide.getWaypoints().get(i);
            var groupId = waypoint.getGroupId();
            if (groupId == null) {
                throw new SrmLogicException("SRM: getGroupId: group id is null!");
            }
            final var waypointSet = new HashSet<>(getByGroup(sharedRide.getWaypoints(), groupId));
            if (isSetInsideRadius(waypointSet, targetWaypoint, includeRadius)) {
                return groupId;
            }
        }
        return null;
    }

    // проверка что первая и последняя точки лежат на маршруте в нужном порядке
    // находим группу для первой точки маршрута
    // находим группу для последней точки маршрута
    // если первая и последняя точка на маршруте и следуют в заданном порядке - поездка подходит
    private boolean isMatchingByFirstAndLastWaypoints(SrmSharedRide sharedRide, SrmSingleRequestDTO singleRequestDTO) {
        double includeRadius = sharedRide.getCoopTariffParams().getDistanceDeviationKm();

        final var waypointFirst = singleRequestDTO.getWaypoints().get(0);
        final var waypointLast = singleRequestDTO.getWaypoints().get(singleRequestDTO.getWaypoints().size() - 1);

        int firstPointMatchIndex = -1;
        int lastPointMatchIndex = -1;
        for (int i = 0; i < sharedRide.getWaypoints().size(); i++) {
            final var waypoint = sharedRide.getWaypoints().get(i);
            var groupId = waypoint.getGroupId();
            final var waypointSet = new HashSet<SrmWaypoint>();
            if (groupId == null) {
                waypointSet.add(waypoint);
            } else {
                waypointSet.addAll(getByGroup(sharedRide.getWaypoints(), groupId));
            }
            if ((firstPointMatchIndex == -1) && (isSetInsideRadius(waypointSet, waypointFirst, includeRadius))) {
                firstPointMatchIndex = i;
            }
            if ((lastPointMatchIndex == -1) && (isSetInsideRadius(waypointSet, waypointLast, includeRadius))) {
                lastPointMatchIndex = i;
            }
        }
        // если условие firstPointMatchIndex < lastPointMatchIndex то первая точка и последняя должны быть в разных группах
        // если условие firstPointMatchIndex <= lastPointMatchIndex их группы могу совпадать
        if (firstPointMatchIndex > -1 && lastPointMatchIndex > -1 && firstPointMatchIndex <= lastPointMatchIndex) {
            return true;
        } else {
            log.debug("isMatchingByFirstAndLastWaypoints: for singleRequestDTO {} returning false", singleRequestDTO.getRequestId());
            return false;
        }
    }

    private UUID getGroupId(SrmSharedRide sharedRide, SrmWaypoint targetWaypoint, SrmWaypoint prevWaypoint, double includeRadius) {
        int prevIndex = 0;
        if (prevWaypoint != null) {
            prevIndex = sharedRide.getWaypoints().indexOf(prevWaypoint);
        }
        if (prevIndex == -1) {
            throw new SrmLogicException("SRM: ERROR: insertWaypointNotOptimized: getGroupId: prevWaypoint not found!");
        }
        for (int i = prevIndex; i < sharedRide.getWaypoints().size(); i++) {
            final var waypoint = sharedRide.getWaypoints().get(i);
            UUID groupId = waypoint.getGroupId();
            if (groupId == null) {
                throw new SrmLogicException("SRM: getGroupId: group id is null!");
            }
            final var waypointSet = new HashSet<>(getByGroup(sharedRide.getWaypoints(), groupId));
            if (isSetInsideRadius(waypointSet, targetWaypoint, includeRadius)) {
                return groupId;
            }
        }
        return null;
    }

    private void setGroupsToNewWaypointsOptimized(SrmSharedRide sharedRide, List<SrmWaypoint> newWaypoints) {
        final var includeRadius = sharedRide.getCoopTariffParams().getDistanceDeviationKm();

        int firstPointMatchIndex = -1;
        int lastPointMatchIndex = -1;
        for (int i = 0; i < sharedRide.getWaypoints().size(); i++) {

            final var waypoint = sharedRide.getWaypoints().get(i);
            var groupId = waypoint.getGroupId();
            final var waypointSet = new HashSet<SrmWaypoint>();
            if (groupId == null) {
                waypointSet.add(waypoint);
                groupId = UUID.randomUUID();
            } else {
                waypointSet.addAll(getByGroup(sharedRide.getWaypoints(), groupId));
            }

            final var waypointFirst = newWaypoints.get(0);
            final var waypointLast = newWaypoints.get(newWaypoints.size() - 1);

            if (firstPointMatchIndex == -1 && isSetInsideRadius(waypointSet, waypointFirst, includeRadius)) {
                firstPointMatchIndex = i;
                waypointFirst.setGroupId(groupId);
                for (SrmWaypoint waypoint1 : waypointSet) {
                    if (waypoint1.getGroupId() == null) {
                        waypoint1.setGroupId(groupId);
                    }
                }
            }
            if (lastPointMatchIndex == -1 && isSetInsideRadius(waypointSet, waypointLast, includeRadius)) {
                lastPointMatchIndex = i;
                waypointLast.setGroupId(groupId);
                for (final var waypoint1 : waypointSet) {
                    if (waypoint1.getGroupId() == null) {
                        waypoint1.setGroupId(groupId);
                    }
                }
            }
        }
    }

    private List<SrmWaypoint> getByGroup(List<SrmWaypoint> waypoints, UUID groupId) {
        return waypoints.stream().filter(it -> groupId.equals(it.getGroupId())).toList();
    }

    private boolean isSetInsideRadius(Set<SrmWaypoint> waypointSet, Waypoint targetWaypoint, double includeRadius) {
        return waypointSet.stream().allMatch(it -> isWaypointInsideRadius(it, targetWaypoint, includeRadius));
    }

    private boolean isWaypointInsideRadius(
            Waypoint waypointFrom, Waypoint waypointTo,
            double includeRadius
    ) {
        final var distance = calculateDistanceInKilometer(waypointFrom.getLatitude(), waypointFrom.getLongitude(),
                waypointTo.getLatitude(), waypointTo.getLongitude());
        // *** здесь сравниваются километры с радиусом
        return distance <= includeRadius;
    }

    private boolean filterByCapacity(SrmSharedRide sharedRide, BaseTariff tariff) {
        boolean result = false;
        if (sharedRide.getTransportType() == TransportTypeEnum.TAXI) {
            result = filterByMaxPassengers(sharedRide, tariff);
        }
        if (sharedRide.getTransportType() == TransportTypeEnum.PERSONAL) {
            result = filterByMaxPassengers(sharedRide, tariff);
        }
        return result;
    }

    private boolean filterByMaxPassengers(SrmSharedRide sharedRide, BaseTariff tariff) {
        int maxCapacity = Optional.ofNullable(getMaxCapacity(tariff)).orElse(0);
        if (maxCapacity == 0) {
            if (sharedRide.getTransportType() == TransportTypeEnum.TAXI) {
                maxCapacity = 4;
            }
            if (sharedRide.getTransportType() == TransportTypeEnum.PERSONAL) {
                maxCapacity = 5;
            }
        }
        log.trace("SRM: filterByCapacity: capacity = {}", maxCapacity);
        int totalPassengers = 0;
        for (final var waypoint : sharedRide.getWaypoints()) {
            if (waypoint.getEventType() == EventType.BOARDING) {
                SrmRequestKpi requestKpi = getRequestKpiById(sharedRide.getRequestKpiList(), waypoint.getRequestKpiId());
                if (requestKpi == null) {
                    throw new SrmLogicException("filterByCapacity: requestKpi not found 1!");
                }
                totalPassengers += requestKpi.getRequiredPassengers();
                if (totalPassengers > maxCapacity) {
                    log.warn("SRM: filterByCapacity capacity exceeded");
                    return false;
                }
            }
            if (waypoint.getEventType() == EventType.UNBOARDING) {
                SrmRequestKpi requestKpi = getRequestKpiById(sharedRide.getRequestKpiList(), waypoint.getRequestKpiId());
                if (requestKpi == null) {
                    throw new SrmLogicException("filterByCapacity: requestKpi not found 2!");
                }
                totalPassengers -= requestKpi.getRequiredPassengers();
            }
        }
        return true;
    }

    private SrmRequestKpi getRequestKpiById(List<SrmRequestKpi> requestKpiList, UUID requestKpiId) {
        for (final var requestKpi : requestKpiList) {
            if (requestKpi.getId().equals(requestKpiId)) {
                return requestKpi;
            }
        }
        return null;
    }

    private SrmRequestKpi getRequestKpiByOrgRequestId(List<SrmRequestKpi> requestKpiList, UUID requestKpiId) {
        for (SrmRequestKpi requestKpi : requestKpiList) {
            if (requestKpi.getOrgRequestId().equals(requestKpiId)) {
                return requestKpi;
            }
        }
        return null;
    }

    private boolean filterByEconomyProcent(SrmSharedRide sharedRide) {
        double minimalSavingProcent = sharedRide.getCoopTariffParams().getSavingsDeviationPct();
        for (SrmRequestKpi requestKpi : sharedRide.getRequestKpiList()) {
            if (!((requestKpi.getSavingsProcents() >= minimalSavingProcent) && (requestKpi.getSavingsCash() > 0))) {
                log.warn("SRM: filterByEconomyProcent: rejected: {} with minimalSavingProcent = {} requestKpi.getSavingsProcents() = {} requestKpi.getSavingsCash() = {}",
                        sharedRide.getId(), minimalSavingProcent, requestKpi.getSavingsProcents(), requestKpi.getSavingsCash());
                if (requestKpi.getSavingsProcents() == 49d) {
                    log.warn("SRM: POSSIBLE ERROR! 49% found: {}", sharedRide);
                }
                return false;
            }
        }
        return true;
    }

    private Integer getUniqueOldId(UUID requestId) {
        var oldId = generateOldId(requestId);
        while (!(oldId > DRUG_LOWER_BOUND && oldId < DRUG_UPPER_BOUND)) {
            oldId = generateOldId(UUID.randomUUID());
        }
        return oldId;
    }

    private Integer generateOldId(UUID requestId) {
        var oldId = uuidToInt(requestId);
        var requestKpiOpt = requestKpiRepository.findByOldId(oldId);
        while (requestKpiOpt.isPresent()) {
            oldId += 1;
            requestKpiOpt = requestKpiRepository.findByOldId(oldId);
        }
        return oldId;
    }

    private SrmSharedRide uniteRequestWithSharedRide(SrmSharedRide sharedRide, BaseTariff tariff, SrmMultipleRequestDTO multipleRequestDTO) {
        sharedRide.getRequestKpiList().removeIf(Objects::isNull);
        // stage 1: add new requestKpis (to the end of list)
        final var requestKpiList = new ArrayList<SrmRequestKpi>();
        for (final var singleRequestDTO : multipleRequestDTO.getRequestDTOList()) {
            final var requestKpi = new SrmRequestKpi();
            requestKpi.setId(UUID.randomUUID());
            requestKpi.setOrgRequestId(singleRequestDTO.getRequestId());
            requestKpi.setOldId(getUniqueOldId(singleRequestDTO.getRequestId()));
            requestKpi.setPickupTime(multipleRequestDTO.getPickupTime());
            requestKpi.setDropTime(multipleRequestDTO.getDropTime());
            requestKpi.setRequiredPassengers(singleRequestDTO.getRequiredPassengers());
            requestKpi.setRequiredVolume(singleRequestDTO.getRequiredVolume());
            requestKpi.setRequiredWeight(singleRequestDTO.getRequiredWeight());
            requestKpi.setCreationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())));
            requestKpi.setCargoExpress(singleRequestDTO.getCargoExpress());
            requestKpi.setCandidate(singleRequestDTO.isCandidate());
            requestKpi.setActive(true);
            requestKpi.setSharedRide(sharedRide);

            requestKpiList.add(requestKpi);
        }

        // stage 2: add new stops to shared ride (to the end of list)
        final var newRequestWaypointList = new ArrayList<SrmWaypoint>();
        for (final var singleRequestDTO : multipleRequestDTO.getRequestDTOList()) {
            final var requestKpi = getRequestKpiByOrgRequestId(requestKpiList, singleRequestDTO.getRequestId());
            if (requestKpi == null) {
                throw new SrmLogicException("uniteRequestWithSharedRide: requestKpi not found!");
            }
            newRequestWaypointList.addAll(mapSrmWaypointPostDtoListToSrmWaypointList(singleRequestDTO.getWaypoints(),
                    sharedRide,
                    requestKpi.getId()));
        }
        if (sharedRide.getRequestKpiList().isEmpty()) { // if new shared ride
            newRequestWaypointList.get(0).setStartTime(requestKpiList.get(0).getPickupTime());
        }
        sharedRide.getRequestKpiList().addAll(requestKpiList);
        return insertWaypoints(sharedRide, tariff, newRequestWaypointList);
    }

    private SrmSharedRide insertWaypoints(SrmSharedRide sharedRide, BaseTariff tariff, List<SrmWaypoint> newWaypointList) {
        printPath(sharedRide.getWaypoints(), "stage0@" + sharedRide.getId());
        switch (sharedRide.getPointMatchingType()) {
            case MATCH_ALL_POINTS -> insertWaypointsNotOptimized(sharedRide, newWaypointList);
            case MATCH_FIRST_AND_LAST_POINTS -> {
                setGroupsToNewWaypointsOptimized(sharedRide, newWaypointList);
                sharedRide.getWaypoints().addAll(newWaypointList);
            }
            case MATCH_POINTS_FALSE -> sharedRide.getWaypoints().addAll(newWaypointList);
        }
        printPath(sharedRide.getWaypoints(), "stage1@" + sharedRide.getId());
        return calculateDistancesAndTimesForSharedRide(sharedRide, tariff);  //uniteRequestWithSharedRide
    }

    private void insertWaypointsNotOptimized(SrmSharedRide sharedRide, List<SrmWaypoint> waypointList) {
        if (sharedRide.getWaypoints().isEmpty()) { // insert all at once
            for (SrmWaypoint waypoint : waypointList) {
                waypoint.setGroupId(UUID.randomUUID());
                sharedRide.getWaypoints().add(waypoint);
            }
        } else {   // insert one by one
            SrmWaypoint prevWaypoint = null;
            for (SrmWaypoint waypoint : waypointList) {
                prevWaypoint = insertWaypointNotOptimized(sharedRide, waypoint, prevWaypoint);
            }
        }
    }

    private SrmWaypoint insertWaypointNotOptimized(
            SrmSharedRide sharedRide,
            SrmWaypoint targetWaypoint,
            SrmWaypoint prevWaypoint
    ) {
        double includeRadius = sharedRide.getCoopTariffParams().getDistanceDeviationKm();
        boolean newGroup = false;
        UUID groupId = getGroupId(sharedRide, targetWaypoint, prevWaypoint, includeRadius);
        if (groupId == null) { // we allready checked that all points have a group
            throw new SrmLogicException("SRM: ERROR: insertWaypointNotOptimized: groupId is null for waypoint " + targetWaypoint.getId());
        }
        targetWaypoint.setGroupId(groupId);

        LinkedList<SrmWaypoint> linkedList = new LinkedList<>(sharedRide.getWaypoints());
        int index = getGroupLastElementIndexPlusOne(linkedList, groupId);
        log.trace("SRM: insertWaypointNotOptimized: for targetWaypoint = {} total size = {} newGroup = {} groupId = {} index = {}", targetWaypoint.getAddress(), linkedList.size(), newGroup, groupId, index);
        linkedList.add(index, targetWaypoint);
        sharedRide.getWaypoints().clear();
        sharedRide.getWaypoints().addAll(linkedList);
        return targetWaypoint;
    }

    private int getGroupLastElementIndexPlusOne(List<SrmWaypoint> list, UUID groupId) {
        if (list.isEmpty()) {
            return 0;
        }
        int index = 0;
        while (!list.get(index).getGroupId().equals(groupId)) {
            index++;
            if (index == list.size()) {
                break;
            }
        }
        if (index < list.size()) {
            while (list.get(index).getGroupId().equals(groupId)) {
                index++;
                if (index == list.size()) {
                    break;
                }
            }
        }
        return index;
    }

    private SrmSharedRide calculateDistancesAndTimesForSharedRide(SrmSharedRide sharedRide, BaseTariff tariff) {
        final var gisDistances = new HashMap<ImmutablePair<SrmWaypoint, SrmWaypoint>, Double>();
        final var gisDurations = new HashMap<ImmutablePair<SrmWaypoint, SrmWaypoint>, Integer>();
        // get data from geoservice
        setUpGisData(sharedRide, gisDistances, gisDurations);
        // create graph
        final var waypoints = sharedRide.getWaypoints();
        final var graph = createGraph(waypoints);
        // set weights
        final var setGraphResult = checkGraphWeightsDefined(sharedRide, graph, gisDistances);
        if (!setGraphResult) {
            return null;
        }
        // print graph
        printGraph(graph);

        final var pointMatchingType = sharedRide.getPointMatchingType();
        final var optimized = pointMatchingType.isOptimized();
        log.trace("calculateDistancesAndTimes: optimizeWaypoints is {}", optimized);
        if (optimized && waypoints.size() > 2) { // run optimization algorithm
            ZonedDateTime startTime = waypoints.get(0).getStartTime();
            if (pointMatchingType.equals(PointMatchingType.MATCH_POINTS_FALSE)) {
                List<TspAlgorithm> tspAlgorithmList = new ArrayList<>();
                final var activeRequestKpiList = sharedRide.getActiveRequestKpiList();
                for (SrmRequestKpi requestKpi : activeRequestKpiList) {
                    Optional<SrmWaypoint> startOpt = getStartPoint(waypoints, requestKpi.getId());
                    if (startOpt.isPresent()) {
                        log.trace("SRM: trying start point {}", startOpt.get().getAddress());
                        List<SrmWaypoint> optimizedWaypointList = calculateTSP(graph, startOpt.get());
                        setOrderingIndexWaypoints(optimizedWaypointList);
                        if (filterByPreservationOfAllWaypoints(activeRequestKpiList, optimizedWaypointList)) {
                            double pathDistance = processPath(optimizedWaypointList, graph);
                            tspAlgorithmList.add(new TspAlgorithm(optimizedWaypointList, pathDistance));
                        }
                    }
                }
                if (!tspAlgorithmList.isEmpty()) { // optimization succeeded. else optimization failed and order remains inserted to end of list
                    int index = getShortestAlgorithm(tspAlgorithmList);
                    log.trace("SRM: chosen start point {}", tspAlgorithmList.get(index).waypointList.get(0).getAddress());
                    waypoints.clear();
                    waypoints.addAll(tspAlgorithmList.get(index).waypointList);
                }
            } else {
                List<SrmWaypoint> optimizedWaypointList = calculateTSP(graph, waypoints.get(0));
                waypoints.clear();
                waypoints.addAll(optimizedWaypointList);
            }
            waypoints.get(0).setStartTime(startTime);
        }

        setOrderingIndexWaypoints(waypoints);
        setOrderingIndexKpis(sharedRide.getRequestKpiList());
        // set total times
        SrmMetrics distanceAndTime = calculatePathMetrics(waypoints,
                graph,
                gisDurations,
                true);
        // set shared ride distance and time
        sharedRide.setRideTime(distanceAndTime.totalSeconds + distanceAndTime.primaryWaitingTime + distanceAndTime.intermediateWaitingTime);
        sharedRide.setRideDistance(distanceAndTime.totalDistance);
        sharedRide.setRideCost(calculateByTariff(sharedRide, tariff, distanceAndTime));
        recalculateRequestKpis(sharedRide, tariff, graph, gisDurations);
        printPath(waypoints, "stage2@" + sharedRide.getId());
        return sharedRide;
    }

    Optional<SrmWaypoint> getStartPoint(List<SrmWaypoint> waypointList, UUID requestKpiId) {
        return waypointList.stream().filter(e -> e.getRequestKpiId().equals(requestKpiId)).findFirst();
    }

    private Long calculateByTariff(SrmSharedRide sharedRide, BaseTariff tariff, SrmMetrics distanceAndTime) {
        if (sharedRide.getWaypoints().isEmpty()) {
            return 0L;
        }
        return switch (sharedRide.getTransportType()) {
            case PERSONAL ->
                    TariffUtils.calculateByPersonalTariff((PersonalTariff) tariff, sharedRide.getWaypoints().get(0).getStartTime(), distanceAndTime);
            case TAXI -> TariffUtils.calculateByTaxiTariff((TaxiTariff) tariff,
                    sharedRide.getWaypoints().get(0).getStartTime(),
                    distanceAndTime);
            default -> null;
        };
    }

    private void recalculateRequestKpis(
            SrmSharedRide sharedRide,
            BaseTariff tariff,
            Graph<SrmWaypoint, DefaultWeightedEdge> graph,
            HashMap<ImmutablePair<SrmWaypoint, SrmWaypoint>, Integer> gisDurations
    ) {
        List<SrmRequestKpi> requestKpiList = sharedRide.getActiveRequestKpiList();
        // calculate distance and time for each original request
        for (SrmRequestKpi requestKpi : requestKpiList) {
            List<SrmWaypoint> list = extractRequestWaypointsOrgOrder(sharedRide.getWaypoints(), requestKpi.getId());
            SrmMetrics metrics = calculatePathMetrics(list,
                    graph,
                    gisDurations,
                    false);
            requestKpi.setRequestTime(metrics.totalSeconds
                    + metrics.primaryWaitingTime
                    + metrics.intermediateWaitingTime);
            requestKpi.setRequestDistance(metrics.totalDistance);
            requestKpi.setRequestFullPrice(calculateByTariff(sharedRide, tariff, metrics));
        }
        // calculate request prices within shared ride
        calculateRequestPricesForPassengers(requestKpiList, sharedRide);
        checkPriceLogicPassengers(sharedRide);
        // calculate economy for each request
        calculateEconomy(requestKpiList, sharedRide);
    }

    private void calculateRequestPricesForPassengers(List<SrmRequestKpi> requestKpiList, SrmSharedRide sharedRide) {
        double totalDistance = requestKpiList.stream()
                .mapToDouble(SrmRequestKpi::getRequestDistance)
                .sum();
        // recalculate costSharePart by calculating percentage of request distance from total accumulated distance
        for (SrmRequestKpi requestKpi : requestKpiList) {
            double costSharePart = 1d;
            if (totalDistance != 0) {
                costSharePart = roundDouble(requestKpi.getRequestDistance() / totalDistance, 7);
            }
            if (costSharePart < 1) {
                log.debug("costSharePart = {}", costSharePart);
            }
            requestKpi.setCostSharePart(costSharePart);
            requestKpi.setRequestPrice((long) (sharedRide.getRideCost() * requestKpi.getCostSharePart()));
            log.trace("SRM: calculateRequestPricesForPassengers: SHARE PART: requestKpi.getRequestDistance() = {}, totalDistance = {}, share part = {}",
                    requestKpi.getRequestDistance(), totalDistance, requestKpi.getCostSharePart());
        }
        double totalShareParts = requestKpiList.stream().mapToDouble(SrmRequestKpi::getCostSharePart).sum();
        if (!requestKpiList.isEmpty() && (1 - totalShareParts) > 0.00001) {
            log.warn("SRM: ERROR: calculateRequestPricesForPassengers: SHARE PART CHECK1: totalShareParts = {}", totalShareParts);
        }
    }

    private void calculateEconomy(List<SrmRequestKpi> requestKpiList, SrmSharedRide sharedRide) {
        for (var requestKpi : requestKpiList) {
            var economyCash = requestKpi.getRequestFullPrice() - requestKpi.getRequestPrice();
            var economyPercent = Math.ceil((100 * economyCash) / requestKpi.getRequestFullPrice().doubleValue());

            log.trace("SRM: recalculateRequestKpis: sharedRide.getRideCost() = {}", sharedRide.getRideCost());
            log.trace("SRM: recalculateRequestKpis: requestKpi.getCostSharePart() = {}", requestKpi.getCostSharePart());
            log.trace("SRM: recalculateRequestKpis: cash in shared ride = {}", (long) (sharedRide.getRideCost() * requestKpi.getCostSharePart()));
            log.trace("SRM: recalculateRequestKpis: request price (cash out of shared ride) = {}", requestKpi.getRequestPrice());
            log.trace("SRM: recalculateRequestKpis: economyCash = {}", economyCash);
            log.trace("SRM: recalculateRequestKpis: economyProcent = {}", economyPercent);
            requestKpi.setSavingsCash(economyCash);
            requestKpi.setSavingsProcents(economyPercent);
        }
    }

    private void checkPriceLogicPassengers(SrmSharedRide sharedRide) {
        long totalPrice = 0;
        List<SrmRequestKpi> activeRequestKpiList = sharedRide.getActiveRequestKpiList();
        for (SrmRequestKpi requestKpi : activeRequestKpiList) {
            totalPrice += requestKpi.getRequestPrice();
        }
        if (Math.abs(totalPrice - sharedRide.getRideCost()) > activeRequestKpiList.size()) {
            log.warn("checkPriceLogicPassengers: WARNING! requestPrices vs sharedRidePrice: {}/{}, sharedRide = {}", totalPrice, sharedRide.getRideCost(), sharedRide);
        }
    }

    private List<SrmWaypoint> extractRequestWaypointsOrgOrder(List<SrmWaypoint> waypoints, UUID requestKpiId) {
        // sort by original order
        return waypoints.stream()
                .filter(e -> e.getRequestKpiId().equals(requestKpiId))
                .sorted(Comparator.comparing(SrmWaypoint::getOrgOrderingIndex))
                .collect(Collectors.toList()); // NOSONAR
    }

    private List<SrmWaypoint> extractRequestWaypointsSharedOrder(List<SrmWaypoint> waypoints, UUID requestKpiId) {
        // sort by original order
        return waypoints.stream()
                .filter(e -> e.getRequestKpiId().equals(requestKpiId))
                .sorted(Comparator.comparing(SrmWaypoint::getOrderingIndex))
                .collect(Collectors.toList()); // NOSONAR
    }

    // distance in km with double, duration in seconds with long
    private SrmMetrics calculatePathMetrics(
            List<SrmWaypoint> list,
            Graph<SrmWaypoint, DefaultWeightedEdge> graph,
            HashMap<ImmutablePair<SrmWaypoint, SrmWaypoint>,
                    Integer> gisDurations,
            boolean updateWaypoints
    ) {
        if (list.isEmpty()) {
            return new SrmMetrics(0, 0, 0, 0);
        }
        Double totalDistance = 0d;
        int primaryWaitingTime = list.get(0).getWaitingTime();
        int intermediateWaitingTime = 0;
        int totalSeconds = 0;

        if (updateWaypoints && list.get(0).getStartTime() != null) {
            list.get(0).setEndTime(plusTime(list.get(0).getStartTime(), list.get(0).getWaitingTime()));
        }
        for (int i = 1; i < list.size(); i++) {
            SrmWaypoint waypointFrom = list.get(i - 1);
            SrmWaypoint waypointTo = list.get(i);
            var distanceAndTime = getDistanceAndTime(waypointFrom, waypointTo,
                    graph, gisDurations);
            totalDistance += distanceAndTime.left;
            totalSeconds += distanceAndTime.right;
            intermediateWaitingTime += waypointTo.getWaitingTime();
            if (updateWaypoints) {
                waypointTo.setStartTime(plusTime(waypointFrom.getEndTime(), distanceAndTime.right));
                waypointTo.setEndTime(plusTime(waypointTo.getStartTime(),
                        waypointTo.getWaitingTime()));
                waypointTo.setDistanceFromPrevWaypoint(distanceAndTime.left);
            }
        }
        totalDistance = roundDouble(totalDistance, 3);
        return new SrmMetrics(totalDistance, totalSeconds, primaryWaitingTime, intermediateWaitingTime);
    }

    private ImmutablePair<Double, Integer> getDistanceAndTime(
            SrmWaypoint waypointFrom,
            SrmWaypoint waypointTo,
            Graph<SrmWaypoint, DefaultWeightedEdge> graph,
            HashMap<ImmutablePair<SrmWaypoint, SrmWaypoint>, Integer> gisDurations
    ) {
        double distance = graph.getEdgeWeight(graph.getEdge(waypointFrom, waypointTo));
        Integer duration = gisDurations.get(new ImmutablePair<>(waypointFrom, waypointTo));
        if (duration == null) {
            duration = getDurationByDistance(distance);
        }
        return new ImmutablePair<>(distance, duration);
    }

    private void setUpGisData(
            SrmSharedRide sharedRide,
            Map<ImmutablePair<SrmWaypoint, SrmWaypoint>, Double> gisDistances,
            Map<ImmutablePair<SrmWaypoint, SrmWaypoint>, Integer> gisDurations
    ) {
        final var waypointList = sharedRide.getWaypoints();
        log.info("Waypoints count: {}", waypointList.size());
        final var matrix = waypointList.size() <= 25 // согласно документации 2Гис маршруты менее 25 точек можно строить синхронно
                ? geoService.getDistanceMatrix(waypointList, sharedRide.getTransportType())
                : geoService.getDistanceMatrixAsync(waypointList, sharedRide.getTransportType());

        checkMatrix(matrix);
        // copy matrix to hashmap
        if (matrix.getOrigins().isEmpty()) {
            log.error("SRM: ERROR! Distance matrix came empty!");
        }
        for (int org = 0; org < waypointList.size(); org++) {
            for (int dest = 0; dest < waypointList.size(); dest++) {
                final var key = new ImmutablePair<>(waypointList.get(org), waypointList.get(dest));
                try {
                    final var element = matrix.getOrigins().get(org).getDestinations().get(dest);
                    var distance = Double.MAX_VALUE;
                    var duration = Integer.MAX_VALUE;
                    if (OPERATION_OK.equalsIgnoreCase(element.status())) {
                        distance = (double) element.distance() / 1000;
                        duration = element.duration();
                    }
                    gisDistances.put(key, distance);
                    gisDurations.put(key, duration);
                } catch (IndexOutOfBoundsException e) {
                    log.error("setUpGisData: value for org {}, dest {} not found!", org, dest);
                }
            }
        }
    }

    private void checkMatrix(DistanceMatrixResponseDTO matrix) {
        // check if matrix contains all values A-B = B-A
        int numOfFailed = 0;
        for (int org = 0; org < matrix.getOrigins().size(); org++) {
            var row = matrix.getOrigins().get(org);
            for (int dest = 0; dest < row.getDestinations().size(); dest++) {
                var element = row.getDestinations().get(dest);
                if (!OPERATION_OK.equalsIgnoreCase(element.status())) {
                    numOfFailed++;
                    log.warn("SRM: matrix: failed element: x = " + org + ", y = " + dest);
                }
            }
        }
        int x = matrix.getOrigins().size();
        int y = 0;
        if (x > 0) {
            y = matrix.getOrigins().get(0).getDestinations().size();
        }
        log.trace("SRM: matrix size: " + x + "/" + y + ", failed elements number = " + numOfFailed);
    }

    /**
     * Returns duration in SECONDS
     */
    private int getDurationByDistance(double distance) { // now estimated by velocity 50km/hour
        return (int) ((distance / 50) * 60 * 60);
    }

    /**
     * Pluses SECONDS
     */
    private ZonedDateTime plusTime(ZonedDateTime time, Integer duration) {
        if (time == null) {
            throw new SrmLogicException("plusTime: time is null!");
        }
        LocalDateTime localDateTime = time.toLocalDateTime();
        return ZonedDateTime.of(localDateTime.plusSeconds(duration), ZoneId.of(ZoneOffset.UTC.getId()));
    }

    /**
     * Creates graph and fills in with data.
     *
     * @return graph.
     */
    private Graph<SrmWaypoint, DefaultWeightedEdge> createGraph(List<SrmWaypoint> waypointList) {
        Graph<SrmWaypoint, DefaultWeightedEdge> graph =
                new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        for (SrmWaypoint waypoint : waypointList) {
            graph.addVertex(waypoint);
        }
        for (int i = 0; i < waypointList.size(); ++i) {
            for (int j = i + 1; j < waypointList.size(); ++j) {
                SrmWaypoint waypoint1 = waypointList.get(i);
                SrmWaypoint waypoint2 = waypointList.get(j);
                graph.addEdge(waypoint1, waypoint2);
            }
        }
        return graph;
    }

    /**
     * Set weights for graph. Сейчас в качестве весов устанавливаются расстояния между точками. Опционально в качестве весов могут устанавливаться
     * длительности между точками.
     */
    private boolean checkGraphWeightsDefined(
            SrmSharedRide sharedRide,
            Graph<SrmWaypoint, DefaultWeightedEdge> graph,
            HashMap<ImmutablePair<SrmWaypoint, SrmWaypoint>, Double> gisDistances
    ) {
        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            SrmWaypoint source = graph.getEdgeSource(edge);
            SrmWaypoint target = graph.getEdgeTarget(edge);
            Double weight = gisDistances.get(new ImmutablePair<>(source, target));
            if (weight == null) { // if geoservice is not working get direct distance
                weight = calculateDistanceInKilometer(source.getLatitude(), source.getLongitude(),
                        target.getLatitude(), target.getLongitude());
            }
            if (weight.isInfinite() || weight.isNaN()) {
                log.error("SRM: NaN or Infinite double detected: weight = {}: {}", weight, sharedRide.toString());
                return false;
            }
            graph.setEdgeWeight(edge, weight);
        }
        return true;
    }

    /**
     * Run algorithm.
     *
     * @param startWaypoint staring vertex
     */
    private List<SrmWaypoint> calculateTSP(Graph<SrmWaypoint, DefaultWeightedEdge> graph, SrmWaypoint startWaypoint) {
        final var path = new ArrayList<>(new NearestNeighborHeuristicTSP<SrmWaypoint, DefaultWeightedEdge>(startWaypoint).getTour(graph).getVertexList());
        path.remove(path.size() - 1);
        return path;
    }

    private int getShortestAlgorithm(List<TspAlgorithm> tspAlgorithmList) {
        if (tspAlgorithmList.size() == 1) {
            return 0;
        }
        int index = 0;
        double shortestDistance = tspAlgorithmList.get(0).totalDistanceKm;
        for (int i = 1; i < tspAlgorithmList.size(); i++) {
            TspAlgorithm tspAlgorithm = tspAlgorithmList.get(i);
            if (tspAlgorithm.totalDistanceKm < shortestDistance) {
                shortestDistance = tspAlgorithm.totalDistanceKm;
                index = i;
            }
        }
        return index;
    }

    /**
     * Print graph.
     */
    private void printGraph(Graph<SrmWaypoint, DefaultWeightedEdge> graph) {
        if (log.isTraceEnabled()) {
            log.trace("SRM: printGraph: vertices = {}, edges = {}", graph.vertexSet().size(), graph.edgeSet().size());
            for (DefaultWeightedEdge edge : graph.edgeSet()) {
                log.trace("EDGE: {} - {} : {}",
                        graph.getEdgeSource(edge).getAddress(), graph.getEdgeTarget(edge).getAddress(), graph.getEdgeWeight(edge));
            }
        }
    }

    private void setOrderingIndexWaypoints(List<SrmWaypoint> list) {
        if (list.isEmpty()) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            SrmWaypoint waypoint = list.get(i);
            waypoint.setOrderingIndex(i);
        }
    }

    private void setOrderingIndexKpis(List<SrmRequestKpi> list) {
        if (list.isEmpty()) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            SrmRequestKpi kpi = list.get(i);
            if (kpi != null) {
                int orderingIndex = i;
                if (!kpi.isActive()) {
                    orderingIndex += 1000;
                }
                kpi.setOrderingIndex(orderingIndex);
            }
        }
    }

    private double processPath(List<SrmWaypoint> list, Graph<SrmWaypoint, DefaultWeightedEdge> graph) {
        if (list.isEmpty()) {
            return 0;
        }
        double totalWeight = 0;
        for (int i = 0; i < list.size(); i++) {
            SrmWaypoint waypoint = list.get(i);
            log.trace("{}: {}", i, waypoint.getAddress());
            if ((i + 1) < list.size()) {
                SrmWaypoint waypoint1 = list.get(i + 1);
                DefaultWeightedEdge edge = graph.getEdge(waypoint, waypoint1);
                double weight = graph.getEdgeWeight(edge);
                totalWeight += weight;
                log.trace("- {}", weight);
            }
        }
        log.trace("totalWeight: {}", totalWeight);
        return totalWeight;
    }

    private List<SrmWaypoint> mapSrmWaypointPostDtoListToSrmWaypointList(
            List<SrmWaypointPostDTO> waypointPostDTOList,
            SrmSharedRide sharedRide,
            UUID requestId
    ) {
        List<SrmWaypoint> waypointList = new ArrayList<>();
        for (int i = 0; i < waypointPostDTOList.size(); i++) {
            SrmWaypointPostDTO waypointPostDTO = waypointPostDTOList.get(i);
            SrmWaypoint waypoint = new SrmWaypoint();
            waypoint.setId(Optional.ofNullable(waypointPostDTO.getId()).orElseGet(UUID::randomUUID));
            EventType eventType = EventType.WAIT;
            if (i == 0) {
                eventType = EventType.BOARDING;
            }
            if (i == waypointPostDTOList.size() - 1) {
                eventType = EventType.UNBOARDING;
            }
            waypoint.setEventType(eventType);
            waypoint.setWaitingTime(waypointPostDTO.getWaitingTime());
            waypoint.setLatitude(waypointPostDTO.getLatitude());
            waypoint.setLongitude(waypointPostDTO.getLongitude());
            waypoint.setStartTime(null);
            waypoint.setEndTime(null);
            waypoint.setOrderingIndex(Integer.MAX_VALUE);
            waypoint.setOrgOrderingIndex(i);
            waypoint.setAddress(waypointPostDTO.getAddress());
            waypoint.setSharedRide(sharedRide);
            waypoint.setRequestKpiId(requestId);
            waypoint.setLoaderNumber(waypointPostDTO.getLoaderNumber() != null ? waypoint.getLoaderNumber() : 0);

            waypointList.add(waypoint);
        }
        return waypointList;
    }

    private double calculateDistanceInKilometer(double lat1, double lon1, double lat2, double lon2) {
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(lat1))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double result = (AVERAGE_RADIUS_OF_EARTH_KM * c);

        BigDecimal bd = BigDecimal.valueOf(result).setScale(3, RoundingMode.HALF_UP); // scale to meters
        return bd.doubleValue();
    }

    private void printPath(List<SrmWaypoint> list, String marker) {
        StringBuilder sb = new StringBuilder();
        if (list.isEmpty()) {
            return;
        }
        for (SrmWaypoint waypoint : list) {
            sb.append(waypoint.getAddress()).append(" (").append(waypoint.getGroupId()).append(") distanceFromPrevWaypoint = ")
                    .append(waypoint.getDistanceFromPrevWaypoint()).append(" * ");
        }
        log.trace("SRM: printPath: " + marker + ": " + sb);
    }

    /**
     * ************************************************************ Лишние отсюда и ниже
     */

    private static class TspAlgorithm {
        private final List<SrmWaypoint> waypointList;
        private final double totalDistanceKm;

        public TspAlgorithm(List<SrmWaypoint> waypointList, double totalDistanceKm) {
            this.waypointList = waypointList;
            this.totalDistanceKm = totalDistanceKm;
        }
    }

    private Integer uuidToInt(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        var l = uuid.getMostSignificantBits();
        return (int) Math.abs(l);
    }

    private double roundDouble(double value, int scale) {
        BigDecimal bd = BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private String getSharedRideShort(SrmSharedRide sharedRide) {
        StringBuilder sb = new StringBuilder();
        sb.append("SrmSharedRide id ");
        sb.append(sharedRide.getId());
        sb.append(" with requestKpis [");
        for (SrmRequestKpi requestKpi : sharedRide.getRequestKpiList()) {
            if (requestKpi != null) {
                sb.append("SrmRequestKpi id ");
                sb.append(requestKpi.getId());
                sb.append(" (org ");
                sb.append(requestKpi.getOrgRequestId());
                sb.append(");");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private String getSharedRideListShort(List<SrmSharedRide> sharedRideList) {
        StringBuilder sb = new StringBuilder();
        sb.append("sharedRideList [");
        for (SrmSharedRide sharedRide : sharedRideList) {
            sb.append(getSharedRideShort(sharedRide));
            sb.append(";");
        }
        sb.append("]");
        return sb.toString();
    }

    private String getMultipleRequestDTOShort(SrmMultipleRequestDTO multipleRequestDTO) {
        return "SrmMultipleRequestDTO id = %s with singleRequestDTOs [%s]"
                .formatted(multipleRequestDTO.getMultipleRequestId(), multipleRequestDTO.getRequestDTOList()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(SrmSingleRequestDTO::getRequestId)
                        .map(UUID::toString)
                        .collect(Collectors.joining(",")));
    }
}
