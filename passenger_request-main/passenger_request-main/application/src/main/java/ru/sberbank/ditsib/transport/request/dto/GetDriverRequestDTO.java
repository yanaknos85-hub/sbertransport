package ru.sberbank.ditsib.transport.request.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.request.database.model.driversData.Driver;

import java.util.UUID;

@JsonPropertyOrder({ "id" })
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Schema(title = "Водитель", description = "Данные водителя")
@SuperBuilder
public class GetDriverRequestDTO {
    
    /**
     * ID of driver
     */
    @Id
    @Schema(description = "Идентификатор водителя")
    private UUID driverId;
    
    /**
     * Personal car
     */
    @Schema(description = "Водитель")
    private Driver driver;
    
}
