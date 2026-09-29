package ru.sber.transport.dispatcher.dto;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import ru.sber.transport.dispatcher.database.model.CargoVehicleData;
import ru.sber.transport.dispatcher.database.model.TaxiVehicleData;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class VehicleAdditionalDeserialize extends JsonDeserializer<VehicleAdditional> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public VehicleAdditional deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        var valueMap = getValueMap(jsonParser);
        try {
            return objectMapper.convertValue(valueMap, TaxiVehicleData.class);
        } catch (Exception e){
            return objectMapper.convertValue(valueMap, CargoVehicleData.class);
        }
    }

    private Map<String, Object> getValueMap(JsonParser jsonParser) throws IOException {
        var valueMap = new HashMap<String, Object>();
        var fields = Arrays.stream(VehicleAdditional.Fields.values()).map(VehicleAdditional.Fields::getFieldName).collect(Collectors.toList());
        while (jsonParser.getCurrentName()!=null){
            if(fields.contains(jsonParser.getCurrentName())) {
                valueMap.put(jsonParser.getCurrentName(), jsonParser.getValueAsString());
            }
            jsonParser.nextValue();
        }
        return valueMap;
    }

}
