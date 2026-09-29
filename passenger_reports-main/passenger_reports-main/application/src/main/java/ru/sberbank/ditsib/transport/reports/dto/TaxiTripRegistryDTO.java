package ru.sberbank.ditsib.transport.reports.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@JsonPropertyOrder({ "id", "isValid", "contractor", "date" })
@Schema(
        title = "Информация об импортированном реестре поездок на такси от контрагента",
        description = "Информация об импортированном реестре поездок на такси от контрагента"
)
@Builder
public class TaxiTripRegistryDTO {
    
    @Schema(description = "ID реестра")
    private UUID id;
    
    @Schema(description = "Валидность всего реестра. " +
                          "Определяется по результатам прохождения проверок каждой из строк реестра")
    private Boolean valid;
    
    @Schema(description = "Контрагент")
    private ContractorDTO contractor;
    
    @Schema(description = "Дата (месяц и год) реестра")
    private LocalDate date;
    
    @Schema(description = "Количество строк реестра")
    private Integer stringsQnt;
    
    @Schema(description = "Список ячеек с пустыми значениями")
    @Builder.Default
    private Set<String> blankCells = new HashSet<>();
    
    @Schema(description = "Список ячеек с ошибками - несоответствие типов данных таблицы и класса-шаблона, " +
                          "а также формулы/ошибки excel")
    @Builder.Default
    private Set<String> incorrectTypeCells = new HashSet<>();
    
    @Schema(description = "Полное имя сохраненного файла " )
    private String uploadFileFullName;
}
