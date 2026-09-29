package ru.sber.transport.dispatcher.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.dispatcher.dto.AttributeDTO;
import ru.sber.transport.dispatcher.dto.NewAttributeDTO;
import ru.sber.transport.dispatcher.service.AttributeService;
import ru.sber.transport.dispatcher.controller.DriverAttributeController;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class DriverAttributeControllerImpl implements DriverAttributeController {

    private final AttributeService attributeService;

    @Override
    public List<AttributeDTO> add(UUID contractorId, @Valid List<NewAttributeDTO> tagDto) {
        return attributeService.addAll(contractorId, tagDto);
    }

    @Override
    public void edit(UUID contractorId, UUID tagId, @Valid NewAttributeDTO tagDto) {
        attributeService.edit(contractorId, tagId, tagDto);
    }

    @Override
    public void delete(UUID contractorId, UUID tagId) {
        attributeService.delete(tagId, contractorId);
    }

    @Override
    public AttributeDTO get(UUID contractorId, UUID tagId) {
        return attributeService.findByIdAndContractorId(tagId, contractorId);
    }

    @Override
    public List<AttributeDTO> getAll(UUID contractorId) {
        return attributeService.findAllByContractorId(contractorId);
    }

}
