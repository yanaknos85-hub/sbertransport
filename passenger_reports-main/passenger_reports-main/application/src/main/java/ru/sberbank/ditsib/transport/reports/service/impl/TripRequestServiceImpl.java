package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.*;
import ru.sberbank.ditsib.transport.reports.dao.SharedRideRepository;
import ru.sberbank.ditsib.transport.reports.dao.TransportCompensationRepository;
import ru.sberbank.ditsib.transport.reports.dao.WaypointRepository;
import ru.sberbank.ditsib.transport.reports.mappers.DriverMapper;
import ru.sberbank.ditsib.transport.reports.mappers.EntityDTOMapper;
import ru.sberbank.ditsib.transport.reports.mappers.VehicleMapper;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.reports.model.*;
import ru.sberbank.ditsib.transport.reports.model.tariff.BaseTariff;
import ru.sberbank.ditsib.transport.reports.service.*;
import ru.sberbank.ditsib.transport.reports.utils.StatusProcessingHelper;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TripRequestServiceImpl implements TripRequestService {
    
    private final RequestService requestService;
    private final AddressService addressService;
    private final TripPurposeService purposeService;
    private final PersonalCarService personalCarService;
    private final EntityDTOMapper mapper;
    private final EmployeeService employeeService;
    private final WaypointRepository waypointRepository;
    private final TransportCompensationRepository transportCompensationRepository;
    private final ContractorService contractorService;
    private final SharedRideRepository sharedRideRepository;
    private final VehicleMapper vehicleMapper;
    private final DriverMapper driverMapper;
    private final DepartmentService departmentService;
    private final Map<TransportTypeEnum, TariffService<BaseTariff>> publicTariffServiceMap;
    private final PaymentService paymentService;
    
    @Override
    public void processMessage(RequestMessage message) {
        var requestId = message.getId();
        if (!acceptedTransportType(message.getTransportType())) {
            return;
        }
        if (!message.isDeleted()) {
            log.info("Началась обработка сообщения из топика service.request по заявке с id = {}", requestId);
            var request = requestService.findById(requestId)
                                        .orElseGet(() -> requestService.save(Request.builder().id(message.getId()).status(message.getStatus()).build()));

            if (StatusProcessingHelper.stopProcessingMessage(request.getStatus(),
                    message.getId(),
                    message.getTransportType(),
                    message.getStatus())) {
                log.info("Сообщение c messageId = {} проигнорировано по причине \"Текущий статус новее полученного\"", message.getId());
                return;
            }
            
            request.setCostCenter(Optional.ofNullable(message.getPassengerId())
                                          .map(employeeService::findOrCreateEmployeeById)
                                          .map(Employee::getCostCenter)
                                          .orElse(null));
            request.setBalanceUnit(getBalanceUnit(request.getCostCenter()));
            request.setChangeDate(LocalDateTime.now(ZoneOffset.UTC));
            request.getWaypoints().clear();
            request.setResolution(message.getResolution());
            waypointRepository.flush();
            var waypoints = message.getWaypoints();
            for (var messageWaypoint : waypoints) {
                Waypoint waypoint = getWaypoint(request, messageWaypoint);
                if (waypoint != null) {
                    request.getWaypoints().add(waypoint);
                }
            }
            waypointRepository.flush();
            
            if (message.isPublicCompensationDocumentExist()) {
                request.setPublicCompensationDocumentExist(true);
            }
            
            List<RequestMessage.TransportCompensation> transportCompensationMessageList = message.getTransportCompensation();
            if (!transportCompensationMessageList.isEmpty()) {
                request.getTransportCompensation().clear();
                transportCompensationRepository.flush();
                
                for (var compensation : transportCompensationMessageList) {
                    var transportCompensation = transportCompensationRepository.findById(compensation.getId()).
                                                                               orElse(TransportCompensation.builder().id(compensation.getId())
                                                                                                           .build());
                    
                    transportCompensation.setCompensationType(PublicCompensationType.valueOf(compensation.getCompensationType()));
                    transportCompensation.setTransportType(PublicTransportType.valueOf(compensation.getTransportType()));
                    transportCompensation.setTicketsCost(compensation.getTicketsCost());
                    transportCompensation.setTicketsCount(compensation.getTicketsCount());
                    transportCompensation.setTicketsExpirationStart(compensation.getTicketsExpirationStart());
                    transportCompensation.setTicketsExpirationEnd(compensation.getTicketsExpirationEnd());
                    transportCompensation.setAttachedDocumentId(compensation.getAttachedDocumentId());
                    transportCompensation.setRequest(request);
                    
                    request.getTransportCompensation().add(transportCompensation);
                }
                transportCompensationRepository.flush();
            }
            
            if (message.getPersonalCarId() != null) {
                request.setPersonalCar(personalCarService.findOrCreateById(message.getPersonalCarId()));
            }
            if (message.getPurposeId() != null) {
                request.setPurpose(purposeService.findOrCreateByPurposeId(message.getPurposeId()));
            }
            if (message.getPassengerId() != null) {
                request.setPassenger(employeeService.findOrCreateEmployeeById(message.getPassengerId()));
            }
            if (message.getAuthorId() != null) {
                request.setAuthor(employeeService.findOrCreateEmployeeById(message.getAuthorId()));
            }
            if (message.getApprovalId() != null) {
                request.setApprovedBy(employeeService.findOrCreateEmployeeById(message.getApprovalId()));
            }
            if (message.getContractorId() != null) {
                request.setContractor(contractorService.findOrCreateContractorById(message.getContractorId()));
            }
            if (message.getTripClass() != null) {
                request.setTransportClass(message.getTripClass());
            }
            if (message.getCarsharingClass() != null) {
                request.setCarsharingClass(message.getCarsharingClass());
            }
            if (message.getTransportType() != null) {
                request.setTransportType(message.getTransportType());
            }
            if (message.getStatusCode() != null) {
                request.setStatusCode(message.getStatusCode());
            }
            if (message.getStatus() != null) {
                request.setStatus(message.getStatus());
            }
            if (message.getEmployeeDriverId() != null) {
                request.setEmployeeDriverId(message.getEmployeeDriverId());
            }
            request.setDriver(driverMapper.driver(message.getDriverData()));
            request.setVehicle(vehicleMapper.vehicle(message.getVehicleData()));
            if (message.getTariffId() != null && request.getTransportType() != null) {
                TransportTypeEnum.getByName(request.getTransportType()).map(publicTariffServiceMap::get)
                                 .flatMap(service -> service.findById(message.getTariffId()))
                                 .ifPresent(request::setTariff);
            }
            
            request.setPassengerCount(message.getPassengerCount());
            request.setTimeZone(message.getTimeZone());
            request.setHumanReadableId(message.getHumanReadableId());
            request.setDesiredDate(message.getDesiredDate());
            request.setCoopTrip(message.isCoopTrip());
            request.setCommentForDriver(message.getCommentForDriver());
            request.setCreationTime(message.getCreationTime());
            request.setApprovalDate(message.getApprovalDate());
            request.setOrderPaymentFormationFinishingDate(message.getOrderPaymentFormationFinishingDate());
            request.setOrderPaymentFormationStartDate(message.getOrderPaymentFormationStartDate());
            if (message.getIsSlaExpired() != null) {
                request.setSlaExpired(message.getIsSlaExpired());
            }
            if (message.getOrganizationId() != null) {
                request.setOrganizationId(message.getOrganizationId());
            }
            if (message.getApprovalState() != null) {
                request.setApprovalState(message.getApprovalState());
            }
            if (message.getExpected() != null) {
                request.setExpected(mapper.expectedDataMessageToModel(message.getExpected()));
            }
            if (message.getEconomyData() != null && message.getEconomyData().getCostSharePart() != null) {
                request.setCostSharePart(message.getEconomyData().getCostSharePart() * 100);
            }
            if (message.getEconomyData() != null && message.getEconomyData().getSavingsCash() != null) {
                request.setSavingsCash(message.getEconomyData().getSavingsCash());
            }
            if (message.getEconomyData() != null && message.getEconomyData().getSavingsProcents() != null) {
                request.setSavingsProcents(message.getEconomyData().getSavingsProcents());
            }
            if (message.getNumberPassengersJoined() != null) {
                request.setNumberPassengersJoined(message.getNumberPassengersJoined());
            }
            if (message.getAdditionalSum() != null) {
                request.setAdditionalSum(message.getAdditionalSum());
            }
            request.setFinishedTime(message.getFinishedTime());
            if ((request.getTransportType().equals(TransportTypeEnum.PERSONAL.name())
                 || request.getTransportType().equals(TransportTypeEnum.TAXI.name()))
                && request.isCoopTrip()
                && message.getRideId() != null) {
                request.setRideId(message.getRideId());
                request.setSharedRide(null);
                sharedRideRepository.findById(message.getRideId()).ifPresent(request::setSharedRide);
            }
            if (TransportTypeEnum.PUBLIC.name().equals(request.getTransportType())) {
                request.setPaymentData(getPublicPaymentData(request));
            }
            request = paymentService.fillPaymentData(request);
            if (message.getPassenger() != null && message.getPassenger().getDepartmentId() != null) {
                request.setDepartmentHierarchy(departmentService.getDepartmentHierarchy(message.getPassenger().getDepartmentId()));
                request.setPassengerDepartment1(getPassengerDepartmentLevel(request, 1));
                request.setPassengerDepartment2(getPassengerDepartmentLevel(request, 2));
                request.setPassengerDepartment3(getPassengerDepartmentLevel(request, 3));
                request.setPassengerDepartment4(getPassengerDepartmentLevel(request, 4));
                request.setPassengerDepartment5(getPassengerDepartmentLevel(request, 5));
                request.setPassengerDepartment6(getPassengerDepartmentLevel(request, 6));
            }
            request.setDepartureAddress(getDepartureAddress(request));
            request.setIntermediateAddresses(getIntermediateAddresses(request));
            request.setDestinationAddress(getDestinationAddress(request));
            request.setSharedRideOwner(message.isSharedRideOwner());
            request.setRentId(message.getRentId());
            request.setPhoneNumber(message.getPhoneNumber());
            
            // Контрольный срок по прибытию водителя в точку отправления
            request.setDeadline(message.getDeadline());
            request.setDeadlineState(mapDeadlineState(message.getDeadlineState()));
            request.setDriverArrivedDatetime(message.getDriverArrivedDatetime());
            
            // Групповой трансфер
            request.setVip(message.isVip());
            if (message.getGroupTransferClass() != null) {
                request.setTransportClass(message.getGroupTransferClass());
            }
            
            // Группа исполнителей
            request.setExecutorGroupId(message.getExecutorGroupId());
            request.setExecutorGroupName(message.getExecutorGroupName());
            
            request.setSource(message.getSource());
            
            if (message.getJoinedPassengerIds() != null) {
                var joinedPassenger = employeeService.findAllById(message.getJoinedPassengerIds())
                                                     .stream()
                                                     .map(e -> "%s (%s)".formatted(e.getFIO(), e.getPersonnelNumber()))
                                                     .collect(Collectors.joining(", "));
                request.setJoinedPassengers(joinedPassenger);
            }
            if (message.getMinTaxiTariffCost() != null) {
                request.setMinTaxiTariffCost(message.getMinTaxiTariffCost());
            }
            if (message.getCommentForPurpose() != null) {
                request.setCommentForPurpose(message.getCommentForPurpose());
            }
            requestService.save(request);
            log.info("Завершена обработка сообщения из топика service.request по заявке с id = {}", requestId);
        }
    }
    
    private boolean acceptedTransportType(String transportType) {
        return TransportTypeEnum.getByName(transportType)
                                .map(TransportTypeEnum::getServiceType)
                                .filter(TransportServiceType.EMPLOYEE_TRANSPORTATION::equals)
                                .isPresent();
    }
    
    private DeadlineState mapDeadlineState(String deadlineState) {
        if (deadlineState == null || deadlineState.isEmpty()) {
            return null;
        }
        try {
            return DeadlineState.valueOf(deadlineState.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Не получилось определить DeadlineState по коду: {}", deadlineState);
            return null;
        }
    }
    
    private String getDepartureAddress(Request r) {
        if (r.getWaypoints() != null && !r.getWaypoints().isEmpty()) {
            return r.getWaypoints().get(0).getAddress().toString();
        } else {
            return null;
        }
    }
    
    private String getDestinationAddress(Request r) {
        if (r.getWaypoints() != null && r.getWaypoints().size() > 1) {
            return r.getWaypoints().get(r.getWaypoints().size() - 1).getAddress().toString();
        } else {
            return null;
        }
    }
    
    private String getIntermediateAddresses(Request r) {
        if (r.getWaypoints() != null && r.getWaypoints().size() > 2) {
            StringBuilder fullAddressPath = new StringBuilder();
            for (int i = 1; i < r.getWaypoints().size() - 2; i++) {
                fullAddressPath.append(r.getWaypoints().get(i).getAddress().toString()).append(" -> ");
            }
            fullAddressPath.append(r.getWaypoints().get(r.getWaypoints().size() - 2).getAddress().toString());
            return fullAddressPath.toString();
        } else {
            return null;
        }
    }
    
    private String getPassengerDepartmentLevel(Request r, int index) {
        Map<Integer, Department> departmentHierarchy = r.getDepartmentHierarchy();
        if (departmentHierarchy != null) {
            Department department = departmentHierarchy.get(index);
            if (department != null) {
                return department.getDepartmentName();
            }
        }
        return null;
    }
    
    private Integer getBalanceUnit(String costCenter) {
        Integer balanceUnit = null;
        if (costCenter != null && costCenter.length() >= 4) {
            try {
                balanceUnit = Integer.parseInt(costCenter.substring(0, 4));
            } catch (NumberFormatException ignored) {
                // ignore when costCenter is not a number.
            }
        }
        return balanceUnit;
    }
    
    
    private Waypoint getWaypoint(Request request, RequestMessage.Waypoint messageWaypoint) {
        if (messageWaypoint.getId() != null && messageWaypoint.getAddress() != null) {
            var address = addressService.saveIfNotExists(mapper.addressMessageToAddress(messageWaypoint.getAddress()));
            var waypoint = waypointRepository.findById(messageWaypoint.getId()).orElseGet(Waypoint::new);
            waypoint.setId(messageWaypoint.getId());
            waypoint.setAddress(address);
            waypoint.setWaitTime(messageWaypoint.getWaitTime());
            waypoint.setCheckinAutomatic(messageWaypoint.getCheckinAutomatic());
            waypoint.setCheckinManual(messageWaypoint.getCheckinManual());
            waypoint.setRequest(request);
            return waypoint;
        } else {
            return null;
        }
    }
    
    private PaymentData getPublicPaymentData(Request request) {
        PaymentData paymentData = null;
        if (request == null || request.getExpected() == null || request.getExpected().getCost() == null ||
            request.getExpected().getCost().longValue() <= 0L) {
            return null;
        }
        
        PaymentTypeCode paymentTypeCodeMain = PaymentTypeCode.CODE_4666;
        Long paymentPriceMain = request.getExpected().getCost().longValue();
        
        PaymentTypeCode paymentTypeCodeOptional = null;
        
        Long paymentPriceOptional = Optional.ofNullable(request.getTransportCompensation()).orElseGet(ArrayList::new)
                                            .stream()
                                            .filter(ticket -> PublicCompensationType.PAID_SERVICES_COMPENSATION.equals(
                                                    ticket.getTransportType().getPublicCompensationType()))
                                            .mapToLong(TransportCompensation::getTicketsCost)
                                            .sum();
        if (paymentPriceOptional > 0) {
            paymentTypeCodeOptional = PaymentTypeCode.CODE_4664;
            paymentPriceMain = paymentPriceMain - paymentPriceOptional;
        }
        
        if (paymentPriceMain <= 0) {
            paymentTypeCodeMain = paymentTypeCodeOptional;
            paymentPriceMain = paymentPriceOptional;
            paymentTypeCodeOptional = null;
            paymentPriceOptional = 0L;
        }
        
        if (paymentTypeCodeMain != null || paymentTypeCodeOptional != null) {
            paymentData = new PaymentData();
        }
        if (paymentTypeCodeMain != null) {
            paymentData.setPaymentTypeCodeMain(paymentTypeCodeMain);
            paymentData.setPaymentPriceMain(paymentPriceMain);
        }
        if (paymentTypeCodeOptional != null) {
            paymentData.setPaymentTypeCodeOptional(paymentTypeCodeOptional);
            paymentData.setPaymentPriceOptional(paymentPriceOptional);
        }
        
        return paymentData;
    }
    
}
