package ru.sber.transport.contractor.dto.cargo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * Dto for new cargo package.
 */
@Data
@NoArgsConstructor
@SuperBuilder
@Schema(title = "Вид упаковки. Новый", description = "Данные вида упаковки")
@AllArgsConstructor
public class NewCargoPackageDto {
    
    /**
     * Наименование вида груза
     */
    @NotBlank
    @Schema(description = "Наименование вида груза", maxLength = 128)
    @Size(max = 128)
    private String label;
    
    /**
     * Контрагент
     */
    @Schema(description = "Контрагент")
    private UUID contractor;
    
    /**
     * Cтоимость
     */
    @NotNull
    @Schema(description = "Cтоимость, руб")
    private Double cost;
    
    /**
     * Единицы измерения
     */
    @NotBlank
    @Schema(description = "Единицы измерения", maxLength = 128)
    @Size(max = 128)
    private String unit;
}
