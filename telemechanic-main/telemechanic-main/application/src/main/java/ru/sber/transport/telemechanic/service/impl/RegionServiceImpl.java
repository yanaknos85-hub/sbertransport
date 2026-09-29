package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.telemechanic.database.dao.RegionRepository;
import ru.sber.transport.telemechanic.dto.RegionDto;
import ru.sber.transport.telemechanic.mapper.RegionMapper;
import ru.sber.transport.telemechanic.service.RegionService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;
    private final RegionMapper mapper;

    @Override
    public List<RegionDto> getRegionList() {
        return regionRepository.findAll()
                .stream()
                .map(mapper::mapRegionToDto)
                .toList();
    }
}
