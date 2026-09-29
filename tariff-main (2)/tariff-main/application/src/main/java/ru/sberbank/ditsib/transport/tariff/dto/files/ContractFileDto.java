package ru.sberbank.ditsib.transport.tariff.dto.files;

import lombok.Getter;
import lombok.Setter;
import ru.sber.transport.spreadsheet.annotation.NullRender;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Getter
@Setter
public class ContractFileDto {
    
    private String serviceType;
    
    @NotBlank
    @NullRender
    private String region;
    
    @NotBlank
    private String transportType;
    
    @NotNull
    @NullRender("Не найден")
    private String contractor;
    
    private Long sum;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private boolean active = true;
    
    @NotBlank
    private String contractNumber;
    
    private String uvhd;
    
    private boolean includeVat = false;
    
    private Integer vatValue;
    
    @NotBlank
    private String organization;
}
