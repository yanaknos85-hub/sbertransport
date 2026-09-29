package ru.sberbank.ditsib.geo_zones.web.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.utils.collections.MapUtils;
import ru.sberbank.ditsib.geo_zones.providers.geo_zone.dao.GeoZoneRepository;
import ru.sberbank.ditsib.geo_zones.providers.geo_zone.model.GeoZone;
import ru.sberbank.ditsib.geo_zones.web.http.dto.GeoZoneWithChildrenDto;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.messaging.messages.GeoZoneMessage;

import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("unused")
@UnitTest
@Isolated
@IsolatedTest
@Feature("app_platform_geo_zones")
@Transactional
@SpringBootTest(properties = {"spring.jpa.properties.hibernate.search.backend.directory.root=./target/index/${random.uuid}/"})
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
@DisplayName("Проверка контроллера геозон")
@MockitoBean(types = JwtDecoder.class)
@ActiveProfiles("test")
@Import({MapUtils.class})
class GeoZoneControllerTest {
    
    private static final String USER_ROLE = "GUEST";
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private GeoZoneRepository geoZoneRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @MockitoBean
    private AuthorizationManager<?> authorizationManager;

    @MockitoBean(name = "geoZoneSource")
    private OutputBridge geoZoneSource;

    @MockitoBean(name = "geoZoneSourceSsl")
    private OutputBridge geoZoneSourceSsl;

    @BeforeEach
    void setup() {
        AuthorizeUtils.authorize(authorizationManager, "GUEST");
    }
    
    @AfterAll
    static void deleteIndex() {
        try {
            FileUtils.deleteDirectory(new File("./target/index"));
        } catch (Exception ignore) {
        }
    }
    
