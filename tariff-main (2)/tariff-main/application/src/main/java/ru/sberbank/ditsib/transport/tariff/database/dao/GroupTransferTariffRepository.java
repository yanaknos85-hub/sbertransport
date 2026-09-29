package ru.sberbank.ditsib.transport.tariff.database.dao;

import org.springframework.data.jpa.repository.Query;
import ru.sberbank.ditsib.transport.tariff.database.model.GroupTransferTariff;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GroupTransferTariffRepository extends TariffRepository<GroupTransferTariff> {
    
    @Query(nativeQuery = true, value = "select t.* from tariff.tariff t " +
                                       "inner join tariff.contract c on t.contract_id  = c.id " +
                                       "where t.taxi_class = 'TRANSFER_CAR_CHOICE' " +
                                       "and c.contractor_id = :contractorId " +
                                       "and t.organization_id = :organizationId " +
                                       "and least(cast(:endDate as date), cast(t.tariff_end_date as date)) - " +
                                       "greatest(cast(:startDate as date), cast(t.tariff_start_date as date)) >= 0 " +
                                       "and t.region_ids && cast(:regionIds as uuid[]) " +
                                       "and t.transport_ids && cast(:transportIds as uuid[]) " +
                                       "and t.active = true")
    List<GroupTransferTariff> findConflictTariff(
            LocalDate startDate,
            LocalDate endDate,
            UUID organizationId,
            UUID contractorId,
            String regionIds,
            String transportIds
                                                );
    
    @Query(nativeQuery = true, value = "select t.* from tariff.tariff t " +
                                       "where t.taxi_class = 'TRANSFER_CAR_CHOICE' " +
                                       "and t.organization_id = cast(:organizationId as uuid) " +
                                       "and cast(t.tariff_end_date as date) >= :tripDate " +
                                       "and :tripDate >= cast(t.tariff_start_date as date) " +
                                       "and t.region_ids && cast(:regionIds as uuid[]) " +
                                       "and t.active = true")
    List<GroupTransferTariff> findAllTariff(
            LocalDate tripDate,
            UUID organizationId,
            String regionIds
                                           );
    
    @Query(nativeQuery = true, value = "select t.* from tariff.tariff t " +
                                       "where t.taxi_class = 'TRANSFER_CAR_CHOICE' " +
                                       "and t.organization_id = cast(:organizationId as uuid) " +
                                       "and cast(t.tariff_end_date as date) >= :tripDate " +
                                       "and :tripDate >= cast(t.tariff_start_date as date) " +
                                       "and t.region_ids && cast(:regionIds as uuid[]) " +
                                       "and t.transport_ids && cast(:transportIds as uuid[]) " +
                                       "and t.active = true")
    List<GroupTransferTariff> findAllTariff(
            LocalDate tripDate,
            UUID organizationId,
            String regionIds,
            String transportIds
                                           );
    
}
