package ru.sber.transport.telemechanic.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Department ids not in organization")
public class DepartmentNotInOrganizationException extends BusinessException {
    
    public static final String MSG_FORMAT = """
                                            Подразделения не найдены в организации с id:%s, id подразделений:%s
                                            """;
    
    public DepartmentNotInOrganizationException(UUID organizationId, Set<UUID> departmentIdSet) {
        super(String.format(MSG_FORMAT,
                            organizationId.toString(),
                            departmentIdSet.stream()
                                    .map(UUID::toString)
                                    .collect(Collectors.joining(", "))));
    }
}
