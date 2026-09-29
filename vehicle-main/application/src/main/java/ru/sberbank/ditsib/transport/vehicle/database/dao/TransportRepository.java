package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import ru.sberbank.ditsib.transport.vehicle.database.model.Transport;
import ru.sberbank.ditsib.transport.vehicle.database.projection.ReportProjection;
import ru.sberbank.ditsib.transport.vehicle.database.projection.TransportShortProjection;
import ru.sberbank.ditsib.transport.vehicle.dto.files.TransportReportProjection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransportRepository extends JpaRepository<Transport, UUID>, JpaSpecificationExecutor<Transport> {

    @Override
    @EntityGraph("whole-transport")
    Optional<Transport> findById(UUID id);

    @Query(value = """
                   select vo.official_name as organizationName,
                          ovd.easup_id as easupId,
                          t1_0.state_number as stateNumber,
                          vb.title as brand,
                          vm.title as model,
                          t1_0.vin_code as vin,
                          t1_0.year as year,
                          t1_0.exploitation_start as exploitationStart,
                          t1_0.vehicle_type as type,
                          vs.title as subtype,
                          :year as reportYear,
                          :reportType as reportType,
                          sum(case when ov.month = 0 then ov.value end) as valueJanuary,
                          sum(case when ov.month = 1 then ov.value end) as valueFebruary,
                          sum(case when ov.month = 2 then ov.value end) as valueMarch,
                          sum(case when ov.month = 3 then ov.value end) as valueApril,
                          sum(case when ov.month = 4 then ov.value end) as valueMay,
                          sum(case when ov.month = 5 then ov.value end) as valueJune,
                          sum(case when ov.month = 6 then ov.value end) as valueJuly,
                          sum(case when ov.month = 7 then ov.value end) as valueAugust,
                          sum(case when ov.month = 8 then ov.value end) as valueSeptember,
                          sum(case when ov.month = 9 then ov.value end) as valueOctober,
                          sum(case when ov.month = 10 then ov.value end) as valueNovember,
                          sum(case when ov.month = 11 then ov.value end) as valueDecember
                   from vehicle.transport t1_0
                            left join vehicle.odometer_value ov on ov.transport_id = t1_0.id and ov.year = :year
                            left join vehicle.transport_organization vto on t1_0.id = vto.transport_id
                            left join vehicle.organization vo on vo.id = vto.organization_id AND vo.id = :organizationId
                            left join vehicle.transport_department vtd on t1_0.id = vtd.transport_id
                            left join vehicle.department ovd on vtd.department_id = ovd.id
                            join vehicle.vehicle vv on t1_0.vehicle_id = vv.id
                            join vehicle.model vm on vv.model_id = vm.id
                            join vehicle.brand vb on vm.brand_id = vb.id
                            join vehicle.subtype vs on t1_0.subtype_id = vs.id
                   where vto.organization_id = :organizationId
                   group by vo.official_name,
                            ovd.easup_id,
                            t1_0.state_number,
                            vb.title,
                            vm.title,
                            t1_0.vin_code,
                            t1_0.year,
                            t1_0.exploitation_start,
                            t1_0.vehicle_type,
                            vs.title
                   """,
            nativeQuery = true)
    List<ReportProjection> createOdometerReport(UUID organizationId, int year, String reportType);

    @Query(value = """
                   select vo.official_name as organizationName,
                          ovd.easup_id as easupId,
                          t1_0.state_number as stateNumber,
                          vb.title as brand,
                          vm.title as model,
                          t1_0.vin_code as vin,
                          t1_0.year as year,
                          t1_0.exploitation_start as exploitationStart,
                          t1_0.vehicle_type as type,
                          vs.title as subtype,
                          :year as reportYear,
                          :reportType as reportType,
                          sum(case when ov.month = 0 then ov.consumption end) as valueJanuary,
                          sum(case when ov.month = 1 then ov.consumption end) as valueFebruary,
                          sum(case when ov.month = 2 then ov.consumption end) as valueMarch,
                          sum(case when ov.month = 3 then ov.consumption end) as valueApril,
                          sum(case when ov.month = 4 then ov.consumption end) as valueMay,
                          sum(case when ov.month = 5 then ov.consumption end) as valueJune,
                          sum(case when ov.month = 6 then ov.consumption end) as valueJuly,
                          sum(case when ov.month = 7 then ov.consumption end) as valueAugust,
                          sum(case when ov.month = 8 then ov.consumption end) as valueSeptember,
                          sum(case when ov.month = 9 then ov.consumption end) as valueOctober,
                          sum(case when ov.month = 10 then ov.consumption end) as valueNovember,
                          sum(case when ov.month = 11 then ov.consumption end) as valueDecember
                   from vehicle.transport t1_0
                            left join vehicle.fuel_consumption ov on ov.transport_id = t1_0.id and ov.year = :year
                            left join vehicle.transport_organization vto on t1_0.id = vto.transport_id
                            left join vehicle.organization vo on vo.id = vto.organization_id AND vo.id = :organizationId
                            left join vehicle.transport_department vtd on t1_0.id = vtd.transport_id
                            left join vehicle.department ovd on vtd.department_id = ovd.id
                            join vehicle.vehicle vv on t1_0.vehicle_id = vv.id
                            join vehicle.model vm on vv.model_id = vm.id
                            join vehicle.brand vb on vm.brand_id = vb.id
                            join vehicle.subtype vs on t1_0.subtype_id = vs.id
                   where vto.organization_id = :organizationId
                   group by vo.official_name,
                            ovd.easup_id,
                            t1_0.state_number,
                            vb.title,
                            vm.title,
                            t1_0.vin_code,
                            t1_0.year,
                            t1_0.exploitation_start,
                            t1_0.vehicle_type,
                            vs.title
                   """,
            nativeQuery = true)
    List<ReportProjection> createFuelReport(UUID organizationId, int year, String reportType);

    @Query(value = """
                   select vo.official_name as organizationName,
                          ovd.easup_id as easupId,
                          t1_0.state_number as stateNumber,
                          vb.title as brand,
                          vm.title as model,
                          t1_0.vin_code as vin,
                          t1_0.year as year,
                          t1_0.exploitation_start as exploitationStart,
                          t1_0.vehicle_type as type,
                          vs.title as subtype,
                          :year as reportYear,
                          :reportType as reportType,
                          (select (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 0) -
                                  (select value from vehicle.odometer_value where transport_id = t1_0.id and year = (:year -1) and month = 11)) as valueJanuary,
                          (select (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 1) -
                                  (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 0)) as valueFebruary,
                          (select (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 2) -
                                  (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 1)) as valueMarch,
                          (select (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 3) -
                                  (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 2)) as valueApril,
                          (select (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 4) -
                                  (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 3)) as valueMay,
                          (select (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 5) -
                                  (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 4)) as valueJune,
                          (select (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 6) -
                                  (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 5)) as valueJuly,
                          (select (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 7) -
                                  (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 6)) as valueAugust,
                          (select (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 8) -
                                  (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 7)) as valueSeptember,
                          (select (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 9) -
                                  (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 8)) as valueOctober,
                          (select (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 10) -
                                  (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 9)) as valueNovember,
                          (select (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 11) -
                                  (select value from vehicle.odometer_value where transport_id = t1_0.id and year = :year and month = 10)) as valueDecember
                   from vehicle.transport t1_0
                            left join vehicle.transport_organization vto on t1_0.id = vto.transport_id
                            left join vehicle.organization vo on vto.organization_id = vo.id AND vo.id = :organizationId
                            left join vehicle.transport_department vtd on t1_0.id = vtd.transport_id
                            left join vehicle.department ovd on vtd.department_id = ovd.id
                            join vehicle.vehicle vv on t1_0.vehicle_id = vv.id
                            join vehicle.model vm on vv.model_id = vm.id
                            join vehicle.brand vb on vm.brand_id = vb.id
                            join vehicle.subtype vs on t1_0.subtype_id = vs.id
                   where vto.organization_id = :organizationId
                   """,
            nativeQuery = true)
    List<ReportProjection> createMileageReport(UUID organizationId, int year, String reportType);

    @Query(value = """
                   select t.id as id,
                   m.title as model,
                   b.title as brand,
                   t.stateNumber as stateNumber
                   from Transport t
                   left join Vehicle v on v.id = t.vehicle.id
                   left join Model m on m.id = v.model.id
                   left join Brand b on b.id = m.brand.id
                   inner join t.departments d
                     where t.status = TransportStatus.IN_USE
                       and t.stateNumber like concat('%', upper(:searchText), '%')
                       and d.id in (:departmentIds)
                       order by t.stateNumber
                   """,
            countQuery = """
                        select count(t)
                          from Transport t
                          inner join t.departments d
                          where t.status = TransportStatus.IN_USE
                            and t.stateNumber like concat('%', upper(:searchText), '%')
                            and d.id in (:departmentIds)
                         """)
    Page<TransportShortProjection> findTransportWithStructure(List<UUID> departmentIds, String searchText, Pageable pageable);

    Optional<Transport> findTransportByStateNumber(String stateNumber);

    @Query(value = """
            with fuel_types as (
                select
                    t.id as transport_id,
                    string_agg(distinct ft.title, ', ') as fuel_type_titles
                from vehicle.transport t
                    join vehicle.transport_organization vto on t.id = vto.transport_id
                    join vehicle.vehicle v on t.vehicle_id = v.id
                    join vehicle.vehicle_fuel_type vft on v.id = vft.vehicle_id
                    join vehicle.fuel_type ft on vft.fuel_type_id = ft.id
                where :organizationId is null or vto.organization_id = :organizationId
                group by t.id
            ), org_and_dept as (
                select
                    t.id as transport_id,
                    string_agg(distinct o.official_name, ', ') as official_name,
                    string_agg(distinct d.department_name, ', ') as department_name
                from vehicle.transport t
                    join vehicle.transport_organization vto on t.id = vto.transport_id
                    join vehicle.organization o on vto.organization_id = o.id
                    join vehicle.transport_department td on t.id = td.transport_id
                    join vehicle.department d on td.department_id = d.id
                where :organizationId is null or vto.organization_id = :organizationId
                group by t.id
            )
            select
                    t.inventory_number as inventoryNumber,
                    t.asset_number as assetNumber,
                    oad.official_name as officialName,
                    oad.department_name as departmentName,
                    t2.title as typeTitle,
                    s.title  as subtypeTitle,
                    t.state_number as stateNumber,
                    t.vin_code as vinCode,
                    t.chassis_number as chassisNumber,
                    t.body_number as bodyNumber,
                    t.certificate_number as certificateNumber,
                    t.certificate_issued_date as certificateIssuedDate,
                    t.passport_number as passportNumber,
                    t.passport_issued_date as passportIssuedDate,
                    t.brand_by_passport as brandByPassport,
                    t.model_by_passport as modelByPassport,
                    t.body_color as bodyColor,
                    t.balance_unit_number as balanceUnitNumber,
                    t.facility as facility,
                    t.equipment_unit_system_number as equipmentUnitSystemNumber,
                    t3.imei as telematicsIMEI,
                    t3.title as telematicsTitle,
                    t.exploitation_start as exploitationStart,
                    t.exploitation_end as exploitationEnd,
                    t.current_mileage as currentMileage,
                    t.status,
                    t."year",
                    t.vehicle_type as vehicleType,
                    t.location_address as locationAddress,
                    t.parking_address as parkingAddress,
                    t.comment as comment,
                    m.title as modelTitle,
                    b.title as brandTitle,
                    c.title as categoryTitle,
                    v.manufacturer as manufacturer,
                    v.ecological_class as ecologicalClass,
                    v.engine_power as enginePower,
                    et.title as engineTypeTitle,
                    v.engine_capacity as engineCapacity,
                    v.fuel_tank_volume as fuelTankVolume,
                    ft.fuel_type_titles as fuelTypeTitle,
                    d2.title as driveTitle,
                    v.mudguard_installed as mudguardInstalled,
                    v.spare_wheel_holder_installed as spareWheelHolderInstalled,
                    v.weight,
                    v.max_weight as maxWeight,
                    v.height,
                    v.width,
                    v.length,
                    v.service_interval_days as serviceIntervalDays,
                    v.service_interval_mileage as serviceIntervalMileage,
                    v.service_authorization_days as serviceAuthorizationDays,
                    v.service_authorization_mileage as serviceAuthorizationMileage,
                    bt.title as bodyTypeTitle,
                    tt.title as transmissionTypeTitle,
                    ws.title as frontWheelSizeTitle,
                    ws2.title as rearWheelSizeTitle,
                    v.rear_wheel_size_id ,
                    v.year_manufacture_begin as yearManufactureBegin,
                    v.year_manufacture_end as yearManufactureEnd,
                    v.city_consumption_rate as cityConsumptionRate,
                    v.country_consumption_rate as countryConsumptionRate,
                    v.hybrid_consumption_rate as hybridConsumptionRate,
                    ap.title as accessiblePositionTitle
            from vehicle.transport t
                    join fuel_types ft on t.id = ft.transport_id
                    join org_and_dept oad on t.id = oad.transport_id
                    join vehicle.subtype s on t.subtype_id = s.id
                    join vehicle."type" t2 on s.type_id = t2.id
                    left join vehicle.telematics t3 on t.telematics_id = t3.id
                    join vehicle.vehicle v on t.vehicle_id = v.id
                    join vehicle.model m on v.model_id = m.id
                    join vehicle.category c on v.category_id = c.id
                    join vehicle.engine_type et on v.engine_type_id = et.id
                    join vehicle.brand b on m.brand_id = b.id
                    join vehicle.drive d2 on v.drive_id = d2.id
                    join vehicle.body_type bt on v.body_type_id = bt.id
                    join vehicle.transmission_type tt on v.transmission_type_id = tt.id
                    join vehicle.wheel_size ws on v.front_wheel_size_id = ws.id
                    join vehicle.wheel_size ws2 on v.rear_wheel_size_id = ws2.id
                    left join accessible_position ap on t.accessible_position_id = ap.id
                   """,

            nativeQuery = true)
    List<TransportReportProjection> findAllByOrganizationId(UUID organizationId);

    @Query(value = """

            with fuel_types as (
                    select
                        t.id as transport_id,
                        string_agg(distinct ft.title, ', ') as fuel_type_titles
                    from vehicle.transport t
                        join vehicle.vehicle v on t.vehicle_id = v.id
                        join vehicle.vehicle_fuel_type vft on v.id = vft.vehicle_id
                        join vehicle.fuel_type ft on vft.fuel_type_id = ft.id
                    where :contractorId is null or t.contractor_id = :contractorId
                    and :autoparkId is null or t.autopark_id = :autoparkId
                    group by t.id
                )
                select
                        t.inventory_number as inventoryNumber,
                        t.asset_number as assetNumber,
                        'Н/Д' as officialName,
                        'Н/Д' as departmentName,
                        t2.title as typeTitle,
                        s.title  as subtypeTitle,
                        t.state_number as stateNumber,
                        t.vin_code as vinCode,
                        t.chassis_number as chassisNumber,
                        t.body_number as bodyNumber,
                        t.certificate_number as certificateNumber,
                        t.certificate_issued_date as certificateIssuedDate,
                        t.passport_number as passportNumber,
                        t.passport_issued_date as passportIssuedDate,
                        t.brand_by_passport as brandByPassport,
                        t.model_by_passport as modelByPassport,
                        t.body_color as bodyColor,
                        t.balance_unit_number as balanceUnitNumber,
                        t.facility as facility,
                        t.equipment_unit_system_number as equipmentUnitSystemNumber,
                        t3.imei as telematicsIMEI,
                        t3.title as telematicsTitle,
                        t.exploitation_start as exploitationStart,
                        t.exploitation_end as exploitationEnd,
                        t.current_mileage as currentMileage,
                        t.status,
                        t."year",
                        t.vehicle_type as vehicleType,
                        t.location_address as locationAddress,
                        t.parking_address as parkingAddress,
                        t.comment as comment,
                        m.title as modelTitle,
                        b.title as brandTitle,
                        c.title as categoryTitle,
                        v.manufacturer as manufacturer,
                        v.ecological_class as ecologicalClass,
                        v.engine_power as enginePower,
                        et.title as engineTypeTitle,
                        v.engine_capacity as engineCapacity,
                        v.fuel_tank_volume as fuelTankVolume,
                        ft.fuel_type_titles as fuelTypeTitle,
                        d2.title as driveTitle,
                        v.mudguard_installed as mudguardInstalled,
                        v.spare_wheel_holder_installed as spareWheelHolderInstalled,
                        v.weight,
                        v.max_weight as maxWeight,
                        v.height,
                        v.width,
                        v.length,
                        v.service_interval_days as serviceIntervalDays,
                        v.service_interval_mileage as serviceIntervalMileage,
                        v.service_authorization_days as serviceAuthorizationDays,
                        v.service_authorization_mileage as serviceAuthorizationMileage,
                        bt.title as bodyTypeTitle,
                        tt.title as transmissionTypeTitle,
                        ws.title as frontWheelSizeTitle,
                        ws2.title as rearWheelSizeTitle,
                        v.rear_wheel_size_id ,
                        v.year_manufacture_begin as yearManufactureBegin,
                        v.year_manufacture_end as yearManufactureEnd,
                        v.city_consumption_rate as cityConsumptionRate,
                        v.country_consumption_rate as countryConsumptionRate,
                        v.hybrid_consumption_rate as hybridConsumptionRate,
                        ap.title as accessiblePositionTitle
                from vehicle.transport t
                        join fuel_types ft on t.id = ft.transport_id
                        join vehicle.subtype s on t.subtype_id = s.id
                        join vehicle."type" t2 on s.type_id = t2.id
                        left join vehicle.telematics t3 on t.telematics_id = t3.id
                        join vehicle.vehicle v on t.vehicle_id = v.id
                        join vehicle.model m on v.model_id = m.id
                        join vehicle.category c on v.category_id = c.id
                        join vehicle.engine_type et on v.engine_type_id = et.id
                        join vehicle.brand b on m.brand_id = b.id
                        join vehicle.drive d2 on v.drive_id = d2.id
                        join vehicle.body_type bt on v.body_type_id = bt.id
                        join vehicle.transmission_type tt on v.transmission_type_id = tt.id
                        join vehicle.wheel_size ws on v.front_wheel_size_id = ws.id
                        join vehicle.wheel_size ws2 on v.rear_wheel_size_id = ws2.id
                        left join vehicle.accessible_position ap on t.accessible_position_id = ap.id
    """, nativeQuery = true)
    List<TransportReportProjection> findAllByContractorIdAndAutoparkId(UUID contractorId, UUID autoparkId);
}