package ru.sberbank.ditsib.transport.request.database.dao;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.PublicCompensationType;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.TransportCompensation;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Repository of transportCompensation
 */
@Repository
public interface TransportCompensationRepository extends JpaRepository<TransportCompensation, UUID> {
    /**
     * Получить связанную с заявкой совместную поездку
     *
     * @param employeeId Id сотрудника
     * @param compensationType Тип компенсации
     *
     * @return совместная поездка
     */
    @Query("SELECT transportCompensation FROM TransportCompensation transportCompensation " +
           "INNER JOIN FETCH transportCompensation.request request " +
           "INNER JOIN FETCH request.passenger pssngr " +
           "WHERE pssngr.id = :employeeId AND transportCompensation.compensationType = :compensationType AND NOT request.status = 'PUBLIC_CANCELLED'")
    List<TransportCompensation> findByEmployeeAndCompensationType(UUID employeeId, PublicCompensationType compensationType);

    /**
     * Проверка существования активных проездных для конкретной даты
     *
     * @param requestId   исключающий идентификатор заявки
     * @param passengerId идентификатор пассажира
     * @param desiredDate дата
     * @param cardTypes   тип ОТ, для которого ищется проездной
     * @return true в случае наличия активных проездных, false в обратном
     */
    @Query(""" 
                SELECT COUNT(tc) > 0
                FROM TransportCompensation tc
                JOIN tc.request r
                JOIN r.passenger p
                WHERE p.id = :passengerId
                  AND r.id != :requestId
                  AND r.active = true
                  AND tc.transportType IN :cardTypes
                  AND :desiredDate BETWEEN tc.ticketsExpirationStart AND tc.ticketsExpirationEnd
            """)
    boolean existsActiveTravelCard(@NonNull UUID requestId,
                                   @NonNull UUID passengerId,
                                   @NonNull LocalDate desiredDate,
                                   @NonNull List<String> cardTypes);

    /**
     * Проверка существования активных проездных для промежутка времени
     *
     * @param requestId   исключающий идентификатор заявки
     * @param passengerId идентификатор пассажира
     * @param startDate   начало периода
     * @param endDate     конец периода
     * @return true в случае наличия активных проездных, false в обратном
     */
    @Query("""
                SELECT COUNT(tc) > 0
                FROM TransportCompensation tc
                JOIN tc.request r
                JOIN r.passenger p
                WHERE p.id = :passengerId
                  AND r.id != :requestId
                  AND r.active = true
                  AND tc.compensationType = 'TRAVEL_CARD_COMPENSATION'
                  AND :startDate <= tc.ticketsExpirationEnd
                  AND :endDate >= tc.ticketsExpirationStart
            """)
    boolean existsOverlappingTravelCard(@NonNull UUID requestId,
                                        @NonNull UUID passengerId,
                                        @NonNull LocalDate startDate,
                                        @NonNull LocalDate endDate);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("DELETE from TransportCompensation")
    void deleteAll();
    
}
