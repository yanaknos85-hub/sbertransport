package ru.sber.transport.authsb.api.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.*;
import reactor.core.publisher.Mono;
import ru.sber.transport.authsb.config.SberBusinessIdConfiguration;
import ru.sber.transport.authsb.config.UserServiceConfiguration;
import ru.sber.transport.authsb.exceptions.BadResponseException;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static ru.sber.transport.authsb.enums.TokenTypeEnum.AUTHORIZATION_CODE;
import static ru.sber.transport.authsb.enums.TokenTypeEnum.REFRESH_TOKEN;

@ExtendWith(MockitoExtension.class)
public class RestApiSberBuisnessTest {

    private SberBusinessIdConfiguration sberBusinessIdConfiguration = mock(SberBusinessIdConfiguration.class);

    private UserServiceConfiguration userServiceConfiguration = mock(UserServiceConfiguration.class);

    private final ExchangeFunction exchangeFunction = mock(ExchangeFunction.class);
    private final WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestApiSberBuisness restApiSberBuisness
            = new RestApiSberBuisness(objectMapper, webClient, sberBusinessIdConfiguration, userServiceConfiguration);



    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Captor private ArgumentCaptor<ClientRequest> requestCaptor;

    @BeforeEach
    void setUp() {
        // Настраиваем конфигурацию
        when(sberBusinessIdConfiguration.getUrlSbid()).thenReturn("https://test-sbid.ru");
        when(sberBusinessIdConfiguration.getPostAuthMetod()).thenReturn("/token");
        when(sberBusinessIdConfiguration.getRedirectUri()).thenReturn("https://myapp.ru/callback");
        when(sberBusinessIdConfiguration.getClientId()).thenReturn("test-client-id");
        when(sberBusinessIdConfiguration.getClientSecret()).thenReturn("test-secret");

        when(userServiceConfiguration.getRestUrl()).thenReturn("https://test-sbid.ru");
        when(userServiceConfiguration.getPostAuthMetod()).thenReturn("/token");
    }

    @Test
    @DisplayName("Тест отправки формы авторизации")
    void test1() {
        stubOkResponse();
        Map<String, Object> result = restApiSberBuisness.sendForm(AUTHORIZATION_CODE.getName(), "code-123");
        assertThat(result).isNotNull();
        assertThat(result).containsKey("access_token");
        assertThat(result.get("access_token")).isEqualTo("mock-jwt-token");
        assertThat(result).containsEntry("token_type", "Bearer");
        assertThat((Integer) result.get("expires_in")).isEqualTo(3600);
    }

    @Test
    @DisplayName("Тест отправки формы авторизации refresh_token")
    void test11() {
        stubOkResponse();
        Map<String, Object> result = restApiSberBuisness.sendForm(REFRESH_TOKEN.getName(), "code-123");
        assertThat(result).isNotNull();
        assertThat(result).containsKey("access_token");
        assertThat(result.get("access_token")).isEqualTo("mock-jwt-token");
        assertThat(result).containsEntry("token_type", "Bearer");
        assertThat((Integer) result.get("expires_in")).isEqualTo(3600);
    }

