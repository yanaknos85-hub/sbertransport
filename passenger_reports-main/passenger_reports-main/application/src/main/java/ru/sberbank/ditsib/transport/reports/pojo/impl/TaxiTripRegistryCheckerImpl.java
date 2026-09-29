package ru.sberbank.ditsib.transport.reports.pojo.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TripType;
import ru.sberbank.ditsib.transport.reports.dao.CoopTaxiTripRepository;
import ru.sberbank.ditsib.transport.reports.dao.RequestRepository;
import ru.sberbank.ditsib.transport.reports.dao.SingleTaxiTripRepository;
import ru.sberbank.ditsib.transport.reports.dao.TaxiTripRepository;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.Waypoint;
import ru.sberbank.ditsib.transport.reports.model.excel.CalculatedTripStatus;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistry;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistryString;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistryStringTemplate;
import ru.sberbank.ditsib.transport.reports.model.tariff.TaxiTariff;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.SingleTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.TaxiTrip;
import ru.sberbank.ditsib.transport.reports.pojo.TaxiTripRegistryChecker;

import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Реализация класса для проверки Реестра поездок на такси от контрагента
 * В ходе проверок вычисляется:
 * 1) существует ли такая поездка (сопоставление по taxi_id)
 * 2) забирается из АС тип поездки - личная или совместная
 * 3) вычисляется статус поездки - завершена, отклонена или ошибка в строке реестра
 * Если поездка не существует - все проверки false, все статусы null, итоговый статус false
 * Для отклоненных поездок - только проверки 0-3, остальные всегда true
 * Для ошибочных строк - только проверки 0-3, остальные всегда false
 * Для завершенных поездок - все проверки
 */
@Component
@RequiredArgsConstructor
public class TaxiTripRegistryCheckerImpl implements TaxiTripRegistryChecker {
    
    private final TaxiTripRepository tripRepository;
    private final CoopTaxiTripRepository coopTripRepository;
    private final SingleTaxiTripRepository singleTripRepository;
    private final RequestRepository requestRepository;
    
    private CalculatedTripStatus tripStatus;
    private boolean isTripExistsByTaxiId;
    private boolean isCoopTrip;
    
    @Transactional
    @Override
    public void checkStrings(TaxiTripRegistry registry) {
        List<TaxiTripRegistryString> registryStrings = registry.getRegistryStrings();
        
        for (TaxiTripRegistryString str : registryStrings) {
            //установка глобальных переменных по дефолту
            tripStatus = null;
            isTripExistsByTaxiId = false;
            isCoopTrip = false;
            //установка значений проверок в false
            str.setValidTaxiId0(false);
            str.setValidTripStatus1(false);
            str.setValidTripDate2(false);
            str.setValidCancelledTripCost3(false);
            str.setValidCalcDistance4(false);
            str.setValidFactDistance4a(false);
            str.setValidTariff5(false);
            str.setValidCalcCost6(false);
            str.setValidFactCost7(false);
            str.setValidWaitTime8(false);
            
            String taxiId = str.getParsedString().getTaxiTripId();
            TaxiTrip trip = null;
            if (taxiId != null) {
                trip = findTaxiTripByTaxiId(taxiId);
            }
            //Если вернулась поездка, осуществить проверки
            if (trip != null) {
                isTripExistsByTaxiId = true;
                //Забрать тип поездки, записать в строку реестра
                if (trip.getTripType().equals(TripType.COOP)) {
                    isCoopTrip = true;
                    str.setCalculatedTripType(TripType.COOP);
                    trip = findCoopTripById(trip.getId());
                } else {
                    str.setCalculatedTripType(TripType.SINGLE);
                    trip = findSingleTripById(trip.getId());
                }
                calculateTripStatusFromString(str);
                checkTaxiId0(str);
                checkTripStatus1(str);
                checkTripDate2(str);
                checkCancelledTripCost3(str);
                
                if (tripStatus.equals(CalculatedTripStatus.CANCELED)) {
                    str.setValidCalcDistance4(true);
                    str.setValidFactDistance4a(true);
                    str.setValidTariff5(true);
                    str.setValidCalcCost6(true);
                    str.setValidFactCost7(true);
                    str.setValidWaitTime8(true);
                } else if (tripStatus.equals(CalculatedTripStatus.ERROR_STRING)) {
                    str.setValidCalcDistance4(false);
                    str.setValidFactDistance4a(false);
                    str.setValidTariff5(false);
                    str.setValidCalcCost6(false);
                    str.setValidFactCost7(false);
                    str.setValidWaitTime8(false);
                } else {
                    checkCalcDistance4(str, trip);
                    checkFactDistance4a(str, trip);
                    checkTariff5(str, trip);
                    checkCalcCost6(str, trip);
                    checkFactCost7(str, trip);
                    checkWaitTime8(str, trip);
                }
            }
        }
        setRegistryStatusByCheckingResults(registry);
    }
    
