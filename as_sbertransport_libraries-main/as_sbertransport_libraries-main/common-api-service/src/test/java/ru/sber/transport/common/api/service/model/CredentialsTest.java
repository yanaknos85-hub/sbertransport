package ru.sber.transport.common.api.service.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Проверка сохранения токена в каждой комбинации хост/логин")
class CredentialsTest {

    @Test
    @DisplayName("Одинаковый host, разный логин")
    void sameHostDiffLogins() {
        var cut = new Credentials();
        URI sameURI = URI.create("http://www.same.host.ru");
        var login1 = "login1";
        var login2 = "login2";
        var contractor1 = Contractor.builder().token("token1").build();
        var contractor2 = Contractor.builder().token("token2").build();
        cut.add(sameURI, login1, contractor1);
        cut.add(sameURI, login2, contractor2);

        var result = cut.get(sameURI, login1);
        assertEquals("token1", result.getTokenWithoutPrefix(), "Даже в случае одинакового host'а для каждого логина должен сохраняться/возвращаться токен для данного логина");

        result = cut.get(sameURI, login2);
        assertEquals("token2", result.getTokenWithoutPrefix(), "Даже в случае одинакового host'а для каждого логина должен сохраняться/возвращаться токен для данного логина");
    }

    @Test
    @DisplayName("Одинаковый логин, разный host")
    void sameLoginsDiffHosts() {
        var cut = new Credentials();
        URI URI1 = URI.create("http://www.same.host1.ru");
        URI URI2 = URI.create("http://www.same.host2.ru");
        var sameLogin = "samelogin";
        var contractor1 = Contractor.builder().token("token1").build();
        var contractor2 = Contractor.builder().token("token2").build();
        cut.add(URI1, sameLogin, contractor1);
        cut.add(URI2, sameLogin, contractor2);

        var result = cut.get(URI1, sameLogin);
        assertEquals("token1", result.getTokenWithoutPrefix(), "Даже в случае одинакового логина, для разных хостов должен сохраняться/возвращаться свой токен");

        result = cut.get(URI2, sameLogin);
        assertEquals("token2", result.getTokenWithoutPrefix(), "Даже в случае одинакового логина, для разных хостов должен сохраняться/возвращаться свой токен");
    }
}