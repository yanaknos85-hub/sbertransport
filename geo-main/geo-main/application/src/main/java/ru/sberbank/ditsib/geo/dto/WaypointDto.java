package ru.sberbank.ditsib.geo.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sberbank.ditsib.converters.DurationMillisConverter;
import ru.sberbank.ditsib.converters.MillisDurationConverter;

import java.time.Duration;

/**
 * Object for transferring data about waypoints.
 */
@Setter
@Getter
@NoArgsConstructor
@Schema(title = "Ключевая точка", description = "Ключевая точка маршрута")
public class WaypointDto extends AddressDto {
    
    
    /**
     * Waypoint waiting time.
     */
    @JsonDeserialize(using = MillisDurationConverter.class)
    @JsonSerialize(using = DurationMillisConverter.class)
    @Schema(description = "Время ожидания на точке")
    private Duration waitTime;

    /**
     * Create a new waypoint.
     *
     * @param address address.
     * @param waitTime waiting time ad the address.
     */
    public WaypointDto(
            AddressDto address,
            Duration waitTime
                      ) {
        super(address.getCountry(), address.getRegion(), address.getDistrict(), address.getCity(), address.getSettlement(),
                address.getLivingArea(), address.getPlace(), address.getStreet(),
              address.getHouse(), address.getLatitude(), address.getLongitude(), address.getAttributeGroups(),
                address.getNameEx(), address.getIsPaid(), address.getPoint(), address.getFullName(), address.getName(),
                address.getType(), address.getGeometry(), address.getObjectId(), address.getPurposeName());
        this.waitTime = waitTime;
    }
    
}
