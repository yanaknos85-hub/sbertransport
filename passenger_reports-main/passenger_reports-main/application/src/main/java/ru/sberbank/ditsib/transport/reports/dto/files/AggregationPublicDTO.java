package ru.sberbank.ditsib.transport.reports.dto.files;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.spreadsheet.annotation.NullRender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class AggregationPublicDTO {

    /**
     * Код оплаты
     */
    @NullRender
    private Integer paymentType;

    /**
     * Ресурс
     */
    @NullRender
    private String resource;

    /**
     * Табельный номер
     */
    @NullRender
    private String personnelNumber;

    /**
     * Стоимость
     */
    @NullRender
    private Object money;

    /**
     * ФИО пользователя
     */
    @NullRender
    private String fio;

}
