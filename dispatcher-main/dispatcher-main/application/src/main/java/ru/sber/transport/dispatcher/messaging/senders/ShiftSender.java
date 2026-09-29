package ru.sber.transport.dispatcher.messaging.senders;

import ru.sber.transport.dispatcher.database.model.Shift;
import ru.sber.transport.dispatcher.dto.SignEwbRequestDto;
import ru.sber.transport.dispatcher.messages.EwbMessage;
import ru.sber.transport.dispatcher.messages.Source;

import java.util.List;

/**
 * Отправитель данных смен.
 */
public interface ShiftSender {

    /**
     * Отправить данные смены.
     *
     * @param shift смена.
     * @param source источник.
     */
    void send(Shift shift, Source source);

    /**
     * Отправить данные для EWB.
     *
     * @param signRequests список запросов на подпись.
     */
    void sendForEwb(List<SignEwbRequestDto> signRequests);

    /**
     * Отправить данные по сокету.
     *
     * @param shift смена.
     * @param errorMessage сообщение об ошибке
     * @param success признак успешности
     */
    void sendBySocket(Shift shift, String errorMessage, boolean success);

}
