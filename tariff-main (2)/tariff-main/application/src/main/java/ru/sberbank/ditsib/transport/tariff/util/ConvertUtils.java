package ru.sberbank.ditsib.transport.tariff.util;

import lombok.experimental.UtilityClass;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Optional;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

@UtilityClass
public class ConvertUtils {
    
    public static String setToStringArray(Collection<UUID> uuids) {
        return "{%s}".formatted(uuids.stream().map(UUID::toString).collect(Collectors.joining(",")));
    }
    
    public static ZoneOffset toOffset(ZoneId zoneId) {
        return Optional.ofNullable(zoneId)
                       .map(TimeZone::getTimeZone)
                       .map(TimeZone::getRawOffset)
                       .map(Duration::ofMillis)
                       .map(Duration::getSeconds)
                       .map(Number::intValue)
                       .map(ZoneOffset::ofTotalSeconds)
                       .orElse(null);
    }
    
}
