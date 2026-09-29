package ru.sber.transport.dispatcher.dto.files.shift;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ShiftFile {

    /**
     * Табельный номер водителя
     */
    @NotEmpty
    @Size(min = 1, max = 9)
    private String personnelNumber;

    /**
     * Регистрационный знак автомобиля
     */
    @NotEmpty
    @Pattern(regexp = "([А-Яа-яA-Za-z]\\d{3}[А-Яа-яA-Za-z]{2}\\d{2,3}|[А-Яа-яA-Za-z]{2}\\d{3}\\d{2,3})")
    private String stateNumber;

    /**
     * Дата начала смены
     */
    @NotNull
    @DateTimeFormat(pattern = "dd.MM.yyyy")
    private LocalDate startDate;

}
