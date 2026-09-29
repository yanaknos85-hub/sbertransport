package ru.sberbank.ditsib.transport.reports.service.impl.mapping;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.dto.PersonalUIVisibilityDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryCheckDTO;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.TaxiTrip;
import ru.sberbank.ditsib.transport.reports.service.DefaultResolver;
import ru.sberbank.ditsib.transport.reports.service.Mappings;
import ru.sberbank.ditsib.transport.reports.utils.Function4;

import java.util.LinkedHashMap;
import java.util.Map;

import static ru.sberbank.ditsib.transport.reports.service.impl.mapping.AllColumnNames.*;

@Component
@RequiredArgsConstructor
class PersonalMappingsImpl implements Mappings<PersonalUIVisibilityDTO> {
    
    private final DefaultResolver<PersonalUIVisibilityDTO> resolver;
    
    @Override
    public TransportTypeEnum transportType() {
        return TransportTypeEnum.PERSONAL;
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
    
    private final Map<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>> personalMapping =
            useDefaultMapping(MVZ, //0
                              REQUEST_ID,
                              SHARED_RIDE_ID,
                              TRIP_TYPE,
                              CREATION_DATETIME,
                              ACTUAL_DEPARTURE_DATETIME,
                              APPROVE_DATE,
                              //5
                              KK_PERSONAL_NUMBER,
                              KK_FIO,
                              PASSENGER_ACTIVITY,
                              TRIP_PURPOSE,
                              REQUEST_STATUS,
                              //10
                              OWNERSHIP_OF_CAR,
                              MARRIAGE_CERTIFICATE_NUMBER,
                              CAR_REGISTRATION_NUMBER,
                              CAR_BRAND_NAME,
                              CAR_ENGINE_VOLUME,
                              OSAGO_NUMBER,
                              // 16
                              REQUEST_EXPECTED_COST_3,
                              REQUEST_EXPECTED_DISTANCE_2,
                              WAYPOINTS_COUNT,
                              WAYPOINTS_COUNT_WITH_CHECK_IN,
                              PAYMENT_PERIOD,
                              DESIRED_TIME,
                              RATING,
                              RATING_COMMENT,
                              // 42
                              DEPARTURE_ADDRESS,
                              DESTINATION_ADDRESS,
                              INTERMEDIATE_ADDRESSES,
                              PASSENGER_DEPARTMENT_1,
                              PASSENGER_DEPARTMENT_2,
                              PASSENGER_DEPARTMENT_3,
                              PASSENGER_DEPARTMENT_4,
                              PASSENGER_DEPARTMENT_5,
                              PASSENGER_DEPARTMENT_6
                             );
    
    private void useDefaultMapping(
            Map<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>> map,
            String name
                                  ) {
        var function = DefaultMapping.get(name);
        map.put(name, function);
    }
    
    @Override
    public Map<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>> get(
            PersonalUIVisibilityDTO personalUIVisibilityDTO
                                                                                          ) {
        var customMap = new LinkedHashMap<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>>();
        if (personalUIVisibilityDTO == null) {
            personalUIVisibilityDTO = resolver.getDefault();
        }
        if (personalUIVisibilityDTO.mvzVisible()) {
            useDefaultMapping(customMap, MVZ);
        }
        if (personalUIVisibilityDTO.requestIdVisible()) {
            useDefaultMapping(customMap, REQUEST_ID);
        }
        if (personalUIVisibilityDTO.orderPaymentFormationStartDateVisible()) {
            useDefaultMapping(customMap, ORDER_PAYMENT_FORMATION_START_DATE);
        }
        if (personalUIVisibilityDTO.kkPersonalNumberVisible()) {
            useDefaultMapping(customMap, KK_PERSONAL_NUMBER);
        }
        if (personalUIVisibilityDTO.passengerFioVisible()) {
            useDefaultMapping(customMap, KK_FIO);
        }
        if (personalUIVisibilityDTO.requestStatusVisible()) {
            useDefaultMapping(customMap, REQUEST_STATUS);
        }
        if (personalUIVisibilityDTO.ownershipOfCarVisible()) {
            useDefaultMapping(customMap, OWNERSHIP_OF_CAR);
        }
        if (personalUIVisibilityDTO.carEngineVolumeVisible()) {
            useDefaultMapping(customMap, CAR_ENGINE_VOLUME);
        }
        
        if (personalUIVisibilityDTO.tripFactPriceVisible()) {
            useDefaultMapping(customMap, REQUEST_EXPECTED_COST_3);
        }
        if (personalUIVisibilityDTO.actualRangeVisible()) {
            useDefaultMapping(customMap, REQUEST_EXPECTED_DISTANCE_2);
        }
       
        if (personalUIVisibilityDTO.desiredDateVisible()) {
            useDefaultMapping(customMap, DESIRED_DATE);
            useDefaultMapping(customMap, DESIRED_TIME);
        }
       
        if (personalUIVisibilityDTO.paymentPeriodVisible()) {
            useDefaultMapping(customMap, PAYMENT_PERIOD);
        }

        return customMap;
    }
    
    @Override
    public Map<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>> get() {
        return personalMapping;
    }
}
