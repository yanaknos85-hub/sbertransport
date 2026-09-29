package ru.sber.transport.dispatcher.database.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.dispatcher.dto.VehicleAdditional;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaxiVehicleData implements VehicleAdditional {
}
