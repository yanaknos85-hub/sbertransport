package ru.sberbank.ditsib.transport.tariff.util;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.qameta.allure.Feature;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@DisplayName("Проверка десериализатора")
@ActiveProfiles("test")
public class OffsetDateTimeDeserializerTest {
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    @DisplayName("Проверка десериализатора")
    @Test
    void test() throws JsonProcessingException {
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        var test = new TestClass(OffsetDateTime.parse("2024-11-14T02:59:59.999+03:00"));
        var serialized = mapper.writeValueAsString(test);
        assertThat(serialized).isEqualTo("{\"time\":\"2024-11-14T02:59:59.999+03:00\"}");
        var deserialized = mapper.readValue(serialized, TestClass.class);
        assertThat(mapper.writeValueAsString(deserialized)).isEqualTo("{\"time\":\"2024-11-14T02:59:59.999+03:00\"}");
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestClass {
        
        @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
        private OffsetDateTime time;
        
    }
    
}
