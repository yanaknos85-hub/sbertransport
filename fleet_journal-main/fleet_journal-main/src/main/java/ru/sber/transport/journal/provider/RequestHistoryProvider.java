package ru.sber.transport.journal.provider;

import ru.sber.transport.journal.dto.GetStatusDto;

import java.util.LinkedList;
import java.util.UUID;

/**
 * Поставщик данных по истории заявок
 */
public interface RequestHistoryProvider {
    LinkedList<GetStatusDto> get(UUID requestId);
}
