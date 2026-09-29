package ru.sberbank.ditsib.transport.tariff.dto.contractor.transport;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.transport.tariff.util.OffsetDateTimeDeserializer;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime start;
    
    @JsonDeserialize(using = OffsetDateTimeDeserializer.class)
    private OffsetDateTime end;
    
}
