package ru.sberbank.ditsib.transport.request.controller.personal;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sberbank.ditsib.transport.request.dto.personal.PersonalTransportSplitCheckRqDTO;

@Tag(
        name = "Контроллер для проверки заявок на личный транспорт",
        description = "Набор операций для проверки заявок на личный транспорт"
)
@RequestMapping("/personal-transport/checks")
@Validated
public interface PersonalTransportRequestCheckController {

    @Operation(
            summary = "Проверк поездки на дробление",
            description = "Проверяет, не является ли поездка дроблением",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Данные для проверки дробления поездки",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PersonalTransportSplitCheckRqDTO.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешная проверка"
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Некорректные данные в запросе",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "Поездка является дроблением",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Внутренняя ошибка сервера",
                            content = @Content
                    )
            }
    )
    @PostMapping("/split")
    void splitCheck(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Запрос на проверку дробления поездки",
                    required = true
            )
            @Valid @RequestBody PersonalTransportSplitCheckRqDTO request,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
    );
}