    /**
     * Вычислить статус поездки по строке реестра
     * @param str строка реестра
     */
    private void calculateTripStatusFromString(TaxiTripRegistryString str) {
        Double factDistanceKm = str.getParsedString().getFactDistanceKm();
        Integer factWaitTimeMin = str.getParsedString().getFactWaitTimeMin();
        Double factSumRub = str.getParsedString().getFactSumRub();
        if (factDistanceKm == null || factWaitTimeMin == null) {
            tripStatus = CalculatedTripStatus.ERROR_STRING;
        } else if (factDistanceKm == 0 && factWaitTimeMin == 0 && (factSumRub == null || factSumRub == 0)) {
            tripStatus = CalculatedTripStatus.CANCELED;
        } else if (factDistanceKm > 0 && factWaitTimeMin >= 0 && (factSumRub != null && factSumRub > 0)) {
            tripStatus = CalculatedTripStatus.DONE;
        } else {
            tripStatus = CalculatedTripStatus.ERROR_STRING;
        }
        str.setCalculatedTripStatus(tripStatus);
    }
    
    /**
     * Вернуть CoopTaxiTrip по taxiId из реестра контрагента или null
     * @param id ID поездки из БД
     * @return CoopTaxiTrip или null, если поездка не найдена
     */
    private CoopTaxiTrip findCoopTripById(UUID id) {
        return coopTripRepository.findById(id).orElse(null);
    }
    
    /**
     * Вернуть SingleTaxiTrip по taxiId из реестра контрагента или null
     * @param id ID поездки из БД
     * @return SingleTaxiTrip или null, если поездка не найдена
     */
    private SingleTaxiTrip findSingleTripById(UUID id) {
        return singleTripRepository.findById(id).orElse(null);
    }
    
    /**
     * Вернуть TaxiTrip по taxiId из реестра контрагента
     * @param taxiId ID поездки из реестра
     * @return TaxiTrip или null, если поездка не найдена
     */
    private TaxiTrip findTaxiTripByTaxiId(String taxiId) {
        return tripRepository.findByTaxiId(taxiId).orElse(null);
    }
    
    /**
     * Проверка 0 - Проверка совпадения ID поездки
     * @param str строка реестра
     */
    private void checkTaxiId0(TaxiTripRegistryString str) {
        if (isTripExistsByTaxiId) str.setValidTaxiId0(true);
    }
    
    //todo пока копирует результаты проверки 3. Как после добавления комментария в стори
    /**
     * Проверка 1 - Проверка статуса поездки
     * @param str строка реестра
     */
    private void checkTripStatus1(TaxiTripRegistryString str) {
        if (!tripStatus.equals(CalculatedTripStatus.ERROR_STRING)) {
            str.setValidTripStatus1(true);
        }
    }
    
