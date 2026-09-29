package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.telemechanic.database.dao.DepartmentTimeZoneRepository;
import ru.sber.transport.telemechanic.exception.DepartmentTimeZoneException;
import ru.sber.transport.telemechanic.service.DepartmentTimeZoneService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DepartmentTimeZoneServiceImpl implements DepartmentTimeZoneService {
    
    private final DepartmentTimeZoneRepository departmentTimeZoneRepository;
    
    private static final String TIME_ZONE_PATTERN = "^UTC[+-](0\\d|1\\d|2[0-3]):[0-5]\\d$";
    
    @Override
    public String getTimeZoneByDepartmentId(UUID departmentId) {
        if (departmentId == null) {
            throw new DepartmentTimeZoneException("departmentId не может быть null");
        }
        
        var timeZones = departmentTimeZoneRepository.findByDepartmentIdWithHierarchy(departmentId);
        
        if (timeZones == null || timeZones.isEmpty()) {
            throw new DepartmentTimeZoneException("Часовой пояс не найден для подразделения с id: %s", departmentId);
        }
        
        var departmentTimeZone = timeZones.getFirst();
        validateTimeZonePattern(departmentTimeZone, departmentId);
        
        return departmentTimeZone;
    }
    
    public void validateTimeZonePattern(String timeZone, UUID departmentId) {
        if (!timeZone.matches(TIME_ZONE_PATTERN)) {
            throw new DepartmentTimeZoneException("Часовой пояс для подразделения с id: %s не соответствует шаблону UTC+HH:mm", departmentId);
        }
    }
}
