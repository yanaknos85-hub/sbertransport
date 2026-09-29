package ru.sber.transport.trips.cargo.business.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.trips.cargo.business.dto.IntegrationRequestDTO;
import ru.sber.transport.trips.cargo.business.dto.WaypointType;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record Waypoint(
        UUID id,
        double latitude,
        double longitude,
        int orderingIndex,
        String country,
        String region,
        String city,
        String street,
        String house,
        String building,
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Duration waitingTime,
        String address,
        List<Contact> contacts
) {
    public record Contact(
            ContactData contact,

            List<RouteRequest> requests
    ) {
    }

    public record ContactData(
            String fullName,

            String phone
    ) {
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RouteRequest {

        @Schema(description = "Человекочитаемый идентификатор")
        private String humanReadableId;

        @Schema(description = "Тип точки")
        private WaypointType type;

        @Schema(description = "Организация")
        private String organization;

        @Schema(description = "Груз")
        private List<CargoData> cargo;

        @Schema(description = "Количество грузчиков")
        private Integer loaders;

        @Schema(description = "Комментарий")
        private String comment;

        @Schema(description = "Упаковки")
        private List<Pack> pack;

        @Schema(description = "QR коды")
        private List<String> qrs = new ArrayList<>();
    }

    public record CargoData(

            Integer orderingIndex,

            String cargoName,

            Double weight,

            Double volume,

            Integer occupiedPlacesCount,

            Double height,

            Double length,

            Double width,

            boolean fragile
    ) {
    }

    public record Pack(
            String name,

            String unit,

            int count
    ) {
    }
}
