package ru.sberbank.ditsib.transport.request.controller.carsharing;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sberbank.ditsib.transport.request.dto.carsharing.GetCarsharingJoinRequestTextDTO;
import ru.sberbank.ditsib.transport.request.dto.carsharing.UpdateCarsharingJoinRequestTextDTO;

import java.util.UUID;

/**
 * Контроллер Текстовых полей Заявок на подключение к корп.каршерингу
 */
@RequestMapping({"carsharing-join-request/text-fields","carsharing-join-request/text-fields/"})
@Validated
@Tag(name = "Текстовые поля Заявки на подключение к корп.каршерингу", description = "Просмотр, редактирование")
public interface CarsharingJoinRequestTextController {
    
    /**
     * Получить созданные текстовые поля заявки на подключение сотрудника к корп.каршерингам или создать по умолчанию
     * @param organizationId ID корп.клиента
     * @return текстовые поля заявки на подключение
     */
    @GetMapping(path = {"{organizationId}","{organizationId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получить текстовые поля заявки на подключение сотрудника к корп.каршерингам",
               description = "Текстовые поля заявки для отбражение на фронте")
    GetCarsharingJoinRequestTextDTO getCreatedTextFieldsOrDefaults(@PathVariable @NotNull UUID organizationId);
    
    /**
     * Сбросить текстовые поля заявки на подключение сотрудника к корп.каршерингам по умолчанию
     * @param organizationId ID корп.клиента
     * @return текстовые поля заявки на подключение по умолчанию
     */
    @PostMapping(path = {"{organizationId}/defaults","{organizationId}/defaults/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Сбросить текстовые поля заявки на подключение сотрудника к корп.каршерингам по умолчанию",
               description = "Текстовые поля заявки для отбражение на фронте")
    GetCarsharingJoinRequestTextDTO setToDefaults(@PathVariable @NotNull UUID organizationId);
    
    /**
     * Редактировать текстовые поля заявки на подключение сотрудника к корп.каршеринга
     * @param organizationId ID корп.клиента
     * @param updateTextDto данные для изменения
     */
    @PutMapping(path = {"{organizationId}","{organizationId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Редактировать текстовые поля заявки на подключение сотрудника к корп.каршерингам",
               description = "Текстовые поля заявки для отбражение на фронте")
    void updateTextFields(@Valid @RequestBody UpdateCarsharingJoinRequestTextDTO updateTextDto,
                          @PathVariable @NotNull UUID organizationId);
}
