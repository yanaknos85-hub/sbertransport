package ru.sber.transport.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Function;

/**
 * Модель данных страницы.
 *
 * @param <T> тип включенных данных.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public final class Page<T> {

    private final List<T> content;

    private final PageData pageData;

    private final SortData sortData;

    /**
     * Создать модель данных на основании абстракции spring.
     *
     * @param source исходная абстракция.
     */
    public Page(org.springframework.data.domain.Page<T> source) {
        content = source.getContent();
        pageData = new PageData(source.getSize(),
            source.getNumber(),
            source.getNumberOfElements(),
            source.getTotalElements(),
            source.getTotalPages(),
            source.isFirst(),
            source.isLast());
        var sortIterator = source.getSort().iterator();
        if (sortIterator.hasNext()) {
            var sort = sortIterator.next();
            sortData = new SortData(sort.getProperty(), sort.isAscending());
        } else {
            sortData = null;
        }
    }

    /**
     * Конвертировать содержимое.
     *
     * @param converter конвертер.
     * @param <U> целевой тип данных.
     * @return объект страницы с конвертированными данными.
     */
    public <U> Page<U> map(Function<T, U> converter) {
        return new Page<>(
            content.stream().map(converter).toList(),
            pageData,
            sortData
        );
    }

    /**
     * Данные страницы.
     *
     * @param first признак первой страницы.
     * @param last признак последней страницы.
     * @param size размер страницы.
     * @param number номер страницы.
     * @param totalElements общее количество элементов.
     * @param totalPages общее количество страниц.
     * @param numberOfElements количество элементов на странице.
     */
    public record PageData(
        int size,
        int number,
        int numberOfElements,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last) {
    }

    /**
     * Данные сортировки.
     *
     * @param field поле для сортировки.
     * @param asc признак восходящей сортировки.
     */
    public record SortData(String field, boolean asc) {}
}
