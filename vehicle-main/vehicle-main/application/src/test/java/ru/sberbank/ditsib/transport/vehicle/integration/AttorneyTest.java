package ru.sberbank.ditsib.transport.vehicle.integration;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.JUnitException;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.ResultMatcher;
import ru.sber.transport.authorization.test.AuthorizeUtils;
import ru.sberbank.ditsib.transport.vehicle.BaseIntegrationTest;
import ru.sberbank.ditsib.transport.vehicle.dto.attorney.AttorneyCreateDto;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.sberbank.ditsib.transport.vehicle.constants.Role.ROLE_ADMIN_CORP_CLIENT;


class AttorneyTest extends BaseIntegrationTest {

    private static final String ROOT_PATH = "/attorney";
    
    @BeforeEach
    void beforeEach() {
        Mockito.reset(manager);
        AuthorizeUtils.authorize(manager, ROLE_ADMIN_CORP_CLIENT.name());
    }

    @Test
    @DisplayName("Проверка получения списком")
    @Sql("/scripts/basic_corp_structure.sql")
    @Sql("/scripts/attorney_find_all_test.sql")
    @SneakyThrows
    void getAllTest() {
        mockMvc.perform(post(ROOT_PATH + "/all")
                .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                        .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id",
                                    containsInAnyOrder("c2f9c3ee-492f-49cf-95c6-1fbb9d33312e",
                                                       "7f07dfd9-8b6d-4b95-930d-9011047d2d36",
                                                       "943018d7-9d35-4004-b78f-581b701311ed")));

        mockMvc.perform(post(ROOT_PATH + "/all")
                .with(jwt().jwt(builder -> builder.jti("15BDC75D-533B-44BB-9539-3DDC8693F3D3"))
                        .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id",
                                    containsInAnyOrder("377246e6-66b4-4b37-a3da-e0d0838dbd7c",
                                                       "fd3e90ef-cefb-40fa-8087-60369341466a")));

    }

    @Test
    @DisplayName("Доверeнность должна быть добавлена")
    @Sql("/scripts/basic_corp_structure.sql")
    @SneakyThrows
    void addAttorney() {
        var response = mockMvc.perform(post(ROOT_PATH)
                .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                        .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "telemechanicId": "7dd56ea0-fa38-400d-93a6-2a4ef4b7df70",
                            "attorneyId": "523aa29d-8e57-4553-88ed-a475756bd027",
                            "issueDate": 1714146841601,
                            "expiryDate": 1714146848897,
                            "creationSystem": "Notorious B.I.G."
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        
        var attorneyId = objectMapper.readValue(response, AttorneyCreateDto.class).attorneyId();
        
        var attorney = attorneyRepository.findByAttorneyId(attorneyId)
                .orElseThrow(() -> new JUnitException("Attorney not found"));
    }


    @Test
    @DisplayName("Доверенность должна быть изменена")
    @Sql("/scripts/basic_corp_structure.sql")
    @Sql("/scripts/attorney_find_all_test.sql")
    @SneakyThrows
    void editAttorney() {
        var response = mockMvc.perform(patch(ROOT_PATH + "/7F07DFD9-8B6D-4B95-930D-9011047D2D36")
                        .with(jwt().jwt(builder -> builder.jti("3cd35c19-fd39-413c-99a0-30f35bd642a8"))
                                .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "attorneyId": "782F59D3-2D21-4A75-A8F6-15EE22E59985",
                            "issueDate": 1714146841601,
                            "expiryDate": 1714146848897,
                            "creationSystem": "Notorious B.I.G."
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attorneyId").value("782f59d3-2d21-4a75-a8f6-15ee22e59985"))
                              .andReturn()
                              .getResponse()
                              .getContentAsString(StandardCharsets.UTF_8);
        
        var attorneyId = objectMapper.readValue(response, AttorneyCreateDto.class).attorneyId();
        
        var attorney = attorneyRepository.findByAttorneyId(attorneyId)
                                         .orElseThrow(() -> new JUnitException("Attorney not found"));
    }
    
    @SneakyThrows
    @MethodSource
    @ParameterizedTest(name = "{0}")
    @Sql("/scripts/basic_corp_structure.sql")
    @Sql("/scripts/attorney_find_all_test.sql")
    void getAttorneyByTelemechanicId(String testCase, String employeeId, List<ResultMatcher> matchers) {
        var result = mockMvc.perform(get(ROOT_PATH)
                                .with(jwt().jwt(builder -> builder.jti(employeeId))
                                              .authorities(new SimpleGrantedAuthority(ROLE_ADMIN_CORP_CLIENT.name()))));
               
        for (var matcher : matchers) {
            result.andExpect(matcher);
        }
    }
    
    static Stream<Arguments> getAttorneyByTelemechanicId() {
        return Stream.of(
                Arguments.of("Поиск доверенности",
                "3cd35c19-fd39-413c-99a0-30f35bd642a8",
                List.of(status().isOk(),
                        jsonPath("$.id", equalTo("c2f9c3ee-492f-49cf-95c6-1fbb9d33312e")),
                        jsonPath("$.telemechanic.id", equalTo("3cd35c19-fd39-413c-99a0-30f35bd642a8")),
                        jsonPath("$.expiryDate", equalTo(1715421660000L)))
                            ),
                Arguments.of("Ошибка поиска доверенности",
                        "7dd56ea0-fa38-400d-93a6-2a4ef4b7df70",
                List.of(status().isNotFound(),
                        jsonPath("$.message", equalTo("Доверенность у пользователя с id=7dd56ea0-fa38-400d-93a6-2a4ef4b7df70 не найдена или " +
                                                       "просрочена!")))
                            )
                        );
    }

}
