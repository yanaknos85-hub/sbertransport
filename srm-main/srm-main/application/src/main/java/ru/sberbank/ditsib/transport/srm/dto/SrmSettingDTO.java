package ru.sberbank.ditsib.transport.srm.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.srm.config.SrmSettingNames;

/**
 * DTO for adding request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@SuperBuilder(toBuilder = true)
@Schema(title = "Настройки лимитов", description = "Настройки лимитов")
public class SrmSettingDTO {
    
    /**
     * Year
     */
    @Schema(description = "Ключ")
    private SrmSettingNames name;
    
    /**
     * Status
     */
    @Schema(description = "Значение")
    private String value;
}
