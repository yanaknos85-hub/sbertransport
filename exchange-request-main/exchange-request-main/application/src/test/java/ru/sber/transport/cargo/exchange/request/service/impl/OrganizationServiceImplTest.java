package ru.sber.transport.cargo.exchange.request.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.cargo.exchange.request.database.dao.OrganizationRepository;
import ru.sber.transport.cargo.exchange.request.database.model.Organization;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceImplTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationServiceImpl organizationService;

    private static final UUID ORG_ID = UUID.randomUUID();
    private static final String INN = "7701023456";
    private static final String NAME = "ООО Транспортная Компания";
    private static final String KPP = "770101001";
    private static final String LEGAL_ADDRESS = "123456, Москва, ул. Ленина, д. 1";
    private static final String BANK_ACCOUNT = "40702810111110000001";
    private static final String BANK_NAME = "Сбербанк";
    private static final String BIC = "044525225";

    @Test
    void findById_shouldReturnOrganization_withAllFields_whenExists() {
        // Given
        Organization org = Organization.builder()
                .id(ORG_ID)
                .inn(INN)
                .name(NAME)
                .kpp(KPP)
                .legalAddress(LEGAL_ADDRESS)
                .bankAccount(BANK_ACCOUNT)
                .bankName(BANK_NAME)
                .bic(BIC)
                .build();

        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.of(org));

        // When
        Optional<Organization> result = organizationService.findById(ORG_ID);

        // Then
        assertThat(result).isPresent().hasValueSatisfying(organization -> {
            assertThat(organization.getId()).isEqualTo(ORG_ID);
            assertThat(organization.getInn()).isEqualTo(INN);
            assertThat(organization.getName()).isEqualTo(NAME);
            assertThat(organization.getKpp()).isEqualTo(KPP);
            assertThat(organization.getLegalAddress()).isEqualTo(LEGAL_ADDRESS);
            assertThat(organization.getBankAccount()).isEqualTo(BANK_ACCOUNT);
            assertThat(organization.getBankName()).isEqualTo(BANK_NAME);
            assertThat(organization.getBic()).isEqualTo(BIC);
        });
        verify(organizationRepository, times(1)).findById(ORG_ID);
    }

    @Test
    void findById_shouldReturnEmpty_whenNotFound() {
        // Given
        when(organizationRepository.findById(ORG_ID)).thenReturn(Optional.empty());

        // When
        Optional<Organization> result = organizationService.findById(ORG_ID);

        // Then
        assertThat(result).isEmpty();
        verify(organizationRepository, times(1)).findById(ORG_ID);
    }

    @Test
    void findByInn_shouldReturnOrganization_withAllFields_whenExists() {
        // Given
        Organization org = Organization.builder()
                .id(ORG_ID)
                .inn(INN)
                .name(NAME)
                .kpp(KPP)
                .legalAddress(LEGAL_ADDRESS)
                .bankAccount(BANK_ACCOUNT)
                .bankName(BANK_NAME)
                .bic(BIC)
                .build();

        when(organizationRepository.findByInn(INN)).thenReturn(Optional.of(org));

        // When
        Optional<Organization> result = organizationService.findByInn(INN);

        // Then
        assertThat(result).isPresent().hasValueSatisfying(organization -> {
            assertThat(organization.getId()).isEqualTo(ORG_ID);
            assertThat(organization.getInn()).isEqualTo(INN);
            assertThat(organization.getName()).isEqualTo(NAME);
            assertThat(organization.getKpp()).isEqualTo(KPP);
            assertThat(organization.getLegalAddress()).isEqualTo(LEGAL_ADDRESS);
            assertThat(organization.getBankAccount()).isEqualTo(BANK_ACCOUNT);
            assertThat(organization.getBankName()).isEqualTo(BANK_NAME);
            assertThat(organization.getBic()).isEqualTo(BIC);
        });
        verify(organizationRepository, times(1)).findByInn(INN);
    }

    @Test
    void findByInn_shouldReturnEmpty_whenNotFound() {
        // Given
        when(organizationRepository.findByInn(INN)).thenReturn(Optional.empty());

        // When
        Optional<Organization> result = organizationService.findByInn(INN);

        // Then
        assertThat(result).isEmpty();
        verify(organizationRepository, times(1)).findByInn(INN);
    }

    @Test
    void save_shouldSaveAndReturnOrganization_withAllFields() {
        // Given
        Organization orgToSave = Organization.builder()
                .inn(INN)
                .name(NAME)
                .kpp(KPP)
                .legalAddress(LEGAL_ADDRESS)
                .bankAccount(BANK_ACCOUNT)
                .bankName(BANK_NAME)
                .bic(BIC)
                .build();

        Organization savedOrg = Organization.builder()
                .id(ORG_ID)
                .inn(INN)
                .name(NAME)
                .kpp(KPP)
                .legalAddress(LEGAL_ADDRESS)
                .bankAccount(BANK_ACCOUNT)
                .bankName(BANK_NAME)
                .bic(BIC)
                .build();

        when(organizationRepository.save(orgToSave)).thenReturn(savedOrg);

        // When
        Organization result = organizationService.save(orgToSave);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ORG_ID);
        assertThat(result.getInn()).isEqualTo(INN);
        assertThat(result.getName()).isEqualTo(NAME);
        assertThat(result.getKpp()).isEqualTo(KPP);
        assertThat(result.getLegalAddress()).isEqualTo(LEGAL_ADDRESS);
        assertThat(result.getBankAccount()).isEqualTo(BANK_ACCOUNT);
        assertThat(result.getBankName()).isEqualTo(BANK_NAME);
        assertThat(result.getBic()).isEqualTo(BIC);
        verify(organizationRepository, times(1)).save(orgToSave);
    }
}


