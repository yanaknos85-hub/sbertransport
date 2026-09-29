package ru.sberbank.ditsib.geo_zones.use_cases.impl.resolver;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.file_works.exceptions.ParsingFailedException;
import ru.sber.transport.file_works.importer.DataImporter;
import ru.sberbank.ditsib.geo_zones.export.dto.GeoZoneFileDTO;
import ru.sberbank.ditsib.geo_zones.use_cases.GeoZoneCases;
import ru.sberbank.ditsib.geo_zones.use_cases.model.GeoZone;

import javax.naming.OperationNotSupportedException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@UnitTest
@Isolated
@IsolatedTest
@Feature("app_platform_geo_zones")
@DisplayName("Проверка импортера")
class GeoZoneImporterTest {

    private final GeoZoneCases geoZoneCases = mock(GeoZoneCases.class);

    private final DataImporter<GeoZoneFileDTO> importer = new GeoZoneImporter(geoZoneCases);

    @Test
    @DisplayName("Проверка импорта ")
    void test_import() throws OperationNotSupportedException, ParsingFailedException {
        var source = Instancio.createList(GeoZoneFileDTO.class);

        when(geoZoneCases.find(any())).thenAnswer(inv -> Optional.of(new GeoZone()));
        when(geoZoneCases.save(any())).thenAnswer(inv -> {
            var geoZone = inv.getArgument(0, GeoZone.class);
            geoZone.setId(UUID.randomUUID());
            return geoZone;
        });

        for (var item : source) {
            importer.importData(item, Map.of(), new JwtAuthenticationToken(Jwt.withTokenValue("value").header("algo", "none").jti(UUID.randomUUID().toString()).build()));
        }

        var geoZoneCaptor = ArgumentCaptor.forClass(GeoZone.class);

        verify(geoZoneCases, times(source.size() * 2)).save(geoZoneCaptor.capture());

        assertThat(geoZoneCaptor.getAllValues()).hasSize(source.size() * 2); // С родителями

        for (var i = 0; i < source.size(); i++) {
            var expected = source.get(i);
            var actual = geoZoneCaptor.getAllValues().get(i * 2 + 1);

            assertThat(actual.getCode()).isEqualTo(expected.getCode());
            assertThat(actual.getName()).isEqualTo(expected.getName());
            assertThat(actual.getTimeZone()).isEqualTo(expected.getTimeZone());
        }
    }

}