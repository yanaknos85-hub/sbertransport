package ru.sberbank.ditsib.transport.request.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import ru.sber.transport.magenta.model.EmployeeDTO;
import ru.sber.transport.magenta.model.KpiDTO;
import ru.sber.transport.magenta.model.MagentaWaypointResponseDTO;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.dto.personal.PersonalCarDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class ShareRideResponseDTO {
    
    @Schema(
            description = "Идентификатор",
            required = true
    )
    private String id;
    
    @Schema(
            description = "Тип транспорта",
            required = true
    )
    private TransportTypeEnum transportType;
    @Schema(
            description = "Количество пассажиров",
            required = true
    )
    private Integer passengers;
    
    @Schema(
            description = "Список идентификаторов новых заказов",
            required = true,
            deprecated = true
    )
    private List<String> newOrdersIds = new ArrayList();
    
    @Schema(
            description = "Список сотрудников участников поездки",
            required = true,
            deprecated = true
    )
    private List<EmployeeDTO> employeePassengers = new ArrayList();
    
    @Schema(description = "Присоединенные пассажиры")
    private List<EmployeeDTO> joinedPassengers = new ArrayList<>();
    
    @Schema(
            description = "Остановки маршртута",
            required = true
    )
    private List<MagentaWaypointResponseDTO> stops = new ArrayList();
    
    @Schema(
            description = "Расчётный KPI  поездки",
            required = true
    )
    private KpiDTO kpi;
    
    @Schema(description = "Список заявок совместной поездки")
    private List<SharedRideResponseRequestDTO> requests = new ArrayList<>();
    
    @Schema(description = "Класс такси")
    private TaxiClass taxiClass;
    
    @Data
    @Builder
    public static class SharedRideResponseRequestDTO {
        
        @Schema(description = "ИД заявки")
        private UUID id;
        
        @Schema(description = "Номер заявки")
        private String humanReadableId;
        
        @Schema(description = "Признак владельца заявки")
        @Builder.Default
        private boolean sharedRideOwner = false;
        
        @Schema(description = "Данные сотрудника")
        private EmployeeDTO employee;
        
        @Schema(description = "Данные о машине")
        private PersonalCarDTO car;
        
        @Schema(description = "Статус заявки")
        private TripRequestStatus status;
        
        @Schema(description = "Статус код")
        private Integer statusCode;
        
        @Schema(description = "Текст статуса")
        private String statusDescription;
        
        @Schema(description = "Комментарий для водителя")
        private String commentForDriver;
        
    }
}
