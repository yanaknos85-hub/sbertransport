package ru.sberbank.ditsib.transport.reports.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.UUID;

@Getter
@Setter
@JsonPropertyOrder({ "id" })
@Schema(title = "Краткие данные тарифа", description = "Данные должности тарифа")
public class TariffShortDTO {
    @Schema(description = "Идентификатор")
    UUID id;

    @Schema(description = "Идентификатор (человекочитаемый)")
    String humanReadableId;

    @Schema(description = "Регион действия")
    String region;

    @Schema(description = "Вид транспорта")
    TransportTypeEnum transportType;

    @Schema(description = "Вид транспортной услуги")
    TransportServiceType serviceType = TransportServiceType.EMPLOYEE_TRANSPORTATION;

    @Schema(description = "Статус (false - удален)")
    boolean active = true;

    @Schema(description = "Организация владелец тарифа")
    UUID organizationId;

    @Schema(description = "Контракт")
    UUID contractId;
    
    @Schema(description = "Рабочая группа")
    String workGroup;
    
    public TariffShortDTO(UUID id, String humanReadableId, String workGroup) {
        this.id = id;
        this.humanReadableId = humanReadableId;
        this.workGroup = workGroup;
    }
    
    public TariffShortDTO() {
    
    }
}
