package ru.sber.transport.contractor.dto.internal;

import ru.sber.transport.contractor.dto.enums.StaffSpeciality;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * DTO for creating staff
 * @param speciality speciality
 * @param firstName first name
 * @param lastName last name
 * @param patronymic patronymic
 * @param phone phone
 * @param email email
 * @param organizationId organization id
 * @param employeeId employee id
 * @param branchId branch id
 * @param ewbCreationPossibility ewb creation possibility
 * @param personnelNumber personnel number
 * @param attorneyNumber attorney number
 * @param issueDate issue date
 * @param expiryDate expiry date
 */
public record CreateStaffDto(

        StaffSpeciality speciality,

        String firstName,

        String lastName,

        String patronymic,

        String phone,

        String email,

        UUID organizationId,

        UUID employeeId,

        UUID branchId,

        boolean ewbCreationPossibility,

        String personnelNumber,

        String attorneyNumber,

        String issueDate,

        String expiryDate,

        String creationSystem,

        String snils,

        String tin,

        String driverLicenseNumber,

        Set<String> driverLicenses
) {
}
