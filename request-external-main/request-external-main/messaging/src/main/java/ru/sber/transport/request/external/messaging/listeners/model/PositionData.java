package ru.sber.transport.request.external.messaging.listeners.model;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.request.external.model.Position;
import ru.sberbank.ditsib.transport.messaging.messages.PositionMessage;

/**
 * Данные о должности
 */
@RequiredArgsConstructor
public class PositionData implements Position {

    @Delegate
    private final PositionMessage delegatee;

    @Override
    public List<String> getAvailableClasses() {
        return delegatee.getAvailableClasses().stream().toList();
    }

}
