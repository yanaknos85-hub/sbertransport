package ru.sber.transport.audit.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.CompositeFilter;
import ru.sber.transport.audit.Result;
import ru.sber.transport.audit.service.DocumentationResolver;
import ru.sber.transport.audit.writer.AuditWriter;

import java.io.IOException;
import java.time.temporal.ValueRange;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static ru.sber.transport.audit.Result.*;

/**
 * Фильтр, подмешивающий аудит.
 */
@Slf4j
@WebFilter
@RequiredArgsConstructor
public class AuditFilter extends CompositeFilter {

    private final DocumentationResolver documentationResolver;

    private final AuditWriter auditWriter;

    private static final Map<ValueRange, Result> STATUSES = Map.of(
            ValueRange.of(200, 299), SUCCESS,
            ValueRange.of(401, 401), UNAUTHORIZED,
            ValueRange.of(402, 402), FAIL,
            ValueRange.of(403, 403), FORBIDDEN,
            ValueRange.of(404, 404), NOT_FOUND,
            ValueRange.of(405, 499), USER_ERROR,
            ValueRange.of(500, 599), FAIL
    );

    @Override
    public void doFilter(@NonNull ServletRequest servletRequest, @NonNull ServletResponse servletResponse, @NonNull FilterChain filterChain)
        throws ServletException, IOException {
        String method = null;
        String url = null;
        Integer status = null;
        String remoteAddr = null;
        String clientIPv4 = null;
        if (servletRequest instanceof HttpServletRequest request) {
            method = request.getMethod();
            url = request.getRequestURI();
            remoteAddr = getAddress(request);
            clientIPv4 = getIp(request);
        }
        filterChain.doFilter(servletRequest, servletResponse);
        if (servletResponse instanceof HttpServletResponse response) {
            status = response.getStatus();
        }
        var authentication = Optional.ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication).orElse(null);

        var finalMethod = method;
        var finalUrl = url;
        if(documentationResolver.isAuditable(method, url)) {
            auditWriter.write(remoteAddr,
                    Optional.ofNullable(getDocumentationString(method, url)).orElseGet(() -> getRawString(finalMethod, finalUrl)),
                    "", new String[]{}, authentication,
                    getStatusString(status), null, null, null, clientIPv4);
        }
    }

    private String getAddress(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Referer"))
                .orElseGet(() -> Optional.ofNullable(request.getHeader("Forwarded"))
                        .orElseGet(request::getRemoteAddr));
    }

    private Result getStatusString(Integer status) {
        for (var entry : STATUSES.entrySet()) {
            var range = entry.getKey();
            if (range.isValidIntValue(status)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private String getRawString(String method, String url) {
        return "%s %s".formatted(method.toUpperCase(Locale.ROOT), url);
    }

    private String getDocumentationString(String method, String url) {
        return documentationResolver.getDescription(method, url);
    }

    private String getIp(HttpServletRequest request){
        var realIpHeader = "x-real-ip";
        var forwardedForHeader = "x-forwarded-for";
        if(request.getHeader(realIpHeader) != null && !request.getHeader(realIpHeader).isEmpty()){
            return request.getHeader(realIpHeader);
        } else if(request.getHeader(forwardedForHeader) != null && !request.getHeader(forwardedForHeader).isEmpty()){
            return request.getHeader(forwardedForHeader);
        } else return request.getRemoteAddr();
    }
}
