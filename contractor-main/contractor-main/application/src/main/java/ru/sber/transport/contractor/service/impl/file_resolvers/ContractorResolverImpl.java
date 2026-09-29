package ru.sber.transport.contractor.service.impl.file_resolvers;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import ru.sber.transport.contractor.database.model.Employee;
import ru.sber.transport.contractor.dto.ContractorDTO;
import ru.sber.transport.contractor.dto.files.ContractorFile;
import ru.sber.transport.contractor.mappers.ContractorMapper;
import ru.sber.transport.contractor.service.ContractorControllerService;
import ru.sber.transport.contractor.service.EmployeeService;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.file_works.importer.DataImporter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Реализация разбора контрагентов.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ContractorResolverImpl implements DataExporter<ContractorFile>, DataImporter<ContractorFile> {

    private final ContractorControllerService service;

    private final ContractorMapper mapper;

    private final EmployeeService employeeService;

    @Override
    public void importData(ContractorFile source, @NonNull Map<String, ?> parameters, @NonNull JwtAuthenticationToken token) {
        var id = UUID.fromString(token.getToken().getId());
        var employee = employeeService.getByUserId(id)
                .orElseThrow(() -> new EntityNotFoundException(Employee.class, id));
        var orgId = employee.getOrganizationId();
        service.add(mapper.fromFile(source), orgId, null);
    }

    @Override
    public @NonNull List<ContractorFile> exportData(@NonNull Map<String, ?> parameters, @NonNull JwtAuthenticationToken authentication) {
        return mapper.toFile(service.getAll().stream()
                .sorted(Comparator.comparing(ContractorDTO::name))
                .toList());
    }
}
