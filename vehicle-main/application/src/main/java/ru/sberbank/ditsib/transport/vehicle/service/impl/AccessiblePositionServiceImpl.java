package ru.sberbank.ditsib.transport.vehicle.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.vehicle.database.dao.AccessiblePositionRepository;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.AccessiblePositionDto;
import ru.sberbank.ditsib.transport.vehicle.mapper.AccessiblePositionMapper;
import ru.sberbank.ditsib.transport.vehicle.service.AccessiblePositionService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccessiblePositionServiceImpl implements AccessiblePositionService {
    private final AccessiblePositionRepository accessiblePositionRepository;
    private final AccessiblePositionMapper accessiblePositionMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AccessiblePositionDto> findAll() {
        return accessiblePositionRepository.findAll().stream()
                .map(accessiblePositionMapper::accessiblePositionToAccessiblePositionDto).toList();
    }
}
