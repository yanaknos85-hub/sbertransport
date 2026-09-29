package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.sber.transport.telemechanic.database.model.Employee_;
import ru.sber.transport.telemechanic.database.model.Organization_;
import ru.sber.transport.telemechanic.database.model.Request_;
import ru.sber.transport.telemechanic.enumerate.RequestSortOption;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Schema(title = "Поиск заявки для реестра", description = "Поиск заявок с фильтрацией и пагинацией")
@Builder
public class ReportSearchDto {

    @Schema(description = "Табельный номер")
    private String personnelNumber;
    @Schema(description = "Номер заявки")
    private String humanReadableId;
    @Schema(description = "Идентификатор организации, к которой привязана заявка")
    private UUID organizationId;
    @Schema(description = "Настройки сортировки")
    private SortSetting sortSetting;
    @Schema(description = "Настройки разделения на страницы")
    private PageSetting pageSetting;
    @Schema(description = "Период создания заявки")
    private DateRange period;
    @Schema(description = "Идентификаторы подразделений, к которой привязана заявка")
    private Set<UUID> departmentIds;
    
    @Getter
    @Setter
    @ToString
    public static class DateRange {
        @Schema(description = "Начало периода")
        private LocalDateTime start;
        
        @Schema(description = "Конец периода")
        private LocalDateTime end;
    }

    @Getter
    @Setter
    @ToString
    public static class SortSetting {
        @Schema(description = "Выбор сортировки")
        private RequestSortOption property = RequestSortOption.HUMAN_READABLE_ID;

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


    public static PageRequest getPageRequest(ReportSearchDto requestSearchDTO) {
        return getPageRequest(requestSearchDTO, getSort(requestSearchDTO));
    }

    public static PageRequest getPageRequest(ReportSearchDto requestSearchDTO, @NotNull Sort sort) {
        if (requestSearchDTO.getPageSetting() == null) {
            return PageRequest.of(0, 20, sort);
        }
        return PageRequest.of(requestSearchDTO.getPageSetting().getPage(), requestSearchDTO.getPageSetting().getSize(), sort);
    }

    @NotNull
    public static Sort getSort(ReportSearchDto requestSearchDTO) {

        if (requestSearchDTO.getSortSetting() == null) {
            return Sort.by(Sort.Direction.DESC, Request_.HUMAN_READABLE_ID);
        }

        Sort sort;
        switch (requestSearchDTO.getSortSetting().getProperty()) {
            case ID -> sort = Sort.by(Request_.ID);
            case ORGANIZATION_OFFICIAL_NAME ->
                    sort = Sort.by(Request_.AUTHOR
                            .concat(".")
                            .concat(Employee_.ORGANIZATION)
                            .concat(".")
                            .concat(Organization_.OFFICIAL_NAME));
            default -> sort = Sort.by(Request_.HUMAN_READABLE_ID);
        }

        return requestSearchDTO.getSortSetting().isDirectionAsc() ? sort.ascending() : sort.descending();
    }

}


