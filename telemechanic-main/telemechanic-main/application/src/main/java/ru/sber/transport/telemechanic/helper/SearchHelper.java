package ru.sber.transport.telemechanic.helper;

import lombok.experimental.UtilityClass;
import ru.sber.transport.telemechanic.dto.DateRange;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class SearchHelper {
    public static LocalDateTime getDateRangeStart(DateRange dateRange) {
        return Objects.isNull(dateRange) ? null : dateRange.start();
    }
    
    public static LocalDateTime getDateRangeEnd(DateRange dateRange) {
        return Objects.isNull(dateRange) ? null : dateRange.end();
    }
    
    public static Set<String> getStatusSet(Set<TelemedicineStatus> statusSet) {
        return statusSet == null ? Collections.emptySet()
                                 : statusSet.stream()
                                            .map(TelemedicineStatus::name)
                                            .collect(Collectors.toSet());
    }
}
