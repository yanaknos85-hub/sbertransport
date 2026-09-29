package ru.sber.transport.dispatcher.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Признак водителя", description = "Признак водителя")
@JsonPropertyOrder({"id", "contractorId"})
public class AttributeDTO extends NewAttributeDTO {
    
    /** ID признака */
    @NotNull
    @Schema(description = "ID признака")
    private UUID id;

}
