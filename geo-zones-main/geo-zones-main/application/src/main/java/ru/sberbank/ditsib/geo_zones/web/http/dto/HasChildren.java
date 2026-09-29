package ru.sberbank.ditsib.geo_zones.web.http.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public interface HasChildren {

    @Schema(description = "Список дочерних объектов")
    List<? extends HasChildren> getChildren();

}
