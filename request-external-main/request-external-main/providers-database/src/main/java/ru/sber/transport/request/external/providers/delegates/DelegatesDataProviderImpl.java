package ru.sber.transport.request.external.providers.delegates;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jooq.impl.DSL;
import ru.sber.database.repository.JooqRepository;
import ru.sber.transport.business.providers.DelegatesProvider;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.database.external_request.Tables;
import ru.sber.transport.database.external_request.tables.Delegates;
import ru.sber.transport.database.external_request.tables.records.DelegatesRecord;
import ru.sber.transport.request.external.model.Delegate;
import ru.sber.transport.request.external.model.Employee;

/**
 * Реализация провайдера делегатов
 */
@Slf4j
public class DelegatesDataProviderImpl implements DelegatesProvider, JooqRepository<Delegates, DelegatesRecord, UUID> {

    private final EmployeesProvider employeesProvider;

    public DelegatesDataProviderImpl(EmployeesProvider employeesProvider, DelegatesProvider additional) {
        this.employeesProvider = employeesProvider;
        CompletableFuture.runAsync(() -> {
            if (context().fetchCount(table()) == 0) {
                log.info("Delegates list is empty, syncing...");
                additional.getAll().parallelStream().forEach(this::save);
                log.info("Delegates list is empty, sync completed");
            }
        });
    }

    @Override
    public void save(@NotNull Delegate source) {
        if (source.active()) {
            final var delegate = employeesProvider.get(source.delegateId());
            final var supervisor = employeesProvider.get(source.supervisorId());
            context().insertInto(table())
                    .set(table().DELEGATE_ID, delegate.getId())
                    .set(table().SUPERVISOR_ID, supervisor.getId())
                    .set(table().START_DATE, source.startDate())
                    .set(table().END_DATE, source.endDate())
                    .onDuplicateKeyUpdate()
                    .setAllToExcluded()
                    .where(table().START_DATE.le(DSL.excluded(table().START_DATE)))
                    .execute();
        } else {
            context().deleteFrom(table())
                    .where(table().DELEGATE_ID.eq(source.delegateId()))
                    .and(table().SUPERVISOR_ID.eq(source.supervisorId()))
                    .and(table().START_DATE.le(source.startDate()))
                    .execute();
        }
    }

    @Override
    public List<Employee> get(UUID headId) {
        final var now = LocalDate.now();
        return context().select()
                .from(table())
                .where(table().SUPERVISOR_ID.eq(headId))
                .and(table().START_DATE.le(now))
                .and(table().END_DATE.ge(now))
                .fetch(it -> new Employee() {
                    @Override
                    public UUID getId() {
                        return it.get(table().DELEGATE_ID);
                    }

                    @Override
                    public UUID getDepartmentId() {
                        return null;
                    }

                    @Override
                    public UUID getOrganizationId() {
                        return null;
                    }

                    @Override
                    public String getLastName() {
                        return null;
                    }

                    @Override
                    public String getFirstName() {
                        return null;
                    }

                    @Override
                    public String getPatronymic() {
                        return null;
                    }

                    @Override
                    public UUID getPositionId() {
                        return null;
                    }

                    @Override
                    public String getPersonnelNumber() {
                        return null;
                    }

                    @Override
                    public String getCostCenter() {
                        return null;
                    }
                });
    }

    @Override
    public List<Delegate> getAll() {
        return findAll().stream().map(it ->
                        new Delegate() {
                            @Override
                            public UUID delegateId() {
                                return it.getDelegateId();
                            }

                            @Override
                            public UUID supervisorId() {
                                return it.getSupervisorId();
                            }

                            @Override
                            public LocalDate startDate() {
                                return it.getStartDate();
                            }

                            @Override
                            public LocalDate endDate() {
                                return it.getEndDate();
                            }

                            @Override
                            public boolean active() {
                                return true;
                            }
                        })
                .map(Delegate.class::cast).toList();
    }

    @Override
    public Delegates table() {
        return Tables.DELEGATES;
    }
}
