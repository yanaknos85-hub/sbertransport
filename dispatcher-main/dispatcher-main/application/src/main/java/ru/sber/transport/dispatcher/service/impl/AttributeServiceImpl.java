package ru.sber.transport.dispatcher.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.dispatcher.dto.AttributeDTO;
import ru.sber.transport.dispatcher.dto.NewAttributeDTO;
import ru.sber.transport.dispatcher.mappers.AttributeMapper;
import ru.sber.transport.dispatcher.service.AttributeService;
import ru.sber.transport.dispatcher.database.dao.AttributeRepository;
import ru.sber.transport.dispatcher.database.dao.ContractorRepository;
import ru.sber.transport.dispatcher.database.model.ActiveStatus;
import ru.sber.transport.dispatcher.database.model.Attribute;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.exceptions.DuplicateDataException;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class AttributeServiceImpl implements AttributeService {

    private final AttributeRepository attributeRepository;

    private final ContractorRepository contractorRepository;

    private final AttributeMapper mapper;

    @Override
    public AttributeDTO add(UUID contractorId, NewAttributeDTO tag) {
        var contractor = contractorRepository.findById(contractorId).
                orElseThrow(() -> new EntityNotFoundException(Contractor.class, contractorId));

        var attributeOpt = attributeRepository.findByNameAndContractorIdAndAnyActive(tag.getName(), contractor.getId());

        Attribute newAttribute;

        if (attributeOpt.isPresent()) {
            var attribute = attributeOpt.get();
            if (ActiveStatus.ACTIVE.equals(attribute.getStatus())) {
                throw new DuplicateDataException(Attribute.class.getSimpleName(), Map.of("id", attribute.getId(), "[\"contractorId\", \"name\"]",
                        new ArrayList<>(List.of(contractorId, attribute.getName()))));
            } else {
                attribute.setStatus(ActiveStatus.ACTIVE);
                newAttribute = attribute;
            }
        } else {
            newAttribute = new Attribute();
            mapper.update(newAttribute, tag);
            newAttribute.setContractor(contractor);
        }

        newAttribute = attributeRepository.save(newAttribute);
        return mapper.toDto(newAttribute);
    }

    @Override
    public void edit(UUID contractorId, UUID attributeId, NewAttributeDTO attributeDTO) {
        var contractor = contractorRepository.findById(contractorId).
                orElseThrow(() -> new EntityNotFoundException(Contractor.class, contractorId));
        var attribute = attributeRepository.findByIdAndContractorId(attributeId, contractorId).
                orElseThrow(() -> new EntityNotFoundException(Attribute.class, attributeId));

        var exists = attributeRepository.findByNameAndContractorIdExclude(attributeDTO.getName(), contractorId, attribute.getId());
        if (exists.isPresent()) {
            throw new DuplicateDataException(Attribute.class.getSimpleName(), Map.of("id", exists.get().getId(), "[\"contractorId\", \"name\"]",
                    new ArrayList<>(List.of(contractorId, attribute.getName()))));
        }
        mapper.update(attribute, attributeDTO);
        attribute.setContractor(contractor);

        attributeRepository.save(attribute);
    }

    @Override
    public void delete(UUID attributeId, UUID contractorId) {
        if (!contractorRepository.existsById(contractorId)) {
            throw new EntityNotFoundException(Contractor.class, contractorId);
        }
        var attribute = attributeRepository.findByIdAndContractorId(attributeId, contractorId)
                .orElseThrow(() -> new EntityNotFoundException(Attribute.class, attributeId));

        attribute.setStatus(ActiveStatus.INACTIVE);
        attributeRepository.save(attribute);
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<AttributeDTO> addAll(UUID contractorId, List<NewAttributeDTO> newAttributeDTOs) {
        return newAttributeDTOs.stream().map(dto -> add(contractorId, dto)).toList();
    }

    @Override
    public AttributeDTO findByIdAndContractorId(UUID attributeId, UUID contractorId) {
        var attribute = attributeRepository.findByIdAndContractorId(attributeId, contractorId)
                .orElseThrow(() -> new EntityNotFoundException(Attribute.class, attributeId.toString()));

        return mapper.toDto(attribute);
    }

    @Override
    public List<AttributeDTO> findAllByContractorId(UUID contractorId) {
        if (!contractorRepository.existsById(contractorId)) {
            throw new EntityNotFoundException(Contractor.class, contractorId);
        }

        return attributeRepository.findAllByContractorId(contractorId).stream()
                .map(mapper::toDto).toList();
    }
}
