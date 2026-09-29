package ru.sberbank.ditsib.transport.reports.utils;

import lombok.experimental.UtilityClass;
import org.jboss.logging.MDC;

import java.util.Map;

@UtilityClass
public class LogUtils {
    public Map<String, Object> getStringMap() {
        return MDC.getMap();
    }

    public Map<String, Object> getOverriddenStringMap(Map<String, Object> overrideMap) {
        overrideMap.forEach(MDC::put);
        return getStringMap();
    }
}
