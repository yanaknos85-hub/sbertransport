package ru.sber.transport.audit.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.sber.transport.audit.Result;
import ru.sber.transport.audit.annotation.Audited;
import ru.sber.transport.audit.writer.AuditWriter;

import java.util.Optional;

/**
 * Аспект аудита.
 */
@Aspect
@RequiredArgsConstructor
public class AuditedAspect {

    private final AuditWriter auditWriter;

    /**
     * Действие при аудите.
     *
     * @param proceedingJoinPoint состояние среза.
     * @return результат работы.
     * @throws Throwable ошибка в результате работы.
     */
    @Around("@annotation(ru.sber.transport.audit.annotation.Audited)")
    Object auditAction(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        var args = proceedingJoinPoint.getArgs();
        var signature = (MethodSignature) proceedingJoinPoint.getSignature();

        var method = (MethodSignature) proceedingJoinPoint.getSignature();
        var audited = method.getMethod().getAnnotation(Audited.class);
        Result result = null;
        Object proceeded = null;
        try {
            proceeded = proceedingJoinPoint.proceed(args);
            result = Result.SUCCESS;
            return proceeded;
        } catch (Throwable e) {
            result = Result.FAIL;
            throw e;
        } finally {
            auditWriter.write(audited.source(),
                    audited.value(),
                    audited.format(),
                    audited.params(),
                    getUser(),
                    result,
                    proceeded,
                    signature.getParameterNames(),
                    args,
                    null);
        }
    }

    private Authentication getUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext()).map(SecurityContext::getAuthentication)
                .orElse(null);
    }

}
