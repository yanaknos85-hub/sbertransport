package ru.sberbank.ditsib.geo.config.properties.routing;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Available types of route.
 */
@Schema
public enum RouteType {

    /**
     * Route for cars.
     */
    CAR,

    /**
     * Route for pedestrian.
     */
    PEDESTRIAN
}
