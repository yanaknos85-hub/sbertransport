package ru.sber.transport.contractor.dto.internal;

import java.util.UUID;

/**
 * DTO для создания филиала
 * @param name - название филиала
 * @param departmentId - id подразделения
 * @param organizationId - id организации
 */
public record CreateBranchDto(

        String name,

        UUID departmentId,

        UUID organizationId,

        Integer vehicleCountNorm

){}
