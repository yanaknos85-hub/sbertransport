package ru.sberbank.ditsib.transport.reports.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sber.transport.messaging.kafka.test.KafkaTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.reports.dao.UserPropertiesRepository;
import ru.sberbank.ditsib.transport.reports.dao.UserControlRepository;
import ru.sberbank.ditsib.transport.reports.dao.UserPreferencesRepository;
import ru.sberbank.ditsib.transport.reports.dto.user.Control;
import ru.sberbank.ditsib.transport.reports.dto.user.Settings;
import ru.sberbank.ditsib.transport.reports.dto.user.UserPreferencesRequestDTO;
import ru.sberbank.ditsib.transport.reports.dto.user.UserPreferencesResponseDTO;
import ru.sberbank.ditsib.transport.reports.mappers.UserPreferencesMapperImpl;
import ru.sberbank.ditsib.transport.reports.model.user.UserControls;
import ru.sberbank.ditsib.transport.reports.model.user.UserPreferences;
import ru.sberbank.ditsib.transport.reports.model.user.UserProperty;
import ru.sberbank.ditsib.transport.reports.service.UserPreferencesService;

import java.util.*;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.ditsib.transport.reports.dao.SharedTest.ROLE_STR;
import static ru.sberbank.ditsib.transport.reports.dao.SharedTest.USER3_ID_STR;

@AutoConfigureMockMvc
@DisplayName("Проверка контроллера - Пользовательские настройки")
@EmbeddedPostgres
@SpringBootTest
@MockitoBean(types = JwtDecoder.class)
class UserPreferencesControllerTest extends KafkaTest {
    
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserPreferencesRepository userPreferencesRepository;
    @Autowired
    private UserControlRepository userControlRepository;
    @Autowired
    private UserPropertiesRepository userPropertiesRepository;
    @Autowired
    private UserPreferencesMapperImpl userPreferencesMapper;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private AuthorizationManager<?> roleCheckService;
    
    @BeforeEach
    void setupRoles() {
        AuthorizeUtils.authorize(roleCheckService);
    }
    
    @Test
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    @DisplayName("Положительный тест на GET-запрос")
    public void testGetPreferencesByUserId_Positive() throws Exception {
        UUID userId = UUID.fromString("5682bae3-d6e5-4aee-b764-aa5468949532");
        String nameForm = "registry_public";
        
        UserPreferences userPreferences = new UserPreferences();
        userPreferences.setUserId(userId);
        userPreferences.setNameForm(nameForm);
        UserPreferences savedUserPref = userPreferencesRepository.save(userPreferences);
        
        Control control = new Control();
        control.setTypeControl("table");
        control.setValue("request_public");
        UserControls userControls = userPreferencesMapper.toUserControls(control);
        userControls.setUserPreferencesId(savedUserPref);
        UserControls savedUserControl = userControlRepository.save(userControls);
        
        Settings setting1 = new Settings("column", "number_request", 1);
        Settings setting2 = new Settings("column", "mvz", 2);
        Settings setting3 = new Settings("column", "dezair_request", 3);
        List<UserProperty> userProperties =
                Stream.of(setting1, setting2, setting3).map(settings -> userPreferencesMapper.toUserProperty(settings)).toList();
        userProperties.forEach(prop -> prop.setUserControlsId(savedUserControl));
        userPropertiesRepository.saveAll(userProperties);
        control.setSettings(List.of(setting1, setting2, setting3));
        
        UserPreferencesResponseDTO responseDTO = new UserPreferencesResponseDTO();
        responseDTO.setUserID(userId);
        responseDTO.setNameForm(nameForm);
        responseDTO.setStatusCode(0);
        responseDTO.setStatusDescription("ошибок нет");
        responseDTO.setControls(Collections.singletonList(control));
        
        when(Mockito.mock(UserPreferencesService.class).getPreferencesByIdAndNameForm(userId, nameForm))
                .thenReturn(responseDTO);
        
        
        String expectedResponse = objectMapper.writeValueAsString(responseDTO);
        
        mockMvc.perform(get("/users/ui-preferences")
                                .param("userID", userId.toString())
                                .param("nameForm", nameForm)
                                .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().json(expectedResponse));
    }
    
    @Test
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    @DisplayName("Отрицательный тест на GET-запрос")
    public void testGetPreferencesByUserId_Negative() throws Exception {
        UUID userId = UUID.fromString("5682bae3-d6e5-4aee-b764-aa5468949532");
        String nameForm = "non_existent_form";
        
        UserPreferencesResponseDTO emptyResponseDto = new UserPreferencesResponseDTO();
        emptyResponseDto.setStatusCode(1);
        emptyResponseDto.setStatusDescription("данные для userID и nameForm отсутствуют");
        
        String expectedResponse = objectMapper.writeValueAsString(emptyResponseDto);
        
        mockMvc.perform(get("/users/ui-preferences")
                                .param("userID", userId.toString())
                                .param("nameForm", nameForm)
                                .accept(MediaType.APPLICATION_JSON))
               .andExpect(status().isOk())
               .andExpect(content().json(expectedResponse));
    }
    
    @Test
    @DisplayName("Тест на PUT-запрос с проверкой записи в БД")
    @WithMockUser(username = USER3_ID_STR, roles = ROLE_STR)
    @Transactional
    public void testUpdateUserPreferences_Put() throws Exception {
        UserPreferencesRequestDTO requestDTO = new UserPreferencesRequestDTO();
        requestDTO.setUserID(UUID.fromString("5682bae3-d6e5-4aee-b764-aa5468949532"));
        requestDTO.setNameForm("registry_public");
        
        Settings setting1 = new Settings("column", "number_request", 1);
        Settings setting2 = new Settings("column", "mvz", 2);
        Settings setting3 = new Settings("column", "dezair_request", 3);
        
        Control control = new Control("table", "request_public", Arrays.asList(setting1, setting2, setting3));
        requestDTO.setControls(Collections.singletonList(control));
        
        mockMvc.perform(put("/users/ui-preferences")
                                .contentType(APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDTO)))
               .andExpect(status().isOk());
        
        Optional<?> savedPreferences = userPreferencesRepository.findAByUserIdAndNameForm(
                UUID.fromString("5682bae3-d6e5-4aee-b764-aa5468949532"), "registry_public");
        
        assert savedPreferences.isPresent();
    }
}