package ru.sberbank.ditsib.transport.request.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * Информация о пассажире
 */
@ToString
@Getter
@Setter
@EqualsAndHashCode
@JsonPropertyOrder({ "id" })
@Builder(toBuilder = true)
@RequiredArgsConstructor
@Schema(title = "Информация о пассажире", description = "Информация о пассажире")
public class PassengerDTO {
    /**
     * Порядковый номер пассажира
     */
    private final String id;
    
    /**
     * ФИО
     */
    private final String name;
    
    /**
     * Телефон
     */
    private final String phone;
    
    /**
     * Комментарий для водителя
     */
    private final String comment;
}