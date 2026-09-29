package ru.sberbank.ditsib.transport.request.messaging.senders;

import ru.sberbank.ditsib.transport.request.messaging.message.CarLocationMessage;

public interface CarLocationSender {

    void send(CarLocationMessage carLocationMessage);
}
