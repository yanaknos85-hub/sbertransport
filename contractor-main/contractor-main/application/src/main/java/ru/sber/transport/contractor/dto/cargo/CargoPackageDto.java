package ru.sber.transport.contractor.dto.cargo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Dto for cargo package.
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@Schema(title = "Вид упаковки", description = "Данные вида упаковки")
public class CargoPackageDto extends NewCargoPackageDto {
    
    /**
     * Идентификатор
     */
    @Schema(description = "Идентификатор")
    @NotNull
    private UUID id;
}
