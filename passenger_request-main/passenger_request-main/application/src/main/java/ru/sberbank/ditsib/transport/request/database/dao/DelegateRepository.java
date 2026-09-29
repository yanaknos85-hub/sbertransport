package ru.sberbank.ditsib.transport.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.sberbank.ditsib.transport.request.database.model.corp.Delegate;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Репозиторий для работы с делегатами.
 */
public interface DelegateRepository extends JpaRepository<Delegate, UUID> {
    
    /**
     * Поиск делегата по идентификатору делегата.
     *
     * @param id идентификатор делегирующего.
     *
     * @return делегат.
     */
    @Query("SELECT delegate FROM Delegate delegate WHERE delegate.delegateId = :id " +
           "AND delegate.startDate <= :date AND :date <= delegate.endDate")
    List<Delegate> findByDelegateIdAndDateBetweenStartAndEnd(UUID id, LocalDate date);
    
    /**
     * Получить всех делегатов по идентификатору делегирующего.
     *
     * @param id идентификатор делегирующего.
     * @param date дата для выборки.
     *
     * @return список делегатов.
     */
    @Query("SELECT delegate FROM Delegate delegate WHERE delegate.supervisorId = :id " +
           "AND delegate.startDate <= :date AND :date <= delegate.endDate")
    List<Delegate> findAllBySupervisorIdAndDateBetweenStartAndEnd(UUID id, LocalDate date);
}
