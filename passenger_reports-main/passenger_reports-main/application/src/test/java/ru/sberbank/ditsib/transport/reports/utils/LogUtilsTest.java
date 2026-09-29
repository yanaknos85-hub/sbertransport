package ru.sberbank.ditsib.transport.reports.utils;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import ru.sberbank.ditsib.transport.logging.model.LogField;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LogUtilsTest {

    @Test
    void getStringMap() {
        var traceId = UUID.randomUUID().toString();
        MDC.clear();
        MDC.put(LogField.TRACE_ID.getKey(), traceId);
        assertThat(LogUtils.getStringMap())
                .usingRecursiveComparison()
                .isEqualTo(Map.of(LogField.TRACE_ID.getKey(), traceId));
    }

    @Test
    void getOverriddenStringMap() {
        var traceId = UUID.randomUUID().toString();
        MDC.clear();
        MDC.put(LogField.TRACE_ID.getKey(), traceId);
        var key1 = Instancio.create(String.class);
        var key2 = Instancio.create(String.class);
        var value1 = Instancio.create(String.class);
        var value2 = Instancio.create(String.class);
        var overrideMap = new HashMap<String, Object>();
        overrideMap.put(key1, value1);
        overrideMap.put(key2, value2);
        var expected = new HashMap<String, Object>();
        expected.put(LogField.TRACE_ID.getKey(), traceId);
        expected.put(key1, value1);
        expected.put(key2, value2);
        assertThat(LogUtils.getOverriddenStringMap(overrideMap))
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
}