package ru.sberbank.ditsib.transport.reports.dto.filters;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.sberbank.ditsib.converters.MillisDurationConverter;
import ru.sberbank.ditsib.transport.constants.ItinerantType;
import ru.sberbank.ditsib.transport.constants.RequestSortOption;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.config.converters.SpecialStringRemoveDeserializer;
import ru.sberbank.ditsib.transport.reports.dto.PersonalUIVisibilityDTO;
import ru.sberbank.ditsib.transport.reports.dto.PublicUIVisibilityDTO;
import ru.sberbank.ditsib.transport.reports.dto.TripPurposeDTO;
import ru.sberbank.ditsib.transport.reports.model.Employee;
import ru.sberbank.ditsib.transport.reports.model.ExpectedData;
import ru.sberbank.ditsib.transport.reports.model.Limit;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.validation.interval.NotNullValue;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.lang.reflect.Field;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Data
@NoArgsConstructor
@SuperBuilder
@Schema(title = "Фильтры для поиска поездок")
public class RequestReportDTO {
    
    @Schema(description = "Ид заявки")
    private UUID id;

    @Schema(description = "Ид организации")
    private UUID organizationId;

    @Schema(description = "Ид группы исполнителей")
    private List<UUID> executorGroupIds;
    
    @Schema(description = "Человекочитаемый идентификатор заявки", minLength = 3)
    @Size(min = 3, message = "Запрос должен содержать минимум 3 символа")
    private String requestHumanId;
    
    @Schema(description = "Статусы поездки")
    private Set<TripRequestStatus> requestStatusSet = new HashSet<>();
    
    @Schema(description = "Дата создания поездки, диапазон")
    @Valid
    private DateRange creationDate;
    
    @Schema(description = "Желаемая дата поездки, диапазон")
    @Valid
    private DateRange desiredDate;
    
    @Schema(description = "Дата закрытия обращения, диапазон")
    private DateRange requestClosedDatetime;
    
    @Schema(description = "ID цели поездки")
    private Set<TripPurposeDTO> purposeSet = new HashSet<>();
    
    @Schema(description = "Стоимость поездки (диапазон)")
    private DoubleRange expectedCost;
    
    @Schema(description = "Длительность поездки (диапазон)")
    private DoubleRange expectedDistance;
    
    @Schema(description = "ФИО пассажира", maxLength = 50, minLength = 3)
    @Size(max = 50, min = 3)
    @JsonDeserialize(using = SpecialStringRemoveDeserializer.class)
    private String employeeFIO;
    
    @Schema(description = "Табельный № заявителя")
    private String personnelNumber;
    
    @Schema(description = "Место возникновения затрат")
    private String costCenter;
    
    @Schema(description = "Балансовая единица")
    private Set<Integer> balanceUnitSet;
    
    @Schema(description = "ОЕ Подразделения")
    private String departmentCode;
    
    @Schema(description = "Подразделения 1го уровня")
    private List<String> department1;
    
    @Schema(description = "Подразделения 2го уровня")
    private List<String> department2;
    
    @Schema(description = "Подразделения 3го уровня")
    private List<String> department3;
    
    @Schema(description = "Подразделения 4го уровня")
    private List<String> department4;
    
    @Schema(description = "Подразделения 5го уровня")
    private List<String> department5;
    
    @Schema(description = "Подразделения 6го уровня")
    private List<String> department6;
    
    @Schema(description = "Список Ид тарифов")
    private Set<UUID> tariffIdSet;
    
    @Schema(description = "Перевозчик")
    private Set<UUID> contractorSet = new HashSet<>();
    
    @Schema(description = "Должность пассажира")
    private Set<UUID> employeePositionSet = new HashSet<>();
    
    @Schema(description = "Подразделение пассажира")
    private Set<UUID> employeeDepartmentSet = new HashSet<>();
    
    @Schema(description = "Организации пассажира")
    private Set<String> employeeOrganizationSet;
    
    @Schema(description = "Адрес отправления", maxLength = 50, minLength = 3)
    @Size(max = 50, min = 3)
    @JsonDeserialize(using = SpecialStringRemoveDeserializer.class)
    private String departureAddress;
    
    @Schema(description = "Адрес назначения", maxLength = 50, minLength = 3)
    @Size(max = 50, min = 3)
    @JsonDeserialize(using = SpecialStringRemoveDeserializer.class)
    private String destinationAddress;
    
    @Schema(description = "Список характеров деятельности сотрудника")
    private Set<ItinerantType> employeeItinerantTypeSet = new HashSet<>();
    
    @Schema(description = "Список оценок для фильтрации, от 0 до 5, 0 - без оценки")
    private Set<Integer> ratingMarkSet;
    
    @Schema(description = "Количество пассажиров")
    private Set<Integer> passengerCountSet;
    
    @Schema(description = "Настройки сортировки")
    private SortSetting sortSetting;
    
    @Schema(description = "Настройки разделения на страницы")
    private PageSetting pageSetting;
    
    @Schema(description = "Причины отмены")
    private Set<Integer> requestStatusCodes;
    
