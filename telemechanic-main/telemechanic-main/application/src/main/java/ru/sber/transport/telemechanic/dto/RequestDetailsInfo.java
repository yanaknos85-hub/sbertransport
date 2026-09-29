package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@Schema(title = "Данные ответа на запрос добавления значения одометра на выезде", description = "Данные ответа на запрос добавления значения одометра на выезде")
public class RequestDetailsInfo {
    @Schema(description = "Идентификатор заявки телемеханика")
    private UUID id;
    @Schema(description = "Человекочитаемый идентификатор заявки телемеханика")
    private String humanReadableId;
    @Schema(description = "Дата и время создания заявки телемеханика")
    private LocalDateTime creationTime;
    @Schema(description = "Статус заявки телемеханика")
    private RequestStatus requestStatus;
    @Schema(description = "Пробег при выезде")
    private int odometerOut;
    @Schema(description = "Индикатор версии телемеханика (ewbPath=true - работа с ЭПЛ/ewbPath=false - работа по старому пути)")
    private boolean ewbPath;
    @Schema(description = "Проверки")
    private Set<CheckDto> checks;
    @Schema(description = "Данные по ЭПЛ")
    private Ewb ewb;
    @Schema(description = "Автор")
    private Author author;
    @Schema(description = "Транспорт")
    private Transport transport;
    
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Schema(name = "AddOdometerValueResponse.Ewb")
    public static class Ewb {
        @Schema(description = "Идентификатор ЭПЛ")
        private UUID id;
        @Schema(description = "Статус ЭПЛ")
        private EwbStatus ewbStatus;
        @Schema(description = "Идентификатор заявки медика")
        private UUID medicRequestId;
        @Schema(description = "Статус заявки медика")
        private TelemedicineStatus medicStatus;
    }
    
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Schema(name = "AddOdometerValueResponse.Author")
    public static class Author {
        @Schema(description = "Идентификатор автора")
        private UUID id;
        @Schema(description = "Имя")
        private String firstName;
        @Schema(description = "Фамилия")
        private String lastName;
        @Schema(description = "Отчество")
        private String patronymic;
        @Schema(description = "Персональный номер")
        private String personnelNumber;
    }
    
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @Schema(name = "AddOdometerValueResponse.Transport")
    public static class Transport {
        @Schema(description = "Идентификатор транспорта")
        private UUID id;
        @Schema(description = "Государственный номер")
        private String stateNumber;
        @Schema(description = "Марка")
        private String brand;
        @Schema(description = "Модель")
        private String model;
        @Schema(description = "Текущий пробег")
        private int mileage;
    }
}
