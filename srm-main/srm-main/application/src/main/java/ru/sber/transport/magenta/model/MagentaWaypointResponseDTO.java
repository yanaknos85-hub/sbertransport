package ru.sber.transport.magenta.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.sberbank.ditsib.converters.MagentaTimeDeserializer;
import ru.sberbank.ditsib.converters.MagentaZonedDateTimeToUTCSerializer;
import ru.sberbank.utils.reflection.DoubleUtil;

import java.time.ZonedDateTime;

/**
 * DTO Адреса маршрута, результат публикации
 */
@ToString
@Getter
@Setter
@NoArgsConstructor
@Schema(title = "Адрес", description = "Один из адресов маршрута")
public class MagentaWaypointResponseDTO {

    /**
     * Константа типа точка "Посадка".
     */
    public static final String EVENT_BOARD = "Посадка";

    /**
     * Константа типа точка "Высадка".
     */
    public static final String EVENT_UNBOARD = "Высадка";

    /**
     * Константа типа точка "Ожидание".
     */
    public static final String EVENT_WAIT = "Ожидание";

    /**
     * Константа активности точки.
     */
    public static final String ACTIVE_STATE = "Active";
    
    /**
     * ID заказа, в котором были заданы остановки поездки
     */
    @NotNull
    private String orderId;
    
    /**
     * Состояние заказа в поездке, активен/неактивен
     */
    private String state = ACTIVE_STATE;
    
    /**
     * Улица номер дома
     */
    @NotBlank
    private String address;
    
    /**
     * Широта
     */
    @NotNull
    private Double latitude;
    
    /**
     * Долгота
     */
    @NotNull
    private Double longitude;
    
    /**
     * рассчитанное время остановки для начала поездки
     */
    @JsonSerialize(using = MagentaZonedDateTimeToUTCSerializer.class)
    @JsonDeserialize(using = MagentaTimeDeserializer.class)
    @NotNull
    private ZonedDateTime startTime;
    
    /**
     * рассчитанное время остановки для окончания поездки
     */
    @JsonSerialize(using = MagentaZonedDateTimeToUTCSerializer.class)
    @JsonDeserialize(using = MagentaTimeDeserializer.class)
    @NotNull
    private ZonedDateTime endTime;
    
    /**
     * тип остановки
     */
    @NotBlank
    private String eventType;

    /**
     * Определение эквивалентности по координатам.
     *
     * @param toCompare объект для сравнения.
     * @return <code>true</code> если объекты эквивалентны.
     */
    public boolean equalsByCoords(MagentaWaypointResponseDTO toCompare) {
        if (toCompare == null) {
            return false;
        }
        if (toCompare.getLatitude() == 0 || toCompare.getLongitude() == 0) {
            return false;
        }
        return DoubleUtil.doubleEqualsWithPrecision(getLatitude(),
                                                    toCompare.getLatitude(),
                                                    DoubleUtil.EPSILON)
               && DoubleUtil.doubleEqualsWithPrecision(getLongitude(), toCompare.getLongitude(),
                                                       DoubleUtil.EPSILON);
    }
}