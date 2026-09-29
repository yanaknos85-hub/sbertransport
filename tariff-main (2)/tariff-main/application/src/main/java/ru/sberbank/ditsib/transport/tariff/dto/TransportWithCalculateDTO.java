package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import ru.sber.transport.tariff.model.CalculatedDto;
import ru.sberbank.ditsib.transport.tariff.dto.contractor.transport.TransportDTO;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@Schema(title = "Содержит информацию о транспорте и о тарифе", description = "Содержит информацию о транспорте и о тарифе (Если есть конфликт и " +
                                                                             "данный тип транспорта нельзя выбрать для создания тарифа)")
public class TransportWithCalculateDTO extends TransportDTO {
    
    private CalculatedDto calculated;
    
    private Boolean availableOnly;
}
