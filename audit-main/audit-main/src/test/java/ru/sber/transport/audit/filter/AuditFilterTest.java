package ru.sber.transport.audit.filter;

import io.qameta.allure.Feature;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.audit.Result;
import ru.sber.transport.audit.service.DocumentationResolver;
import ru.sber.transport.audit.writer.AuditWriter;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Проверка фильтра аудита")
@UnitTest
@IsolatedTest
@Feature("lib_audit")
class AuditFilterTest {

    private final DocumentationResolver documentationResolver = mock(DocumentationResolver.class);
    private final AuditWriter auditWriter = mock(AuditWriter.class);

    @Test
    @DisplayName("Запрос проходит через фильтр, аудит записывается для аудитабельного endpoint'а")
    void test_doFilter_auditableEndpoint() throws ServletException, IOException {
        when(documentationResolver.isAuditable("GET", "/test")).thenReturn(true);
        when(documentationResolver.getDescription("GET", "/test")).thenReturn("GET /test");

        var filter = new AuditFilter(documentationResolver, auditWriter);

        var request = new MockHttpServletRequest(new MockServletContext());
        request.setMethod("GET");
        request.setRequestURI("/test");

        var response = new MockHttpServletResponse();
        var filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(auditWriter).write(
                anyString(),
                eq("GET /test"),
                eq(""),
                eq(new String[0]),
                eq(null),
                eq(Result.SUCCESS),
                eq(null),
                eq(null),
                eq(null),
                anyString()
        );
    }

