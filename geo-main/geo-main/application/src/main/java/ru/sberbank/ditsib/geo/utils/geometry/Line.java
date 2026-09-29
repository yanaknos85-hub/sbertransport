package ru.sberbank.ditsib.geo.utils.geometry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.sberbank.utils.reflection.ReflectionUtils;

/**
 * Фигура линии.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class Line implements Shape {
    
    private final Double x;
    
    private final Double y;
    
    @Override
    public void addPoint(double x, double y) {
        throw new UnsupportedOperationException("Adding point to line is not supported");
    }
    
    @Override
    public <T> T get(String fieldName) {
        Object value = switch (fieldName) {
            case "x" -> x;
            case "y" -> y;
            default -> null;
        };
        return ReflectionUtils.cast(value);
    }
    
}
