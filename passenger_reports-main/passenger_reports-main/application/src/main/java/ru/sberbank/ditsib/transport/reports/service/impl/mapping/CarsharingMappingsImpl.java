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
class CarsharingMappingsImpl implements Mappings<IVisibilityDto> {
    
    public Map<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>> useDefaultMapping(
            String... names
                                                                                                        ) {
        var mapping = new LinkedHashMap<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>>();
        for (String name : names) {
            mapping.put(name, DefaultMapping.get(name));
        }
        return mapping;
    }
    
    private final Map<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>> carsharingMapping =
            useDefaultMapping(
                    SERVICE_NAME, // 0
                    REQUEST_ID,
                    REQUEST_STATUS,
                    RATING,
                    RATING_COMMENT,
                    EXPENSE_ITEM,
                    MVZ, // 5
                    LIMIT_ID,
                    LIMIT_PARENT_DEPARTMENT,
                    HIERARCHY_OF_THE_DEPARTMENT,
                    PASSENGER_PERSONNEL_NUMBER,
                    PASSENGER_FIO,
                    PASSENGER_ACTIVITY,
                    CUSTOMER_PERSONNEL_NUMBER,
                    CUSTOMER_FIO,
                    APPROVER_PERSONNEL_NUMBER,
                    APPROVER_FIO, // 23
                    TARIFF_ID,
                    TARIFF_BY_KM,
                    TARIFF_BY_MIN,
                    TARIFF_BY_WAITING_MIN,
                    TARIFF_CAR_SERVICE_COST, // 28
                    TARIFF_MIN_RIDE_COST,
                    TRIP_PURPOSE,
                    DEPARTURE_ADDRESS,
                    INTERMEDIATE_ADDRESSES,
                    DESTINATION_ADDRESS, // 33
                    CREATION_DATETIME,
                    EXECUTION_DATE_OF_REQUEST,
                    CLOSE_DATE_OF_REQUEST,
                    CONTROL_DATE,
                    DESIRED_TIME, // 38
                    DESIRED_DATE,
                    PASSENGER_MOBILE_PHONE,
                    FREE_WAITING_TIME,
                    REQUEST_DESCRIPTION,
                    VSP_GOSB_TB_ADDRESS, // 43
                    REQUEST_DECISION,
                    CONTRACTOR_NAME,
                    REQUEST_EXPECTED_DISTANCE,
                    TRIP_FACT_DISTANCE,
                    REQUEST_EXPECTED_COST, // 48
                    TRIP_FACT_PRICE,
                    REQUEST_EXPECTED_TIME,
                    TRIP_FACT_DURATION,
                    WAYPOINT_WAIT_TIME,
                    ACTUAL_WAITING_TIME, // 53
                    INTERMEDIATE_WAYPOINT_WAIT_TIME,
                    INTERMEDIATE_WAYPOINT_PRICE,
                    DEPARTURE_WAIT_TIME,
                    DEPARTURE_WAIT_PRICE,
                    TRIP_TYPE, // 58
                    CONTRAGENT_WAITING_TIME,
                    CONTRAGENT_DISTANCE,
                    CONTRAGENT_COST,
                    CHECK_0, // 62
                    CHECK_1,
                    CHECK_2,
                    CHECK_3,
                    CHECK_4,
                    CHECK_4A,
                    CHECK_5,
                    CHECK_6,
                    CHECK_7,
                    CHECK_8 //71
                             );
    
    public Map<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>> get(IVisibilityDto dto) {
        return carsharingMapping;
    }
    
    @Override
    public TransportTypeEnum transportType() {
        return TransportTypeEnum.CARSHARING;
    }
}
