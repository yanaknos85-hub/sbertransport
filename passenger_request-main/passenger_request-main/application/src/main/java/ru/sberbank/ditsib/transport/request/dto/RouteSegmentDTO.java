package ru.sberbank.ditsib.transport.request.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.converters.DurationMillisConverter;
import ru.sberbank.ditsib.converters.MillisDurationConverter;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Data transfer object with data about route segment
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Перегон", description = "Участок маршрута между ключевых точек")
public class RouteSegmentDTO {
    
    /**
     * cost
     */
    @Min(0)
    @Schema(description = "Стоимость")
    private double cost;
    
    /**
     * distance
     */
    @Min(0)
    @Schema(description = "Дальность")
    private double distance;
    
    /**
     * time
     */
    @JsonSerialize(using = DurationMillisConverter.class)
    @JsonDeserialize(using = MillisDurationConverter.class)
    @Schema(description = "Время")
    private Duration time;
    
    /**
     * Coordinates of route segment.
     */
    @Size(min = 2)
    @Builder.Default
    @Schema(description = "Маршрутные точки")
    private final List<CoordinatesDTO> coordinates = new ArrayList<>();
    
    public @Min(0) double getCost() {
        return this.cost;
    }
    
    public @Min(0) double getDistance() {
        return this.distance;
    }
    
    public Duration getTime() {
        return this.time;
    }
    
    public @Size(min = 2) List<CoordinatesDTO> getCoordinates() {
        return this.coordinates;
    }
    
    public void setCost(@Min(0) double cost) {
        this.cost = cost;
    }
    
    public void setDistance(@Min(0) double distance) {
        this.distance = distance;
    }
    
    @JsonDeserialize(using = MillisDurationConverter.class)
    public void setTime(Duration time) {
        this.time = time;
    }
    
    public boolean equals(final Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof RouteSegmentDTO)) {
            return false;
        }
        final RouteSegmentDTO other = (RouteSegmentDTO) o;
        if (!other.canEqual((Object) this)) {
            return false;
        }
        if (Double.compare(this.getCost(), other.getCost()) != 0) {
            return false;
        }
        if (Double.compare(this.getDistance(), other.getDistance()) != 0) {
            return false;
        }
        final Object this$time = this.getTime();
        final Object other$time = other.getTime();
        if (this$time == null ? other$time != null : !this$time.equals(other$time)) {
            return false;
        }
        var this$coordinates = this.getCoordinates();
        var other$coordinates = other.getCoordinates();
        if (this$coordinates == null ? other$coordinates != null : !this$coordinates.equals(other$coordinates)) {
            return false;
        }
        return true;
    }
    
    protected boolean canEqual(final Object other) {
        return other instanceof RouteSegmentDTO;
    }
    
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final long $cost = Double.doubleToLongBits(this.getCost());
        result = result * PRIME + (int) ($cost >>> 32 ^ $cost);
        final long $distance = Double.doubleToLongBits(this.getDistance());
        result = result * PRIME + (int) ($distance >>> 32 ^ $distance);
        final Object $time = this.getTime();
        result = result * PRIME + ($time == null ? 43 : $time.hashCode());
        final Object $coordinates = this.getCoordinates();
        result = result * PRIME + ($coordinates == null ? 43 : $coordinates.hashCode());
        return result;
    }
    
    public String toString() {
        return "RouteSegmentDTO(cost=" + this.getCost() + ", distance=" + this.getDistance() + ", time=" + this.getTime() + ", coordinates=" +
               this.getCoordinates() + ")";
    }
}
