package ru.sber.transport.audit.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.transport.audit.service.DocumentationResolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Проверка получателя документации")
class DocumentationResolverImplTest {

    private final DocumentationResolver resolver = new DocumentationResolverImpl() {{
        init();
    }};

    @Test
    @DisplayName("Получение доков")
    void test_docs() throws InterruptedException {
        assertThat(resolver.getDescription("GET", "/")).isEqualTo("Get test");
        assertThat(resolver.getDescription("PUT", "/")).isNull();
        assertThat(resolver.getDescription("DELETE", "/")).isEqualTo("Delete test");
        assertThat(resolver.getDescription("GET", "/request/")).isEqualTo("Multiple operations test");
        assertThat(resolver.getDescription("PUT", "/request/")).isEqualTo("Multiple operations test");

        assertThat(resolver.getDescription("GET", "/mapping/")).isEqualTo("Get test");
        assertThat(resolver.getDescription("PUT", "/mapping/")).isNull();
        assertThat(resolver.getDescription("DELETE", "/mapping/")).isEqualTo("Delete test");
        assertThat(resolver.getDescription("GET", "/mapping/request/")).isEqualTo("Multiple operations test");
        assertThat(resolver.getDescription("PUT", "/mapping/request/")).isEqualTo("Multiple operations test");

        assertThat(resolver.getDescription("POST", "/interfaced/")).isNull();
        assertThat(resolver.getDescription("PATCH", "/interfaced/")).isEqualTo("Interfaced patch test");
        assertThat(resolver.getDescription("GET", "/interfaced/first/")).isEqualTo("Interfaced et request test");
        assertThat(resolver.getDescription("GET", "/interfaced/second/")).isEqualTo("Interfaced et request test");
    }

}