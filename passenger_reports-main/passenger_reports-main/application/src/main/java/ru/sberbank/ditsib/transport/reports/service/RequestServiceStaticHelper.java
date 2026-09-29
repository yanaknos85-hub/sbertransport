package ru.sberbank.ditsib.transport.reports.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.util.CollectionUtils;
import ru.sberbank.ditsib.transport.constants.*;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.dto.*;
import ru.sberbank.ditsib.transport.reports.dto.filters.*;
import ru.sberbank.ditsib.transport.reports.dto.personalTransport.PersonalCarDTO;
import ru.sberbank.ditsib.transport.reports.dto.personalTransport.SharedRideKpiResultSet;
import ru.sberbank.ditsib.transport.reports.dto.publicTransport.TransportCompensationDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.*;
import ru.sberbank.ditsib.transport.reports.dto.taxi.ContractorResultSet;
import ru.sberbank.ditsib.transport.reports.dto.taxi.LimitResultSet;
import ru.sberbank.ditsib.transport.reports.dto.taxi.TaxiTripResultSet;
import ru.sberbank.ditsib.transport.reports.mappers.DriverMapper;
import ru.sberbank.ditsib.transport.reports.mappers.VehicleMapper;
import ru.sberbank.ditsib.transport.reports.model.*;
import ru.sberbank.ditsib.transport.reports.model.driversData.Driver;
import ru.sberbank.ditsib.transport.reports.model.mapping.*;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Сервис работы с заявками.
 */
@Slf4j
public class RequestServiceStaticHelper {
    
    public static Map<UUID, Organization> prepareMapForOrganization(EntityManager em, List<UUID> orgIds) {
        var query = em.createNativeQuery("SELECT * FROM reports.organization WHERE id IN :orgIds", Organization.class);
        query.setParameter("orgIds", orgIds);
        List<Organization> orgs = query.getResultList();
        
        return orgs.stream().collect(Collectors.toMap(Organization::getId, Function.identity()));
    }
    
    public static Map<UUID, TariffShortDTO> prepareMapForTariffsShort(EntityManager em, List<UUID> tariffIds) {
        Map<UUID, TariffShortDTO> mapForTariffs = new HashMap<>();
        if (tariffIds == null || tariffIds.isEmpty()) {
            return mapForTariffs;
        }
        var sqlString =
                """
                SELECT id, humanreadableid, work_group
                FROM reports.tariff
                WHERE id IN (:tariffIds)
                """;
        Query query = em.createNativeQuery(sqlString, "TariffShortDTO");
        query.setParameter("tariffIds", tariffIds);
        List<TariffShortDTO> tariffs = query.getResultList();
        
        mapForTariffs = tariffs.stream().collect(Collectors.toMap(TariffShortDTO::getId, Function.identity()));
        
        return mapForTariffs;
    }
    
    public static Map<UUID, OrganizationShortDTO> prepareMapForOrganizations(EntityManager em, List<UUID> organizationIds) {
        Map<UUID, OrganizationShortDTO> mapForOrganizations = new HashMap<>();
        if (organizationIds == null || organizationIds.isEmpty()) {
            return mapForOrganizations;
        }
        var sqlString =
                """
                SELECT id, official_name
                FROM reports.organization
                WHERE id IN (:organizationIds)
                """;
        Query query = em.createNativeQuery(sqlString, "OrganizationShortDTO");
        query.setParameter("organizationIds", organizationIds);
        List<OrganizationShortDTO> organizations = query.getResultList();
        
        mapForOrganizations = organizations.stream().collect(Collectors.toMap(OrganizationShortDTO::getId, Function.identity()));
        
        return mapForOrganizations;
    }
    
    public static Map<UUID, String> prepareMapForTariffsPersonal(EntityManager em, List<UUID> tariffIds) {
        Map<UUID, String> mapForTariffs = new HashMap<>();
        if (tariffIds == null || tariffIds.isEmpty()) {
            return mapForTariffs;
        }
        var sqlString =
                """
                SELECT id, humanreadableid, work_group
                FROM reports.tariff
                WHERE humanreadableid is not null and id IN (:tariffIds)
                """;
        Query query = em.createNativeQuery(sqlString, "TariffShortDTO");
        query.setParameter("tariffIds", tariffIds);
        List<TariffShortDTO> tariffs = query.getResultList();
        
        mapForTariffs = tariffs.stream().collect(Collectors.toMap(TariffShortDTO::getId, TariffShortDTO::getHumanReadableId));
        
        return mapForTariffs;
    }
    
    public static Map<UUID, EconomyDataDTO> prepareDataForCalculatingEconomyForTaxi(
            EntityManager em, Set<UUID> sharedRideIds
                                                                                   ) {
        var sqlString =
                """
                select cast(shared_ride_id as text), count(*), sum(passenger_count) - count(*) from reports.request
                where transport_type = 'TAXI' and shared_ride_id in (:sharedRideIds) and request_status not in ('TAXI_AWAITING_APPROVAL', 'TAXI_CANCELLED')
                group by shared_ride_id
                """;
        Query query = em.createNativeQuery(sqlString);
        query.setParameter("sharedRideIds", sharedRideIds);
        List<Object[]> result = query.getResultList();
        return result.stream().collect(Collectors.toMap(i -> UUID.fromString(i[0].toString()),
                                                        i -> EconomyDataDTO.builder().requestCount(Integer.parseInt(i[1].toString()))
                                                                           .joinedPassengerCount(Integer.parseInt(i[2].toString())).build()
                                                       ));
        
    }
    
    /**
     * Получаем информацию по кол-ву активных заявок (не в статусе отмена) в поездке
     *
     * @param em
     * @param sharedRideIds список sharedRideId
     *
     * @return ключ - sharedRideId, значение - кол-во активных заявок и кол-во присоединённых пассажиров
     */
    public static Map<UUID, EconomyDataDTO> prepareDataForCalculatingEconomyForPersonal(
            EntityManager em, Set<UUID> sharedRideIds
                                                                                       ) {
        var sqlString =
                """
                select cast(shared_ride_id as text), count(*), sum(passenger_count) - count(*) from reports.request
                where transport_type = 'PERSONAL' and shared_ride_id in (:sharedRideIds) and request_status != 'PERSONAL_CANCELLED'
                group by shared_ride_id
                """;
        Query query = em.createNativeQuery(sqlString);
        query.setParameter("sharedRideIds", sharedRideIds);
        List<Object[]> result = query.getResultList();
        return result.stream().collect(Collectors.toMap(i -> UUID.fromString(i[0].toString()),
                                                        i -> EconomyDataDTO.builder().requestCount(Integer.parseInt(i[1].toString()))
                                                                           .joinedPassengerCount(Integer.parseInt(i[2].toString())).build()
                                                       ));
        
    }
    
    public static Map<UUID, List<WaypointDTO>> prepareMapForWaypoints(EntityManager em, List<UUID> requestIds) {
        Map<UUID, List<WaypointDTO>> mapForWaypoints = new HashMap<>();
        if (requestIds == null || requestIds.isEmpty()) {
            return mapForWaypoints;
        }
        var sqlString =
                """
                SELECT request_id, ordering_index, building, city, country, house, region, street, structure, checkin_automatic, checkin_manual, wait_time, exist_in_vsp_tb_registry
                FROM reports.waypoint AS waypoint
                INNER JOIN reports.address AS address
                ON waypoint.address_id = address.id
                WHERE request_id IN (:requestIds)
                ORDER BY request_id, ordering_index
                """;
        Query query = em.createNativeQuery(sqlString, "ResultSetWaypointWithAddress");
        query.setParameter("requestIds", requestIds);
        List<ResultSetWaypointWithAddress> resultList = query.getResultList();
        
        mapForWaypoints = resultList.stream().collect(
                Collectors.groupingBy(ResultSetWaypointWithAddress::getRequest_id,
                                      Collectors.mapping(r ->
                                                                 WaypointDTO.builder()
                                                                            .country(r.getCountry())
                                                                            .region(r.getRegion())
                                                                            .city(r.getCity())
                                                                            .street(r.getStreet())
                                                                            .house(r.getHouse())
                                                                            .building(r.getBuilding())
                                                                            .structure(r.getStructure())
                                                                            .checkinAutomatic(r.getCheckin_automatic())
                                                                            .checkinManual(r.getCheckin_manual())
                                                                            .waitTime(Long.valueOf(r.getWait_time() == null ? 0 :
                                                                                                   r.getWait_time().toMinutes()).intValue())
                                                                            .existInVspGosbTbRegistry(r.getExist_in_vsp_tb_registry())
                                                                            .build(), Collectors.toList())));
        
        return mapForWaypoints;
    }
    
    public static Map<UUID, SharedRideKpiResultSet> prepareMapForSharedRidePersonal(EntityManager em, List<UUID> sharedRideIds) {
        Map<UUID, SharedRideKpiResultSet> mapForSharedRide = new HashMap<>();
        if (sharedRideIds == null || sharedRideIds.isEmpty()) {
            return mapForSharedRide;
        }
        var sqlString =
                """
                SELECT shared_ride.magenta_id AS id, kpi.total_cost, kpi.total_distance_km, kpi.total_time_min
                FROM reports.shared_ride AS shared_ride
                INNER JOIN reports.kpi AS kpi
                ON shared_ride.kpi = kpi.id
                WHERE magenta_id IN (:sharedRideIds)
                """;
        Query query = em.createNativeQuery(sqlString, "SharedRideKpiResultSet");
        query.setParameter("sharedRideIds", sharedRideIds);
        List<SharedRideKpiResultSet> resultList = query.getResultList();
        
        mapForSharedRide = resultList.stream().collect(Collectors.toMap(SharedRideKpiResultSet::getId, Function.identity()));
        
        return mapForSharedRide;
    }
    
    public static Map<UUID, TaxiTripResultSet> prepareMapForTaxiTripSingle(EntityManager em, List<UUID> ids) {
        Map<UUID, TaxiTripResultSet> mapForTaxiTrip = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return mapForTaxiTrip;
        }
        var sqlString =
                """
                SELECT id,
                       human_readable_id,
                       trip_fact_wait_time,
                       trip_fact_price,
                       trip_fact_distance,
                       trip_fact_duration,
                       trip_start_time,
                       fact_parameters_setting_time,
                       ride_id,
                       request_id,
                       registry_fact_waiting_time,
                       registry_human_readable_id,
                       registry_fact_cost,
                       registry_fact_distance,
                       registry_fact_payment
                FROM reports.taxi_trip
                WHERE request_id IN (:ids)
                """;
        Query query = em.createNativeQuery(sqlString, "TaxiTripResultSet");
        query.setParameter("ids", ids);
        List<TaxiTripResultSet> resultList = query.getResultList();
        
        mapForTaxiTrip = resultList.stream().collect(Collectors.toMap(TaxiTripResultSet::getRequestId, Function.identity()));
        
