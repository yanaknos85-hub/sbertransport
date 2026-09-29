package ru.sberbank.ditsib.transport.request.controller.carsharing;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.tariff.model.CalculatedDto;
import ru.sberbank.ditsib.transport.request.dto.carsharing.CarsharingJoinAndCalculatedDTO;
import ru.sberbank.ditsib.transport.request.dto.carsharing.GetCarsharingJoinRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.carsharing.NewCarsharingJoinRequestDTO;

import java.util.List;
import java.util.UUID;

/**
 * Контроллер Заявок на подключение к корп.каршерингу
 */
@RequestMapping({"carsharing-join-request","carsharing-join-request/"})
@Validated
@Tag(name = "Заявки на подключение к корп.каршерингу",
     description = "Создание, просмотр, согласование, отклонение заявок. Информация о подключении сотрудника")
public interface CarsharingJoinRequestController {
    
    /**
     * Запрос на получение всех подключенных и неподключенных корп.каршерингов для сотрудника
     * @param calculatedList список CalculatedDto для каршеринга, получаемый из расчета по тарифам
     * @param organizationId ID корп.клиента
     * @param employeeId ID сотрудника
     * @return список каршерингов с инфой о подключении и расчетах по тарифам
     */
    @PostMapping(path = {"{organizationId}/{employeeId}/joins","{organizationId}/{employeeId}/joins/"}, consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получить список подключений для сотрудника по списку расчетов по тарифам",
               description = "Содержит объединенный список подключенных и неподключенных каршерингов, а также данные " +
                             "расчета по тарифу. Служит для отображения и выбора каршеринга при создании заявки")
    List<CarsharingJoinAndCalculatedDTO> getCarsharingJoinsForEmployee(
            @Valid @RequestBody List<@Valid CalculatedDto> calculatedList,
            @PathVariable @NotNull UUID organizationId,
            @PathVariable @NotNull UUID employeeId);
    
    /**
     * Получить созданную заявку на подключение сотрудника или чистый бланк
     * @param organizationId ID корп.клиента
     * @param employeeId ID сотрудника
     * @return созданная заявка или чистый бланк
     */
    @GetMapping(path = {"{organizationId}/{employeeId}","{organizationId}/{employeeId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получить созданную заявку на подключение сотрудника или чистый бланк",
               description = "В бланке поля заполнены по умолчанию")
    GetCarsharingJoinRequestDTO getCreatedOrBlank(
            @PathVariable @NotNull UUID organizationId,
            @PathVariable @NotNull UUID employeeId);
    
    /**
     * Создать заявку на подключение сотрудника ко всем возможным корп.каршерингам
     * @param joinRequestDto данные заявки
     * @param organizationId ID корп.клиента
     * @param employeeId ID сотрудника
     * @return созданная заявка
     */
    @PostMapping(path = {"{organizationId}/{employeeId}","{organizationId}/{employeeId}/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Создать заявку на подключение сотрудника ко всем возможным корп.каршерингам",
               description = "Под возможными каршерингами подразумеваются доступные в регионе для данного " +
                             "корп.клиента каршеринги, к которым сотрудник еще не подключен. Заявка создается на " +
                             "основе бланка, сгенерированного в get-запросе")
    GetCarsharingJoinRequestDTO create(
            @Valid @RequestBody NewCarsharingJoinRequestDTO joinRequestDto,
            @PathVariable @NotNull UUID organizationId,
            @PathVariable @NotNull UUID employeeId);
    
    /**
     * Редактировать заявку на подключение сотрудника к корп.каршерингам
     * @param joinRequestDto данные для изменения
     * @param joinRequestId ID заявки
     */
    @PutMapping(path = {"{joinRequestId}","{joinRequestId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Редактировать заявку на подключение сотрудника к корп.каршерингам",
               description = "Список каршеринговых компаний")
    void update(
            @Valid @RequestBody NewCarsharingJoinRequestDTO joinRequestDto,
            @PathVariable @NotNull UUID joinRequestId);
    
    // todo выпилить DELETE после окончания разработки фронтами и тестирования
    /**
     * Удалить заявку на подключение сотрудника к корп.каршерингам. ТОЛЬКО В ЦЕЛЯХ РАЗРАБОТКИ
     * @param joinRequestId ID заявки
     */
    @DeleteMapping(path = {"{joinRequestId}","{joinRequestId}/"})
    @Operation(summary = "Удалить заявку. *DEPRECATED - endpoint только для разработки, в дальнейшем будет выпилен",
               description = "Удалить созданную заявку (только в целях разработки и тестирования)", deprecated = true)
    void delete(@PathVariable @NotNull UUID joinRequestId);
}
