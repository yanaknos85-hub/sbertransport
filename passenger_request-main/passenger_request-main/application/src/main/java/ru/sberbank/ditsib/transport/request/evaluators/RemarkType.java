package ru.sberbank.ditsib.transport.request.evaluators;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        title = "Тип замечания",
        description = "Тип замечания/комментария в оценке сервиса"
)
public enum RemarkType {
    ADVANTAGES,
    DRAWBACKS
}
