package ru.sber.transport.telemechanic.service;

import java.util.UUID;

public interface DepartmentTimeZoneService {
    
    String getTimeZoneByDepartmentId(UUID departmentId);
}
