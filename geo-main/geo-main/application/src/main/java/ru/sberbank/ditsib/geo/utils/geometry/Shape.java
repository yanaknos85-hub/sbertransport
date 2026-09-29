package ru.sberbank.ditsib.geo.utils.geometry;

/**
 * Интерфейс фигуры.
 */
public interface Shape {
    
    /**
     * Добавление координат.
     *
     * @param x Х-координата.
     * @param y У-координата.
     */
    void addPoint(double x, double y);
    
    /**
     * Get value of field.
     *
     * @param fieldName name of a field to get value.
     * @param <T> type of value.
     *
     * @return value of the field.
     */
    <T> T get(String fieldName);
}
