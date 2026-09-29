package ru.sber.transport.contractor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VehicleNormDto {

    /**
     * Значение норматива внутреннего автопарка в dispatcher.contractors той организации, которая была передана в параметре запроса
     */
    private Integer contractorCountNorm;

    /**
     * Доступное для ввода количество автомобилей в филиале.
     * Расчет: contractorCarNorm - totalCount (в рамках одного внутреннего автопарка)
     */
    private Integer availableCount;

    /**
     * Общее количество автомобилей по филиалам автопарка (сумма по vehicleCountNorm всех autoparkId по contractorId организации, переданной в параметрах)
     */
    private Integer totalCount;
}
