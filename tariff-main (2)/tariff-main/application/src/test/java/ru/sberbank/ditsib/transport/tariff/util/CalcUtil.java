package ru.sberbank.ditsib.transport.tariff.util;

import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.tariff.model.SuburbTripDataDTO;
import ru.sber.transport.tariff.model.TripDto;
import ru.sberbank.ditsib.transport.constants.RequestOptions;
import ru.sberbank.ditsib.transport.tariff.database.model.*;

import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.TimeZone;

@Slf4j
public class CalcUtil {
    private static final int TRAFFIC_JAM_SCORE_THRESHOLD = 7;
    
    public static long calculateTaxi(TaxiTariff tariff, TripDto tripData) {
        var basicCost = calculateTaxiBasicCost(tariff, tripData);
        var taxiSuburbCost = calculateTaxiSuburbCost(tariff.getSuburbTariffParams(), tripData.getSuburbTripData());
        var coefficient = calculateTotalTaxiCoefficient(tariff, tripData);
        log.debug("Taxi:\nBasic cost: {}\nSuburb cost: {}\nCoefficient: {}", basicCost, taxiSuburbCost, coefficient);
        return (long) ((basicCost + taxiSuburbCost) * coefficient);
    }
    
    public static long calculatePersonal(PersonalTariff tariff, TripDto tripDto) {
        return (long) ((calculatePersonalBasicCost(tariff, tripDto)
                        + calculatePersonalSuburbCost(tariff.getSuburbTariffParams(), tripDto.getSuburbTripData()))
                       * calculateTotalPersonalCoefficient(tariff, tripDto));
    }
    
    public static long calculateCarSharing(CarSharingTariff tariff, TripDto tripDto) {
        return (long) (calculateCarSharingBasicCost(tariff, tripDto) * calculateCarSharingCoefficient(tariff, tripDto));
    }
    
    private static long calculateBicycle(BicycleTariff tariff, TripDto tripDto) {
        return (long) (calculateBicycleBasicCost(tariff, tripDto) * calculateBicycleCoefficient(tariff, tripDto));
    }
    
    private static long calculateScooter(ScooterTariff tariff, TripDto tripDto) {
        return (long) (calculateScooterBasicCost(tariff, tripDto) * calculateScooterCoefficient(tariff, tripDto));
    }
    
    private static Double getTimeCoefficient(@NotNull TimedTariffParams params, @NotNull LocalDateTime time) {
        if (time.getDayOfWeek() == DayOfWeek.SATURDAY || time.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return params.getCoefDayOff();
        }
        LocalTime localTime = time.toLocalTime();
        if (localTime.compareTo(TimedTariffParams.MORNING_START) >= 0 &&
            localTime.compareTo(TimedTariffParams.NOON_START) < 0) {
            return params.getCoefWorkDayMorning();
        }
        if (localTime.compareTo(TimedTariffParams.NOON_START) >= 0 &&
            localTime.compareTo(TimedTariffParams.EVENING_START) < 0) {
            return params.getCoefWorkDayNoon();
        }
        if (localTime.compareTo(TimedTariffParams.EVENING_START) >= 0 &&
            localTime.compareTo(TimedTariffParams.NIGHT_START) < 0) {
            return params.getCoefWorkDayEvening();
        }
        if (localTime.compareTo(TimedTariffParams.NIGHT_START) >= 0 ||
            localTime.compareTo(TimedTariffParams.MORNING_START) < 0) {
            return params.getCoefWorkDayNight();
        }
        return 1d;
    }
    
