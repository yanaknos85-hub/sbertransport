package ru.sberbank.ditsib.transport.reports.dao.spec;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;
import ru.sberbank.ditsib.transport.constants.ItinerantType;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;
import ru.sberbank.ditsib.transport.constants.PublicTransportType;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dto.TripPurposeDTO;
import ru.sberbank.ditsib.transport.reports.model.*;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRide;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRide_;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.CoopTaxiTrip_;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.SingleTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.SingleTaxiTrip_;

import jakarta.persistence.criteria.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RequestSearchSpecs {
    
    public static Specification<Request> createdBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from != null && to != null) {
                return cb.and(cb.between(root.get(Request_.creationTime), from, to));
            }
            if (from != null) {
                return cb.and(cb.greaterThan(root.get(Request_.creationTime), from));
            }
            return cb.and(cb.lessThanOrEqualTo(root.get(Request_.creationTime), to));
        };
    }
    
    public static Specification<Request> approvedBetween(LocalDateTime from, LocalDateTime to){
        return (root, query, cb) -> {
            if (from != null && to != null) {
                return cb.and(cb.between(root.get(Request_.approvalDate), from, to));
            }
            if (from != null) {
                return cb.and(cb.greaterThan(root.get(Request_.approvalDate), from));
            }
            return cb.and(cb.lessThanOrEqualTo(root.get(Request_.approvalDate), to));
        };
    }
    
    public static Specification<Request> orderPaymentFormationStartDateBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from != null && to != null) {
                return cb.and(cb.between(root.get(Request_.orderPaymentFormationStartDate), from, to));
            }
            if (from != null) {
                return cb.and(cb.greaterThan(root.get(Request_.orderPaymentFormationStartDate), from));
            }
            return cb.and(cb.lessThanOrEqualTo(root.get(Request_.orderPaymentFormationStartDate), to));
        };
    }
    
    public static Specification<Request> orderPaymentFormationFinishingDateBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from != null && to != null) {
                return cb.and(cb.between(root.get(Request_.orderPaymentFormationFinishingDate), from, to));
            }
            if (from != null) {
                return cb.and(cb.greaterThan(root.get(Request_.orderPaymentFormationFinishingDate), from));
            }
            return cb.and(cb.lessThanOrEqualTo(root.get(Request_.orderPaymentFormationFinishingDate), to));
        };
    }
    
    public static Specification<Request> desiredBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from != null && to != null) {
                return cb.and(cb.between(root.get(Request_.desiredDate), from, to));
            }
            if (from != null) {
                return cb.and(cb.greaterThan(root.get(Request_.desiredDate), from));
            }
            return cb.and(cb.lessThanOrEqualTo(root.get(Request_.desiredDate), to));
        };
    }
    
    public static Specification<Request> withHumanId(String requestHumanReadableId) {
        return (root, query, cb) -> cb.like(cb.lower(root.get(Request_.humanReadableId)),
                                            "%" + requestHumanReadableId.toLowerCase(Locale.ROOT) + "%");
    }
    
    public static Specification<Request> withTransportType(String transportType) {
        return (root, query, cb) -> cb.equal(root.get(Request_.transportType), transportType);
    }
    
    public static Specification<Request> withCoopTrip(Boolean coopTrip) {
        return (root, query, cb) -> cb.equal(root.get(Request_.coopTrip), coopTrip);
    }
    
    public static Specification<Request> inTripCompensationType(Collection<PublicCompensationType> compensationTypeSet) {
        return (root, query, cb) -> {
            var transportCompensation = root.join(Request_.transportCompensation);
            return transportCompensation.get(TransportCompensation_.COMPENSATION_TYPE).in(compensationTypeSet);
        };
    }
    
    public static Specification<Request> inPublicTransportType(Collection<PublicTransportType> publicTransportTypeSet) {
        return (root, query, cb) -> {
            var transportCompensation = root.join(Request_.transportCompensation);
            return transportCompensation.get(TransportCompensation_.transportType).in(publicTransportTypeSet);
        };
    }
    
    public static Specification<Request> inTripRequestStatus(Collection<TripRequestStatus> statuses) {
        return (root, query, cb) -> root.get(Request_.status).in(statuses.stream().map(TripRequestStatus::name)
                                                                         .collect(Collectors.toSet()));
    }
    
    public static Specification<Request> expectedDistanceBetween(Double start, Double end) {
        return (root, query, cb) -> {
            var expected = root.join(Request_.expected);
            return cb.between(expected.get(ExpectedData_.distance), start, end);
        };
    }
    
    public static Specification<Request> expectedCostBetween(Double start, Double end) {
        return (root, query, cb) -> {
            var expected = root.join(Request_.expected);
            return cb.between(expected.get(ExpectedData_.cost), start, end);
        };
    }
    
    public static Specification<Request> withSharedRideId(UUID sharedRideId) {
        return (root, query, cb) -> cb.equal(root.get(Request_.rideId), sharedRideId);
    }
    
    public static Specification<Request> inTripPurposeSet(Collection<TripPurposeDTO> tripPurposeSet) {
        var collect = tripPurposeSet.stream()
                                    .map(TripPurposeDTO::getId)
                                    .collect(Collectors.toSet());
        return (root, query, cb) -> {
            var purposes = root.join(Request_.purpose);
            return purposes.get(TripPurpose_.id).in(collect);
        };
    }
    
    public static Specification<Request> inRatingMarkSet(Collection<Integer> ratingMarkSet) {
        return (root, query, cb) -> {
            var reqRating = root.join(Request_.requestRating);
            return reqRating.get(RequestRating_.rating).in(ratingMarkSet);
        };
    }
    
    public static Specification<Request> inContractorSet(Collection<UUID> contractorSet) {
        return (root, query, cb) -> {
            var contractor = root.join(Request_.contractor);
            return contractor.get(Contractor_.id).in(contractorSet);
        };
    }
    
    public static Specification<Request> inEmployeeItinerantTypeSet(Collection<ItinerantType> itinerantTypeSet) {
        return (root, query, cb) -> {
            var passenger = root.join(Request_.passenger);
            return passenger.get(Employee_.itinerantType).in(itinerantTypeSet);
        };
    }
    
    public static Specification<Request> withPassengerCostCenter(String costCenter) {
        return (root, query, builder) -> {
            var passenger = root.join(Request_.passenger);
            return builder.equal(passenger.get(Employee_.costCenter), costCenter);
        };
    }
    
    public static Specification<Request> inPassengerDepartmentSet(Collection<UUID> passengerDepartmentSet) {
        return (root, query, cb) -> {
            var passenger = root.join(Request_.passenger);
            var department = passenger.join(Employee_.department);
            return department.get(Department_.id).in(passengerDepartmentSet);
        };
    }
    
    public static Specification<Request> inPassengerOrganizationSet(Collection<String> passengerOrganizationSet) {
        return (root, query, cb) -> {
            var passenger = root.join(Request_.passenger);
            var department = passenger.join(Employee_.department);
            return department.get(Department_.organizationId).in(passengerOrganizationSet.stream().map(UUID::fromString).collect(Collectors.toSet()));
        };
    }
    
    public static Specification<Request> inPassengerPositionSet(Collection<UUID> passengerPositionSet) {
        return (root, query, cb) -> {
            var passenger = root.join(Request_.passenger);
            var position = passenger.join(Employee_.position);
            return position.get(Position_.id).in(passengerPositionSet);
        };
    }
    
    public static Specification<Request> withPassengerFio(String passengerFio) {
        return (root, query, builder) -> {
            var passenger = root.join(Request_.passenger);
            var fullName = passengerFio.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
            
            switch (fullName.split(" ").length) {
                case 1 -> {
                    String search = "%".concat(fullName).concat("%");
                    var name = builder.like(builder.lower(passenger.get(Employee_.firstName)), search);
                    var lastName = builder.like(builder.lower(passenger.get(Employee_.lastName)), search);
                    var patronymic = builder.like(builder.lower(passenger.get(Employee_.patronymic)), search);
                    
                    return builder.or(name, lastName, patronymic);
                }
                case 2 -> {
                    var firstLast = builder.concat(passenger.get(Employee_.firstName), " ");
                    firstLast = builder.concat(firstLast, passenger.get(Employee_.lastName));
                    
                    var lastFirst = builder.concat(passenger.get(Employee_.lastName), " ");
                    lastFirst = builder.concat(lastFirst, passenger.get(Employee_.firstName));
                    
                    var lastPatronymic = builder.concat(passenger.get(Employee_.lastName), " ");
                    lastPatronymic = builder.concat(lastPatronymic, passenger.get(Employee_.patronymic));
                    
                    var firstPatronymic = builder.concat(passenger.get(Employee_.firstName), " ");
                    firstPatronymic = builder.concat(firstPatronymic, passenger.get(Employee_.patronymic));
                    
                    String search = "%".concat(fullName).concat("%");
                    var partIFPredicate = builder.like(builder.lower(firstLast), search);
                    var partFIPredicate = builder.like(builder.lower(lastFirst), search);
                    var partIOPredicate = builder.like(builder.lower(lastPatronymic), search);
                    var partOIPredicate = builder.like(builder.lower(firstPatronymic), search);
                    
                    return builder.or(partIFPredicate, partFIPredicate, partIOPredicate, partOIPredicate);
                }
                default -> {
                    var lastFirstPatronymic = builder.concat(passenger.get(Employee_.lastName), " ");
                    lastFirstPatronymic = builder.concat(lastFirstPatronymic, passenger.get(Employee_.firstName));
                    lastFirstPatronymic = builder.concat(lastFirstPatronymic, " ");
                    lastFirstPatronymic = builder.concat(lastFirstPatronymic, passenger.get(Employee_.patronymic));
                    lastFirstPatronymic = builder.lower(lastFirstPatronymic);
                    
                    var firstPatronymicLast = builder.concat(passenger.get(Employee_.firstName), " ");
                    firstPatronymicLast = builder.concat(firstPatronymicLast, passenger.get(Employee_.patronymic));
                    firstPatronymicLast = builder.concat(firstPatronymicLast, " ");
                    firstPatronymicLast = builder.concat(firstPatronymicLast, passenger.get(Employee_.lastName));
                    firstPatronymicLast = builder.lower(firstPatronymicLast);
                    
                    String search = "%".concat(fullName).concat("%");
                    return builder.or(builder.like(lastFirstPatronymic, search), builder.like(firstPatronymicLast, search));
                }
            }
        };
    }
    
    public static Specification<Request> withDepartureAddress(String departureAddress) {
        return (root, query, cb) -> {
            var waypoint = root.join(Request_.waypoints);
            var address = waypoint.join(Waypoint_.address);
            
            var predicate = cb.and(cb.equal(waypoint.get(Waypoint_.orderingIndex), 0));
            var addressSearchPredicate = getAddressSearchPredicate(departureAddress, cb, predicate, address);
            if (addressSearchPredicate != null) {
                predicate = cb.and(predicate, cb.and(addressSearchPredicate));
            }
            return predicate;
        };
    }
    
    public static Specification<Request> withDestinationAddress(String destinationAddress) {
        return (root, query, cb) -> {
            var waypoint = root.join(Request_.waypoints);
            var address = waypoint.join(Waypoint_.address);
            
            var predicate = cb.and(cb.greaterThan(waypoint.get(Waypoint_.orderingIndex), 0));
            
            var addressSearchPredicate = getAddressSearchPredicate(destinationAddress, cb, predicate, address);
            if (addressSearchPredicate != null) {
                return cb.and(predicate, cb.and(addressSearchPredicate));
            }
            return predicate;
        };
    }
    
    public static Specification<Request> withWaypointAddress(String waypointAddress) {
        return (root, query, cb) -> {
            Join<Request, Waypoint> waypoint = root.join(Request_.waypoints);
            Join<Waypoint, Address> address = waypoint.join(Waypoint_.address);
            
            Predicate predicate = cb.isNotNull(root.get(Request_.id));
            
            if (!StringUtils.isEmpty(waypointAddress)) {
                Predicate addressSearchPredicate =
                        getAddressSearchPredicate(waypointAddress, cb, predicate, address);
                if (addressSearchPredicate != null) {
                    predicate = cb.and(predicate, cb.and(addressSearchPredicate));
                }
            }
            
            return predicate;
        };
    }
    
    public static Specification<Request> departureDateBetween(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            Join<Request, SingleTaxiTrip> singleRide = root.join(Request_.singleTaxiTrip, JoinType.LEFT);
            Path<LocalDateTime> singlePath = singleRide.get(SingleTaxiTrip_.tripStartTime);
            Predicate singlePredicate = (to != null) ? cb.lessThanOrEqualTo(singlePath, to) : cb.greaterThan(singlePath, from);
            
            Join<Request, SharedRide> sharedRide = root.join(Request_.sharedRide, JoinType.LEFT);
            Join<SharedRide, CoopTaxiTrip> coopSharedRide = sharedRide.join(SharedRide_.coopTaxiTrip, JoinType.LEFT);
            Path<LocalDateTime> coopPath = coopSharedRide.get(CoopTaxiTrip_.tripStartTime);
            Predicate sharedPredicate = (to != null) ? cb.lessThanOrEqualTo(coopPath, to) : cb.greaterThan(coopPath, from);
            
            if ((to != null && from != null)) {
                singlePredicate = cb.between(singlePath, from, to);
                sharedPredicate = cb.between(coopPath, from, to);
            }
            
            return cb.or(sharedPredicate, singlePredicate);
        };
    }
    
    public static Specification<Request> withWaypointWaitTime(Duration start, Duration end) {
        return (root, query, cb) -> {
            Join<Request, SingleTaxiTrip> singleRide = root.join(Request_.singleTaxiTrip, JoinType.LEFT);
            Path<Duration> singlePath = singleRide.get(SingleTaxiTrip_.tripFactWaitTime);
            Predicate singlePredicate = (end != null) ? cb.between(singlePath, start, end) : cb.greaterThan(singlePath, start);
            
            Join<Request, SharedRide> sharedRide = root.join(Request_.sharedRide, JoinType.LEFT);
            Join<SharedRide, CoopTaxiTrip> coopSharedRide = sharedRide.join(SharedRide_.coopTaxiTrip, JoinType.LEFT);
            Path<Duration> factWaitTimePath = coopSharedRide.get(CoopTaxiTrip_.tripFactWaitTime);
            Predicate sharedPredicate = (end != null) ? cb.between(factWaitTimePath, start, end) : cb.greaterThan(factWaitTimePath, start);
            
            return cb.or(sharedPredicate, singlePredicate);
        };
    }
    
    public static Specification<Request> factCostBetween(Integer start, Integer end) {
        return (root, query, cb) -> {
            Join<Request, SingleTaxiTrip> singleRide = root.join(Request_.singleTaxiTrip, JoinType.LEFT);
            Path<Integer> singlePath = singleRide.get(SingleTaxiTrip_.tripFactPrice);
            Predicate singlePredicate = (end != null) ? cb.between(singlePath, start, end) : cb.greaterThanOrEqualTo(singlePath, start);
            
            Join<Request, SharedRide> sharedRide = root.join(Request_.sharedRide, JoinType.LEFT);
            Join<SharedRide, CoopTaxiTrip> coopSharedRide = sharedRide.join(SharedRide_.coopTaxiTrip, JoinType.LEFT);
            Path<Integer> coopPath = coopSharedRide.get(CoopTaxiTrip_.tripFactPrice);
            Predicate sharedPredicate = (end != null) ? cb.between(coopPath, start, end) : cb.greaterThanOrEqualTo(coopPath, start);
            
            return cb.or(sharedPredicate, singlePredicate);
        };
    }
    
    public static Specification<Request> factDistanceBetween(Double start, Double end) {
        return (root, query, cb) -> {
            Join<Request, SingleTaxiTrip> singleRide = root.join(Request_.singleTaxiTrip, JoinType.LEFT);
            Path<Double> singlePath = singleRide.get(SingleTaxiTrip_.tripFactDistance);
            Predicate singlePredicate = (end != null) ? cb.between(singlePath, start, end) : cb.greaterThanOrEqualTo(singlePath, start);
            
            Join<Request, SharedRide> sharedRide = root.join(Request_.sharedRide, JoinType.LEFT);
            Join<SharedRide, CoopTaxiTrip> coopSharedRide = sharedRide.join(SharedRide_.coopTaxiTrip, JoinType.LEFT);
            Path<Double> coopPath = coopSharedRide.get(CoopTaxiTrip_.tripFactDistance);
            Predicate sharedPredicate = (end != null) ? cb.between(coopPath, start, end) : cb.greaterThanOrEqualTo(coopPath, start);
            
            return cb.or(sharedPredicate, singlePredicate);
        };
    }
    
    public static Specification<Request> andSpec(@Nullable Specification<Request> sourceSpec, @Nullable Specification<Request> addedSpec) {
        if (sourceSpec != null) {
            return sourceSpec.and(addedSpec);
        }
        return addedSpec;
    }
    
    /**
     * Построение предиката адреса.
     *
     * @param address поисковая строка.
     * @param builder конструктор для построения предиката.
     * @param predicate исходный предикат.
     * @param waypointAddress узел запроса с информацией об адреса.
     *
     * @return построенный предикат для поиска.
     */
    @Nullable
    private static Predicate getAddressSearchPredicate(
            String address, CriteriaBuilder builder, Predicate predicate, Join<Waypoint, Address> waypointAddress
                                                      ) {
        String regex = "\\S{3,}";
        Matcher wordMatcher = Pattern.compile(regex).matcher(address);
        while (wordMatcher.find()) {
            String word = wordMatcher.group().toLowerCase(Locale.ROOT).trim();
            String search = "%".concat(word).concat("%");
            if (isLetter(word)) {
                Predicate city = builder.like(builder.lower(waypointAddress.get(Address_.city)), search);
                Predicate street = builder.like(builder.lower(waypointAddress.get(Address_.street)), search);
                
                predicate = builder.and(predicate, builder.and(builder.or(city, street)));
            } else {
                Predicate street = builder.like(builder.lower(waypointAddress.get(Address_.street)), search);
                Predicate house = builder.like(builder.lower(waypointAddress.get(Address_.house)), search);
                
                predicate = builder.and(predicate, builder.or(builder.or(house, street)));
            }
        }
        
        Predicate housePredicate = null;
        for (String word : address.split(regex)) {
            if (!isLetter(word.trim()) && !StringUtils.isBlank(word)) {
                String search = "%".concat(word.toLowerCase(Locale.ROOT).trim()).concat("%");
                Predicate house = builder.like(builder.lower(waypointAddress.get(Address_.house)), search);
                Predicate building = builder.like(builder.lower(waypointAddress.get(Address_.building)), search);
                Predicate structure = builder.like(builder.lower(waypointAddress.get(Address_.structure)), search);
                
                if (housePredicate == null) {
                    housePredicate = builder.or(house, building, structure);
                } else {
                    housePredicate = builder.or(housePredicate, builder.or(house, building, structure));
                }
            }
        }
        
        if (housePredicate != null) {
            predicate = builder.and(predicate, housePredicate);
        }
        return predicate;
    }
    
    public static boolean isLetter(String variable) {
        if (StringUtils.isEmpty(variable)) {
            return false;
        }
        for (char ch : variable.toCharArray()) {
            if (!Character.isLetter(ch)) {
                return false;
            }
        }
        return true;
    }
    
}
