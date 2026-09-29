package ru.sberbank.ditsib.transport.request.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.sberbank.ditsib.converters.MillisDurationConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.transport.constants.RequestSortOption;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.config.converters.SpecialStringRemoveDeserializer;
import ru.sberbank.ditsib.transport.request.database.model.ExpectedData;
import ru.sberbank.ditsib.transport.request.database.model.Request;
import ru.sberbank.ditsib.transport.request.database.model.Request_;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.validation.interval.Interval;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Schema(title = "Поиск заявки", description = "Фильтры для поиска заявок")
public class RequestSearchDTO {
    
    @Schema(description = "Тип транспорта")
    private TransportTypeEnum transportTypeEnum;
    
    @Schema(description = "Типы транспорта")
    private Set<TransportTypeEnum> transportTypeSet;
    
    @Schema(description = "Идентификатор заявки")
    private UUID requestId;
    
    @Size(max = 20, min = 3)
    @Schema(description = "Человекочитаемый идентификатор заявки")
    private String humanReadableId;
    
    @Schema(description = "Идентификатор лимита")
    private UUID limitId;
    
    @Schema(description = "Список статусов поездки")
    private Set<String> requestStatusSet;
    
    @Schema(description = "Дата создания поездки")
    @Valid
    private RequestSearchDTO.DateRange creationDate;
    
    @Schema(description = "Дата поездки")
    @Valid
    private DateRange desiredDate;
    
    @Schema(description = "Список целей поездки")
    private Set<TripPurposeDTO> purposeSet;
    
    @Schema(description = "Стоимость")
    @Valid
    private DoubleRange expectedCost;
    
    @Schema(description = "Расстояние")
    @Valid
    private DoubleRange expectedDistance;
    
    @Schema(description = "Экономия для текущего заказчика")
    @Valid
    private DoubleRange kpiSaving;
    
    @Schema(description = "Идентификатор пассажира")
    private UUID passengerId;
    
    @Schema(description = "Табельный номер пользователя", maxLength = 20, minLength = 3)
    @Size(max = 20, min = 3)
    @JsonDeserialize(using = SpecialStringRemoveDeserializer.class)
    private String employeeNumber;
    
    @Schema(description = "ФИО пассажира", maxLength = 50, minLength = 3)
    @Size(max = 50, min = 3)
    @JsonDeserialize(using = SpecialStringRemoveDeserializer.class)
    private String employeeFIO;
    
    @Schema(description = "Список должностей пассажира")
    private Set<UUID> employeePositionSet;
    
    @Schema(description = "Список подразделений пассажира")
    private Set<UUID> employeeDepartmentSet;
    
    @Schema(description = "Адрес отправления", maxLength = 50, minLength = 3)
    @Size(max = 50, min = 3)
    @JsonDeserialize(using = SpecialStringRemoveDeserializer.class)
    private String departureAddress;
    
    @Schema(description = "Адрес назначения", maxLength = 50, minLength = 3)
    @Size(max = 50, min = 3)
    @JsonDeserialize(using = SpecialStringRemoveDeserializer.class)
    private String destinationAddress;
    
    @Schema(description = "Точка маршрута", maxLength = 20, minLength = 3)
    @Size(max = 50, min = 3)
    @JsonDeserialize(using = SpecialStringRemoveDeserializer.class)
    private String waypointAddress;
    
    @Schema(description = "Время ожидания на точках маршрута")
    private DurationRange waypointWaitTime;
    
    @Schema(description = "Поездка завершена Да/Нет")
    private Boolean terminalStatus;
    
    @Schema(description = "Настройки сортировки")
    private SortSetting sortSetting;
    
    @Schema(description = "Настройки разделения на страницы")
    private PageSetting pageSetting;
    
    @Getter
    @Setter
    @ToString
    public static class SortSetting {
        @Schema(description = "Выбор сортировки")
        private RequestSortOption property = RequestSortOption.DESIRED_DATE;
        
