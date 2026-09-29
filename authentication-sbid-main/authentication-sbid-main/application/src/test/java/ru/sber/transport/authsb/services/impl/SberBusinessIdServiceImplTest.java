package ru.sber.transport.authsb.services.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import ru.sber.transport.authsb.api.service.client.RestApiSberBuisness;
import ru.sber.transport.authsb.config.SSLConfiguration;
import ru.sber.transport.authsb.config.SberBusinessIdConfiguration;
import ru.sber.transport.authsb.database.dao.SessionRepository;
import ru.sber.transport.authsb.database.model.Organization;
import ru.sber.transport.authsb.database.model.Role;
import ru.sber.transport.authsb.database.model.Session;
import ru.sber.transport.authsb.database.model.User;
import ru.sber.transport.authsb.dto.TokenResponseDto;
import ru.sber.transport.authsb.exceptions.BadResponseException;
import ru.sber.transport.authsb.exceptions.ResponseNotFoundException;
import ru.sber.transport.authsb.jwt.GostJwtDecoder;
import ru.sber.transport.authsb.services.OrganizationService;
import ru.sber.transport.authsb.services.ServiceController;
import ru.sber.transport.authsb.services.SessionService;
import ru.sber.transport.authsb.services.UserService;
import ru.sber.transport.authsb.utils.HashUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static ru.sber.transport.authsb.api.service.client.ParamRequest.*;

@SpringBootTest(classes = {ServiceControllerImpl.class, RestApiSberBuisness.class, SessionServiceImpl.class})

@Import(SberBusinessIdServiceImplTest.TestConfig.class)
class SberBusinessIdServiceImplTest {

    @Autowired
    private ServiceController serviceController;

    @Autowired
    private SessionService sessionService;

    @MockBean
    private SessionRepository sessionRepository;

    @MockBean
    private GostJwtDecoder gostJwtDecoder;

    @MockBean
    private RestApiSberBuisness restApiSberBuisness;

    @MockBean
    private UserService userService;

    @MockBean
    private SSLConfiguration sslConfiguration;

    @MockBean
    private OrganizationService organizationService;

    private static final String SESSION_ID = "2c9e8d8e-c4fe-4832-b805-43cfe0e408c0";
    private static final String HASHED_STATE = HashUtils.hashSha256(SESSION_ID);
    private static final String HASHED_SESSION = HashUtils.hashSha256(SESSION_ID);
    private static final String AUTH_CODE = "test-auth-code";

    @Test
    @DisplayName("Тест на createUrl")
    void test1() {
        when(restApiSberBuisness.sendForm("authorization_code", "code"))
                .thenReturn(Collections.emptyMap());

        String url = serviceController.createUrl(SESSION_ID);

        assertThat(url).isNotBlank();
        assertThat(url).contains("https://sberid.ru/auth/oauth/authorize")
                .contains("client_id=test-client-id")
                .contains("response_type=code")
                .contains("redirect_uri=https://myapp.ru/callback")
                .contains("scope=openid+profile")
                .contains("state=" + HASHED_SESSION)
                .contains("nonce=");
    }

    @Test
    @DisplayName("Тест на getAuthToken")
    void test2() {
        when(restApiSberBuisness.sendForm(anyString(), anyString()))
                .thenReturn(Collections.emptyMap());

        String url = serviceController.createUrl(SESSION_ID);

        assertThat(url).isNotBlank();
        assertThat(url).contains("https://sberid.ru/auth/oauth/authorize")
                .contains("client_id=test-client-id")
                .contains("response_type=code")
                .contains("redirect_uri=https://myapp.ru/callback")
                .contains("scope=openid+profile")
                .contains("state=" + HASHED_SESSION)
                .contains("nonce=");
    }

