package ru.sberbank.ditsib.transport.vehicle.database.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.sberbank.ditsib.transport.vehicle.database.model.Vehicle;
import ru.sberbank.ditsib.transport.vehicle.database.model.VehicleSearchFiltersProjection;
import ru.sberbank.ditsib.transport.vehicle.database.projection.VehicleShortProjection;
import ru.sberbank.ditsib.transport.vehicle.dto.vehicle.VehicleSearchDto;

import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID>, JpaSpecificationExecutor<Vehicle> {

    @Query(nativeQuery = true,
            value = """
            select count(*)>0 from vehicle.vehicle v
            where v.model_id = :#{#vehicle.model.id} and
                  v.category_id = :#{#vehicle.category.id} and
                  v.manufacturer = :#{#vehicle.manufacturer} and
                  v.ecological_class = :#{#vehicle.ecologicalClass} and
                  v.engine_power = :#{#vehicle.enginePower} and
                  v.engine_capacity = :#{#vehicle.engineCapacity} and
                  v.fuel_tank_volume = :#{#vehicle.fuelTankVolume} and
                  v.drive_id = :#{#vehicle.drive.id} and
                  v.mudguard_installed = :#{#vehicle.mudguardInstalled} and
                  v.spare_wheel_holder_installed = :#{#vehicle.spareWheelHolderInstalled} and
                  v.weight = :#{#vehicle.weight} and
                  v.max_weight = :#{#vehicle.maxWeight} and
                  v.height = :#{#vehicle.height} and
                  v.width = :#{#vehicle.width} and
                  v.length = :#{#vehicle.length} and
                  v.service_authorization_days = :#{#vehicle.serviceAuthorizationDays} and
                  v.service_interval_days = :#{#vehicle.serviceIntervalDays} and
                  v.service_interval_mileage = :#{#vehicle.serviceIntervalMileage} and
                  v.body_type_id = :#{#vehicle.bodyType.id} and
                  v.transmission_type_id = :#{#vehicle.transmissionType.id} and
                  v.front_wheel_size_id = :#{#vehicle.frontWheelSize.id} and
                  v.rear_wheel_size_id = :#{#vehicle.rearWheelSize.id} and 
                  v.year_manufacture_begin = :#{#vehicle.yearManufactureBegin} and
                  v.engine_type_id = :#{#vehicle.engineType.id} and
                  ((cast(:#{#vehicle.cityConsumptionRate} as decimal) is null and v.city_consumption_rate is null) or v.city_consumption_rate = :#{#vehicle.cityConsumptionRate}) and
                  ((cast(:#{#vehicle.countryConsumptionRate} as decimal) is null and v.country_consumption_rate is null) or v.country_consumption_rate = :#{#vehicle.countryConsumptionRate}) and
                  ((cast(:#{#vehicle.hybridConsumptionRate} as decimal) is null and v.hybrid_consumption_rate is null) or v.hybrid_consumption_rate = :#{#vehicle.hybridConsumptionRate}) and
                  coalesce(v.year_manufacture_end, 0) = coalesce(:#{#vehicle.yearManufactureEnd}, 0) 
            """)
    boolean existsByAllFields(@Param("vehicle") Vehicle vehicle);


    @Override
    @EntityGraph("vehicle-search")
    Page<Vehicle> findAll(Specification<Vehicle> spec, Pageable pageable);

    @Query(value = """
                   select v.id,
                          b.title as brand,
                          m.title as model,
                          et.title as engineType,
                          v.engine_capacity as engineCapacity,
                          v.engine_power as enginePower,
                          v.fuel_tank_volume as fuelTankVolume,
                          v.spare_wheel_holder_installed as spareWheelHolderInstalled,
                          v.mudguard_installed as mudguardInstalled,
                          d.title as drive,
                          bt.title as bodyType,
                          tt.title as transmissionType,
                          coalesce(ft_list.fuel_types, '') as fuelType,
                          v.weight as weight,
                          v.height as height,
                          v.width as width,
                          v.length as length,
                          v.year_manufacture_begin as yearManufactureBegin,
                          v.year_manufacture_end as yearManufactureEnd
                   from vehicle.vehicle v
                            join vehicle.model m on m.id = v.model_id
                            join vehicle.brand b on b.id = m.brand_id
                            join vehicle.engine_type et on et.id = v.engine_type_id
                            join vehicle.drive d on d.id = v.drive_id
                            join vehicle.transmission_type tt on tt.id = v.transmission_type_id
                            join vehicle.body_type bt on bt.id = v.body_type_id
                            left join (
                                select vft.vehicle_id,
                                string_agg(ft.title, ', ') as fuel_types
                                from vehicle_fuel_type vft
                                    join vehicle.fuel_type ft on ft.id = vft.fuel_type_id
                                group by vft.vehicle_id) ft_list on ft_list.vehicle_id = v.id
                   order by b.title, m.title
                   """,
            countQuery = "select count(*) from vehicle.vehicle",
            nativeQuery = true)
    Page<VehicleShortProjection> getAllProjections(Pageable pageable);

    @Query(nativeQuery = true,
            value = """
                    select jsonb_agg(distinct bt)                                                           as bodyType,
                           jsonb_agg(distinct cast(v.year_manufacture_begin as text) || ' - ' ||
                                              coalesce(cast(v.year_manufacture_end as text), 'н.в.'))       as manufacturePeriod,
                           json_agg(distinct et)                                                            as engineType,
                           json_agg(distinct v.engine_capacity)                                             as engineCapacity,
                           json_agg(distinct v.engine_power)                                                as enginePower,
                           json_agg(distinct tt)                                                            as transmissionType,
                           json_agg(distinct d)                                                             as driveType
                    from vehicle.vehicle v
                             join vehicle.drive d on d.id = v.drive_id
                             join vehicle.vehicle_fuel_type vft on vft.vehicle_id = v.id
                             join vehicle.fuel_type ft on ft.id = vft.fuel_type_id
                             join vehicle.engine_type et on et.id = ft.engine_type_id
                             join vehicle.transmission_type tt on tt.id = v.transmission_type_id
                             join vehicle.body_type bt on bt.id = v.body_type_id
                             join vehicle.model m on m.id = v.model_id
                    where (cast(:#{#searchDto.getVehicle().brand()} as text) is null or m.brand_id = :#{#searchDto.getVehicle().brand()}) and
                          (cast(:#{#searchDto.getVehicle().model()} as text) is null or v.model_id = :#{#searchDto.getVehicle().model()}) and
                          (cast(:#{#searchDto.getVehicle().bodyType()} as text) is null or v.body_type_id = :#{#searchDto.getVehicle().bodyType()}) and
                          (cast(:#{#searchDto.getVehicle().manufactureYear()} as text) is null or
                            (v.year_manufacture_begin <= :#{#searchDto.getVehicle().manufactureYear()} and
                            (v.year_manufacture_end >= :#{#searchDto.getVehicle().manufactureYear()} or v.year_manufacture_end is null))) and
                          (cast(:#{#searchDto.getVehicle().manufacturePeriod()} as text) is null or
                            cast(v.year_manufacture_begin as text) || ' - ' || coalesce(cast(v.year_manufacture_end as text), 'н.в.') =
                            :#{#searchDto.getVehicle().manufacturePeriod()}) and
                          (cast(:#{#searchDto.getEngine().engineType()} as text) is null or ft.engine_type_id = :#{#searchDto.getEngine().engineType()}) and
                          (cast(:#{#searchDto.getEngine().drive()} as text) is null or v.drive_id = :#{#searchDto.getEngine().drive()}) and
                          (cast(:#{#searchDto.getEngine().transmissionType()} as text) is null or v.transmission_type_id = :#{#searchDto.getEngine().transmissionType()}) and
                          (cast(:#{#searchDto.getEngine().engineCapacity()} as text) is null or v.engine_capacity = :#{#searchDto.getEngine().engineCapacity()}) and
                          (cast(:#{#searchDto.getEngine().enginePower()} as text) is null or v.engine_power = :#{#searchDto.getEngine().enginePower()})
                    """)
    VehicleSearchFiltersProjection getFilters(VehicleSearchDto searchDto);
}
