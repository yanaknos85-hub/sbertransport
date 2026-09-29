package ru.sberbank.ditsib.transport.reports.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.reports.dao.DepartmentRepository;
import ru.sberbank.ditsib.transport.reports.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.reports.dao.SharedTest;
import ru.sberbank.ditsib.transport.reports.dto.DepartmentDTO;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@DisplayName("Проверка контроллера поиска подразделений")
@EmbeddedPostgres
@SpringBootTest
@MockitoBean(types = JwtDecoder.class)
class SearchFilterDataControllerTest extends SharedTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private OrganizationRepository organizationRepository;
    
    @Autowired
    private DepartmentRepository departmentRepository;
    
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    @SneakyThrows
    @BeforeEach
    public void setUp() {
        // необходимые подразделения
        organizationRepository.saveAndFlush(organization1);
        departmentRepository.saveAndFlush(dep1);
        departmentRepository.saveAndFlush(dep2);
        departmentRepository.saveAndFlush(dep3);
        departmentRepository.saveAndFlush(dep4);
        departmentRepository.saveAndFlush(dep5);
        departmentRepository.saveAndFlush(dep6);
        
        // лишнии подразделения - для уточноения тестов
        departmentRepository.saveAndFlush(departmentHead);
        departmentRepository.saveAndFlush(departmentLocal);
        departmentRepository.saveAndFlush(department1);
        departmentRepository.saveAndFlush(department2);
        departmentRepository.saveAndFlush(department3);
        departmentRepository.saveAndFlush(department5);
    }
    
    @AfterEach
    public void tearDown() {
        departmentRepository.deleteAll();
        organizationRepository.deleteAll();
    }
    
    @ParameterizedTest(name = "{index} - Уровень запрашиваемого подразделения - {0} ")
    @DisplayName("Проверка получения списка подраздалений по ID организации")
    @ValueSource(ints = { 1, 2, 3, 4, 5, 6 })
    void test_getDepartmentListLevel(int level) throws Exception {
        DepartmentDTO departmentDTO = DepartmentDTO.builder()
                                                   .departmentLevel(level)
                                                   .build();
        
        var request = objectMapper.writeValueAsString(departmentDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/department", organization1.getId())).content(request)
                                                                                                             .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                                                                             .contentType(
                                                                                                                     MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        List<String> actualList =
                objectMapper.readValue(response.andReturn().getResponse().getContentAsString(),
                                       new TypeReference<>() {
                                       });
        var size = actualList.size();
        assertTrue(size >= 1);
        if (size == 1) {
            assertEquals("level " + level, actualList.get(0));
        } else {
            assertTrue(actualList.contains("level " + level));
        }
    }
    
    @ParameterizedTest(name = "{index} - Уровень запрашиваемого подразделения - {0} ")
    @DisplayName("Проверка получения списка подраздалений по ID организации по 2ому подразделению")
    @ValueSource(ints = { 3, 4, 5, 6 })
    void test_getDepartmentListLevelByLevel2Departments(int level) throws Exception {
        DepartmentDTO departmentDTO = DepartmentDTO.builder()
                                                   .departmentLevel(level)
                                                   .department2(List.of("level 2"))
                                                   .build();
        
        var request = objectMapper.writeValueAsString(departmentDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/department", organization1.getId())).content(request)
                                                                                                             .with(jwt().jwt(builder -> builder
                                                                                                                     .jti(USER3_ID_STR)
                                                                                                                     .claim("roles", "ROLE_USER")))
                                                                                                             .contentType(
                                                                                                                     MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        List<String> actualList =
                objectMapper.readValue(response.andReturn().getResponse().getContentAsString(),
                                       new TypeReference<>() {
                                       });
        
        assertEquals(1, actualList.size());
        assertEquals("level " + level, actualList.get(0));
    }
    
    @ParameterizedTest(name = "{index} - Уровень запрашиваемого подразделения - {0} ")
    @DisplayName("Проверка получения списка подраздалений по ID организации по 2ому и 3ему подразделениям")
    @ValueSource(ints = { 4, 5, 6 })
    void test_getDepartmentListLevelByLevel2And3Departments(int level) throws Exception {
        DepartmentDTO departmentDTO = DepartmentDTO.builder()
                                                   .departmentLevel(level)
                                                   .department2(List.of("level 2"))
                                                   .department3(List.of("level 3"))
                                                   .build();
        
        var request = objectMapper.writeValueAsString(departmentDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/department", organization1.getId()))
                                                         .content(request)
                                                         .with(jwt().jwt(builder -> builder.jti(USER3_ID_STR).claim("roles", "ROLE_USER")))
                                                         .contentType(
                                                                 MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        List<String> actualList =
                objectMapper.readValue(response.andReturn().getResponse().getContentAsString(),
                                       new TypeReference<>() {
                                       });
        
        assertEquals(1, actualList.size());
        assertEquals("level " + level, actualList.get(0));
    }
    
    @ParameterizedTest(name = "{index} - Уровень запрашиваемого подразделения - {0} ")
    @DisplayName("Проверка получения списка подраздалений по ID организации по 2ому, 3ему и 4ому подразделениям")
    @ValueSource(ints = { 5, 6 })
    void test_getDepartmentListLevelByLevel2And3And4Departments(int level) throws Exception {
        DepartmentDTO departmentDTO = DepartmentDTO.builder()
                                                   .departmentLevel(level)
                                                   .department2(List.of("level 2"))
                                                   .department3(List.of("level 3"))
                                                   .department4(List.of("level 4"))
                                                   .build();
        
        var request = objectMapper.writeValueAsString(departmentDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/department", organization1.getId())).content(request)
                                                                                                             .with(jwt().jwt(builder -> builder
                                                                                                                     .jti(USER3_ID_STR)
                                                                                                                     .claim("roles", "ROLE_USER")))
                                                                                                             .contentType(
                                                                                                                     MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        List<String> actualList =
                objectMapper.readValue(response.andReturn().getResponse().getContentAsString(),
                                       new TypeReference<>() {
                                       });
        
        assertEquals(1, actualList.size());
        assertEquals("level " + level, actualList.get(0));
    }
    
    @ParameterizedTest(name = "{index} - Уровень запрашиваемого подразделения - {0} ")
    @DisplayName("Проверка получения списка подраздалений по ID организации по 2ому, 3ему, 4ому и 5ому подразделениям")
    @ValueSource(ints = { 6 })
    void test_getDepartmentListLevelByLevel2And3And4And5Departments(int level) throws Exception {
        DepartmentDTO departmentDTO = DepartmentDTO.builder()
                                                   .departmentLevel(level)
                                                   .department2(List.of("level 2"))
                                                   .department3(List.of("level 3"))
                                                   .department4(List.of("level 4"))
                                                   .department5(List.of("level 5"))
                                                   .build();
        
        var request = objectMapper.writeValueAsString(departmentDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/department", organization1.getId())).content(request)
                                                                                                             .with(jwt().jwt(builder -> builder
                                                                                                                     .jti(USER3_ID_STR)
                                                                                                                     .claim("roles", "ROLE_USER")))
                                                                                                             .contentType(
                                                                                                                     MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        List<String> actualList =
                objectMapper.readValue(response.andReturn().getResponse().getContentAsString(),
                                       new TypeReference<>() {
                                       });
        
        assertEquals(1, actualList.size());
        assertEquals("level " + level, actualList.get(0));
    }
    
    @ParameterizedTest(name = "{index} - Уровень запрашиваемого подразделения - {0} ")
    @DisplayName("Проверка получения списка подраздалений по ID организации по 2ому, 4ому и 5ому подразделениям")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    @ValueSource(ints = { 6 })
    void test_getDepartmentListLevelByLevel2And4And5Departments(int level) throws Exception {
        DepartmentDTO departmentDTO = DepartmentDTO.builder()
                                                   .departmentLevel(level)
                                                   .department2(List.of("level 2"))
                                                   .department4(List.of("level 4"))
                                                   .department5(List.of("level 5"))
                                                   .build();
        
        var request = objectMapper.writeValueAsString(departmentDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/department", organization1.getId())).content(request)
                                                                                                             .with(jwt().jwt(builder -> builder
                                                                                                                     .jti(USER3_ID_STR)
                                                                                                                     .claim("roles", "ROLE_USER")))
                                                                                                             .contentType(
                                                                                                                     MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        List<String> actualList =
                objectMapper.readValue(response.andReturn().getResponse().getContentAsString(),
                                       new TypeReference<>() {
                                       });
        
        assertEquals(1, actualList.size());
        assertEquals("level " + level, actualList.get(0));
    }
    
    @ParameterizedTest(name = "{index} - Уровень запрашиваемого подразделения - {0} ")
    @DisplayName("Проверка получения списка подраздалений по ID организации по 3ему и 4ому подразделениям")
    @ValueSource(ints = { 5, 6 })
    void test_getDepartmentListLevelByLevel3And4Departments(int level) throws Exception {
        DepartmentDTO departmentDTO = DepartmentDTO.builder()
                                                   .departmentLevel(level)
                                                   .department3(List.of("level 3"))
                                                   .department4(List.of("level 4"))
                                                   .build();
        
        var request = objectMapper.writeValueAsString(departmentDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/department", organization1.getId())).content(request)
                                                                                                             .with(jwt().jwt(builder -> builder
                                                                                                                     .jti(USER3_ID_STR)
                                                                                                                     .claim("roles", "ROLE_USER")))
                                                                                                             .contentType(
                                                                                                                     MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        List<String> actualList =
                objectMapper.readValue(response.andReturn().getResponse().getContentAsString(),
                                       new TypeReference<>() {
                                       });
        
        assertEquals(1, actualList.size());
        assertEquals("level " + level, actualList.get(0));
    }
    
    @Test
    @DisplayName("Проверка получения пустого списка подраздалений по ID организации по 4ему и 3ому подразделениям")
    void test_getDepartmentListLevelByIncorrectDepartmentNames() throws Exception {
        DepartmentDTO departmentDTO = DepartmentDTO.builder()
                                                   .departmentLevel(5)
                                                   .department3(List.of("level 4"))
                                                   .department4(List.of("level 3"))
                                                   .build();
        
        var request = objectMapper.writeValueAsString(departmentDTO);
        
        ResultActions response = mockMvc.perform(post(String.format("/%s/department", organization1.getId())).content(request)
                                                                                                             .with(jwt().jwt(builder -> builder
                                                                                                                     .jti(USER3_ID_STR)
                                                                                                                     .claim("roles", "ROLE_USER")))
                                                                                                             .contentType(
                                                                                                                     MediaType.APPLICATION_JSON))
                                        .andExpect(status().isOk());
        List<String> actualList =
                objectMapper.readValue(response.andReturn().getResponse().getContentAsString(),
                                       new TypeReference<>() {
                                       });
        
        assertTrue(actualList.isEmpty());
    }
}