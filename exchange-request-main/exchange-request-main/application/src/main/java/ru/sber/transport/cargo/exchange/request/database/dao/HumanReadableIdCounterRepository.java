package ru.sber.transport.cargo.exchange.request.database.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.sber.transport.cargo.exchange.request.database.model.HumanReadableIdCounter;
import ru.sber.transport.cargo.exchange.request.database.model.HumanReadableIdCounterId;

import java.util.Optional;

/**
 * Репозиторий для управления счётчиком human-readable ID.
 * Обеспечивает потокобезопасное увеличение номера в пределах месяца.
 */
@Repository
public interface HumanReadableIdCounterRepository extends JpaRepository<HumanReadableIdCounter, HumanReadableIdCounterId> {

    /**
     * Увеличивает значение счётчика для указанного месяца (в формате ГГГГММ).
     * Если запись отсутствует — создаёт новую с next_val = 1.
     * Использует ON CONFLICT для потокобезопасности в PostgreSQL.
     *
     * @param yearMonth месяц в формате "202504"
     */
    @Modifying
    @Query(value = """
        INSERT INTO exchange_request.humanreadable_id_counter (year_month, prefix, next_val)
        VALUES (:yearMonth, :prefix, 1)
        ON CONFLICT (year_month, prefix) DO UPDATE
        SET next_val = humanreadable_id_counter.next_val + 1
        """, nativeQuery = true)
    void incrementNextVal(String yearMonth, String prefix);


    /**
     * Получает текущее значение счётчика для месяца.
     *
     * @param yearMonth месяц в формате "202504"
     * @param prefix префикс ID
     * @return значение next_val или пустой Optional, если нет записи
     */
    @Query("SELECT h.nextVal FROM HumanReadableIdCounter h WHERE h.yearMonth = :yearMonth AND h.prefix = :prefix")
    Optional<Long> findNextValue(String yearMonth, String prefix);
}
