package ru.sber.transport.roles.check.controller.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.roles.check.dto.UrlAllowDto;
import ru.sber.transport.roles.check.services.RoleProvider;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@IsolatedTest
@UnitTest
@Feature("lib_role_check")
@WebMvcTest(UrlEndpointAllowControllerImpl.class)
@ContextConfiguration(classes = UrlEndpointAllowControllerImpl.class)
@DisplayName("Проверка контроллера endpoint-ов")
class UrlEndpointAllowControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleProvider provider;

    @Test
    @WithMockUser
    @DisplayName("Получение")
    void test_get() throws Exception {
        var role = Instancio.create(String.class);
        var urls = Instancio.createList(String.class);

        when(provider.getUrls(role)).thenReturn(urls);

        mockMvc.perform(get("/allow/%s".formatted(role)))
                .andExpectAll(
                    status().isOk(),
                    content().json(new ObjectMapper().writeValueAsString(urls))
                );
    }

    @Test
    @DisplayName("Разрешение")
    @WithMockUser
    void test_allow() throws Exception {
        var urls = Instancio.ofList(UrlAllowDto.class)
            .set(Select.field(UrlAllowDto::getUrl), IntStream.range(0, 10).mapToObj(i -> "%s %s".formatted(Instancio.create(HttpMethod.class).name(), Instancio.create(String.class))).toList())
            .create();

        mockMvc.perform(put("/allow").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(urls)))
            .andExpectAll(
                status().isOk()
            );

        var clearedCaptor = ArgumentCaptor.forClass(String.class);
        verify(provider, times(urls.size())).clearRole(clearedCaptor.capture());

        assertThat(clearedCaptor.getAllValues()).hasSameElementsAs(urls.stream().map(UrlAllowDto::getRole).toList());

        var roleCaptor = ArgumentCaptor.forClass(String.class);
        var urlCaptor = ArgumentCaptor.forClass(String.class);
        var methodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        verify(provider, times((int) urls.stream().flatMap(u -> u.getUrl().stream()).count())).save(roleCaptor.capture(), methodCaptor.capture(), urlCaptor.capture());

        assertThat(roleCaptor.getAllValues()).hasSameElementsAs(urls.stream().map(UrlAllowDto::getRole).toList());
        assertThat(urlCaptor.getAllValues()).hasSameElementsAs(urls.stream().flatMap(u -> u.getUrl().stream()).map(u -> u.split(" ")[1]).toList());
        assertThat(methodCaptor.getAllValues()).hasSameElementsAs(urls.stream().flatMap(u -> u.getUrl().stream()).map(u -> HttpMethod.valueOf(u.split(" ")[0])).toList());
    }
}