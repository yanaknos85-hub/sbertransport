package ru.sberbank.ditsib.geo.utils.geometry;

import lombok.experimental.UtilityClass;
import ru.sberbank.utils.reflection.ReflectionUtils;

/**
 * Парсер WKT-формата.
 */
@UtilityClass
public class WktParser {
    
    /**
     * Разбор WKT-форматированной строки.
     *
     * @param source строка.
     * @param <T> type of shape.
     *
     * @return фигура.
     */
    public <T extends Shape> T parseWkt(String source) {
        Shape shape;
        if (source.startsWith("LINESTRING")) {
            shape = new PolyLine();
            var coordinatesFullString = source.substring(source.indexOf("(") + 1, source.indexOf(")"));
            var coordinatePairsString = coordinatesFullString.split(", ");
            for (var coordinatesPairString : coordinatePairsString) {
                var coordinatesPair = coordinatesPairString.split(" ");
                if (coordinatesPair.length != 2) {
                    throw new IllegalArgumentException(String.format("Unparcelable linestring '%s'", coordinatesFullString));
                }
                var x = Double.parseDouble(coordinatesPair[0]);
                var y = Double.parseDouble(coordinatesPair[1]);
                shape.addPoint(x, y);
            }
        } else {
            throw new IllegalArgumentException(String.format("Unknown WKT type for string '%s'", source));
        }
        return ReflectionUtils.cast(shape);
    }
    
}
