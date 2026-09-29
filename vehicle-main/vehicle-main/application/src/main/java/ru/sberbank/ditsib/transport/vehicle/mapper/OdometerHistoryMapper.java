package ru.sberbank.ditsib.transport.vehicle.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Setter;
import lombok.SneakyThrows;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sberbank.ditsib.transport.vehicle.database.model.OdometerHistory;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.OdometerHistoryValueMessage;

import java.time.LocalDateTime;
import java.util.Objects;

@Mapper(componentModel = "spring",
        imports = { LocalDateTime.class },
        builder = @Builder(disableBuilder = true),
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
public abstract class OdometerHistoryMapper {

    @Setter(onMethod_ = @Autowired)
    private ObjectMapper objectMapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "metaAttributes", ignore = true)
    public abstract OdometerHistory addIndicatorsHistoryDtoToOdometerHistory(OdometerHistoryValueMessage source);

    @AfterMapping
    @SneakyThrows
    protected void fillOutMetaAttributes(OdometerHistoryValueMessage source, @MappingTarget OdometerHistory target) {
        if (Objects.nonNull(source.metaAttributes())) {
            target.setMetaAttributes(objectMapper.writeValueAsString(source.metaAttributes()));
        }
    }

}
