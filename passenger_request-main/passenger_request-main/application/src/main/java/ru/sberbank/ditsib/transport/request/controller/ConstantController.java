package ru.sberbank.ditsib.transport.request.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sberbank.ditsib.transport.request.dto.constant.TransportClassDTO;

import java.util.List;

@RequestMapping({"constant","constant/"})
@Tag(name = "Константы", description = "Набор операций для работы с константами")
public interface ConstantController {
    
    @GetMapping({"transport/taxi/class/","transport/taxi/class//"})
    @Operation(summary = "Список классов такси")
    List<TransportClassDTO> taxiClass();
    
    @GetMapping({"transport/transfer/class/","transport/transfer/class//"})
    @Operation(summary = "Список классов группового трансфера")
    List<TransportClassDTO> transferClass();
}
