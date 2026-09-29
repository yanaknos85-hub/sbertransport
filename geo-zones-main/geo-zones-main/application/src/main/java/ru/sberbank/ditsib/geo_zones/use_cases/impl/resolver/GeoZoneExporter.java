package ru.sberbank.ditsib.geo_zones.use_cases.impl.resolver;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sberbank.ditsib.geo_zones.export.dto.GeoZoneFileDTO;
import ru.sberbank.ditsib.geo_zones.use_cases.GeoZoneCases;
import ru.sberbank.ditsib.geo_zones.use_cases.model.GeoZoneWithChildren;

import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Реализация распознавания документа с геозонами.
 */
@Component
@RequiredArgsConstructor
class GeoZoneExporter implements DataExporter<GeoZoneFileDTO> {

    private final GeoZoneCases geoZoneCases;

    @Override
    public @NonNull List<GeoZoneFileDTO> exportData(@NonNull Map<String, ?> parameters, @NonNull JwtAuthenticationToken authentication) {
        var result = new ArrayList<GeoZoneFileDTO>();
        geoZoneCases.getRoots().forEach(elt -> addToResult(elt, result));
        return result;
    }

    /**
     * Добавит зону к результату.
     *
     * @param geoZone зона.
     * @param result  результат.
     */
    private void addToResult(GeoZoneWithChildren geoZone, @NotNull ArrayList<GeoZoneFileDTO> result) {
        result.add(toFileDTO(geoZone));
        geoZone.getChildren().forEach(elt -> addToResult(elt, result));
    }

    /**
     * Конвертация зоны с ребенком в зону для файла.
     *
     * @param geoZone исходные данные.
     * @return данные для файла.
     */
    private GeoZoneFileDTO toFileDTO(GeoZoneWithChildren geoZone) {
        return GeoZoneFileDTO.builder()
            .name(geoZone.getName())
            .code(geoZone.getCode())
            .parentCode(geoZone.getParentCode())
            .parentName(geoZone.getParentName())
            .build();
    }
}
