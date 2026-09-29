package ru.sberbank.ditsib.transport.reports.service.impl.mapping;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.dto.PublicUIVisibilityDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryCheckDTO;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.TaxiTrip;
import ru.sberbank.ditsib.transport.reports.service.DefaultResolver;
import ru.sberbank.ditsib.transport.reports.service.Mappings;
import ru.sberbank.ditsib.transport.reports.utils.Function4;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import static ru.sberbank.ditsib.transport.reports.service.impl.mapping.AllColumnNames.*;
@Component
@RequiredArgsConstructor
class PublicMappingsImpl implements Mappings<PublicUIVisibilityDTO> {
    
    private final DefaultResolver<PublicUIVisibilityDTO> resolver;
    private final Map<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>> publicMapping
            = useDefaultMapping(MVZ, //0
                                REQUEST_ID,
                                CREATION_DATETIME,
                                ACTUAL_DEPARTURE_DATETIME,
                                APPROVE_DATE,
                                KK_PERSONAL_NUMBER,
                                //5
                                KK_FIO,
                                PASSENGER_ACTIVITY,
                                TRIP_PURPOSE,
                                TRANSPORT_TYPE,
                                REQUEST_STATUS,
                                // 10
                                REQUEST_EXPECTED_COST_3,
                                HAS_ATTACHMENT,
                                COMPENSATION_TYPE,
                                WAYPOINTS_COUNT,
                                WAYPOINTS_COUNT_WITH_CHECK_IN,
                                WAYPOINTS_COUNT_WITHOUT_CHECK_IN,
                                DEPARTURE_ADDRESS,
                                DESTINATION_ADDRESS,
                                PAYMENT_PERIOD,
                                RATING,
                                RATING_COMMENT,
                                PASSENGER_DEPARTMENT_1,
                                PASSENGER_DEPARTMENT_2,
                                PASSENGER_DEPARTMENT_3,
                                PASSENGER_DEPARTMENT_4,
                                PASSENGER_DEPARTMENT_5,
                                PASSENGER_DEPARTMENT_6
                               );
    
    @Override
    public TransportTypeEnum transportType() {
        return TransportTypeEnum.PUBLIC;
    }
    
    public Map<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>> useDefaultMapping(
            String... names
                                                                                                        ) {
        var mapping = new LinkedHashMap<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>>();
        for (String name : names) {
            mapping.put(name, DefaultMapping.get(name));
        }
        return mapping;
    }
    
    private void useDefaultMapping(
            Map<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>> map,
            String name
                                  ) {
        var function = DefaultMapping.get(name);
        Objects.requireNonNull(function, "Not found mapping implementation for '" + name + "'");
        map.put(name, function);
    }
    
