package ru.sber.transport.address.business.model;

import java.util.UUID;

/**
 * Business entity of employee.
 *
 * @param id ID of employee.
 * @param userId ID of employee user.
 * @param organizationId ID of organization.
 */
public record Employee(
    UUID id,
    UUID userId,
    UUID organizationId
) {
}
