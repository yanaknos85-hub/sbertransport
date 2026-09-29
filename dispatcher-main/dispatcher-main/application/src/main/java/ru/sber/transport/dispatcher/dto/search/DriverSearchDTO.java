package ru.sber.transport.dispatcher.dto.search;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import ru.sber.transport.dispatcher.converters.SpecialStringRemoveDeserializer;
import ru.sber.transport.dispatcher.dto.DriverLicenseDto;
import ru.sber.transport.dispatcher.dto.enums.DriverSpecialityType;
import ru.sberbank.ditsib.request.PageSortFilterParameters;

import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Schema(title = "Фильтры для поиска водителей")
public class DriverSearchDTO extends PageSortFilterParameters<DriverSearchParameters> {

    /**
     * Создать новый объект.
     */
    public DriverSearchDTO() {
        super(DriverSearchParameters.DRIVER_FULL_NAME);
    }

    @Schema(description = "Человекочитаемый идентификатор водителя", maxLength = 13, minLength = 3)
    @Size(max = 13, min = 3)
    private String driverHumanId;

    @Schema(description = "ФИО водителя", maxLength = 128, minLength = 3)
    @Pattern(regexp = "^[\\p{L}|\\s]*$")
    @Size(max = 128, min = 3)
    @JsonDeserialize(using = SpecialStringRemoveDeserializer.class)
    private String driverFullName;

    @Schema(description = "Активность")
    private Boolean isActive;

    @Schema(description = "Рейтинг", minimum= "0", maximum = "500")
    @Min(0)
    @Max(500)
    private Integer ratingFrom;

    @Schema(description = "Категории прав")
    private List<DriverLicenseDto> driverLicenses;

    @Schema(description = "Признак выхода на линию")
    private Boolean online;

    @Schema(description = "Специализация")
    private DriverSpecialityType driverSpeciality;

    @Schema(description = "Идентификатор автопарка")
    private UUID autoparkId;

    @Schema(description = "Табельный номер")
    private String personnelNumber;
}
