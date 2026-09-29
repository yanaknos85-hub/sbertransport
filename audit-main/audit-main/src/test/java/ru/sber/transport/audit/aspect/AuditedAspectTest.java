package ru.sber.transport.audit.aspect;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.sber.transport.audit.Result;
import ru.sber.transport.audit.fakeApp.AuditedAction;
import ru.sber.transport.audit.fakeApp.DoNotStartThis;
import ru.sber.transport.audit.writer.AuditWriter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = DoNotStartThis.class)
@DisplayName("Проверка аудита действий")
class AuditedAspectTest {

    @Autowired
    private AuditedAction action;

    @MockitoBean
    private AuditWriter writer;

    @DisplayName("Успех")
    @Test
    void test_success() {
        action.action();

        var sourceCaptor = ArgumentCaptor.forClass(String.class);
        var userCaptor = ArgumentCaptor.forClass(Authentication.class);
        var actionCaptor = ArgumentCaptor.forClass(String.class);
        var resultCaptor = ArgumentCaptor.forClass(Result.class);

        verify(writer).write(sourceCaptor.capture(), actionCaptor.capture(), any(), any(), userCaptor.capture(),
                resultCaptor.capture(), any(), any(), any(), any());

        assertThat(sourceCaptor.getValue()).isEqualTo("source1");
        assertThat(userCaptor.getValue()).isNull();
        assertThat(actionCaptor.getValue()).isEqualTo("Check success action");
        assertThat(resultCaptor.getValue()).isEqualTo(Result.SUCCESS);
    }

    @DisplayName("Успех пользователя")
    @Test
    void test_success_user() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("test_user", "", List.of()));

        action.action();

        var sourceCaptor = ArgumentCaptor.forClass(String.class);
        var userCaptor = ArgumentCaptor.forClass(Authentication.class);
        var actionCaptor = ArgumentCaptor.forClass(String.class);
        var resultCaptor = ArgumentCaptor.forClass(Result.class);

        verify(writer).write(sourceCaptor.capture(), actionCaptor.capture(), any(), any(), userCaptor.capture(),
                resultCaptor.capture(), any(), any(), any(), any());

        assertThat(sourceCaptor.getValue()).isEqualTo("source1");
        assertThat(userCaptor.getValue().getName()).isEqualTo("test_user");
        assertThat(actionCaptor.getValue()).isEqualTo("Check success action");
        assertThat(resultCaptor.getValue()).isEqualTo(Result.SUCCESS);
    }

    @DisplayName("Провал")
    @Test
    void test_failed() {
        try {
            action.failed_action();
        } catch (RuntimeException e) {
            // Не имеет значения
        }

        var sourceCaptor = ArgumentCaptor.forClass(String.class);
        var userCaptor = ArgumentCaptor.forClass(Authentication.class);
        var actionCaptor = ArgumentCaptor.forClass(String.class);
        var resultCaptor = ArgumentCaptor.forClass(Result.class);

        verify(writer).write(sourceCaptor.capture(), actionCaptor.capture(), any(), any(), userCaptor.capture(),
                resultCaptor.capture(), any(), any(), any(), any());

        assertThat(sourceCaptor.getValue()).isEqualTo("source2");
        assertThat(userCaptor.getValue()).isNull();
        assertThat(actionCaptor.getValue()).isEqualTo("Check failed action");
        assertThat(resultCaptor.getValue()).isEqualTo(Result.FAIL);
    }

    @DisplayName("Успех с данными")
    @Test
    void test_success_with_data() {
        action.success_with_data("test");

        var sourceCaptor = ArgumentCaptor.forClass(String.class);
        var userCaptor = ArgumentCaptor.forClass(Authentication.class);
        var actionCaptor = ArgumentCaptor.forClass(String.class);
        var resultCaptor = ArgumentCaptor.forClass(Result.class);
        var argsCaptor = ArgumentCaptor.forClass(Object[].class);

        verify(writer).write(sourceCaptor.capture(), actionCaptor.capture(), any(), any(), userCaptor.capture(),
                resultCaptor.capture(), any(), any(), argsCaptor.capture(), any());

        assertThat(sourceCaptor.getValue()).isEqualTo("source3");
        assertThat(userCaptor.getValue()).isNull();
        assertThat(actionCaptor.getValue()).isEqualTo("Check success action with data '{data}'");
        assertThat(resultCaptor.getValue()).isEqualTo(Result.SUCCESS);
        assertThat(argsCaptor.getValue()).hasSize(1);
        assertThat(argsCaptor.getValue()[0]).hasToString("test");
    }

    @DisplayName("Провал с данными")
    @Test
    void test_fail_with_data() {
        try {
            action.fail_with_data("test");
        } catch (RuntimeException e) {
            // Не имеет значения
        }

        var sourceCaptor = ArgumentCaptor.forClass(String.class);
        var userCaptor = ArgumentCaptor.forClass(Authentication.class);
        var actionCaptor = ArgumentCaptor.forClass(String.class);
        var resultCaptor = ArgumentCaptor.forClass(Result.class);
        var argsCaptor = ArgumentCaptor.forClass(Object[].class);

        verify(writer).write(sourceCaptor.capture(), actionCaptor.capture(), any(), any(), userCaptor.capture(),
                resultCaptor.capture(), any(), any(), argsCaptor.capture(), any());

        assertThat(sourceCaptor.getValue()).isEqualTo("source4");
        assertThat(userCaptor.getValue()).isNull();
        assertThat(actionCaptor.getValue()).isEqualTo("Check fail action with data '{data}'");
        assertThat(resultCaptor.getValue()).isEqualTo(Result.FAIL);
        assertThat(argsCaptor.getValue()).hasSize(1);
        assertThat(argsCaptor.getValue()[0]).hasToString("test");
    }
}