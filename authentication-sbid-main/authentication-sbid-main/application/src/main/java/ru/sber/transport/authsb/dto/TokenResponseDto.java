package ru.sber.transport.authsb.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder(toBuilder = true)
@Schema(title = "Долгосрочные токены")
public class TokenResponseDto {

    @Schema(title = "Токен для доступа к защищенным ресурсам API")
    private String accessToken;

    @Schema(title = "Токен для получения нового access_token после истечения срока его действия.")
    private String refreshToken;

    @Schema(title = "Время жизни access_token в секундах")
    private Integer accessExpiration;

    @Schema(title = "Время жизни refresh_token в секундах")
    private Integer refreshExpiration;
}
