package ru.sber.transport.handlers;

import io.qameta.allure.Feature;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.DefaultMethodValidationResultWrapper;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.exceptions.NotAuthorizedException;
import ru.sber.transport.exceptions.dto.ExceptionBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@IsolatedTest
@Feature("lib_transport_core")
@DisplayName("Перехватчик ошибочных запросов")
class RequestExceptionHandlerTest {

    private final RequestExceptionHandler handler = new RequestExceptionHandler();

    @Test
    @DisplayName("Перехват не распознанных исключений")
    void test_commonHandle() {
        var response = handler.handleServiceException(new EntityNotFoundException(Object.class, "id"), mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));

        var rawResponseBody = response.getBody();
        assertThat(rawResponseBody).isInstanceOf(Map.class);

        var responseBody = (Map<String, Object>) rawResponseBody;
        assertThat(responseBody)
            .containsKey("timestamp")
            .containsEntry("status", 500)
            .containsEntry("message", "Request failed. Please contact support")
            .containsEntry("error", "Internal Server Error")
            .containsEntry("path", "");
    }

    @Test
    @DisplayName("Перехват ошибки вызова метода")
    void test_commonHandleHandlerMethodValidationException() throws NoSuchMethodException {
        var object = new TestObject();
        var results = new ArrayList<ParameterValidationResult>();
        var errors = List.of(
            new DefaultMessageSourceResolvable("code")
        );
        var method = object.getClass().getDeclaredMethod("method", UUID.class);
        var param = new MethodParameter(method, 0);
        param.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
        results.add(new ParameterValidationResult(param, "Wrong data", errors));
        var validationResult = new DefaultMethodValidationResultWrapper(object, method, results);
        var validation = new HandlerMethodValidationException(validationResult.getDelegatee());
        var response = handler.handleServiceException(validation, mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(500));

        var rawResponseBody = response.getBody();
        assertThat(rawResponseBody).isInstanceOf(ExceptionBody.class);

        var responseBody = (ExceptionBody) rawResponseBody;
        assertThat(responseBody.getMessage()).isEqualTo("Validation failed");
        assertThat(responseBody.getPath()).isEmpty();
        assertThat(responseBody.getProblems()).hasSize(1);
        assertThat(responseBody.getProblems().iterator().next().getConstraints()).hasSize(1);
        assertThat(responseBody.getProblems().iterator().next().getConstraints().iterator().next().getValue()).isEqualTo("UUID");
        assertThat(responseBody.getProblems().iterator().next().getConstraints().iterator().next().getType()).isEqualTo("Wrong type");
        assertThat(responseBody.getProblems().iterator().next().getField()).isEqualTo("arg");
    }

    @Test
    @DisplayName("Проверка исключения статуса")
    void test_responseStatusException() {
        var ex = new ResponseStatusException(HttpStatusCode.valueOf(402), "Payment required");
        var entity = handler.handleResponseStatusException(ex, mockRequest());

        assertThat(entity.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(402));

        var rawResponseBody = entity.getBody();
        assertThat(rawResponseBody).isInstanceOf(Map.class);

        var responseBody = (Map<String, Object>) rawResponseBody;
        assertThat(responseBody)
            .containsKey("timestamp")
            .containsEntry("status", 402)
            .containsEntry("message", "402 PAYMENT_REQUIRED \"Payment required\"")
            .containsEntry("error", "Payment Required")
            .containsEntry("path", "");
    }

    @Test
    @DisplayName("Проверка отработки - Не найдено")
    void test_handleEntityNotFoundException() {
        var response = handler.handleEntityNotFoundExceptions(new EntityNotFoundException(Object.class, "id"), mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(404));

        var rawResponseBody = response.getBody();
        assertThat(rawResponseBody).isInstanceOf(ExceptionBody.class);

        var responseBody = (ExceptionBody) rawResponseBody;
        assertThat(responseBody.getMessage()).isEqualTo("Data not found: Entity: Object, ID: id");
        assertThat(responseBody.getPath()).isEmpty();
        assertThat(responseBody.getEntity().getName()).isEqualTo("Object");
        assertThat(responseBody.getEntity().getId()).isEqualTo("id");

        response = handler.handleEntityNotFoundExceptions(new EntityNotFoundException(Object.class, "id", false), mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(404));

        rawResponseBody = response.getBody();
        assertThat(rawResponseBody).isInstanceOf(ExceptionBody.class);

        responseBody = rawResponseBody;
        assertThat(responseBody.getMessage()).isEqualTo("Data not found: Entity: Object, ID: id");
        assertThat(responseBody.getPath()).isEmpty();
        assertThat(responseBody.getEntity().getName()).isEqualTo("Object");
        assertThat(responseBody.getEntity().getId()).isEqualTo("id");
    }

    @Test
    @DisplayName("Проверка отработки - Не авторизован")
    void test_handleUnauthorized() {
        UUID employeeId = UUID.randomUUID();
        var response = handler.handleUnauthorized(new NotAuthorizedException(employeeId), mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(403));

        var rawResponseBody = response.getBody();
        assertThat(rawResponseBody).isInstanceOf(ExceptionBody.class);

        var responseBody = (ExceptionBody) rawResponseBody;
        assertThat(responseBody.getMessage()).isEqualTo("Not authorized");
        assertThat(responseBody.getPath()).isEmpty();
        assertThat(responseBody.getEntity().getId()).isEqualTo(employeeId);
    }

    @Test
    @DisplayName("Проверка отработки - Ошибка валидации")
    void test_handleConstraintViolation() {
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        var response = handler.handleConstraintViolation(new ConstraintViolationException(validator.validate(new TestObject())), mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));

        var rawResponseBody = response.getBody();
        assertThat(rawResponseBody).isInstanceOf(ExceptionBody.class);

        var responseBody = (ExceptionBody) rawResponseBody;
        assertThat(responseBody.getMessage()).isEqualTo("Bad Request");
        assertThat(responseBody.getProblems()).hasSize(1);
        assertThat(responseBody.getProblems().iterator().next().getConstraints()).hasSize(1);
        assertThat(responseBody.getProblems().iterator().next().getConstraints().iterator().next().getValue()).isNull();
        assertThat(responseBody.getProblems().iterator().next().getConstraints().iterator().next().getType()).isEqualTo("NotNull");
        assertThat(responseBody.getProblems().iterator().next().getField()).isEqualTo("field");
        assertThat(responseBody.getProblems().iterator().next().getValue()).isEqualTo("null");
    }

    @Test
    @DisplayName("Проверка отработки - Ошибка валидации аргумента метода")
    void test_handleMethodArgumentTypeNotValid() throws NoSuchMethodException {
        var object = new TestObject();
        var method = object.getClass().getDeclaredMethod("method", UUID.class);
        var param = new MethodParameter(method, 0);
        param.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());

        var exception = new MethodArgumentTypeMismatchException("args", UUID.class, "name", new MethodParameter(method, 0), new RuntimeException());

        var response = handler.handleMethodArgumentTypeNotValid(exception, mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));

        var rawResponseBody = response.getBody();
        assertThat(rawResponseBody).isInstanceOf(ExceptionBody.class);

        var responseBody = (ExceptionBody) rawResponseBody;
        assertThat(responseBody.getMessage()).isEqualTo("Bad Request");
        assertThat(responseBody.getProblems()).hasSize(1);
        assertThat(responseBody.getProblems().iterator().next().getConstraints()).hasSize(1);
        assertThat(responseBody.getProblems().iterator().next().getConstraints().iterator().next().getValue()).isEqualTo("UUID");
        assertThat(responseBody.getProblems().iterator().next().getConstraints().iterator().next().getType()).isEqualTo("TypeRequired");
        assertThat(responseBody.getProblems().iterator().next().getField()).isEqualTo("name");
        assertThat(responseBody.getProblems().iterator().next().getValue()).isEqualTo("args");
    }

    @Test
    @DisplayName("Проверка отработки - Ошибка валидации аргумент некорректерн")
    void test_handleMethodArgumentTypeNotValidException() throws NoSuchMethodException {
        var object = new TestObject();
        var method = object.getClass().getDeclaredMethod("method", UUID.class);
        var param = new MethodParameter(method, 0);
        param.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());

        var bindingResult = new BeanPropertyBindingResult(object, "name");
        var objectError = new ObjectError("name", new String[] {"code"}, new String[] {"field"}, null);
        var fieldError = new FieldError("name", "field", "rejected", true, new String[] { "field code"}, new String[] { "field error" }, null);
        bindingResult.addError(objectError);
        bindingResult.addError(fieldError);
        var exception = new MethodArgumentNotValidException(new MethodParameter(method, 0), new BeanPropertyBindingResult(bindingResult, "name"));

        var response = handler.handleMethodArgumentNotValid(exception, new HttpHeaders(), HttpStatusCode.valueOf(400), mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));

        var rawResponseBody = response.getBody();
        assertThat(rawResponseBody).isInstanceOf(ExceptionBody.class);

        var responseBody = (ExceptionBody) rawResponseBody;
        assertThat(responseBody.getMessage()).isEqualTo("Bad Request");
        assertThat(responseBody.getProblems()).hasSize(2);
        var problems = new ArrayList<>(responseBody.getProblems());
        assertThat(problems.get(0).getConstraints()).hasSize(1);
        assertThat(problems.get(0).getConstraints().iterator().next().getType()).isEqualTo("code");
        assertThat(problems.get(0).getField()).isEmpty();
        assertThat(problems.get(0).getValue()).isEmpty();
        assertThat(problems.get(1).getConstraints()).hasSize(1);
        assertThat(problems.get(1).getConstraints().iterator().next().getType()).isEqualTo("field code");
        assertThat(problems.get(1).getField()).isEqualTo("field");
        assertThat(problems.get(1).getValue()).isEqualTo("rejected");
    }

    private WebRequest mockRequest() {
        var request = new MockHttpServletRequest();
        var response = new MockHttpServletResponse();
        return new ServletWebRequest(request, response);
    }

    static class TestObject {

        @NotNull
        private String field;

        void method(UUID arg) {
        }

    }

}