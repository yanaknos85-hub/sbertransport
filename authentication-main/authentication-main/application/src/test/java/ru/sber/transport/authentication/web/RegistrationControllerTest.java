package ru.sber.transport.authentication.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.AbstractContextedTest;
import ru.sber.transport.authentication.business.dto.Scope;
import ru.sber.transport.authentication.business.providers.AccountProvider;
import ru.sber.transport.authentication.web.exceptions.IncorrectScopeException;
import ru.sber.transport.authentication.web.exceptions.RegistrationDataConflictException;
import ru.sber.transport.authentication.web.model.RegistrationRequestDto;
import ru.sber.transport.authorization.service.BlackListService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@EmbeddedPostgres(liquibase = "src/main/resources/db/changelog-master.yml")
@Transactional
@DisplayName("Проверка регистрации")
@MockitoBean(types = BlackListService.class)
@AutoConfigureMockMvc
class RegistrationControllerTest extends AbstractContextedTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private AccountProvider accountProvider;

    @DisplayName("Проверка регистрации нового ТУЗ")
    @Test
    void test_registrationTA() throws Exception {
        var data = RegistrationRequestDto.builder()
                .login("login")
                .password("password")
                .email("test@test.ru")
                .scope(Scope.CONTRACTOR.name())
                .build();
        mockMvc.perform(post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "WEB_CORP")
                        .with(csrf())
                        .content(
                                mapper.writeValueAsString(data)
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").isNotEmpty());

        var account = accountProvider.get(data.login()).get();
        assertThat(account.getScope()).isEqualTo(Scope.CONTRACTOR);
        assertThat(account.getEmail()).isEqualTo(data.email());
        assertThat(account.isActive()).isTrue();
        assertThat(account.isTransferPassword()).isFalse();

        mockMvc.perform(get("/login").header("Authorization", "Basic %s:%s".formatted(data.login(), data.password())).header("User-Agent", "WEB_CORP").with(csrf()))
                .andExpect(status().isOk());
    }

    @DisplayName("Проверка конфликта при повторной регистрации")
    @Test
    void test_registrationConflictLoginTA() throws Exception {
        var data = RegistrationRequestDto.builder()
                .login("login")
                .password("password")
                .email("test@test.ru")
                .scope(Scope.CONTRACTOR.name())
                .build();
        mockMvc.perform(post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "WEB_CORP")
                        .with(csrf())
                        .content(
                                mapper.writeValueAsString(data)
                        ))
                .andExpect(status().isOk());
        data = RegistrationRequestDto.builder()
                .login("login")
                .password("password")
                .email("test@test.ru")
                .scope(Scope.CONTRACTOR.name())
                .build();
        mockMvc.perform(post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "WEB_CORP")
                        .with(csrf())
                        .content(
                                mapper.writeValueAsString(data)
                        ))
                .andExpect(status().isConflict()).andExpect(result -> assertInstanceOf(RegistrationDataConflictException.class, result.getResolvedException()));
    }

    @DisplayName("Некорректная область видимости")
    @ParameterizedTest
    @ValueSource(strings = {"AUTOSERVICE", "DRIVER", "EMPLOYEE", "USER", "NULL"})
    void test_registrationIncorrectScope(String scope) throws Exception {
        var data = RegistrationRequestDto.builder()
                .login("login")
                .password("password")
                .email("test@test.ru")
                .scope(scope)
                .build();
        mockMvc.perform(post("/registration")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("User-Agent", "WEB_CORP")
                        .with(csrf())
                        .content(
                                mapper.writeValueAsString(data)
                        ))
                .andExpect(status().isBadRequest()).andExpect(result -> assertInstanceOf(IncorrectScopeException.class, result.getResolvedException()));
    }


}
