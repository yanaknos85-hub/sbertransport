package ru.sber.transport.request.external.web.query;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import ru.sber.transport.business.providers.DepartmentsProvider;
import ru.sber.transport.request.external.mapper.DepartmentsMapper;
import ru.sber.transport.request.external.model.DepartmentStatus;
import ru.sber.transport.web.api.ExternalDepartmentsQueryApi;
import ru.sber.transport.web.model.DepartmentRegistryFilterRs;

@RequiredArgsConstructor
public class DepartmentsQueryDelegateImpl implements ExternalDepartmentsQueryApi {
    private final DepartmentsProvider departmentsProvider;
    private final DepartmentsMapper departmentsMapper;

    @Override
    public CompletableFuture<ResponseEntity<List<DepartmentRegistryFilterRs>>> getDepartmentsForRegistryFilters(UUID organizationId) {
        return CompletableFuture.supplyAsync(() -> {
            final var departments = departmentsProvider.findDepartmentsByOrganizationIdAndStatusAndLevelBetween(organizationId, DepartmentStatus.ACTIVE, 1, 6);
            return  ResponseEntity.ok()
                    .body(departmentsMapper.toDepartmentRegistryFiltersRs(departments));
        });
    }

}
