package ru.sberbank.ditsib.transport.reports.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;
import ru.sberbank.ditsib.transport.reports.controller.SearchRequestOrganizationController;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForCarsharingReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForGroupTransferReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPersonalReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForPublicReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.CarsharingResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.GroupTransferResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.PersonalResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.PublicResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.TaxiResponseDTO;
import ru.sberbank.ditsib.transport.reports.dto.search.OrganizationSearchDto;
import ru.sberbank.ditsib.transport.reports.mappers.OrganizationMapper;
import ru.sberbank.ditsib.transport.reports.model.Organization;
import ru.sberbank.ditsib.transport.reports.service.OrganizationService;
import ru.sberbank.ditsib.transport.reports.service.RequestRegisterService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@E2EController
@RequiredArgsConstructor
@Slf4j
public class SearchRequestOrganizationControllerImpl implements SearchRequestOrganizationController {
    private static final String ALL_ORGANIZATIONS = "all";
    
    private final RequestRegisterService requestRegisterService;
    private final OrganizationService organizationService;
    private final OrganizationMapper organizationMapper;

    @Override
    public Page<TaxiResponseDTO> getTaxi(
            @Valid RequestForTaxiReportDTO requestSearchDTO, UUID organizationId,
            @E2EUser("principal") JwtAuthenticationToken authentication
                                        ) {
        requestSearchDTO.setOrganizationId(organizationId);
        return requestRegisterService.findTaxiRequests(requestSearchDTO);
    }

    @Override
    public Page<PersonalResponseDTO> getPersonal(
            @Valid RequestForPersonalReportDTO requestSearchDTO,
            UUID organizationId,
            @E2EUser("principal") JwtAuthenticationToken authentication
                                                ) {
        requestSearchDTO.setOrganizationId(organizationId);
        return requestRegisterService.findPersonalRequests(requestSearchDTO);
    }

    @Override
    public Page<PublicResponseDTO> getPublic(
            @Valid RequestForPublicReportDTO requestSearchDTO,
            UUID organizationId,
            @E2EUser("principal") JwtAuthenticationToken authentication
                                            ) {
        requestSearchDTO.setOrganizationId(organizationId);
        return requestRegisterService.findPublicRequests(requestSearchDTO);
    }

    @Override
    public Page<CarsharingResponseDTO> getCarsharing(
            @Valid RequestForCarsharingReportDTO requestSearchDTO,
            UUID organizationId,
            @E2EUser("principal") JwtAuthenticationToken authentication
                                                    ) {
        requestSearchDTO.setOrganizationId(organizationId);
        return requestRegisterService.findCarsharingRequests(requestSearchDTO);
    }
    
    @Override
    public Page<GroupTransferResponseDTO> getGroupTransferReport(
            RequestForGroupTransferReportDTO requestSearchDTO, UUID organizationId
                                                                ) {
        requestSearchDTO.setOrganizationId(organizationId);
        return requestRegisterService.findGroupTransferRequests(requestSearchDTO);
    }
    
    @Override
    public OrganizationSearchDto getOrganizationBySearchParameter(String organizationName) {
        List<Organization> organizationList;
        if (ALL_ORGANIZATIONS.equalsIgnoreCase(organizationName)) {
            organizationList =  organizationService.findAll();
        } else {
            organizationList = organizationService.findOrganizationsBySearchParameter(organizationName);
        }
        var resultList = organizationMapper.toOrganizationShortDTOList(organizationList);
        return OrganizationSearchDto.builder().organizations(resultList).build();
    }
}