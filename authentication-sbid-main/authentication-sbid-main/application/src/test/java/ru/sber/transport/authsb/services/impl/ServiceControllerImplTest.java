package ru.sber.transport.authsb.services.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.authsb.api.service.client.RestApiSberBuisness;
import ru.sber.transport.authsb.config.SSLConfiguration;
import ru.sber.transport.authsb.config.SberBusinessIdConfiguration;
import ru.sber.transport.authsb.database.model.Organization;
import ru.sber.transport.authsb.database.model.User;
import ru.sber.transport.authsb.exceptions.BadResponseException;
import ru.sber.transport.authsb.jwt.GostJwtDecoder;
import ru.sber.transport.authsb.services.OrganizationService;
import ru.sber.transport.authsb.services.SessionService;
import ru.sber.transport.authsb.services.UserService;
import ru.sber.transport.authsb.utils.DateUtils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceControllerImplTest {

    @Mock
    private RestApiSberBuisness restApiSberBuisness;

    @Mock
    private GostJwtDecoder gostJwtDecoder;

    @Mock
    private SessionService sessionService;

    @Mock
    private UserService userService;

    @Mock
    private OrganizationService organizationService;

    @Mock
    private SberBusinessIdConfiguration sberBusinessIdConfiguration;

    @Mock
    private SSLConfiguration sslConfiguration;

    @InjectMocks
    private ServiceControllerImpl serviceController;

    private final String VALID_ACCESS_TOKEN = "Bearer valid-access-token";
    private final String SUB = "user-12345";
    private final String INN = "1234567890";
    private final String OGRN = "1234567890123";
    private final String KPP = "123456789";

    private Map<String, Object> mockUserInfo;
    private Organization existingOrg;
    private User expectedUser;

    @BeforeEach
    void setUp() {
        // Подготавливаем данные пользователя из SBID
        mockUserInfo = Map.of(
                "sub", SUB,
                "name", "Иван Иванов",
                "inn", INN,
                "phone_number", "+7 (900) 123-45-67",
                "orgOgrn", OGRN,
                "orgKpp", KPP,
                "orgFullName", "ООО Тестовая Компания",
                "email", "test@example.com",
                "offerExpirationDate", "2031-02-20T08:31:41.794Z"
        );

        // Организация
        existingOrg = Organization.builder()
                .id(UUID.randomUUID())
                .inn(INN)
                .ogrn(OGRN)
                .kpp(KPP)
                .fullName("ООО Тестовая Компания")
                .build();

        // Ожидаемый пользователь
        expectedUser = User.builder()
                .sub(SUB)
                .fullName("Иван Иванов")
                .inn(INN)
                .phoneNumber("+7 (900) 123-45-67")
                .organization(existingOrg)
                .build();
    }

    @Test
    void buildUser_ShouldCreateNewUserAndOrganization_WhenUserDoesNotExist() {

        when(restApiSberBuisness.getUserInfo(any())).thenReturn(mockUserInfo.toString());
        when(gostJwtDecoder.decode(any())).thenReturn(mockUserInfo);
        when(organizationService.findByOgrnAndKpp(OGRN, KPP)).thenReturn(Optional.empty());
        when(organizationService.save(any(Organization.class))).thenReturn(existingOrg);
        when(userService.save(any(User.class))).thenReturn(expectedUser);

        User result = serviceController.buildUser(VALID_ACCESS_TOKEN);

        assertThat(result).isNotNull();
        assertThat(result.getSub()).isEqualTo(SUB);
        assertThat(result.getFullName()).isEqualTo("Иван Иванов");
        assertThat(result.getInn()).isEqualTo(INN);
        assertThat(result.getPhoneNumber()).isEqualTo("+7 (900) 123-45-67");
        assertThat(result.getOrganization().getId()).isEqualTo(existingOrg.getId());

        verify(organizationService, times(1)).findByOgrnAndKpp(eq(OGRN), eq(KPP));
        verify(organizationService, times(1)).save(argThat(org ->
                org.getInn().equals(INN) &&
                        org.getOgrn().equals(OGRN) &&
                        org.getKpp().equals(KPP) &&
                        org.getFullName().equals("ООО Тестовая Компания") &&
                        org.getOfferExpirationDate().isEqual(DateUtils.fromIsoZonedDateTime("2031-02-20T08:31:41.794Z"))
        ));
        verify(userService, times(1)).save(argThat(user ->
                user.getSub().equals(SUB) &&
                        user.getFullName().equals("Иван Иванов") &&
                        user.getOrganization().getId().equals(existingOrg.getId())
        ));
    }

    @Test
    @DisplayName("Создание пользователя с существующей организацией")
    void buildUser_ShouldUseExistingOrganization_WhenFoundByOgrnAndKpp() {
        when(restApiSberBuisness.getUserInfo(any())).thenReturn(mockUserInfo.toString());
        when(gostJwtDecoder.decode(any())).thenReturn(mockUserInfo);
        when(organizationService.findByOgrnAndKpp(OGRN, KPP)).thenReturn(Optional.of(existingOrg));
        when(userService.save(any(User.class))).thenReturn(expectedUser);

        User result = serviceController.buildUser(VALID_ACCESS_TOKEN);

        assertThat(result).isNotNull();
        assertThat(result.getOrganization().getId()).isEqualTo(existingOrg.getId());

        verify(organizationService, times(1)).findByOgrnAndKpp(eq(OGRN), eq(KPP));
        verify(organizationService, never()).save(any(Organization.class)); // Не сохраняем новую организацию
        verify(userService, times(1)).save(any(User.class));
    }

    @Test
    void buildUser_ShouldThrowException_WhenAccessTokenIsInvalid() {
        when(restApiSberBuisness.getUserInfo(any())).thenThrow(new RuntimeException("Connection failed"));

        assertThatThrownBy(() -> serviceController.buildUser(VALID_ACCESS_TOKEN))
                .isInstanceOf(BadResponseException.class)
                .hasMessageContaining("Не удалось получить информацию о пользователе");

        verify(restApiSberBuisness, times(1)).getUserInfo(any());
    }

    @Test
    void buildUser_ShouldThrowException_WhenAccessTokenIsNull() {
        assertThatThrownBy(() -> serviceController.buildUser(null))
                .isInstanceOf(BadResponseException.class)
                .hasMessageContaining("Некорректный формат параметров");
    }
}