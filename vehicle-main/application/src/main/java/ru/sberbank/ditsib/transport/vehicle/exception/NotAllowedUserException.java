package ru.sberbank.ditsib.transport.vehicle.exception;

import java.util.Set;
import java.util.UUID;

public class NotAllowedUserException extends RuntimeException {
    
    public NotAllowedUserException(Set<UUID> organizationIds, UUID userId, UUID userOrganizationId) {
        super(String.format("Попытка получить доступ к данным организаций без необходимых прав, " +
                            "организации:%s, пользователь:%s, организация пользователя:%s", organizationIds, userId, userOrganizationId));
    }
}
