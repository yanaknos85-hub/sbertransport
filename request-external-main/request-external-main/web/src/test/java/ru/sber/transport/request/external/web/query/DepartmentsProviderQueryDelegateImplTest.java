package ru.sber.transport.request.external.web.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import jdk.jfr.Name;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sber.transport.business.providers.DepartmentsProvider;
import ru.sber.transport.request.external.mapper.DepartmentsMapper;
import ru.sber.transport.request.external.model.DepartmentStatus;

class DepartmentsProviderQueryDelegateImplTest {
    private final DepartmentsProvider departmentsProvider = mock(DepartmentsProvider.class);
    private final DepartmentsMapper departmentsMapper = mock(DepartmentsMapper.class);
    private final DepartmentsQueryDelegateImpl departmentsQueryDelegate =
            new DepartmentsQueryDelegateImpl(departmentsProvider, departmentsMapper);

    @Test
    @Name("Проверка получения списка подразделений для фитров в реестре. Пустой список вовзращается.")
    void test_getDepartmentsForRegistryFilters() throws ExecutionException, InterruptedException {
        final var organizationId = UUID.randomUUID();

        final var userId = UUID.randomUUID();
        SecurityContextHolder
                .getContext()
                .setAuthentication(new JwtAuthenticationToken(Jwt.withTokenValue("token").header("algo", "none").jti(userId.toString()).build()));

        final var result = departmentsQueryDelegate.getDepartmentsForRegistryFilters(organizationId).get();

        when(departmentsProvider.findDepartmentsByOrganizationIdAndStatusAndLevelBetween(organizationId, DepartmentStatus.ACTIVE, 1, 6))
                .thenReturn(Collections.emptyList());

        when(departmentsMapper.toDepartmentRegistryFiltersRs(anyList()))
                .thenReturn(Collections.emptyList());

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(Collections.emptyList());
    }
}
