package ru.sber.transport.request.external.messaging.listeners.model.avro;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.messages.corporate.avro.PositionMessage;
import ru.sber.transport.request.external.model.Position;

/**
 * Данные о должности
 */
@RequiredArgsConstructor
public class PositionAvroData implements Position {

    @Delegate
    private final PositionMessage delegatee;

}
