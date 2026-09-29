package ru.sberbank.ditsib.transport.reports.dto.files;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.spreadsheet.annotation.NullRender;

@Getter
@Setter
@NoArgsConstructor
public class PublicPaymentDTO {
    
    /**
     * ID заявки
     */
    @NullRender
    private String humanReadableId;
    
    /**
     * Вид оплаты
     */
    @NullRender
    private int paymentCode;
    
    /**
     * Ресурс
     */
    @NullRender
    private String resource;
    
    /**
     * Стоимость
     */
    @NullRender
    private Double money;
    
    /**
     * Табельный номер
     */
    @NullRender
    private String personnelNumber;
    
    /**
     * ФИО пользователя
     */
    @NullRender
    private String fio;
    
    /**
     * Период формирования приказа на выплату
     */
    @NullRender
    private String periodFormationForPayment;
    
}
