package ru.sber.transport.cargo.exchange.request.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import ru.sber.transport.cargo.exchange.request.database.model.Request_;

import java.math.BigDecimal;
import java.time.LocalDate;

import static ru.sber.transport.cargo.exchange.request.dto.common.FilterComponents.SortOption.REQUEST_HUMAN_ID;

public class FilterComponents {

    @Getter
    @Schema(description = "Настройки сортировки")
    @AllArgsConstructor
    public static class SortSetting {
        @Schema(description = "Выбор сортировки")
        private SortOption property = REQUEST_HUMAN_ID;

        @Schema(description = "Направление сортировки")
        private boolean directionAsc = false;

        public Sort getSort() {

            Sort sort;
            switch (this.getProperty()) {
                case REQUEST_ID: {
                    sort = Sort.by(Request_.ID);
                    break;
                }
                case REQUEST_HUMAN_ID: {
                    sort = Sort.by(Request_.HUMAN_READABLE_ID);
                    break;
                }
                case CREATION_DATE: {
                    sort = Sort.by(Request_.CREATED_AT);
                    break;
                }
                case LOADING_DATE: {
                    sort = Sort.by(Request_.LOADING_DATE);
                    break;
                }
                case DELIVERY_DATE: {
                    sort = Sort.by(Request_.DELIVERY_DATE);
                    break;
                }

                default: {
                    sort = Sort.by(Request_.CREATED_AT);
                }
            }

            return this.isDirectionAsc() ? sort.ascending() : sort.descending();
        }
    }

    @Getter
    @Schema(description = "Настройки пагинации")
    @AllArgsConstructor
    public static class PageSetting {
        @Schema(description = "Номер страницы", example = "0")
        private int page = 0;

        @Schema(description = "Количество элементов на странице", example = "20")
        private int size = 10;
    }

    @Getter
    @AllArgsConstructor
    public enum SortOption {
        REQUEST_ID("ID поездки"),
        REQUEST_HUMAN_ID("ID поездки человекочитаемый"),
        CREATION_DATE("Время создания поездки"),
        LOADING_DATE("Дата погрузки"),
        DELIVERY_DATE("Дата доставки");

        @Schema(description = "Описание опции сортировки")
        private final String description;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "Диапазон значений BigDecimal")
    public static class BigDecimalRange {
        private BigDecimal from;
        private BigDecimal to;
    }

    @Getter
    @AllArgsConstructor
    @Schema(description = "Диапазон дат")
    public static class DateRange {
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate start;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        private LocalDate end;
    }


}