    //todo проверка не выполняется до тех пор, пока:
    // а) либо не будет передаваться часовой пояс в реестре
    // б) либо не будет заполняться часовой пояс в модуле контрагента
    /**
     * Проверка 2 - Проверка даты поездки
     * @param str строка реестра
     */
    private void checkTripDate2(TaxiTripRegistryString str) {
        str.setValidTripDate2(true);
    }
    
    /**
     * Проверка 3 - Проверка стоимость = 0 для отмененной поездки
     * @param str строка реестра
     */
    private void checkCancelledTripCost3(TaxiTripRegistryString str) {
        if (!tripStatus.equals(CalculatedTripStatus.ERROR_STRING)) {
            str.setValidCancelledTripCost3(true);
        }
    }
    
    /**
     * Проверка 4 - Сравнение протяженности маршрута с расчетной
     * @param str строка реестра
     * @param trip CoopTaxiTrip или SingleTaxiTrip
     */
    private void checkCalcDistance4(TaxiTripRegistryString str, TaxiTrip trip) {
        Double distanceKmFromRegistry = str.getParsedString().getFactDistanceKm();
        if (distanceKmFromRegistry == null) {
            return;
        }
        Integer maxDelta = trip.getTariff().getContractorDeviationParams().getMaxDiffComputedDistancePercent();
        Double calcDistanceKmFromAs;
        if (isCoopTrip) {
            calcDistanceKmFromAs = ((CoopTaxiTrip) trip).getSharedRide().getKpi().getTotalDistanceKm();
        } else {
            calcDistanceKmFromAs = ((SingleTaxiTrip) trip).getRequest().getExpected().getDistance();
        }
        Double calcDelta = calculateDeltaInPercents(distanceKmFromRegistry, calcDistanceKmFromAs);
        if (calcDelta.isNaN()) {
            return;
        }
        if (maxDelta >= calcDelta) {
            str.setValidCalcDistance4(true);
        }
    }
    
    /**
     * Проверка 4a - Сравнение протяженности маршрута с фактической
     * @param str строка реестра
     * @param trip TaxiTrip
     */
    private void checkFactDistance4a(TaxiTripRegistryString str, TaxiTrip trip) {
        Double distanceKmFromRegistry = str.getParsedString().getFactDistanceKm();
        if (distanceKmFromRegistry == null) {
            return;
        }
        Integer maxDelta = trip.getTariff().getContractorDeviationParams().getMaxDiffFactDistancePercent();
        Double factDistanceKmFromAs = trip.getTripFactDistance();
        Double calcDelta = calculateDeltaInPercents(distanceKmFromRegistry, factDistanceKmFromAs);
        if (calcDelta.isNaN()) {
            return;
        }
        if (maxDelta >= calcDelta) {
            str.setValidFactDistance4a(true);
        }
    }
    
