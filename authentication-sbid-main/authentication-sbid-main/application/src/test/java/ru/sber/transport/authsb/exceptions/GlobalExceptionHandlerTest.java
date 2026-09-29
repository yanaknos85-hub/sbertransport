package ru.sber.transport.authsb.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.authsb.controller.impl.AuthenticationControllerImpl;
import ru.sber.transport.authsb.services.ServiceController;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthenticationControllerImpl.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceController serviceController;

    @Test
    @DisplayName("Тест исключения")
    void test1() throws Exception {
        when(serviceController.getAuthToken("code", null))
                .thenThrow(new BadResponseException("Ошибка авторизации"));

        mockMvc.perform(post("/auth/login/")
                        .param("code", "code"))
                .andExpect(status().is4xxClientError())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.description").value("Ошибка авторизации"))
                .andExpect(jsonPath("$.message").value("Ошибка валидации"));
    }
}