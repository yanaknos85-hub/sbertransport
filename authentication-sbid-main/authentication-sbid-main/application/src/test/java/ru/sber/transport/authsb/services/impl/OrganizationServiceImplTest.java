package ru.sber.transport.authsb.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.authsb.database.dao.OrganizationRepository;
import ru.sber.transport.authsb.database.model.Organization;
import ru.sber.transport.authsb.services.OrganizationService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceImplTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationServiceImpl organizationService;

    private Organization organization;
    private final UUID ORGANIZATION_ID = UUID.randomUUID();
    private static final String OGRN = "1234567890123";
    private static final String KPP = "123456789";

    @BeforeEach
    void setUp() {
        organization = Organization.builder()
                .id(ORGANIZATION_ID)
                .inn("9876543210")
                .ogrn(OGRN)
                .kpp(KPP)
                .fullName("ООО Тестовая Компания")
                .legalFormShort("ООО")
                .juridicalAddress("г. Москва, ул. Тестовая, д. 1")
                .actualAddress("г. Москва, ул. Реальная, д. 2")
                .territorialBank("Сбербанк")
                .email("mail@mail.ru")
                .individualExecutiveAgency(1)
                .oktmo("12345678")
                .orgLawForm("1")
                .updatedAt(LocalDateTime.of(2024, 12, 31, 0, 0))
                .offerExpirationDate(LocalDateTime.of(2024, 12, 31, 0, 0))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findByOgrnAndKpp_ShouldReturnOrganization_WhenExists() {

        when(organizationRepository.findByOgrnAndKpp(OGRN, KPP))
                .thenReturn(Optional.of(organization));

        Optional<Organization> result = organizationService.findByOgrnAndKpp(OGRN, KPP);

        assertThat(result).isPresent();
        assertThat(result.get().getOgrn()).isEqualTo(OGRN);
        assertThat(result.get().getKpp()).isEqualTo(KPP);
        assertThat(result.get().getInn()).isEqualTo("9876543210");
        assertThat(result.get().getLegalFormShort()).isEqualTo("ООО");
        assertThat(result.get().getJuridicalAddress()).isEqualTo("г. Москва, ул. Тестовая, д. 1");
        assertThat(result.get().getActualAddress()).isEqualTo("г. Москва, ул. Реальная, д. 2");
        assertThat(result.get().getTerritorialBank()).isEqualTo("Сбербанк");
        assertThat(result.get().getUpdatedAt()).isEqualTo(LocalDateTime.of(2024, 12, 31, 0, 0));
        assertThat(result.get().getFullName()).isEqualTo("ООО Тестовая Компания");
        assertThat(result.get().getEmail()).isEqualTo("mail@mail.ru");
        assertThat(result.get().getOktmo()).isEqualTo("12345678");
        assertThat(result.get().getIndividualExecutiveAgency()).isEqualTo(1);
        assertThat(result.get().getOrgLawForm()).isEqualTo("1");
        assertThat(result.get().getOfferExpirationDate()).isEqualTo(LocalDateTime.of(2024, 12, 31, 0, 0));


        verify(organizationRepository, times(1)).findByOgrnAndKpp(eq(OGRN), eq(KPP));
    }

    @Test
    void findByOgrnAndKpp_ShouldReturnEmpty_WhenNotFound() {
        when(organizationRepository.findByOgrnAndKpp(any(), any())).thenReturn(Optional.empty());

        Optional<Organization> result = organizationService.findByOgrnAndKpp("unknown-ogrn", "unknown-kpp");

        assertThat(result).isEmpty();

        verify(organizationRepository, times(1)).findByOgrnAndKpp(eq("unknown-ogrn"), eq("unknown-kpp"));
    }

    @Test
    void save_ShouldSaveAndReturnOrganization() {
        when(organizationRepository.save(any(Organization.class))).thenReturn(organization);

        Organization result = organizationService.save(organization);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(ORGANIZATION_ID);
        assertThat(result.getOgrn()).isEqualTo(OGRN);
        assertThat(result.getKpp()).isEqualTo(KPP);

        verify(organizationRepository, times(1)).save(argThat(org ->
                org.getOgrn().equals(OGRN) &&
                        org.getKpp().equals(KPP) &&
                        org.getFullName().equals("ООО Тестовая Компания")
        ));
    }

    @Test
    void save_ShouldHandleNewOrganization() {
        Organization newOrg = Organization.builder()
                .id(null)
                .inn("1112223334")
                .ogrn("9998887776665")
                .kpp("999888777")
                .build();

        Organization savedOrg = Organization.builder()
                .id(UUID.randomUUID())
                .inn("1112223334")
                .ogrn("9998887776665")
                .kpp("999888777")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(organizationRepository.save(any(Organization.class))).thenReturn(savedOrg);

        Organization result = organizationService.save(newOrg);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getOgrn()).isEqualTo("9998887776665");
        assertThat(result.getKpp()).isEqualTo("999888777");
        assertThat(result.getCreatedAt()).isNotNull();

        verify(organizationRepository, times(1)).save(argThat(org ->
                org.getId() == null &&
                        org.getOgrn().equals("9998887776665") &&
                        org.getKpp().equals("999888777")
        ));
    }
}