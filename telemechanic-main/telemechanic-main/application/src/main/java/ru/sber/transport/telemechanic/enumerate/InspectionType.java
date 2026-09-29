package ru.sber.transport.telemechanic.enumerate;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.EnumSet;
import java.util.Set;

/**
 * Вид осмотра
 */
@Getter
@RequiredArgsConstructor
@Schema(title = "Вид осмотра", description = "Вид осмотра")
public enum InspectionType {
    @Schema(description = "Медицинский")
    MEDIC,
    @Schema(description = "Технический")
    TECHNIC,
    @Schema(description = "Телемедицинский")
    TELEMEDIC;
    
    public static Set<InspectionType> getMedicineTypes() {
        return EnumSet.of(MEDIC, TELEMEDIC);
    }
}