    public static long calculateTaxiBasicCost(@NotNull TaxiTariff tariff, @NotNull TripDto tripData) {
        long minRideDistanceCost = tariff.getMinRideDistanceCost();
        
        var tripDistance = tripData.getDistance();
        var distanceIncluded = tariff.getDistanceIncluded();
        var rideCostPerKm = tariff.getRideCostPerKm();
        long distanceCost = (long) (minRideDistanceCost + Math.max(0, tripDistance - distanceIncluded) * rideCostPerKm);
        log.debug("""

                  Min distance cost: {}
                  Trip distance: {}
                  Distance included: {}
                  Cost per KM: {}
                   Distance cost: {}
                  """, minRideDistanceCost, tripDistance, distanceIncluded, rideCostPerKm, distanceCost);
        
        long minRideTimeCost = tariff.getMinRideTimeCost();
        var tripTimeMinutes = tripData.getTime().toMinutes();
        var timeIncluded = tariff.getTimeIncluded();
        var rideCostPerMin = tariff.getRideCostPerMin();
        long timeCost = (minRideTimeCost + Math.max(0, (tripTimeMinutes - timeIncluded) * rideCostPerMin));
        log.debug("\nMin time cost: {}\nTrip time: {}\nTime included: {}\nCost per min: {}\nTime cost: {}\n",
                  minRideTimeCost, tripTimeMinutes, timeIncluded, rideCostPerMin, timeCost);
        
        var waitingTimeMinutes = tripData.getWaitingTime().toMinutes();
        var freeWaitingTime = tariff.getFreeWaitingTime();
        var waitCostPerMin = tariff.getWaitCostPerMin();
        var waitCostPerMinIntermediate = tariff.getWaitCostPerMinIntermediate();
        var intermediateWaitingTime = tripData.getIntermediateWaitingTime().toMinutes();
        long waitingPrice = Math.max(0, waitingTimeMinutes - freeWaitingTime) *
                            waitCostPerMin
                            + waitCostPerMinIntermediate * intermediateWaitingTime;
        log.debug("""

                  Waiting time: {}
                  Free: {}
                  Wait cost per min: {}
                  Intermediate: {}
                  Intermediate waiting time: {}
                  Waiting price: {}
                  """, waitingTimeMinutes, freeWaitingTime, waitCostPerMin,
                  waitCostPerMinIntermediate, intermediateWaitingTime, waitingPrice);
        
        return distanceCost + timeCost + waitingPrice;
    }
    
    
    private static long calculateTaxiSuburbCost(
            @NotNull SuburbTariffParams suburbTariffParams,
            @NotNull SuburbTripDataDTO suburbTripData
                                        ) {
        long suburbServiceCost = (long) (
                suburbTripData.getSuburbServiceDistance() * suburbTariffParams.getSuburbServiceCostPerKm()
                + (long) suburbTripData.getSuburbServiceTime() * suburbTariffParams.getSuburbServiceCostPerMin());
        long suburbCost = calculateSuburbanTimeAndDistanceCost(suburbTariffParams, suburbTripData);
        return suburbServiceCost + suburbCost;
    }
    
