package ru.sber.transport.journal.service;

import org.springframework.data.domain.Page;
import ru.sber.transport.journal.dto.*;

import java.util.List;
import java.util.UUID;

public interface JournalService {
    
    /**
     * Получение данных заявки
     *
     * @param requestId Идентификатор записи о заявке
     * @param userId Идентификатор записи с таблицы corporate.user
     *
     * @return {@link AbstractGetRequestDto}
     */
    AbstractGetRequestDto get(UUID requestId, UUID userId);
    
    /**
     * Получение истории изменения статусов заявки
     *
     * @param requestId Идентификатор записи о заявке
     * @param userId Идентификатор записи с таблицы corporate.user
     *
     * @return {@link List<GetStatusDto>}
     */
    List<GetStatusDto> getStatusHistory(UUID requestId, UUID userId);
    
    /**
     * Отмена заявки
     *
     * @param requestId Идентификатор записи о заявке
     * @param userId Идентификатор записи с таблицы corporate.user
     */
    void cancel(UUID requestId, UUID userId);
    
    /**
     * Завершение заявки
     *
     * @param requestId Идентификатор записи о заявке
     * @param userId Идентификатор записи с таблицы corporate.user
     */
    void complete(UUID requestId, UUID userId);
    
    /**
     * Добавление возврата на доработку к заявке
     *
     * @param requestId Идентификатор записи о заявке
     * @param comment Комментарий
     * @param userId Идентификатор записи с таблицы corporate.user
     */
    void addRevision(UUID requestId, String comment, UUID userId);
    
    /**
     * Добавление оценки к заявке
     *
     * @param requestId Идентификатор записи о заявке
     * @param evaluationDto {@link EvaluationDto}
     * @param userId Идентификатор записи с таблицы corporate.user
     */
    void addEvaluation(UUID requestId, EvaluationDto evaluationDto, UUID userId);
    
    /**
     * Получение своих завершенных заявок c пагинацией
     *
     * @param journalDto {@link JournalDto}
     * @param userId Идентификатор записи с таблицы corporate.user
     *
     * @return {@link Page<GetRequestJournalDto>}
     */
    Page<GetRequestJournalDto> getCompletedBySelf(JournalDto journalDto, UUID userId);
    
    /**
     * Получение своих активных заявок c пагинацией
     *
     * @param journalDto {@link JournalDto}
     * @param userId Идентификатор записи с таблицы corporate.user
     *
     * @return {@link Page<GetRequestJournalDto>}
     */
    Page<GetRequestJournalDto> getActiveBySelf(JournalDto journalDto, UUID userId);
    
    /**
     * Получение завершенных заявок по своему подразделению c пагинацией
     *
     * @param journalDto {@link JournalDto}
     * @param userId Идентификатор записи с таблицы corporate.user
     *
     * @return {@link Page<GetRequestJournalDto>}
     */
    Page<GetRequestJournalDto> getCompletedByStructure(JournalDto journalDto, UUID userId);
    
    /**
     * Получение активных заявок по своему подразделению c пагинацией
     *
     * @param journalDto {@link JournalDto}
     * @param userId Идентификатор записи с таблицы corporate.user
     *
     * @return {@link Page<GetRequestJournalDto>}
     */
    Page<GetRequestJournalDto> getActiveByStructure(JournalDto journalDto, UUID userId);
}
