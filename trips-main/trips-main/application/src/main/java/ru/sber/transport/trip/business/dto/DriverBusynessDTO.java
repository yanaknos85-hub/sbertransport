package ru.sber.transport.trip.business.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.trip.business.model.TripStatus;
import ru.sber.transport.trip.serializer.OffsetDateTimeSerializer;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DriverBusynessDTO {

    private List<BusynessData> busyness;
    private List<TripData> orders;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class BusynessData {
        private DriverData driver;
        private List<TripData> trips;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class DriverData {

        private UUID id;

        private String humanReadableId;

        @Override
        public boolean equals(Object object){
            if(object == this){
                return true;
            }
            if(object == null || object.getClass() != this.getClass()){
                return false;
            }
            var driverData = (DriverData) object;
            return driverData.id.equals(this.getId())
                    && driverData.humanReadableId.equals(this.getHumanReadableId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, humanReadableId);
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TripData {

        private UUID id;

        private String humanReadableId;

        private TripStatus status;

        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime factStartTime;

        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime factEndTime;

        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime expectedStartTime;

        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime expectedEndTime;

        @JsonSerialize(using = OffsetDateTimeSerializer.class)
        private OffsetDateTime driverProcessingTime;

        private boolean isPlanning;

        private VehicleData vehicle;

        private Address startAddress;

        private List<String> passenger;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VehicleData {

        private UUID id;

        private String stateNumber;

        private String vehicleType;

        private VehicleModelData model;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VehicleModelData {

        private String name;

        private String brand;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Address {

        private String name;

        private Double latitude;

        private Double longitude;
    }
}
