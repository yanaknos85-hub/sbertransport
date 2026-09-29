package ru.sber.transport.trips.cargo.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Поездка", description = "Список данных по завершенной поездке")
public class FinalInfoTripDto {

    private List<TripFactInfoDto> tripFactInfo;
}
