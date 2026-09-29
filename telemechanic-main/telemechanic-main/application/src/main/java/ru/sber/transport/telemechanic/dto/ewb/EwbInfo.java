package ru.sber.transport.telemechanic.dto.ewb;

import ru.sber.transport.telemechanic.database.model.Ewb;
import ru.sber.transport.telemechanic.database.model.EwbTitle;

public record EwbInfo(
        Ewb ewb,
        EwbTitle title,
        String signature
) {
}
