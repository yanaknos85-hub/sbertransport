package ru.sber.transport.request.external.web.model;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import ru.sber.transport.request.external.model.Assessment;

@RequiredArgsConstructor
public class WebRequestAssessment implements Assessment {

    @Delegate
    private final ru.sber.transport.web.model.Assessment delegatee;

    @Override
    public byte getRating() {
        return delegatee.getRating().byteValue();
    }

}
