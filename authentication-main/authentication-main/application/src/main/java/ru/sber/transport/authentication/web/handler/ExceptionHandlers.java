package ru.sber.transport.authentication.web.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import ru.sber.transport.authentication.web.exceptions.LoginException;
import ru.sber.transport.authentication.web.exceptions.PasswordException;
import ru.sber.transport.exceptions.dto.Constraint;
import ru.sber.transport.exceptions.dto.ExceptionBody;
import ru.sber.transport.exceptions.dto.Problem;
import ru.sber.transport.handlers.RequestExceptionHandler;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Обработка исключений.
 */
@Slf4j
@ControllerAdvice
class ExceptionHandlers extends RequestExceptionHandler {

    @SuppressWarnings("java:S3958")
    @ExceptionHandler(PasswordException.class)
    ResponseEntity<ExceptionBody> handlePasswordProblem(PasswordException e, WebRequest request) {
        var statusAnnotation = Optional.ofNullable(PasswordException.class.getAnnotation(ResponseStatus.class))
                .orElseThrow();

        var constraints = e.getChecks().stream()
                .map(check -> Constraint.builder().type(check).build()).toList();

        var problem = Problem.builder()
                .field("password")
                .value("[protected]")
                .constraints(constraints).build();

        var body = ExceptionBody.builder()
                .message("Password check failed")
                .problem(problem)
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .build();

        return ResponseEntity.status(statusAnnotation.value()).body(body);
    }

    @ExceptionHandler(LoginException.class)
    ResponseEntity<Object> handleLoginException(LoginException e, WebRequest request) {
        var status = Optional.ofNullable(LoginException.class.getAnnotation(ResponseStatus.class))
                .orElseThrow().value();
        var httpServletRequest = ((ServletWebRequest) request).getRequest();
        var authentication = Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication).orElse(null);
        var body = getExceptionBody(e, status, request, false);
        log.debug("action = " + httpServletRequest.getMethod() + " " + body.get("path") + "; " +
                "clientType = " + httpServletRequest.getHeader("x-client-type") + "; " +
                "user = " + getLogin(authentication) + "; " +
                "id = " + getUserId(authentication) + "; " +
                "IPv4 = " + Optional.ofNullable(httpServletRequest.getHeader("x-real-ip")).orElse("null") + "; " +
                "result = " + body.get("error") + "; " +
                "reason = " + e.getType() + ";");
        return ResponseEntity.status(status).body(body);
    }

    private Map<String, Object> getExceptionBody(LoginException exception, HttpStatus status, WebRequest request, boolean explain) {
        var body = getExceptionBody((Exception) exception, status, request, explain);
        body.put("type", exception.getType());
        return body;
    }

    protected Map<String, Object> getExceptionBody(Exception exception, HttpStatus status, WebRequest request, boolean explain) {
        var exceptionMessage = exception.getMessage();
        var message = !explain && status.is5xxServerError() ? "Request failed. Please contact support" : exceptionMessage;
        var newBody = new LinkedHashMap<String, Object>();
        var currentTime = ZonedDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
        newBody.put("timestamp", currentTime);
        newBody.put("status", status.value());
        newBody.put("message", message != null ? message : "No message available");
        newBody.put("error", status.getReasonPhrase());
        newBody.put("path", ((ServletWebRequest) request).getRequest().getRequestURI());
        return newBody;
    }

    private String getLogin(Authentication user) {
        return Optional.ofNullable(user).map(Authentication::getName).orElse("anonymous");
    }

    private String getUserId(Authentication user) {
        if (user instanceof JwtAuthenticationToken token) {
            return token.getToken().getId();
        } else return getLogin(user);
    }
}