    @Schema(description = "Контрольный срок")
    private Boolean deadlineState;
    
    @Schema(description = "Контрольная дата")
    private String deadline;
    
    @Schema(description = "Экономия")
    private Boolean savings;
    
    @Schema(description = "Номер договора")
    private String contractNumber;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @NotNullValue(fields = { "start", "end" })
    public static class DateRange {
        private LocalDateTime start;
        private LocalDateTime end;
    }
    
    @Data
    public static class DurationRange {
        @JsonDeserialize(using = MillisDurationConverter.class)
        private Duration start;
        
        @JsonDeserialize(using = MillisDurationConverter.class)
        private Duration end;
    }
    
    @Data
    public static class DoubleRange {
        @NotNull
        private Double start;
        private Double end;
    }
    
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IntegerRange {
        @NotNull
        private Integer start;
        private Integer end;
    }
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrganizationSetting {
        private Set<String> employeeOrganizationSet;
    }
    
    @Getter
    @Setter
    @ToString
    public static class SortSetting {
        @Schema(description = "Выбор сортировки")
        private RequestSortOption property = RequestSortOption.CREATION_DATE;
        
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
    
    public boolean isEmpty() {
        return isEmpty(RequestReportDTO.class.getDeclaredFields());
    }
    
    protected boolean isEmpty(Field[] fieldArray) {
        var isEmpty = new HashMap<String, Boolean>();
        for (var declaredField : fieldArray) {
            if (SortSetting.class.equals(declaredField.getType()) ||
                PageSetting.class.equals(declaredField.getType()) ||
                OrganizationSetting.class.equals(declaredField.getType()) ||
                PersonalUIVisibilityDTO.class.equals(declaredField.getType()) ||
                PublicUIVisibilityDTO.class.equals(declaredField.getType())) {
                continue;
            }
            
            declaredField.setAccessible(true); // NOSONAR
            try {
                var value = declaredField.get(this);
                var empty = value == null
                            || (Collection.class.isAssignableFrom(declaredField.getType()) && ((Collection<?>) value).isEmpty())
                            || (String.class.isAssignableFrom(declaredField.getType()) && StringUtils.isEmpty(value.toString()));
                isEmpty.put(declaredField.getName(), empty);
            } catch (IllegalAccessException ignore) {
                // ignore
            }
        }
        return isEmpty.values().stream().allMatch(Boolean::booleanValue);
    }
    
    public static PageRequest getPageRequest(RequestReportDTO requestSearchDTO) {
        Sort sort = getSort(requestSearchDTO);
        if (requestSearchDTO.getPageSetting() == null) {
            return PageRequest.of(0, 20, sort);
        }
        return PageRequest.of(requestSearchDTO.getPageSetting().getPage(), requestSearchDTO.getPageSetting().getSize(), sort);
    }
    
    public static PageRequest getPageRequest(
            RequestReportDTO requestSearchDTO,
            @NotNull Sort sort
                                            ) {
        if (requestSearchDTO.getPageSetting() == null) {
            return PageRequest.of(0, 20, sort);
        }
        return PageRequest.of(requestSearchDTO.getPageSetting().getPage(), requestSearchDTO.getPageSetting().getSize(), sort);
    }
    
    public static Sort getSort(RequestReportDTO requestSearchDTO) {
        if (requestSearchDTO.getSortSetting() == null) {
            return Sort.sort(Request.class).by(Request::getCreationTime).descending();
        }
        
        var sort = switch (requestSearchDTO.getSortSetting().getProperty()) {
            case PASSENGER_FULL_NAME -> createFullnameSort();
            case REQUEST_ID -> Sort.sort(Request.class).by(Request::getId);
            case REQUEST_HUMAN_ID -> Sort.sort(Request.class).by(Request::getHumanReadableId);
            case CREATION_DATE -> Sort.sort(Request.class).by(Request::getCreationTime);
            case DESIRED_DATE -> Sort.sort(Request.class).by(Request::getDesiredDate);
            case EXPECTED_COST -> Sort.sort(Request.class).by(Request::getExpected).by(ExpectedData::getCost);
            case LIMIT_ID -> Sort.sort(Request.class).by(Request::getLimit).by(Limit::getHumanReadableId);
            default -> Sort.sort(Request.class).by(Request::getCreationTime);
        };
        
        return requestSearchDTO.getSortSetting().isDirectionAsc() ? sort.ascending() : sort.descending();
    }
    
    private static Sort createFullnameSort() {
        Sort sort;
        Sort.TypedSort<Request> request = Sort.sort(Request.class);
        Sort sortLastName = Sort.sort(Request.class).by(Request::getPassenger).by(Employee::getLastName);
        Sort sortFirstName = Sort.sort(Request.class).by(Request::getPassenger).by(Employee::getFirstName);
        Sort sortPatronymic = Sort.sort(Request.class).by(Request::getPassenger).by(Employee::getPatronymic);
        
        sort = request.and(sortLastName).and(sortFirstName).and(sortPatronymic);
        return sort;
    }
    
}

