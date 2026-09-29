package ru.sber.transport.dispatcher.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;

import ru.sber.transport.dispatcher.controller.ShiftConflictController;
import ru.sber.transport.dispatcher.dto.ShiftConflictResponseDTO;
import ru.sber.transport.dispatcher.dto.search.ShiftConflictSearchDTO;
import ru.sber.transport.dispatcher.service.ShiftConflictService;

/**
 * Реализация контроллера для работы с конфликтами смен.
 */
@RestController
@RequiredArgsConstructor
class ShiftConflictControllerImpl implements ShiftConflictController {

    private final ShiftConflictService shiftConflictService;

    @Override
    public Page<ShiftConflictResponseDTO> getAll(ShiftConflictSearchDTO searchDTO) {
        return shiftConflictService.getPage(searchDTO);
    }

    @Override
    public void delete(String routeId) {
        shiftConflictService.delete(routeId);
    }
}
