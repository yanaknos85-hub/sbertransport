package ru.sberbank.ditsib.transport.vehicle.helper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.vehicle.dto.FilterValue;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class VehicleFiltersUtil {
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public List<FilterValue> parseFilterValues(String filterValuesAsString) {
        if (Strings.isBlank(filterValuesAsString)) {
            return List.of();
        }
        var typeRef =  new TypeReference<List<FilterValue>>() {};
        return objectMapper.readValue(filterValuesAsString, typeRef);
    }

    @SneakyThrows
    public List<Integer> parseInts(String intsAsString) {
        if (Strings.isBlank(intsAsString)) {
            return List.of();
        }
        var typeRef =  new TypeReference<List<Integer>>() {};
        return objectMapper.readValue(intsAsString, typeRef);
    }

    @SneakyThrows
    public List<BigDecimal> parseBigDecimals(String bigdecimalsAsString) {
        if (Strings.isBlank(bigdecimalsAsString)) {
            return List.of();
        }
        var typeRef =  new TypeReference<List<BigDecimal>>() {};
        return objectMapper.readValue(bigdecimalsAsString, typeRef);
    }

    @SneakyThrows
    public List<String> parseStrings(String strings) {
        if (Strings.isBlank(strings)) {
            return List.of();
        }
        var typeRef =  new TypeReference<List<String>>() {};
        return objectMapper.readValue(strings, typeRef);
    }

}
