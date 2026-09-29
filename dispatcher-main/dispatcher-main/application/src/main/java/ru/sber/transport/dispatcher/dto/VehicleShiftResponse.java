package ru.sber.transport.dispatcher.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.dispatcher.dto.enums.DriverSpecialityType;
import ru.sber.transport.dispatcher.dto.enums.VehicleType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VehicleShiftResponse {

    private Vehicle vehicle;

    private List<Shift> shifts = new ArrayList<>();

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Vehicle {

        private UUID id;

        private Model model;

        private String stateNumber;

        private VehicleType vehicleType;

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Model {

            private String brand;

            private String name;
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Shift {

        private UUID id;

        private LocalDateTime startDate;

        private LocalDateTime endDate;

        private boolean active;

        private Driver driver;

        private UUID rowId;

        private UUID ewbId;

        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        public static class Driver {

            private UUID id;

            private String humanReadableId;

            private String firstName;

            private String lastName;

            private String patronymic;

            private DriverSpecialityType driverSpeciality;

        }

    }

}