    @Test
    @DisplayName("Добавление")
    void test_add() throws Exception {
        var newData = "{" +
                      "\"code\": 10," +
                      "\"name\": \"name\"" +
                      "}";
        
        mockMvc.perform(post("/").with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE)))
                .contentType(MediaType.APPLICATION_JSON)
                                 .content(newData))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.code").value(geoZoneRepository.findAll().getFirst().getCode()))
               .andExpect(jsonPath("$.name").value(geoZoneRepository.findAll().getFirst().getName()))
               .andExpect(jsonPath("$.id").value(geoZoneRepository.findAll().getFirst().getId().toString()));
        
        var database = geoZoneRepository.findAll().getFirst();
        
        assertThat(database.getId()).isNotNull();
        assertThat(database.getName()).isEqualTo("name");
        assertThat(database.getCode()).isEqualTo(10 + "");
        assertThat(database.getParent()).isNull();

        var messageCaptor = ArgumentCaptor.forClass(GeoZoneMessage.class);
        verify(geoZoneSource, times(2)).send(messageCaptor.capture());

        var message = new LinkedList<>(messageCaptor.getAllValues()).getLast();
        assertThat(message.getId()).isEqualTo(database.getId());
        assertThat(message.getName()).isEqualTo(database.getName());
        assertThat(message.getCode()).isEqualTo(database.getCode());
        assertThat(message.getParentId()).isNull();
        assertThat(message.isDeleted()).isFalse();
    }
    
    @Test
    @DisplayName("Добавление. Конфликт. Код")
    void test_add_conflict_code() throws Exception {
        var geoZone = new GeoZone();
        
        geoZone.setCode(10 + "");
        geoZone.setName("Geo-zone");
        
        geoZoneRepository.save(geoZone);
        
        var newData = "{" +
                      "\"code\": 10," +
                      "\"name\": \"name\"" +
                      "}";
        
        mockMvc.perform(post("/").with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))).contentType(MediaType.APPLICATION_JSON)
                                 .content(newData))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.entity.name").value("GeoZone"))
               .andExpect(jsonPath("$.problems.length()").value(1))
               .andExpect(jsonPath("$.problems[0].field").value("code"))
               .andExpect(jsonPath("$.problems[0].value").value(10))
               .andExpect(jsonPath("$.problems[0].constraints.length()").value(0));
    }
    
    @Test
    @DisplayName("Добавление. Конфликт. Название")
    void test_add_conflict_name() throws Exception {
        var geoZone = new GeoZone();
        
        geoZone.setCode(100 + "");
        geoZone.setName("name");
        
        geoZoneRepository.save(geoZone);
        
        var newData = "{" +
                      "\"code\": 10," +
                      "\"name\": \"name\"" +
                      "}";
        
        mockMvc.perform(post("/").with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))).contentType(MediaType.APPLICATION_JSON)
                                 .content(newData))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.entity.name").value("GeoZone"))
               .andExpect(jsonPath("$.problems.length()").value(1))
               .andExpect(jsonPath("$.problems[0].field").value("name"))
               .andExpect(jsonPath("$.problems[0].value").value("name"))
               .andExpect(jsonPath("$.problems[0].constraints.length()").value(0));
    }
    
    @Test
    @DisplayName("Добавление. Конфликт. Нет имени")
    void test_add_noName() throws Exception {
        var newData = "{" +
                      "\"code\": 10" +
                      "}";
        
        mockMvc.perform(post("/").with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))).contentType(MediaType.APPLICATION_JSON)
                                 .content(newData))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").value("Bad Request"))
               .andExpect(jsonPath("$.problems[0].field").value("name"))
               .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotBlank"));
    }
    
    @Test
    @DisplayName("Добавление. Конфликт. Нет кода")
    void test_add_noCode() throws Exception {
        var newData = "{" +
                      "\"name\": \"name\"" +
                      "}";
        
        mockMvc.perform(post("/").with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))).contentType(MediaType.APPLICATION_JSON)
                                 .content(newData))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").value("Bad Request"))
               .andExpect(jsonPath("$.problems[0].field").value("code"))
               .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotNull"));
    }
    
    @Test
    @DisplayName("Добавление с родителем")
    void test_add_parent() throws Exception {
        var geoZone = new GeoZone();
        
        geoZone.setCode(100 + "");
        geoZone.setName("name2");
        
        var geoZone1 = geoZoneRepository.save(geoZone);
        
        var newData = "{" +
                      "\"code\": 10," +
                      "\"name\": \"name\"," +
                      "\"parent_id\": \"" + geoZone1.getId() + "\"" +
                      "}";
        
        mockMvc.perform(post("/").with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))).contentType(MediaType.APPLICATION_JSON)
                                                    .content(newData))
                                  .andExpect(status().isOk())
                                  .andExpect(jsonPath("$.code").value(geoZoneRepository.findAll().get(1).getCode()))
                                  .andExpect(jsonPath("$.parent_id").value(
                                          geoZoneRepository.findAll().get(1).getParent().getId().toString()))
                                  .andExpect(jsonPath("$.name").value(geoZoneRepository.findAll().get(1).getName()))
                                  .andExpect(jsonPath("$.id").value(
                                          geoZoneRepository.findAll().get(1).getId().toString()))
                                  .andReturn();
    
        var database = geoZoneRepository.findAll().get(1);
        
        assertThat(database.getId()).isNotNull();
        assertThat(database.getName()).isEqualTo("name");
        assertThat(database.getCode()).isEqualTo(10 + "");
        assertThat(database.getParent()).isEqualTo(geoZoneRepository.findAll().getFirst());

        var messageCaptor = ArgumentCaptor.forClass(GeoZoneMessage.class);
        verify(geoZoneSourceSsl, times(3)).send(messageCaptor.capture());
        var message = new LinkedList<>(messageCaptor.getAllValues()).getLast();
        
        assertThat(message.getId()).isEqualTo(database.getId());
        assertThat(message.getName()).isEqualTo(database.getName());
        assertThat(message.getCode()).isEqualTo(database.getCode());
        assertThat(message.getParentId()).isEqualTo(database.getParent().getId());
        assertThat(message.isDeleted()).isFalse();
    }
    
    @Test
    @DisplayName("Добавление с родителем. Родителя не существует")
    void test_add_parent_noExists() throws Exception {
        UUID uuid = UUID.randomUUID();
        
        var newData = "{" +
                      "\"code\": 10," +
                      "\"name\": \"name\"," +
                      "\"parent_id\": \"" + uuid + "\"" +
                      "}";
        
        mockMvc.perform(post("/").with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))).contentType(MediaType.APPLICATION_JSON)
                                 .content(newData))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.entity.name").value("GeoZone"))
               .andExpect(jsonPath("$.entity.id").value(uuid.toString()))
               .andExpect(jsonPath("$.message").value("Data not found: Entity: GeoZone, ID: " + uuid));
    }
    
    @Test
    @DisplayName("Изменение")
    void test_edit() throws Exception {
        var geoZone = new GeoZone();
        
        geoZone.setCode(10 + "");
        geoZone.setName("name");
        
        geoZone = geoZoneRepository.save(geoZone);
        
        var newData = "{" +
                      "\"code\": 100," +
                      "\"name\": \"name2\"" +
                      "}";
        
        mockMvc.perform(put("/" + geoZone.getId()).with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))).contentType(MediaType.APPLICATION_JSON).content(newData))
               .andExpect(status().isOk());
        
        var database = geoZoneRepository.findAll().getFirst();
        
        assertThat(database.getId()).isEqualTo(geoZone.getId());
        assertThat(database.getName()).isEqualTo("name2");
        assertThat(database.getCode()).isEqualTo(100 + "");
        assertThat(database.getParent()).isNull();

        var messageCaptor = ArgumentCaptor.forClass(GeoZoneMessage.class);
        verify(geoZoneSourceSsl, times(3)).send(messageCaptor.capture());
        var message = new LinkedList<>(messageCaptor.getAllValues()).getLast();
        
        assertThat(message.getId()).isEqualTo(database.getId());
        assertThat(message.getName()).isEqualTo(database.getName());
        assertThat(message.getCode()).isEqualTo(database.getCode());
        assertThat(message.getParentId()).isNull();
        assertThat(message.isDeleted()).isFalse();
    }
    
    @Test
    @DisplayName("Изменение. Конфликт. Код")
    void test_edit_conflict_code() throws Exception {
        var geoZone = new GeoZone();
        
        geoZone.setCode(10 + "");
        geoZone.setName("name");
        
        geoZone = geoZoneRepository.save(geoZone);
        
        var geoZone2 = new GeoZone();
        
        geoZone2.setCode(100 + "");
        geoZone2.setName("name3");
        
        geoZoneRepository.save(geoZone2);
        
        var newData = "{" +
                      "\"code\": 100," +
                      "\"name\": \"name2\"" +
                      "}";
        
        var result = mockMvc.perform(put("/" + geoZone.getId()).with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))).contentType(MediaType.APPLICATION_JSON).content(newData))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.entity.name").value("GeoZone"))
               .andExpect(jsonPath("$.problems.length()").value(1))
               .andExpect(jsonPath("$.problems[0].field").value("code"))
               .andExpect(jsonPath("$.problems[0].value").value(100))
               .andExpect(jsonPath("$.problems[0].constraints.length()").value(0))
                .andReturn();
        assertThat(result).isNotNull();
    }
    
    @Test
    @DisplayName("Изменение. Конфликт. Название")
    void test_edit_conflict_name() throws Exception {
        var geoZone = new GeoZone();
        
        geoZone.setCode(10 + "");
        geoZone.setName("name");
        
        geoZone = geoZoneRepository.save(geoZone);
        
        var geoZone2 = new GeoZone();
        
        geoZone2.setCode(1000 + "");
        geoZone2.setName("name2");
        
        geoZoneRepository.save(geoZone2);
        
        var newData = "{" +
                      "\"code\": 100," +
                      "\"name\": \"name2\"" +
                      "}";
        
        var result = mockMvc.perform(put("/" + geoZone.getId()).with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))).contentType(MediaType.APPLICATION_JSON).content(newData))
               .andExpect(status().isConflict())
               .andExpect(jsonPath("$.entity.name").value("GeoZone"))
               .andExpect(jsonPath("$.problems.length()").value(1))
               .andExpect(jsonPath("$.problems[0].field").value("name"))
               .andExpect(jsonPath("$.problems[0].value").value("name2"))
               .andExpect(jsonPath("$.problems[0].constraints.length()").value(0))
               .andReturn();
        assertThat(result).isNotNull();
    }
    
    @Test
    @DisplayName("Изменение без имени")
    void test_edit_noName() throws Exception {
        var geoZone = new GeoZone();
        
        geoZone.setCode(10 + "");
        geoZone.setName("name");
        
        geoZone = geoZoneRepository.save(geoZone);
        
        var newData = "{" +
                      "\"code\": 100" +
                      "}";
        
        var result = mockMvc.perform(put("/" + geoZone.getId()).with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))).contentType(MediaType.APPLICATION_JSON).content(newData))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").value("Bad Request"))
               .andExpect(jsonPath("$.problems[0].field").value("name"))
               .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotBlank"))
               .andReturn();
        assertThat(result).isNotNull();
    }
    
    @Test
    @DisplayName("Изменение без кода")
    void test_edit_noCode() throws Exception {
        var geoZone = new GeoZone();
        
        geoZone.setCode(10 + "");
        geoZone.setName("name");
        
        geoZone = geoZoneRepository.save(geoZone);
        
        var newData = "{" +
                      "\"name\": \"name\"" +
                      "}";
        
        var result = mockMvc.perform(put("/" + geoZone.getId()).with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))).contentType(MediaType.APPLICATION_JSON).content(newData))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message").value("Bad Request"))
               .andExpect(jsonPath("$.problems[0].field").value("code"))
               .andExpect(jsonPath("$.problems[0].constraints[0].type").value("NotNull"))
               .andReturn();
        assertThat(result).isNotNull();
    }
    
    @Test
    @DisplayName("Изменение родителя. Родителя нет")
    void test_edit_parent_noExists() throws Exception {
        var getZoneParent1 = new GeoZone();
        getZoneParent1.setCode(101 + "");
        getZoneParent1.setName("name1");
        
        getZoneParent1 = geoZoneRepository.save(getZoneParent1);
        
        var geoZone = new GeoZone();
        
        geoZone.setCode(10 + "");
        geoZone.setName("name");
        geoZone.setParent(getZoneParent1);
        
        geoZone = geoZoneRepository.save(geoZone);
        
        UUID uuid = UUID.randomUUID();
        var newData = "{" +
                      "\"code\": 100," +
                      "\"name\": \"name2\"," +
                      "\"parent_id\": \"" + uuid + "\"" +
                      "}";
        
        var result = mockMvc.perform(put("/" + geoZone.getId()).with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))).contentType(MediaType.APPLICATION_JSON).content(newData))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.entity.name").value("GeoZone"))
               .andExpect(jsonPath("$.entity.id").value(uuid.toString()))
               .andExpect(jsonPath("$.message").value("Data not found: Entity: GeoZone, ID: " + uuid))
               .andReturn();
        assertThat(result).isNotNull();
    }
    
    @Test
    @DisplayName("Удаление")
    void test_delete() throws Exception {
        var geoZone = new GeoZone();
        
        geoZone.setCode(10 + "");
        geoZone.setName("name");
        
        geoZone = geoZoneRepository.save(geoZone);
        
        assertThat(geoZoneRepository.count()).isEqualTo(1);
        
        mockMvc.perform(delete("/" + geoZone.getId()).with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))))
               .andExpect(status().isInternalServerError());
        
        assertThat(geoZoneRepository.findAll()).hasSize(1);
    }
    
    @Test
    @DisplayName("Удаление несуществующего")
    void test_delete_unexists() throws Exception {
        var uuid = UUID.randomUUID();
        mockMvc.perform(delete("/" + uuid).with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))))
               .andExpect(status().isInternalServerError());
    }
    
    @Test
    @DisplayName("Получение")
    void test_get() throws Exception {
        var parent = new GeoZone();
        
        parent.setCode(10 + "");
        parent.setName("name");
        
        parent = geoZoneRepository.save(parent);
        
        var geoZone = new GeoZone();
        
        geoZone.setCode(10 + "");
        geoZone.setName("name");
        geoZone.setParent(parent);
        
        geoZone = geoZoneRepository.save(geoZone);
        
        var result = mockMvc.perform(get("/" + geoZone.getId()).with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(geoZone.getId().toString()))
               .andExpect(jsonPath("$.name").value(geoZone.getName()))
               .andExpect(jsonPath("$.code").value(geoZone.getCode()))
               .andExpect(jsonPath("$.parent_id").value(geoZone.getParent().getId().toString()))
               .andReturn();
        assertThat(result).isNotNull();
    }
    
    @Test
    @DisplayName("Получение несуществующего")
    void test_get_unexists() throws Exception {
        var uuid = UUID.randomUUID();
        mockMvc.perform(get("/" + uuid).with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))))
               .andExpect(status().isNotFound())
               .andExpect(jsonPath("$.entity.name").value("GeoZone"))
               .andExpect(jsonPath("$.entity.id").value(uuid.toString()))
               .andExpect(jsonPath("$.message").value("Data not found: Entity: GeoZone, ID: " + uuid));
    }
    
    @Test
    @DisplayName("Получение всех")
    void test_getAll() throws Exception {
        List<GeoZone> firstLevelZones = new ArrayList<>();
        var count = 5;
        
        for (var i = 0; i < count; i++) {
            var geoZone = new GeoZone();
            
            geoZone.setCode(i + "");
            geoZone.setName("name " + i);
            
            firstLevelZones.add(geoZoneRepository.save(geoZone));
        }
        List<GeoZone> secondLevelZones = new ArrayList<>();
        for (var i = 0; i < count * 2; i++) {
            var geoZone = new GeoZone();
            
            geoZone.setCode(count + i + "");
            geoZone.setName("name " + (count + i));
            geoZone.setParent(firstLevelZones.get(i / 2));
            secondLevelZones.add(geoZoneRepository.save(geoZone));
        }
        
        for (var i = 0; i < secondLevelZones.size(); i++) {
            var geoZone = new GeoZone();
            
            geoZone.setCode(3 * count + i + "");
            geoZone.setName("name " + (3 * count + i));
            geoZone.setParent(secondLevelZones.get(i));
            geoZoneRepository.save(geoZone);
        }
        
        
        var results = mockMvc.perform(get("/").with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))))
                             .andExpect(status().isOk())
                             .andExpect(jsonPath("$.length()").value(count * 5));
    
        for (var i = 0; i < count * 5; i++) {
            results
                    .andExpect(jsonPath("$[" + i + "].id").value(geoZoneRepository.findAll().get(i).getId().toString()))
                    .andExpect(jsonPath("$[" + i + "].code").value(geoZoneRepository.findAll().get(i).getCode()))
                    .andExpect(jsonPath("$[" + i + "].name").value(geoZoneRepository.findAll().get(i).getName()));
        }
        
    }
    
    
    @Test
    @DisplayName("Получение всех в виде дерева")
    void test_getAllTree() throws Exception {
        List<GeoZone> firstLevelZones = new ArrayList<>();
        var count = 5;
        
        for (var i = 0; i < count; i++) {
            var geoZone = new GeoZone();
            
            geoZone.setCode(i + "");
            geoZone.setName("name " + i);
            
            firstLevelZones.add(geoZoneRepository.save(geoZone));
        }
        List<GeoZone> secondLevelZones = new ArrayList<>();
        for (var i = 0; i < count * 2; i++) {
            var geoZone = new GeoZone();
            
            geoZone.setCode(count + i + "");
            geoZone.setName("name " + (count + i));
            geoZone.setParent(firstLevelZones.get(i / 2));
            secondLevelZones.add(geoZoneRepository.save(geoZone));
        }
        
        for (var i = 0; i < secondLevelZones.size(); i++) {
            var geoZone = new GeoZone();
            
            geoZone.setCode(3 * count + i + "");
            geoZone.setName("name " + (3 * count + i));
            geoZone.setParent(secondLevelZones.get(i));
            geoZoneRepository.save(geoZone);
        }
        
        
        var results = mockMvc.perform(get("/roots").with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))))
                             .andExpect(status().isOk())
                             .andExpect(jsonPath("$.length()").value(firstLevelZones.size()));
        List<GeoZoneWithChildrenDto> geoZoneDtos =
                objectMapper.readValue(results.andReturn().getResponse().getContentAsString(),
                                       new TypeReference<>() {
                                       });
        
        for (GeoZoneWithChildrenDto firstLevelZone : geoZoneDtos) {
            List<GeoZoneWithChildrenDto> children = firstLevelZone.getChildren();
            assertEquals(2, children.size());
            for (GeoZoneWithChildrenDto child : children) {
                assertEquals(1, child.getChildren().size());
            }
        }
    }
    
    @Test
    @DisplayName("Поиск")
    void test_search() throws Exception {
        var regionZone = new GeoZone();
        regionZone.setCode(10 + "");
        regionZone.setName("Москва и Московская область");
        
        regionZone = geoZoneRepository.save(regionZone);

        var districtZone = new GeoZone();
        districtZone.setCode(11 + "");
        districtZone.setName("Московская область");
        districtZone.setParent(regionZone);

        districtZone = geoZoneRepository.save(districtZone);

        var cityZone = new GeoZone();
        cityZone.setCode(12 + "");
        cityZone.setName("город Москва");
        cityZone.setParent(districtZone);
        
        cityZone = geoZoneRepository.save(cityZone);
        
        var streetZone = new GeoZone();
        streetZone.setCode(13 + "");
        streetZone.setName("улица Белокаменная");
        streetZone.setParent(cityZone);
        
        streetZone = geoZoneRepository.save(streetZone);
        
        var houseZone = new GeoZone();
        houseZone.setCode(14 + "");
        houseZone.setName("дом 8");
        houseZone.setParent(streetZone);
        
        houseZone = geoZoneRepository.save(houseZone);
        
        var regionZone2 = new GeoZone();
        regionZone2.setCode(21 + "");
        regionZone2.setName("Иваново и Ивановская область");
        
        regionZone2 = geoZoneRepository.save(regionZone2);

        var districtZone2 = new GeoZone();
        districtZone2.setCode(22 + "");
        districtZone2.setName("Ивановский округ");
        districtZone2.setParent(regionZone2);

        districtZone2 = geoZoneRepository.save(districtZone2);

        var cityZone2 = new GeoZone();
        cityZone2.setCode(23 + "");
        cityZone2.setName("город Иваново");
        cityZone2.setParent(districtZone2);
        
        cityZone2 = geoZoneRepository.save(cityZone2);
        
        var streetZone2 = new GeoZone();
        streetZone2.setCode(24 + "");
        streetZone2.setName("улица Белокаменная");
        streetZone2.setParent(cityZone2);
        
        streetZone2 = geoZoneRepository.save(streetZone2);
        
        var houseZone2 = new GeoZone();
        houseZone2.setCode(25 + "");
        houseZone2.setName("дом 8");
        houseZone2.setParent(streetZone2);
        
        geoZoneRepository.save(houseZone2);
        
        mockMvc.perform(post("/search").with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))).header("Content-Type", "application/json")
                                       .content("{" +
                                                "\"region\": \"Московская\"," +
                                                "\"district\":\"Московская\"," +
                                                "\"city\": \"Москва\"," +
                                                "\"street\": \"Белокаменная\"," +
                                                "\"house\": \"8\"" +
                                                "}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(houseZone.getId().toString()))
               .andExpect(jsonPath("$.code").value(houseZone.getCode()))
               .andExpect(jsonPath("$.name").value(houseZone.getName()));
        
        mockMvc.perform(post("/search").with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))).header("Content-Type", "application/json")
                                       .content("{" +
                                                "\"region\": \"Московская\"," +
                                                "\"city\": \"Москва\"," +
                                                "\"street\": \"Белокаменная\"" +
                                                "}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(streetZone.getId().toString()))
               .andExpect(jsonPath("$.code").value(streetZone.getCode()))
               .andExpect(jsonPath("$.name").value(streetZone.getName()));
        
        mockMvc.perform(post("/search").with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))).header("Content-Type", "application/json")
                                       .content("{" +
                                                "\"region\": \"Ивановская\"," +
                                                "\"city\": \"Иваново\"," +
                                                "\"street\": \"Белокаменная\"" +
                                                "}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(streetZone2.getId().toString()))
               .andExpect(jsonPath("$.code").value(streetZone2.getCode()))
               .andExpect(jsonPath("$.name").value(streetZone2.getName()));
        
        mockMvc.perform(post("/search").with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))).header("Content-Type", "application/json")
                                       .content("{" +
                                                "\"region\": \"Ивановская\"," +
                                                "\"city\": \"Иваново\"" +
                                                "}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(cityZone2.getId().toString()))
               .andExpect(jsonPath("$.code").value(cityZone2.getCode()))
               .andExpect(jsonPath("$.name").value(cityZone2.getName()));
    
        mockMvc.perform(post("/search").with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))).header("Content-Type", "application/json")
                                       .content("{" +
                                                "\"region\": \"Ивановская\"" +
                                                "}"))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.id").value(regionZone2.getId().toString()))
               .andExpect(jsonPath("$.code").value(regionZone2.getCode()))
               .andExpect(jsonPath("$.name").value(regionZone2.getName()));
    }
    
    
    @Test
    @DisplayName("Поиск ветки")
    void test_searchBranch() throws Exception {
        var regionZone = new GeoZone();
        regionZone.setCode(10 + "");
        regionZone.setName("Московская область");
        
        regionZone = geoZoneRepository.save(regionZone);
        
        var cityZone = new GeoZone();
        cityZone.setCode(11 + "");
        cityZone.setName("город Москва");
        cityZone.setParent(regionZone);
        
        cityZone = geoZoneRepository.save(cityZone);
        
        var streetZone = new GeoZone();
        streetZone.setCode(12 + "");
        streetZone.setName("улица Белокаменная");
        streetZone.setParent(cityZone);
        
        streetZone = geoZoneRepository.save(streetZone);
        
        var houseZone = new GeoZone();
        houseZone.setCode(13 + "");
        houseZone.setName("дом 8");
        houseZone.setParent(streetZone);
        
        geoZoneRepository.save(houseZone);
        
        var regionZone2 = new GeoZone();
        regionZone2.setCode(21 + "");
        regionZone2.setName("Ивановская область");
        
        regionZone2 = geoZoneRepository.save(regionZone2);
        
        var cityZone2 = new GeoZone();
        cityZone2.setCode(22 + "");
        cityZone2.setName("город Иваново");
        cityZone2.setParent(regionZone2);
        
        cityZone2 = geoZoneRepository.save(cityZone2);
        
        var streetZone2 = new GeoZone();
        streetZone2.setCode(23 + "");
        streetZone2.setName("улица Белокаменная");
        streetZone2.setParent(cityZone2);
        
        streetZone2 = geoZoneRepository.save(streetZone2);
        
        var houseZone2 = new GeoZone();
        houseZone2.setCode(24 + "");
        houseZone2.setName("дом 8");
        houseZone2.setParent(streetZone2);
        
        geoZoneRepository.save(houseZone2);
        
        objectMapper.readValue(mockMvc.perform(post("/searchBranch").with(jwt().authorities(new SimpleGrantedAuthority(USER_ROLE))).header("Content-Type", "application/json")
                                                                                                  .content("{" +
                                                                                                           "\"region\": \"Московская\"," +
                                                                                                           "\"city\": \"Москва\"," +
                                                                                                           "\"street\": \"Белокаменная\"," +
                                                                                                           "\"house\": \"8\"" +
                                                                                                           "}"))
                                                                    .andExpect(status().isOk())
                                                                    .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8),
                                                             new TypeReference<>() {
                                                             });
    }
}