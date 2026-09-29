package ru.sber.transport.request.external.providers.department;

import static ru.sber.transport.database.external_request.enums.DepartmentStatus.ACTIVE;
import static ru.sber.transport.database.external_request.enums.DepartmentStatus.INACTIVE;

import ru.sber.transport.database.external_request.enums.DepartmentStatus;

public final class DepartmentStatusMapper {

    private DepartmentStatusMapper() {
        throw new UnsupportedOperationException();
    }

    public static DepartmentStatus toJooq(ru.sber.transport.request.external.model.DepartmentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status is null");
        }
        return switch (status) {
            case ACTIVE -> ACTIVE;
            case INACTIVE -> INACTIVE;
        };
    }
}