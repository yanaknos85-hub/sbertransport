package ru.sberbank.ditsib.transport.request.dto.dispatcherRoom;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(title = "Данные аттрибутов для диспетчера",
        description = "Данные для отображения атрибутов для диспетчерской")
public class DefaultDispatcherAttributesDTO {
   
    @NotNull
    @Schema(description = "Видимость поля: Идентификатор", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
    private Boolean requestIdVisible = false;
 
    @NotNull
    @Schema(description = "Видимость поля: Идентификатор (человекочитаемый)", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean humanReadableIdVisible = true;

    @NotNull
    @Schema(description = "Видимость поля: Статус заявки", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean statusVisible = true;
    
    @NotNull
    @Schema(description = "Видимость поля: Пассажир", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean passengerVisible = true;

    @NotNull
    @Schema(description = "Видимость поля: Должность пассажира", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean passengerPositionVisible = true;

    @NotNull
    @Schema(description = "Видимость поля: Телефона пассажира", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean passengerMobilePhoneVisible = true;
    
    @NotNull
    @Schema(description = "Видимость поля: Комментарий для водителя", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
    private Boolean commentForDriverVisible = false;
    
    @NotNull
    @Schema(description = "Видимость поля: Адрес подачи", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean departureAddressVisible = true;
    
    @NotNull
    @Schema(description = "Видимость поля: Промежуточные адреса", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean intermediateAddressesVisible = true;
    
    @NotNull
    @Schema(description = "Видимость поля: Адрес назначения", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean destinationAddressVisible = true;
    
    @NotNull
    @Schema(description = "Видимость поля: Дата и время создания заявки", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean creationTimeVisible = true;
    
    @NotNull
    @Schema(description = "Видимость поля: Желаемая дата и время отправления", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean desiredDateVisible = true;
    
    @NotNull
    @Schema(description = "Видимость поля: Состояние контрольного срока", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean deadlineStateVisible = true;
    
    @NotNull
    @Schema(description = "Видимость поля: Контрольный срок, мин", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean deadlineVisible = true;
    
    @NotNull
    @Schema(description = "Видимость поля: Предварительная стоимость, руб", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
    private Boolean costVisible = false;
    
    @NotNull
    @Schema(description = "Видимость поля: Предварительный километраж, км", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
    private Boolean distanceVisible = false;
    
    @NotNull
    @Schema(description = "Видимость поля: Время ожидания, мин", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
    private Boolean waitTimeVisible = false;
    
    @NotNull
    @Schema(description = "Видимость поля: Количество пассажиров", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
    private Boolean passengerCountVisible = false;
    
    @NotNull
    @Schema(description = "Видимость поля: Идентификатор используемого тарифа", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
    private Boolean tariffIdVisible = false;

    @NotNull
    @Schema(description = "Видимость поля: Тариф за км", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
    private Boolean tariffDistanceVisible = false;

    @NotNull
    @Schema(description = "Видимость поля: Тариф за мин", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
    private Boolean tariffTimeVisible = false;
    
    @NotNull
    @Schema(description = "Видимость поля: Класс автомобиля", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean taxiClassVisible = true;
    
    @NotNull
    @Schema(description = "Видимость поля: Перевозчик", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean contractorIdVisible = true;
    
    @NotNull
    @Schema(description = "Видимость поля: Дата и время получения заявки перевозчиком", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
    private Boolean dateTimeRegisteredVisible = false;
    
    @NotNull
    @Schema(description = "Видимость поля: Фактическое время поиска автомобиля, мин", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "false")
    private Boolean factSearchTimeVisible = false;
    
    @NotNull
    @Schema(description = "Видимость поля: Фактическое время выезда", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean tripStartTimeVisible = true;
    
    @NotNull
    @Schema(description = "Видимость поля: Фактическая дата и время закрытия заявки", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean tripFinishTimeVisible = true;
    
    @NotNull
    @Schema(description = "Видимость поля: Ответственный диспетчер", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean dispatcherVisible = true;

    @NotNull
    @Schema(description = "Видимость поля: Назначеный водитель", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean driverInfoVisible = true;

    @NotNull
    @Schema(description = "Видимость поля: Рейтинг водителя", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean driverRateVisible = false;

    @NotNull
    @Schema(description = "Видимость поля: Назначеный транспорт", requiredMode = Schema.RequiredMode.REQUIRED, defaultValue = "true")
    private Boolean driverAutoInfoVisible = true;

}
