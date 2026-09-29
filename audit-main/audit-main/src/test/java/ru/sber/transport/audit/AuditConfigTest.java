package ru.sber.transport.audit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.sber.transport.audit.fakeApp.DoNotStartThis;
import ru.sber.transport.audit.writer.AuditWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Проверка аудита")
@SpringBootTest(classes = {DoNotStartThis.class})
@TestPropertySource(properties = "logging.level.root=DEBUG")
@AutoConfigureMockMvc
class AuditConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuditWriter writer;

    @Test
    @DisplayName("Проверка аудита. Прямой контроллер, GET")
    void test_audit_direct_get() throws Exception {
        mockMvc.perform(get("/?data=test").header("x-real-ip", "127.0.0.1"))
                .andExpect(status().isOk());

        var sourceCaptor = ArgumentCaptor.forClass(String.class);
        var userCaptor = ArgumentCaptor.forClass(Authentication.class);
        var actionCaptor = ArgumentCaptor.forClass(String.class);
        var resultCaptor = ArgumentCaptor.forClass(Result.class);

        verify(writer).write(sourceCaptor.capture(), actionCaptor.capture(), any(), any(), userCaptor.capture(),
                resultCaptor.capture(), any(), any(), any(), any());

        assertThat(sourceCaptor.getValue()).isEqualTo("127.0.0.1");
        assertThat(userCaptor.getValue()).isNull();
        assertThat(actionCaptor.getValue()).isEqualTo("Get test");
        assertThat(resultCaptor.getValue()).isEqualTo(Result.SUCCESS);
    }

    @Test
    @DisplayName("Проверка аудита. Интерфейсный контроллер, POST")
    void test_audit_interfaced_post() throws Exception {
        mockMvc.perform(post("/interfaced/?data=test").header("x-forwarded-for", "127.0.0.1"))
                .andExpect(status().isOk());

        var sourceCaptor = ArgumentCaptor.forClass(String.class);
        var userCaptor = ArgumentCaptor.forClass(Authentication.class);
        var actionCaptor = ArgumentCaptor.forClass(String.class);
        var resultCaptor = ArgumentCaptor.forClass(Result.class);

        verify(writer).write(sourceCaptor.capture(), actionCaptor.capture(), any(), any(), userCaptor.capture(),
                resultCaptor.capture(), any(), any(), any(), any());

        assertThat(sourceCaptor.getValue()).isEqualTo("127.0.0.1");
        assertThat(userCaptor.getValue()).isNull();
        assertThat(actionCaptor.getValue()).isEqualTo("POST /interfaced/");
        assertThat(resultCaptor.getValue()).isEqualTo(Result.SUCCESS);
    }

    @Test
    @DisplayName("Проверка аудита. Прямой контроллер, DELETE")
    @WithMockUser(username = "test_user")
    void test_audit_direct_delete() throws Exception {
        mockMvc.perform(delete("/?data=test"))
                .andExpect(status().isOk());

        var sourceCaptor = ArgumentCaptor.forClass(String.class);
        var userCaptor = ArgumentCaptor.forClass(Authentication.class);
        var actionCaptor = ArgumentCaptor.forClass(String.class);
        var resultCaptor = ArgumentCaptor.forClass(Result.class);

        verify(writer).write(sourceCaptor.capture(), actionCaptor.capture(), any(), any(), userCaptor.capture(),
                resultCaptor.capture(), any(), any(), any(), any());

        assertThat(sourceCaptor.getValue()).isEqualTo("127.0.0.1");
        assertThat(userCaptor.getValue().getName()).isEqualTo("test_user");
        assertThat(actionCaptor.getValue()).isEqualTo("Delete test");
        assertThat(resultCaptor.getValue()).isEqualTo(Result.SUCCESS);
    }

    @Test
    @DisplayName("Проверка аудита. Прямой контроллер, Forbidden")
    @WithMockUser(username = "test_user")
    void test_audit_direct_forbidden() throws Exception {
        mockMvc.perform(get("/request/?data=test"))
                .andExpect(status().isForbidden())
                .andReturn();

        var sourceCaptor = ArgumentCaptor.forClass(String.class);
        var userCaptor = ArgumentCaptor.forClass(Authentication.class);
        var actionCaptor = ArgumentCaptor.forClass(String.class);
        var resultCaptor = ArgumentCaptor.forClass(Result.class);

        verify(writer).write(sourceCaptor.capture(), actionCaptor.capture(), any(), any(), userCaptor.capture(),
                resultCaptor.capture(), any(), any(), any(), any());

        assertThat(sourceCaptor.getValue()).isEqualTo("127.0.0.1");
        assertThat(userCaptor.getValue().getName()).isEqualTo("test_user");
        assertThat(actionCaptor.getValue()).isEqualTo("Multiple operations test");
        assertThat(resultCaptor.getValue()).isEqualTo(Result.FORBIDDEN);
    }

}