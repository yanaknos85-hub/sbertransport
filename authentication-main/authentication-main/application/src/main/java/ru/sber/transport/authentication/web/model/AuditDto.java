package ru.sber.transport.authentication.web.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import ru.sber.transport.authentication.business.dto.Action;
import ru.sber.transport.authentication.business.dto.Result;

import java.time.LocalDateTime;

@Schema(title = "Аудит", description = "Описание действия пользователя")
@Builder
@Getter
public class AuditDto {
    
    /**
     * Время операции.
     */
    @Schema(title = "Время операции")
    private final LocalDateTime timestamp;
    
    /**
     * Результат операции.
     */
    @Schema(title = "Результат операции", description = "WRONG_PASSWORD | WRONG_LOGIN | SUCCESS")
    private final Result result;
    
    /**
     * Операция.
     */
    @Schema(title = "Операция", description = "LOGOUT | LOGIN | RELOGIN")
    private final Action action;
    
    /**
     * Инициатор операции.
     */
    @Schema(title = "Инициатор", description = "Логин пользователя, совершившего операцию")
    private final String login;
}
