package ru.sberbank.ditsib.geo_zones.use_cases.impl.resolver;

import org.springframework.transaction.annotation.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.file_works.importer.DataImporter;
import ru.sberbank.ditsib.geo_zones.export.dto.GeoZoneFileDTO;
import ru.sberbank.ditsib.geo_zones.use_cases.GeoZoneCases;
import ru.sberbank.ditsib.geo_zones.use_cases.model.GeoZone;

import java.util.Map;

/**
 * Реализация распознавания документа с геозонами.
 */
@Component
@RequiredArgsConstructor
@Transactional
class GeoZoneImporter implements DataImporter<GeoZoneFileDTO> {

    /**
     * Замены символов.
     */
    private static final Map<Character, Character> REPLACEMENTS = Map.of(
        '\u2005', ' '
    );

    private final GeoZoneCases geoZoneCases;

    @SneakyThrows
    @Override
    public void importData(GeoZoneFileDTO source, @NonNull Map<String, ?> parameters, @NonNull JwtAuthenticationToken jwtAuthenticationToken) {
        var name = source.getName();
        var parentName = source.getParentName();
        for (var entry : REPLACEMENTS.entrySet()) {
            name = name.replace(entry.getKey(), entry.getValue());
            parentName = parentName.replace(entry.getKey(), entry.getValue());
        }

        var geoZone = geoZoneCases.find(source.getCode()).orElseGet(GeoZone::new);
        geoZone.setCode(source.getCode());
        geoZone.setName(name);

        var parentGeoZone = geoZoneCases.find(source.getParentCode()).orElseGet(GeoZone::new);
        if (source.getParentCode() != null) {
            parentGeoZone.setName(parentName);
            parentGeoZone.setCode(source.getParentCode());
            var savedParent = saveGeoZone(parentGeoZone);
            geoZone.setParentId(savedParent.getId());
        }
        geoZone.setTimeZone(source.getTimeZone());
        saveGeoZone(geoZone);
    }

    /**
     * Сохранить геозону.
     *
     * @param geoZone зона для сохранения.
     * @return сохраненная зона.
     */
    private GeoZone saveGeoZone(GeoZone geoZone) {
        return geoZoneCases.save(geoZone);
    }
}
