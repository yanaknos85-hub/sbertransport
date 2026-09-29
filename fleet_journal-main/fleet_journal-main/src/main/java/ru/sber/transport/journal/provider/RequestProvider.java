package ru.sber.transport.journal.provider;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import ru.sber.transport.journal.dto.AbstractGetRequestDto;
import ru.sber.transport.journal.dto.EvaluationDto;
import ru.sber.transport.journal.dto.GetRequestJournalDto;

import java.util.UUID;

/**
 * Поставщик данных по заявкам
 */
public interface RequestProvider {
    /**
     * Получение заявок по своему подразделению c пагинацией
     *
     * @param userId Идентификатор записи с таблицы corporate.user
     * @param isCompleted Требуется ли поиск по завершенным статусам заявок
     * @param pageable {@link PageRequest}
     *
     * @return {@link Page<GetRequestJournalDto>}
     */
    Page<GetRequestJournalDto> getByStructure(UUID userId, boolean isCompleted, PageRequest pageable);
    
    /**
     * Получение своих заявок c пагинацией
     *
     * @param userId Идентификатор записи с таблицы corporate.user
     * @param isCompleted Требуется ли поиск по завершенным статусам заявок
     * @param pageable {@link PageRequest}
     *
     * @return {@link Page<GetRequestJournalDto>}
     */
    Page<GetRequestJournalDto> getBySelf(UUID userId, boolean isCompleted, PageRequest pageable);
    
    /**
     * Получение данных заявки
     *
     * @param requestId Идентификатор записи о заявке
     *
     * @return {@link AbstractGetRequestDto}
     */
    AbstractGetRequestDto get(UUID requestId);
    
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
}
