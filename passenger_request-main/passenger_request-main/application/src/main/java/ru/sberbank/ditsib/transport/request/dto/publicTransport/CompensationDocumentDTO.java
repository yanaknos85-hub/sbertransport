package ru.sberbank.ditsib.transport.request.dto.publicTransport;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * DTO with single point coordinates
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Документ, подтверждающий сумму компенсации", description = "Документ, подтверждающий сумму компенсации")
public class CompensationDocumentDTO {
    
    @Schema(description = "ID")
    @NotNull
    private UUID id;
    
    @Schema(description = "Папка загрузки")
    @NotNull
    private UUID folder;
    
    @Schema(description = "Имя файла (<имя>.<формат>)")
    @NotBlank
    private String fileName;
    
    @Schema(description = "Формат файла")
    @NotBlank
    private String fileFormat;
    
    @Schema(description = "Размер файла, байт")
    @NotNull @Min(0)
    private Integer fileSize;
}