    @Test
    @DisplayName("Тест на getAuthToken refreshToken")
    void test3() {
        when(sslConfiguration.getPrivateKey())
                .thenReturn("key/jwt.key");
        String expectedAccessToken = "eyJhbGciOiJSUzUxMiJ9.eyJzdWIiOiJhMzc4YTRlZmFkOGY0NTc5YjA4ZTAyNDQ3OWM2Y2ZhZTQ3MzBjYzYyM2Q1ODk3ZWM3NTMwNTUwNzg2NzEwNzExIiwiYXVkIjoiY2FyZ28tbWljcm9zZXJ2aWNlcyIsInJvbGUiOiJVU0VSIiwibmFtZSI6ImEzNzhhNGVmYWQ4ZjQ1NzliMDhlMDI0NDc5YzZjZmFlNDczMGNjNjIzZDU4OTdlYzc1MzA1NTA3ODY3MTA3MTEiLCJpc3MiOiJhdXRoZW50aWNhdGlvbi1zYmlkIiwiaWF0IjoxNzcyNTYwMTUxLCJleHAiOjE3ODgxMTIxNTF9.BlXNiPMwIVtq4wYDGi4kZuHflXWkT1ds4yty9qPUItPPbD_qBUm9QSyyFVFB_SRbxWzGC0G8HvGYWCD7Deh33DXhDYqJAEXkcO14KoJhMCJ4cW3KjdUemyiijVOiLCJyOpBv9Ffdd15k32uNXq_C0pKLMnvnDpvxiF-44bz5cIzqVcYHjI5wGflRUJKwFu3m5lIqw1S2Uoa5hWgheFlQXXpkmqlpshrWh1S8UUqlF73RovIVpDBrocobVNks4y_Y9FFZLyW60yoK5weV6oUYZF1OKANneC5MBIpUcI1jEYI5_OxfKcUAbJWmAeV0GGlAAW_1cXnn_9zyfwNWoJhARw";
        String expectedIdToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJnb3N0MzQuMTAtMjAxMiJ9.eyJhdWQiOiIyNDAzMSIsImFjciI6ImxvYS0zIiwic3ViIjoiYTM3OGE0ZWZhZDhmNDU3OWIwOGUwMjQ0NzljNmNmYWE0NzMwY2M2MjNkNTg5N2VjNzUzMDU1MDc4NjcxMDcxMSIsImF6cCI6IjI0MDMxIiwiYW1yIjoie3NtcywgcHdkfSIsImF1dGhfdGltZSI6MTc3MTg1NjkwNiwiaXNzIjoiZWZzLXNiYm9sLWlmdC13ZWIudGVzdHNiaS5zYmVyYmFuay5ydTo5NDQzIiwiZXhwIjoxNzcxODU3MzkzLCJpYXQiOjE3NzE4NTcwODgsIm5vbmNlIjoiN2FhNWFkMDNkZmI4MDU4ZTA0OTY2ZDg3NDg1MjM5NTQ1MjM5MDBlZjdhZmVjMDNkOTJjMTNmODQwYjcwYzdlOSJ9.MIAGCSqGSIb3DQEHAqCAMIACAQExDDAKBggqhQMHAQECAjALBgkqhkiG9w0BBwGggDCCBNYwggSDoAMCAQICCn1GMg13l3QLQwIwCgYIKoUDBwEBAwIwggFKMRUwEwYFKoUDZAQSCjc3MDIyMzUxMzMxGzAZBgkqhkiG9w0BCQEWDHRlc3RAY2IudGVzdDEYMBYGBSqFA2QBEg0xMDM3NzAwMDEzMDIwMQswCQYDVQQGEwJSVTEYMBYGA1UECAwPNzcg0JzQvtGB0LrQstCwMRkwFwYDVQQHDBDQsy4g0JzQvtGB0LrQstCwMSkwJwYDVQQJDCDRg9C7LiDQndC10LPQu9C40L3QvdCw0Y8sINC0LiAxMjEvMC0GA1UECwwm0KLQtdGB0YLQvtCy0YvQuSDQkdCw0L3QuiDQoNC-0YHRgdC40LgxLTArBgNVBAoMJNCj0KYg0KbQkSDQotCV0KHQoiDQmtCh0JrQnyAo0JjQpNCiKTEtMCsGA1UEAwwk0KPQpiDQptCRINCi0JXQodCiINCa0KHQmtCfICjQmNCk0KIpMB4XDTI1MDUxMjEyMzEwMFoXDTI2MDgxMjEyMzI1M1owggEuMR8wHQYDVQQKDBbQodCx0LXRgNCi0LXRhdCi0LXRgdGCMQswCQYDVQQGEwJSVTEcMBoGA1UECAwTNzcg0LMuINCc0L7RgdC60LLQsDEZMBcGA1UEBwwQ0LMuINCc0L7RgdC60LLQsDEmMCQGA1UECQwd0YPQuy4g0JLQsNCy0LjQu9C-0LLQsCwg0LQuMTkxFTATBgUqhQNkBBIKNzczNjYzMjQ2NzEYMBYGBSqFA2QBEg0xMTE3NzQ2NTMzOTI2MR8wHQYDVQQDDBbQodCx0LXRgNCi0LXRhdCi0LXRgdGCMUswSQYDVQQLDELQotC10YHRgtC-0LLQvtC1INC_0L7QtNGA0LDQt9C00LXQu9C10L3QuNC1INCh0LHQtdGA0KLQtdGF0KLQtdGB0YIwZjAfBggqhQMHAQEBATATBgcqhQMCAiMCBggqhQMHAQECAgNDAARAr9_uJsQ7p8FCtbX5lXVRd4oKh9bOo5JsFlmlCMQjon3IuOPMmyNk-gUlg6wEIQ_s3J_8dBdmOFaXxUpZqCkgWKOCAVowggFWMDoGByqFAwN7AwEELwwtU0JUSjNKOFBh0KLQtdGB0YLQn9Cf0KDQkdCu0JtEaWdCMkLQutCy0JjQpNCiMAwGA1UdEwEB_wQCMAAwDgYDVR0PAQH_BAQDAgP4MBMGA1UdJQQMMAoGCCsGAQUFBwMBMB0GBSqFA2RvBBQMEtCR0LjQutGA0LjQv9GCIDUuMDATBgNVHSAEDDAKMAgGBiqFA2RxATAdBgNVHQ4EFgQU_GGlOUesrm-Ip3Yoc4DUj9sJZLowRAYDVR0fBD0wOzA5oDegNYYzaHR0cDovL3d3dy5zYmVyYmFuay5ydS9jYS9pc3N1c2VyX3Rlc3RfMDBDQTA0ODYuY3JsMB8GA1UdIwQYMBaAFNhJleAd7Mu6AAdcDAhAuRPrkx_9MCsGA1UdEAQkMCKADzIwMjUwNTEyMTIzMTAwWoEPMjAyNjA4MTIxMjMyNTNaMAoGCCqFAwcBAQMCA0EABaykQvLXkB9MFP30Vxee0wXsKxXcMtVhZV-_wLaru-YItTC1b3orW4HO4WtojKhI3QutkoW2Q2v4g-Ac8t39_zCCB7UwggdioAMCAQICCnyYpkqU1SXOUYMwCgYIKoUDBwEBAwIwggFSMRwwGgYJKoZIhvcNAQkBFg10ZXN0QG1jci50ZXN0MQswCQYDVQQGEwJSVTEYMBYGA1UECAwPNzcg0JzQvtGB0LrQstCwMRkwFwYDVQQHDBDQsy4g0JzQvtGB0LrQstCwMU0wSwYDVQQJDETQotC10YHRgtC-0LLQsNGPINC90LDQsdC10YDQtdC20L3QsNGPLCDQtNC-0LwgMTAsINGB0YLRgNC-0LXQvdC40LUgMjE3MDUGA1UECgwu0KLQtdGB0YLQvtCy0YvQuSDQutC-0YDQtdC90Ywg0JzQuNC90YbQuNGE0YDRizEYMBYGBSqFA2QBEg01MjI5MTM0MzMyNjMxMRUwEwYFKoUDZAQSCjQ0NTUwMTc1NjYxNzA1BgNVBAMMLtCi0LXRgdGC0L7QstGL0Lkg0LrQvtGA0LXQvdGMINCc0LjQvdGG0LjRhNGA0YswHhcNMjQxMDE3MDAwMDAwWhcNMzkxMDE3MDAwMDAwWjCCAUoxFTATBgUqhQNkBBIKNzcwMjIzNTEzMzEbMBkGCSqGSIb3DQEJARYMdGVzdEBjYi50ZXN0MRgwFgYFKoUDZAESDTEwMzc3MDAwMTMwMjAxCzAJBgNVBAYTAlJVMRgwFgYDVQQIDA83NyDQnNC-0YHQutCy0LAxGTAXBgNVBAcMENCzLiDQnNC-0YHQutCy0LAxKTAnBgNVBAkMINGD0LsuINCd0LXQs9C70LjQvdC90LDRjywg0LQuIDEyMS8wLQYDVQQLDCbQotC10YHRgtC-0LLRi9C5INCR0LDQvdC6INCg0L7RgdGB0LjQuDEtMCsGA1UECgwk0KPQpiDQptCRINCi0JXQodCiINCa0KHQmtCfICjQmNCk0KIpMS0wKwYDVQQDDCTQo9CmINCm0JEg0KLQldCh0KIg0JrQodCa0J8gKNCY0KTQoikwZjAfBggqhQMHAQEBATATBgcqhQMCAiMCBggqhQMHAQECAgNDAARAUjuPro-zqitag2WJ1IJrNoHlZTiqyUXp4c-3MIVh9j8ZhN2wNvYVICzGpKm4g6BVPGihL4kAXg5sMCLv0bCEFqOCBBUwggQRMAwGBSqFA2RyBAMCAQAwHQYDVR0OBBYEFNhJleAd7Mu6AAdcDAhAuRPrkx_9MEYGCCsGAQUFBwEBBDowODA2BggrBgEFBQcwAoYqaHR0cDovL3d3dy5zYmVyYmFuay5ydS9jYS90ZXN0LnJvb3QubWMuY2VyMCsGA1UdEAQkMCKADzIwMjQxMDE3MTMyNTIzWoEPMjAyNzEwMTcxMzI1MjNaMDsGA1UdHwQ0MDIwMKAuoCyGKmh0dHA6Ly93d3cuc2JlcmJhbmsucnUvY2EvdGVzdC5yb290Lm1jLmNybDCB9QYFKoUDZHAEgeswgegMNNCf0JDQmtCcIMKr0JrRgNC40L_RgtC-0J_RgNC-IEhTTcK7INCy0LXRgNGB0LjQuCAyLjAMQ9Cf0JDQmiDCq9CT0L7Qu9C-0LLQvdC-0Lkg0YPQtNC-0YHRgtC-0LLQtdGA0Y_RjtGJ0LjQuSDRhtC10L3RgtGAwrsMNdCX0LDQutC70Y7Rh9C10L3QuNC1IOKEliAxNDkvMy8yLzIvMjMg0L7RgiAwMi4wMy4yMDE4DDTQl9Cw0LrQu9GO0YfQtdC90LjQtSDihJYgMTQ5LzcvNi00NDkg0L7RgiAzMC4xMi4yMDIxMF0GBSqFA2RvBFQMUtCQ0J_QmiAi0KHQuNCz0L3QsNGC0YPRgNCwLdC60LvQuNC10L3RgiBMIiDQstC10YDRgdC40Y8gNiAo0LjRgdC_0L7Qu9C90LXQvdC40LUgMykwJwYDVR0gBCAwHjAIBgYqhQNkcQEwCAYGKoUDZHECMAgGBiqFA2RxAzASBgNVHRMBAf8ECDAGAQH_AgEAMAsGA1UdDwQEAwIBBjCCAY0GA1UdIwSCAYQwggGAgBRhjIJDEzOTqRFSTPjDRYfayUyWCaGCAVqkggFWMIIBUjEcMBoGCSqGSIb3DQEJARYNdGVzdEBtY3IudGVzdDELMAkGA1UEBhMCUlUxGDAWBgNVBAgMDzc3INCc0L7RgdC60LLQsDEZMBcGA1UEBwwQ0LMuINCc0L7RgdC60LLQsDFNMEsGA1UECQxE0KLQtdGB0YLQvtCy0LDRjyDQvdCw0LHQtdGA0LXQttC90LDRjywg0LTQvtC8IDEwLCDRgdGC0YDQvtC10L3QuNC1IDIxNzA1BgNVBAoMLtCi0LXRgdGC0L7QstGL0Lkg0LrQvtGA0LXQvdGMINCc0LjQvdGG0LjRhNGA0YsxGDAWBgUqhQNkARINNTIyOTEzNDMzMjYzMTEVMBMGBSqFA2QEEgo0NDU1MDE3NTY2MTcwNQYDVQQDDC7QotC10YHRgtC-0LLRi9C5INC60L7RgNC10L3RjCDQnNC40L3RhtC40YTRgNGLggp7aZryplS-j0ytMAoGCCqFAwcBAQMCA0EAUcvPGGZ-pu9n9rc5aISr74gA7igiTNmEwpnDtHPtGyc9iYsEp3edEkWWoQ_Ljm0_1Knyi3lMXr9O0y8-AMm_jgAAMYID4TCCA90CAQEwggFaMIIBSjEVMBMGBSqFA2QEEgo3NzAyMjM1MTMzMRswGQYJKoZIhvcNAQkBFgx0ZXN0QGNiLnRlc3QxGDAWBgUqhQNkARINMTAzNzcwMDAxMzAyMDELMAkGA1UEBhMCUlUxGDAWBgNVBAgMDzc3INCc0L7RgdC60LLQsDEZMBcGA1UEBwwQ0LMuINCc0L7RgdC60LLQsDEpMCcGA1UECQwg0YPQuy4g0J3QtdCz0LvQuNC90L3QsNGPLCDQtC4gMTIxLzAtBgNVBAsMJtCi0LXRgdGC0L7QstGL0Lkg0JHQsNC90Log0KDQvtGB0YHQuNC4MS0wKwYDVQQKDCTQo9CmINCm0JEg0KLQldCh0KIg0JrQodCa0J8gKNCY0KTQoikxLTArBgNVBAMMJNCj0KYg0KbQkSDQotCV0KHQoiDQmtCh0JrQnyAo0JjQpNCiKQIKfUYyDXeXdAtDAjAKBggqhQMHAQECAqCCAh4wGAYJKoZIhvcNAQkDMQsGCSqGSIb3DQEHATAcBgkqhkiG9w0BCQUxDxcNMjYwMjIzMTQzMTMwWjAvBgkqhkiG9w0BCQQxIgQgNnUBbyUb5AhIjAKHdmUq5Zy3Aq80LO3qO4QIVPf9j7cwggGxBgsqhkiG9w0BCRACLzGCAaAwggGcMIIBmDCCAZQwCgYIKoUDBwEBAgIEIAB75KZyWF0M9ydTT2YvVts-PEh4iWDsIC6ohdiykx-LMIIBYjCCAVKkggFOMIIBSjEVMBMGBSqFA2QEEgo3NzAyMjM1MTMzMRswGQYJKoZIhvcNAQkBFgx0ZXN0QGNiLnRlc3QxGDAWBgUqhQNkARINMTAzNzcwMDAxMzAyMDELMAkGA1UEBhMCUlUxGDAWBgNVBAgMDzc3INCc0L7RgdC60LLQsDEZMBcGA1UEBwwQ0LMuINCc0L7RgdC60LLQsDEpMCcGA1UECQwg0YPQuy4g0J3QtdCz0LvQuNC90L3QsNGPLCDQtC4gMTIxLzAtBgNVBAsMJtCi0LXRgdGC0L7QstGL0Lkg0JHQsNC90Log0KDQvtGB0YHQuNC4MS0wKwYDVQQKDCTQo9CmINCm0JEg0KLQldCh0KIg0JrQodCa0J8gKNCY0KTQoikxLTArBgNVBAMMJNCj0KYg0KbQkSDQotCV0KHQoiDQmtCh0JrQnyAo0JjQpNCiKQIKfUYyDXeXdAtDAjAKBggqhQMHAQEBAQRAWFYHyiTEdA6mgoM6JhWc7K0sehxR2z3ot9VK4J3BGgN3eHmft909ClND0BBvQ5EsFnEyEeLsStXU2r48pW0KkwAAAAAAAA";
        String expectedRefreshToken = "mock-refresh-token";
        String expectedTokenType = "Bearer";
        int expectedExpiresIn = 3600;

        Map<String, Object> mockResponse = Map.of(
                "access_token", expectedAccessToken,
                "id_token", expectedIdToken,
                "refresh_token", expectedRefreshToken,
                "token_type", expectedTokenType,
                "expires_in", expectedExpiresIn
        );

        when(restApiSberBuisness.sendForm("authorization_code", AUTH_CODE))
                .thenReturn(mockResponse);

        Session session1 = Session.builder()
                .id(UUID.fromString(SESSION_ID))
                .nonce(HASHED_STATE)
                .state(HASHED_SESSION)
                .active(true)
                .creationTime(LocalDateTime.now(ZoneId.of(ZoneOffset.UTC.getId())).minusDays(1))
                .build();
        when(sessionRepository.findByStateAndActiveTrue(HASHED_SESSION))
                .thenReturn(Optional.of(session1));

        Map<String, Object> expectedResult = Map.of(
                NONCE, HASHED_STATE,
                AUD, "test-client-id",
                ISS, "sberid.ru/auth",
                SUB, "a378a4efad8f4579b08e024479c6cfae4730cc623d5897ec7530550786710711");
        when(gostJwtDecoder.decode(anyString())).thenReturn(expectedResult);
        when(organizationService.findByOgrnAndKpp(any(), any())).thenReturn(Optional.of(Organization.builder().id(UUID.randomUUID())
                .inn("1111").kpp("11111").build()));
        when(userService.findBySub(any()))
                .thenReturn(Optional.of(User.builder().id(UUID.randomUUID())
                        .roles(Set.of(Role.builder().role("CARRIER").build())).build()));
        TokenResponseDto result = serviceController.getAuthToken(AUTH_CODE, HASHED_SESSION);

        assertThat(result).isNotNull();
        assertThat(result.getRefreshToken()).isEqualTo(expectedRefreshToken);
        assertThat(result.getAccessExpiration()).isEqualTo(3600);
        assertThat(result.getRefreshExpiration()).isEqualTo(25500300);

        verify(sessionRepository, times(1)).save(argThat(savedSession ->
                savedSession.getAccessToken().equals(expectedAccessToken) &&
                        savedSession.getExpiresIn() == expectedExpiresIn
        ));
    }

