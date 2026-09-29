package ru.sberbank.ditsib.geo.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Координаты для восстановления маршрута.
 */
@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@SuperBuilder
public class RouteRecreationCoordinates extends Coordinates {
    private long time;
}
