package ru.sber.transport.request.external.model;

import java.util.List;

/**
 * Интерфейс для модели страницы с сортировкой.
 *
 * @param <T> тип элемента на странице
 */
public interface Page<T> {

    /**
     * Возвращает список элементов на странице.
     *
     * @return список элементов на странице
     */
    List<T> content();

    /**
     * Возвращает данные о странице.
     *
     * @return данные о странице
     */
    PageData page();

    /**
     * Возвращает данные о сортировке.
     *
     * @return данные о сортировке
     */
    SortData sort();

}
