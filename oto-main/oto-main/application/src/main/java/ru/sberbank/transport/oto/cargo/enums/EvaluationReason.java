package ru.sberbank.transport.oto.cargo.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(title = "Причины оценки", description = "Дополнительные причины оценки")
public enum EvaluationReason {
    
    INCONVENIENT_PICKUP_LOCATION,
    WRINKLED_OR_TORN_PACKAGING,
    RUDENESS,
    LATE,
    FAST_SHIPPING,
    POLITENESS
}