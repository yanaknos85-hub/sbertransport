package ru.sber.transport.trip.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.OffsetDateTime;

public class OffsetDateTimeDeserializerTest extends JsonDeserializer<OffsetDateTime> { //NOSONAR

    @Override
    public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
            throws IOException {
        String dateAsString = jsonParser.getText();
        if (dateAsString != null) {
            return OffsetDateTime.parse(dateAsString);
        } else return null;
    }
}
