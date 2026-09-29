package ru.sber.transport.contractor.dto.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GetStaffDto {

    @NotNull
    @Schema(description = "Идентификатор организации")
    private UUID organizationId;

    @NotNull
    @Schema(description = "Специальность сотрудника")
    private Speciality speciality;

    @Schema(description = "Номер страницы")
    private int page = 0;

    @Schema(description = "Размер страницы")
    private int size = 20;

    /**
     * Специальности сотрудников
     */
    public enum Speciality {

        /**
         * Водитель
         */
        DRIVER,

        /**
         * Диспетчер
         */
        DISPATCHER
    }

}