        @Schema(description = "Направление сортировки")
        private boolean directionAsc = true;
    }
    
    @Getter
    @Setter
    @ToString
    public static class PageSetting {
        @Schema(description = "Номер страницы")
        private int page = 0;
        
        @Schema(description = "Количество элементов на странице")
        private int size = 20;
    }
    
    @Getter
    @Setter
    @ToString
    @Interval(startField = "start", endField = "end", inclusion = Interval.Include.INCLUDE)
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DateRange {
        
        @PastOrPresent
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        private LocalDateTime start;
        
        @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
        private LocalDateTime end;
    }
    
    @Getter
    @Setter
    @Interval(startField = "start", endField = "end", inclusion = Interval.Include.INCLUDE)
    @ToString
    public static class DoubleRange {
        @NotNull
        private Double start;
        
        private Double end;
        
        public void prepareQuery() {
            if (end == null) {
                end = start;
            }
        }
    }
    
    @Getter
    @Setter
    @Interval(startField = "start", endField = "end", inclusion = Interval.Include.INCLUDE)
    @ToString
    public static class DurationRange {
        @JsonDeserialize(using = MillisDurationConverter.class)
        private Duration start;
        
        @JsonDeserialize(using = MillisDurationConverter.class)
        private Duration end;
    }
    
    public boolean isJoinWaypoint() {
        return !StringUtils.isEmpty(departureAddress) ||
               !StringUtils.isEmpty(destinationAddress) ||
               !StringUtils.isEmpty(waypointAddress) ||
               waypointWaitTime != null;
    }
    
    
    public static PageRequest getPageRequest(RequestSearchDTO requestSearchDTO) {
        return getPageRequest(requestSearchDTO, getSort(requestSearchDTO));
    }
    
    public static PageRequest getPageRequest(RequestSearchDTO requestSearchDTO, @NotNull Sort sort) {
        if (requestSearchDTO.getPageSetting() == null) {
            return PageRequest.of(0, 20, sort);
        }
        return PageRequest.of(requestSearchDTO.getPageSetting().getPage(), requestSearchDTO.getPageSetting().getSize(), sort);
    }
    
    @NotNull
    public static Sort getSort(RequestSearchDTO requestSearchDTO) {
        if (requestSearchDTO.getSortSetting() == null) {
            return Sort.by(Sort.Direction.ASC, Request_.DESIRED_DATE);
        }
        
        Sort sort;
        switch (requestSearchDTO.getSortSetting().getProperty()) {
            case PASSENGER_FULL_NAME: {
                Sort.TypedSort<Request> request = Sort.sort(Request.class);
                Sort sortLastName = Sort.sort(Request.class).by(Request::getPassenger).by(Employee::getLastName);
                Sort sortFirstName = Sort.sort(Request.class).by(Request::getPassenger).by(Employee::getFirstName);
                Sort sortPatronymic = Sort.sort(Request.class).by(Request::getPassenger).by(Employee::getPatronymic);
                
                sort = request.and(sortLastName).and(sortFirstName).and(sortPatronymic);
                break;
            }
            case REQUEST_ID: {
                sort = Sort.by(Request_.ID);
                break;
            }
            case REQUEST_HUMAN_ID: {
                sort = Sort.by(Request_.HUMAN_READABLE_ID);
                break;
            }
            case CREATION_DATE: {
                sort = Sort.by(Request_.CREATION_TIME);
                break;
            }
            case DESIRED_DATE: {
                sort = Sort.by(Request_.DESIRED_DATE);
                break;
            }
            
            case EXPECTED_COST: {
                sort = Sort.sort(Request.class).by(Request::getExpected).by(ExpectedData::getCost);
                break;
            }
            
            default: {
                sort = Sort.by(Request_.CREATION_TIME);
            }
        }
        
        return requestSearchDTO.getSortSetting().isDirectionAsc() ? sort.ascending() : sort.descending();
    }
    
}
