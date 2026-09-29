package ru.sber.transport.request.external.providers.available_classes;

import static ru.sber.transport.database.external_request.Tables.DEPARTMENT;
import static ru.sber.transport.database.external_request.Tables.DEPARTMENT_TRANSPORT_TYPE;
import static ru.sber.transport.database.external_request.Tables.ORGANIZATION_TRANSPORT_TYPE;
import static ru.sber.transport.database.external_request.Tables.POSITION_TRANSPORT_TYPE;

import java.time.Clock;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import lombok.RequiredArgsConstructor;
import org.jooq.impl.DSL;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.business.providers.AvailableClasses;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.database.external_request.tables.TransportTypes;
import ru.sber.transport.database.external_request.tables.records.TransportTypesRecord;

@RequiredArgsConstructor
@Transactional
public class AvailableClassesImpl implements AvailableClasses, JooqRepository<TransportTypes, TransportTypesRecord, Map<String, Object>> {

    private static final String AVAILABLE_CLASS = "YANDEX";
    private static final long CLEANUP_INTERVAL_MS = 60 * 60 * 1000;

    private final EmployeesProvider employeesProvider;

    private final boolean mvp;
    private final boolean allowEnabled;
    private final Clock clock;

    private final ConcurrentHashMap<UUID, Boolean> accessCache = new ConcurrentHashMap<>();
    private final ReentrantLock cleanupLock = new ReentrantLock();
    private volatile long lastCleanupTime;

    @Override
    public TransportTypes table() {
        return Tables.TRANSPORT_TYPES;
    }

    @Override
    public boolean exists(UUID organizationId, UUID positionId, @Deprecated UUID departmentId) {
        if (!mvp) {
            return context().fetchExists(context().select()
                    .from(table())
                    .leftJoin(ORGANIZATION_TRANSPORT_TYPE).on(table().TRANSPORT_TYPE.eq(ORGANIZATION_TRANSPORT_TYPE.TRANSPORT_TYPE))
                    .leftJoin(POSITION_TRANSPORT_TYPE).on(table().TRANSPORT_TYPE.eq(POSITION_TRANSPORT_TYPE.TRANSPORT_TYPE))
                    .where(ORGANIZATION_TRANSPORT_TYPE.ORGANIZATION_ID.eq(organizationId).or(POSITION_TRANSPORT_TYPE.POSITION_ID.eq(positionId)))
                    .and(table().TRANSPORT_TYPE.eq(AVAILABLE_CLASS)));
        } else {
            final var allowed = context().select(DEPARTMENT_TRANSPORT_TYPE.DEPARTMENT_ID)
                    .from(table())
                    .leftJoin(DEPARTMENT_TRANSPORT_TYPE).on(table().TRANSPORT_TYPE.eq(DEPARTMENT_TRANSPORT_TYPE.TRANSPORT_TYPE))
                    .where(table().TRANSPORT_TYPE.eq(AVAILABLE_CLASS))
                    .fetchInto(UUID.class);

            final var recursiveDepartment = DSL.name("recursive_department");
            final var recursiveDepartmentIdField = DSL.field(recursiveDepartment.append(DEPARTMENT.ID.getUnqualifiedName()), UUID.class);
            final var children = context().withRecursive(recursiveDepartment).as(
                            DSL.select(DSL.asterisk()).from(DEPARTMENT).where(DEPARTMENT.ID.in(allowed))
                                    .union(DSL.select(DEPARTMENT.asterisk()).from(DEPARTMENT).innerJoin(recursiveDepartment).on(recursiveDepartmentIdField.eq(DEPARTMENT.PARENT_ID)))
                    )
                    .select(recursiveDepartmentIdField).from(recursiveDepartment)
                    .fetchInto(UUID.class);
            return children.contains(departmentId);
        }
    }

    @Override
    public boolean allow(UUID user) {
        if (!allowEnabled) {
            return true;
        }
        tryCleanupExpiredEntries();

        var cachedResult = accessCache.get(user);
        if (cachedResult != null) {
            return cachedResult;
        }

        final var userData = employeesProvider.get(user);

        var isAllow = exists(userData.getOrganizationId(), userData.getPositionId(), userData.getDepartmentId());
        accessCache.put(user, isAllow);

        return isAllow;
    }

    private void tryCleanupExpiredEntries() {
        var currentTime = clock.millis();

        if (currentTime - lastCleanupTime < CLEANUP_INTERVAL_MS) {
            return;
        }

        if (cleanupLock.tryLock()) {
            try {
                if (currentTime - lastCleanupTime >= CLEANUP_INTERVAL_MS) {
                    accessCache.clear();
                    lastCleanupTime = currentTime;
                }
            } finally {
                cleanupLock.unlock();
            }
        }
    }
}
