package ru.sberbank.ditsib.transport.request.dto.carsharing;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Schema(title = "Текстовые поля заявки на подключение к корп.каршерингу",
        description = "Поле до ФИО разбито на три части. Вторая часть - значение КС - берется из настроек")
public class UpdateCarsharingJoinRequestTextDTO {
    
    @Schema(description = "Первая часть текстового поля до ФИО (до значения КС). Значение КС берется из настроек",
            required = true)
    @NotBlank
    private String beforeFioFirstPart;
    
    @Schema(description = "Третья часть текстового поля до ФИО (после значения КС). Значение КС берется из настроек",
            required = true)
    @NotBlank
    private String beforeFioThirdPart;
    
    @Schema(description = "Текст после номера телефона", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String afterPhone;
    
    @Schema(description = "Текст после почты", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String afterEmail;
    
    @Schema(description = "Текст после списка корп.каршерингов", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String afterCarsharings;
    
    @Schema(description = "Текст чекбокса \"Согласие с Памяткой П-144\"", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String withRulesP144Agree;
    
    @Schema(description = "Текст чекбокса \"Согласие на обработку персональных данных\"", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String withPersonalDataAgree;
}
