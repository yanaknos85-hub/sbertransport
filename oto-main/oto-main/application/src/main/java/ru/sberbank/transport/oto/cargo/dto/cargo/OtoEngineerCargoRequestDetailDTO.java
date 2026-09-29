package ru.sberbank.transport.oto.cargo.dto.cargo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.transport.oto.cargo.dto.ContractorDTO;

import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Детальная информация по заявке для инженера ОТО", description = "Детальная информация по заявке для инженера ОТО")
public class OtoEngineerCargoRequestDetailDTO {
    
    /**
     * Человекочитаемы идентификаторь заявки
     */
    @Schema(description = "Идентификатор (человекочитаемый)")
    private String humanReadableId;
    
    /**
     * Статус заявки
     */
    @Schema(description = "Статус заявки")
    private String status;
    
    /**
     * Дата и время сбора (факт)
     */
    @Schema(description = "Время завершения сборки груза")
    private ZonedDateTime transferTime;
    
    /**
     * Дата и время доставки (факт)
     */
    @Schema(description = "Время завершения доставки груза")
    private ZonedDateTime shipmentTime;
    
    /**
     * ФИО заявителя заявки, автор
     */
    @Schema(description = "Отправитель заявки на грузоперевозку(ФИО)")
    private String author;
    
    /**
     * Телефон заявителя, автора
     */
    private String authorPhone;
    
    /**
     * Дата отправления (ДД:ММ:ГГГГ)
     */
    @Schema(description = "Желаемое время поездки")
    private ZonedDateTime desiredDate;
    
    /**
     * ФИО отправителя
     */
    @Schema(description = "ФИО отправителя")
    private String sender;
    
    /**
     * Телефон отправителя
     */
    @Schema(description = "Телефон отправителя")
    private String senderPhone;
    
    /**
     * Адрес отправления
     */
    @Schema(description = "Адрес отправления")
    private String senderAddress;
    
    /**
     * Организация-отправитель
     */
    @Schema(description = "Организация-отправитель")
    private String senderOrganization;
    
    /**
     * Получатель заявки на грузоперевозку
     */
    @Schema(description = "Получатель заявки на грузоперевозку(ФИО)")
    private String recipient;
    
    /**
     * Организация-получатель
     */
    @Schema(description = "Организация-получатель")
    private String recipientOrganization;
    
    /**
     * Телефон получателя
     */
    @Schema(description = "Телефон получателя")
    private String recipientPhone;
    
    /**
     * Адрес получения
     */
    @Schema(description = "Адрес получения")
    private String recipientAddress;
    
    /**
     * Общий вес (было Вес), кг
     */
    @Schema(description = "Общий вес (было Вес), кг")
    private String weight;
    
    /**
     * Общий объем (было Объем), м3
     */
    @Schema(description = "Общий объем (было Объем), м3")
    private String volume;
    
    /**
     * Дата создания поездки
     */
    @Schema(description = "Время создания")
    private ZonedDateTime creationTime;
    
    /**
     * Источник создания
     */
    @Schema(description = "Источник создания")
    private String source;
    
    /**
     * Номер маршрута: человеко читаемый, либо UUID, либо '-'
     */
    @Schema(description = "Номер маршрута: человеко читаемый, либо UUID, либо '-'")
    private String routeNumber;
    
    /**
     * Идентификатор маршрута
     */
    @Schema(description = "Идентификатор маршрута", nullable = true)
    private UUID routeId;

    /**
     * Идентификатор заявки
     */
    @Schema(description = "Идентификатор")
    private UUID id;
    
    /**
     * Тип транспорта
     */
    @Schema(description = "Тип транспорта")
    private String cargoTransportType;

    /**
     * Предварительные данные поездки
     */
    @Schema(description = "Предварительные данные поездки")
    private ExpectedCargoDataDTO expected;
    
    /**
     * Информация о перевозчике
     */
    @Schema(description = "Информация о перевозчике(контрагент)")
    private ContractorDTO contractor;
    
    /**
     * Плановая стоимость
     */
    @Schema(description = "Плановая стоимость")
    private Double plannedPrice;
    
    /**
     * Плановая стоимость
     */
    @Schema(description = "Плановая дальность")
    private Double plannedRange;


    /**
     * Перечисление типлв грузов
     */
    @Schema(description = "Перечисление типов груза в заявке")
    private String cargoTypes;
    
    /**
     * Комментарий для водителя
     */
    @Schema(description = "Комментарий для водителя")
    private String commonComment;

    @Schema(description = "Контрольная дата с учетом выходных дней")
    private ZonedDateTime controlDate;
    
    /**
     * Запрос созданный по расписанию (регулярная перевозка)
     */
    @Schema(description = "Запрос созданный по расписанию (регулярная перевозка)")
    private String template;
    
    @Schema(description = "Количество грузчиков")
    private int loaders;

    @Schema(description = "Тип заявки")
    private String requestType;
}
