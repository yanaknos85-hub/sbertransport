package ru.sberbank.ditsib.transport.reports.service.impl.mapping;

import lombok.experimental.UtilityClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryCheckDTO;
import ru.sberbank.ditsib.transport.reports.model.Department;
import ru.sberbank.ditsib.transport.reports.model.Employee;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.Waypoint;
import ru.sberbank.ditsib.transport.reports.model.magenta.OrderKpi;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.TaxiTrip;
import ru.sberbank.ditsib.transport.reports.utils.Colors;
import ru.sberbank.ditsib.transport.reports.utils.Function4;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static ru.sberbank.ditsib.transport.reports.service.impl.mapping.AllColumnNames.*;

@UtilityClass
class DefaultMapping {
    
    private final String DATE_PATTERN = "dd.MM.yyyy";
    private final String DATE_TIME_PATTERN = "dd.MM.yyyy HH:mm:ss";
    private final String TIME_PATTERN = "HH:mm:ss";
    private final String DURATION_FORMAT = "%02d:%02d:%02d";
    private final String NOT_IMPLEMENTED_YET = "Not implemented yet";
    private final String NA = "Н/Д";
    private final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN, new Locale("ru"));
    
    Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String> get(String name) {
        return get().get(name);
    }
    
    Map<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>> get() {
        var mapping = new LinkedHashMap<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>>();
        mapping.put(REQUEST_ID, (r, tt, check) -> r.getHumanReadableId());
        mapping.put(RATING, (r, tt, check) -> {
            var rating = r.getRequestRating();
            if (rating != null && rating.getRating() != null) {
                return String.valueOf(rating.getRating());
            } else {
                return "";
            }
        });
        mapping.put(RATING_COMMENT, (r, tt, check) -> {
            var rating = r.getRequestRating();
            if (rating != null && rating.getRatingComment() != null) {
                return rating.getRatingComment();
            } else {
                return "";
            }
        });
        mapping.put(TRANSPORT_TYPE,
                    (r, tt, check) -> TransportTypeEnum.getByName(r.getTransportType()).map(TransportTypeEnum::getRusName).orElse(NA));
        mapping.put(DESIRED_DATE, (r, tt, check) -> {
            LocalDateTime desiredDateLocalDateTime = r.getDesiredDate();
            ZonedDateTime zdt = desiredDateLocalDateTime.atZone(ZoneId.of("UTC"));
            zdt = zdt.withZoneSameInstant(ZoneId.of(r.getTimeZone() == null ? "UTC" : r.getTimeZone()));
            return zdt.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
        });
        mapping.put(DESIRED_TIME, (r, tt, check) -> {
            LocalDateTime desiredTimeLocalDateTime = r.getDesiredDate();
            ZonedDateTime zdt = desiredTimeLocalDateTime.atZone(ZoneId.of("UTC"));
            zdt = zdt.withZoneSameInstant(ZoneId.of(r.getTimeZone() == null ? "UTC" : r.getTimeZone()));
            return zdt.format(DateTimeFormatter.ofPattern(TIME_PATTERN));
        });
        mapping.put(REQUEST_STATUS,
                    (r, tt, check) -> TripRequestStatus.getByName(r.getStatus()).map(TripRequestStatus::getDescription).orElse(NA));
        mapping.put(PASSENGER_FIO, (r, tt, check) -> {
            if (r.getPassenger() != null) {
                return r.getPassenger().getFIO();
            } else {
                return NA;
            }
        });
        mapping.put(APPROVER_FIO, (r, tt, check) ->
                r.getApprovedBy() != null ? r.getApprovedBy().getFIO() : NA);
        
        mapping.put(CREATION_DATETIME, (r, tt, check) -> {
            LocalDateTime creationTimeLocalDateTime = r.getCreationTime();
            ZonedDateTime zdt = creationTimeLocalDateTime.atZone(ZoneOffset.UTC);
            zdt = zdt.withZoneSameInstant(ZoneId.of(r.getTimeZone() == null ? "UTC" : r.getTimeZone()));
            
            return zdt.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
        });
        
        mapping.put(DEPARTURE_ADDRESS, (r, tt, check) -> {
            if (r.getWaypoints() != null && !r.getWaypoints().isEmpty()) {
                return r.getWaypoints().get(0).getAddress().toString();
            } else {
                return NA;
            }
        });
        mapping.put(CARRIER, (r, tt, check) -> NOT_IMPLEMENTED_YET);
        mapping.put(DESTINATION_ADDRESS, (r, tt, check) -> {
            if (r.getWaypoints() != null && r.getWaypoints().size() > 1) {
                return r.getWaypoints().get(r.getWaypoints().size() - 1).getAddress().toString();
            } else {
                return NA;
            }
        });
        mapping.put(TRIP_PURPOSE,
                    (r, tt, check) -> (r.getPurpose() != null && r.getPurpose().getPurpose() != null) ?
                                      r.getPurpose().getPurpose() :
                                      NA);
        mapping.put(TRIP_TYPE, (r, tt, check) -> r.isCoopTrip() ? "Совместная" : "Индивидуальная");
        mapping.put(TARIFF_ID, (r, tt, check) -> {
            if (r.getTariff() != null) {
                return String.valueOf(r.getTariff().getHumanReadableId());
            }
            return NA;
        });
        mapping.put(COMMENT_FOR_DRIVER, (r, tt, check) -> Optional.ofNullable(r.getCommentForDriver()).orElse(""));
        
        mapping.put(ACTUAL_WAITING_TIME, (r, tt, check) -> {
            if (!TripRequestStatus.getTripFinishStatuses().stream().map(TripRequestStatus::name)
                                  .toList().contains(r.getStatus())) {
                Duration result = null;
                for (Waypoint waypoint : r.getWaypoints()) {
                    if (result != null) {
                        result = result.plus(waypoint.getWaitTime());
                    } else {
                        result = waypoint.getWaitTime();
                    }
                }
                if (result == null) {
                    return String.format("0:00:%02d", Duration.ZERO.getSeconds());
                } else {
                    return String
                            .format(DURATION_FORMAT, result.toHours(), result.toMinutesPart(), result.toSecondsPart());
                }
            } else {
                return "";
            }
        });
        mapping.put(ACTUAL_DURATION, (r, tt, check) -> {
            if (r.getExpected() != null) {
                return String.valueOf(r.getExpected().getTime());
            } else {
                return "-";
            }
        });
        mapping.put(LIMIT_ID, (r, tt, check) -> {
            var lim = r.getLimit();
            if (lim != null) {
                return lim.getHumanReadableId();
            } else {
                return NA;
            }
        });
        mapping.put(ADDITIONAL_OPTIONS, (r, tt, check) -> NOT_IMPLEMENTED_YET);
        mapping.put(SERVICE_NAME,
                    (r, tt, check) -> TransportTypeEnum.getByName(r.getTransportType()).map(TransportTypeEnum::getRusName).orElse(NA));
        mapping.put(EXPENSE_ITEM, (r, tt, check) -> "180.8 Расходы по найму транспортных средств");
        mapping.put(MVZ, (r, tt, check) -> {
            if (r.getPassenger() != null && r.getPassenger().getCostCenter() != null) {
                return r.getPassenger().getCostCenter();
            }
            return NA;
        });
        mapping.put(LIMIT_PARENT_DEPARTMENT, (r, tt, check) -> {
            var lim = r.getLimit();
            if (lim != null) {
                return String.valueOf(lim.getDepartmentId());
            } else {
                return NA;
            }
        });
        mapping.put(HIERARCHY_OF_THE_DEPARTMENT, (r, tt, check) -> NOT_IMPLEMENTED_YET);
        mapping.put(PASSENGER_PERSONNEL_NUMBER, (r, tt, check) -> {
            if (r.getPassenger() != null && r.getPassenger().getPersonnelNumber() != null) {
                return r.getPassenger().getPersonnelNumber();
            }
            return NA;
        });
        mapping.put(PASSENGER_ACTIVITY, (r, tt, check) -> {
            if (r.getPassenger() != null && r.getPassenger().getItinerantType() != null) {
                return "Да";
            } else {
                return "Нет";
            }
        });
        mapping.put(PASSENGER_DEPARTMENT_1, (r, tt, check) -> {
            Map<Integer, Department> departmentHierarchy = r.getDepartmentHierarchy();
            if (departmentHierarchy != null) {
                Department department = departmentHierarchy.get(1);
                if (department != null) {
                    return department.getDepartmentName();
                }
            }
            return NA;
        });
        mapping.put(PASSENGER_DEPARTMENT_2, (r, tt, check) -> {
            Map<Integer, Department> departmentHierarchy = r.getDepartmentHierarchy();
            if (departmentHierarchy != null) {
                Department department = departmentHierarchy.get(2);
                if (department != null) {
                    return department.getDepartmentName();
                }
            }
            return NA;
        });
        mapping.put(PASSENGER_DEPARTMENT_3, (r, tt, check) -> {
            Map<Integer, Department> departmentHierarchy = r.getDepartmentHierarchy();
            if (departmentHierarchy != null) {
                Department department = departmentHierarchy.get(3);
                if (department != null) {
                    return department.getDepartmentName();
                }
            }
            return NA;
        });
        mapping.put(PASSENGER_DEPARTMENT_4, (r, tt, check) -> {
            Map<Integer, Department> departmentHierarchy = r.getDepartmentHierarchy();
            if (departmentHierarchy != null) {
                Department department = departmentHierarchy.get(4);
                if (department != null) {
                    return department.getDepartmentName();
                }
            }
            return NA;
        });
        mapping.put(PASSENGER_DEPARTMENT_5, (r, tt, check) -> {
            Map<Integer, Department> departmentHierarchy = r.getDepartmentHierarchy();
            if (departmentHierarchy != null) {
                Department department = departmentHierarchy.get(5);
                if (department != null) {
                    return department.getDepartmentName();
                }
            }
            return NA;
        });
        mapping.put(PASSENGER_DEPARTMENT_6, (r, tt, check) -> {
            Map<Integer, Department> departmentHierarchy = r.getDepartmentHierarchy();
            if (departmentHierarchy != null) {
                Department department = departmentHierarchy.get(6);
                if (department != null) {
                    return department.getDepartmentName();
                }
            }
            return NA;
        });
        mapping.put(PASSENGER_POSITION, (r, tt, check) -> {
            if (r.getPassenger() != null && r.getPassenger().getPosition() != null) {
                return r.getPassenger().getPosition().getName();
            } else {
                return NA;
            }
        });
        mapping.put(CUSTOMER_PERSONNEL_NUMBER, (r, tt, check) -> {
            if (r.getAuthor() != null && r.getAuthor().getHumanReadableId() != null) {
                return r.getAuthor().getHumanReadableId();
            } else {
                return NA;
            }
        });
        mapping.put(CUSTOMER_FIO, (r, tt, check) -> {
            if (r.getAuthor() != null && r.getAuthor().getFIO() != null) {
                return r.getAuthor().getFIO();
            } else {
                return NA;
            }
        });
        mapping.put(APPROVER_PERSONNEL_NUMBER, (r, tt, check) -> {
            if (r.getApprovedBy() != null && r.getApprovedBy().getHumanReadableId() != null) {
                return r.getApprovedBy().getHumanReadableId();
            } else {
                return NA;
            }
        });
        mapping.put(TARIFF_BY_KM, (r, tt, check) -> {
            if (r.getTransportType().equals(TransportTypeEnum.TAXI.name()) && tt != null && tt.getTariff() != null
                && tt.getTariff().getRideCostPerKm() != null) {
                return String.valueOf(tt.getTariff().getRideCostPerKm());
            } else {
                return NA;
            }
        });
        mapping.put(TARIFF_BY_MIN, (r, tt, check) -> {
            if (r.getTransportType().equals(TransportTypeEnum.TAXI.name()) && tt != null && tt.getTariff() != null
                && tt.getTariff().getRideCostPerMin() != null) {
                return String.valueOf(tt.getTariff().getRideCostPerMin());
            } else {
                return NA;
            }
        });
        mapping.put(TARIFF_BY_WAITING_MIN, (r, tt, check) -> {
            if (r.getTransportType().equals(TransportTypeEnum.TAXI.name()) && tt != null && tt.getTariff() != null
                && tt.getTariff().getWaitCostPerMin() != null) {
                return String.valueOf(tt.getTariff().getWaitCostPerMin());
            } else {
                return NA;
            }
        });
        mapping.put(TARIFF_CAR_SERVICE_COST, (r, tt, check) -> {
            if (r.getTransportType().equals(TransportTypeEnum.TAXI.name()) && tt != null && tt.getTariff() != null
                && tt.getTariff().getCarServiceCost() != null) {
                return String.valueOf(tt.getTariff().getCarServiceCost());
            } else {
                return NA;
            }
        });
        mapping.put(TARIFF_MIN_RIDE_COST, (r, tt, check) -> {
            if (r.getTransportType().equals(TransportTypeEnum.TAXI.name()) && tt != null && tt.getTariff() != null
                && tt.getTariff().getCarServiceCost() != null) {
                return String.valueOf(tt.getTariff().getCarServiceCost());
            } else {
                return NA;
            }
        });
        mapping.put(INTERMEDIATE_ADDRESSES, (r, tt, check) -> {
            if (r.getWaypoints() != null && r.getWaypoints().size() > 2) {
                StringBuilder fullAddressPath = new StringBuilder();
                for (int i = 1; i < r.getWaypoints().size() - 2; i++) {
                    fullAddressPath.append(r.getWaypoints().get(i).getAddress().toString()).append(" -> ");
                }
                fullAddressPath.append(r.getWaypoints().get(r.getWaypoints().size() - 2).getAddress().toString());
                return fullAddressPath.toString();
            } else {
                return NA;
            }
        });
        mapping.put(FREE_WAITING_TIME, (r, tt, check) -> NOT_IMPLEMENTED_YET);
        mapping.put(EXECUTION_DATE_OF_REQUEST, (r, tt, check) -> {
            if (r.getTransportType().equals(TransportTypeEnum.TAXI.name()) && tt != null && tt.getTripStartTime() != null) {
                return tt.getTripStartTime().format(DATE_TIME_FORMATTER);
            } else {
                return NA;
            }
        });
        mapping.put(CLOSE_DATE_OF_REQUEST, (r, tt, check) -> {
            if (r.getTransportType().equals(TransportTypeEnum.TAXI.name()) && tt != null && tt.getTripFinishTime() != null) {
                return tt.getTripFinishTime().format(DATE_TIME_FORMATTER);
            } else {
                return NA;
            }
        });
        mapping.put(CONTROL_DATE, (r, tt, check) -> NOT_IMPLEMENTED_YET);
        mapping.put(PASSENGER_MOBILE_PHONE, (r, tt, check) -> {
            if (r.getPassenger() != null
                && r.getPassenger().getMobilePhone() != null
                && !r.getPassenger().getMobilePhone().isBlank()) {
                return r.getPassenger().getMobilePhone();
            } else {
                return NA;
            }
        });
        mapping.put(REQUEST_DESCRIPTION, (r, tt, check) -> {
            if (r.getPassenger() != null && r.getPurpose() != null) {
                return "Мобильный телефон: " + r.getPassenger().getMobilePhone() + "\nЦель выезда: "
                       + r.getPurpose().getPurpose();
            } else {
                return NA;
            }
        });
        mapping.put(VSP_GOSB_TB_ADDRESS, (r, tt, check) -> String.valueOf(r.getWaypoints() != null && !r.getWaypoints().isEmpty() &&
                                                                          r.getWaypoints().stream()
                                                                           .anyMatch(waypoint -> Boolean.TRUE.equals(
                                                                                   waypoint.getAddress().getExistInVspGosbTbRegistry()))));
        mapping.put(REQUEST_DECISION, (r, tt, check) -> {
            if (r.getResolution() != null) {
                return r.getResolution();
            } else {
                return NA;
            }
        });
        mapping.put(CONTRACTOR_NAME, (r, tt, check) -> {
            if (r.getTransportType().equals(TransportTypeEnum.TAXI.name()) && tt != null && tt.getTariff() != null
                && tt.getTariff().getContract() != null
                && tt.getTariff().getContract().getContractor() != null
                && tt.getTariff().getContract().getContractor().getName() != null) {
                return tt.getTariff().getContract().getContractor().getName();
            } else {
                return NA;
            }
        });
        mapping.put(REQUEST_EXPECTED_DISTANCE, (r, tt, check) -> {
            if (r.getExpected() != null) {
                return String.valueOf(r.getExpected().getDistance());
            } else {
                return NA;
            }
        });
        mapping.put(TRIP_FACT_DISTANCE, (r, tt, check) -> {
            if (r.getTransportType().equals(TransportTypeEnum.TAXI.name()) && tt != null && tt.getTripFactDistance() != null) {
                return String.valueOf(tt.getTripFactDistance());
            } else {
                return NA;
            }
        });
        mapping.put(REQUEST_EXPECTED_COST, (r, tt, check) -> {
            if (r.getExpected() != null) {
                return String.valueOf(r.getExpected().getCost());
            } else {
                return NA;
            }
        });
        mapping.put(TRIP_FACT_PRICE, (r, tt, check) -> {
            if (r.getTransportType().equals(TransportTypeEnum.TAXI.name()) && tt != null && tt.getTripFactPrice() != null) {
                return String.valueOf(tt.getTripFactPrice());
            } else {
                return NA;
            }
        });
        mapping.put(REQUEST_EXPECTED_TIME, (r, tt, check) -> {
            if (r.getTransportType().equals(TransportTypeEnum.TAXI.name()) && tt != null && tt.getTripFactPrice() != null) {
                return String.valueOf(tt.getTripFactPrice());
            } else {
                return NA;
            }
        });
        mapping.put(TRIP_FACT_DURATION, (r, tt, check) -> {
            if (r.getTransportType().equals(TransportTypeEnum.TAXI.name()) && tt != null && tt.getTripFactDuration() != null) {
                Duration factDuration = tt.getTripFactDuration();
                return String.format(DURATION_FORMAT, factDuration.toHours(), factDuration.toMinutesPart(), factDuration.toSecondsPart());
            }
            return NA;
        });
        mapping.put(WAYPOINT_WAIT_TIME, (r, tt, check) -> {
            Duration resultWaitTime = Duration.ZERO;
            if (r.getWaypoints().size() > 2) {
                for (int i = 1; i < r.getWaypoints().size() - 1; i++) {
                    resultWaitTime = resultWaitTime.plus(r.getWaypoints().get(i).getWaitTime());
                }
            }
            return String.format(DURATION_FORMAT, resultWaitTime.toHours(), resultWaitTime.toMinutesPart(), resultWaitTime.toSecondsPart());
        });
        mapping.put(INTERMEDIATE_WAYPOINT_WAIT_TIME, (r, tt, check) -> NOT_IMPLEMENTED_YET);
        mapping.put(INTERMEDIATE_WAYPOINT_PRICE, (r, tt, check) -> NOT_IMPLEMENTED_YET);
        mapping.put(DEPARTURE_WAIT_TIME, (r, tt, check) -> NOT_IMPLEMENTED_YET);
        mapping.put(DEPARTURE_WAIT_PRICE, (r, tt, check) -> NOT_IMPLEMENTED_YET);
        mapping.put(ACTUAL_REQUEST_PARAMS_UPDATE_DATE, (r, tt, check) -> NOT_IMPLEMENTED_YET);
        
        mapping.put(ACTUAL_DEPARTURE_DATETIME, (r, tt, check) -> {
            if (r.getSingleTaxiTrip() != null && r.getSingleTaxiTrip().getTripStartTime() != null) {
                LocalDateTime tripStartTime = r.getSingleTaxiTrip().getTripStartTime();
                ZonedDateTime zdt = tripStartTime.atZone(ZoneId.of("UTC"));
                zdt = zdt.withZoneSameInstant(ZoneId.of(r.getTimeZone() == null ? "UTC" : r.getTimeZone()));
                
                return zdt.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
            } else {
                return NA;
            }
        });
        
        mapping.put(SHARED_REQUEST_ID, (r, tt, check) -> {
            if (r.isCoopTrip() && r.getSharedRide() != null) {
                return String.valueOf(r.getSharedRide().getId());
            } else {
                return NA;
            }
        });
        mapping.put(COST_SHARE_PART, (r, tt, check) -> {
            if (r.isCoopTrip() && r.getSharedRide() != null && r.getSharedRide().getKpi() != null
                && r.getSharedRide().getKpi().getOrdersKpi() != null
                && !r.getSharedRide().getKpi().getOrdersKpi().isEmpty()) {
                
                var collect = r.getSharedRide()
                               .getKpi().getOrdersKpi().stream()
                               .filter(ordersKpi -> Objects.equals(ordersKpi.getRequestId(), r.getId()))
                               .toList();
                
                if (!collect.isEmpty()) {
                    return String.valueOf(collect.get(0).getCostSharePart());
                }
            }
            return NA;
        });
        mapping.put(REQUEST_EXPECTED_COST_2, (r, tt, check) -> {
            if (r.getExpected() != null) {
                return String.valueOf(r.getExpected().getCost());
            } else {
                return NA;
            }
        });
        mapping.put(REQUEST_SAVING, (r, tt, check) -> {
            if (r.isCoopTrip() && r.getSharedRide() != null && r.getSharedRide().getKpi() != null
                && r.getSharedRide().getKpi().getOrdersKpi() != null
                && !r.getSharedRide().getKpi().getOrdersKpi().isEmpty()) {
                List<OrderKpi> collect = r.getSharedRide()
                                          .getKpi().getOrdersKpi().stream()
                                          .filter(ordersKpi -> Objects.equals(ordersKpi.getRequestId(), r.getId()))
                                          .toList();
                if (!collect.isEmpty()) {
                    return String.valueOf(collect.get(0).getSavings());
                }
            }
            return NA;
        });
        mapping.put(ACTUAL_REQUEST_SAVING, (r, tt, check) -> NOT_IMPLEMENTED_YET);
        mapping.put(TRIP_FACT_PRICE_2, (r, tt, check) -> {
            if (tt != null && tt.getTripFactPrice() != null) {
                return String.valueOf(tt.getTripFactPrice());
            } else {
                return NA;
            }
        });
        mapping.put(SHARED_PASSENGERS_COUNT, (r, tt, check) -> {
            if (r.getSharedRide() != null) {
                return String.valueOf(r.getSharedRide().getPassengers());
            } else {
                return NA;
            }
        });
        mapping.put(TARIFF_ACCURACY, (r, tt, check) -> {
            if (tt != null && r.getExpected() != null && tt.getTripFactPrice() != null) {
                return String.valueOf(tt.getTripFactPrice() - r.getExpected().getCost());
            } else {
                return NA;
            }
        });
        mapping.put(ORDER_DISTANCE_KM, (r, tt, check) -> {
            if (r.isCoopTrip() && r.getSharedRide() != null
                && r.getSharedRide().getKpi() != null
                && r.getSharedRide().getKpi().getOrdersKpi() != null
                && !r.getSharedRide().getKpi().getOrdersKpi().isEmpty()) {
                List<OrderKpi> collect = r.getSharedRide()
                                          .getKpi().getOrdersKpi().stream()
                                          .filter(ordersKpi -> Objects.equals(ordersKpi.getRequestId(), r.getId()))
                                          .toList();
                if (!collect.isEmpty()) {
                    return String.valueOf(collect.get(0).getOrderDistanceKm());
                }
            }
            return NA;
        });
        mapping.put(TRIP_FACT_DISTANCE_2, (r, tt, check) -> {
            if (tt != null && tt.getTripFactDistance() != null) {
                return String.valueOf(tt.getTripFactDistance());
            } else {
                return NA;
            }
        });
        mapping.put(CONTRAGENT_WAITING_TIME, (r, tt, check) -> NOT_IMPLEMENTED_YET);
        mapping.put(CONTRAGENT_DISTANCE, (r, tt, check) -> NOT_IMPLEMENTED_YET);
        mapping.put(CONTRAGENT_COST, (r, tt, check) -> NOT_IMPLEMENTED_YET);
        mapping.put(CHECK_0, (r, tt, check) -> {
            if (check != null && check.getValidTaxiId0()) {
                return Colors.GREEN.name();
            } else {
                return Colors.RED.name();
            }
        });
        mapping.put(CHECK_1, (r, tt, check) -> {
            if (check != null && check.getValidTripStatus1()) {
                return Colors.GREEN.name();
            } else {
                return Colors.RED.name();
            }
        });
        mapping.put(CHECK_2, (r, tt, check) -> {
            if (check != null && check.getValidTripDate2()) {
                return Colors.GREEN.name();
            } else {
                return Colors.RED.name();
            }
        });
        mapping.put(CHECK_3, (r, tt, check) -> {
            if (check != null && check.getValidCancelledTripCost3()) {
                return Colors.GREEN.name();
            } else {
                return Colors.RED.name();
            }
        });
        mapping.put(CHECK_4, (r, tt, check) -> {
            if (check != null && check.getValidCalcDistance4()) {
                return Colors.GREEN.name();
            } else {
                return Colors.RED.name();
            }
        });
        mapping.put(CHECK_4A, (r, tt, check) -> {
            if (check != null && check.getValidFactDistance4a()) {
                return Colors.GREEN.name();
            } else {
                return Colors.RED.name();
            }
        });
        mapping.put(CHECK_5, (r, tt, check) -> {
            if (check != null && check.getValidTariff5()) {
                return Colors.GREEN.name();
            } else {
                return Colors.RED.name();
            }
        });
        mapping.put(CHECK_6, (r, tt, check) -> {
            if (check != null && check.getValidCalcCost6()) {
                return Colors.GREEN.name();
            } else {
                return Colors.RED.name();
            }
        });
        mapping.put(CHECK_7, (r, tt, check) -> {
            if (check != null && check.getValidFactCost7()) {
                return Colors.GREEN.name();
            } else {
                return Colors.RED.name();
            }
        });
        mapping.put(CHECK_8, (r, tt, check) -> {
            if (check != null && check.getValidWaitTime8()) {
                return Colors.GREEN.name();
            } else {
                return Colors.RED.name();
            }
        });
        mapping.put(APPROVE_DATE, (r, tt, check) -> {
            if (r.getApprovalDate() != null) {
                LocalDateTime approvalDateLocalDateTime = r.getApprovalDate();
                ZonedDateTime zdt = approvalDateLocalDateTime.atZone(ZoneId.of("UTC"));
                zdt = zdt.withZoneSameInstant(ZoneId.of(r.getTimeZone() == null ? "UTC" : r.getTimeZone()));
                
                return zdt.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
            } else {
                return NA;
            }
        });
        mapping.put(KK_PERSONAL_NUMBER, (r, tt, check) -> {
            if (r.getPassenger() != null && r.getPassenger().getPersonnelNumber() != null) {
                return String.valueOf(r.getPassenger().getPersonnelNumber());
            } else {
                return NA;
            }
        });
        mapping.put(KK_FIO, (r, tt, check) -> {
            if (r.getPassenger() != null) {
                return String.valueOf(r.getPassenger().getFIO());
            } else {
                return NA;
            }
        });
        mapping.put(HAS_ATTACHMENT, (r, tt, check) -> NOT_IMPLEMENTED_YET);
        mapping.put(COMPENSATION_TYPE, (r, tt, check) -> {
            if (r.getTransportCompensation() != null) {
                return r.getTransportCompensation().stream()
                        .map(v -> v.getCompensationType().getRusName()).collect(Collectors.joining(", "));
            }
            return NA;
        });
        mapping.put(WAYPOINTS_COUNT, (r, tt, check) -> {
            if (r.getWaypoints() != null) {
                return String.valueOf(r.getWaypoints().size());
            }
            return NA;
        });
        mapping.put(WAYPOINTS_COUNT_WITH_CHECK_IN, (r, tt, check) -> {
            if (r.getWaypoints() != null) {
                List<Waypoint> waypointList = r.getWaypoints();
                return String.valueOf(waypointList.stream().filter(waypoint ->
                                                                           Boolean.TRUE.equals(waypoint.getCheckinAutomatic()) ||
                                                                           Boolean.TRUE.equals(waypoint.getCheckinManual()))
                                                  .count());
                
            }
            return "0";
        });
        mapping.put(WAYPOINTS_COUNT_WITHOUT_CHECK_IN, (r, tt, check) -> {
            if (r.getWaypoints() != null) {
                List<Waypoint> waypointList = r.getWaypoints();
                return String.valueOf(waypointList.stream().filter(waypoint ->
                                                                           waypoint.getCheckinAutomatic() == null ||
                                                                           waypoint.getCheckinManual() == null ||
                                                                           Boolean.FALSE.equals(waypoint.getCheckinAutomatic()) ||
                                                                           Boolean.FALSE.equals(waypoint.getCheckinManual()))
                                                  .count());
            }
            return "0";
        });
        mapping.put(PAYMENT_PERIOD, (r, tt, check) -> {
            Integer periodOfPayment = r.getPeriodOfPayment();
            if (periodOfPayment != null) {
                return periodOfPayment.toString();
            }
            return NA;
        });
        mapping.put(OWNERSHIP_OF_CAR, (r, tt, check) -> {
            if (r.getPersonalCar() != null && r.getPersonalCar().getOwnerInfo() != null && r.getPersonalCar().getOwnerInfo().getRusName() != null) {
                return r.getPersonalCar().getOwnerInfo().getRusName();
            }
            return NA;
        });
        mapping.put(MARRIAGE_CERTIFICATE_NUMBER, (r, tt, check) -> {
            //если пассажиров несколько
            if (r.isCoopTrip()) {
                var sbMarriageCertificateNumbers = new StringBuilder();
                Set<Employee> passengers = r.getPassengers();
                if (passengers != null) {
                    for (Employee p : passengers) {
                        if (p.getMarriageCertificateNumber() != null) {
                            sbMarriageCertificateNumbers.append(p.getMarriageCertificateNumber()).append(" ");
                        }
                    }
                }
                if (!sbMarriageCertificateNumbers.isEmpty()) {
                    return sbMarriageCertificateNumbers.toString();
                }
            } else {
                if (r.getPassenger() != null && r.getPassenger().getMarriageCertificateNumber() != null
                    && !r.getPassenger().getMarriageCertificateNumber().isBlank()) {
                    return r.getPassenger().getMarriageCertificateNumber();
                }
            }
            return NA;
        });
        mapping.put(CAR_REGISTRATION_NUMBER, (r, tt, check) -> {
            if (r.getPersonalCar() != null && r.getPersonalCar().getRegistrationNumber() != null) {
                return r.getPersonalCar().getRegistrationNumber();
            } else {
                return NA;
            }
        });
        mapping.put(CAR_BRAND_NAME, (r, tt, check) -> {
            if (r.getPersonalCar() != null && r.getPersonalCar().getBrandName() != null) {
                return r.getPersonalCar().getBrandName();
            } else {
                return NA;
            }
        });
        mapping.put(CAR_ENGINE_VOLUME, (r, tt, check) -> {
            if (r.getPersonalCar() != null) {
                var engineVolume = r.getPersonalCar().getEngineVolume();
                return Integer.toString(engineVolume);
            }
            return NA;
        });
        mapping.put(OSAGO_NUMBER, (r, tt, check) -> {
            if (r.getPersonalCar() != null && r.getPersonalCar().getInsuranceNumber() != null) {
                return r.getPersonalCar().getInsuranceNumber();
            } else {
                return NA;
            }
        });
        mapping.put(REQUEST_EXPECTED_DISTANCE_2, (r, tt, check) -> {
            if (r.getExpected() != null) {
                return String.valueOf(r.getExpected().getDistance());
            } else {
                return NA;
            }
        });
        mapping.put(REQUEST_EXPECTED_COST_3, (r, tt, check) -> {
            if (r.getExpected() != null && r.getExpected().getCost() != null) {
                int rub = r.getExpected().getCost().intValue() / 100;
                int kop = r.getExpected().getCost().intValue() % 100;
                return String.format("%d.%02d", rub, kop);
            } else {
                return NA;
            }
        });
        mapping.put(SHARED_RIDE_ID, (r, tt, check) -> {
            if (r.getSharedRide() != null) {
                return String.valueOf(r.getSharedRide().getId());
            } else {
                return NA;
            }
        });
        mapping.put(CUSTOMER_STRUCTURE_UNIT, (r, tt, check) -> {
            //если пассажиров несколько
            if (r.isCoopTrip()) {
                var sbDepartments = new StringBuilder();
                Set<Employee> passengers = r.getPassengers();
                if (passengers != null) {
                    for (Employee p : passengers) {
                        if (p.getDepartment() != null) {
                            sbDepartments.append(p.getDepartment().getDepartmentName()).append(" ");
                        }
                    }
                }
                return sbDepartments.toString();
            } else {
                if (r.getPassenger() != null && r.getPassenger().getDepartment() != null) {
                    return r.getPassenger().getDepartment().getDepartmentName();
                }
            }
            return NA;
        });
        
        mapping.put(CODE_ORG_STRUCTURE_PASSENGER, (r, tt, check) -> {
            if (r.isCoopTrip()) {
                StringBuilder sbORG = new StringBuilder();
                Set<Employee> passengers = r.getPassengers();
                if (passengers != null) {
                    for (var p : passengers) {
                        if (p.getDepartment().getOrganizationId() != null) {
                            sbORG.append(p.getDepartment().getOrganizationId()).append(" ");
                        }
                    }
                }
                return sbORG.toString();
            } else {
                if (r.getPassenger() != null && r.getPassenger().getDepartment().getOrganizationId() != null) {
                    return r.getPassenger().getDepartment().getOrganizationId().toString();
                }
            }
            return NA;
        });
        
        mapping.put(ORDER_PAYMENT_FORMATION_FINISHING_DATE, (r, tt, check) -> {
            if (r.getOrderPaymentFormationFinishingDate() != null) {
                LocalDateTime orderPaymentFormationFinishingDate = r.getOrderPaymentFormationFinishingDate();
                ZonedDateTime zdt = orderPaymentFormationFinishingDate.atZone(ZoneId.of("UTC"));
                zdt = zdt.withZoneSameInstant(ZoneId.of(r.getTimeZone() == null ? "UTC" : r.getTimeZone()));
                
                return zdt.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
            } else {
                return NA;
            }
        });
        
        mapping.put(ORDER_PAYMENT_FORMATION_START_DATE, (r, tt, check) -> {
            if (r.getOrderPaymentFormationStartDate() != null) {
                LocalDateTime orderPaymentFormationStartDate = r.getOrderPaymentFormationStartDate();
                ZonedDateTime zdt = orderPaymentFormationStartDate.atZone(ZoneId.of("UTC"));
                zdt = zdt.withZoneSameInstant(ZoneId.of(r.getTimeZone() == null ? "UTC" : r.getTimeZone()));
                
                return zdt.format(DateTimeFormatter.ofPattern(DATE_TIME_PATTERN));
            } else {
                return NA;
            }
        });
        return mapping;
    }
}
