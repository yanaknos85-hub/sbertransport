package ru.sber.transport.audit.writer.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.util.StringUtils;
import ru.sber.transport.audit.Result;
import ru.sber.transport.audit.resolver.AuthenticatedResolver;
import ru.sber.transport.audit.resolver.DbResolver;
import ru.sber.transport.audit.writer.AuditWriter;
import ru.sber.transport.utils.collections.MapUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * Реализация записи аудита.
 */
@Slf4j
@RequiredArgsConstructor
class AuditWriterImpl implements AuditWriter {

    private final ObjectProvider<AuthenticatedResolver> authenticatedResolverObjectFactory;

    private final ObjectProvider<DbResolver> dbResolverObjectFactory;

    private final MapUtils mapUtils;

    @Override
    public void write(String source,
                      String action,
                      String format,
                      String[] formatParams,
                      Authentication user,
                      Result result,
                      Object returnData,
                      String[] parameterNames,
                      Object[] arguments,
                      String clientIPv4) {
        var authenticatedResolver = authenticatedResolverObjectFactory.getIfAvailable();
        String authenticated;
        if (authenticatedResolver != null) {
            authenticated = authenticatedResolver.resolveUser(user);
        } else {
            authenticated = createDefaultString(user);
        }

        var isActuator = action.startsWith("GET /actuator");
        var decodedAction = StringUtils.hasText(action) ? decodeAction(action, result, returnData, parameterNames, arguments) :
                decodeAction(format, formatParams, parameterNames, arguments);

        var userId = getUserId(user);
        var formatted = "AUDIT: source = %s; user = %s; id = %s; IPv4 = %s; action = %s; result = %s"
                .formatted(source,
                        authenticated,
                        userId,
                        clientIPv4,
                        decodedAction,
                        result);

        if (log().isInfoEnabled() && !isActuator) {
            log().info(formatted);
        }

        if(log().isTraceEnabled() && isActuator) {
            log().trace(formatted);
        }

        var dbResolver = dbResolverObjectFactory.getIfAvailable();

        if (dbResolver != null && !StringUtils.hasText(clientIPv4)) {
            try {
                dbResolver.save(source, authenticated, LocalDateTime.now(), decodedAction);
            } catch (Exception e) {
                log().error("Ошибка при записи в БД", e);
            }
        }
    }

    private String decodeAction(String action, Result result, Object returnData, String[] parameterNames, Object[] arguments) {
        if (action.contains("{") && action.contains("}")) {
            var matcher = Pattern.compile("(\\{.*})").matcher(action);
            if (matcher.find()) {
                var expression = matcher.group(0);
                var constant = action.replace(expression, "%s");
                return constant.formatted(decodeExpression(expression, result, returnData, parameterNames, arguments));
            } else {
                return action;
            }
        } else {
            return action;
        }
    }

    private String decodeAction(String format, String[] params, String[] parameterNames, Object[] arguments) {
        if (StringUtils.hasText(format) && params.length > 0) {
            var paramList = Arrays.stream(params).toList();
            var result = new ArrayList<String>();
            for (var i = 0; i < parameterNames.length; i++) {
                var name = parameterNames[i];
                if (paramList.contains(name)) {
                    var argument = arguments[i];
                    result.add(String.valueOf(argument));
                }
            }

            if (!result.isEmpty()) {
                return format.formatted(result.toArray());
            }
        }

        return "";
    }

    private String decodeExpression(String expression, Result result, Object returnData, String[] parameterNames, Object[] arguments) {
        expression = expression.substring(1, expression.length() - 1);
        var parts = expression.split("\\?");
        if (parts.length == 2) {
            var expectedResult = parts[0].trim();
            var subexpression = parts[1].trim().split(":");
            if (result.name().equalsIgnoreCase(expectedResult) || (expectedResult.startsWith("!") && !result.name().equalsIgnoreCase(expectedResult))) {
                var positive = subexpression[0].trim();
                return decodeResultedExpression(true, positive, returnData, parameterNames, arguments);
            } else if (subexpression.length == 2) {
                var negative = subexpression[1].trim();
                return decodeResultedExpression(false, negative, returnData, parameterNames, arguments);
            }
        } else if (parts.length == 1) {
            return decodeResultedExpression(Result.SUCCESS.equals(result), expression, returnData, parameterNames, arguments);
        }
        throw new IllegalArgumentException("Wrong expression: %s".formatted(expression));
    }

    private String decodeResultedExpression(boolean success, String expression, Object returnData, String[] parameterNames, Object[] arguments) {
        var plan = expressionToPlan(expression);
        var result = new ArrayList<String>();
        for (var planItem : plan) {
            if (planItem.get().equals("{result}")) {
                planItem.set(success ? String.valueOf(returnData) : "error");
            } else {
                result.addAll(extractValue(parameterNames, arguments, planItem));
            }
        }
        return String.join("", result);
    }

    private List<String> extractValue(String[] parameterNames, Object[] arguments, AtomicReference<String> planItem) {
        var result = new ArrayList<String>();
        var planItemValue = planItem.get();
        for (var i = 0; i < parameterNames.length; i++) {
            var name = parameterNames[i];
            if (name.equals(planItemValue.split("\\.")[0])) {
                var argument = arguments[i];
                if (name.contains(".")) {
                    var fieldName = planItemValue.replace(name + ".", "");
                    result.add(String.valueOf(mapUtils.extractNode(argument, fieldName, Object.class)));
                } else {
                    result.add(String.valueOf(argument));
                }
            }
        }
        return result;
    }

    @SuppressWarnings("java:S3958")
    private List<AtomicReference<String>> expressionToPlan(String expression) {
        expression = expression.replace("{", "-{");
        expression = expression.replace("}", "}-");
        return Arrays.stream(expression.split("-"))
                .map(AtomicReference::new)
                .toList();
    }

    private String createDefaultString(Authentication user) {
        return Optional.ofNullable(user).map(Authentication::getName).orElse("anonymous");
    }

    private String getUserId(Authentication user) {
        if (user instanceof JwtAuthenticationToken token) {
            return token.getToken().getId();
        } else return createDefaultString(user);
    }

    Logger log() {
        return log;
    }
}
