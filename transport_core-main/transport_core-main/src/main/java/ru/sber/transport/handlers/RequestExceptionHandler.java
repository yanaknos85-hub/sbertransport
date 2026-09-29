package ru.sber.transport.handlers;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.exceptions.NotAuthorizedException;
import ru.sber.transport.exceptions.dto.Constraint;
import ru.sber.transport.exceptions.dto.Entity;
import ru.sber.transport.exceptions.dto.ExceptionBody;
import ru.sber.transport.exceptions.dto.Problem;
import ru.sberbank.ditsib.transport.exceptions.IllegalCallerResponseException;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

/**
 * Handler for web exception
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
@Slf4j
@Setter
public class RequestExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String DEFAULT_MESSAGE = "No message available";

    private static final String HEADER_TIMESTAMP = "timestamp";

    private static final String HEADER_STATUS = "status";

    private static final String HEADER_ERROR = "error";

    private static final String HEADER_MESSAGE = "message";

    private static final String HEADER_PATH = "path";

    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @Value("${spring.explain-errors:false}")
    private boolean explainErrors;

    /**
     * Перехват системного исключения.
     *
     * @param ex      исходное исключение.
     * @param request {@link WebRequest} выполняемый запрос.
     * @return {@link  ResponseEntity<Object>} с сообщением об ошибке.
     */
    @ExceptionHandler({IllegalCallerResponseException.class,
        IllegalStateResponseException.class, RuntimeException.class, IllegalArgumentException.class,
        Exception.class, Error.class})
    public ResponseEntity<Object> handleServiceException(Exception ex, WebRequest request) {
        var status = resolveAnnotatedResponseStatus(ex);
        return handleExceptionInternal(ex, getExceptionBody(ex, status, request, explainErrors),
            new HttpHeaders(), status, request);
    }

    /**
     * Перехват исключения статуса запроса.
     *
     * @param ex      исключение.
     * @param request выполняемый запрос.
     * @return тело ответа.
     */
    @ExceptionHandler({ResponseStatusException.class})
    public ResponseEntity<Object> handleResponseStatusException(ResponseStatusException ex, WebRequest request) {
        var status = HttpStatus.valueOf(ex.getStatusCode().value());
        return handleExceptionInternal(ex, getExceptionBody(ex, status, request, explainErrors), new HttpHeaders(),
            status, request);
    }

    /**
     * Перехват исключения отсутствия ресурса.
     *
     * @param e       исключение.
     * @param request выполняемый запрос.
     * @return тело ответа.
     */
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ExceptionBody> handleEntityNotFoundExceptions(EntityNotFoundException e, WebRequest request) {
        var bodyBuilder = ExceptionBody.builder();
        if (e.getEntityName() != null && e.getEntityId() != null) {
            var entityName = e.getEntityName();
            bodyBuilder.entity(Entity.builder().name(entityName)
                .id(e.getEntityId())
                .build());
        }
        bodyBuilder.message(e.getMessage()).path(((ServletWebRequest) request).getRequest().getRequestURI())
            .timestamp(OffsetDateTime.now(ZoneOffset.UTC));
        if (e.isStackTraceEnabled()) {
            log.error("Entity not found", e);
        } else {
            log.error(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(bodyBuilder.build());
    }

    /**
     * Перехват исключения отсутствия авторизации.
     *
     * @param e исключение.
     * @return тело ответа.
     */
    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<ExceptionBody> handleUnauthorized(NotAuthorizedException e, WebRequest webRequest) {
        var entity = Entity.builder()
            .id(e.getEntityId())
            .build();

        var body = ExceptionBody.builder()
            .entity(entity)
            .message(e.getReason())
            .path(((ServletWebRequest) webRequest).getRequest().getRequestURI())
            .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
            .build();

        return ResponseEntity.status(ReflectionUtils.getAnnotation(ResponseStatus.class, e.getClass()).value())
            .body(body);
    }

    /**
     * Перехват исключения нарушения ограничений.
     *
     * @param e       исключение.
     * @param request выполняемый запрос.
     * @return тело ответа.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException e, WebRequest request) {
        var status = HttpStatus.BAD_REQUEST;
        var bodyBuilder = ExceptionBody.builder().message(status.getReasonPhrase())
            .path(((ServletWebRequest) request).getRequest().getRequestURI());

        for (var violation : e.getConstraintViolations()) {
            var constraintType = violation.getConstraintDescriptor().getAnnotation().annotationType();
            if (constraintType.getPackageName().startsWith("ru.sber")) {
                return handleServiceException(e, request);
            }
            var constraint = Constraint.builder()
                .type(constraintType.getSimpleName())
                .build();

            var problem = Problem.builder()
                .field(violation.getPropertyPath().toString().replace("set.data", ""))
                .value(String.valueOf(violation.getInvalidValue()))
                .constraints(Collections.singletonList(constraint)).build();

            bodyBuilder.problem(problem);
        }

        return ResponseEntity.status(status).body(bodyBuilder.build());
    }

    /**
     * Ошибка типа аргумента метода.
     *
     * @param e       исключение.
     * @param request выполняемый запрос.
     * @return ответ.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ExceptionBody> handleMethodArgumentTypeNotValid(MethodArgumentTypeMismatchException e,
                                                                          @NonNull WebRequest request) {
        var status = HttpStatus.BAD_REQUEST;
        var bodyBuilder = ExceptionBody.builder().message(status.getReasonPhrase())
            .path(((ServletWebRequest) request).getRequest().getRequestURI());

        var requiredType = Optional.ofNullable(e.getRequiredType()).map(Class::getSimpleName).orElse(null);
        bodyBuilder.problem(Problem.builder().field(e.getName()).value(String.valueOf(e.getValue()))
            .constraints(Collections.singletonList(Constraint.builder().type("TypeRequired").value(requiredType)
                .build())).build());

        return ResponseEntity.status(status).body(bodyBuilder.build());
    }

    @Override
    protected @NonNull
    ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException e,
        @NonNull HttpHeaders headers,
        HttpStatusCode status,
        @NonNull WebRequest request
    ) {
        var bodyBuilder = ExceptionBody.builder().message(HttpStatus.valueOf(status.value()).getReasonPhrase())
            .path(((ServletWebRequest) request).getRequest().getRequestURI());

        var model = e.getBindingResult().getModel();
        var problems = extractProblems(model);
        bodyBuilder.problems(problems);
        return ResponseEntity.status(status).body(bodyBuilder.build());
    }

    @Override
    protected @NonNull
    ResponseEntity<Object> handleHttpMessageNotReadable(
        HttpMessageNotReadableException ex,
        @NonNull HttpHeaders headers,
        HttpStatusCode status,
        @NonNull WebRequest request
    ) {
        var message = ex.getMessage();

        var bodyBuilder = ExceptionBody.builder()
            .message(HttpStatus.valueOf(status.value()).getReasonPhrase())
            .path(((ServletWebRequest) request).getRequest().getRequestURI());

        if (ex.getCause() instanceof InvalidFormatException format) {
            bodyBuilder.problems(extractInvalidFormatProblems(format));
        } else if (ex.getCause() instanceof JsonParseException) {
            bodyBuilder.message(ex.getCause().getMessage());
        } else if (message != null && message.contains("body is missing")) {
            bodyBuilder.message("Request body is missing");
        } else {
            bodyBuilder.message(message);
        }

        return ResponseEntity.status(status).body(bodyBuilder.build());
    }

    /**
     * Перехват ошибки дубликата данных.
     *
     * @param e       исключение.
     * @param request выполняемый запрос.
     * @return ответ.
     */
    @ExceptionHandler(DuplicateDataException.class)
    public ResponseEntity<Object> handleDuplicateDataException(DuplicateDataException e, WebRequest request) {
        var status = HttpStatus.CONFLICT;
        var bodyBuilder = ExceptionBody.builder();
        if (e.getEntityName() != null && !e.getValues().isEmpty()) {
            var entityName = e.getEntityName();
            bodyBuilder = bodyBuilder.entity(Entity.builder().name(entityName).build());

            for (var entry : e.getValues().entrySet()) {
                bodyBuilder = bodyBuilder.problem(
                    Problem.builder().field(entry.getKey()).value(String.valueOf(entry.getValue())).build());
            }
        }
        bodyBuilder.message(e.getMessage()).path(((ServletWebRequest) request).getRequest().getRequestURI()).build();
        return ResponseEntity.status(status).body(bodyBuilder.build());
    }

    HttpStatus resolveAnnotatedResponseStatus(Exception exception) {
        ResponseStatus annotation = findMergedAnnotation(exception.getClass(), ResponseStatus.class);
        if (annotation != null) {
            return annotation.value();
        }
        if (exception instanceof RestClientResponseException ex) {
            return HttpStatus.valueOf(ex.getStatusCode().value());
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
        @NonNull Exception exception,
        Object body,
        @NonNull HttpHeaders headers,
        @NonNull HttpStatusCode status,
        @NonNull WebRequest request
    ) {
        var newHeaders = new HttpHeaders();
        newHeaders.addAll(headers);
        newHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (exception instanceof HandlerMethodValidationException validation) {
            var problems = new LinkedList<Problem>();
            for (var result : validation.getAllValidationResults()) {
                var parameter = result.getMethodParameter();
                var name = parameter.getParameterName();
                var problem = Problem.builder()
                    .field(name)
                    .constraints(List.of(Constraint.builder().type("Wrong type").value(parameter.getParameterType().getSimpleName()).build()))
                    .build();
                problems.add(problem);
            }
            var error = ExceptionBody.builder()
                .path(((ServletWebRequest) request).getRequest().getRequestURI())
                .timestamp(OffsetDateTime.now(ZoneOffset.UTC))
                .message("Validation failed")
                .problems(problems)
                .build();
            return ResponseEntity
                .status(status)
                .headers(newHeaders)
                .body(error);
        }
        log.error(exception.getMessage(), exception);
        return super.handleExceptionInternal(exception, Objects.isNull(body) || "".equals(body)
            ? getExceptionBody(exception, HttpStatus.valueOf(status.value()), request, explainErrors)
            : body, newHeaders, status, request);
    }

    protected Map<String, Object> getExceptionBody(Exception exception, HttpStatus status, WebRequest request,
                                                 boolean explain) {
        var exceptionMessage = exception.getMessage();
        var message = explain || !status.is5xxServerError()
            ? exceptionMessage
            : "Request failed. Please contact support";

        Map<String, Object> newBody = new LinkedHashMap<>();
        var currentTime = ZonedDateTime.now(ZoneId.of(ZoneOffset.UTC.getId()))
            .format(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT));
        newBody.put(HEADER_TIMESTAMP, currentTime);
        newBody.put(HEADER_STATUS, status.value());
        newBody.put(HEADER_MESSAGE, message != null ? message : DEFAULT_MESSAGE);
        newBody.put(HEADER_ERROR, status.getReasonPhrase());
        newBody.put(HEADER_PATH, ((ServletWebRequest) request).getRequest().getRequestURI());
        return newBody;
    }

    private String extractPath(List<JsonMappingException.Reference> path) {
        var builder = new StringBuilder();
        for (var item : path) {
            var pathIndex = Optional.of(item.getIndex()).filter(i -> i >= 0).map(i -> String.format("[%s]", i))
                .orElse("");
            if (!builder.toString().isBlank() && !pathIndex.startsWith("[") && !pathIndex.endsWith("]")) {
                builder.append(".");
            }
            builder.append(Optional.ofNullable(item.getFieldName()).orElse(""));
            builder.append(pathIndex);
        }
        return builder.toString();
    }

    private Set<Problem> extractInvalidFormatProblems(InvalidFormatException ex) {
        var problems = new HashSet<Problem>();

        problems.add(Problem.builder()
            .field(extractPath(ex.getPath()))
            .value(String.valueOf(ex.getValue()))
            .constraints(extractConstraintMap(ex.getTargetType()))
            .build());

        return problems;
    }

    private List<Constraint> extractConstraintMap(Class<?> target) {
        var constraints = new ArrayList<Constraint>();
        if (Enum.class.isAssignableFrom(target)) {
            var constraint = Constraint.builder()
                .type("Enum")
                .value(Arrays.stream(target.getEnumConstants())
                    .map(Enum.class::cast).map(Enum::name)
                    .toArray()).build();
            constraints.add(constraint);
        }
        return constraints;
    }

    /**
     * Конвертирует отклоненное значение для ошибки.
     *
     * @param rejectedValue отклоненное сообщение.
     * @return конвертированное сообщение.
     */
    private String convertValue(Object rejectedValue) {
        if (rejectedValue == null) {
            return null;
        }
        if (rejectedValue instanceof String
            || rejectedValue instanceof Number
            || rejectedValue instanceof Boolean) {
            return String.valueOf(rejectedValue);
        }
        return rejectedValue.toString();
    }

    /**
     * Получает список ограничений из ошибок.
     *
     * @param error объект ошибки.
     * @return ограничения.
     */
    private Object extractValues(FieldError error) {
        if (error != null) {
            var arguments = error.getArguments();
            if (arguments == null) {
                return null;
            }
            if ("Size".equals(error.getCode())) {
                var map = new HashMap<String, Object>();
                map.put("minLength", arguments[2]);
                map.put("maxLength", arguments[1]);
                return map;
            }
            if ("Pattern".equals(error.getCode())) {
                var pattern = arguments[2];
                var map = new HashMap<String, Object>();
                map.put("pattern", pattern.toString());
                return map;
            }
        }
        return null;
    }

    /**
     * Получает список ограничений из ошибок.
     *
     * @return ограничения.
     */
    private Object extractValues(Annotation annotation) {
        if (annotation != null) {
            if (annotation.annotationType().isAssignableFrom(Size.class)) {
                var size = (Size) annotation;

                var map = new HashMap<String, Object>();
                map.put("min", size.min());
                map.put("max", size.max());
                return map;
            } else if (annotation.annotationType().isAssignableFrom(Pattern.class)) {
                var pattern = (Pattern) annotation;

                var map = new HashMap<String, Object>();
                map.put("pattern", pattern.regexp());
                return map;
            }
        }
        return null;
    }

    private Class<? extends Annotation> findValidatorAnnotation(String n) {
        try {
            //noinspection unchecked
            return (Class<? extends Annotation>) Class.forName(n);
        } catch (ClassNotFoundException ignore) {
            // existing is not matter
            return null;
        }
    }

    private List<Problem> extractProblems(Map<String, Object> model) {
        var problems = new ArrayList<Problem>();
        for (var entry : model.entrySet()) {
            extractProblem(problems, entry);
        }
        return problems;
    }

    private void extractProblem(List<Problem> problems, Map.Entry<String, Object> entry) {
        var value = entry.getValue();
        if (value instanceof BeanPropertyBindingResult result) {
            for (var error : result.getAllErrors()) {
                if (error instanceof FieldError fieldError) {
                    extractFieldProblem(problems, fieldError);
                } else if (error != null) {
                    extractObjectProblem(problems, error);
                }
            }
        }
    }

    private void extractObjectProblem(List<Problem> problems, ObjectError error) {
        var fieldsNamesBuilder = new StringBuilder();
        for (var argument : Objects.requireNonNull(error.getArguments())) {
            if (argument.getClass().getName().equals("org.springframework.validation.beanvalidation" +
                ".SpringValidatorAdapter$ResolvableAttribute")) {
                var fieldName = argument.toString();
                if (!fieldsNamesBuilder.isEmpty()) {
                    fieldsNamesBuilder.append(" - ");
                }
                fieldsNamesBuilder.append(fieldName);
            }
        }
        var problem = Problem.builder()
            .field(fieldsNamesBuilder.toString())
            .value("")
            .constraints(Collections.singletonList(Constraint.builder()
                .type(error.getCode())
                .build())).build();
        problems.add(problem);
    }

    private <T> T getDynamicPayload(FieldError fieldError, Class<T> payloadClass) {
        try {
            var field = fieldError.getClass().getDeclaredField("violation");
            field.setAccessible(true); // NOSONAR
            var rawViolation = field.get(fieldError);
            if (rawViolation instanceof ConstraintViolationImpl<?> violation) {
                return violation.getDynamicPayload(payloadClass);
            }
        } catch (Exception ignore) {
            // ignore
        }
        return null;
    }

    @SuppressWarnings("java:S3958")
    private void extractFieldProblem(List<Problem> problems, FieldError fieldError) {

        var fieldName = fieldError.getField();
        addProblem(problems, fieldName, fieldError.getCode(), fieldError.getRejectedValue(), extractValues(fieldError));

        try {
            var violation = getDynamicPayload(fieldError, ConstraintViolationImpl.class);
            if (violation == null) {
                return;
            }

            var validatedRootBeanClass = violation.getRootBeanClass();
            var validatedRootField = validatedRootBeanClass.getDeclaredField(fieldName);
            var validatedBeanClass = validatedRootField.getType();
            var validatedField = validatedBeanClass.getDeclaredField(violation.getPropertyPath().toString());

            var annotations = ((List<?>) violation.getConstraintDescriptor().getConstraintValidatorClasses())
                .stream()
                .map(Class.class::cast)
                .map(Class::getGenericSuperclass)
                .map(Type::getTypeName)
                .map(c -> c.split("<")[1].split(",")[0].trim().replace(">", ""))
                .map(this::findValidatorAnnotation)
                .filter(Objects::nonNull)
                .map(validatedField::getAnnotation)
                .toList();

            for (var annotation : annotations) {
                addProblem(problems, fieldName + "." + violation.getPropertyPath().toString(),
                    annotation.annotationType().getSimpleName(), violation.getInvalidValue(),
                    extractValues(annotation));
            }
        } catch (NoSuchFieldException ignore) {
            // field can not exist
        }
    }

    private void addProblem(List<Problem> problems, String fieldName, String errorCode, Object rejected, Object expected) {
        var problem =
            problems.stream().filter(p -> fieldName.equals(p.getField())).findFirst()
                .orElse(Problem.builder()
                    .field(fieldName)
                    .value(convertValue(rejected)).build());
        problem.getConstraints().add(Constraint.builder()
            .type(errorCode)
            .value(expected)
            .build());
        problems.add(problem);
    }
}
