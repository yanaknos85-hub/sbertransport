package ru.sber.transport.telemechanic.dto.ewb.telemech_out;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ru.sber.transport.telemechanic.common.EwbTitleType;

@Schema(name = "TelemechOutTitleSendResponse", title = "Ответ на отправку подписанного титула допуска на линию")
public record TelemechOutTitleSendResponse(
        @NotNull(message = "Следующий титул не должен быть пустым")
        @Schema(description = "Следующий титул", requiredMode = Schema.RequiredMode.REQUIRED)
        EwbTitleType nextTitleType
) {
}
