package ru.sber.transport.trip.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateTripResponse(

        @Schema(title = "Успеношность", description = "Успеношность сохранения поездки")
        boolean isSuccess,

        @Schema(title = "Идентификатор", description = "Идентификатор поездки")
        String orderParthnerId,

        @Schema(title = "Идентификатор", description = "Идентификатор маршрута, по которому была создана поездка")
        String orderSbertransportId,

        @Schema(title = "Ошибка", description = "Описание ошибки")
        Error error
) {
    /**
     * Объект ошибки.
     *
     * @param status  Статус ошибки.
     * @param message Сообщение об ошибке.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Error(

            @Schema(title = "Статус", description = "Статус ошибки")
            int status,

            @Schema(title = "Сообщение", description = "Сообщение об ошибке")
            String message
    ) {
    }
}
