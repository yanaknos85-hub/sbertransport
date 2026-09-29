package ru.sberbank.ditsib.transport.vehicle.service.corp.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.vehicle.database.dao.OrganizationRepository;
import ru.sberbank.ditsib.transport.vehicle.database.model.Organization;
import ru.sberbank.ditsib.transport.vehicle.dto.DepartmentInfoDto;
import ru.sberbank.ditsib.transport.vehicle.dto.GetDepartmentsInfo;
import ru.sberbank.ditsib.transport.vehicle.dto.OrganizationDto;
import ru.sberbank.ditsib.transport.vehicle.dto.OrganizationNameWithDepartmentInfo;
import ru.sberbank.ditsib.transport.vehicle.mapper.OrganizationMapperImpl;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceImplTest {
    
    @Mock
    private OrganizationRepository organizationRepository;
    @Spy
    private OrganizationMapperImpl organizationMapper;
    @InjectMocks
    private OrganizationServiceImpl organizationService;
    
    @Test
    void getAll() {
        var organization = Instancio.create(Organization.class);
        doReturn(List.of(organization)).when(organizationRepository).findAllByActiveIsTrueOrderByOfficialName();
        doCallRealMethod().when(organizationMapper).organizationToOrganizationDto(any(Organization.class));
        var actual = organizationService.getAll();
        
        assertThat(actual).hasSize(1);
        assertThat(actual).extracting(
                                  OrganizationDto::id,
                                  OrganizationDto::officialName,
                                  OrganizationDto::digitId
                                     )
                          .containsExactly(
                                  tuple(organization.getId(), organization.getOfficialName(), organization.getDigitId())
                                          );
    }
    
    @Test
    void getAllWithDepartment() {
        var organization = Instancio.create(OrganizationNameWithDepartmentInfo.class);
        doReturn(List.of(organization)).when(organizationRepository).findByIdsWithActiveDepartments(Set.of(organization.organizationId()));
        var actual = organizationService.getAllWithDepartment(Set.of(organization.organizationId()));
        
        assertThat(actual).hasSize(1);
        assertThat(actual).extracting(
                GetDepartmentsInfo::organizationName,
                GetDepartmentsInfo::organizationId,
                GetDepartmentsInfo::departmentList
                                     ).containsExactly(
                tuple(
                        organization.organizationName(),
                        organization.organizationId(),
                        List.of(new DepartmentInfoDto(organization.departmentId(), organization.departmentName(), organization.parentId()))
                     )
                                                      );
    }
}
