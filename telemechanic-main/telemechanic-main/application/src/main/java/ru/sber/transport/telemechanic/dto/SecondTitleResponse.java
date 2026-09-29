package ru.sber.transport.telemechanic.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

@Schema(name = "SecondTitleResponse", title = "Ответ на запрос формирования второго титула")
public record SecondTitleResponse(
        @NotBlank
        @Schema(description = "Имя файла", requiredMode = REQUIRED)
        String fileName,
        @Schema(description = "Сформированный файл", requiredMode = REQUIRED)
        byte[] content,
        @Schema(description = "Данные о медике", requiredMode = REQUIRED)
        MedicInfo medicInfo,
        @Schema(description = "Данные о лицензии", requiredMode = REQUIRED)
        MedicalLicenseInfo medicalLicenseInfo
) {
    
    @Schema(name = "SecondTitleResponse.MedicInfo", title = "Данные о медике")
    public record MedicInfo(
            @Schema(description = "Имя", requiredMode = REQUIRED)
            String firstName,
            @Schema(description = "Фамилия", requiredMode = REQUIRED)
            String lastName,
            @Schema(description = "Отчество", requiredMode = REQUIRED)
            String patronymic,
            @Schema(description = "Должность", requiredMode = REQUIRED)
            String position,
            @Schema(description = "Название организации", requiredMode = REQUIRED)
            String organizationName
    ) {}
    
    @Schema(name = "SecondTitleResponse.MedicalLicenseInfo", title = "Данные о лицензии")
    public record MedicalLicenseInfo(
            @Schema(description = "Серия", requiredMode = REQUIRED)
            String series,
            @Schema(description = "Номер", requiredMode = REQUIRED)
            String number,
            @Schema(description = "Дата выдачи", requiredMode = REQUIRED)
            LocalDate issueDate,
            @Schema(description = "Дата окончания срока действия", requiredMode = REQUIRED)
            LocalDate expiryDate
    ) {}
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        var that = (SecondTitleResponse) o;
        return fileName.equals(that.fileName) && Arrays.equals(content, that.content);
    }
    
    @Override
    public int hashCode() {
        int result = Objects.hash(fileName);
        result = 31 * result + Arrays.hashCode(content);
        return result;
    }
    
    @Override
    public String toString() {
        return "SecondTitleResponse{fileName='" + fileName + "', content=" + Arrays.toString(content) + "}";
    }
}
