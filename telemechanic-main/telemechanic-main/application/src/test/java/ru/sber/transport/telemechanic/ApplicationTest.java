package ru.sber.transport.telemechanic;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("Проверка запуска")
class ApplicationTest {
    
    @Test
    void testApplication() {
        var mockStatic = Mockito.mockStatic(SpringApplication.class);
        mockStatic.when((MockedStatic.Verification) SpringApplication.run(Application.class, new String[] {})).thenReturn(null);
        Application.main();
        assertNull(SpringApplication.run(Application.class));
        mockStatic.close();
    }
}