    /**
     * Проверка 5 - Проверка цифр тарифа с точностью до +- одной копейки (без дельт)
     * @param str строка реестра
     */
    private void checkTariff5(TaxiTripRegistryString str, TaxiTrip trip) {
        //данные из реестра
        TaxiTripRegistryStringTemplate parsedString = str.getParsedString();
        Double minCostRubFromRegistry = parsedString.getFactMinimalCostRub();
        Double rubPerKmFromRegistry = parsedString.getFactPriceRub();
        Double waitCostRubPerMin = parsedString.getFactWaitCostRub();
        if (minCostRubFromRegistry == null || rubPerKmFromRegistry == null || waitCostRubPerMin == null) {
            return;
        }
        Integer delta = 1;
        Integer minCostKopFromRegistry = convertRubToKopWithTrim(minCostRubFromRegistry);
        Integer rideCostKopPerKmFromRegistry = convertRubToKopWithTrim(rubPerKmFromRegistry);
        Integer waitCostKopPerMinFromRegistry = convertRubToKopWithTrim(waitCostRubPerMin);
        
        //данные из АС
        TaxiTariff tariff = trip.getTariff();
        Integer rideCostKopPerKmFromAS = tariff.getRideCostPerKm();
        Integer waitCostKopPerMinFromAS = tariff.getWaitCostPerMin();
    
        //выбрать, расчет по времени или по километражу
        Integer minDistanceCost = tariff.getMinRideDistanceCost();
        Integer minTimeCost = tariff.getMinRideTimeCost();
        Integer resultMinCostKopFromAS;
        
        if ((minDistanceCost == null || minDistanceCost == 0) && (minTimeCost != null && minTimeCost != 0)) {
            resultMinCostKopFromAS = minTimeCost;
        } else if ((minTimeCost == null || minTimeCost == 0) && (minDistanceCost != null && minDistanceCost != 0)) {
            resultMinCostKopFromAS = minDistanceCost;
        }
        //todo случаи ниже не описаны аналитиком, и нет ответа от бизнеса
        else if ((minDistanceCost != null && minDistanceCost != 0) && (minTimeCost != null && minTimeCost != 0)) {
            resultMinCostKopFromAS = (minDistanceCost > minTimeCost) ? minDistanceCost : minTimeCost;
        } else {
            resultMinCostKopFromAS = 0;
        }
    
        if (delta >= calculateDelta(rideCostKopPerKmFromRegistry, rideCostKopPerKmFromAS) &&
            delta >= calculateDelta(waitCostKopPerMinFromRegistry, waitCostKopPerMinFromAS) &&
            delta >= calculateDelta(minCostKopFromRegistry, resultMinCostKopFromAS)) {
            str.setValidTariff5(true);
        }
    }
    
    /**
     * Проверка 6 - Сравнение стоимости поездки с расчетной
     * @param str строка реестра
     */
    private void checkCalcCost6(TaxiTripRegistryString str, TaxiTrip trip) {
        Double factSumRubFromRegistry = str.getParsedString().getFactSumRub();
        if (factSumRubFromRegistry == null) {
            return;
        }
        Integer maxDelta = trip.getTariff().getContractorDeviationParams().getMaxDiffComputedCostPercent();
        Double calcCostRubFromAs;
        if (isCoopTrip) {
            calcCostRubFromAs = ((CoopTaxiTrip) trip).getSharedRide().getKpi().getTotalCost();
        } else {
            calcCostRubFromAs = ((SingleTaxiTrip) trip).getRequest().getExpected().getCost();
        }
        Integer factSumKopFromRegistry = convertRubToKopWithTrim(factSumRubFromRegistry);
        Integer calcCostKopFromAs = convertRubToKopWithTrim(calcCostRubFromAs);
    
        Double calcDelta = calculateDeltaInPercents(factSumKopFromRegistry, calcCostKopFromAs);
        if (calcDelta.isNaN()) {
            return;
        }
        if (maxDelta >= calcDelta) {
            str.setValidCalcCost6(true);
        }
    }
    
    /**
     * Проверка 7 - Сравнение стоимости поездки с фактической
     * @param str строка реестра
     */
    private void checkFactCost7(TaxiTripRegistryString str, TaxiTrip trip) {
        Double factSumRubFromRegistry = str.getParsedString().getFactSumRub();
        if (factSumRubFromRegistry == null) {
            return;
        }
        Integer maxDelta = trip.getTariff().getContractorDeviationParams().getMaxDiffContractorCostPercent();
        Integer factSumKopFromAS = trip.getTripFactPrice();
        Integer factSumKopFromRegistry = convertRubToKopWithTrim(factSumRubFromRegistry);
    
        Double calcDelta = calculateDeltaInPercents(factSumKopFromRegistry, factSumKopFromAS);
        if (calcDelta.isNaN()) {
            return;
        }
        if (maxDelta >= calcDelta) {
            str.setValidFactCost7(true);
        }
    }
    