    @Test
    @DisplayName("Тест 1 обработки исключения при отправке формы")
    void test2() {
        stubGenericException();

        assertThatThrownBy(() -> restApiSberBuisness.sendForm("authorization_code", "code-123"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ошибка при вызове SBID");
    }

    @Test
    @DisplayName("Тест 2 обработки исключения при отправке формы")
    void test0() {
        stubWebClientResponseException2();
        assertThatThrownBy(() -> restApiSberBuisness.sendForm("authorization_code", "code-123"))
                .isInstanceOf(BadResponseException.class)
                .hasMessageContaining("Пустой или невалидный ответ от SBID");
    }

    @Test
    @DisplayName("Тест 2 обработки исключения при отправке формы")
    void test() {
        stubWebClientResponseException();
        assertThatThrownBy(() -> restApiSberBuisness.sendForm("authorization_code", "code-123"))
                .isInstanceOf(BadResponseException.class)
                .hasMessageContaining("Ошибка авторизации: unknown_error — No description");
    }

    @Test
    @DisplayName("Тест 3 обработки исключения при отправке формы")
    void test5() {
        String authToken = "crash.token";
        when(sberBusinessIdConfiguration.getRestUrlSbid()).thenReturn("https://iftfintech.testsbi.sberbank.ru:9443");
        when(sberBusinessIdConfiguration.getUserInfoMetod()).thenReturn("/ic/sso/api/v2/oauth/user-info");
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenThrow(new RuntimeException("Connection failed"));
        BadResponseException thrown = assertThrows(BadResponseException.class, () -> {
            restApiSberBuisness.getUserInfo(authToken);
        });
        assertThat(thrown.getMessage()).contains("Ошибка при вызове SBID");
    }

    @Test
    @DisplayName("Тест 4 обработки исключения при отправке формы")
    void test4() {
        String authToken = "crash.token";
        when(sberBusinessIdConfiguration.getRestUrlSbid()).thenReturn("https://iftfintech.testsbi.sberbank.ru:9443");
        when(sberBusinessIdConfiguration.getUserInfoMetod()).thenReturn("/ic/sso/api/v2/oauth/user-info");
        WebClientResponseException exception = WebClientResponseException.create(
                500,
                "Internal Server Error",
                HttpHeaders.EMPTY,
                "Connection failed".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );
        when(exchangeFunction.exchange(any(ClientRequest.class))).thenThrow(exception);
        BadResponseException thrown = assertThrows(BadResponseException.class, () -> {
            restApiSberBuisness.getUserInfo(authToken);
        });
        assertThat(thrown.getMessage()).contains("Ошибка авторизации: unknown_error");
    }


    private void stubGenericException() {
        when(exchangeFunction.exchange(any(ClientRequest.class)))
                .thenThrow(new RuntimeException("Simulated generic error"));
    }

    private void stubOkResponse() {
        ClientResponse response = ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body("{\n" +
                        "    \"access_token\": \"mock-jwt-token\",\n" +
                        "    \"token_type\": \"Bearer\",\n" +
                        "    \"expires_in\": 3600,\n" +
                        "    \"refresh_token\": \"b89FABe82db60A13A81b160EBde2A5FfF815Dd\",\n" +
                        "    \"scope\": \"openid DICT GET_CRYPTO_INFO GET_CRYPTO_INFO_EIO GET_STATEMENT_ACCOUNT GET_STATEMENT_TRANSACTION OrgName PAY_DOC_RU PAY_DOC_RU_INVOICE PAY_DOC_RU_INVOICE_ANY PAY_DOC_RU_INVOICE_BUDGET accounts activityType email individualExecutiveAgency inn name offerExpirationDate orgActualAddress orgFullName orgJuridicalAddress orgKpp orgLawForm orgLawFormShort orgOgrn orgOktmo phone_number terBank userPosition\",\n" +
                        "    \"id_token\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJnb3N0MzQuMTAtMjAxMiJ9.e30.MIAGCSqGSIb3DQEHAqCAMIACAQExDDAKBggqhQMHAQECAjCABgkqhkiG9w0BBwEAAKCAMIIE1jCCBIOgAwIBAgIKfUYyDXeXdAtDAjAKBggqhQMHAQEDAjCCAUoxFTATBgUqhQNkBBIKNzcwMjIzNTEzMzEbMBkGCSqGSIb3DQEJARYMdGVzdEBjYi50ZXN0MRgwFgYFKoUDZAESDTEwMzc3MDAwMTMwMjAxCzAJBgNVBAYTAlJVMRgwFgYDVQQIDA83NyDQnNC-0YHQutCy0LAxGTAXBgNVBAcMENCzLiDQnNC-0YHQutCy0LAxKTAnBgNVBAkMINGD0LsuINCd0LXQs9C70LjQvdC90LDRjywg0LQuIDEyMS8wLQYDVQQLDCbQotC10YHRgtC-0LLRi9C5INCR0LDQvdC6INCg0L7RgdGB0LjQuDEtMCsGA1UECgwk0KPQpiDQptCRINCi0JXQodCiINCa0KHQmtCfICjQmNCk0KIpMS0wKwYDVQQDDCTQo9CmINCm0JEg0KLQldCh0KIg0JrQodCa0J8gKNCY0KTQoikwHhcNMjUwNTEyMTIzMTAwWhcNMjYwODEyMTIzMjUzWjCCAS4xHzAdBgNVBAoMFtCh0LHQtdGA0KLQtdGF0KLQtdGB0YIxCzAJBgNVBAYTAlJVMRwwGgYDVQQIDBM3NyDQsy4g0JzQvtGB0LrQstCwMRkwFwYDVQQHDBDQsy4g0JzQvtGB0LrQstCwMSYwJAYDVQQJDB3Rg9C7LiDQktCw0LLQuNC70L7QstCwLCDQtC4xOTEVMBMGBSqFA2QEEgo3NzM2NjMyNDY3MRgwFgYFKoUDZAESDTExMTc3NDY1MzM5MjYxHzAdBgNVBAMMFtCh0LHQtdGA0KLQtdGF0KLQtdGB0YIxSzBJBgNVBAsMQtCi0LXRgdGC0L7QstC-0LUg0L_QvtC00YDQsNC30LTQtdC70LXQvdC40LUg0KHQsdC10YDQotC10YXQotC10YHRgjBmMB8GCCqFAwcBAQEBMBMGByqFAwICIwIGCCqFAwcBAQICA0MABECv3-4mxDunwUK1tfmVdVF3igqH1s6jkmwWWaUIxCOifci448ybI2T6BSWDrAQhD-zcn_x0F2Y4VpfFSlmoKSBYo4IBWjCCAVYwOgYHKoUDA3sDAQQvDC1TQlRKM0o4UGHQotC10YHRgtCf0J_QoNCR0K7Qm0RpZ0IyQtC60LLQmNCk0KIwDAYDVR0TAQH_BAIwADAOBgNVHQ8BAf8EBAMCA_gwEwYDVR0lBAwwCgYIKwYBBQUHAwEwHQYFKoUDZG8EFAwS0JHQuNC60YDQuNC_0YIgNS4wMBMGA1UdIAQMMAowCAYGKoUDZHEBMB0GA1UdDgQWBBT8YaU5R6yub4indihzgNSP2wlkujBEBgNVHR8EPTA7MDmgN6A1hjNodHRwOi8vd3d3LnNiZXJiYW5rLnJ1L2NhL2lzc3VzZXJfdGVzdF8wMENBMDQ4Ni5jcmwwHwYDVR0jBBgwFoAU2EmV4B3sy7oAB1wMCEC5E-uTH_0wKwYDVR0QBCQwIoAPMjAyNTA1MTIxMjMxMDBagQ8yMDI2MDgxMjEyMzI1M1owCgYIKoUDBwEBAwIDQQAFrKRC8teQH0wU_fRXF57TBewrFdwy1WFlX7_Atqu75gi1MLVveitbgc7ha2iMqEjdC62ShbZDa_iD4Bzy3f3_MIIHtTCCB2KgAwIBAgIKfJimSpTVJc5RgzAKBggqhQMHAQEDAjCCAVIxHDAaBgkqhkiG9w0BCQEWDXRlc3RAbWNyLnRlc3QxCzAJBgNVBAYTAlJVMRgwFgYDVQQIDA83NyDQnNC-0YHQutCy0LAxGTAXBgNVBAcMENCzLiDQnNC-0YHQutCy0LAxTTBLBgNVBAkMRNCi0LXRgdGC0L7QstCw0Y8g0L3QsNCx0LXRgNC10LbQvdCw0Y8sINC00L7QvCAxMCwg0YHRgtGA0L7QtdC90LjQtSAyMTcwNQYDVQQKDC7QotC10YHRgtC-0LLRi9C5INC60L7RgNC10L3RjCDQnNC40L3RhtC40YTRgNGLMRgwFgYFKoUDZAESDTUyMjkxMzQzMzI2MzExFTATBgUqhQNkBBIKNDQ1NTAxNzU2NjE3MDUGA1UEAwwu0KLQtdGB0YLQvtCy0YvQuSDQutC-0YDQtdC90Ywg0JzQuNC90YbQuNGE0YDRizAeFw0yNDEwMTcwMDAwMDBaFw0zOTEwMTcwMDAwMDBaMIIBSjEVMBMGBSqFA2QEEgo3NzAyMjM1MTMzMRswGQYJKoZIhvcNAQkBFgx0ZXN0QGNiLnRlc3QxGDAWBgUqhQNkARINMTAzNzcwMDAxMzAyMDELMAkGA1UEBhMCUlUxGDAWBgNVBAgMDzc3INCc0L7RgdC60LLQsDEZMBcGA1UEBwwQ0LMuINCc0L7RgdC60LLQsDEpMCcGA1UECQwg0YPQuy4g0J3QtdCz0LvQuNC90L3QsNGPLCDQtC4gMTIxLzAtBgNVBAsMJtCi0LXRgdGC0L7QstGL0Lkg0JHQsNC90Log0KDQvtGB0YHQuNC4MS0wKwYDVQQKDCTQo9CmINCm0JEg0KLQldCh0KIg0JrQodCa0J8gKNCY0KTQoikxLTArBgNVBAMMJNCj0KYg0KbQkSDQotCV0KHQoiDQmtCh0JrQnyAo0JjQpNCiKTBmMB8GCCqFAwcBAQEBMBMGByqFAwICIwIGCCqFAwcBAQICA0MABEBSO4-uj7OqK1qDZYnUgms2geVlOKrJRenhz7cwhWH2PxmE3bA29hUgLMakqbiDoFU8aKEviQBeDmwwIu_RsIQWo4IEFTCCBBEwDAYFKoUDZHIEAwIBADAdBgNVHQ4EFgQU2EmV4B3sy7oAB1wMCEC5E-uTH_0wRgYIKwYBBQUHAQEEOjA4MDYGCCsGAQUFBzAChipodHRwOi8vd3d3LnNiZXJiYW5rLnJ1L2NhL3Rlc3Qucm9vdC5tYy5jZXIwKwYDVR0QBCQwIoAPMjAyNDEwMTcxMzI1MjNagQ8yMDI3MTAxNzEzMjUyM1owOwYDVR0fBDQwMjAwoC6gLIYqaHR0cDovL3d3dy5zYmVyYmFuay5ydS9jYS90ZXN0LnJvb3QubWMuY3JsMIH1BgUqhQNkcASB6zCB6Aw00J_QkNCa0JwgwqvQmtGA0LjQv9GC0L7Qn9GA0L4gSFNNwrsg0LLQtdGA0YHQuNC4IDIuMAxD0J_QkNCaIMKr0JPQvtC70L7QstC90L7QuSDRg9C00L7RgdGC0L7QstC10YDRj9GO0YnQuNC5INGG0LXQvdGC0YDCuww10JfQsNC60LvRjtGH0LXQvdC40LUg4oSWIDE0OS8zLzIvMi8yMyDQvtGCIDAyLjAzLjIwMTgMNNCX0LDQutC70Y7Rh9C10L3QuNC1IOKEliAxNDkvNy82LTQ0OSDQvtGCIDMwLjEyLjIwMjEwXQYFKoUDZG8EVAxS0JDQn9CaICLQodC40LPQvdCw0YLRg9GA0LAt0LrQu9C40LXQvdGCIEwiINCy0LXRgNGB0LjRjyA2ICjQuNGB0L_QvtC70L3QtdC90LjQtSAzKTAnBgNVHSAEIDAeMAgGBiqFA2RxATAIBgYqhQNkcQIwCAYGKoUDZHEDMBIGA1UdEwEB_wQIMAYBAf8CAQAwCwYDVR0PBAQDAgEGMIIBjQYDVR0jBIIBhDCCAYCAFGGMgkMTM5OpEVJM-MNFh9rJTJYJoYIBWqSCAVYwggFSMRwwGgYJKoZIhvcNAQkBFg10ZXN0QG1jci50ZXN0MQswCQYDVQQGEwJSVTEYMBYGA1UECAwPNzcg0JzQvtGB0LrQstCwMRkwFwYDVQQHDBDQsy4g0JzQvtGB0LrQstCwMU0wSwYDVQQJDETQotC10YHRgtC-0LLQsNGPINC90LDQsdC10YDQtdC20L3QsNGPLCDQtNC-0LwgMTAsINGB0YLRgNC-0LXQvdC40LUgMjE3MDUGA1UECgwu0KLQtdGB0YLQvtCy0YvQuSDQutC-0YDQtdC90Ywg0JzQuNC90YbQuNGE0YDRizEYMBYGBSqFA2QBEg01MjI5MTM0MzMyNjMxMRUwEwYFKoUDZAQSCjQ0NTUwMTc1NjYxNzA1BgNVBAMMLtCi0LXRgdGC0L7QstGL0Lkg0LrQvtGA0LXQvdGMINCc0LjQvdGG0LjRhNGA0YuCCntpmvKmVL6PTK0wCgYIKoUDBwEBAwIDQQBRy88YZn6m72f2tzlohKvviADuKCJM2YTCmcO0c-0bJz2JiwSnd50SRZahD8uObT_UqfKLeUxev07TLz4Ayb-OAAAxggPjMIID3wIBATCCAVowggFKMRUwEwYFKoUDZAQSCjc3MDIyMzUxMzMxGzAZBgkqhkiG9w0BCQEWDHRlc3RAY2IudGVzdDEYMBYGBSqFA2QBEg0xMDM3NzAwMDEzMDIwMQswCQYDVQQGEwJSVTEYMBYGA1UECAwPNzcg0JzQvtGB0LrQstCwMRkwFwYDVQQHDBDQsy4g0JzQvtGB0LrQstCwMSkwJwYDVQQJDCDRg9C7LiDQndC10LPQu9C40L3QvdCw0Y8sINC0LiAxMjEvMC0GA1UECwwm0KLQtdGB0YLQvtCy0YvQuSDQkdCw0L3QuiDQoNC-0YHRgdC40LgxLTArBgNVBAoMJNCj0KYg0KbQkSDQotCV0KHQoiDQmtCh0JrQnyAo0JjQpNCiKTEtMCsGA1UEAwwk0KPQpiDQptCRINCi0JXQodCiINCa0KHQmtCfICjQmNCk0KIpAgp9RjINd5d0C0MCMAoGCCqFAwcBAQICoIICHjAYBgkqhkiG9w0BCQMxCwYJKoZIhvcNAQcBMBwGCSqGSIb3DQEJBTEPFw0yNTA5MzAwNjIwMTdaMC8GCSqGSIb3DQEJBDEiBCDvD-NC-u3FkyklbS7fT0rNIRox8_WQD8BuBQkM1tAIKTCCAbEGCyqGSIb3DQEJEAIvMYIBoDCCAZwwggGYMIIBlDAKBggqhQMHAQECAgQgAHvkpnJYXQz3J1NPZi9W2z48SHiJYOwgLqiF2LKTH4swggFiMIIBUqSCAU4wggFKMRUwEwYFKoUDZAQSCjc3MDIyMzUxMzMxGzAZBgkqhkiG9w0BCQEWDHRlc3RAY2IudGVzdDEYMBYGBSqFA2QBEg0xMDM3NzAwMDEzMDIwMQswCQYDVQQGEwJSVTEYMBYGA1UECAwPNzcg0JzQvtGB0LrQstCwMRkwFwYDVQQHDBDQsy4g0JzQvtGB0LrQstCwMSkwJwYDVQQJDCDRg9C7LiDQndC10LPQu9C40L3QvdCw0Y8sINC0LiAxMjEvMC0GA1UECwwm0KLQtdGB0YLQvtCy0YvQuSDQkdCw0L3QuiDQoNC-0YHRgdC40LgxLTArBgNVBAoMJNCj0KYg0KbQkSDQotCV0KHQoiDQmtCh0JrQnyAo0JjQpNCiKTEtMCsGA1UEAwwk0KPQpiDQptCRINCi0JXQodCiINCa0KHQmtCfICjQmNCk0KIpAgp9RjINd5d0C0MCMAoGCCqFAwcBAQEBBEA60fTvGtGdbGNUDvPDMxoolCbUApcOVYq18e-HJwKrlhfyZC1UUjq_vrRxFznk35jGSjiazvkpINbkf0oqb78ZoQAAAAAAAAA\"\n" +
                        "}").build();
        when(exchangeFunction.exchange(any(ClientRequest.class))).thenReturn(Mono.just(response));
    }

    private void stubWebClientResponseException() {
        WebClientResponseException exception = WebClientResponseException.create(
                500,
                "Internal Server Error",
                HttpHeaders.EMPTY,
                "Server error".getBytes(StandardCharsets.UTF_8),
                null
        );
        when(exchangeFunction.exchange(any(ClientRequest.class))).thenThrow(exception);
    }

    private void stubWebClientResponseException2() {
        WebClientResponseException exception = WebClientResponseException.create(
                500,
                "Internal Server Error",
                HttpHeaders.EMPTY,
                null,
                null
        );
        when(exchangeFunction.exchange(any(ClientRequest.class))).thenThrow(exception);
    }
}
