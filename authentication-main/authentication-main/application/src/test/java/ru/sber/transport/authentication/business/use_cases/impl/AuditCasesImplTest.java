package ru.sber.transport.authentication.business.use_cases.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.authentication.business.dto.AccountDto;
import ru.sber.transport.authentication.business.dto.Action;
import ru.sber.transport.authentication.business.dto.AuditDto;
import ru.sber.transport.authentication.business.dto.Result;
import ru.sber.transport.authentication.business.exceptions.AccountNotFoundException;
import ru.sber.transport.authentication.business.providers.AccountProvider;
import ru.sber.transport.authentication.business.providers.AuditProvider;
import ru.sber.transport.authentication.business.providers.RefreshProvider;
import ru.sber.transport.authentication.business.use_cases.AuditCases;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_authentication")
@DisplayName("Проверка кейсов аудита")
class AuditCasesImplTest {
    
    private final AuditProvider auditProvider = mock(AuditProvider.class);
    
    private final AccountProvider accountProvider = mock(AccountProvider.class);
    
    private final RefreshProvider refreshProvider = mock(RefreshProvider.class);
    
    private final AuditCases cases = new AuditCasesImpl(auditProvider, accountProvider, refreshProvider);
    
    @DisplayName("Проверка записи логина")
    @Test
    void test_login() throws AccountNotFoundException {
        var login = "Login";
        cases.login(login, null, null);
        
        var dataCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditProvider).login(dataCaptor.capture(), eq(null), eq(null));
        assertThat(dataCaptor.getValue()).isEqualTo(login);
    }
    
    @DisplayName("Проверка записи логина. Неверный логин")
    @Test
    void test_login_wrongLogin() {
        var login = "Login";
        cases.wrongLogin(login);
        
        var dataCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditProvider).wrongLogin(dataCaptor.capture());
        assertThat(dataCaptor.getValue()).isEqualTo(login);
    }
    
    @DisplayName("Проверка записи логина. Неверный пароль")
    @Test
    void test_login_wrongPassword() {
        var login = "Login";
        cases.wrongPassword(login);
        
        var dataCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditProvider).wrongPassword(dataCaptor.capture());
        assertThat(dataCaptor.getValue()).isEqualTo(login);
    }
    
    @DisplayName("Проверка записи логаута")
    @Test
    void test_logout() {
        var userId = UUID.randomUUID();
        var login = "Login";
    
        var account = AccountDto.builder().id(userId).login(login).build();
    
        when(accountProvider.get(userId)).thenReturn(Optional.of(account));
        
        cases.logout(userId);
        
        var dataCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditProvider).logout(dataCaptor.capture());
        assertThat(dataCaptor.getValue()).isEqualTo(login);
    }
    
    @DisplayName("Проверка записи обновления")
    @Test
    void test_refresh() throws AccountNotFoundException {
        var refresh = UUID.randomUUID().toString();
        var login = "Login";
    
        var account = AccountDto.builder().login(login).build();
    
        when(refreshProvider.getAccount(refresh)).thenReturn(Optional.of(account));
        
        cases.refresh(refresh, null, null);
        
        var dataCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditProvider).refresh(dataCaptor.capture(), eq(null), eq(null), any());
        assertThat(dataCaptor.getValue()).isEqualTo(login);
    }
    
    @DisplayName("Проверка записи обновления. Истек")
    @Test
    void test_refresh_expired() {
        var refresh = UUID.randomUUID().toString();
        var login = "Login";
    
        var account = AccountDto.builder().login(login).build();
    
        when(refreshProvider.getAccount(refresh)).thenReturn(Optional.of(account));
        
        cases.refreshExpired(refresh);
        
        var dataCaptor = ArgumentCaptor.forClass(String.class);
        verify(auditProvider).refreshExpired(dataCaptor.capture(), any());
        assertThat(dataCaptor.getValue()).isEqualTo(login);
    }
    
    @DisplayName("Получение записей")
    @Test
    void test_getRecords() {
        var page = 0;
        var size = 20;
    
        var audits = new ArrayList<AuditDto>();
        for (var i = 0; i < 20; i++) {
            var record = new AuditDto();
            record.setResult(Result.values()[i % Result.values().length]);
            record.setAction(Action.values()[i % Action.values().length]);
            record.setTimestamp(LocalDateTime.now());
            record.setLogin("Login" + i);
            audits.add(record);
        }
        
        when(auditProvider.getRecords(page, size)).thenReturn(new PageImpl<>(audits));
        
        var actualList = cases.get(size, page);
        
        assertThat(actualList.getTotalElements()).isEqualTo(size);
        for (var i = 0; i < size; i++) {
            var actual = actualList.toList().get(i);
            var expected = audits.get(i);
            
            assertThat(actual.getAction()).isEqualTo(expected.getAction());
            assertThat(actual.getLogin()).isEqualTo(expected.getLogin());
            assertThat(actual.getTimestamp()).isEqualTo(expected.getTimestamp());
            assertThat(actual.getResult()).isEqualTo(expected.getResult());
        }
    }
    
}