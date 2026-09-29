package ru.sber.transport.etrn.exceptions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import ru.sber.transport.etrn.dto.ErrorResponseDto;
import ru.sber.transport.etrn.dto.LockConflictResponse;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private MethodParameter createMethodParameter() throws Exception {
        return new MethodParameter(
                GlobalExceptionHandler.class.getMethod(
                        "handleNotFound", EtrnNotFoundException.class),
                0
        );
    }

    @Test
    @DisplayName("handleNotFound — возвращает 404 NOT_FOUND с сообщением")
    void handleNotFound_returns404_withMessage() {
        EtrnNotFoundException ex = new EtrnNotFoundException("ETRn-2026-001234");
        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsEntry("status", HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody()).containsEntry("error", "Not Found");
        assertThat(response.getBody()).containsKey("timestamp");
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody()).containsEntry("message", "ЭТрН ETRn-2026-001234 не найдена");
    }

    @Test
    @DisplayName("handleNotFound — сообщение содержит идентификатор ЭТрН")
    void handleNotFound_messageContainsEtrnId() {
        EtrnNotFoundException ex = new EtrnNotFoundException("123e4567-e89b-12d3-a456-426614174000");
        ResponseEntity<Map<String, Object>> response = handler.handleNotFound(ex);

        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().get("message")).asString()
                .isEqualTo("ЭТрН 123e4567-e89b-12d3-a456-426614174000 не найдена");
    }

    @Test
    @DisplayName("handleBadRequest — возвращает 400 BAD_REQUEST с сообщением")
    void handleBadRequest_returns400_withMessage() {
        BadRequestException ex = new BadRequestException("Некорректные данные");
        ResponseEntity<Map<String, Object>> response = handler.handleBadRequest(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody()).containsEntry("error", "Bad Request");
        assertThat(response.getBody()).containsEntry("message", "Некорректные данные");
    }

    @Test
    @DisplayName("handleLockConflict — возвращает 409 CONFLICT с body LockConflictResponse")
    void handleLockConflict_returns409_withLockConflictResponse() {
        UUID lockedBy = UUID.randomUUID();
        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(5);
        LockConflictException ex = new LockConflictException(lockedBy, lockUntil);

        ResponseEntity<LockConflictResponse> response = handler.handleLockConflict(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().lockedBy()).isEqualTo(lockedBy);
        assertThat(response.getBody().lockUntil()).isEqualTo(lockUntil);
    }

    @Test
    @DisplayName("handleLockConflict — без параметров lockedBy и lockUntil null")
    void handleLockConflict_withoutParams_lockedByAndLockUntilNull() {
        LockConflictException ex = new LockConflictException();

        ResponseEntity<LockConflictResponse> response = handler.handleLockConflict(ex);

        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().lockedBy()).isNull();
        assertThat(response.getBody().lockUntil()).isNull();
    }

    @Test
    @DisplayName("handleValidation — валидация с несколькими ошибками полей")
    void handleValidation_multipleFieldErrors_concatenated() throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("obj", "field1", "обязательное поле"));
        bindingResult.addError(new FieldError("obj", "field2", "некорректное значение"));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                createMethodParameter(), bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertNotNull(response.getBody());
        assertThat(response.getBody().get("message")).asString().contains("field1").contains("field2");
    }

    @Test
    @DisplayName("handleValidation — без ошибок полей default message")
    void handleValidation_noFieldErrors_defaultMessage() throws Exception {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(
                createMethodParameter(), bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "Некорректный запрос");
    }

    @Test
    @DisplayName("handleAttorneyCheckException — возвращает статус из исключения и тело ErrorResponseDto")
    void handleAttorneyCheckException_returnsCorrectStatusAndBody() {
        var exception = new AttorneyCheckException("Дверенность просрочена");
        ResponseEntity<ErrorResponseDto> response = handler.handleAttorneyCheckException(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        var body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.errorCode()).isEqualTo("ATTORNEY_CHECK_ERROR");
        assertThat(body.errorMessage()).isEqualTo("Дверенность просрочена");
    }

    @Test
    @DisplayName("handleGeneral — логирование ошибки и возвращение 500")
    void handleGeneral_logsError_andReturns500() {
        RuntimeException ex = new RuntimeException("Что-то пошло не так");
        ResponseEntity<Map<String, Object>> response = handler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getBody()).containsEntry("error", "Internal Server Error");
        assertThat(response.getBody()).containsEntry("message", "Внутренняя ошибка сервера");
    }

    @Test
    @DisplayName("handleGeneral — обработка AttorneyCheckException")
    void handleGeneral_attorneyCheckException_returns500() {
        AttorneyCheckException ex = new AttorneyCheckException("Проверка доверенности не пройдена");
        ResponseEntity<Map<String, Object>> response = handler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("message", "Внутренняя ошибка сервера");
    }

    // ==================== Checked Exceptions ====================

    @Test
    @DisplayName("EtrnNotFoundException — проверять HTTP статус и message")
    void etrnNotFound_messageCorrect() {
        String etrnId = "TEST-001";
        EtrnNotFoundException ex = new EtrnNotFoundException(etrnId);

        assertThat(ex.getMessage()).isEqualTo("ЭТрН " + etrnId + " не найдена");
    }

    @Test
    @DisplayName("BadRequestException — проверять HTTP статус и message")
    void badRequest_messagePreserved() {
        String message = "Ошибка валидации данных";
        BadRequestException ex = new BadRequestException(message);

        assertThat(ex.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("LockConflictException — проверять HTTP статус и поля")
    void lockConflict_fieldsCorrect() {
        UUID lockedBy = UUID.randomUUID();
        LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(10);
        LockConflictException ex = new LockConflictException(lockedBy, lockUntil);

        assertThat(ex.getLockedBy()).isEqualTo(lockedBy);
        assertThat(ex.getLockUntil()).isEqualTo(lockUntil);
    }

    @Test
    @DisplayName("UserNotFoundException — проверять формат сообщения")
    void userNotFound_messageFormatCorrect() {
        UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        UserNotFoundException ex = new UserNotFoundException(userId);

        assertThat(ex.getMessage()).isEqualTo(String.format(UserNotFoundException.MSG_FORMAT, userId));
    }

    @Test
    @DisplayName("UserNotFoundException — проверять HTTP статус")
    void userNotFound_httpStatus() {
        UserNotFoundException ex = new UserNotFoundException(UUID.randomUUID());

        assertThat(ex.getMessage()).contains("не найден");
    }
}
