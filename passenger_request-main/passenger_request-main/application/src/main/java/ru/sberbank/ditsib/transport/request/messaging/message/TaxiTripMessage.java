package ru.sberbank.ditsib.transport.request.messaging.message;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.*;
import ru.sber.transport.messaging.Message;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @deprecated use request-messaging
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Deprecated(since = "2023-06-19")
public class TaxiTripMessage implements Message<UUID> {
    
    /**
     * Тип поездки: совместная или индивидуальная
     */
    private String tripType;
    
    /**
     * Идентификатор поездки
     */
    private UUID id;
    
    /**
     * ID в системе Исполнителя
     */
    private String taxiId;
    
    /**
     * Идентификатор организации
     */
    private UUID organizationId;
    
    /**
     *  Произвольное описание работ . Например: [#ЗАКАЗА], [АВТО МАРКА], [АВТО ЦВЕТ], [АВТО РЕГ НОМЕР], [ФИО ВОДИТЕЛЯ],
     *      * [КОНТАКТНЫЙ ТЕЛЕФОН]
     */
    private String resolution;
    
    /**
     * Идентификатор тарифа
     */
    private UUID tariffId;
    
    /**
     * Время регистрации в системе Исполнителя
     */
    private LocalDateTime dateTimeRegistered;
    
    /**
     * Дата внесения фактических параметров поездки
     */
    private LocalDateTime factParametersSettingTime;
    
    /**
     * Время начала работ
     */
    private LocalDateTime tripStartTime;
    
    /**
     * Время завершения поездки
     */
    private LocalDateTime tripFinishTime;
    
    /**
     * статус из системы исполнителя
     */
    private String status;
    
    /**
     * Километраж
     */
    private Double tripFactDistance;
    
    /**
     * Длительность поездки
     */
    private Duration tripFactDuration;
    
    /**
     * Стоимость заявки
     */
    private Integer tripFactPrice;
    
    /**
     * Время простоя ТС
     */
    private Duration tripFactWaitTime;

    /**
     * Дата и время назначения водителя на заявку
     */
    private LocalDateTime tripAssignmentDateTime;
    
    private Vehicle vehicle;

    /**
     * ID связанной заявки
     */
    private UUID requestId;
    
    /**
     * Список ID связанных заявки при совместной поездке
     */
    private List<UUID> requestIds;
    
    /**
     * ID совместной поездки
     */
    private UUID sharedRideId;
    
    /**
     * Дата и время получения последнего сообщения из интеграции
     */
    private LocalDateTime lastXmlReceivedDateTime;

    /**
     * Флаг удаления
     */
    private boolean deleted;
    
    /**
     * Фактическое время ожидания в минутах
     */
    private Double registryFactWaitingTime;
    
    /**
     * Человекочитаемый идентификатор реестра
     */
    private String registryHumanReadableId;
    
    /**
     * Фактическая стоимость
     */
    private Double registryFactCost;
    
    /**
     * Фактическое расстояние
     */
    private Double registryFactDistance;
    
    /**
     * Факт оплаты
     */
    private Boolean registryFactPayment;
    
    /**
     * Водитель
     */
    private Driver driver;
    
    /**
     * Сообщение с данными водителя
     *
     * @param lastName фамилия.
     * @param firstName имя.
     * @param patronymic отчество.
     * @param phone номер телефона.
     */
    public record Driver(
            @JsonAlias("secName")
            String lastName,
            @JsonAlias("name")
            String firstName,
            String patronymic,
            String phone
    ) {
    }
    
    /**
     * Данные автомобиля.
     *
     * @param brand марка.
     * @param model модель.
     * @param color цвет.
     * @param stateNumber рег. номер.
     */
    public record Vehicle(
            String brand,
            String model,
            String color,
            String stateNumber
    ) {}
}