    /**
     * Проверка 8 - Сравнение времени ожидания c фактическим
     * @param str строка реестра
     */
    private void checkWaitTime8(TaxiTripRegistryString str, TaxiTrip trip) {
        //из реестра
        Integer factWaitTimeMinFromRegistry = str.getParsedString().getFactWaitTimeMin();
        if (factWaitTimeMinFromRegistry == null) {
            return;
        }
        Integer maxDelta = trip.getTariff().getContractorDeviationParams().getMaxDiffComputedWaitingPercent();
        
        //расчет по АС
        Integer factWaitTimeMinFromAS = (int) trip.getTripFactWaitTime().toMinutes();
        List<Waypoint> waypoints = new ArrayList<>();
        
        if (isCoopTrip) {
            CoopTaxiTrip coopTaxiTrip = (CoopTaxiTrip) trip;
            if (coopTaxiTrip.getSharedRide() != null) {
                //TODO: coop trip should use waypoints of SharedRide
                List<Request> magentaSharedRequest = requestRepository.findByRideId(coopTaxiTrip.getSharedRide().getId());
                if (magentaSharedRequest.size() > 0) {
                    waypoints = magentaSharedRequest.get(0).getWaypoints();
                }
            }
        }
        if (!isCoopTrip) {
            waypoints = ((SingleTaxiTrip) trip).getRequest().getWaypoints();
        }
        //если точек пути больше двух, приплюсовать время в промежуточных точках
        if (waypoints != null && waypoints.size() > 2) {
            for (Waypoint waypoint : waypoints) {
                if (waypoint.getOrderingIndex() == 0 || waypoint.getOrderingIndex() == waypoints.size() - 1) continue;
                factWaitTimeMinFromAS += (int) waypoint.getWaitTime().toMinutes();
            }
        }
    
        Double calcDelta = calculateDeltaInPercents(factWaitTimeMinFromRegistry, factWaitTimeMinFromAS);
        if (calcDelta.isNaN()) {
            return;
        }
        if (maxDelta >= calcDelta) {
            str.setValidWaitTime8(true);
        }
    }
    
    /**
     * Установить итоговый статус реестра по результатам проверок строк
     * @param registry реестр
     */
    private void setRegistryStatusByCheckingResults(TaxiTripRegistry registry) {
        boolean isValid = true;
        for (TaxiTripRegistryString str : registry.getRegistryStrings()) {
            if (!str.getValidTaxiId0() || !str.getValidTripStatus1() || !str.getValidTripDate2() ||
                !str.getValidCancelledTripCost3() || !str.getValidCalcDistance4() ||
                !str.getValidFactDistance4a() || !str.getValidTariff5() || !str.getValidCalcCost6() ||
                !str.getValidFactCost7() || !str.getValidWaitTime8()
            ) {
                isValid = false;
                break;
            }
        }
        registry.setValid(isValid);
    }
    
    /**
     * Вычислить дельту двух чисел, в процентах
     * @param fromRegistry число 1
     * @param fromAS число 2
     * @return дельта (по модулю)
     */
    private Double calculateDeltaInPercents(Number fromRegistry, Number fromAS) {
        if (fromAS.doubleValue() == 0) {
            if (fromRegistry.doubleValue() == 0) {
                return 0d;
            } else {
                return Double.NaN;
            }
        }
        return Math.abs(((fromRegistry.doubleValue() - fromAS.doubleValue()) * 100)/fromAS.doubleValue());
    }
    
    /**
     * Вычислить разницу двух целых чисел
     * @param fromRegistry число 1
     * @param fromAS число 2
     * @return дельта (по модулю)
     */
    private Integer calculateDelta(Integer fromRegistry, Integer fromAS) {
        return Math.abs(fromRegistry - fromAS);
    }
    
    /**
     * Перевод рублей в копейки с обрезкой до двух знаков и округлением
     * @param cost стоимость в рублях (число знаков после запятой м.б. более двух)
     * @return округленная стоимость в копейках
     */
    private Integer convertRubToKopWithTrim (Double cost) {
        return (int) Math.round(cost * 100);
    }
}
