package ru.sber.transport.dispatcher.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**Дополнительные данные автомобиля*/
public interface VehicleAdditional {

    @Getter
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    enum Fields {

        SEMITRAILER_NUMBER("semitrailerNumber"),

        VOLUME("volume"),

        LENGTH("length"),

        WIDTH("width"),

        HEIGHT("height");

        private final String fieldName;
    }


}