    @Test
    @DisplayName("Аудит не записывается для неаудитабельного endpoint'а")
    void test_doFilter_notAuditableEndpoint() throws ServletException, IOException {
        when(documentationResolver.isAuditable(any(), any())).thenReturn(false);

        var filter = new AuditFilter(documentationResolver, auditWriter);

        var request = new MockHttpServletRequest(new MockServletContext());
        request.setMethod("GET");
        request.setRequestURI("/other");

        var response = new MockHttpServletResponse();
        var filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(auditWriter, never()).write(any(), any(), any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Аудит записывается с описанием из документации")
    void test_doFilter_withDocumentationDescription() throws ServletException, IOException {
        when(documentationResolver.isAuditable("POST", "/api/resource")).thenReturn(true);
        when(documentationResolver.getDescription("POST", "/api/resource"))
                .thenReturn("Create resource");

        var filter = new AuditFilter(documentationResolver, auditWriter);

        var request = new MockHttpServletRequest(new MockServletContext());
        request.setMethod("POST");
        request.setRequestURI("/api/resource");

        var response = new MockHttpServletResponse();
        var filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(auditWriter).write(
                anyString(),
                eq("Create resource"),
                eq(""),
                eq(new String[0]),
                eq(null),
                eq(Result.SUCCESS),
                eq(null),
                eq(null),
                eq(null),
                anyString()
        );
    }

    @Test
    @DisplayName("Аудит записывается с сырым описанием, если документация не найдена")
    void test_doFilter_withRawDescription() throws ServletException, IOException {
        when(documentationResolver.isAuditable("PUT", "/data")).thenReturn(true);
        when(documentationResolver.getDescription("PUT", "/data")).thenReturn(null);

        var filter = new AuditFilter(documentationResolver, auditWriter);

        var request = new MockHttpServletRequest(new MockServletContext());
        request.setMethod("PUT");
        request.setRequestURI("/data");

        var response = new MockHttpServletResponse();
        var filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(auditWriter).write(
                anyString(),
                eq("PUT /data"),
                eq(""),
                eq(new String[0]),
                eq(null),
                eq(Result.SUCCESS),
                eq(null),
                eq(null),
                eq(null),
                anyString()
        );
    }

    @Test
    @DisplayName("Статус 200 маппится на Result.SUCCESS")
    void test_doFilter_status200() throws ServletException, IOException {
        when(documentationResolver.isAuditable(any(), any())).thenReturn(true);

        var filter = new AuditFilter(documentationResolver, auditWriter);

        var request = new MockHttpServletRequest(new MockServletContext());
        request.setMethod("GET");
        request.setRequestURI("/ok");

        var response = new MockHttpServletResponse();
        response.setStatus(200);
        var filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(auditWriter).write(any(), any(), any(), any(), any(), eq(Result.SUCCESS), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Статус 401 маппится на Result.UNAUTHORIZED")
    void test_doFilter_status401() throws ServletException, IOException {
        when(documentationResolver.isAuditable(any(), any())).thenReturn(true);

        var filter = new AuditFilter(documentationResolver, auditWriter);

        var request = new MockHttpServletRequest(new MockServletContext());
        request.setMethod("GET");
        request.setRequestURI("/auth");

        var response = new MockHttpServletResponse();
        response.setStatus(401);
        var filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(auditWriter).write(any(), any(), any(), any(), any(), eq(Result.UNAUTHORIZED), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Статус 403 маппится на Result.FORBIDDEN")
    void test_doFilter_status403() throws ServletException, IOException {
        when(documentationResolver.isAuditable(any(), any())).thenReturn(true);

        var filter = new AuditFilter(documentationResolver, auditWriter);

        var request = new MockHttpServletRequest(new MockServletContext());
        request.setMethod("GET");
        request.setRequestURI("/forbidden");

        var response = new MockHttpServletResponse();
        response.setStatus(403);
        var filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(auditWriter).write(any(), any(), any(), any(), any(), eq(Result.FORBIDDEN), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Статус 404 маппится на Result.NOT_FOUND")
    void test_doFilter_status404() throws ServletException, IOException {
        when(documentationResolver.isAuditable(any(), any())).thenReturn(true);

        var filter = new AuditFilter(documentationResolver, auditWriter);

        var request = new MockHttpServletRequest(new MockServletContext());
        request.setMethod("GET");
        request.setRequestURI("/notfound");

        var response = new MockHttpServletResponse();
        response.setStatus(404);
        var filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(auditWriter).write(any(), any(), any(), any(), any(), eq(Result.NOT_FOUND), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Статус 4xx (405-499) маппится на Result.USER_ERROR")
    void test_doFilter_status4xx() throws ServletException, IOException {
        when(documentationResolver.isAuditable(any(), any())).thenReturn(true);

        var filter = new AuditFilter(documentationResolver, auditWriter);

        var request = new MockHttpServletRequest(new MockServletContext());
        request.setMethod("GET");
        request.setRequestURI("/badrequest");

        var response = new MockHttpServletResponse();
        response.setStatus(422);
        var filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(auditWriter).write(any(), any(), any(), any(), any(), eq(Result.USER_ERROR), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Статус 5xx маппится на Result.FAIL")
    void test_doFilter_status5xx() throws ServletException, IOException {
        when(documentationResolver.isAuditable(any(), any())).thenReturn(true);

        var filter = new AuditFilter(documentationResolver, auditWriter);

        var request = new MockHttpServletRequest(new MockServletContext());
        request.setMethod("GET");
        request.setRequestURI("/servererror");

        var response = new MockHttpServletResponse();
        response.setStatus(500);
        var filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(auditWriter).write(any(), any(), any(), any(), any(), eq(Result.FAIL), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Неизвестный статус возвращает null")
    void test_doFilter_unknownStatus() throws ServletException, IOException {
        when(documentationResolver.isAuditable(any(), any())).thenReturn(true);

        var filter = new AuditFilter(documentationResolver, auditWriter);

        var request = new MockHttpServletRequest(new MockServletContext());
        request.setMethod("GET");
        request.setRequestURI("/custom");

        var response = new MockHttpServletResponse();
        response.setStatus(100);
        var filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(auditWriter).write(any(), any(), any(), any(), any(), eq(null), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Авторизация из SecurityContext передаётся в аудитор")
    void test_doFilter_withAuthentication() throws ServletException, IOException {
        when(documentationResolver.isAuditable(any(), any())).thenReturn(true);

        var authentication = mock(Authentication.class);
        var securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        try {
            var filter = new AuditFilter(documentationResolver, auditWriter);

            var request = new MockHttpServletRequest(new MockServletContext());
            request.setMethod("GET");
            request.setRequestURI("/test");

            var response = new MockHttpServletResponse();
            var filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            verify(auditWriter).write(
                    anyString(), anyString(), eq(""), eq(new String[0]),
                    eq(authentication), any(), any(), any(), any(), anyString()
            );
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("Авторизация: null если SecurityContext пуст")
    void test_doFilter_noAuthentication() throws ServletException, IOException {
        when(documentationResolver.isAuditable(any(), any())).thenReturn(true);

        SecurityContextHolder.clearContext();

        try {
            var filter = new AuditFilter(documentationResolver, auditWriter);

            var request = new MockHttpServletRequest(new MockServletContext());
            request.setMethod("GET");
            request.setRequestURI("/test");

            var response = new MockHttpServletResponse();
            var filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            verify(auditWriter).write(
                    anyString(), anyString(), eq(""), eq(new String[0]),
                    eq(null), any(), any(), any(), any(), anyString()
            );
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    @DisplayName("IPv4 берётся из заголовка x-real-ip")
    void test_doFilter_ipv4FromXRealIp() throws ServletException, IOException {
        when(documentationResolver.isAuditable(any(), any())).thenReturn(true);

        var filter = new AuditFilter(documentationResolver, auditWriter);

        var request = new MockHttpServletRequest(new MockServletContext());
        request.setMethod("GET");
        request.setRequestURI("/test");
        request.addHeader("x-real-ip", "192.168.1.100");

        var response = new MockHttpServletResponse();
        var filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(auditWriter).write(
                anyString(), anyString(), eq(""), eq(new String[0]),
                any(), any(), any(), any(), any(), eq("192.168.1.100")
        );
    }

    @Test
    @DisplayName("IPv4 берётся из заголовка x-forwarded-for, если x-real-ip нет")
    void test_doFilter_ipv4FromXForwardedFor() throws ServletException, IOException {
        when(documentationResolver.isAuditable(any(), any())).thenReturn(true);

        var filter = new AuditFilter(documentationResolver, auditWriter);

        var request = new MockHttpServletRequest(new MockServletContext());
        request.setMethod("GET");
        request.setRequestURI("/test");
        request.addHeader("x-forwarded-for", "10.0.0.50");

        var response = new MockHttpServletResponse();
        var filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(auditWriter).write(
                anyString(), anyString(), eq(""), eq(new String[0]),
                any(), any(), any(), any(), any(), eq("10.0.0.50")
        );
    }

    @Test
    @DisplayName("IPv4 берётся из remoteAddr, если заголовков нет")
    void test_doFilter_ipv4FromRemoteAddr() throws ServletException, IOException {
        when(documentationResolver.isAuditable(any(), any())).thenReturn(true);

        var filter = new AuditFilter(documentationResolver, auditWriter);

        var request = new MockHttpServletRequest(new MockServletContext());
        request.setMethod("GET");
        request.setRequestURI("/test");
        request.setRemoteAddr("172.16.0.1");

        var response = new MockHttpServletResponse();
        var filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(auditWriter).write(
                anyString(), anyString(), eq(""), eq(new String[0]),
                any(), any(), any(), any(), any(), eq("172.16.0.1")
        );
    }

    @Test
    @DisplayName("x-real-ip приоритетнее x-forwarded-for")
    void test_doFilter_ipv4XRealIpPriority() throws ServletException, IOException {
        when(documentationResolver.isAuditable(any(), any())).thenReturn(true);

        var filter = new AuditFilter(documentationResolver, auditWriter);

        var request = new MockHttpServletRequest(new MockServletContext());
        request.setMethod("GET");
        request.setRequestURI("/test");
        request.addHeader("x-real-ip", "192.168.1.1");
        request.addHeader("x-forwarded-for", "10.0.0.1");

        var response = new MockHttpServletResponse();
        var filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(auditWriter).write(
                anyString(), anyString(), eq(""), eq(new String[0]),
                any(), any(), any(), any(), any(), eq("192.168.1.1")
        );
    }

    @Test
    @DisplayName("Пустой x-real-ip переходит к x-forwarded-for")
    void test_doFilter_ipv4EmptyXRealIp() throws ServletException, IOException {
        when(documentationResolver.isAuditable(any(), any())).thenReturn(true);

        var filter = new AuditFilter(documentationResolver, auditWriter);

        var request = new MockHttpServletRequest(new MockServletContext());
        request.setMethod("GET");
        request.setRequestURI("/test");
        request.addHeader("x-real-ip", "");
        request.addHeader("x-forwarded-for", "10.0.0.99");

        var response = new MockHttpServletResponse();
        var filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(auditWriter).write(
                anyString(), anyString(), eq(""), eq(new String[0]),
                any(), any(), any(), any(), any(), eq("10.0.0.99")
        );
    }

    @Test
    @DisplayName("Метод в описании в верхнем регистре")
    void test_doFilter_methodUpperCase() throws ServletException, IOException {
        when(documentationResolver.isAuditable(any(), any())).thenReturn(true);
        when(documentationResolver.getDescription(any(), any())).thenReturn(null);

        var filter = new AuditFilter(documentationResolver, auditWriter);

        var request = new MockHttpServletRequest(new MockServletContext());
        request.setMethod("patch");
        request.setRequestURI("/resource/1");

        var response = new MockHttpServletResponse();
        var filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        verify(auditWriter).write(
                anyString(),
                eq("PATCH /resource/1"),
                eq(""),
                eq(new String[0]),
                any(),
                any(),
                any(),
                any(),
                any(),
                anyString()
        );
    }

}
