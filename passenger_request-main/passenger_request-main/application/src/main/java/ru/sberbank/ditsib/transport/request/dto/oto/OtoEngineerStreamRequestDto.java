package ru.sberbank.ditsib.transport.request.dto.oto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import ru.sberbank.ditsib.transport.constants.DeadlineState;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.dto.RequestSearchDTO;

import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "Фильтр для получения потока заявок", description = "Фильтр для получения потока заявок")
public class OtoEngineerStreamRequestDto extends RequestSearchDTO {

    /**
     * Вид транспорта
     */
    @Schema(description = "Вид транспорта")
    private TransportTypeEnum transportType;

    /**
     * Идентификаторы перевозчиков
     */
    @Schema(description = "Идентификаторы перевозчиков")
    private List<UUID> contractorIds;

    /**
     * Мобильный телефон пассажира
     */
    @Pattern(regexp = "(\\+?\\d-\\d{3}-\\d{3}-\\d{2}-\\d{2})|(\\+?\\d{11})", message = "Мобильный телефон должен быть вида +1-123-123-12-12 или +12345678910")
    @Schema(description = "Мобильный телефон пассажира. Маска мобильного телефона вида +1-123-123-12-12 или +12345678910")
    private String passengerPhoneNumber;


    /**
     * Класс автомобиля
     */
    @Schema(description = "Класс автомобиля")
    private TaxiClass taxiClass;

    /**
     * Дата и время старта поездки
     */
    @Schema(description = "Дата и время старта поездки")
    @Valid
    private RequestSearchDTO.DateRange startTripDate;

    /**
     * Дата и время окончания поездки
     */
    @Schema(description = "Дата и время окончания поездки")
    @Valid
    private RequestSearchDTO.DateRange endTripDate;

    /**
     * Состояние контрольного срока
     */
    @Schema(description = "Состояние контрольного срока")
    private DeadlineState deadlineState;

    /**
     * Контрольный срок, мин
     */
    @Schema(description = "Контрольный срок, мин")
    private Long deadline;
}
