package ru.sber.transport.authentication.business.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "OwnershipProofDto")
public class OwnershipProofDto {

    @NotBlank
    @Schema(description = "Логин")
    private String login;

    @Schema(description = "Канал отправки сообщения")
    private SendingChannel channel = SendingChannel.EMAIL;

}
