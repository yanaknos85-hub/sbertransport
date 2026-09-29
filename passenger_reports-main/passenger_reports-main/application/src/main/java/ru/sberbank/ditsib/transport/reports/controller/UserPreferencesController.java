package ru.sberbank.ditsib.transport.reports.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.reports.dto.user.UserPreferencesRequestDTO;
import ru.sberbank.ditsib.transport.reports.dto.user.UserPreferencesResponseDTO;

import java.util.UUID;

@RequestMapping({"users/ui-preferences","users/ui-preferences/"})
@Tag(name = "Пользовательские настройки", description = "Набор операций для работы с пользовательскими настройками")
public interface UserPreferencesController {
    
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получить информацию о настройке UI")
    UserPreferencesResponseDTO getPreferencesByUserId(
            @RequestParam UUID userID,
            @RequestParam String nameForm);
    
    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Сохранение или обновление информации о настройке UI")
    void updateUserPreferences(@RequestBody UserPreferencesRequestDTO requestDTO);
}
