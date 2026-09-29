package ru.sberbank.ditsib.transport.request.controller.carsharing;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sberbank.ditsib.transport.request.dto.GetRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.RequestCarsharingSearchDTO;

/**
 * Controller for working with requests.
 */
@RequestMapping
@Validated
@Tag(name = "Заявки", description = "Набор операций для работы с заявками по каршерингу")
public interface CarsharingRequestController {
    
    /**
     * Get all requests by carsharing search dto
     *
     * @return list of requests.
     */
    @PostMapping(value = {"carsharing_search","carsharing_search/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск", description = "Поиск заявок для каршеринга c пагинацией")
    Page<? extends GetRequestDTO> getRequestsByCarsharingSearchDTO(
            @RequestBody @Valid RequestCarsharingSearchDTO requestSearchDTO);
}