        return mapForTaxiTrip;
    }
    
    public static Map<UUID, TaxiTripResultSet> prepareMapForTaxiTripCoop(EntityManager em, Set<UUID> ids) {
        Map<UUID, TaxiTripResultSet> mapForTaxiTrip = new HashMap<>();
        if (ids == null || ids.isEmpty()) {
            return mapForTaxiTrip;
        }
        var sqlString =
                """
                SELECT id,
                       human_readable_id,
                       trip_fact_wait_time,
                       trip_fact_price,
                       trip_fact_distance,
                       trip_fact_duration,
                       trip_start_time,
                       fact_parameters_setting_time,
                       ride_id,
                       request_id,
                       registry_fact_waiting_time,
                       registry_human_readable_id,
                       registry_fact_cost,
                       registry_fact_distance,
                       registry_fact_payment
                FROM reports.taxi_trip
                WHERE ride_id IN (:ids)
                """;
        Query query = em.createNativeQuery(sqlString, "TaxiTripResultSet");
        query.setParameter("ids", ids);
        List<TaxiTripResultSet> resultList = query.getResultList();
        
        mapForTaxiTrip = resultList.stream().collect(Collectors.toMap(TaxiTripResultSet::getRideId, Function.identity()));
        
        return mapForTaxiTrip;
    }
    
    public static Map<UUID, LimitResultSet> prepareMapForLimit(EntityManager em, List<UUID> limitIds) {
        Map<UUID, LimitResultSet> map = new HashMap<>();
        if (limitIds == null || limitIds.isEmpty()) {
            return map;
        }
        var sqlString =
                """
                SELECT id, human_readable_id, department_id
                FROM reports.limit
                WHERE id IN (:limitIds)
                """;
        Query query = em.createNativeQuery(sqlString, "LimitResultSet");
        query.setParameter("limitIds", limitIds);
        List<LimitResultSet> result = query.getResultList();
        
        map = result.stream().collect(Collectors.toMap(LimitResultSet::getId, Function.identity()));
        
        return map;
    }
    
    public static Map<UUID, ContractorResultSet> prepareMapForContractor(EntityManager em, List<UUID> contractorIds) {
        Map<UUID, ContractorResultSet> map = new HashMap<>();
        if (contractorIds == null || contractorIds.isEmpty()) {
            return map;
        }
        var sqlString =
                """
                SELECT id, name
                FROM reports.contractor
                WHERE id IN (:contractorIds)
                """;
        Query query = em.createNativeQuery(sqlString, "ContractorResultSet");
        query.setParameter("contractorIds", contractorIds);
        List<ContractorResultSet> result = query.getResultList();
        
        map = result.stream().collect(Collectors.toMap(ContractorResultSet::getId, Function.identity()));
        
        return map;
    }
    
    public static Map<UUID, TripPurposeDTO> prepareMapForPurpose(EntityManager em, List<UUID> purposeIds) {
        Map<UUID, TripPurposeDTO> mapForPurpose = new HashMap<>();
        if (purposeIds == null || purposeIds.isEmpty()) {
            return mapForPurpose;
        }
        var sqlString =
                """
                SELECT id, label
                FROM reports.trip_purpose
                WHERE id IN (:purposeIds)
                """;
        Query query = em.createNativeQuery(sqlString, "TripPurposeDTO");
        query.setParameter("purposeIds", purposeIds);
        List<TripPurposeDTO> cars = query.getResultList();
        
        mapForPurpose = cars.stream().collect(Collectors.toMap(TripPurposeDTO::getId, Function.identity()));
        
        return mapForPurpose;
    }
    
    public static Map<UUID, DepartmentShortDTO> prepareMapForDepartments(EntityManager em, List<UUID> departmentIds) {
        Map<UUID, DepartmentShortDTO> mapForDepartments = new HashMap<>();
        if (departmentIds == null || departmentIds.isEmpty()) {
            return mapForDepartments;
        }
        var sqlString =
                """
                SELECT id, code, department_name
                FROM reports.department
                WHERE id IN (:departmentIds)
                """;
        Query query = em.createNativeQuery(sqlString, "DepartmentShortDTO");
        query.setParameter("departmentIds", departmentIds);
        List<DepartmentShortDTO> departments = query.getResultList();
        
        mapForDepartments = departments.stream().collect(Collectors.toMap(DepartmentShortDTO::getId, Function.identity()));
        
        return mapForDepartments;
    }
    
    public static Map<UUID, PersonalCarDTO> prepareMapForCars(EntityManager em, List<UUID> carIds) {
        Map<UUID, PersonalCarDTO> mapForCars = new HashMap<>();
        if (carIds == null || carIds.isEmpty()) {
            return mapForCars;
        }
        var sqlString =
                """
                SELECT id, brand_name, model, reg_number, reg_cert, engine_volume, insurance_number, corporate_user_id, owner_info
                FROM reports.personal_auto
                WHERE id IN (:carIds)
                """;
        Query query = em.createNativeQuery(sqlString, "PersonalCarDTO");
        query.setParameter("carIds", carIds);
        List<PersonalCarDTO> cars = query.getResultList();
        
        mapForCars = cars.stream().collect(Collectors.toMap(PersonalCarDTO::getId, Function.identity()));
        
        return mapForCars;
    }
    
    public static Map<UUID, EmployeeDTO> prepareMapForEmployees(EntityManager em, List<UUID> employeesIds) {
        Map<UUID, EmployeeDTO> mapForEmployees = new HashMap<>();
        if (employeesIds == null || employeesIds.isEmpty()) {
            return mapForEmployees;
        }
        String sqlString =
                """
                          SELECT emp.id as id,
                                human_readable_id,
                                user_id,
                                personnel_number,
                                first_name,
                                last_name,
                                patronymic,
                                itinerant_type,
                                marriage_certificate_number,
                                position_id,
                                pos.position_name as position_name,
                                organization_id,
                                department_id,
                                mobile_phone
                          FROM reports.employee emp
                          LEFT JOIN reports.position pos
                          ON emp.position_id = pos.id
                          WHERE emp.id in (:employeeIds)
                """;
        Query query = em.createNativeQuery(sqlString, "EmployeeDTO");
        query.setParameter("employeeIds", employeesIds);
        List<EmployeeDTO> employees = query.getResultList();
        
        mapForEmployees = employees.stream()
                                   .collect(Collectors.toMap(EmployeeDTO::getId, Function.identity()));
        
        return mapForEmployees;
    }
    
    public static Map<UUID, List<TransportCompensationDTO>> prepareMapForTransportCompensation(EntityManager em, List<UUID> requestIds) {
        Map<UUID, List<TransportCompensationDTO>> mapForTransportCompensation = new HashMap<>();
        if (requestIds == null || requestIds.isEmpty()) {
            return mapForTransportCompensation;
        }
        requestIds.forEach(r -> mapForTransportCompensation.put(r, new ArrayList<>()));
        var sqlString =
                """
                SELECT id, compensation_type, transport_type, tickets_cost, tickets_count, tickets_expiration_start, tickets_expiration_end, request_id, attached_document_id
                FROM reports.transport_compensation
                WHERE request_id IN (:requestIds)
                """;
        Query query = em.createNativeQuery(sqlString, "TransportCompensationDTO");
        query.setParameter("requestIds", requestIds);
        List<TransportCompensationDTO> transportCompensations = query.getResultList();
        
        transportCompensations.forEach(tc -> mapForTransportCompensation.get(tc.getRequestId()).add(tc));
        
        return mapForTransportCompensation;
    }
    
    public static List<PaymentDataDTO> getPaymentDataList(PersonalResponseSqlResultSetMapping rec) {
        List<PaymentDataDTO> paymentDataList = new ArrayList<>();
        if (rec.getPaymentTypeCodeMain() == null &&
            rec.getPaymentPriceMain() == null &&
            rec.getPaymentTypeCodeOptional() == null &&
            rec.getPaymentPriceOptional() == null &&
            rec.getPaymentTypeCodeInsurance() == null &&
            rec.getPaymentPriceInsurance() == null) {
            return paymentDataList;
        }
        PaymentDataDTO paymentMain = PaymentDataDTO.builder()
                                                   .paymentTypeCode(PaymentTypeCode.parse(rec.getPaymentTypeCodeMain()))
                                                   .paymentPrice(
                                                           rec.getPaymentPriceMain())
                                                   .build();
        paymentDataList.add(paymentMain);
        if (rec.getPaymentTypeCodeOptional() != null) {
            PaymentDataDTO paymentOptional = PaymentDataDTO.builder()
                                                           .paymentTypeCode(PaymentTypeCode.parse(rec.getPaymentTypeCodeOptional()))
                                                           .paymentPrice(rec.getPaymentPriceOptional())
                                                           .build();
            
            paymentDataList.add(paymentOptional);
        }
        if (rec.getPaymentTypeCodeInsurance() != null) {
            PaymentDataDTO paymentInsurance = PaymentDataDTO.builder()
                                                            .paymentTypeCode(PaymentTypeCode.parse(rec.getPaymentTypeCodeInsurance()))
                                                            .paymentPrice(
                                                                    rec.getPaymentPriceInsurance())
                                                            .build();
            
            paymentDataList.add(paymentInsurance);
        }
        return paymentDataList;
    }
    
    public static <T extends ReportResponseDTO> Page<T> enrichDepartmentsTree(Page<T> requests) {
        // Планировалась реализация по обогащению объекта заявок иерархией подразделений, но сменились приоритеты 2023-05-25
        return requests;
    }
    
    public static LocalDateTime withTimeZone(LocalDateTime time, String timeZone) {
        if (time != null && timeZone != null) {
            ZonedDateTime zdt = time.atZone(ZoneOffset.UTC);
            zdt = zdt.withZoneSameInstant(ZoneId.of(timeZone));
            String str = zdt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return LocalDateTime.parse(str);
        } else {
            return time;
        }
    }
    
    public static DataAndCountQueries prepareFindPersonalRequestsQuery(
            EntityManager em, DepartmentService departmentService,
            RequestForPersonalReportDTO requestDTO
                                                                      ) {
        
        Map<String, Object> parameters = new HashMap<>();
        
        StringBuilder qbData = new StringBuilder();
        StringBuilder qbCount = new StringBuilder();
        
        qbCount.append(
                """
                SELECT count(*) AS cnt
                """);
        
        qbData.append("""
                      SELECT CAST(id as uuid) as id,
                        humanreadableid,
                        CAST(author_id as uuid) as author_id,
                        CAST(passenger_id as uuid) as passenger_id,
                        CAST(personal_car as uuid) as personal_car,
                        creation_time,
                        desired_date,
                        approval_date,
                        order_payment_formation_start_date,
                        transport_type,
                        request_status,
                        request_status_code,
                        CAST(purpose_id as uuid) as purpose_id,
                        coop_trip,
                        CAST(shared_ride_id as uuid) as shared_ride_id,
                        passenger_count,
                        expected_cost,
                        expected_distance,
                        expected_time,
                        payment_price_insurance,
                        payment_price_main,
                        payment_price_optional,
                        payment_type_code_insurance,
                        payment_type_code_main,
                        payment_type_code_optional,
                        rating_mark,
                        rating_comment,
                        change_date,
                        time_zone,
                        cost_share_part,
                        savings_cash,
                        savings_procents,
                        shared_ride_owner,
                        number_passengers_joined,
                        additional_sum,
                        CAST(tariff_id as uuid) as tariff_id,
                        CAST(employee_driver_id as uuid) as employee_driver_id,
                        passenger_department1,
                        passenger_department2,
                        passenger_department3,
                        passenger_department4,
                        passenger_department5,
                        passenger_department6,
                        departure_address,
                        intermediate_addresses,
                        destination_address,
                        cost_center,
                        joined_passengers,
                        source,
                        min_taxi_tariff_cost,
                        comment_for_purpose,
                        deadline_state,
                        finished_time,
                        request_closed_datetime,
                        CAST(executor_group_id as uuid) as executor_group_id,
                        executor_group_name,
                        trip_start_time,
                        deadline
                      """);
        
        StringBuilder qb = preparePersonalFilters(departmentService, requestDTO, parameters);
        
        qbData.append(qb);
        qbCount.append(qb);
        prepareSortAndPagination(qbData, requestDTO.getSortSetting(), requestDTO.getPageSetting());
        
        String sqlString = qbData.toString();
        log.debug("prepareFindPersonalRequestsQuery(), sqlString = {}", sqlString);
        
        Query queryData = em.createNativeQuery(sqlString, Map.class);
        Query queryCount = em.createNativeQuery(qbCount.toString());
        
        parameters.forEach((paramName, paramValue) ->
                           {
                               queryData.setParameter(paramName, paramValue);
                               queryCount.setParameter(paramName, paramValue);
                           });
        if (log.isDebugEnabled()) {
            parameters.forEach((paramName, paramValue) -> log.debug("paramName = {}, paramValue = {}", paramName, paramValue));
        }
        
        return new DataAndCountQueries(queryData, queryCount);
    }
    
    public static Query prepareFindPersonalPaymentRequestsQuery(
            EntityManager em, DepartmentService departmentService,
            RequestForPersonalReportDTO requestDTO
                                                               ) {
        
        Map<String, Object> parameters = new HashMap<>();
        
        StringBuilder qbData = new StringBuilder();
        
        qbData.append("""
                      SELECT CAST(id as varchar) as id
                      """);
        
        StringBuilder qb = preparePersonalFilters(departmentService, requestDTO, parameters);
        
        qbData.append(qb);
        
        prepareSortAndPagination(qbData, requestDTO.getSortSetting(), requestDTO.getPageSetting());
        
        String sqlString = qbData.toString();
        log.debug("prepareFindPersonalPaymentRequestsQuery(), sqlString = {}", sqlString);
        
        Query queryData = em.createNativeQuery(sqlString);
        
        parameters.forEach(queryData::setParameter);
        
        if (log.isDebugEnabled()) {
            parameters.forEach((paramName, paramValue) -> log.debug("paramName = {}, paramValue = {}", paramName, paramValue));
        }
        
        return queryData;
    }
    
    public static DataAndCountQueries prepareFindTaxiRequestsQuery(
            EntityManager em, DepartmentService departmentService,
            RequestForTaxiReportDTO requestDTO
                                                                  ) {
        Map<String, Object> parameters = new HashMap<>();
        
        StringBuilder qb = new StringBuilder();
        StringBuilder qbData = new StringBuilder();
        StringBuilder qbCount = new StringBuilder();
        
        qbCount.append(
                """
                SELECT count(*) AS cnt
                """);
        
        qbData.append("""
                      SELECT CAST(request.id as uuid) as id,
                        request.humanreadableid,
                        CAST(request.author_id as uuid) as author_id,
                        CAST(request.passenger_id as uuid) as passenger_id,
                        CAST(request.personal_car as uuid) as personal_car,
                        request.creation_time,
                        request.desired_date,
                        request.approval_date,
                        request.transport_type,
                        request.request_status,
                        request.request_status_code,
                        request.purpose_id as purpose_id,
                        request.coop_trip,
                        CAST(request.ride_id as uuid) as ride_id,
                        request.passenger_count,
                        request.expected_cost,
                        request.expected_distance,
                        request.expected_time,
                        request.rating_mark,
                        request.rating_comment,
                        request.change_date,
                        request.time_zone,
                        request.cost_share_part,
                        request.savings_cash,
                        request.savings_procents,
                        request.shared_ride_owner,
                        request.number_passengers_joined,
                        CAST(request.tariff_id as uuid) as tariff_id,
                        request.trip_class,
                        request.approval_state,
                        CAST(request.approved_by_id as uuid) as approved_by_id,
                        CAST(request.contractor_id as uuid) as contractor_id,
                        request.comment_for_driver,
                        CAST(request.driver as varchar) as driver,
                        CAST(request.vehicle as varchar) as vehicle,
                        request.finished_time,
                        request.passenger_department1,
                        request.passenger_department2,
                        request.passenger_department3,
                        request.passenger_department4,
                        request.passenger_department5,
                        request.passenger_department6,
                        request.departure_address,
                        request.intermediate_addresses,
                        request.destination_address,
                        request.resolution,
                        request.organization_id,
                        request.limit_id,
                        request.deadline,
                        request.deadline_state,
                        request.driver_arrived_datetime,
                        request.cost_center,
                        request.request_closed_datetime,
                        request.joined_passengers,
                        request.source,
                        request.min_taxi_tariff_cost,
                        request.comment_for_purpose,
                        contract.contract_number,
                        request.executor_group_id,
                        request.executor_group_name
                      """);
        
        if (requestDTO.getRegistryFactPayment() != null) {
            qb.append(
                    """
                     FROM reports.request as request
                     LEFT JOIN reports.tariff as tariff ON request.tariff_id = tariff.id
                     LEFT JOIN reports.contract as contract ON tariff.contract_id = contract.id
                     LEFT JOIN reports.taxi_trip as taxi_trip ON request.id = taxi_trip.request_id
                    WHERE request.transport_type = 'TAXI' AND taxi_trip.registry_fact_payment = :registryFactPayment
                    """);
            
            boolean registryFactPayment = "Да".equals(requestDTO.getRegistryFactPayment());
            
            parameters.put("registryFactPayment", registryFactPayment);
        } else {
            qb.append(
                    """
                     FROM reports.request as request
                     LEFT JOIN reports.tariff as tariff ON request.tariff_id = tariff.id
                     LEFT JOIN reports.contract as contract ON tariff.contract_id = contract.id
                    WHERE request.transport_type = 'TAXI'
                    """);
        }
        
        // Список Ид тарифов
        if (requestDTO.getTariffIdSet() != null && !requestDTO.getTariffIdSet().isEmpty()) {
            qb.append(" AND request.tariff_id IN (:tariff_ids)");
            parameters.put("tariff_ids", requestDTO.getTariffIdSet());
        }
        
        // Список контрагентов
        if (requestDTO.getContractorSet() != null && !requestDTO.getContractorSet().isEmpty()) {
            qb.append(" AND request.contractor_id IN (:contractor_ids)");
            parameters.put("contractor_ids", requestDTO.getContractorSet());
        }
        
        // Список оценок для фильтрации, 0 - без оценки
        if (requestDTO.getRatingMarkSet() != null && !requestDTO.getRatingMarkSet().isEmpty()) {
            qb.append(requestDTO.getRatingMarkSet().contains(0) ? " AND (request.rating_mark IS NULL OR request.rating_mark IN (:rating_marks))"
                                                                : " AND request.rating_mark IN (:rating_marks)");
            parameters.put("rating_marks", requestDTO.getRatingMarkSet());
        }
        
        // Желаемая дата поездки, диапазон
        if (requestDTO.getDesiredDateRange() != null) {
            if ((requestDTO.getDesiredDateRange().getStart() != null) &&
                (requestDTO.getDesiredDateRange().getEnd() != null)) {
                qb.append(" AND request.desired_date BETWEEN :desired_date_start AND :desired_date_end");
                parameters.put("desired_date_start", requestDTO.getDesiredDateRange().getStart());
                parameters.put("desired_date_end", requestDTO.getDesiredDateRange().getEnd());
            }
            if ((requestDTO.getDesiredDateRange().getStart() != null) &&
                (requestDTO.getDesiredDateRange().getEnd() == null)) {
                qb.append(" AND request.desired_date > :desired_date_start");
                parameters.put("desired_date_start", requestDTO.getDesiredDateRange().getStart());
            }
            if ((requestDTO.getDesiredDateRange().getStart() == null) &&
                (requestDTO.getDesiredDateRange().getEnd() != null)) {
                qb.append(" AND request.desired_date <= :desired_date_end");
                parameters.put("desired_date_end", requestDTO.getDesiredDateRange().getEnd());
            }
        }
        
        // Дистанция поездки факт (диапазон)
        if (requestDTO.getFactDistance() != null) {
            if (requestDTO.getFactDistance().getStart() != null && requestDTO.getFactDistance().getEnd() == null) {
                qb.append(" AND request.id IN (SELECT id FROM reports.taxi_trip WHERE trip_fact_distance >= :trip_fact_distance_start)");
                parameters.put("trip_fact_distance_start", requestDTO.getFactDistance().getStart());
            }
            if (requestDTO.getFactDistance().getStart() == null && requestDTO.getFactDistance().getEnd() != null) {
                qb.append(" AND request.id IN (SELECT id FROM reports.taxi_trip WHERE trip_fact_distance <= :trip_fact_distance_end)");
                parameters.put("trip_fact_distance_end", requestDTO.getFactDistance().getEnd());
            }
            if (requestDTO.getFactDistance().getStart() != null && requestDTO.getFactDistance().getEnd() != null) {
                qb.append(" AND request.id IN (SELECT id FROM reports.taxi_trip WHERE trip_fact_distance BETWEEN :trip_fact_distance_start AND " +
                          ":trip_fact_distance_end)");
                parameters.put("trip_fact_distance_start", requestDTO.getFactDistance().getStart());
                parameters.put("trip_fact_distance_end", requestDTO.getFactDistance().getEnd());
            }
        }
        
        // Флаг совместной поездки
        if (requestDTO.getCoopTrip() != null) {
            qb.append(" AND request.coop_trip = :coop_trip");
            parameters.put("coop_trip", requestDTO.getCoopTrip());
        }
        
        // ID совместной поездки
        if (requestDTO.getSharedRideId() != null) {
            qb.append(" AND request.shared_ride_id = :shared_ride_id");
            parameters.put("shared_ride_id", requestDTO.getSharedRideId());
        }
        
        // Количество пассажиров
        if (requestDTO.getPassengerCountSet() != null && !requestDTO.getPassengerCountSet().isEmpty()) {
            qb.append(" AND request.passenger_count in (:passengerCountSet)");
            parameters.put("passengerCountSet", requestDTO.getPassengerCountSet());
        }
        
        //Группа исполнителей
        if (!CollectionUtils.isEmpty(requestDTO.getExecutorGroupIds())) {
            qb.append(" AND request.executor_group_id IN :executor_group_ids");
            parameters.put("executor_group_ids", requestDTO.getExecutorGroupIds());
        }
        
        Set<String> employeeOrganizationSet = requestDTO.getEmployeeOrganizationSet();
        boolean organizationsIsNotAll = employeeOrganizationSet != null && !employeeOrganizationSet.isEmpty() && !employeeOrganizationSet.contains(
                "all");
        // Организации пассажира
        if (organizationsIsNotAll) {
            qb.append(" AND request.organization_id IN (:organization_ids)");
            parameters.put("organization_ids", employeeOrganizationSet.stream().map(UUID::fromString).collect(Collectors.toSet()));
        }
        
        // Организация
        if (requestDTO.getOrganizationId() != null && !organizationsIsNotAll) {
            qb.append(" AND request.organization_id = :organization_id");
            parameters.put("organization_id", requestDTO.getOrganizationId());
        }
        
        // Человекочитаемый идентификатор заявки
        if (requestDTO.getRequestHumanId() != null && !requestDTO.getRequestHumanId().isEmpty()) {
            qb.append(" AND lower(request.humanreadableid) like lower(:humanreadableidLikeExpression)");
            String humanreadableidLikeExpression = "%" + requestDTO.getRequestHumanId() + "%";
            parameters.put("humanreadableidLikeExpression", humanreadableidLikeExpression);
        }
        
        // Статусы поездки
        if (requestDTO.getRequestStatusSet() != null && !requestDTO.getRequestStatusSet().isEmpty()) {
            qb.append(" AND request.request_status IN (:request_statuses)");
            parameters.put("request_statuses", requestDTO.getRequestStatusSet().stream().map(Enum::name).collect(Collectors.toList()));
        }
        
        // Дата создания поездки, диапазон
        if (requestDTO.getCreationDate() != null) {
            if ((requestDTO.getCreationDate().getStart() != null) &&
                (requestDTO.getCreationDate().getEnd() != null)) {
                qb.append(" AND request.creation_time BETWEEN :creation_time_start AND :creation_time_end");
                parameters.put("creation_time_start", requestDTO.getCreationDate().getStart());
                parameters.put("creation_time_end", requestDTO.getCreationDate().getEnd());
            }
            if ((requestDTO.getCreationDate().getStart() != null) &&
                (requestDTO.getCreationDate().getEnd() == null)) {
                qb.append(" AND request.creation_time > :creation_time_start");
                parameters.put("creation_time_start", requestDTO.getCreationDate().getStart());
            }
            if ((requestDTO.getCreationDate().getStart() == null) &&
                (requestDTO.getCreationDate().getEnd() != null)) {
                qb.append(" AND request.creation_time <= :creation_time_end");
                parameters.put("creation_time_end", requestDTO.getCreationDate().getEnd());
            }
        }
        
        // ID цели поездки
        if (requestDTO.getPurposeSet() != null && !requestDTO.getPurposeSet().isEmpty()) {
            qb.append(" AND request.purpose_id IN (:purpose_ids)");
            parameters.put("purpose_ids", requestDTO.getPurposeSet().stream().map(TripPurposeDTO::getId).collect(Collectors.toList()));
        }
        
        // Стоимость поездки (диапазон), с фронта рубли, на бэке копейки
        if (requestDTO.getExpectedCost() != null) {
            if (requestDTO.getExpectedCost().getStart() != null && requestDTO.getExpectedCost().getEnd() == null) {
                qb.append(" AND request.expected_cost >= :expected_cost_start");
                parameters.put("expected_cost_start", requestDTO.getExpectedCost().getStart() * 100);
            }
            if (requestDTO.getExpectedCost().getStart() == null && requestDTO.getExpectedCost().getEnd() != null) {
                qb.append(" AND request.expected_cost <= :expected_cost_end");
                parameters.put("expected_cost_end", requestDTO.getExpectedCost().getEnd() * 100);
            }
            if (requestDTO.getExpectedCost().getStart() != null && requestDTO.getExpectedCost().getEnd() != null) {
                qb.append(" AND request.expected_cost BETWEEN :expected_cost_start AND :expected_cost_end");
                parameters.put("expected_cost_start", requestDTO.getExpectedCost().getStart() * 100);
                parameters.put("expected_cost_end", requestDTO.getExpectedCost().getEnd() * 100);
            }
        }
        
        // Длительность поездки (диапазон)
        if (requestDTO.getExpectedDistance() != null) {
            if (requestDTO.getExpectedDistance().getStart() != null && requestDTO.getExpectedDistance().getEnd() != null) {
                qb.append(" AND request.expected_distance BETWEEN :expected_distance_start AND :expected_distance_end");
                parameters.put("expected_distance_start", requestDTO.getExpectedDistance().getStart());
                parameters.put("expected_distance_end", requestDTO.getExpectedDistance().getEnd());
            }
            if (requestDTO.getExpectedDistance().getStart() != null && requestDTO.getExpectedDistance().getEnd() == null) {
                qb.append(" AND request.expected_distance >= :expected_distance_start");
                parameters.put("expected_distance_start", requestDTO.getExpectedDistance().getStart());
            }
            if (requestDTO.getExpectedDistance().getStart() == null && requestDTO.getExpectedDistance().getEnd() != null) {
                qb.append(" AND request.expected_distance <= :expected_distance_end");
                parameters.put("expected_distance_end", requestDTO.getExpectedDistance().getEnd());
            }
        }
        
        // Состояние проверки контрольного срока
        if (requestDTO.getDeadlineState() != null) {
            if (requestDTO.getDeadlineState()) {
                qb.append(" AND request.deadline_state = :deadline_state");
                parameters.put("deadline_state", DeadlineState.RED.name());
            } else {
                qb.append(" AND (request.deadline_state = :deadline_state OR request.deadline_state is null)");
                parameters.put("deadline_state", DeadlineState.NONE.name());
            }
        }
        
        // Место возникновения затрат
        if (requestDTO.getCostCenter() != null && !requestDTO.getCostCenter().isEmpty()) {
            qb.append(" AND request.cost_center = :cost_center");
            parameters.put("cost_center", requestDTO.getCostCenter());
        }
        
        // Добавление фильтров по поздразделению
        addDepartmentsFilter(requestDTO, parameters, qb);
        
        StringBuilder sroQueryBuilder = new StringBuilder();
        
        if (requestDTO.getSharedRideOwnerFIO() != null && !requestDTO.getSharedRideOwnerFIO().isEmpty()) {
            sroQueryBuilder.append("SELECT id FROM reports.employee WHERE");
            var fullName = requestDTO.getSharedRideOwnerFIO().replaceAll("\\s+", " ");
            switch (fullName.split(" ").length) {
                case 1 -> sroQueryBuilder.append(
                        " (lower(last_name) LIKE lower(:sharedRideOwnerFullNameLikeExpression) OR lower(first_name) LIKE lower" +
                        "(:sharedRideOwnerFullNameLikeExpression) " +
                        "OR lower(patronymic) LIKE lower(:sharedRideOwnerFullNameLikeExpression))");
                case 2 -> sroQueryBuilder.append(" (lower(concat(last_name, ' ', first_name)) LIKE lower" +
                                                 "(:sharedRideOwnerFullNameLikeExpression)" +
                                                 " " +
                                                 "OR" +
                                                 " " +
                                                 "lower(concat(first_name, ' ', last_name)) LIKE lower(:sharedRideOwnerFullNameLikeExpression) OR " +
                                                 "lower(concat(first_name, ' ', patronymic)) LIKE lower(:sharedRideOwnerFullNameLikeExpression))");
                default -> sroQueryBuilder.append(
                        " (lower(concat(last_name, ' ', first_name, ' ', patronymic)) LIKE lower(:sharedRideOwnerFullNameLikeExpression)" +
                        " OR lower(concat(first_name, ' ', patronymic, ' ', last_name)) LIKE lower(:sharedRideOwnerFullNameLikeExpression))");
            }
            var sharedRideOwnerFullNameLikeExpression = "%" + fullName + "%";
            parameters.put("sharedRideOwnerFullNameLikeExpression", sharedRideOwnerFullNameLikeExpression);
        }
        
        //Инициатор совместной поездки
        if (!sroQueryBuilder.isEmpty()) {
            qb.append(
                    (" AND request.shared_ride_id IN (SELECT shared_ride_id from reports.request WHERE author_id IN (%s) AND request.coop_trip = true AND " +
                     "shared_ride_owner = true)").formatted(sroQueryBuilder.toString()));
        }
        
        StringBuilder empQueryBuilder = new StringBuilder();
        
        if (requestDTO.getEmployeeFIO() != null && !requestDTO.getEmployeeFIO().isEmpty()) {
            var fullName = requestDTO.getEmployeeFIO().replaceAll("\\s+", " ");
            switch (fullName.split(" ").length) {
                case 1 -> empQueryBuilder.append(
                        " AND (lower(last_name) LIKE lower(:fullNameLikeExpression) OR lower(first_name) LIKE lower(:fullNameLikeExpression) " +
                        "OR lower(patronymic) LIKE lower(:fullNameLikeExpression))");
                case 2 -> empQueryBuilder.append(" AND (lower(concat(last_name, ' ', first_name)) LIKE lower(:fullNameLikeExpression) OR " +
                                                 "lower(concat(first_name, ' ', last_name)) LIKE lower(:fullNameLikeExpression) OR " +
                                                 "lower(concat(first_name, ' ', patronymic)) LIKE lower(:fullNameLikeExpression))");
                default -> empQueryBuilder.append(
                        " AND (lower(concat(last_name, ' ', first_name, ' ', patronymic)) LIKE lower(:fullNameLikeExpression)" +
                        " OR lower(concat(first_name, ' ', patronymic, ' ', last_name)) LIKE lower(:fullNameLikeExpression))");
            }
            var fullNameLikeExpression = "%" + fullName + "%";
            parameters.put("fullNameLikeExpression", fullNameLikeExpression);
        }
        
        if (requestDTO.getEmployeeDepartmentSet() != null && !requestDTO.getEmployeeDepartmentSet().isEmpty()) {
            Set<UUID> employeeDepartmentSet = departmentService.getDepartmentWithAllChildren((HashSet<UUID>) requestDTO.getEmployeeDepartmentSet());
            empQueryBuilder.append(" AND department_id in (:department_ids)");
            parameters.put("department_ids", employeeDepartmentSet);
        }
        
        // Табельный № заявителя
        if (requestDTO.getPersonnelNumber() != null) {
            empQueryBuilder.append(" AND personnel_number = :personnel_number");
            parameters.put("personnel_number", requestDTO.getPersonnelNumber());
        }
        
        // ОЕ Подразделения
        if (requestDTO.getDepartmentCode() != null) {
            empQueryBuilder.append(" AND department_id IN (SELECT id from reports.department WHERE code = :code)");
            parameters.put("code", requestDTO.getDepartmentCode());
        }
        
        if (!empQueryBuilder.isEmpty()) {
            qb.append(" AND request.passenger_id IN (SELECT id FROM reports.employee WHERE 1 = 1");
            qb.append(empQueryBuilder);
            qb.append(")");
        }
        
        // Причина отмены
        if (requestDTO.getRequestStatusCodes() != null && !requestDTO.getRequestStatusCodes().isEmpty()) {
            qb.append(" AND request.request_status_code IN :request_status_codes");
            parameters.put("request_status_codes", requestDTO.getRequestStatusCodes());
        }
        
        // Контрольный срок
        if (requestDTO.getDeadlineState() != null) {
            if (requestDTO.getDeadlineState()) {
                qb.append(" AND request.deadline_state = :deadline_state");
                parameters.put("deadline_state", DeadlineState.RED.name());
            } else {
                qb.append(" AND (request.deadline_state = :deadline_state OR request.deadline_state is null)");
                parameters.put("deadline_state", DeadlineState.NONE.name());
            }
        }
        
        // Экономия
        if (requestDTO.getSavings() != null) {
            if (requestDTO.getSavings()) {
                qb.append(" AND request.savings_cash > 0");
            } else {
                qb.append(" AND (request.savings_cash = 0 OR request.savings_cash is null)");
            }
        }
        
        // Номер договора
        if (requestDTO.getContractNumber() != null) {
            qb.append(" AND contract.contract_number = :contract_number");
            parameters.put("contract_number", requestDTO.getContractNumber());
        }
        
        // Дата закрытия обращения
        if (requestDTO.getRequestClosedDatetime() != null) {
            qb.append(" AND request.request_closed_datetime BETWEEN :request_closed_datetime_start AND :request_closed_datetime_end");
            parameters.put("request_closed_datetime_start", requestDTO.getRequestClosedDatetime().getStart());
            parameters.put("request_closed_datetime_end", requestDTO.getRequestClosedDatetime().getEnd());
        }
        
        // Вид тарифа
        if (requestDTO.getTripClass() != null && !requestDTO.getTripClass().isEmpty()) {
            qb.append(" AND request.trip_class in :trip_classes ");
            parameters.put("trip_classes", requestDTO.getTripClass());
        }
        
        qbData.append(qb);
        qbCount.append(qb);
        
        // Сортировка
        prepareSortAndPagination(qbData, requestDTO.getSortSetting(), requestDTO.getPageSetting());
        
        String sqlString = qbData.toString();
        log.debug("prepareFindTaxiRequestsQuery(), sqlString = {}", sqlString);
        
        Query queryData = em.createNativeQuery(sqlString, Map.class);
        Query queryCount = em.createNativeQuery(qbCount.toString());
        
        parameters.forEach((paramName, paramValue) ->
                           {
                               queryData.setParameter(paramName, paramValue);
                               queryCount.setParameter(paramName, paramValue);
                           });
        if (log.isDebugEnabled()) {
            parameters.forEach((paramName, paramValue) -> log.debug("paramName = {}, paramValue = {}", paramName, paramValue));
        }
        
        return new DataAndCountQueries(queryData, queryCount);
    }
    
    private static void addDepartmentsFilter(RequestReportDTO requestDTO, Map<String, Object> parameters, StringBuilder qb) {
        // Подразделение 1го уровня
        if (requestDTO.getDepartment1() != null) {
            qb.append(" AND passenger_department1 IN :passenger_department1");
            parameters.put("passenger_department1", requestDTO.getDepartment1());
        }
        
        // Подразделение 2го уровня
        if (requestDTO.getDepartment2() != null) {
            qb.append(" AND passenger_department2 IN :passenger_department2");
            parameters.put("passenger_department2", requestDTO.getDepartment2());
        }
        
        // Подразделение 3го уровня
        if (requestDTO.getDepartment3() != null) {
            qb.append(" AND passenger_department3 IN :passenger_department3");
            parameters.put("passenger_department3", requestDTO.getDepartment3());
        }
        
        // Подразделение 4го уровня
        if (requestDTO.getDepartment4() != null) {
            qb.append(" AND passenger_department4 IN :passenger_department4");
            parameters.put("passenger_department4", requestDTO.getDepartment4());
        }
        
        // Подразделение 5го уровня
        if (requestDTO.getDepartment5() != null) {
            qb.append(" AND passenger_department5 IN :passenger_department5");
            parameters.put("passenger_department5", requestDTO.getDepartment5());
        }
        
        // Подразделение 6го уровня
        if (requestDTO.getDepartment6() != null) {
            qb.append(" AND passenger_department6 IN :passenger_department6");
            parameters.put("passenger_department6", requestDTO.getDepartment6());
        }
    }
    
    public static DataAndCountQueries prepareFindCarsharingRequestsQuery(
            EntityManager em, DepartmentService departmentService,
            RequestForCarsharingReportDTO requestDTO
                                                                        ) {
        
        Map<String, Object> parameters = new HashMap<>();
        
        StringBuilder qb = new StringBuilder();
        StringBuilder qbData = new StringBuilder();
        StringBuilder qbCount = new StringBuilder();
        
        qbCount.append(
                """
                SELECT count(*) AS cnt
                """);
        
        qbData.append("""
                      SELECT CAST(request.id as uuid) as id,
                        request.humanreadableid,
                        CAST(request.passenger_id as uuid) as passenger_id,
                        request.cost_center,
                        request.creation_time,
                        request.desired_date,
                        request.approval_date,
                        request.finished_time,
                        request.transport_type,
                        request.request_status,
                        request.request_status_code,
                        request.purpose_id as purpose_id,
                        request.expected_cost,
                        request.expected_distance,
                        request.expected_time,
                        request.rating_mark,
                        request.rating_comment,
                        request.change_date,
                        request.time_zone,
                        CAST(request.tariff_id as uuid) as tariff_id,
                        CAST(request.contractor_id as uuid) as contractor_id,
                        request.passenger_department1,
                        request.passenger_department2,
                        request.passenger_department3,
                        request.passenger_department4,
                        request.passenger_department5,
                        request.passenger_department6,
                        request.departure_address,
                        request.intermediate_addresses,
                        request.destination_address,
                        request.rent_id,
                        request.phone_number,
                        request.coop_trip,
                        request.passenger_count,
                        request.joined_passengers,
                        request.source,
                        request.min_taxi_tariff_cost,
                        request.comment_for_purpose,
                        request.deadline_state,
                        request.savings_cash,
                        contract.contract_number,
                        request.request_closed_datetime,
                        request.executor_group_id,
                        request.executor_group_name
                      
                      """);
        qb.append(
                """
                 FROM reports.request as request
                 LEFT JOIN reports.tariff as tariff ON request.tariff_id = tariff.id
                 LEFT JOIN reports.contract as contract ON tariff.contract_id = contract.id
                WHERE request.transport_type = 'CARSHARING'
                """);
        
        //Группа исполнителей
        if (!CollectionUtils.isEmpty(requestDTO.getExecutorGroupIds())) {
            qb.append(" AND request.executor_group_id IN :executor_group_ids");
            parameters.put("executor_group_ids", requestDTO.getExecutorGroupIds());
        }
        
        Set<String> employeeOrganizationSet = requestDTO.getEmployeeOrganizationSet();
        boolean organizationsIsNotAll = employeeOrganizationSet != null && !employeeOrganizationSet.isEmpty() && !employeeOrganizationSet.contains(
                "all");
        // Организации пассажира
        if (organizationsIsNotAll) {
            qb.append(" AND request.organization_id IN (:organization_ids)");
            parameters.put("organization_ids", employeeOrganizationSet.stream().map(UUID::fromString).collect(Collectors.toSet()));
        }
        
        // Организация
        if (requestDTO.getOrganizationId() != null && !organizationsIsNotAll) {
            qb.append(" AND request.organization_id = :organization_id");
            parameters.put("organization_id", requestDTO.getOrganizationId());
        }
        
        // Человекочитаемый идентификатор заявки
        if (requestDTO.getRequestHumanId() != null && !requestDTO.getRequestHumanId().isEmpty()) {
            qb.append(" AND lower(request.humanreadableid) like lower(:humanreadableidLikeExpression)");
            String humanreadableidLikeExpression = "%" + requestDTO.getRequestHumanId() + "%";
            parameters.put("humanreadableidLikeExpression", humanreadableidLikeExpression);
        }
        
        // Статусы поездки
        if (requestDTO.getRequestStatusSet() != null && !requestDTO.getRequestStatusSet().isEmpty()) {
            qb.append(" AND request.request_status IN (:request_statuses)");
            parameters.put("request_statuses", requestDTO.getRequestStatusSet().stream().map(Enum::name).collect(Collectors.toList()));
        }
        
        // Дата создания поездки, диапазон
        if (requestDTO.getCreationDate() != null) {
            if ((requestDTO.getCreationDate().getStart() != null) &&
                (requestDTO.getCreationDate().getEnd() != null)) {
                qb.append(" AND request.creation_time BETWEEN :creation_time_start AND :creation_time_end");
                parameters.put("creation_time_start", requestDTO.getCreationDate().getStart());
                parameters.put("creation_time_end", requestDTO.getCreationDate().getEnd());
            }
            if ((requestDTO.getCreationDate().getStart() != null) &&
                (requestDTO.getCreationDate().getEnd() == null)) {
                qb.append(" AND request.creation_time >= :creation_time_start");
                parameters.put("creation_time_start", requestDTO.getCreationDate().getStart());
            }
            if ((requestDTO.getCreationDate().getStart() == null) &&
                (requestDTO.getCreationDate().getEnd() != null)) {
                qb.append(" AND request.creation_time <= :creation_time_end");
                parameters.put("creation_time_end", requestDTO.getCreationDate().getEnd());
            }
        }
        
        // Желаемая дата поездки, диапазон
        if (requestDTO.getDesiredDate() != null) {
            if ((requestDTO.getDesiredDate().getStart() != null) &&
                (requestDTO.getDesiredDate().getEnd() != null)) {
                qb.append(" AND request.desired_date BETWEEN :desired_date_start AND :desired_date_end");
                parameters.put("desired_date_start", requestDTO.getDesiredDate().getStart());
                parameters.put("desired_date_end", requestDTO.getDesiredDate().getEnd());
            }
            if ((requestDTO.getDesiredDate().getStart() != null) &&
                (requestDTO.getDesiredDate().getEnd() == null)) {
                qb.append(" AND request.desired_date >= :desired_date_start");
                parameters.put("desired_date_start", requestDTO.getDesiredDate().getStart());
            }
            if ((requestDTO.getDesiredDate().getStart() == null) &&
                (requestDTO.getDesiredDate().getEnd() != null)) {
                qb.append(" AND request.desired_date <= :desired_date_end");
                parameters.put("desired_date_end", requestDTO.getDesiredDate().getEnd());
            }
        }
        
        // Дата завершения поездки, диапазон
        if (requestDTO.getFinishedDate() != null) {
            if ((requestDTO.getFinishedDate().getStart() != null) &&
                (requestDTO.getFinishedDate().getEnd() != null)) {
                qb.append(" AND request.finished_time BETWEEN :finished_time_start AND :finished_time_end");
                parameters.put("finished_time_start", requestDTO.getFinishedDate().getStart());
                parameters.put("finished_time_end", requestDTO.getFinishedDate().getEnd());
            }
            if ((requestDTO.getFinishedDate().getStart() != null) &&
                (requestDTO.getFinishedDate().getEnd() == null)) {
                qb.append(" AND request.finished_time >= :finished_time_start");
                parameters.put("finished_time_start", requestDTO.getFinishedDate().getStart());
            }
            if ((requestDTO.getFinishedDate().getStart() == null) &&
                (requestDTO.getFinishedDate().getEnd() != null)) {
                qb.append(" AND request.finished_time <= :finished_time_end");
                parameters.put("finished_time_end", requestDTO.getFinishedDate().getEnd());
            }
        }
        
        // ID цели поездки
        if (requestDTO.getPurposeSet() != null && !requestDTO.getPurposeSet().isEmpty()) {
            qb.append(" AND request.purpose_id IN (:purpose_ids)");
            parameters.put("purpose_ids", requestDTO.getPurposeSet().stream().map(TripPurposeDTO::getId).collect(Collectors.toList()));
        }
        
        // Список оценок для фильтрации, 0 - без оценки
        if (requestDTO.getRatingMarkSet() != null && !requestDTO.getRatingMarkSet().isEmpty()) {
            qb.append(requestDTO.getRatingMarkSet().contains(0) ? " AND (request.rating_mark IS NULL OR request.rating_mark IN (:rating_marks))"
                                                                : " AND request.rating_mark IN (:rating_marks)");
            parameters.put("rating_marks", requestDTO.getRatingMarkSet());
        }
        
        // Длительность поездки (диапазон)
        if (requestDTO.getExpectedDistance() != null) {
            if (requestDTO.getExpectedDistance().getStart() != null && requestDTO.getExpectedDistance().getEnd() != null) {
                qb.append(" AND request.expected_distance BETWEEN :expected_distance_start AND :expected_distance_end");
                parameters.put("expected_distance_start", requestDTO.getExpectedDistance().getStart());
                parameters.put("expected_distance_end", requestDTO.getExpectedDistance().getEnd());
            }
            if (requestDTO.getExpectedDistance().getStart() != null && requestDTO.getExpectedDistance().getEnd() == null) {
                qb.append(" AND request.expected_distance >= :expected_distance_start");
                parameters.put("expected_distance_start", requestDTO.getExpectedDistance().getStart());
            }
            if (requestDTO.getExpectedDistance().getStart() == null && requestDTO.getExpectedDistance().getEnd() != null) {
                qb.append(" AND request.expected_distance <= :expected_distance_end");
                parameters.put("expected_distance_end", requestDTO.getExpectedDistance().getEnd());
            }
        }
        
        // Количество пассажиров
        if (requestDTO.getPassengerCountSet() != null && !requestDTO.getPassengerCountSet().isEmpty()) {
            qb.append(" AND request.passenger_count - 1 in (:passengerCountSet)");
            parameters.put("passengerCountSet", requestDTO.getPassengerCountSet());
        }
        
        
        // Список Ид тарифов
        if (requestDTO.getTariffIdSet() != null && !requestDTO.getTariffIdSet().isEmpty()) {
            qb.append(" AND request.tariff_id IN (:tariff_ids)");
            parameters.put("tariff_ids", requestDTO.getTariffIdSet());
        }
        
        // Список контрагентов
        if (requestDTO.getContractorSet() != null && !requestDTO.getContractorSet().isEmpty()) {
            qb.append(" AND request.contractor_id IN (:contractor_ids)");
            parameters.put("contractor_ids", requestDTO.getContractorSet());
        }
        
        // Место возникновения затрат
        if (requestDTO.getCostCenter() != null && !requestDTO.getCostCenter().isEmpty()) {
            qb.append(" AND request.cost_center = :cost_center");
            parameters.put("cost_center", requestDTO.getCostCenter());
        }
        
        StringBuilder empQueryBuilder = new StringBuilder();
        
        // Табельный номер
        if (requestDTO.getPersonnelNumber() != null) {
            empQueryBuilder.append(" AND request.personnel_number = :personnel_number");
            parameters.put("personnel_number", requestDTO.getPersonnelNumber());
        }
        
        // Дата закрытия обращения
        if (requestDTO.getRequestClosedDatetime() != null) {
            qb.append(" AND request.request_closed_datetime BETWEEN :request_closed_datetime_start AND :request_closed_datetime_end");
            parameters.put("request_closed_datetime_start", requestDTO.getRequestClosedDatetime().getStart());
            parameters.put("request_closed_datetime_end", requestDTO.getRequestClosedDatetime().getEnd());
        }
        
        // Добавление фильтров по поздразделению
        addDepartmentsFilter(requestDTO, parameters, qb);
        
        // Стоимость поездки (диапазон), с фронта рубли, на бэке копейки
        if (requestDTO.getExpectedCost() != null) {
            if (requestDTO.getExpectedCost().getStart() != null && requestDTO.getExpectedCost().getEnd() == null) {
                qb.append(" AND request.expected_cost >= :expected_cost_start");
                parameters.put("expected_cost_start", requestDTO.getExpectedCost().getStart() * 100);
            }
            if (requestDTO.getExpectedCost().getStart() == null && requestDTO.getExpectedCost().getEnd() != null) {
                qb.append(" AND request.expected_cost <= :expected_cost_end");
                parameters.put("expected_cost_end", requestDTO.getExpectedCost().getEnd() * 100);
            }
            if (requestDTO.getExpectedCost().getStart() != null && requestDTO.getExpectedCost().getEnd() != null) {
                qb.append(" AND request.expected_cost BETWEEN :expected_cost_start AND :expected_cost_end");
                parameters.put("expected_cost_start", requestDTO.getExpectedCost().getStart() * 100);
                parameters.put("expected_cost_end", requestDTO.getExpectedCost().getEnd() * 100);
            }
        }
        
        if (requestDTO.getEmployeeFIO() != null && !requestDTO.getEmployeeFIO().isEmpty()) {
            var fullName = requestDTO.getEmployeeFIO().replaceAll("\\s+", " ");
            switch (fullName.split(" ").length) {
                case 1 -> empQueryBuilder.append(
                        " AND (lower(last_name) LIKE lower(:fullNameLikeExpression) OR lower(first_name) LIKE lower(:fullNameLikeExpression) " +
                        "OR lower(patronymic) LIKE lower(:fullNameLikeExpression))");
                case 2 -> empQueryBuilder.append(" AND (lower(concat(last_name, ' ', first_name)) LIKE lower(:fullNameLikeExpression) OR " +
                                                 "lower(concat(first_name, ' ', last_name)) LIKE lower(:fullNameLikeExpression) OR " +
                                                 "lower(concat(first_name, ' ', patronymic)) LIKE lower(:fullNameLikeExpression))");
                default -> empQueryBuilder.append(
                        " AND (lower(concat(last_name, ' ', first_name, ' ', patronymic)) LIKE lower(:fullNameLikeExpression)" +
                        " OR lower(concat(first_name, ' ', patronymic, ' ', last_name)) LIKE lower(:fullNameLikeExpression))");
            }
            var fullNameLikeExpression = "%" + fullName + "%";
            parameters.put("fullNameLikeExpression", fullNameLikeExpression);
        }
        
        // Департаменты
        if (requestDTO.getEmployeeDepartmentSet() != null && !requestDTO.getEmployeeDepartmentSet().isEmpty()) {
            Set<UUID> employeeDepartmentSet = departmentService.getDepartmentWithAllChildren((HashSet<UUID>) requestDTO.getEmployeeDepartmentSet());
            empQueryBuilder.append(" AND department_id in (:department_ids)");
            parameters.put("department_ids", employeeDepartmentSet);
        }
        
        // ОЕ Подразделения
        if (requestDTO.getDepartmentCode() != null) {
            empQueryBuilder.append(" AND department_id IN (SELECT id from reports.department WHERE code = :code)");
            parameters.put("code", requestDTO.getDepartmentCode());
        }
        
        if (!empQueryBuilder.isEmpty()) {
            qb.append(" AND request.passenger_id IN (SELECT id FROM reports.employee WHERE 1 = 1");
            qb.append(empQueryBuilder);
            qb.append(")");
        }
        
        StringBuilder carsharingTripQueryBuilder = new StringBuilder();
        
        // Общий пробег (км), факт
        if (requestDTO.getDrivingLength() != null) {
            if (requestDTO.getDrivingLength().getStart() != null && requestDTO.getDrivingLength().getEnd() == null) {
                carsharingTripQueryBuilder.append(" AND driving_length >= :driving_length_start");
                parameters.put("driving_length_start", requestDTO.getDrivingLength().getStart());
            }
            if (requestDTO.getDrivingLength().getStart() == null && requestDTO.getDrivingLength().getEnd() != null) {
                carsharingTripQueryBuilder.append(" AND driving_length <= :driving_length_end");
                parameters.put("driving_length_end", requestDTO.getDrivingLength().getEnd());
            }
            if (requestDTO.getDrivingLength().getStart() != null && requestDTO.getDrivingLength().getEnd() != null) {
                carsharingTripQueryBuilder.append(" AND driving_length BETWEEN :driving_length_start AND :driving_length_end");
                parameters.put("driving_length_start", requestDTO.getDrivingLength().getStart());
                parameters.put("driving_length_end", requestDTO.getDrivingLength().getEnd());
            }
        }
        
        if (!carsharingTripQueryBuilder.isEmpty()) {
            qb.append(" AND request.rent_id IN (SELECT rent_id FROM reports.carsharing_trip where 1 = 1");
            qb.append(carsharingTripQueryBuilder);
            qb.append(")");
        }
        
        // Причина отмены
        if (requestDTO.getRequestStatusCodes() != null && !requestDTO.getRequestStatusCodes().isEmpty()) {
            qb.append(" AND request.request_status_code IN :request_status_codes");
            parameters.put("request_status_codes", requestDTO.getRequestStatusCodes());
        }
        
        // Контрольный срок
        if (requestDTO.getDeadlineState() != null) {
            if (requestDTO.getDeadlineState()) {
                qb.append(" AND request.deadline_state = :deadline_state");
                parameters.put("deadline_state", DeadlineState.RED.name());
            } else {
                qb.append(" AND (request.deadline_state = :deadline_state OR request.deadline_state is null)");
                parameters.put("deadline_state", DeadlineState.NONE.name());
            }
        }
        
        // Экономия
        if (requestDTO.getSavings() != null) {
            if (requestDTO.getSavings()) {
                qb.append(" AND request.savings_cash > 0");
            } else {
                qb.append(" AND (request.savings_cash = 0 OR request.savings_cash is null)");
            }
        }
        
        // Номер договора
        if (requestDTO.getContractNumber() != null) {
            qb.append(" AND contract.contract_number = :contract_number");
            parameters.put("contract_number", requestDTO.getContractNumber());
        }
        
        qbData.append(qb);
        qbCount.append(qb);
        
        // Сортировка
        prepareSortAndPagination(qbData, requestDTO.getSortSetting(), requestDTO.getPageSetting());
        
        String sqlString = qbData.toString();
        log.debug("prepareFindCarsharingRequestsQuery(), sqlString = {}", sqlString);
        
        Query queryData = em.createNativeQuery(sqlString, Map.class);
        Query queryCount = em.createNativeQuery(qbCount.toString());
        
        parameters.forEach((paramName, paramValue) ->
                           {
                               queryData.setParameter(paramName, paramValue);
                               queryCount.setParameter(paramName, paramValue);
                           });
        if (log.isDebugEnabled()) {
            parameters.forEach((paramName, paramValue) -> log.debug("paramName = {}, paramValue = {}", paramName, paramValue));
        }
        
        return new DataAndCountQueries(queryData, queryCount);
    }
    
    public static PersonalResponseDTO mapSqlResultToResponseForPersonal(
            PersonalResponseSqlResultSetMapping rec,
            Map<UUID, EmployeeDTO> mapForEmployees,
            Map<UUID, PersonalCarDTO> mapForCars,
            Map<UUID, DepartmentShortDTO> mapForDepartments,
            Map<UUID, PositionShortDTO> mapForPosition,
            Map<UUID, TripPurposeDTO> mapForPurpose,
            Map<UUID, SharedRideKpiResultSet> mapForSharedRide,
            Map<UUID, List<WaypointDTO>> mapForWaypoints,
            Map<UUID, String> mapForTariffs,
            Map<UUID, EconomyDataDTO> mapForEconomy
                                                                       
                                                                       ) {
        var passenger = mapForEmployees.get(rec.getPassengerId());
        
        var waypoints = mapForWaypoints.get(rec.getId());
        int waypointsCount = waypoints != null && !waypoints.isEmpty() ? waypoints.size() : 0;
        Integer waypointsCountWithCheckIn = 0;
        Integer waypointsCountWithoutCheckIn = 0;
        if (waypointsCount > 0) {
            for (var waypoint : waypoints) {
                if (waypoint.getCheckinAutomatic() != null &&
                    waypoint.getCheckinAutomatic() ||
                    waypoint.getCheckinManual() != null &&
                    waypoint.getCheckinManual()) {
                    waypointsCountWithCheckIn++;
                } else {
                    waypointsCountWithoutCheckIn++;
                }
            }
        }
        List<PaymentDataDTO> paymentDataList = getPaymentDataList(rec);
        var passengerCount =
                Optional.ofNullable(rec.getPassengerCount())
                        .map(c -> c - ((!rec.isCoopTrip() || Boolean.TRUE.equals(rec.getSharedRideOwner())) ? 1 : 0))
                        .orElse(0);
        var personalReportDTO = PersonalResponseDTO
                .builder()
                .id(rec.getId())
                .humanReadableId(rec.getHumanreadableid())
                .humanReadableLimitId("")
                .author(Optional.ofNullable(rec.getAuthorId()).map(mapForEmployees::get).orElse(null))
                .passenger(passenger)
                .passengers(null) // Это поле, кажется, всегда пустое
                .itinerantType(
                        Optional.ofNullable(passenger).map(EmployeeDTO::getItinerantType).map(ItinerantType::valueOf).orElse(null))
                .personalCar(Optional.ofNullable(rec.getPersonalCar()).map(mapForCars::get).orElse(null))
                .costCenter(rec.getCostCenter())
                .department(Optional.ofNullable(passenger)
                                    .map(EmployeeDTO::getDepartmentId)
                                    .map(mapForDepartments::get)
                                    .orElse(null))
                .position(Optional.ofNullable(passenger)
                                  .map(EmployeeDTO::getPositionId)
                                  .map(mapForPosition::get)
                                  .orElse(null))
                .creationTime(rec.getCreationTime())
                .desiredDate(rec.getDesiredDate())
                .approveDate(rec.getApprovalDate())
                .orderPaymentFormationStartDate(rec.getOrderPaymentFormationStartDate())
                .transportType(Optional.ofNullable(rec.getTransportType()).map(TransportTypeEnum::valueOf)
                                       .orElse(null))
                .status(Optional.ofNullable(rec.getRequestStatus()).map(TripRequestStatus::valueOf)
                                .orElse(null))
                .statusCode(rec.getRequestStatusCode())
                .purpose(Optional.ofNullable(rec.getPurposeId()).map(mapForPurpose::get).orElse(null))
                .coopTrip(rec.isCoopTrip())
                .sharedRideId(rec.getSharedRideId())
                .passengerCount(passengerCount)
                .expected(ExpectedDataDTO.builder()
                                         .cost(Optional.ofNullable(rec.getExpectedCost()).orElse(0.0))
                                         .distance(Optional.ofNullable(rec.getExpectedDistance()).orElse(0.0))
                                         .time(Long.valueOf(rec.getExpectedTime() == null ? 0 : rec.getExpectedTime().toMinutes()).intValue())
                                         .segments(new ArrayList<>())
                                         .waypoints(waypoints)
                                         .waypointsCount(waypointsCount)
                                         .waypointsCountWithCheckIn(waypointsCountWithCheckIn)
                                         .waypointsCountWithoutCheckIn(waypointsCountWithoutCheckIn)
                                         .build())
                .factData(null) // По ЛТ нет trip'ов => это поле всегда пустое
                .paymentDataList(paymentDataList)
                .paymentCost(getPaymentCost(paymentDataList))
                .paymentPeriod(rec.getCreationTime() == null ? null :
                               Request.getPeriodOfPayment(rec.getOrderPaymentFormationStartDate()))
                .kpi(Optional.ofNullable(rec.getSharedRideId())
                             .map(mapForSharedRide::get)
                             .map(SharedRideKpiResultSet::getSharedRideKpi).orElse(null))
                .requestRating(RequestRatingDTO.builder().rating(rec.getRatingMark()).ratingComment(rec.getRatingComment()).build())
                .paymentTime(rec.getRequestStatus() != null && TripRequestStatus.valueOf(rec.getRequestStatus()).isTerminal()
                             ? withTimeZone(rec.getChangeDate(), rec.getTimeZone())
                             // может лучше тайм-зону прибавлять на фронте по аналогии с другими полями?
                             : null)
                .costSharePart(rec.getCostSharePart())
                .savingsCash(rec.getSavingsCash() == null ? 0 : rec.getSavingsCash())
                .savingsProcents(Optional.ofNullable(rec.getSavingsProcents()).orElse(0L))
                .driverFIO(Optional.ofNullable(rec.getEmployeeDriverId())
                                   .map(mapForEmployees::get)
                                   .map(emp -> Optional.ofNullable(emp.getLastName()).orElse("") +
                                               " " +
                                               Optional.ofNullable(emp.getFirstName()).orElse("") +
                                               " " +
                                               Optional.ofNullable(emp.getPatronymic()).orElse(""))
                                   .map(String::trim)
                                   .orElse(null))
                .sharedRideOwner(rec.getSharedRideOwner())
                .additionalSum(Optional.ofNullable(rec.getAdditionalSum()).orElse(0L))
                .numberPassengersJoined(rec.getNumberPassengersJoined())
                .timeZone(rec.getTimeZone())
                .tariff(Optional.ofNullable(rec.getTariffId())
                                .map(mapForTariffs::get)
                                .orElse(null))
                .passengerDepartment1(rec.getPassengerDepartment1())
                .passengerDepartment2(rec.getPassengerDepartment2())
                .passengerDepartment3(rec.getPassengerDepartment3())
                .passengerDepartment4(rec.getPassengerDepartment4())
                .passengerDepartment5(rec.getPassengerDepartment5())
                .passengerDepartment6(rec.getPassengerDepartment6())
                .departureAddress(rec.getDepartureAddress())
                .intermediateAddresses(rec.getIntermediateAddresses())
                .joinedPassengers(rec.getJoinedPassengers())
                .destinationAddress(rec.getDestinationAddress())
                .source(rec.getSource())
                .minTaxiTariffCost(Optional.ofNullable(rec.getMinTaxiTariffCost()).map(i -> i / 100.0).orElse(null))
                .commentForPurpose(rec.getCommentForPurpose())
                .requestStatusCode(rec.getRequestStatusCode())
                .deadlineViolation(rec.getDeadlineState() == null || DeadlineState.NONE.name().equals(rec.getDeadlineState()) ? "Нет" : "Да")
                .savings(rec.getSavingsCash() != null && rec.getSavingsCash() > 0)
                .requestClosedDatetime(rec.getRequestClosedDatetime())
                .executorGroupId(rec.getExecutorGroupId())
                .executorGroupName(rec.getExecutorGroupName())
                .tripStartTime(rec.getTripStartTime())
                .deadline(rec.getDeadline())
                .build();
        var economy = mapForEconomy.getOrDefault(rec.getSharedRideId(), EconomyDataDTO.builder().requestCount(1).joinedPassengerCount(0).build());
        // Берём активное кол-во заявок по совместной поездке - заявка инициатора
        var totalSharedRequestCount = economy.getRequestCount() - 1;
        if (rec.isCoopTrip() && Boolean.TRUE.equals(rec.getSharedRideOwner())) {
            personalReportDTO.setTotalSharedRequestCount(totalSharedRequestCount);
        }
        // Экономия выгружается только для заявок в статусе PERSONAL_PAYMENT_DONE
        if (rec.getExpectedCost() != null && TripRequestStatus.PERSONAL_PAYMENT_DONE.name().equals(rec.getRequestStatus())) {
            var additionalSum = rec.isCoopTrip() && Boolean.FALSE.equals(rec.getSharedRideOwner()) ? 10000 : 0;
            var limitDebit = rec.getExpectedCost() / (totalSharedRequestCount + 1) + additionalSum;
            personalReportDTO.setLimitDebit(limitDebit / 100.0);
            personalReportDTO.setDepartmentEconomy((rec.getExpectedCost() - limitDebit) / 100.0);
            var financialImpactJoined = (rec.getPassengerCount() - 1) * rec.getExpectedCost();
            personalReportDTO.setFinancialImpactJoined(financialImpactJoined / 100.0);
            var financialImpact = financialImpactJoined;
            if (rec.isCoopTrip() && Boolean.TRUE.equals(rec.getSharedRideOwner())) {
                var financialImpactShared =
                        totalSharedRequestCount *
                        (rec.getExpectedCost() - Optional.ofNullable(rec.getAdditionalSum()).orElse(0L));
                financialImpact += financialImpactShared;
                personalReportDTO.setFinancialImpactShared(financialImpactShared / 100.0);
            }
            personalReportDTO.setFinancialImpact(financialImpact / 100.0);
        }
        return personalReportDTO;
    }
    
    public static CarsharingResponseDTO mapSqlResultToResponseForCarsharing(
            CarsharingResponseSqlResultSetMapping rec,
            Map<UUID, EmployeeDTO> mapForEmployees,
            Map<UUID, DepartmentShortDTO> mapForDepartments,
            Map<UUID, TripPurposeDTO> mapForPurpose,
            Map<UUID, TariffShortDTO> mapForTariffs,
            CarsharingTrip mapForCarsharingTrip,
            Map<UUID, Organization> mapForOrgs,
            Map<UUID, ContractorResultSet> mapForContractors
                                                                           ) {
        var trip = Optional.ofNullable(mapForCarsharingTrip);
        var carsharingResponseDTO = CarsharingResponseDTO.builder()
                                                         .id(rec.getId())
                                                         .humanReadableId(rec.getHumanreadableid())
                                                         .costCenter(rec.getCostCenter())
                                                         .organization(Optional.ofNullable(rec.getPassengerId())
                                                                               .map(mapForEmployees::get)
                                                                               .map(EmployeeDTO::getOrganizationId)
                                                                               .map(mapForOrgs::get)
                                                                               .map(Organization::getOfficialName)
                                                                               .orElse(null))
                                                         .contractor(Optional.ofNullable(mapForContractors.get(rec.getContractorId()))
                                                                             .map(ContractorResultSet::getName)
                                                                             .orElse(null))
                                                         .rentId(Optional.ofNullable(rec.getRentId()).map(Objects::toString).orElse(null))
                                                         .fio(Optional.ofNullable(mapForEmployees.get(rec.getPassengerId())).map(EmployeeDTO::getFIO)
                                                                      .orElse(null))
                                                         .personnelNumber(
                                                                 Optional.ofNullable(mapForEmployees.get(rec.getPassengerId()))
                                                                         .map(EmployeeDTO::getPersonnelNumber)
                                                                         .orElse(null))
                                                         .position(Optional.ofNullable(mapForEmployees.get(rec.getPassengerId()))
                                                                           .map(EmployeeDTO::getPositionName)
                                                                           .orElse(null))
                                                         .phoneNumber(rec.getPhoneNumber())
                                                         .purpose(Optional.ofNullable(rec.getPurposeId()).map(mapForPurpose::get).orElse(null))
                                                         .timeZone(rec.getTimeZone())
                                                         .creationTime(rec.getCreationTime())
                                                         .car(trip.map(CarsharingTrip::getCarModel).orElse(null))
                                                         .rentCreatedTime(trip.map(CarsharingTrip::getRentCreatedAt).orElse(null))
                                                         .rentFinishedTime(trip.map(CarsharingTrip::getRentFinishedAt).orElse(null))
                                                         .startAddress(trip.map(CarsharingTrip::getStartAddress).orElse(null))
                                                         .finishAddress(trip.map(CarsharingTrip::getFinishAddress).orElse(null))
                                                         .expectedTime(rec.getExpectedTime() == null ? 0 : TimeUnit.NANOSECONDS.toMinutes(rec.getExpectedTime()))
                                                         .reserveTime(trip.map(CarsharingTrip::getReserveTime).orElse(null))
                                                         .drivingTime(trip.map(CarsharingTrip::getDrivingTime).orElse(null))
                                                         .parkingTime(trip.map(CarsharingTrip::getDrivingTime).orElse(null))
                                                         .expectedDistance(
                                                                 rec.getExpectedDistance() == null ? 0.0 : rec.getExpectedDistance())
                                                         .drivingLength(trip.map(CarsharingTrip::getDrivingLength).orElse(null))
                                                         .reserveTimeCost(trip.map(CarsharingTrip::getReserveTimeCost).orElse(null))
                                                         // Переводим в рубли
                                                         .expectedCost(rec.getExpectedCost() == null ? 0.0 : rec.getExpectedCost() / 100)
                                                         .drivingTimeCost(trip.map(CarsharingTrip::getDrivingTimeCost).orElse(null))
                                                         .parkingTimeCost(trip.map(CarsharingTrip::getParkingTimeCost).orElse(null))
                                                         .drivingLengthCost(trip.map(CarsharingTrip::getDrivingLengthCost).orElse(null))
                                                         .totalCost(trip.map(CarsharingTrip::getTotalCost).orElse(null))
                                                         .department(Optional.ofNullable(rec.getPassengerId())
                                                                             .map(mapForEmployees::get)
                                                                             .map(EmployeeDTO::getDepartmentId)
                                                                             .map(mapForDepartments::get)
                                                                             .orElse(null))
                                                         .tariff(Optional.ofNullable(rec.getTariffId()).map(mapForTariffs::get).orElse(null))
                                                         .passengerDepartment1(rec.getPassengerDepartment1())
                                                         .passengerDepartment2(rec.getPassengerDepartment2())
                                                         .passengerDepartment3(rec.getPassengerDepartment3())
                                                         .passengerDepartment4(rec.getPassengerDepartment4())
                                                         .passengerDepartment5(rec.getPassengerDepartment5())
                                                         .passengerDepartment6(rec.getPassengerDepartment6())
                                                         .desiredDate(rec.getDesiredDate())
                                                         .approveDate(rec.getApprovalDate())
                                                         .status(Optional.ofNullable(rec.getRequestStatus()).map(TripRequestStatus::valueOf)
                                                                         .orElse(null))
                                                         .coopTrip(rec.isCoopTrip())
                                                         .departureAddress(rec.getDepartureAddress())
                                                         .intermediateAddresses(rec.getIntermediateAddresses())
                                                         .destinationAddress(rec.getDestinationAddress())
                                                         .passengerCount(rec.getPassengerCount())
                                                         .ratingMark(rec.getRatingMark())
                                                         .ratingComment(rec.getRatingComment())
                                                         .joinedPassengers(rec.getJoinedPassengers())
                                                         .source(rec.getSource())
                                                         .minTaxiTariffCost(
                                                                 Optional.ofNullable(rec.getMinTaxiTariffCost()).map(i -> i / 100.0).orElse(null))
                                                         .commentForPurpose(rec.getCommentForPurpose())
                                                         .requestStatusCode(rec.getRequestStatusCode())
                                                         .deadlineViolation(
                                                                 rec.getDeadlineState() == null ||
                                                                 DeadlineState.NONE.name().equals(rec.getDeadlineState()) ? "Нет" : "Да")
                                                         .savings(rec.getSavingsCash() != null && rec.getSavingsCash() > 0)
                                                         .contractNumber(rec.getContractNumber())
                                                         .requestClosedDatetime(rec.getRequestClosedDatetime())
                                                         .executorGroupId(rec.getExecutorGroupId())
                                                         .executorGroupName(rec.getExecutorGroupName())
                                                         .build();
        if (trip.isPresent() && trip.get().getTotalCost() != null) {
            var cost = trip.get().getTotalCost();
            var limitDebit = cost;
            carsharingResponseDTO.setLimitDebit(limitDebit);
            carsharingResponseDTO.setDepartmentEconomy(cost - limitDebit);
            var financialImpactJoined = (rec.getPassengerCount() - 1) * cost;
            carsharingResponseDTO.setFinancialImpactJoined(financialImpactJoined);
            var financialImpact = financialImpactJoined;
            carsharingResponseDTO.setFinancialImpact(financialImpact);
        }
        return carsharingResponseDTO;
        
    }
    
    public static Long getPaymentCost(List<PaymentDataDTO> paymentDataList) {
        long sum4664 = 0;
        long sumOtherPaymentTypeCodes = 0;
        
        for (var paymentData : paymentDataList) {
            if (PaymentTypeCode.CODE_4664.equals(paymentData.getPaymentTypeCode())) {
                sum4664 = sum4664 + paymentData.getPaymentPrice();
            } else {
                sumOtherPaymentTypeCodes = sumOtherPaymentTypeCodes + paymentData.getPaymentPrice();
            }
        }
        return (sum4664 * 87 / 100) + sumOtherPaymentTypeCodes;
    }
    
    public static TaxiResponseDTO mapSqlResultToResponseForTaxi(
            ObjectMapper objectMapper, DriverMapper driverMapper, VehicleMapper
            vehicleMapper,
            TaxiResponseSqlResultSetMapping rec,
            Map<UUID, EmployeeDTO> mapForEmployees,
            Map<UUID, DepartmentShortDTO> mapForDepartments,
            Map<UUID, PositionShortDTO> mapForPosition,
            Map<UUID, TripPurposeDTO> mapForPurpose,
            Map<UUID, TaxiTripResultSet> mapForTaxiTripSingle,
            Map<UUID, TaxiTripResultSet> mapForTaxiTripCoop,
            Map<UUID, List<WaypointDTO>> mapForWaypoints,
            Map<UUID, TariffShortDTO> mapForTariffs,
            Map<UUID, LimitResultSet> mapForLimit,
            Map<UUID, ContractorResultSet> mapForContractor,
            Map<UUID, OrganizationShortDTO> mapForOrganizations,
            Map<UUID, EconomyDataDTO> mapForEconomy
                                                               ) {
        var passenger = mapForEmployees.get(rec.getPassengerId());
        
        var waypoints = mapForWaypoints.get(rec.getId());
        int waypointsCount = waypoints != null && !waypoints.isEmpty() ? waypoints.size() : 0;
        Integer waypointsCountWithCheckIn = 0;
        Integer waypointsCountWithoutCheckIn = 0;
        if (waypointsCount > 0) {
            for (var waypoint : waypoints) {
                if (waypoint.getCheckinAutomatic() != null &&
                    waypoint.getCheckinAutomatic() ||
                    waypoint.getCheckinManual() != null &&
                    waypoint.getCheckinManual()) {
                    waypointsCountWithCheckIn++;
                } else {
                    waypointsCountWithoutCheckIn++;
                }
            }
        }
        
        Driver driver = null;
        Vehicle vehicle = null;
        
        try {
            driver = objectMapper.readValue(rec.getDriver(), Driver.class);
            vehicle = objectMapper.readValue(rec.getVehicle(), Vehicle.class);
        } catch (Exception e) {
        }
        
        TaxiTripResultSet taxiTripResultSet;
        if (rec.isCoopTrip()) {
            log.debug("mapSqlResultToResponseForTaxi: coopTrip = true, rideId = " + rec.getRideId());
            taxiTripResultSet = mapForTaxiTripCoop.get(rec.getRideId());
        } else {
            log.debug("mapSqlResultToResponseForTaxi: coopTrip = false, requestId = " + rec.getId());
            taxiTripResultSet = mapForTaxiTripSingle.get(rec.getId());
        }
        if (taxiTripResultSet == null) {
            log.debug("mapSqlResultToResponseForTaxi: taxiTripResultSet not found!");
            taxiTripResultSet = new TaxiTripResultSet();
        }
        ContractorResultSet contractorResultSet = mapForContractor.get(rec.getContractorId());
        if (contractorResultSet == null) {
            log.debug("mapSqlResultToResponseForTaxi: contractorResultSet not found!");
            contractorResultSet = new ContractorResultSet(null, null);
        }
        LimitResultSet limitResultSet = mapForLimit.get(rec.getLimitId());
        if (limitResultSet == null) {
            log.debug("mapSqlResultToResponseForTaxi: не найдена информация по лимиту с id = {}", rec.getLimitId());
            limitResultSet = new LimitResultSet(null, null, null);
        }
        
        var taxiResponseDTO = TaxiResponseDTO
                .builder()
                .id(rec.getId())
                .humanReadableId(rec.getHumanreadableid())
                .organizationId(rec.getOrganizationId())
                .humanReadableLimitId(limitResultSet.getHumanReadableId())
                .author(Optional.ofNullable(rec.getAuthorId()).map(mapForEmployees::get).orElse(null))
                .passenger(passenger)
                .passengers(null) // Это поле, кажется, всегда пустое
                .itinerantType(
                        Optional.ofNullable(passenger).map(EmployeeDTO::getItinerantType).map(ItinerantType::valueOf).orElse(null))
                .costCenter(Optional.ofNullable(rec.getCostCenter()).orElse(null))
                .department(Optional.ofNullable(passenger)
                                    .map(EmployeeDTO::getDepartmentId)
                                    .map(mapForDepartments::get)
                                    .orElse(null))
                .position(Optional.ofNullable(passenger)
                                  .map(EmployeeDTO::getPositionId)
                                  .map(mapForPosition::get)
                                  .orElse(null))
                .creationTime(rec.getCreationTime())
                .finishedTime(rec.getFinishedTime())
                .desiredDate(rec.getDesiredDate())
                .kpiSavings(Optional.ofNullable(rec.getSavingsProcents()).map(Long::doubleValue)
                                    .orElse(null))
                .savingsProcents(rec.getSavingsProcents())
                .costSharePart(rec.getCostSharePart())
                .savingsCash(rec.getSavingsCash() == null ? 0 : rec.getSavingsCash())
                .transportType(Optional.ofNullable(rec.getTransportType()).map(TransportTypeEnum::valueOf)
                                       .orElse(null))
                .status(Optional.ofNullable(rec.getRequestStatus()).map(TripRequestStatus::valueOf)
                                .orElse(null))
                .purpose(Optional.ofNullable(rec.getPurposeId()).map(mapForPurpose::get).orElse(null))
                .coopTrip(rec.isCoopTrip())
                .sharedRideId(rec.getRideId())
                .passengerCount(rec.getPassengerCount())
                .expected(ExpectedDataDTO.builder()
                                         .cost(Optional.ofNullable(rec.getExpectedCost()).orElse(0.0))
                                         .distance(Optional.ofNullable(rec.getExpectedDistance()).orElse(0.0))
                                         .time(rec.getExpectedTime() == null ? 0 : TimeUnit.NANOSECONDS.toMinutes(rec.getExpectedTime()))
                                         .segments(new ArrayList<>())
                                         .waypoints(waypoints)
                                         .waypointsCount(waypointsCount)
                                         .waypointsCountWithCheckIn(waypointsCountWithCheckIn)
                                         .waypointsCountWithoutCheckIn(waypointsCountWithoutCheckIn)
                                         .build())
                .requestRating(RequestRatingDTO.builder()
                                               .rating(rec.getRatingMark())
                                               .ratingComment(rec.getRatingComment())
                                               .build())
                .tariff(Optional.ofNullable(rec.getTariffId())
                                .map(mapForTariffs::get)
                                .orElse(null))
                .driver(driverMapper.toDTO(driver))
                .vehicle(vehicleMapper.toDTO(vehicle))
                .sharedRideOwner(rec.getSharedRideOwner())
                .numberPassengersJoined(rec.getNumberPassengersJoined())
                .timeZone(rec.getTimeZone())
                .passengerDepartment1(rec.getPassengerDepartment1())
                .passengerDepartment2(rec.getPassengerDepartment2())
                .passengerDepartment3(rec.getPassengerDepartment3())
                .passengerDepartment4(rec.getPassengerDepartment4())
                .passengerDepartment5(rec.getPassengerDepartment5())
                .passengerDepartment6(rec.getPassengerDepartment6())
                .departureAddress(rec.getDepartureAddress())
                .intermediateAddresses(rec.getIntermediateAddresses())
                .destinationAddress(rec.getDestinationAddress())
                .resolution((rec.getResolution()))
                .factData(FactDataDTO.builder()
                                     .tripFactWaitTime(taxiTripResultSet.getTripFactWaitTime())
                                     .tripFactPrice(taxiTripResultSet.getTripFactPrice())
                                     .tripFactDistance(taxiTripResultSet.getTripFactDistance())
                                     .tripFactDuration(taxiTripResultSet.getTripFactDuration())
                                     .tripStartTime(taxiTripResultSet.getTripStartTime())
                                     .build())
                .tripId(taxiTripResultSet.getId())
                .tripHumanReadableID(taxiTripResultSet.getHumanReadableId())
                .factParametersSettingTime(taxiTripResultSet.getFactParametersSettingTime())
                .limit(LimitDTO.builder()
                               .id(limitResultSet.getId())
                               .humanReadableId(limitResultSet.getHumanReadableId())
                               .departmentId(limitResultSet.getDepartmentId())
                               .build())
                .contractor(ContractorDTO.builder()
                                         .id(contractorResultSet.getId())
                                         .name(contractorResultSet.getName())
                                         .build())
                .deadline(rec.getDeadline())
                .deadlineViolation(rec.getDeadlineState() == null || DeadlineState.NONE.name().equals(rec.getDeadlineState()) ? "Нет" : "Да")
                .driverArrivedDatetime(rec.getDriverArrivedDatetime())
                .organizationOfficialName(Optional.ofNullable(rec.getOrganizationId())
                                                  .map(mapForOrganizations::get)
                                                  .map(OrganizationShortDTO::getOfficialName)
                                                  .orElse(""))
                .taxiClass(Optional.ofNullable(rec.getTaxiClass()).map(TaxiClass::valueOf).orElse(null))
                .statusCode(rec.getRequestStatusCode())
                .commentForDriver(rec.getCommentForDriver())
                .approvalState(Optional.ofNullable(rec.getApprovalState()).map(ApprovalState::valueOf).orElse(null))
                .approvedBy(Optional.ofNullable(rec.getApprovedBy()).map(mapForEmployees::get).orElse(null))
                .approveDate(rec.getApprovalDate())
                .requestClosedDatetime(rec.getRequestClosedDatetime())
                .joinedPassengers(rec.getJoinedPassengers())
                .source(rec.getSource())
                .minTaxiTariffCost(Optional.ofNullable(rec.getMinTaxiTariffCost()).map(i -> i / 100.0).orElse(null))
                .commentForPurpose(rec.getCommentForPurpose())
                .requestStatusCode(rec.getRequestStatusCode())
                .contractNumber(rec.getContractNumber())
                .savings(rec.getSavingsCash() != null && rec.getSavingsCash() > 0)
                .executorGroupId(rec.getExecutorGroupId())
                .executorGroupName(rec.getExecutorGroupName())
                .registryFactWaitingTime(taxiTripResultSet.getRegistryFactWaitingTime())
                .registryFactDistance(taxiTripResultSet.getRegistryFactDistance())
                .registryFactCost(taxiTripResultSet.getRegistryFactCost())
                .registryHumanReadableId(taxiTripResultSet.getRegistryHumanReadableId())
                .registryFactPayment(
                        Optional.ofNullable(taxiTripResultSet.getRegistryFactPayment()).map(isPayment -> isPayment ? "Да" : "Нет").orElse(null))
                .build();
        var economy = mapForEconomy.getOrDefault(rec.getRideId(), EconomyDataDTO.builder().requestCount(1).joinedPassengerCount(0).build());
        // Берём активное кол-во заявок по совместной поездке - заявка инициатора
        var totalSharedRequestCount = economy.getRequestCount() - 1;
        if (rec.isCoopTrip() && Boolean.TRUE.equals(rec.getSharedRideOwner())) {
            taxiResponseDTO.setTotalSharedRequestCount(totalSharedRequestCount);
        }
        if (taxiTripResultSet.getTripFactPrice() != null) {
            var limitDebit = taxiTripResultSet.getTripFactPrice() / (totalSharedRequestCount + 1);
            taxiResponseDTO.setLimitDebit(limitDebit / 100.0);
            taxiResponseDTO.setDepartmentEconomy((taxiTripResultSet.getTripFactPrice() - limitDebit) / 100.0);
            var financialImpactJoined = (rec.getPassengerCount() - 1) * taxiTripResultSet.getTripFactPrice();
            taxiResponseDTO.setFinancialImpactJoined(financialImpactJoined / 100.0);
            var financialImpact = financialImpactJoined;
            if (rec.isCoopTrip() && Boolean.TRUE.equals(rec.getSharedRideOwner())) {
                var financialImpactShared = totalSharedRequestCount * taxiTripResultSet.getTripFactPrice();
                financialImpact += financialImpactShared;
                taxiResponseDTO.setFinancialImpactShared(financialImpactShared / 100.0);
            }
            taxiResponseDTO.setFinancialImpact(financialImpact / 100.0);
        }
        return taxiResponseDTO;
    }
    
    public static GroupTransferResponseDTO mapSqlResultToResponseForGroupTransfer(
            ObjectMapper objectMapper,
            DriverMapper driverMapper,
            VehicleMapper vehicleMapper,
            GroupTransferResponseSqlResultSetMapping rec,
            Map<UUID, EmployeeDTO> mapForEmployees,
            Map<UUID, DepartmentShortDTO> mapForDepartments,
            Map<UUID, PositionShortDTO> mapForPosition,
            Map<UUID, TripPurposeDTO> mapForPurpose,
            Map<UUID, List<WaypointDTO>> mapForWaypoints,
            Map<UUID, TariffShortDTO> mapForTariffs,
            Map<UUID, LimitResultSet> mapForLimit,
            Map<UUID, ContractorResultSet> mapForContractor,
            Map<UUID, OrganizationShortDTO> mapForOrganizations
                                                                                 ) {
        var passenger = mapForEmployees.get(rec.getPassengerId());
        
        var waypoints = mapForWaypoints.get(rec.getId());
        int waypointsCount = waypoints != null && !waypoints.isEmpty() ? waypoints.size() : 0;
        Integer waypointsCountWithCheckIn = 0;
        Integer waypointsCountWithoutCheckIn = 0;
        if (waypointsCount > 0) {
            for (var waypoint : waypoints) {
                if (waypoint.getCheckinAutomatic() != null &&
                    waypoint.getCheckinAutomatic() ||
                    waypoint.getCheckinManual() != null &&
                    waypoint.getCheckinManual()) {
                    waypointsCountWithCheckIn++;
                } else {
                    waypointsCountWithoutCheckIn++;
                }
            }
        }
        
        Driver driver = null;
        Vehicle vehicle = null;
        
        try {
            driver = objectMapper.readValue(rec.getDriver(), Driver.class);
            vehicle = objectMapper.readValue(rec.getVehicle(), Vehicle.class);
        } catch (Exception e) {
        }
        
        ContractorResultSet contractorResultSet = mapForContractor.get(rec.getContractorId());
        if (contractorResultSet == null) {
            log.debug("mapSqlResultToResponseForTaxi: contractorResultSet not found!");
            contractorResultSet = new ContractorResultSet(null, null);
        }
        LimitResultSet limitResultSet = mapForLimit.get(rec.getLimitId());
        if (limitResultSet == null) {
            log.debug("mapSqlResultToResponseForTaxi: не найдена информация по лимиту с id = {}", rec.getLimitId());
            limitResultSet = new LimitResultSet(null, null, null);
        }
        
        GroupTransferResponseDTO groupTransferResponseDTO = GroupTransferResponseDTO
                .builder()
                .id(rec.getId())
                .humanReadableId(rec.getHumanreadableid())
                .organizationId(rec.getOrganizationId())
                .humanReadableLimitId(limitResultSet.getHumanReadableId())
                .author(Optional.ofNullable(rec.getAuthorId()).map(mapForEmployees::get).orElse(null))
                .passenger(passenger)
                .itinerantType(
                        Optional.ofNullable(passenger).map(EmployeeDTO::getItinerantType).map(ItinerantType::valueOf).orElse(null))
                .costCenter(Optional.ofNullable(rec.getCostCenter()).orElse(null))
                .department(Optional.ofNullable(passenger)
                                    .map(EmployeeDTO::getDepartmentId)
                                    .map(mapForDepartments::get)
                                    .orElse(null))
                .position(Optional.ofNullable(passenger)
                                  .map(EmployeeDTO::getPositionId)
                                  .map(mapForPosition::get)
                                  .orElse(null))
                .creationTime(rec.getCreationTime())
                .finishedTime(rec.getFinishedTime())
                .desiredDate(rec.getDesiredDate())
                .transportType(Optional.ofNullable(rec.getTransportType()).map(TransportTypeEnum::valueOf)
                                       .orElse(null))
                .status(Optional.ofNullable(rec.getRequestStatus()).map(TripRequestStatus::valueOf)
                                .orElse(null))
                .purpose(Optional.ofNullable(rec.getPurposeId()).map(mapForPurpose::get).orElse(null))
                .passengerCount(rec.getPassengerCount())
                .expected(ExpectedDataDTO.builder()
                        .cost(Optional.ofNullable(rec.getExpectedCost()).orElse(0.0))
                        .distance(Optional.ofNullable(rec.getExpectedDistance()).orElse(0.0))
                        .time(Long.valueOf(rec.getExpectedTime() == null ? 0 : rec.getExpectedTime().toMinutes()).intValue())
                        .expectedTime(rec.getExpectedTime() == null ? Duration.of(0, ChronoUnit.NANOS) : Duration.of(rec.getExpectedTime().getSeconds(), ChronoUnit.NANOS))
                        .segments(new ArrayList<>())
                        .waypoints(waypoints)
                        .waypointsCount(waypointsCount)
                        .waypointsCountWithCheckIn(waypointsCountWithCheckIn)
                        .waypointsCountWithoutCheckIn(waypointsCountWithoutCheckIn)
                        .build())
                .requestRating(RequestRatingDTO.builder()
                                               .rating(rec.getRatingMark())
                                               .ratingComment(rec.getRatingComment())
                                               .build())
                .tariff(Optional.ofNullable(rec.getTariffId())
                                .map(mapForTariffs::get)
                                .orElse(null))
                .driver(driverMapper.toDTO(driver))
                .vehicle(vehicleMapper.toDTO(vehicle))
                .timeZone(rec.getTimeZone())
                .passengerDepartment1(rec.getPassengerDepartment1())
                .passengerDepartment2(rec.getPassengerDepartment2())
                .passengerDepartment3(rec.getPassengerDepartment3())
                .passengerDepartment4(rec.getPassengerDepartment4())
                .passengerDepartment5(rec.getPassengerDepartment5())
                .passengerDepartment6(rec.getPassengerDepartment6())
                .departureAddress(rec.getDepartureAddress())
                .intermediateAddresses(rec.getIntermediateAddresses())
                .destinationAddress(rec.getDestinationAddress())
                .resolution((rec.getResolution()))
                .factData(FactDataDTO.builder()
                                     .build())
                .limit(LimitDTO.builder()
                               .id(limitResultSet.getId())
                               .humanReadableId(limitResultSet.getHumanReadableId())
                               .departmentId(limitResultSet.getDepartmentId())
                               .build())
                .contractor(ContractorDTO.builder()
                                         .id(contractorResultSet.getId())
                                         .name(contractorResultSet.getName())
                                         .build())
                .deadline(rec.getDeadline())
                .deadlineViolation(rec.getDeadlineState() == null || DeadlineState.NONE.name().equals(rec.getDeadlineState()) ? "Нет" : "Да")
                .driverArrivedDatetime(rec.getDriverArrivedDatetime())
                .organizationOfficialName(Optional.ofNullable(rec.getOrganizationId())
                                                  .map(mapForOrganizations::get)
                                                  .map(OrganizationShortDTO::getOfficialName)
                                                  .orElse(""))
                .groupTransferClass(Optional.ofNullable(rec.getGroupTransferClass()).map(GroupTransferClass::valueOf).orElse(null))
                .requestStatusCode(rec.getRequestStatusCode())
                .commentForDriver(rec.getCommentForDriver())
                .approvalState(Optional.ofNullable(rec.getApprovalState()).map(ApprovalState::valueOf).orElse(null))
                .approvedBy(Optional.ofNullable(rec.getApprovedBy()).map(mapForEmployees::get).orElse(null))
                .approveDate(rec.getApprovalDate())
                .requestClosedDatetime(rec.getRequestClosedDatetime())
                .vip(rec.getVip())
                .source(rec.getSource())
                .commentForPurpose(rec.getCommentForPurpose())
                .savings(rec.getSavingsCash() != null && rec.getSavingsCash() > 0)
                .minTaxiTariffCost(Optional.ofNullable(rec.getMinTaxiTariffCost()).map(i -> i / 100.0).orElse(null))
                .contractNumber(rec.getContractNumber())
                .executorGroupId(rec.getExecutorGroupId())
                .executorGroupName(rec.getExecutorGroupName())
                .build();
        
        return groupTransferResponseDTO;
    }
    
    public static DataAndCountQueries prepareFindPublicRequestsQuery(
            EntityManager em, DepartmentService departmentService,
            RequestForPublicReportDTO requestDTO
                                                                    ) {
        Map<String, Object> parameters = new HashMap<>();
        
        StringBuilder qbData = new StringBuilder();
        StringBuilder qbCount = new StringBuilder();
        
        qbCount.append(
                """
                SELECT count(*) AS cnt
                """);
        
        qbData.append("""
                      SELECT CAST(id as uuid) as id,
                        humanreadableid,
                        CAST(author_id as uuid) as author_id,
                        CAST(passenger_id as uuid) as passenger_id,
                        creation_time,
                        desired_date,
                        approval_date,
                        order_payment_formation_start_date,
                        transport_type,
                        request_status,
                        request_status_code,
                        purpose_id as purpose_id,
                        passenger_count,
                        expected_cost, expected_distance, expected_time,
                        payment_price_insurance, payment_price_main, payment_price_optional, payment_type_code_insurance, payment_type_code_main, payment_type_code_optional,
                        rating_mark, rating_comment,
                        change_date, time_zone,
                        savings_cash,
                        savings_procents,
                        tariff_id,
                        passenger_department1,
                        passenger_department2,
                        passenger_department3,
                        passenger_department4,
                        passenger_department5,
                        passenger_department6,
                        departure_address,
                        intermediate_addresses,
                        destination_address,
                        public_compensation_document_exist,
                        cost_center,
                        source,
                        min_taxi_tariff_cost,
                        comment_for_purpose,
                        deadline_state,
                        deadline,
                        request_closed_datetime,
                        executor_group_id,
                        executor_group_name
                      """);
        
        StringBuilder qb = preparePublicFilters(departmentService, requestDTO, parameters);
        
        qbData.append(qb);
        qbCount.append(qb);
        
        // Сортировка
        prepareSortAndPagination(qbData, requestDTO.getSortSetting(), requestDTO.getPageSetting());
        
        String sqlString = qbData.toString();
        log.debug("prepareFindPublicRequestsQuery(), sqlString = {}", sqlString);
        
        Query queryData = em.createNativeQuery(sqlString, Map.class);
        Query queryCount = em.createNativeQuery(qbCount.toString());
        
        parameters.forEach((paramName, paramValue) ->
                           {
                               queryData.setParameter(paramName, paramValue);
                               queryCount.setParameter(paramName, paramValue);
                           });
        if (log.isDebugEnabled()) {
            parameters.forEach((paramName, paramValue) -> log.debug("paramName = {}, paramValue = {}", paramName, paramValue));
        }
        
        return new DataAndCountQueries(queryData, queryCount);
    }
    
    public static Query prepareFindPublicPaymentRequestsQuery(
            EntityManager em, DepartmentService departmentService,
            RequestForPublicReportDTO requestDTO
                                                             ) {
        Map<String, Object> parameters = new HashMap<>();
        
        StringBuilder qbData = new StringBuilder();
        
        qbData.append("""
                      SELECT CAST(id as varchar) as id
                      """);
        
        StringBuilder qb = preparePublicFilters(departmentService, requestDTO, parameters);
        
        qbData.append(qb);
        
        // Сортировка
        prepareSortAndPagination(qbData, requestDTO.getSortSetting(), requestDTO.getPageSetting());
        
        String sqlString = qbData.toString();
        log.debug("prepareFindPublicPaymentRequestsQuery(), sqlString = {}", sqlString);
        
        Query queryData = em.createNativeQuery(sqlString);
        
        parameters.forEach(queryData::setParameter);
        if (log.isDebugEnabled()) {
            parameters.forEach((paramName, paramValue) -> log.debug("paramName = {}, paramValue = {}", paramName, paramValue));
        }
        
        return queryData;
    }
    
    private static StringBuilder preparePublicFilters(
            DepartmentService departmentService, RequestForPublicReportDTO requestDTO, Map<String, Object> parameters
                                                     ) {
        StringBuilder qb = new StringBuilder();
        
        qb.append(
                """
                 FROM reports.request as request
                WHERE request.transport_type = 'PUBLIC'
                """);
        
        if (requestDTO.getId() != null) {
            qb.append(" AND request.id = :requestId");
            parameters.put("requestId", requestDTO.getId());
        }
        
        // Список Ид тарифов
        if (requestDTO.getTariffIdSet() != null && !requestDTO.getTariffIdSet().isEmpty()) {
            qb.append(" AND request.tariff_id IN (:tariff_ids)");
            parameters.put("tariff_ids", requestDTO.getTariffIdSet());
        }
        
        // Список контрагентов
        if (requestDTO.getContractorSet() != null && !requestDTO.getContractorSet().isEmpty()) {
            qb.append(" AND request.contractor_id IN (:contractor_ids)");
            parameters.put("contractor_ids", requestDTO.getContractorSet());
        }
        
        // Дата формирование приказа на выплату
        if (requestDTO.getOrderPaymentFormationStartDate() != null) {
            if ((requestDTO.getOrderPaymentFormationStartDate().getStart() != null) &&
                (requestDTO.getOrderPaymentFormationStartDate().getEnd() != null)) {
                qb.append(" AND request.order_payment_formation_start_date BETWEEN :opf_start_date_start AND :opf_start_date_end");
                parameters.put("opf_start_date_start", requestDTO.getOrderPaymentFormationStartDate().getStart());
                parameters.put("opf_start_date_end", requestDTO.getOrderPaymentFormationStartDate().getEnd());
            }
            if ((requestDTO.getOrderPaymentFormationStartDate().getStart() != null) &&
                (requestDTO.getOrderPaymentFormationStartDate().getEnd() == null)) {
                qb.append(" AND request.order_payment_formation_start_date >= :opf_start_date_start");
                parameters.put("opf_start_date_start", requestDTO.getOrderPaymentFormationStartDate().getStart());
            }
            if ((requestDTO.getOrderPaymentFormationStartDate().getStart() == null) &&
                (requestDTO.getOrderPaymentFormationStartDate().getEnd() != null)) {
                qb.append(" AND request.order_payment_formation_start_date <= :opf_start_date_end");
                parameters.put("opf_start_date_end", requestDTO.getOrderPaymentFormationStartDate().getEnd());
            }
        }
        
        // Период выплаты
        if (requestDTO.getPaymentPeriod() != null) {
            var range = getRangeFromPeriodOfPayment(requestDTO.getPaymentPeriod());
            if (range != null) {
                qb.append(
                        " AND :opf_period_payment_start <= EXTRACT('Day' FROM order_payment_formation_start_date) " +
                        "AND EXTRACT('Day' FROM order_payment_formation_start_date) <= :opf_period_payment_end");
                parameters.put("opf_period_payment_start", range.getStart());
                parameters.put("opf_period_payment_end", range.getEnd());
            }
        }
        
        // БЕ
        if (requestDTO.getBalanceUnitSet() != null && !requestDTO.getBalanceUnitSet().isEmpty()) {
            qb.append(" AND request.balance_unit IN (:balance_units)");
            parameters.put("balance_units", new ArrayList<>(requestDTO.getBalanceUnitSet()));
        }
        
        // Желаемая дата поездки, диапазон
        {
            if (requestDTO.getDesiredDateRange() != null) {
                if ((requestDTO.getDesiredDateRange().getStart() != null) &&
                    (requestDTO.getDesiredDateRange().getEnd() != null)) {
                    qb.append(" AND request.desired_date BETWEEN :desired_date_start AND :desired_date_end");
                    parameters.put("desired_date_start", requestDTO.getDesiredDateRange().getStart());
                    parameters.put("desired_date_end", requestDTO.getDesiredDateRange().getEnd());
                }
                if ((requestDTO.getDesiredDateRange().getStart() != null) &&
                    (requestDTO.getDesiredDateRange().getEnd() == null)) {
                    qb.append(" AND request.desired_date > :desired_date_start");
                    parameters.put("desired_date_start", requestDTO.getDesiredDateRange().getStart());
                }
                if ((requestDTO.getDesiredDateRange().getStart() == null) &&
                    (requestDTO.getDesiredDateRange().getEnd() != null)) {
                    qb.append(" AND request.desired_date <= :desired_date_end");
                    parameters.put("desired_date_end", requestDTO.getDesiredDateRange().getEnd());
                }
            }
        }
        
        // Дата согласования поездки, диапазон
        if (requestDTO.getApproveDate() != null) {
            if ((requestDTO.getApproveDate().getStart() != null) &&
                (requestDTO.getApproveDate().getEnd() != null)) {
                qb.append(" AND request.approval_date BETWEEN :approval_date_start AND :approval_date_end");
                parameters.put("approval_date_start", requestDTO.getApproveDate().getStart());
                parameters.put("approval_date_end", requestDTO.getApproveDate().getEnd());
            }
            if ((requestDTO.getApproveDate().getStart() != null) &&
                (requestDTO.getApproveDate().getEnd() == null)) {
                qb.append(" AND request.approval_date > :approval_date_start");
                parameters.put("approval_date_start", requestDTO.getApproveDate().getStart());
            }
            if ((requestDTO.getApproveDate().getStart() == null) &&
                (requestDTO.getApproveDate().getEnd() != null)) {
                qb.append(" AND request.approval_date <= :approval_date_end");
                parameters.put("approval_date_end", requestDTO.getApproveDate().getEnd());
            }
        }
        
        //Группа исполнителей
        if (!CollectionUtils.isEmpty(requestDTO.getExecutorGroupIds())) {
            qb.append(" AND request.executor_group_id IN :executor_group_ids");
            parameters.put("executor_group_ids", requestDTO.getExecutorGroupIds());
        }
        
        Set<String> employeeOrganizationSet = requestDTO.getEmployeeOrganizationSet();
        boolean organizationsIsNotAll = employeeOrganizationSet != null && !employeeOrganizationSet.isEmpty() && !employeeOrganizationSet.contains(
                "all");
        // Организации пассажира
        if (organizationsIsNotAll) {
            qb.append(" AND request.organization_id IN (:organization_ids)");
            parameters.put("organization_ids", employeeOrganizationSet.stream().map(UUID::fromString).collect(Collectors.toSet()));
        }
        
        // Организация
        if (requestDTO.getOrganizationId() != null && !organizationsIsNotAll) {
            qb.append(" AND request.organization_id = :organization_id");
            parameters.put("organization_id", requestDTO.getOrganizationId());
        }
        
        // Человекочитаемый идентификатор заявки
        if (requestDTO.getRequestHumanId() != null && !requestDTO.getRequestHumanId().isEmpty()) {
            qb.append(" AND lower(request.humanreadableid) like lower(:humanreadableidLikeExpression)");
            String humanreadableidLikeExpression = "%" + requestDTO.getRequestHumanId() + "%";
            parameters.put("humanreadableidLikeExpression", humanreadableidLikeExpression);
        }
        
        // Статусы поездки
        if (requestDTO.getRequestStatusSet() != null && !requestDTO.getRequestStatusSet().isEmpty()) {
            qb.append(" AND request_status IN (:request_statuses)");
            parameters.put("request_statuses", requestDTO.getRequestStatusSet().stream().map(Enum::name).toList());
        }
        
        // Дата создания поездки, диапазон
        if (requestDTO.getCreationDate() != null) {
            if ((requestDTO.getCreationDate().getStart() != null) &&
                (requestDTO.getCreationDate().getEnd() != null)) {
                qb.append(" AND request.creation_time BETWEEN :creation_time_start AND :creation_time_end");
                parameters.put("creation_time_start", requestDTO.getCreationDate().getStart());
                parameters.put("creation_time_end", requestDTO.getCreationDate().getEnd());
            }
            if ((requestDTO.getCreationDate().getStart() != null) &&
                (requestDTO.getCreationDate().getEnd() == null)) {
                qb.append(" AND request.creation_time > :creation_time_start");
                parameters.put("creation_time_start", requestDTO.getCreationDate().getStart());
            }
            if ((requestDTO.getCreationDate().getStart() == null) &&
                (requestDTO.getCreationDate().getEnd() != null)) {
                qb.append(" AND request.creation_time <= :creation_time_end");
                parameters.put("creation_time_end", requestDTO.getCreationDate().getEnd());
            }
        }
        
        // ID цели поездки
        if (requestDTO.getPurposeSet() != null && !requestDTO.getPurposeSet().isEmpty()) {
            qb.append(" AND request.purpose_id IN (:purpose_ids)");
            parameters.put("purpose_ids", requestDTO.getPurposeSet().stream().map(TripPurposeDTO::getId).collect(Collectors.toSet()));
        }
        
        // Список оценок для фильтрации, 0 - без оценки
        if (requestDTO.getRatingMarkSet() != null && !requestDTO.getRatingMarkSet().isEmpty()) {
            qb.append(requestDTO.getRatingMarkSet().contains(0) ? " AND (request.rating_mark IS NULL OR request.rating_mark IN (:rating_marks))"
                                                                : " AND rating_mark IN (:rating_marks)");
            parameters.put("rating_marks", requestDTO.getRatingMarkSet());
        }
        
        // Стоимость поездки (диапазон), с фронта рубли, на бэке копейки
        if (requestDTO.getExpectedCost() != null) {
            if (requestDTO.getExpectedCost().getStart() != null && requestDTO.getExpectedCost().getEnd() == null) {
                qb.append(" AND request.expected_cost >= :expected_cost_start");
                parameters.put("expected_cost_start", requestDTO.getExpectedCost().getStart() * 100);
            }
            if (requestDTO.getExpectedCost().getStart() == null && requestDTO.getExpectedCost().getEnd() != null) {
                qb.append(" AND request.expected_cost <= :expected_cost_end");
                parameters.put("expected_cost_end", requestDTO.getExpectedCost().getEnd() * 100);
            }
            if (requestDTO.getExpectedCost().getStart() != null && requestDTO.getExpectedCost().getEnd() != null) {
                qb.append(" AND request.expected_cost BETWEEN :expected_cost_start AND :expected_cost_end");
                parameters.put("expected_cost_start", requestDTO.getExpectedCost().getStart() * 100);
                parameters.put("expected_cost_end", requestDTO.getExpectedCost().getEnd() * 100);
            }
        }
        
        // Длительность поездки (диапазон)
        if (requestDTO.getExpectedDistance() != null) {
            if (requestDTO.getExpectedDistance().getStart() != null && requestDTO.getExpectedDistance().getEnd() != null) {
                qb.append(" AND request.expected_distance BETWEEN :expected_distance_start AND :expected_distance_end");
                parameters.put("expected_distance_start", requestDTO.getExpectedDistance().getStart());
                parameters.put("expected_distance_end", requestDTO.getExpectedDistance().getEnd());
            }
            if (requestDTO.getExpectedDistance().getStart() != null && requestDTO.getExpectedDistance().getEnd() == null) {
                qb.append(" AND request.expected_distance >= :expected_distance_start");
                parameters.put("expected_distance_start", requestDTO.getExpectedDistance().getStart());
            }
            if (requestDTO.getExpectedDistance().getStart() == null && requestDTO.getExpectedDistance().getEnd() != null) {
                qb.append(" AND request.expected_distance <= :expected_distance_end");
                parameters.put("expected_distance_end", requestDTO.getExpectedDistance().getEnd());
            }
        }
        
        // Место возникновения затрат
        if (requestDTO.getCostCenter() != null && !requestDTO.getCostCenter().isEmpty()) {
            qb.append(" AND request.cost_center = :cost_center");
            parameters.put("cost_center", requestDTO.getCostCenter());
        }
        
        StringBuilder empQueryBuilder = new StringBuilder();
        
        if (requestDTO.getEmployeeFIO() != null && !requestDTO.getEmployeeFIO().isEmpty()) {
            var fullName = requestDTO.getEmployeeFIO().replaceAll("\\s+", " ");
            switch (fullName.split(" ").length) {
                case 1 -> empQueryBuilder.append(
                        " AND (lower(last_name) LIKE lower(:fullNameLikeExpression) OR lower(first_name) LIKE lower(:fullNameLikeExpression) " +
                        "OR lower(patronymic) LIKE lower(:fullNameLikeExpression))");
                case 2 -> empQueryBuilder.append(" AND (lower(concat(last_name, ' ', first_name)) LIKE lower(:fullNameLikeExpression) OR " +
                                                 "lower(concat(first_name, ' ', last_name)) LIKE lower(:fullNameLikeExpression) OR " +
                                                 "lower(concat(first_name, ' ', patronymic)) LIKE lower(:fullNameLikeExpression))");
                default -> empQueryBuilder.append(
                        " AND (lower(concat(last_name, ' ', first_name, ' ', patronymic)) LIKE lower(:fullNameLikeExpression)" +
                        " OR lower(concat(first_name, ' ', patronymic, ' ', last_name)) LIKE lower(:fullNameLikeExpression))");
            }
            var fullNameLikeExpression = "%" + fullName + "%";
            parameters.put("fullNameLikeExpression", fullNameLikeExpression);
        }
        
        // Табельный № заявителя
        if (requestDTO.getPersonnelNumber() != null) {
            empQueryBuilder.append(" AND personnel_number = :personnel_number");
            parameters.put("personnel_number", requestDTO.getPersonnelNumber());
        }
        
        // ОЕ Подразделения
        if (requestDTO.getDepartmentCode() != null) {
            empQueryBuilder.append(" AND department_id IN (SELECT id from reports.department WHERE code = :code)");
            parameters.put("code", requestDTO.getDepartmentCode());
        }
        
        if (requestDTO.getEmployeeDepartmentSet() != null && !requestDTO.getEmployeeDepartmentSet().isEmpty()) {
            Set<UUID> employeeDepartmentSet =
                    departmentService.getDepartmentWithAllChildren((HashSet<UUID>) requestDTO.getEmployeeDepartmentSet());
            empQueryBuilder.append(" AND department_id in (:department_ids)");
            parameters.put("department_ids", employeeDepartmentSet);
        }
        if (!empQueryBuilder.isEmpty()) {
            qb.append(" AND request.passenger_id IN (SELECT id FROM reports.employee WHERE 1 = 1");
            qb.append(empQueryBuilder);
            qb.append(")");
        }
        
        // Причина отмены
        if (requestDTO.getRequestStatusCodes() != null && !requestDTO.getRequestStatusCodes().isEmpty()) {
            qb.append(" AND request.request_status_code IN :request_status_codes");
            parameters.put("request_status_codes", requestDTO.getRequestStatusCodes());
        }
        
        // Контрольный срок
        if (requestDTO.getDeadlineState() != null) {
            if (requestDTO.getDeadlineState()) {
                qb.append(" AND request.deadline_state = :deadline_state");
                parameters.put("deadline_state", DeadlineState.RED.name());
            } else {
                qb.append(" AND (request.deadline_state = :deadline_state OR request.deadline_state is null)");
                parameters.put("deadline_state", DeadlineState.NONE.name());
            }
        }
        
        // Экономия
        if (requestDTO.getSavings() != null) {
            if (requestDTO.getSavings()) {
                qb.append(" AND request.savings_cash > 0");
            } else {
                qb.append(" AND (request.savings_cash = 0 OR request.savings_cash is null)");
            }
        }
        
        // Дата закрытия обращения
        if (requestDTO.getRequestClosedDatetime() != null) {
            qb.append(" AND request.request_closed_datetime BETWEEN :request_closed_datetime_start AND :request_closed_datetime_end");
            parameters.put("request_closed_datetime_start", requestDTO.getRequestClosedDatetime().getStart());
            parameters.put("request_closed_datetime_end", requestDTO.getRequestClosedDatetime().getEnd());
        }
        
        // public_compensation_document_exist
        if (requestDTO.getPublicCompensationDocumentExist() != null) {
            qb.append(" AND request.public_compensation_document_exist = :public_compensation_document_exist");
            parameters.put("public_compensation_document_exist", requestDTO.getPublicCompensationDocumentExist());
        }
        
        // Добавление фильтров по поздразделению
        addDepartmentsFilter(requestDTO, parameters, qb);
        
        return qb;
    }
    
    private static StringBuilder preparePersonalFilters(
            DepartmentService departmentService, RequestForPersonalReportDTO requestDTO, Map<String, Object> parameters
                                                       ) {
        StringBuilder qb = new StringBuilder();
        
        qb.append(
                """
                 FROM reports.request as request
                WHERE request.transport_type = 'PERSONAL'
                """);
        
        if (requestDTO.getId() != null) {
            qb.append(" AND request.id = :requestId");
            parameters.put("requestId", requestDTO.getId());
            return qb;
        }
        
        // Список Ид тарифов
        if (requestDTO.getTariffIdSet() != null && !requestDTO.getTariffIdSet().isEmpty()) {
            qb.append(" AND request.tariff_id IN (:tariff_ids)");
            parameters.put("tariff_ids", requestDTO.getTariffIdSet());
        }
        
        // Список контрагентов
        if (requestDTO.getContractorSet() != null && !requestDTO.getContractorSet().isEmpty()) {
            qb.append(" AND request.contractor_id IN (:contractor_ids)");
            parameters.put("contractor_ids", requestDTO.getContractorSet());
        }
        
        // Дата формирование приказа на выплату
        if (requestDTO.getOrderPaymentFormationFinishingDate() != null) {
            if ((requestDTO.getOrderPaymentFormationFinishingDate().getStart() != null) &&
                (requestDTO.getOrderPaymentFormationFinishingDate().getEnd() != null)) {
                qb.append(" AND request.order_payment_formation_finishing_date BETWEEN :opf_finishing_date_start AND :opf_finishing_date_end");
                parameters.put("opf_finishing_date_start", requestDTO.getOrderPaymentFormationFinishingDate().getStart());
                parameters.put("opf_finishing_date_end", requestDTO.getOrderPaymentFormationFinishingDate().getEnd());
            }
            if ((requestDTO.getOrderPaymentFormationFinishingDate().getStart() != null) &&
                (requestDTO.getOrderPaymentFormationFinishingDate().getEnd() == null)) {
                qb.append(" AND request.order_payment_formation_finishing_date > :opf_finishing_date_start");
                parameters.put("opf_finishing_date_start", requestDTO.getOrderPaymentFormationFinishingDate().getStart());
            }
            if ((requestDTO.getOrderPaymentFormationFinishingDate().getStart() == null) &&
                (requestDTO.getOrderPaymentFormationFinishingDate().getEnd() != null)) {
                qb.append(" AND request.order_payment_formation_finishing_date <= :opf_finishing_date_end");
                parameters.put("opf_finishing_date_end", requestDTO.getOrderPaymentFormationFinishingDate().getEnd());
            }
        }
        
        // Желаемая дата поездки, диапазон
        if (requestDTO.getDesiredDateRange() != null) {
            if ((requestDTO.getDesiredDateRange().getStart() != null) &&
                (requestDTO.getDesiredDateRange().getEnd() != null)) {
                qb.append(" AND request.desired_date BETWEEN :desired_date_start AND :desired_date_end");
                parameters.put("desired_date_start", requestDTO.getDesiredDateRange().getStart());
                parameters.put("desired_date_end", requestDTO.getDesiredDateRange().getEnd());
            }
            if ((requestDTO.getDesiredDateRange().getStart() != null) &&
                (requestDTO.getDesiredDateRange().getEnd() == null)) {
                qb.append(" AND request.desired_date > :desired_date_start");
                parameters.put("desired_date_start", requestDTO.getDesiredDateRange().getStart());
            }
            if ((requestDTO.getDesiredDateRange().getStart() == null) &&
                (requestDTO.getDesiredDateRange().getEnd() != null)) {
                qb.append(" AND request.desired_date <= :desired_date_end");
                parameters.put("desired_date_end", requestDTO.getDesiredDateRange().getEnd());
            }
        }
        
        // Дата утверждения поездки (Дата начала формирования приказа на выплату), диапазон
        if (requestDTO.getOrderPaymentFormationStartRange() != null) {
            if ((requestDTO.getOrderPaymentFormationStartRange().getStart() != null) &&
                (requestDTO.getOrderPaymentFormationStartRange().getEnd() != null)) {
                qb.append(" AND request.order_payment_formation_start_date BETWEEN :opf_start_date_start AND :opf_start_date_end");
                parameters.put("opf_start_date_start", requestDTO.getOrderPaymentFormationStartRange().getStart());
                parameters.put("opf_start_date_end", requestDTO.getOrderPaymentFormationStartRange().getEnd());
            }
            if ((requestDTO.getOrderPaymentFormationStartRange().getStart() != null) &&
                (requestDTO.getOrderPaymentFormationStartRange().getEnd() == null)) {
                qb.append(" AND request.order_payment_formation_start_date > :opf_start_date_start");
                parameters.put("opf_start_date_start", requestDTO.getOrderPaymentFormationStartRange().getStart());
            }
            if ((requestDTO.getOrderPaymentFormationStartRange().getStart() == null) &&
                (requestDTO.getOrderPaymentFormationStartRange().getEnd() != null)) {
                qb.append(" AND request.order_payment_formation_start_date <= :opf_start_date_end");
                parameters.put("opf_start_date_end", requestDTO.getOrderPaymentFormationStartRange().getEnd());
            }
        }
        
        // Период выплаты
        if (requestDTO.getPaymentPeriod() != null) {
            var range = getRangeFromPeriodOfPayment(requestDTO.getPaymentPeriod());
            if (range != null) {
                qb.append(
                        " AND :opf_period_payment_start <= EXTRACT('Day' FROM order_payment_formation_start_date) " +
                        "AND EXTRACT('Day' FROM order_payment_formation_start_date) <= :opf_period_payment_end");
                parameters.put("opf_period_payment_start", range.getStart());
                parameters.put("opf_period_payment_end", range.getEnd());
            }
        }
        
        // Количество пассажиров
        if (requestDTO.getPassengerCountSet() != null && !requestDTO.getPassengerCountSet().isEmpty()) {
            qb.append(" AND request.passenger_count in (:passengerCountSet)");
            parameters.put("passengerCountSet", requestDTO.getPassengerCountSet());
        }
        
        // Флаг совместной поездки
        if (requestDTO.getCoopTrip() != null) {
            qb.append(" AND request.coop_trip = :coop_trip");
            parameters.put("coop_trip", requestDTO.getCoopTrip());
        }
        
        // Признак пассажира
        if (requestDTO.getPassenger() != null) {
            if (requestDTO.getPassenger()) {
                qb.append(" AND request.coop_trip = true AND request.shared_ride_owner = false");
            } else {
                qb.append(" AND (request.coop_trip = true AND request.shared_ride_owner = true OR request.coop_trip = false)");
            }
        }
        
        // ID совместной поездки
        if (requestDTO.getSharedRideId() != null) {
            qb.append(" AND request.ride_id = :ride_id");
            parameters.put("ride_id", requestDTO.getSharedRideId());
        }
        
        // Дата согласования поездки, диапазон
        if (requestDTO.getApproveDate() != null) {
            if ((requestDTO.getApproveDate().getStart() != null) &&
                (requestDTO.getApproveDate().getEnd() != null)) {
                qb.append(" AND request.approval_date BETWEEN :approval_date_start AND :approval_date_end");
                parameters.put("approval_date_start", requestDTO.getApproveDate().getStart());
                parameters.put("approval_date_end", requestDTO.getApproveDate().getEnd());
            }
            if ((requestDTO.getApproveDate().getStart() != null) &&
                (requestDTO.getApproveDate().getEnd() == null)) {
                qb.append(" AND request.approval_date > :approval_date_start");
                parameters.put("approval_date_start", requestDTO.getApproveDate().getStart());
            }
            if ((requestDTO.getApproveDate().getStart() == null) &&
                (requestDTO.getApproveDate().getEnd() != null)) {
                qb.append(" AND request.approval_date <= :approval_date_end");
                parameters.put("approval_date_end", requestDTO.getApproveDate().getEnd());
            }
        }
        
        //Группа исполнителей
        if (!CollectionUtils.isEmpty(requestDTO.getExecutorGroupIds())) {
            qb.append(" AND request.executor_group_id IN :executor_group_ids");
            parameters.put("executor_group_ids", requestDTO.getExecutorGroupIds());
        }
        
        Set<String> employeeOrganizationSet = requestDTO.getEmployeeOrganizationSet();
        boolean organizationsIsNotAll = employeeOrganizationSet != null && !employeeOrganizationSet.isEmpty() && !employeeOrganizationSet.contains(
                "all");
        // Организации пассажира
        if (organizationsIsNotAll) {
            qb.append(" AND request.organization_id IN (:organization_ids)");
            parameters.put("organization_ids", employeeOrganizationSet.stream().map(UUID::fromString).collect(Collectors.toSet()));
        }
        
        // Организация
        if (requestDTO.getOrganizationId() != null && !organizationsIsNotAll) {
            qb.append(" AND request.organization_id = :organization_id");
            parameters.put("organization_id", requestDTO.getOrganizationId());
        }
        
        // Человекочитаемый идентификатор заявки
        if (requestDTO.getRequestHumanId() != null && !requestDTO.getRequestHumanId().isEmpty()) {
            qb.append(" AND lower(request.humanreadableid) like lower(:humanreadableidLikeExpression)");
            String humanreadableidLikeExpression = "%" + requestDTO.getRequestHumanId() + "%";
            parameters.put("humanreadableidLikeExpression", humanreadableidLikeExpression);
        }
        
        // Статусы поездки
        if (requestDTO.getRequestStatusSet() != null && !requestDTO.getRequestStatusSet().isEmpty()) {
            qb.append(" AND request.request_status IN (:request_statuses)");
            parameters.put("request_statuses", requestDTO.getRequestStatusSet().stream().map(Enum::name).toList());
        }
        
        // Дата создания поездки, диапазон
        if (requestDTO.getCreationDate() != null) {
            if ((requestDTO.getCreationDate().getStart() != null) &&
                (requestDTO.getCreationDate().getEnd() != null)) {
                qb.append(" AND request.creation_time BETWEEN :creation_time_start AND :creation_time_end");
                parameters.put("creation_time_start", requestDTO.getCreationDate().getStart());
                parameters.put("creation_time_end", requestDTO.getCreationDate().getEnd());
            }
            if ((requestDTO.getCreationDate().getStart() != null) &&
                (requestDTO.getCreationDate().getEnd() == null)) {
                qb.append(" AND request.creation_time > :creation_time_start");
                parameters.put("creation_time_start", requestDTO.getCreationDate().getStart());
            }
            if ((requestDTO.getCreationDate().getStart() == null) &&
                (requestDTO.getCreationDate().getEnd() != null)) {
                qb.append(" AND request.creation_time <= :creation_time_end");
                parameters.put("creation_time_end", requestDTO.getCreationDate().getEnd());
            }
        }
        
        // ID цели поездки
        if (requestDTO.getPurposeSet() != null && !requestDTO.getPurposeSet().isEmpty()) {
            qb.append(" AND request.purpose_id IN (:purpose_ids)");
            parameters.put("purpose_ids", requestDTO.getPurposeSet().stream().map(TripPurposeDTO::getId).toList());
        }
        
        // Список оценок для фильтрации, 0 - без оценки
        if (requestDTO.getRatingMarkSet() != null && !requestDTO.getRatingMarkSet().isEmpty()) {
            qb.append(requestDTO.getRatingMarkSet().contains(0) ? " AND (request.rating_mark IS NULL OR request.rating_mark IN (:rating_marks))"
                                                                : " AND request.rating_mark IN (:rating_marks)");
            parameters.put("rating_marks", requestDTO.getRatingMarkSet());
        }
        
        // Стоимость поездки (диапазон), с фронта рубли, на бэке копейки
        if (requestDTO.getExpectedCost() != null) {
            if (requestDTO.getExpectedCost().getStart() != null && requestDTO.getExpectedCost().getEnd() == null) {
                qb.append(" AND request.expected_cost >= :expected_cost_start");
                parameters.put("expected_cost_start", requestDTO.getExpectedCost().getStart() * 100);
            }
            if (requestDTO.getExpectedCost().getStart() == null && requestDTO.getExpectedCost().getEnd() != null) {
                qb.append(" AND request.expected_cost <= :expected_cost_end");
                parameters.put("expected_cost_end", requestDTO.getExpectedCost().getEnd() * 100);
            }
            if (requestDTO.getExpectedCost().getStart() != null && requestDTO.getExpectedCost().getEnd() != null) {
                qb.append(" AND request.expected_cost BETWEEN :expected_cost_start AND :expected_cost_end");
                parameters.put("expected_cost_start", requestDTO.getExpectedCost().getStart() * 100);
                parameters.put("expected_cost_end", requestDTO.getExpectedCost().getEnd() * 100);
            }
        }
        
        // Длительность поездки (диапазон)
        if (requestDTO.getExpectedDistance() != null) {
            if (requestDTO.getExpectedDistance().getStart() != null && requestDTO.getExpectedDistance().getEnd() != null) {
                qb.append(" AND request.expected_distance BETWEEN :expected_distance_start AND :expected_distance_end");
                parameters.put("expected_distance_start", requestDTO.getExpectedDistance().getStart());
                parameters.put("expected_distance_end", requestDTO.getExpectedDistance().getEnd());
            }
            if (requestDTO.getExpectedDistance().getStart() != null && requestDTO.getExpectedDistance().getEnd() == null) {
                qb.append(" AND request.expected_distance >= :expected_distance_start");
                parameters.put("expected_distance_start", requestDTO.getExpectedDistance().getStart());
            }
            if (requestDTO.getExpectedDistance().getStart() == null && requestDTO.getExpectedDistance().getEnd() != null) {
                qb.append(" AND request.expected_distance <= :expected_distance_end");
                parameters.put("expected_distance_end", requestDTO.getExpectedDistance().getEnd());
            }
        }
        
        // БЕ
        if (requestDTO.getBalanceUnitSet() != null && !requestDTO.getBalanceUnitSet().isEmpty()) {
            qb.append(" AND request.balance_unit IN (:balance_units)");
            parameters.put("balance_units", new ArrayList<>(requestDTO.getBalanceUnitSet()));
        }
        
        // Место возникновения затрат
        if (requestDTO.getCostCenter() != null && !requestDTO.getCostCenter().isEmpty()) {
            qb.append(" AND request.cost_center = :cost_center");
            parameters.put("cost_center", requestDTO.getCostCenter());
        }
        
        StringBuilder empQueryBuilder = new StringBuilder();
        
        if (requestDTO.getEmployeeFIO() != null && !requestDTO.getEmployeeFIO().isEmpty()) {
            var fullName = requestDTO.getEmployeeFIO().replaceAll("\\s+", " ");
            switch (fullName.split(" ").length) {
                case 1 -> empQueryBuilder.append(
                        " AND (lower(last_name) LIKE lower(:fullNameLikeExpression) OR lower(first_name) LIKE lower(:fullNameLikeExpression) " +
                        "OR lower(patronymic) LIKE lower(:fullNameLikeExpression))");
                case 2 -> empQueryBuilder.append(" AND (lower(concat(last_name, ' ', first_name)) LIKE lower(:fullNameLikeExpression) OR " +
                                                 "lower(concat(first_name, ' ', last_name)) LIKE lower(:fullNameLikeExpression) OR " +
                                                 "lower(concat(first_name, ' ', patronymic)) LIKE lower(:fullNameLikeExpression))");
                default -> empQueryBuilder.append(
                        " AND (lower(concat(last_name, ' ', first_name, ' ', patronymic)) LIKE lower(:fullNameLikeExpression)" +
                        " OR lower(concat(first_name, ' ', patronymic, ' ', last_name)) LIKE lower(:fullNameLikeExpression))");
            }
            var fullNameLikeExpression = "%" + fullName + "%";
            parameters.put("fullNameLikeExpression", fullNameLikeExpression);
        }
        
        if (requestDTO.getEmployeeDepartmentSet() != null && !requestDTO.getEmployeeDepartmentSet().isEmpty()) {
            Set<UUID> employeeDepartmentSet = departmentService.getDepartmentWithAllChildren((HashSet<UUID>) requestDTO.getEmployeeDepartmentSet());
            empQueryBuilder.append(" AND department_id in (:department_ids)");
            parameters.put("department_ids", employeeDepartmentSet);
        }
        
        // Табельный № заявителя
        if (requestDTO.getPersonnelNumber() != null) {
            empQueryBuilder.append(" AND personnel_number = :personnel_number");
            parameters.put("personnel_number", requestDTO.getPersonnelNumber());
        }
        
        // ОЕ Подразделения
        if (requestDTO.getDepartmentCode() != null) {
            empQueryBuilder.append(" AND department_id IN (SELECT id from reports.department WHERE code = :code)");
            parameters.put("code", requestDTO.getDepartmentCode());
        }
        
        if (!empQueryBuilder.isEmpty()) {
            qb.append(" AND request.passenger_id IN (SELECT id FROM reports.employee WHERE 1 = 1");
            qb.append(empQueryBuilder);
            qb.append(")");
        }
        
        // Причина отмены
        if (requestDTO.getRequestStatusCodes() != null && !requestDTO.getRequestStatusCodes().isEmpty()) {
            qb.append(" AND request.request_status_code IN :request_status_codes");
            parameters.put("request_status_codes", requestDTO.getRequestStatusCodes());
        }
        
        // Контрольный срок
        if (requestDTO.getDeadlineState() != null) {
            if (requestDTO.getDeadlineState()) {
                qb.append(" AND request.deadline_state = :deadline_state");
                parameters.put("deadline_state", DeadlineState.RED.name());
            } else {
                qb.append(" AND (request.deadline_state = :deadline_state OR request.deadline_state is null)");
                parameters.put("deadline_state", DeadlineState.NONE.name());
            }
        }
        
        // Экономия
        if (requestDTO.getSavings() != null) {
            if (requestDTO.getSavings()) {
                qb.append(" AND request.savings_cash > 0");
            } else {
                qb.append(" AND (request.savings_cash = 0 OR request.savings_cash is null)");
            }
        }
        
        // Дата закрытия обращения
        if (requestDTO.getRequestClosedDatetime() != null) {
            qb.append(" AND request.request_closed_datetime BETWEEN :request_closed_datetime_start AND :request_closed_datetime_end");
            parameters.put("request_closed_datetime_start", requestDTO.getRequestClosedDatetime().getStart());
            parameters.put("request_closed_datetime_end", requestDTO.getRequestClosedDatetime().getEnd());
        }
        
        // Добавление фильтров по поздразделению
        addDepartmentsFilter(requestDTO, parameters, qb);
        
        return qb;
    }
    
    public static DataAndCountQueries prepareFindGroupTransferRequestsQuery(
            EntityManager em, DepartmentService departmentService,
            RequestForGroupTransferReportDTO requestDTO
                                                                           ) {
        Map<String, Object> parameters = new HashMap<>();
        
        StringBuilder qb = new StringBuilder();
        StringBuilder qbData = new StringBuilder();
        StringBuilder qbCount = new StringBuilder();
        
        qbCount.append(
                """
                SELECT count(*) AS cnt
                """);
        qbData.append("""
                      SELECT CAST(request.id as uuid) as id,
                        request.humanreadableid,
                        CAST(request.author_id as uuid) as author_id,
                        CAST(request.passenger_id as uuid) as passenger_id,
                        CAST(request.personal_car as uuid) as personal_car,
                        request.creation_time,
                        request.desired_date,
                        request.approval_date,
                        request.transport_type,
                        request.request_status,
                        request.request_status_code,
                        CAST(request.purpose_id as uuid) as purpose_id,
                        request.passenger_count,
                        request.expected_cost,
                        request.expected_distance,
                        request.expected_time,
                        request.rating_mark,
                        request.rating_comment,
                        request.change_date,
                        request.time_zone,
                        CAST(request.tariff_id as uuid) as tariff_id,
                        request.trip_class,
                        request.approval_state,
                        CAST(request.approved_by_id as uuid) as approved_by_id,
                        CAST(request.contractor_id as uuid) as contractor_id,
                        request.comment_for_driver,
                        request.finished_time,
                        CAST(request.driver as varchar) as driver,
                        CAST(request.vehicle as varchar) as vehicle,
                        request.passenger_department1,
                        request.passenger_department2,
                        request.passenger_department3,
                        request.passenger_department4,
                        request.passenger_department5,
                        request.passenger_department6,
                        request.departure_address,
                        request.intermediate_addresses,
                        request.destination_address,
                        request.resolution,
                        request.organization_id,
                        request.limit_id,
                        request.deadline,
                        request.deadline_state,
                        request.driver_arrived_datetime,
                        request.cost_center,
                        request.request_closed_datetime,
                        request.vip,
                        request.source,
                        request.comment_for_purpose,
                        request.savings_cash,
                        request.min_taxi_tariff_cost,
                        contract.contract_number,
                        request.executor_group_id,
                        request.executor_group_name
                      """);
        
        qb.append(
                """
                 FROM reports.request as request
                 LEFT JOIN reports.tariff as tariff ON request.tariff_id = tariff.id
                 LEFT JOIN reports.contract as contract ON tariff.contract_id = contract.id
                WHERE request.transport_type = 'GROUP_TRANSFER'
                """);
        
        // Список Ид тарифов
        if (requestDTO.getTariffIdSet() != null && !requestDTO.getTariffIdSet().isEmpty()) {
            qb.append(" AND request.tariff_id IN (:tariff_ids)");
            parameters.put("tariff_ids", requestDTO.getTariffIdSet());
        }
        
        // Список контрагентов
        if (requestDTO.getContractorSet() != null && !requestDTO.getContractorSet().isEmpty()) {
            qb.append(" AND request.contractor_id IN (:contractor_ids)");
            parameters.put("contractor_ids", requestDTO.getContractorSet());
        }
        
        // Список классов транспорта
        if (requestDTO.getGroupTransferClassList() != null && !requestDTO.getGroupTransferClassList().isEmpty()) {
            qb.append(" AND request.trip_class IN (:trip_class_ids)");
            parameters.put("trip_class_ids", requestDTO.getGroupTransferClassList().stream().map(GroupTransferClass::name).toList());
        }
        
        // Список оценок для фильтрации, 0 - без оценки
        if (requestDTO.getRatingMarkSet() != null && !requestDTO.getRatingMarkSet().isEmpty()) {
            qb.append(requestDTO.getRatingMarkSet().contains(0) ? " AND (request.rating_mark IS NULL OR request.rating_mark IN (:rating_marks))"
                                                                : " AND request.rating_mark IN (:rating_marks)");
            parameters.put("rating_marks", requestDTO.getRatingMarkSet());
        }
        
        // Желаемая дата поездки, диапазон
        if (requestDTO.getDesiredDateRange() != null) {
            if ((requestDTO.getDesiredDateRange().getStart() != null) &&
                (requestDTO.getDesiredDateRange().getEnd() != null)) {
                qb.append(" AND request.desired_date BETWEEN :desired_date_start AND :desired_date_end");
                parameters.put("desired_date_start", requestDTO.getDesiredDateRange().getStart());
                parameters.put("desired_date_end", requestDTO.getDesiredDateRange().getEnd());
            }
            if ((requestDTO.getDesiredDateRange().getStart() != null) &&
                (requestDTO.getDesiredDateRange().getEnd() == null)) {
                qb.append(" AND request.desired_date > :desired_date_start");
                parameters.put("desired_date_start", requestDTO.getDesiredDateRange().getStart());
            }
            if ((requestDTO.getDesiredDateRange().getStart() == null) &&
                (requestDTO.getDesiredDateRange().getEnd() != null)) {
                qb.append(" AND request.desired_date <= :desired_date_end");
                parameters.put("desired_date_end", requestDTO.getDesiredDateRange().getEnd());
            }
        }
        
        // Дистанция поездки факт (диапазон)
        if (requestDTO.getFactDistance() != null) {
            if (requestDTO.getFactDistance().getStart() != null && requestDTO.getFactDistance().getEnd() == null) {
                qb.append(" AND request.id IN (SELECT id FROM reports.taxi_trip WHERE trip_fact_distance >= :trip_fact_distance_start)");
                parameters.put("trip_fact_distance_start", requestDTO.getFactDistance().getStart());
            }
            if (requestDTO.getFactDistance().getStart() == null && requestDTO.getFactDistance().getEnd() != null) {
                qb.append(" AND request.id IN (SELECT id FROM reports.taxi_trip WHERE trip_fact_distance <= :trip_fact_distance_end)");
                parameters.put("trip_fact_distance_end", requestDTO.getFactDistance().getEnd());
            }
            if (requestDTO.getFactDistance().getStart() != null && requestDTO.getFactDistance().getEnd() != null) {
                qb.append(" AND request.id IN (SELECT id FROM reports.taxi_trip WHERE trip_fact_distance BETWEEN :trip_fact_distance_start AND " +
                          ":trip_fact_distance_end)");
                parameters.put("trip_fact_distance_start", requestDTO.getFactDistance().getStart());
                parameters.put("trip_fact_distance_end", requestDTO.getFactDistance().getEnd());
            }
        }
        
        // Количество пассажиров
        if (requestDTO.getPassengerCountSet() != null && !requestDTO.getPassengerCountSet().isEmpty()) {
            qb.append(" AND request.passenger_count in (:passengerCountSet)");
            parameters.put("passengerCountSet", requestDTO.getPassengerCountSet());
        }
        
        //Группа исполнителей
        if (!CollectionUtils.isEmpty(requestDTO.getExecutorGroupIds())) {
            qb.append(" AND request.executor_group_id IN :executor_group_ids");
            parameters.put("executor_group_ids", requestDTO.getExecutorGroupIds());
        }
        
        Set<String> employeeOrganizationSet = requestDTO.getEmployeeOrganizationSet();
        boolean organizationsIsNotAll = employeeOrganizationSet != null && !employeeOrganizationSet.isEmpty() && !employeeOrganizationSet.contains(
                "all");
        // Организации пассажира
        if (organizationsIsNotAll) {
            qb.append(" AND request.organization_id IN (:organization_ids)");
            parameters.put("organization_ids", employeeOrganizationSet.stream().map(UUID::fromString).collect(Collectors.toSet()));
        }
        
        // Организация
        if (requestDTO.getOrganizationId() != null && !organizationsIsNotAll) {
            qb.append(" AND request.organization_id = :organization_id");
            parameters.put("organization_id", requestDTO.getOrganizationId());
        }
        
        // Человекочитаемый идентификатор заявки
        if (requestDTO.getRequestHumanId() != null && !requestDTO.getRequestHumanId().isEmpty()) {
            qb.append(" AND lower(request.humanreadableid) like lower(:humanreadableidLikeExpression)");
            String humanreadableidLikeExpression = "%" + requestDTO.getRequestHumanId() + "%";
            parameters.put("humanreadableidLikeExpression", humanreadableidLikeExpression);
        }
        
        // Статусы поездки
        if (requestDTO.getRequestStatusSet() != null && !requestDTO.getRequestStatusSet().isEmpty()) {
            qb.append(" AND request.request_status IN (:request_statuses)");
            parameters.put("request_statuses", requestDTO.getRequestStatusSet().stream().map(Enum::name).toList());
        }
        
        // Дата создания поездки, диапазон
        if (requestDTO.getCreationDate() != null) {
            if ((requestDTO.getCreationDate().getStart() != null) &&
                (requestDTO.getCreationDate().getEnd() != null)) {
                qb.append(" AND request.creation_time BETWEEN :creation_time_start AND :creation_time_end");
                parameters.put("creation_time_start", requestDTO.getCreationDate().getStart());
                parameters.put("creation_time_end", requestDTO.getCreationDate().getEnd());
            }
            if ((requestDTO.getCreationDate().getStart() != null) &&
                (requestDTO.getCreationDate().getEnd() == null)) {
                qb.append(" AND request.creation_time > :creation_time_start");
                parameters.put("creation_time_start", requestDTO.getCreationDate().getStart());
            }
            if ((requestDTO.getCreationDate().getStart() == null) &&
                (requestDTO.getCreationDate().getEnd() != null)) {
                qb.append(" AND request.creation_time <= :creation_time_end");
                parameters.put("creation_time_end", requestDTO.getCreationDate().getEnd());
            }
        }
        
        // ID цели поездки
        if (requestDTO.getPurposeSet() != null && !requestDTO.getPurposeSet().isEmpty()) {
            qb.append(" AND request.purpose_id IN (:purpose_ids)");
            parameters.put("purpose_ids", requestDTO.getPurposeSet().stream().map(TripPurposeDTO::getId).toList());
        }
        
        // Стоимость поездки (диапазон), с фронта рубли, на бэке копейки
        if (requestDTO.getExpectedCost() != null) {
            if (requestDTO.getExpectedCost().getStart() != null && requestDTO.getExpectedCost().getEnd() == null) {
                qb.append(" AND request.expected_cost >= :expected_cost_start");
                parameters.put("expected_cost_start", requestDTO.getExpectedCost().getStart() * 100);
            }
            if (requestDTO.getExpectedCost().getStart() == null && requestDTO.getExpectedCost().getEnd() != null) {
                qb.append(" AND request.expected_cost <= :expected_cost_end");
                parameters.put("expected_cost_end", requestDTO.getExpectedCost().getEnd() * 100);
            }
            if (requestDTO.getExpectedCost().getStart() != null && requestDTO.getExpectedCost().getEnd() != null) {
                qb.append(" AND request.expected_cost BETWEEN :expected_cost_start AND :expected_cost_end");
                parameters.put("expected_cost_start", requestDTO.getExpectedCost().getStart() * 100);
                parameters.put("expected_cost_end", requestDTO.getExpectedCost().getEnd() * 100);
            }
        }
        
        // Длительность поездки (диапазон)
        if (requestDTO.getExpectedDistance() != null) {
            if (requestDTO.getExpectedDistance().getStart() != null && requestDTO.getExpectedDistance().getEnd() != null) {
                qb.append(" AND request.expected_distance BETWEEN :expected_distance_start AND :expected_distance_end");
                parameters.put("expected_distance_start", requestDTO.getExpectedDistance().getStart());
                parameters.put("expected_distance_end", requestDTO.getExpectedDistance().getEnd());
            }
            if (requestDTO.getExpectedDistance().getStart() != null && requestDTO.getExpectedDistance().getEnd() == null) {
                qb.append(" AND request.expected_distance >= :expected_distance_start");
                parameters.put("expected_distance_start", requestDTO.getExpectedDistance().getStart());
            }
            if (requestDTO.getExpectedDistance().getStart() == null && requestDTO.getExpectedDistance().getEnd() != null) {
                qb.append(" AND request.expected_distance <= :expected_distance_end");
                parameters.put("expected_distance_end", requestDTO.getExpectedDistance().getEnd());
            }
        }
        
        // Состояние проверки контрольного срока
        if (requestDTO.getDeadlineState() != null) {
            if (requestDTO.getDeadlineState()) {
                qb.append(" AND request.deadline_state = :deadline_state");
                parameters.put("deadline_state", DeadlineState.RED.name());
            } else {
                qb.append(" AND (request.deadline_state = :deadline_state OR request.deadline_state is null)");
                parameters.put("deadline_state", DeadlineState.NONE.name());
            }
        }
        
        // Место возникновения затрат
        if (requestDTO.getCostCenter() != null && !requestDTO.getCostCenter().isEmpty()) {
            qb.append(" AND request.cost_center = :cost_center");
            parameters.put("cost_center", requestDTO.getCostCenter());
        }
        
        StringBuilder empQueryBuilder = new StringBuilder();
        
        if (requestDTO.getEmployeeFIO() != null && !requestDTO.getEmployeeFIO().isEmpty()) {
            var fullName = requestDTO.getEmployeeFIO().replaceAll("\\s+", " ");
            switch (fullName.split(" ").length) {
                case 1 -> empQueryBuilder.append(
                        " AND (lower(last_name) LIKE lower(:fullNameLikeExpression) OR lower(first_name) LIKE lower(:fullNameLikeExpression) " +
                        "OR lower(patronymic) LIKE lower(:fullNameLikeExpression))");
                case 2 -> empQueryBuilder.append(" AND (lower(concat(last_name, ' ', first_name)) LIKE lower(:fullNameLikeExpression) OR " +
                                                 "lower(concat(first_name, ' ', last_name)) LIKE lower(:fullNameLikeExpression) OR " +
                                                 "lower(concat(first_name, ' ', patronymic)) LIKE lower(:fullNameLikeExpression))");
                default -> empQueryBuilder.append(
                        " AND (lower(concat(last_name, ' ', first_name, ' ', patronymic)) LIKE lower(:fullNameLikeExpression)" +
                        " OR lower(concat(first_name, ' ', patronymic, ' ', last_name)) LIKE lower(:fullNameLikeExpression))");
            }
            var fullNameLikeExpression = "%" + fullName + "%";
            parameters.put("fullNameLikeExpression", fullNameLikeExpression);
        }
        
        if (requestDTO.getEmployeeDepartmentSet() != null && !requestDTO.getEmployeeDepartmentSet().isEmpty()) {
            Set<UUID> employeeDepartmentSet = departmentService.getDepartmentWithAllChildren((HashSet<UUID>) requestDTO.getEmployeeDepartmentSet());
            empQueryBuilder.append(" AND department_id in (:department_ids)");
            parameters.put("department_ids", employeeDepartmentSet);
        }
        
        // Табельный № заявителя
        if (requestDTO.getPersonnelNumber() != null) {
            empQueryBuilder.append(" AND personnel_number = :personnel_number");
            parameters.put("personnel_number", requestDTO.getPersonnelNumber());
        }
        
        // ОЕ Подразделения
        if (requestDTO.getDepartmentCode() != null) {
            empQueryBuilder.append(" AND department_id IN (SELECT id from reports.department WHERE code = :code)");
            parameters.put("code", requestDTO.getDepartmentCode());
        }
        
        if (!empQueryBuilder.isEmpty()) {
            qb.append(" AND request.passenger_id IN (SELECT id FROM reports.employee WHERE 1 = 1");
            qb.append(empQueryBuilder);
            qb.append(")");
        }
        
        // Причина отмены
        if (requestDTO.getRequestStatusCodes() != null && !requestDTO.getRequestStatusCodes().isEmpty()) {
            qb.append(" AND request.request_status_code IN :request_status_codes");
            parameters.put("request_status_codes", requestDTO.getRequestStatusCodes());
        }
        
        // Контрольный срок
        if (requestDTO.getDeadlineState() != null) {
            if (requestDTO.getDeadlineState()) {
                qb.append(" AND request.deadline_state = :deadline_state");
                parameters.put("deadline_state", DeadlineState.RED.name());
            } else {
                qb.append(" AND (request.deadline_state = :deadline_state OR request.deadline_state is null)");
                parameters.put("deadline_state", DeadlineState.NONE.name());
            }
        }
        
        // Экономия
        if (requestDTO.getSavings() != null) {
            if (requestDTO.getSavings()) {
                qb.append(" AND request.savings_cash > 0");
            } else {
                qb.append(" AND (request.savings_cash = 0 OR request.savings_cash is null)");
            }
        }
        
        // Номер договора
        if (requestDTO.getContractNumber() != null) {
            qb.append(" AND contract.contract_number = :contract_number");
            parameters.put("contract_number", requestDTO.getContractNumber());
        }
        
        // Дата закрытия обращения
        if (requestDTO.getRequestClosedDatetime() != null) {
            qb.append(" AND request.request_closed_datetime BETWEEN :request_closed_datetime_start AND :request_closed_datetime_end");
            parameters.put("request_closed_datetime_start", requestDTO.getRequestClosedDatetime().getStart());
            parameters.put("request_closed_datetime_end", requestDTO.getRequestClosedDatetime().getEnd());
        }
        
        // Добавление фильтров по поздразделению
        addDepartmentsFilter(requestDTO, parameters, qb);
        
        qbData.append(qb);
        qbCount.append(qb);
        
        // Сортировка
        prepareSortAndPagination(qbData, requestDTO.getSortSetting(), requestDTO.getPageSetting());
        
        String sqlString = qbData.toString();
        log.debug("prepareFindGroupTransferRequestsQuery(), sqlString = {}", sqlString);
        
        Query queryData = em.createNativeQuery(sqlString, Map.class);
        Query queryCount = em.createNativeQuery(qbCount.toString());
        
        parameters.forEach((paramName, paramValue) ->
                           {
                               queryData.setParameter(paramName, paramValue);
                               queryCount.setParameter(paramName, paramValue);
                           });
        if (log.isDebugEnabled()) {
            parameters.forEach((paramName, paramValue) -> log.debug("paramName = {}, paramValue = {}", paramName, paramValue));
        }
        
        return new DataAndCountQueries(queryData, queryCount);
    }
    
    private static void prepareSortAndPagination(
            StringBuilder qbData, RequestReportDTO.SortSetting sortSetting, RequestReportDTO.PageSetting pageSetting
                                                ) {
        // Сортировка
        if (sortSetting == null) {
            qbData.append(" ORDER BY creation_time DESC");
        } else {
            switch (sortSetting.getProperty()) {
                case REQUEST_ID -> qbData.append(" ORDER BY id");
                case REQUEST_HUMAN_ID -> qbData.append(" ORDER BY humanreadableid");
                case DESIRED_DATE -> qbData.append(" ORDER BY desired_date");
                case EXPECTED_COST -> qbData.append(" ORDER BY expected_cost");
                default -> qbData.append(" ORDER BY creation_time");
            }
            if (sortSetting.isDirectionAsc()) {
                qbData.append(" ASC");
            } else {
                qbData.append(" DESC");
            }
        }
        
        // Пагинация
        int offset = 0;
        int limit = 20;
        if (pageSetting != null) {
            offset = pageSetting.getPage() * pageSetting.getSize();
            limit = pageSetting.getSize();
        }
        qbData.append(" OFFSET ").append(offset).append(" LIMIT ").append(limit);
    }
    
    public static PublicResponseDTO mapSqlResultToResponse(
            PublicResponseSqlResultSetMapping rec,
            Map<UUID, EmployeeDTO> mapForEmployees,
            Map<UUID, DepartmentShortDTO> mapForDepartments,
            Map<UUID, PositionShortDTO> mapForPosition,
            Map<UUID, TripPurposeDTO> mapForPurpose,
            Map<UUID, TariffShortDTO> mapForTariffs,
            Map<UUID, TransportCompensation> transportCompensationMap,
            List<TransportCompensationDTO> mapForTransportCompensations,
            Map<UUID, List<WaypointDTO>> mapForWaypoints
                                                          ) {
        var passenger = mapForEmployees.get(rec.getPassengerId());
        var waypoints = mapForWaypoints.get(rec.getId());
        
        return PublicResponseDTO
                .builder()
                .id(rec.getId())
                .humanReadableId(rec.getHumanreadableid())
                .humanReadableLimitId("")
                .author(Optional.ofNullable(rec.getAuthorId()).map(mapForEmployees::get).orElse(null))
                .passenger(passenger)
                .itinerantType(
                        Optional.ofNullable(passenger).map(EmployeeDTO::getItinerantType).map(ItinerantType::valueOf).orElse(null))
                .costCenter(Optional.ofNullable(rec.getCostCenter()).orElse(null))
                .department(Optional.ofNullable(passenger)
                                    .map(EmployeeDTO::getDepartmentId)
                                    .map(mapForDepartments::get)
                                    .orElse(null))
                .position(Optional.ofNullable(passenger)
                                  .map(EmployeeDTO::getPositionId)
                                  .map(mapForPosition::get)
                                  .orElse(null))
                .creationTime(rec.getCreationTime())
                .desiredDate(rec.getDesiredDate())
                .approveDate(rec.getApprovalDate())
                .orderPaymentFormationStartDate(rec.getOrderPaymentFormationStartDate())
                .transportType(Optional.ofNullable(transportCompensationMap.get(rec.getId()))
                                       .map(compensation -> compensation.getTransportType().getRusName())
                                       .orElse(null))
                .status(Optional.ofNullable(rec.getRequestStatus()).map(TripRequestStatus::valueOf)
                                .orElse(null))
                .purpose(Optional.ofNullable(rec.getPurposeId()).map(mapForPurpose::get).orElse(null))
                .expected(ExpectedDataDTO.builder()
                                         .cost(Optional.ofNullable(rec.getExpectedCost()).orElse(0.0))
                                         .distance(Optional.ofNullable(rec.getExpectedDistance()).orElse(0.0))
                                         .time(Long.valueOf(rec.getExpectedTime() == null ? 0 : rec.getExpectedTime().toMinutes()).intValue())
                                         .waypoints(waypoints)
                                         .segments(new ArrayList<>())
                                         .build())
                .paymentQuarter(rec.getCreationTime() == null ? null :
                                Request.getPeriodOfPayment(rec.getOrderPaymentFormationStartDate()))
                .requestRating(RequestRatingDTO.builder().rating(rec.getRatingMark()).ratingComment(rec.getRatingComment()).build())
                .paymentTime(rec.getRequestStatus() != null && TripRequestStatus.valueOf(rec.getRequestStatus()).isTerminal()
                             ? withTimeZone(rec.getChangeDate(), rec.getTimeZone())
                             // может лучше тайм-зону прибавлять на фронте по аналогии с другими полями?
                             : null)
                .tariff(Optional.ofNullable(rec.getTariffId())
                                .map(mapForTariffs::get)
                                .orElse(null))
                .passengerDepartment1(rec.getPassengerDepartment1())
                .passengerDepartment2(rec.getPassengerDepartment2())
                .passengerDepartment3(rec.getPassengerDepartment3())
                .passengerDepartment4(rec.getPassengerDepartment4())
                .passengerDepartment5(rec.getPassengerDepartment5())
                .passengerDepartment6(rec.getPassengerDepartment6())
                .departureAddress(rec.getDepartureAddress())
                .intermediateAddresses(rec.getIntermediateAddresses())
                .destinationAddress(rec.getDestinationAddress())
                .transportCompensation(mapForTransportCompensations.stream().filter(e -> e.getRequestId().equals(rec.getId())).toList())
                .publicCompensationDocumentExist(rec.getPublicCompensationDocumentExist())
                .timeZone(rec.getTimeZone())
                .paymentTypeCodeMain(rec.getPaymentTypeCodeMain())
                .paymentPriceMain(rec.getPaymentPriceMain())
                .paymentTypeCodeOptional(rec.getPaymentTypeCodeOptional())
                .paymentPriceOptional(rec.getPaymentPriceOptional())
                .source(rec.getSource())
                .minTaxiTariffCost(Optional.ofNullable(rec.getMinTaxiTariffCost()).map(i -> i / 100.0).orElse(null))
                .commentForPurpose(rec.getCommentForPurpose())
                .deadlineViolation(rec.getDeadlineState() == null || DeadlineState.NONE.name().equals(rec.getDeadlineState()) ? "Нет" : "Да")
                .deadline(rec.getDeadline())
                .savings(rec.getSavingsCash() != null && rec.getSavingsCash() > 0)
                .requestClosedDatetime(rec.getRequestClosedDatetime())
                .executorGroupId(rec.getExecutorGroupId())
                .executorGroupName(rec.getExecutorGroupName())
                .build();
    }
    
    public static RequestReportDTO.IntegerRange getRangeFromPeriodOfPayment(Integer period) {
        if (period == null) {
            return null;
        }
        return switch (period) {
            case 1 -> new RequestReportDTO.IntegerRange(1, 7);
            case 2 -> new RequestReportDTO.IntegerRange(8, 15);
            case 3 -> new RequestReportDTO.IntegerRange(16, 23);
            case 4 -> new RequestReportDTO.IntegerRange(24, 31);
            default -> null;
        };
        
    }
    
    public static RequestStatusCodeListDTO getRequestStatusCodesByTransportType(
            ru.sberbank.ditsib.transport.reports.model.TransportTypeEnum transportType
                                                                               ) {
        switch (transportType) {
            case TAXI:
                var taxiData = Arrays.stream(TripRequestStatus.TaxiStatusCode.values())
                                     .map(code -> new RequestStatusCodeListDTO.RequestStatusCodeDTO(code.getCode(), code.getDescription()))
                                     .toList();
                return RequestStatusCodeListDTO.builder()
                                               .statusCodes(taxiData)
                                               .build();
            case TRANSFER:
                var transferData = Arrays.stream(TripRequestStatus.GroupTransferStatusCode.values())
                                         .map(code -> new RequestStatusCodeListDTO.RequestStatusCodeDTO(code.getCode(), code.getDescription()))
                                         .toList();
                return RequestStatusCodeListDTO.builder()
                                               .statusCodes(transferData)
                                               .build();
            case PUBLIC:
                var publicData = Arrays.stream(TripRequestStatus.PublicStatusCode.values())
                                       .map(code -> new RequestStatusCodeListDTO.RequestStatusCodeDTO(code.getCode(), code.getDescription()))
                                       .toList();
                return RequestStatusCodeListDTO.builder()
                                               .statusCodes(publicData)
                                               .build();
            case PERSONAL:
                var personalData = Arrays.stream(TripRequestStatus.PersonalStatusCode.values())
                                         .map(code -> new RequestStatusCodeListDTO.RequestStatusCodeDTO(code.getCode(), code.getDescription()))
                                         .toList();
                return RequestStatusCodeListDTO.builder()
                                               .statusCodes(personalData)
                                               .build();
            case CARGO:
                var cargoData = Arrays.stream(TripRequestStatus.CargoStatusCode.values())
                                      .map(code -> new RequestStatusCodeListDTO.RequestStatusCodeDTO(code.getCode(), code.getDescription()))
                                      .toList();
                return RequestStatusCodeListDTO.builder()
                                               .statusCodes(cargoData)
                                               .build();
            case REPAIR:
                var repairData = Arrays.stream(TripRequestStatus.RepairStatusCode.values())
                                       .map(code -> new RequestStatusCodeListDTO.RequestStatusCodeDTO(code.getCode(), code.getDescription()))
                                       .toList();
                return RequestStatusCodeListDTO.builder()
                                               .statusCodes(repairData)
                                               .build();
            case CARSHARING:
                var carsharingData = Arrays.stream(TripRequestStatus.CarsharingStatusCode.values())
                                           .map(code -> new RequestStatusCodeListDTO.RequestStatusCodeDTO(code.getCode(), code.getDescription()))
                                           .toList();
                return RequestStatusCodeListDTO.builder()
                                               .statusCodes(carsharingData)
                                               .build();
            default:
                throw new RuntimeException();
        }
    }
    
    public static class DataAndCountQueries {
        public Query queryData;
        public Query queryCount;
        
        public DataAndCountQueries(Query queryData, Query queryCount) {
            this.queryData = queryData;
            this.queryCount = queryCount;
        }
    }
}
