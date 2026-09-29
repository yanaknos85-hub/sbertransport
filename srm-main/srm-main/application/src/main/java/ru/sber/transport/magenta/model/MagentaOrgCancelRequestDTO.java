package ru.sber.transport.magenta.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO с данными по отмене заявки в совместной поездке
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Данные по тарифу для Magenta", description = "Данные по тарифу")
public class MagentaOrgCancelRequestDTO {
    
    // ID тарифа
    private String extId;
}