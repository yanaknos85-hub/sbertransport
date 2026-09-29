package ru.sberbank.ditsib.transport.reports.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@JsonPropertyOrder({ "id" })
@Schema(
        title = "Краткая информация об импортированном реестре поездок на такси от контрагента",
        description = "Краткая информация об импортированном реестре поездок на такси от контрагента"
)
public class TaxiTripRegistryShortWithoutContractorDTO {
    
    @Schema(description = "ID реестра")
    private UUID id;
    
    @Schema(description = "Валидность всего реестра. " +
                          "Определяется по результатам прохождения проверок каждой из строк реестра")
    private Boolean valid;
    
    @Schema(description = "Дата (месяц и год) реестра")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    @Schema(description = "Количество строк реестра")
    private Integer stringsQnt;
    
    @Schema(description = "Полное имя сохраненного файла " )
    private String uploadFileFullName;
}