    private static double calculateTotalTaxiCoefficient(@NotNull TaxiTariff taxiTariff, @NotNull TripDto tripDto) {
        double coefficient = 1d;
        LocalDateTime tripTime = getTripTime(tripDto);
        coefficient *= getTimeCoefficient(taxiTariff.getTimedTariffParams(), tripTime);
        if (tripDto.getTrafficJamScore() >= 7) {
            coefficient *= (1 + (taxiTariff.getCoefTraffic()) *
                                (tripDto.getTrafficJamScore() - TRAFFIC_JAM_SCORE_THRESHOLD));
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
    
    private static long calculatePersonalBasicCost(@NotNull PersonalTariff tariff, @NotNull TripDto tripData) {
        long minRideDistanceCost = tariff.getMinRideDistanceCost();
        long distanceCost = (long) (minRideDistanceCost+ Math.max(0,
                                                                  tripData.getDistance() -
                                                                  tariff.getDistanceIncluded()) *
                                                         tariff.getRideCostPerKm());
        long minRideTimeCost = tariff.getMinRideTimeCost();
        long timeCost = minRideTimeCost+ Math.max(0,
                                                  (tripData.getTime().toMinutes() - tariff.getTimeIncluded()) *
                                                  tariff.getRideCostPerMin());
        long waitingPrice = tripData.getWaitingTime() == null ? 0 :
                            tripData.getWaitingTime().toMinutes() * tariff.getWaitCostPerMin();
        return distanceCost + timeCost + waitingPrice;
    }
    
    //Расчет стоимости поездки по области и между регионами
    private static long calculatePersonalSuburbCost(
            @NotNull SuburbTariffParams suburbTariffParams,
            @NotNull SuburbTripDataDTO suburbTripData
                                            ) {
        return calculateSuburbanTimeAndDistanceCost(suburbTariffParams, suburbTripData);
    }
    
    private static double getEngineCoefficient(@NotNull EngineTariffParams engineTariffParams, @NotNull TripDto tripDto) {
        int engineVolume = tripDto.getEngineVolume();
        if (engineVolume < EngineTariffParams.BORDER_1_6) {
            return engineTariffParams.getCoefEngine1_6();
        }
        if (engineVolume < EngineTariffParams.BORDER_2_0) {
            return engineTariffParams.getCoefEngine1_6_to_2_0();
        }
        if (engineVolume < EngineTariffParams.BORDER_2_5) {
            return engineTariffParams.getCoefEngine2_0_to_2_5();
        }
        return 1d;
    }
    
    private static double calculateTotalPersonalCoefficient(@NotNull PersonalTariff tariff, @NotNull TripDto tripDto) {
        double coefficient = 1d;
        LocalDateTime tripTime = getTripTime(tripDto);
        if (tripTime.toLocalDate().compareTo(tariff.getSeasonStart()) >= 0 &&
            tripTime.toLocalDate().compareTo(tariff.getSeasonEnd()) < 0) {
            coefficient *= tariff.getSeasonalCoefficient();
        }
        coefficient *= getTimeCoefficient(tariff.getTimedTariffParams(), tripTime);
        if (tripDto.getTrafficJamScore() >= 7) {
            coefficient *= (1 + (tariff.getCoefTraffic()) *
                                (tripDto.getTrafficJamScore() - TRAFFIC_JAM_SCORE_THRESHOLD));
        }
        Set<RequestOptions> options = tripDto.getOptions();
        if (options.contains(RequestOptions.MATERIAL_ASSETS_TRANSPORTATION)) {
            coefficient *= tariff.getCoefMaterialAssets();
        }
        coefficient *= getEngineCoefficient(tariff.getEngineTariffParams(), tripDto);
        return coefficient;
    }
    
    private static long calculateSuburbanTimeAndDistanceCost(
            @NotNull SuburbTariffParams suburbTariffParams, @NotNull SuburbTripDataDTO suburbTripData
                                                     ) {
        return (long) (suburbTripData.getSuburbDistance() * suburbTariffParams.getCostPerKmSuburb()
                       + (long) suburbTripData.getSuburbTime() * suburbTariffParams.getCostPerMinSuburb()
                       + suburbTripData.getInterRegionDistance() * suburbTariffParams.getCostPerKmInterRegion()
                       + (long) suburbTripData.getInterRegionTime() * suburbTariffParams.getCostPerMinInterRegion());
    }
    
    private static long calculateCarSharingBasicCost(@NotNull CarSharingTariff tariff, @NotNull TripDto tripData) {
        return (long) (tariff.getRideCostPerKm() * tripData.getDistance() +
                       tariff.getRideCostPerMin() * tripData.getTime().toMinutes()
                       + tariff.getWaitCostPerMin() * tripData.getWaitingTime().toMinutes());
    }
    
    private static double calculateCarSharingCoefficient(@NotNull CarSharingTariff tariff, @NotNull TripDto tripData) {
        double coefficient = 1d;
        Set<RequestOptions> options = tripData.getOptions();
        if (options.contains(RequestOptions.CHILD_SEAT)) {
            coefficient *= tariff.getCoefChildSeat();
        }
        if (options.contains(RequestOptions.PET_TRANSPORTATION)) {
            coefficient *= tariff.getCoefPetTransport();
        }
        if (tripData.getTrafficJamScore() >= 7) {
            coefficient *= (1 + tariff.getCoefTraffic() * (tripData.getTrafficJamScore() - TRAFFIC_JAM_SCORE_THRESHOLD));
        }
        LocalDateTime tripTime = getTripTime(tripData);
        coefficient *= getTimeCoefficient(tariff.getTimedTariffParams(), tripTime);
        return coefficient;
    }
    
    private static long calculateBicycleBasicCost(@NotNull BicycleTariff tariff, @NotNull TripDto tripData) {
        return (long) (tariff.getRideCostPerKm() * tripData.getDistance() +
                       tariff.getRideCostPerMin() * tripData.getTime().toMinutes()) + tariff.getBookingCost();
    }
    
    private static double calculateBicycleCoefficient(@NotNull BicycleTariff tariff, @NotNull TripDto tripData) {
        double coefficient = 1d;
        LocalDateTime tripTime = getTripTime(tripData);
        coefficient *= getTimeCoefficient(tariff.getTimedTariffParams(), tripTime);
        return coefficient;
    }
    
    private static long calculateScooterBasicCost(@NotNull ScooterTariff tariff, @NotNull TripDto tripData) {
        return (long) (tariff.getRideCostPerKm() * tripData.getDistance() +
                       tariff.getRideCostPerMin() * tripData.getTime().toMinutes()) + tariff.getBookingCost();
    }
    
    private static double calculateScooterCoefficient(@NotNull ScooterTariff tariff, @NotNull TripDto tripData) {
        double coefficient = 1d;
        LocalDateTime tripTime = getTripTime(tripData);
        coefficient *= getTimeCoefficient(tariff.getTimedTariffParams(), tripTime);
        return coefficient;
    }
    
    private static LocalDateTime getTripTime(TripDto tripData) {
        if (tripData == null || tripData.getTripDate() == null) {
            return null;
        }
        String timeZone = tripData.getTimeZone();
        if (timeZone == null) {
            timeZone = "GMT+03";
        }
        return ZonedDateTime.of(tripData.getTripDate(), TimeZone.getTimeZone("UTC").toZoneId())
                            .withZoneSameInstant(TimeZone.getTimeZone(timeZone).toZoneId())
                            .toLocalDateTime();
    }
}