    @Override
    public Map<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>> get(PublicUIVisibilityDTO publicUIVisibilityDTO) {
        var customMap = new LinkedHashMap<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>>();
        if (publicUIVisibilityDTO == null) {
            publicUIVisibilityDTO = resolver.getDefault();
        }
        if (publicUIVisibilityDTO.mvzVisible()) {
            useDefaultMapping(customMap, MVZ);
        }
        if (publicUIVisibilityDTO.requestIdVisible()) {
            useDefaultMapping(customMap, REQUEST_ID);
        }
        if (publicUIVisibilityDTO.creationTimeVisible()) {
            useDefaultMapping(customMap, CREATION_DATETIME);
        }
        if (publicUIVisibilityDTO.desiredDateVisible()) {
            useDefaultMapping(customMap, ACTUAL_DEPARTURE_DATETIME);
        }
        if (publicUIVisibilityDTO.approveDateVisible()) {
            useDefaultMapping(customMap, APPROVE_DATE);
        }
        if (publicUIVisibilityDTO.passengerFioVisible()) {
            useDefaultMapping(customMap, KK_FIO);
        }
        if (publicUIVisibilityDTO.itinerantTypeVisible()) {
            useDefaultMapping(customMap, PASSENGER_ACTIVITY);
        }
        if (publicUIVisibilityDTO.tripPurposeVisible()) {
            useDefaultMapping(customMap, TRIP_PURPOSE);
        }
        if (publicUIVisibilityDTO.transportTypeVisible()) {
            useDefaultMapping(customMap, TRANSPORT_TYPE);
        }
        if (publicUIVisibilityDTO.requestStatusVisible()) {
            useDefaultMapping(customMap, REQUEST_STATUS);
        }
        if (publicUIVisibilityDTO.costVisible()) {
            useDefaultMapping(customMap, REQUEST_EXPECTED_COST_3);
        }
        if (publicUIVisibilityDTO.hasAttachmentVisible()) {
            useDefaultMapping(customMap, HAS_ATTACHMENT);
        }
        if (publicUIVisibilityDTO.waypointsCountVisible()) {
            useDefaultMapping(customMap, WAYPOINTS_COUNT);
        }
        if (publicUIVisibilityDTO.waypointsCountWithCheckInVisible()) {
            useDefaultMapping(customMap, WAYPOINTS_COUNT_WITH_CHECK_IN);
        }
        if (publicUIVisibilityDTO.waypointsCountWithoutCheckInVisible()) {
            useDefaultMapping(customMap, WAYPOINTS_COUNT_WITHOUT_CHECK_IN);
        }
        if (publicUIVisibilityDTO.paymentPeriodVisible()) {
            useDefaultMapping(customMap, PAYMENT_PERIOD);
        }
        if (publicUIVisibilityDTO.passengerDepartmentOneVisible()) {
            useDefaultMapping(customMap, PASSENGER_DEPARTMENT_1);
        }
        if (publicUIVisibilityDTO.passengerDepartmentTwoVisible()) {
            useDefaultMapping(customMap, PASSENGER_DEPARTMENT_2);
        }
        if (publicUIVisibilityDTO.passengerDepartmentThreeVisible()) {
            useDefaultMapping(customMap, PASSENGER_DEPARTMENT_3);
        }
        if (publicUIVisibilityDTO.passengerDepartmentFourVisible()) {
            useDefaultMapping(customMap, PASSENGER_DEPARTMENT_4);
        }
        if (publicUIVisibilityDTO.passengerDepartmentFiveVisible()) {
            useDefaultMapping(customMap, PASSENGER_DEPARTMENT_5);
        }
        if (publicUIVisibilityDTO.passengerDepartmentSixVisible()) {
            useDefaultMapping(customMap, PASSENGER_DEPARTMENT_6);
        }
        if (publicUIVisibilityDTO.compensationTypeVisible()) {
            useDefaultMapping(customMap, COMPENSATION_TYPE);
        }
        if (publicUIVisibilityDTO.waypointFromVisible()) {
            useDefaultMapping(customMap, DEPARTURE_ADDRESS);
        }
        if (publicUIVisibilityDTO.waypointToVisible()) {
            useDefaultMapping(customMap, DESTINATION_ADDRESS);
        }
        if (publicUIVisibilityDTO.intermediateAddressVisible()) {
            useDefaultMapping(customMap, INTERMEDIATE_ADDRESSES);
        }
        if (publicUIVisibilityDTO.personelNumberVisible()) {
            useDefaultMapping(customMap, PASSENGER_PERSONNEL_NUMBER);
        }
        if (publicUIVisibilityDTO.orderPaymentFormationStartDateVisible()) {
            useDefaultMapping(customMap, ORDER_PAYMENT_FORMATION_START_DATE);
        }
        if (publicUIVisibilityDTO.ratingVisible()) {
            useDefaultMapping(customMap, RATING);
        }
        if (publicUIVisibilityDTO.ratingCommentVisible()) {
            useDefaultMapping(customMap, RATING_COMMENT);
        }
        return customMap;
    }
    
    @Override
    public Map<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>> get() {
        return publicMapping;
    }
}
