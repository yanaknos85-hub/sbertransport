package ru.sberbank.ditsib.transport.request.service.impl.search;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department_;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee_;
import ru.sberbank.ditsib.transport.request.dto.RequestSearchDTO;
import ru.sberbank.ditsib.transport.request.dto.TripPurposeDTO;
import ru.sberbank.ditsib.transport.request.service.search.RequestSearchSpecService;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public abstract class AbstractRequestSearchSpecService<T extends Request> implements RequestSearchSpecService<T> {
    
    @Override
    public <R extends RequestSearchDTO> Specification<T> getSpec(R requestSearchDTO) {
        return (root, query, builder) -> {
            query.distinct(true);
            
            Predicate predicate = builder.isNotNull(root.get(Request_.id));
            var passenger = root.join(Request_.passenger);
            
            if (requestSearchDTO.getTransportTypeEnum() != null) {
                predicate = builder.and(predicate, builder.equal(root.get(Request_.TRANSPORT_TYPE),
                                                                 requestSearchDTO.getTransportTypeEnum()));
            }
            if (requestSearchDTO.getTransportTypeSet() != null) {
                predicate = builder.and(predicate, root.get(Request_.TRANSPORT_TYPE).in(requestSearchDTO.getTransportTypeSet()));
            }
            
            if (requestSearchDTO.getRequestId() != null) {
                predicate = builder.and(predicate, builder.equal(root.get(Request_.id), requestSearchDTO.getRequestId()));
            }
            
            if (requestSearchDTO.getHumanReadableId() != null) {
                predicate = builder.and(predicate, builder.like(root.get(Request_.humanReadableId), requestSearchDTO.getHumanReadableId() + '%'));
            }
            
            if (requestSearchDTO.getRequestStatusSet() != null && !requestSearchDTO.getRequestStatusSet().isEmpty()) {
                Set<TripRequestStatus> collect = requestSearchDTO.getRequestStatusSet().stream()
                                                                 .map(TripRequestStatus::getFromString)
                                                                 .filter(Optional::isPresent)
                                                                 .map(Optional::get)
                                                                 .collect(Collectors.toSet());
                
                predicate = builder.and(predicate, builder.and(root.get(Request_.status).in(collect)));
            }
            
            var creationDate = requestSearchDTO.getCreationDate();
            if (creationDate != null) {
                if (creationDate.getStart() == null) {
                    creationDate.setStart(LocalDateTime.of(LocalDateTime.now().minusDays(30).toLocalDate(),
                                                           LocalTime.MIDNIGHT));
                }
                if (creationDate.getEnd() != null) {
                    predicate = builder.and(predicate,
                                            builder.between(root.get(Request_.creationTime),
                                                            creationDate.getStart(),
                                                            creationDate.getEnd()));
                } else {
                    predicate = builder.and(predicate,
                                            builder.greaterThan(root.get(Request_.creationTime),
                                                                creationDate.getStart()));
                }
            }
            
            var desiredDate = requestSearchDTO.getDesiredDate();
            if (desiredDate != null) {
                if (desiredDate.getEnd() != null) {
                    predicate = builder.and(predicate,
                                            builder.between(root.get(Request_.desiredDate),
                                                            desiredDate.getStart(),
                                                            desiredDate.getEnd()));
                } else {
                    predicate = builder.and(predicate,
                                            builder.greaterThan(root.get(Request_.desiredDate),
                                                                desiredDate.getStart()));
                }
            }
            
            if (requestSearchDTO.getPurposeSet() != null && !requestSearchDTO.getPurposeSet().isEmpty()) {
                List<UUID> collect = requestSearchDTO.getPurposeSet().stream()
                                                     .map(TripPurposeDTO::getId)
                        .toList();
                
                predicate = builder.and(predicate,
                                        builder.and(root.get(Request_.purpose).get(TripPurpose_.id).in(collect)));
            }
            
            if (requestSearchDTO.getExpectedCost() != null) {
                requestSearchDTO.getExpectedCost().prepareQuery();
                
                predicate = builder.and(predicate,
                                        builder.between(root.get(Request_.expected).get(ExpectedData_.cost),
                                                        requestSearchDTO.getExpectedCost().getStart(),
                                                        requestSearchDTO.getExpectedCost().getEnd()));
            }
            
            if (requestSearchDTO.getExpectedDistance() != null) {
                requestSearchDTO.getExpectedDistance().prepareQuery();
                
                predicate = builder.and(predicate,
                                        builder.between(root.get(Request_.expected).get(ExpectedData_.distance),
                                                        requestSearchDTO.getExpectedDistance().getStart(),
                                                        requestSearchDTO.getExpectedDistance().getEnd()));
            }
            
            if (!StringUtils.isEmpty(requestSearchDTO.getEmployeeNumber())) {
                predicate = builder.and(predicate, builder.and(builder.equal(
                        builder.lower(passenger.get(Employee_.personnelNumber)),
                        requestSearchDTO.getEmployeeNumber().toLowerCase(Locale.ROOT))));
            }
            
            if (requestSearchDTO.getPassengerId() != null) {
                predicate = builder.and(predicate, builder.and(builder.equal(passenger.get(Employee_.id),
                                                                             requestSearchDTO.getPassengerId())));
            }
            
            var fullName = requestSearchDTO.getEmployeeFIO();
            if (!StringUtils.isEmpty(fullName)) {
                fullName = fullName.toLowerCase(Locale.ROOT);
                switch (fullName.split(" ").length) {
                    case 1: {
                        String search = "%".concat(fullName).concat("%");
                        var name = builder.like(builder.lower(passenger.get(Employee_.firstName)), search);
                        var lastName = builder.like(builder.lower(passenger.get(Employee_.lastName)), search);
                        var patronymic = builder.like(builder.lower(passenger.get(Employee_.patronymic)), search);
                        
                        predicate = builder.and(predicate, builder.or(name, lastName, patronymic));
                        
                        break;
                    }
                    case 2: {
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
                        
                        predicate = builder.and(predicate,
                                                builder.or(partIFPredicate, partFIPredicate, partIOPredicate, partOIPredicate));
                        
                        break;
                    }
                    
                    default: {
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
                        predicate = builder.and(predicate, builder.or(builder.like(lastFirstPatronymic, search),
                                                                      builder.like(firstPatronymicLast, search)));
                    }
                }
            }
            
            if (requestSearchDTO.getEmployeePositionSet() != null &&
                !requestSearchDTO.getEmployeePositionSet().isEmpty()) {
                predicate = builder.and(predicate,
                                        builder.and(passenger.get(Employee_.positionId)
                                                             .in(requestSearchDTO.getEmployeePositionSet())));
            }
            
            if (requestSearchDTO.getEmployeeDepartmentSet() != null &&
                !requestSearchDTO.getEmployeeDepartmentSet().isEmpty()) {
                predicate = builder.and(predicate,
                                        builder.and(passenger.get(Employee_.department).get(Department_.id)
                                                             .in(requestSearchDTO.getEmployeeDepartmentSet())));
            }
            
            if (requestSearchDTO.isJoinWaypoint()) {
                Join<T, Waypoint> waypoint = root.join(Request_.waypoints);
                Join<Waypoint, Address> address = waypoint.join(Waypoint_.address);
                
                if (!StringUtils.isEmpty(requestSearchDTO.getDepartureAddress())) {
                    predicate = builder.and(predicate, builder.and(builder.equal(waypoint.get(Waypoint_.orderingIndex), 0)));
                    
                    Predicate addressSearchPredicate = getAddressSearchPredicate(requestSearchDTO.getDepartureAddress(), builder, predicate, address);
                    if (addressSearchPredicate != null) {
                        predicate = builder.and(predicate, builder.and(addressSearchPredicate));
                    }
                }
                
                if (!StringUtils.isEmpty(requestSearchDTO.getDestinationAddress())) {
                    predicate = builder.and(predicate, builder.and(builder.greaterThan(waypoint.get(Waypoint_.orderingIndex), 0)));
                    
                    Predicate addressSearchPredicate =
                            getAddressSearchPredicate(requestSearchDTO.getDestinationAddress(), builder, predicate, address);
                    if (addressSearchPredicate != null) {
                        predicate = builder.and(predicate, builder.and(addressSearchPredicate));
                    }
                }
                
                if (!StringUtils.isEmpty(requestSearchDTO.getWaypointAddress())) {
                    Predicate addressSearchPredicate = getAddressSearchPredicate(requestSearchDTO.getWaypointAddress(), builder, predicate, address);
                    if (addressSearchPredicate != null) {
                        predicate = builder.and(predicate, builder.and(addressSearchPredicate));
                    }
                }
                RequestSearchDTO.DurationRange waitTime = requestSearchDTO.getWaypointWaitTime();
                if (waitTime != null) {
                    if (waitTime.getEnd() != null) {
                        predicate = builder.and(predicate,
                                                builder.between(waypoint.get(Waypoint_.waitTime),
                                                                waitTime.getStart(),
                                                                waitTime.getEnd()));
                    } else {
                        predicate = builder.and(predicate,
                                                builder.greaterThan(waypoint.get(Waypoint_.waitTime),
                                                                    waitTime.getStart()));
                    }
                }
            }
            
            if (requestSearchDTO.getTerminalStatus() != null) {
                predicate = builder.and(predicate,
                                        builder.and(root.get(Request_.status)
                                                        .in(TripRequestStatus.getTerminalStatus(
                                                                requestSearchDTO.getTerminalStatus()))));
            }
            
            return predicate;
        };
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
    private Predicate getAddressSearchPredicate(
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
    
    public boolean isLetter(String variable) {
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