    @Test
    @DisplayName("Error некорректный формат параметров")
    void test4() {
        assertThatThrownBy(() -> serviceController.getAuthToken(AUTH_CODE, null))
                .isInstanceOf(BadResponseException.class)
                .hasMessage("Некорректный формат параметров");

        verify(sessionRepository, never()).findByStateAndActiveTrue(any());
        verify(restApiSberBuisness, never()).sendForm(any(), any());
    }

    @Test
    @DisplayName("Error отсутствует state")
    void test8() {
        when(sessionRepository.findByStateAndActiveTrue(HASHED_STATE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> serviceController.getAuthToken(AUTH_CODE, HASHED_STATE))
                .isInstanceOf(BadResponseException.class)
                .hasMessage("Недопустимый или устаревший state");

        verify(restApiSberBuisness, never()).sendForm(any(), any());
    }

    @Test
    @DisplayName("Error нет авторизации")
    void test5() {
        Session session = Session.builder()
                .nonce(HASHED_STATE)
                .state(HASHED_STATE)
                .active(true)
                .refreshToken(null)
                .creationTime(LocalDateTime.now().minusDays(1))
                .build();

        when(sessionRepository.findByStateAndActiveTrue(HASHED_STATE))
                .thenReturn(Optional.of(session));

        assertThatThrownBy(() -> serviceController.getAuthToken(null, HASHED_STATE))
                .isInstanceOf(BadResponseException.class)
                .hasMessage("Нет авторизации");
    }

    @Test
    @DisplayName("Error устаревший state")
    void test7() {
        Session session1 = Session.builder()
                .nonce(HASHED_STATE)
                .state(HASHED_STATE)
                .active(true)
                .refreshToken("old-refresh-token")
                .creationTime(LocalDateTime.now().minusDays(181)) // > 30 days
                .build();

        when(sessionRepository.findByStateAndActiveTrue(HASHED_STATE))
                .thenReturn(Optional.of(session1));

        assertThatThrownBy(() -> serviceController.getAuthToken(null, HASHED_STATE))
                .isInstanceOf(BadResponseException.class)
                .hasMessage("Ваша сессия устарела");
    }

    @Test
    @DisplayName("Error неверный ответ")
    void test9() {
        Map<String, Object> invalidResponse = Map.of("token_type", "Bearer"); // missing access_token, expires_in
        Session session = Session.builder()
                .nonce(HASHED_STATE)
                .state(HASHED_STATE)
                .active(true)
                .creationTime(LocalDateTime.now().minusDays(1))
                .build();

        when(sessionRepository.findByStateAndActiveTrue(HASHED_STATE))
                .thenReturn(Optional.of(session));
        when(restApiSberBuisness.sendForm("authorization_code", AUTH_CODE))
                .thenReturn(invalidResponse);

        assertThatThrownBy(() -> serviceController.getAuthToken(AUTH_CODE, HASHED_STATE))
                .isInstanceOf(ResponseNotFoundException.class)
                .hasMessageContaining("access_token")
                .hasMessageContaining("expires_in");
    }

    @Test
    @DisplayName("Error неверный ответ2")
    void test6() {
        Session session = Session.builder()
                .nonce(HASHED_STATE)
                .state(HASHED_STATE)
                .active(true)
                .creationTime(LocalDateTime.now().minusDays(1))
                .build();

        when(sessionRepository.findByStateAndActiveTrue(HASHED_STATE))
                .thenReturn(Optional.of(session));
        when(restApiSberBuisness.sendForm("authorization_code", AUTH_CODE))
                .thenReturn(Collections.emptyMap());

        assertThatThrownBy(() -> serviceController.getAuthToken(AUTH_CODE, HASHED_STATE))
                .isInstanceOf(ResponseNotFoundException.class);
    }

    @Test
    @DisplayName("Тест на getAuthToken refreshToken")
    void test11() {
        when(sslConfiguration.getPrivateKey()).thenReturn("key/jwt.key");
        String refreshToken = "existing-refresh-token";
        String newAccessToken = "new-access-token";

        Map<String, Object> mockResponse = Map.of(
                "access_token", newAccessToken,
                "id_token", "mock-id111111-token",
                "token_type", "Bearer",
                "expires_in", 3600
        );

        Session session = Session.builder()
                .state(HASHED_STATE)
                .nonce(HASHED_STATE)
                .code("mock-code")
                .idToken("mock-id-token")
                .accessToken("mock-access-token")
                .refreshToken("refresh")
                .active(true)
                .refreshToken(refreshToken)
                .expiresIn(14000)
                .creationTime(LocalDateTime.now().minusDays(1))
                .sub("834722822b0a76a7302cde26bd6788d11124bed996a569b7694497337d3e53bf")
                .build();

        when(sessionRepository.findAllByRefreshTokenAndActiveTrue(refreshToken))
                .thenReturn(Optional.of(session));
        when(restApiSberBuisness.sendForm("refresh_token", refreshToken))
                .thenReturn(mockResponse);
        when(userService.findBySub(any())).thenReturn(Optional.of(User.builder().roles(Set.of(Role.builder().role("CARRIER").build()))
                .id(UUID.randomUUID()).build()));

        serviceController.refreshToken(refreshToken);
        verify(restApiSberBuisness).sendForm("refresh_token", refreshToken);
    }

    @Configuration
    static class TestConfig {
        @Bean
        SberBusinessIdConfiguration sberBusinessIdConfiguration() {
            SberBusinessIdConfiguration config = new SberBusinessIdConfiguration();
            config.setUrlSbid("https://sberid.ru/auth");
            config.setGetAuthMetod("/oauth/authorize");
            config.setPostAuthMetod("/oauth/token");
            config.setClientId("test-client-id");
            config.setClientSecret("test-secret");
            config.setResponseType("code");
            config.setRedirectUri("https://myapp.ru/callback");
            config.setScope("openid profile");
            return config;
        }
    }
}