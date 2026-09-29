package ru.sberbank.ditsib.geo_zones.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.util.Strings;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.file_works.services.UploadStates;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.utils.collections.MapUtils;
import ru.sberbank.ditsib.geo_zones.providers.geo_zone.dao.GeoZoneRepository;
import ru.sberbank.ditsib.geo_zones.providers.geo_zone.events.HibernateEventWiring;
import ru.sberbank.ditsib.geo_zones.providers.geo_zone.model.GeoZone;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("unused")
@UnitTest
@Isolated
@IsolatedTest
@Feature("app_platform_geo_zones")
@SpringBootTest(properties = {"export.tempDir=target/test/files", "spring.jpa.properties.hibernate.search.backend.directory.root=./target/index/${random.uuid}/"})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureMockMvc
@Transactional
@DisplayName("Экспорт геозон")
@MockitoBean(types = {JwtDecoder.class, HibernateEventWiring.class})
@ActiveProfiles("test")
@Import(MapUtils.class)
class GeoZoneExportTest {

    public static final String TEMP_DIR = "target/test/files";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GeoZoneRepository geoZoneRepository;

    @Autowired
    private UploadStates states;

    @MockitoBean
    private AuthorizationManager<?> authorizationManager;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(authorizationManager, "ROLE_GUEST");
    }

    @AfterEach
    void deleteTestFolder() throws IOException {
        FileUtils.deleteDirectory(new File(TEMP_DIR));
    }

    @Test
    @DisplayName("Экспорт")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void test_export() throws Exception {
        var count = 10;

        var firstLevelZones = IntStream.range(0, count)
                .mapToObj(i -> {
                    var geoZone = new GeoZone();

                    geoZone.setCode(i + "");
                    geoZone.setName("name " + i);

                    return geoZone;
                }).toList();
        geoZoneRepository.saveAllAndFlush(firstLevelZones);

        var secondLevelZones = IntStream.range(0, count * 2)
                .mapToObj(i -> {
                    var geoZone = new GeoZone();

                    geoZone.setCode(count + i + "");
                    geoZone.setName("name " + i);
                    geoZone.setParent(firstLevelZones.get(i / 2));

                    return geoZone;
                }).toList();
        geoZoneRepository.saveAllAndFlush(secondLevelZones);

        var thirdLevelZones = IntStream.range(0, secondLevelZones.size())
                .mapToObj(i -> {
                    var geoZone = new GeoZone();

                    geoZone.setCode(3 * count + i + "");
                    geoZone.setName("name " + (3 * count + i));
                    geoZone.setParent(secondLevelZones.get(i));

                    return geoZone;
                }).toList();
        geoZoneRepository.saveAllAndFlush(thirdLevelZones);

        geoZoneRepository.flush();

        var content = mockMvc.perform(get("/files/geozones").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_GUEST"))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse().getContentAsString();

        var response = new ObjectMapper().readValue(content, new TypeReference<Map<String, String>>() {
        });

        var file = response.get("result_url");
        final var responseData = new AtomicReference<MockHttpServletResponse>();
        await().timeout(Duration.ofSeconds(30))
                .pollDelay(Duration.ofSeconds(1))
                .until(() -> {
                    responseData.set(mockMvc.perform(get(file).with(jwt().authorities(new SimpleGrantedAuthority("ROLE_GUEST")))).andReturn().getResponse());
                    return !Objects.equals(responseData.get().getHeader(HttpHeaders.CONTENT_TYPE), "application/json");
                });

        var bytes = responseData.get().getContentAsByteArray();

        try (var bais = new ByteArrayInputStream(bytes);
             var excel = new XSSFWorkbook(bais)) {
            var sheet = excel.getSheet("Геозоны");

            assertThat(sheet).isNotNull();

            assertThat(sheet.getPhysicalNumberOfRows()).isEqualTo(count * 5 + 1); // 1 - заголовок

            var row = sheet.getRow(0);
            assertThat(row.getCell(0).getStringCellValue()).isEqualTo("Код");
            assertThat(row.getCell(1).getStringCellValue()).isEqualTo("Название");
            assertThat(row.getCell(2).getStringCellValue()).isEqualTo("Название родительской геозоны");
            assertThat(row.getCell(3).getStringCellValue()).isEqualTo("Код родительской геозоны");

            var expectedList = geoZoneRepository.findByParentId(null);

            for (int rowIndex = 1, index = 0; rowIndex < sheet.getPhysicalNumberOfRows(); rowIndex++, index++) {
                var actualRow = sheet.getRow(rowIndex);
                String code = actualRow.getCell(0).getStringCellValue();
                String name = actualRow.getCell(1).getStringCellValue();
                String parentName = actualRow.getCell(2).getStringCellValue();
                String parentCode = actualRow.getCell(3).getStringCellValue();
                assertFalse(Strings.isNullOrEmpty(code));
                GeoZone expected =
                        geoZoneRepository.findByCodeAndIdIsNot(code, UUID.randomUUID()).orElseThrow();
                GeoZone parentGeoZone =
                        Strings.isNullOrEmpty(parentCode) ? null :
                                geoZoneRepository.findByCodeAndIdIsNot(parentCode, UUID.randomUUID()).orElseThrow();

                assertEquals(expected.getCode(), code);
                assertEquals(expected.getName(), name);
                if (!Strings.isNullOrEmpty(parentCode)) {
                    assertNotNull(parentGeoZone);
                    assertEquals(parentGeoZone.getName(), parentName);
                }
            }
        }
    }
}
