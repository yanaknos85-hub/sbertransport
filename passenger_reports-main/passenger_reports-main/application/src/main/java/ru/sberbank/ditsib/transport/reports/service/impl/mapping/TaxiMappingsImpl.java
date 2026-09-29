package ru.sberbank.ditsib.transport.reports.service.impl.mapping;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.dto.IVisibilityDto;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryCheckDTO;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.TaxiTrip;
import ru.sberbank.ditsib.transport.reports.service.Mappings;
import ru.sberbank.ditsib.transport.reports.utils.Function4;

import java.util.LinkedHashMap;
import java.util.Map;

import static ru.sberbank.ditsib.transport.reports.service.impl.mapping.AllColumnNames.*;

@Component
@RequiredArgsConstructor
class TaxiMappingsImpl implements Mappings<IVisibilityDto> {
    
    @Override
    public TransportTypeEnum transportType() {
        return TransportTypeEnum.TAXI;
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
    
    public Map<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>> get(IVisibilityDto visibility) {
        return useDefaultMapping(SERVICE_NAME, // 0
                                 REQUEST_ID,
                                 SHARED_RIDE_ID,
                                 REQUEST_STATUS,
                                 RATING,
                                 RATING_COMMENT,
                                 ADDITIONAL_OPTIONS,
                                 COMMENT_FOR_DRIVER,
                                 EXPENSE_ITEM,
                                 MVZ,
                                 LIMIT_ID,
                                 LIMIT_PARENT_DEPARTMENT,
                                 PASSENGER_DEPARTMENT_1,
                                 PASSENGER_DEPARTMENT_2,
                                 PASSENGER_DEPARTMENT_3,
                                 PASSENGER_DEPARTMENT_4,
                                 PASSENGER_DEPARTMENT_5,
                                 PASSENGER_DEPARTMENT_6,
                                 HIERARCHY_OF_THE_DEPARTMENT,
        
                                 PASSENGER_PERSONNEL_NUMBER, // 20
                                 PASSENGER_FIO,
                                 PASSENGER_ACTIVITY,
                                 PASSENGER_POSITION,
                                 CUSTOMER_PERSONNEL_NUMBER,
                                 CUSTOMER_FIO,
                                 APPROVER_PERSONNEL_NUMBER,
                                 APPROVER_FIO,
        
                                 TARIFF_ID, // 28
                                 TARIFF_BY_KM,
                                 TARIFF_BY_MIN,
                                 TARIFF_BY_WAITING_MIN,
                                 TARIFF_CAR_SERVICE_COST,
                                 TARIFF_MIN_RIDE_COST,
        
                                 TRIP_PURPOSE, // 34
                                 DEPARTURE_ADDRESS,
                                 INTERMEDIATE_ADDRESSES,
                                 DESTINATION_ADDRESS,
                                 CREATION_DATETIME,
                                 EXECUTION_DATE_OF_REQUEST,
                                 CLOSE_DATE_OF_REQUEST,
                                 CONTROL_DATE,
                                 DESIRED_TIME, // 42
                                 DESIRED_DATE,
                                 PASSENGER_MOBILE_PHONE,
                                 REQUEST_DESCRIPTION,
                                 VSP_GOSB_TB_ADDRESS,
        
                                 REQUEST_DECISION, // 48
                                 CONTRACTOR_NAME,
        
                                 REQUEST_EXPECTED_DISTANCE, // 50
                                 TRIP_FACT_DISTANCE,
                                 REQUEST_EXPECTED_COST,
                                 TRIP_FACT_PRICE,
                                 REQUEST_EXPECTED_TIME,
                                 TRIP_FACT_DURATION,
                                 WAYPOINT_WAIT_TIME,
                                 ACTUAL_WAITING_TIME,
                                 INTERMEDIATE_WAYPOINT_WAIT_TIME,
                                 INTERMEDIATE_WAYPOINT_PRICE,
                                 DEPARTURE_WAIT_TIME,
                                 DEPARTURE_WAIT_PRICE,
                                 TRIP_TYPE,
                                 ACTUAL_REQUEST_PARAMS_UPDATE_DATE,
                                 ACTUAL_DEPARTURE_DATETIME,
        
                                 SHARED_REQUEST_ID, // 65
                                 COST_SHARE_PART,
                                 REQUEST_EXPECTED_COST_2,
                                 REQUEST_SAVING,
                                 ACTUAL_REQUEST_SAVING,
                                 TRIP_FACT_PRICE_2,
                                 SHARED_PASSENGERS_COUNT,
                                 TARIFF_ACCURACY,
                                 ORDER_DISTANCE_KM,
                                 TRIP_FACT_DISTANCE_2,
        
                                 CONTRAGENT_WAITING_TIME, // 75
                                 CONTRAGENT_DISTANCE,
                                 CONTRAGENT_COST,
                                 CHECK_0, //78
                                 CHECK_1,
                                 CHECK_2,
                                 CHECK_3,
                                 CHECK_4,
                                 CHECK_4A,
                                 CHECK_5,
                                 CHECK_6,
                                 CHECK_7,
                                 CHECK_8, //87
                                 CUSTOMER_STRUCTURE_UNIT,
                                 CODE_ORG_STRUCTURE_PASSENGER
                                );
    }
}
