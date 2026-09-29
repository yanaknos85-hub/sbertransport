package ru.sber.transport.journal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.journal.dto.*;

import java.util.List;
import java.util.UUID;

/**
 * Набор операций для работы с журналом заявок
 */
@RequestMapping("/journal")
@Validated
@Tag(name = "Журнал заявок", description = "Набор операций для работы с журналом заявок")
public interface JournalController {
    
    /**
     * Получение данных заявки
     *
     * @param requestId Идентификатор записи о заявке
     * @param authentication {@link Authentication}
     *
     * @return {@link AbstractGetRequestDto}
     */
    @GetMapping(value = "{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Заявка", description = "Получение данных заявки")
    AbstractGetRequestDto get(
            @PathVariable @NotNull UUID requestId,
            @Parameter(hidden = true) Authentication authentication
                             );
    
    /**
     * Получение истории изменения статусов заявки
     *
     * @param requestId Идентификатор записи о заявке
     * @param authentication {@link Authentication}
     *
     * @return {@link List<GetStatusDto>}
     */
    @GetMapping(value = "{requestId}/status/history", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "История изменения статусов заявки", description = "Получение истории изменения статусов заявки")
    List<GetStatusDto> getStatusHistory(
            @PathVariable @NotNull UUID requestId,
            @Parameter(hidden = true) Authentication authentication
                                       );
    
    /**
     * Отмена заявки
     *
     * @param requestId Идентификатор записи о заявке
     * @param authentication {@link Authentication}
     */
    @PatchMapping(value = "{requestId}/cancel")
    @Operation(summary = "Отмена заявки", description = "Отмена заявки")
    void cancel(
            @PathVariable @NotNull UUID requestId,
            @Parameter(hidden = true) Authentication authentication
               );
    
    /**
     * Завершение заявки
     *
     * @param requestId Идентификатор записи о заявке
     * @param authentication {@link Authentication}
     */
    @PatchMapping(value = "{requestId}/complete")
    @Operation(summary = "Завершение заявки", description = "Завершение заявки")
    void complete(
            @PathVariable @NotNull UUID requestId,
            @Parameter(hidden = true) Authentication authentication
                 );
    
    /**
     * Добавление возврата на доработку к заявке
     *
     * @param requestId Идентификатор записи о заявке
     * @param comment Комментарий
     * @param authentication {@link Authentication}
     */
    @PatchMapping(value = "{requestId}/revision")
    @Operation(summary = "Добавление возврата на доработку", description = "Добавление возврата на доработку к заявке")
    void addRevision(
            @PathVariable
            @NotNull UUID requestId,
            @Parameter(description = "Текст коментария для обновления коментария")
            @RequestParam("comment")
            @Valid
            @Size(min = 10, max = 255) String comment,
            @Parameter(hidden = true) Authentication authentication
                    );
    
    /**
     * Добавление оценки к заявке
     * @param requestId Идентификатор записи о заявке
     * @param evaluationDto {@link EvaluationDto}
     * @param authentication {@link Authentication}
     */
    @PatchMapping(value = "{requestId}/evaluation", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Добавление оценки", description = "Добавление оценки к заявке")
    void addEvaluation(
            @PathVariable @NotNull UUID requestId,
            @Valid @RequestBody EvaluationDto evaluationDto,
            @Parameter(hidden = true) Authentication authentication
                      );
    
    /**
     * Получение своих завершенных заявок c пагинацией
     *
     * @param journalDto {@link JournalDto}
     * @param authentication {@link Authentication}
     *
     * @return {@link Page<GetRequestJournalDto>}
     */
    @PostMapping(value = "self/completed", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Свои завершенные заявки", description = "Получение своих завершенных заявок c пагинацией")
    Page<GetRequestJournalDto> getCompetedBySelf(
            @RequestBody @Valid JournalDto journalDto,
            @Parameter(hidden = true) Authentication authentication
                                                );
    
    /**
     * Получение своих активных заявок c пагинацией
     *
     * @param journalDto {@link JournalDto}
     * @param authentication {@link Authentication}
     *
     * @return {@link Page<GetRequestJournalDto>}
     */
    @PostMapping(value = "self/active", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Свои активные заявки", description = "Получение своих активных заявок c пагинацией")
    Page<GetRequestJournalDto> getActiveBySelf(
            @RequestBody @Valid JournalDto journalDto,
            @Parameter(hidden = true) Authentication authentication
                                              );
    
    /**
     * Получение завершенных заявок по своему подразделению c пагинацией
     *
     * @param journalDto {@link JournalDto}
     * @param authentication {@link Authentication}
     *
     * @return {@link Page<GetRequestJournalDto>}
     */
    @PostMapping(value = "structure/completed", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Завершенных заявки по своему подразделению",
               description = "Получение завершенных заявок по своему подразделению c пагинацией")
    Page<GetRequestJournalDto> getCompletedByStructure(
            @RequestBody @Valid JournalDto journalDto,
            @Parameter(hidden = true) Authentication authentication
                                                      );
    
    /**
     * Получение активных заявок по своему подразделению c пагинацией
     *
     * @param journalDto {@link JournalDto}
     * @param authentication {@link Authentication}
     *
     * @return {@link Page<GetRequestJournalDto>}
     */
    @PostMapping(value = "structure/active", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Активные заявки по своему подразделению",
               description = "Получение активных заявок по своему подразделению c пагинацией")
    Page<GetRequestJournalDto> getActiveByStructure(
            @RequestBody @Valid JournalDto journalDto,
            @Parameter(hidden = true) Authentication authentication
                                                   );
}