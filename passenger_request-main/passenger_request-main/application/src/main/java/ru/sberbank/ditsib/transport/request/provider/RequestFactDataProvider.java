package ru.sberbank.ditsib.transport.request.provider;

import ru.sberbank.ditsib.transport.request.messaging.message.RequestFactDataMessage;

public interface RequestFactDataProvider {

    void enrichRequest(RequestFactDataMessage message);
}
