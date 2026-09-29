package ru.sberbank.ditsib.geo.utils.geometry;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.util.LinkedList;

/**
 * Линия, состоящая из множества линий.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class PolyLine implements Shape {
    
    private final LinkedList<Line> lines = new LinkedList<>();
    
    @Override
    public void addPoint(double x, double y) {
        if (lines.isEmpty() || (lines.getLast().getX() != x && lines.getLast().getY() != y)) {
            lines.add(new Line(x, y));
        }
    }
    
    @Override
    public <T> T get(String fieldName) {
        Object value = null;
        if ("lines".equals(fieldName)) {
            value = lines;
        }
        return ReflectionUtils.cast(value);
    }
    
}
