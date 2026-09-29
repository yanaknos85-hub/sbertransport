package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.With;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.sber.transport.telemechanic.database.model.Request_;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;

import java.util.Set;
import java.util.UUID;

@Schema(title = "Поиск заявок", description = "Поиск заявок с фильтрацией и пагинацией")
@With
public record RequestSearchDto(
        @Schema(description = "Идентификатор организации",
                example = "3fbb89b1-6d39-4c8f-8624-1afab05dbfa9")
        UUID organizationId,
        @Schema(description = "Список идентификаторов департаментов",
                example = "[\"ba905bbd-744c-4e85-9ece-d0a22ae8ae30\",\"3b7fb689-9756-4ff1-b020-9137294dc5a3\"]")
        Set<UUID> departmentIds,
        @Size(min = 3, max = 100)
        @Schema(description = "Поисковый текст (Человекочитаемый ID/государственный номер автомобиля/фамилия сотрудника, создавшего заявку)",
                example = "TM-0001-00005589",
                minLength = 3, maxLength = 100)
        String searchText,
        @Schema(description = "Список статусов заявки",
                example = "[\"IN_PROGRESS\", \"DONE\"]")
        Set<RequestStatus> requestStatusSet,
        @Schema(description = "Настройки разделения на страницы",
                example = "{\"page\":0,\"size\":10}")
        PageSetting pageSetting) {
    
    /**
     * Настройка разделения на страницы
     *
     * @param page Номер страницы
     * @param size Количество элементов на странице
     */
    @Schema(title = "Настройка пагинации", description = "Настройка пагинации")
    public record PageSetting(
            @Schema(description = "Номер страницы")
            int page,
            @Schema(description = "Количество элементов на странице")
            int size
    ) {
        /**
         * Конструктор для определения параметров по-умолчанию
         */
        public PageSetting() {
            this(0, 10);
        }
    }
    
    public static PageRequest getPageRequest(RequestSearchDto requestSearchDTO) {
        return getPageRequest(requestSearchDTO, getSort());
    }
    
    public static PageRequest getPageRequest(RequestSearchDto requestSearchDTO, @NotNull Sort sort) {
        if (requestSearchDTO.pageSetting() == null) {
            return PageRequest.of(0, 20, sort);
        }
        return PageRequest.of(requestSearchDTO.pageSetting().page(), requestSearchDTO.pageSetting().size(), sort);
    }
    
    @NotNull
    public static Sort getSort() {
        return Sort.by(Request_.CREATION_TIME).ascending();
    }
}
