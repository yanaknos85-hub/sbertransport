package ru.sberbank.ditsib.transport.role.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Объект с данными роли.
 */
@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
@Schema(title = "Роль", description = "Описание роли")
public class RoleDto {
    
    @NotBlank
    @Pattern(regexp = "(ROLE|role)_[A-Za-z_]+")
    @Schema(description = "Код роли")
    private String code;
    
    @NotBlank
    @Size(min = 3)
    @Schema(description = "Название")
    private String name;
    
    @Schema(description = "Описание")
    private String description;

    @Singular("scope")
    @Schema(description = "Области видимости ролей для назначения по-умолчанию")
    private List<Scope> scopes;

    @Builder.Default
    @Schema(
        description = "Кто может использовать эту роль - внешний или внутренний пользователь",
        defaultValue = "[\"EXTERNAL\", \"INTERNAL\"]")
    private List<ExeclusiveUsing> exclusive = List.of(ExeclusiveUsing.EXTERNAL, ExeclusiveUsing.INTERNAL);
    
    @JsonProperty("data_master")
    @Schema(description = "Роль является ролью мастер-данных", defaultValue = "false")
    private boolean dataMaster;
    
}
