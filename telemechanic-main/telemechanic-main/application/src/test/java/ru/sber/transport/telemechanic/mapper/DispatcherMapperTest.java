package ru.sber.transport.telemechanic.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import ru.sber.transport.telemechanic.database.model.*;
import ru.sber.transport.telemechanic.dto.dispatcher.AddDispatcherRequest;
import ru.sber.transport.telemechanic.dto.dispatcher.GetDispatcherResponse;
import ru.sber.transport.telemechanic.dto.dispatcher.GetOrganizationDispatcherResponse;

import java.util.UUID;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

class DispatcherMapperTest {
    
    private final DispatcherMapper dispatcherMapper = new DispatcherMapperImpl();
    
    @Test
    void addDispatcherRequestToDispatcher() {
        var dispatcherDto = Instancio.of(AddDispatcherRequest.class).create();
        var actual = dispatcherMapper.addDispatcherRequestToDispatcher(dispatcherDto);
        var expected = new Dispatcher(
                null,
                Employee.builder()
                        .id(dispatcherDto.employeeId())
                        .build(),
                Organization.builder()
                            .id(dispatcherDto.organizationId())
                            .contacts(emptySet())
                            .build(),
                Department.builder()
                          .id(dispatcherDto.departmentId())
                          .build(),
                new Attorney()
                        .setNumber(dispatcherDto.attorneyNumber())
                        .setIssueDate(dispatcherDto.issueDate())
                        .setExpiryDate(dispatcherDto.expiryDate())
                        .setCreationSystem(dispatcherDto.creationSystem()),
                true,
                null,
                null
        );
        
        assertThat(actual).usingRecursiveComparison().isEqualTo(expected);
    }
    
    @Test
    void dispatcherToGetDispatcherResponse() {
        var dispatcher = Instancio.of(Dispatcher.class).create();
        var actual = dispatcherMapper.dispatcherToGetDispatcherResponse(dispatcher);
        assertThat(actual)
                .extracting(
                        GetDispatcherResponse::id,
                        el -> el.dispatcher().personnelNumber(),
                        el -> el.dispatcher().fullName(),
                        el -> el.dispatcher().organizationName(),
                        el -> el.dispatcher().departmentName(),
                        GetDispatcherResponse::attorneyNumber,
                        GetDispatcherResponse::issueDate,
                        GetDispatcherResponse::expiryDate,
                        GetDispatcherResponse::creationSystem
                           )
                .containsExactly(
                        dispatcher.getId(),
                        dispatcher.getEmployee().getPersonnelNumber(),
                        dispatcher.getEmployee().getFIO(),
                        dispatcher.getOrganization().getOfficialName(),
                        dispatcher.getDepartment().getDepartmentName(),
                        dispatcher.getAttorney().getNumber(),
                        dispatcher.getAttorney().getIssueDate(),
                        dispatcher.getAttorney().getExpiryDate(),
                        dispatcher.getAttorney().getCreationSystem()
                                );
    }
    
    @Test
    void dispatcherToGetOrganizationDispatcherResponse() {
        var dispatcher = Instancio.of(Dispatcher.class).create();
        var name = UUID.randomUUID().toString();
        var msrn = UUID.randomUUID().toString();
        var tin = UUID.randomUUID().toString();
        var phone = UUID.randomUUID().toString();
        var regionCode = UUID.randomUUID().toString();
        var expected = new GetOrganizationDispatcherResponse(dispatcher.getId(),
                                                             regionCode,
                                                             new GetOrganizationDispatcherResponse.OrganizationDto(
                                                                     dispatcher.getEmployee().getOrganization().getId(),
                                                                     name,
                                                                     msrn,
                                                                     tin,
                                                                     phone
                                                             ));
        var actual = dispatcherMapper.dispatcherToGetOrganizationDispatcherResponse(dispatcher,
                                                                                    name,
                                                                                    msrn,
                                                                                    tin,
                                                                                    phone,
                                                                                    regionCode);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
}
