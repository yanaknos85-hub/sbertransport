package ru.sber.transport.contractor.config;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.contractor.database.model.ServiceType;
import ru.sber.transport.contractor.dto.EnumRusNameDTO;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.sber.transport.contractor.database.model.ContractorType.*;
import static ru.sber.transport.contractor.database.model.ServiceType.AUTOSERVICE;
import static ru.sber.transport.contractor.database.model.ServiceType.EMPLOYEE_TRANSPORTATION;

@UnitTest
@IsolatedTest
@Feature("app_platform_contractor")
@DisplayName("Тестирование конфига интеграции")
class IntegrationConfigTest {

    private IntegrationConfig config;

    @BeforeEach
    void setup() {
        config = new IntegrationConfig();
        config.setIsInternal(true);
        config.setAutoserviceExternalUrl("https://external-autoservice.example.ru");
        config.setAutoserviceInternalUrl("https://internal-autoservice.example.ru");
        config.setDispatcherPassengerInternalUrl("https://employee-dispatcher.example.ru");
        config.setDispatcherCargoInternalUrl("https://cargo-dispatcher.example.ru");
        config.setDispatcherPassengerExternalUrl("https://external-employee-dispatcher.example.ru");
        config.setDispatcherCargoExternalUrl("https://external-cargo-dispatcher.example.ru");
        config.setAutoserviceExternalClientUrl("a-ext");
        config.setAutoserviceInternalClientUrl("a-int");
        config.setDispatcherExternalClientUrl("d-ext");
        config.setDispatcherInternalClientUrl("d-int");
    }

    @Test
    @DisplayName("Тестирование getContractorTypes для внутренних сервисов")
    void testGetClientUrl() {
        assertThat(config.getClientUrl(AUTOSERVICE_EXTERNAL)).isEqualTo("a-ext");
        assertThat(config.getClientUrl(AUTOSERVICE_INTERNAL)).isEqualTo("a-int");
        assertThat(config.getClientUrl(DISPATCHER_EXTERNAL)).isEqualTo("d-ext");
        assertThat(config.getClientUrl(DISPATCHER_INTERNAL)).isEqualTo("d-int");
    }

    @Test
    @DisplayName("Тестирование получение ссылки для нужного метода интеграции")
    void testGetContractorTypesForInternalService() {
        config = new IntegrationConfig();
        config.setIsInternal(true);
        assertThat(config.getContractorTypes(EMPLOYEE_TRANSPORTATION))
                .map(EnumRusNameDTO::rusName)
                .containsExactlyInAnyOrderElementsOf(List.of(
                        API.getInternalRusName(),
                        DISPATCHER_INTERNAL.getInternalRusName(),
                        DISPATCHER_EXTERNAL.getInternalRusName()));
    }

    @Test
    @DisplayName("Тестирование getContractorTypes для внешних сервисов")
    void testGetContractorTypesForExternalService() {
        config = new IntegrationConfig();
        config.setIsInternal(false);
        assertThat(config.getContractorTypes(AUTOSERVICE))
                .map(EnumRusNameDTO::rusName)
                .containsExactlyInAnyOrderElementsOf(List.of(
                        API.getExternalRusName(),
                        AUTOSERVICE_INTERNAL.getExternalRusName(),
                        OFFLINE.getExternalRusName()));
    }

    @Nested
    @DisplayName("Тестирование работы в режиме Банка (isInternal=true)")
    class InternalModeTests {

        @Test
        @DisplayName("При выборе внутреннего автоконтрактора должно вернуть внутренний URL")
        void shouldReturnInternalAutoserviceUrl() {
            var result = config.getUrlByContractorTypeAndServiceType(AUTOSERVICE_INTERNAL, EMPLOYEE_TRANSPORTATION);
            assertEquals("https://internal-autoservice.example.ru", result);
        }

        @Test
        @DisplayName("При выборе внешнего автоконтрактора должно вернуть внешний URL")
        void shouldReturnExternalAutoserviceUrl() {
            var result = config.getUrlByContractorTypeAndServiceType(AUTOSERVICE_EXTERNAL, EMPLOYEE_TRANSPORTATION);
            assertEquals("https://external-autoservice.example.ru", result);
        }

        @Test
        @DisplayName("Должен выбрать правильный URL для пассажира (внутренней диспетчерской)")
        void shouldReturnCorrectUrlForEmployeeTransportation() {
            var result = config.getUrlByContractorTypeAndServiceType(DISPATCHER_INTERNAL, EMPLOYEE_TRANSPORTATION);
            assertEquals("https://employee-dispatcher.example.ru", result);
        }

        @Test
        @DisplayName("Должен выбрать правильный URL для перевозки грузов (внутренней диспетчерской)")
        void shouldReturnCorrectUrlForCargoTransportation() {
            var result = config.getUrlByContractorTypeAndServiceType(DISPATCHER_INTERNAL, ServiceType.CARGO_TRANSPORTATION);
            assertEquals("https://cargo-dispatcher.example.ru", result);
        }

        @Test
        @DisplayName("Должен выбрать правильный URL для перевозки грузов (внешней диспетчерской)")
        void shouldReturnCorrectUrlForCargoTransportationExternal() {
            var result = config.getUrlByContractorTypeAndServiceType(DISPATCHER_EXTERNAL, ServiceType.CARGO_TRANSPORTATION);
            assertEquals("https://external-cargo-dispatcher.example.ru", result);
        }

        @Test
        @DisplayName("Должен выбрать правильный URL для перевозки пассажиров (внешней диспетчерской)")
        void shouldReturnCorrectUrlForEmployeeTransportationExternal() {
            var result = config.getUrlByContractorTypeAndServiceType(DISPATCHER_EXTERNAL, EMPLOYEE_TRANSPORTATION);
            assertEquals("https://external-employee-dispatcher.example.ru", result);
        }

        @Test
        @DisplayName("Выбран неправильный сервис")
        void shouldReturnIllegalArgumentExceptionAutoserviceDispatcherExternal() {
            assertThrows(IllegalArgumentException.class,
                    () -> config.getUrlByContractorTypeAndServiceType(DISPATCHER_EXTERNAL, AUTOSERVICE));
        }

        @Test
        @DisplayName("Выбран неправильный сервис")
        void shouldReturnIllegalArgumentExceptionAutoserviceDispatcherInternal() {
            assertThrows(IllegalArgumentException.class,
                    () -> config.getUrlByContractorTypeAndServiceType(DISPATCHER_INTERNAL, AUTOSERVICE));
        }

    }

    @Nested
    @DisplayName("Тестирование работы в режиме дочерних компаний (isInternal=false)")
    class ExternalModeTests {

        @BeforeEach
        void changeToExternalMode() {
            config.setIsInternal(false);
        }

        @Test
        @DisplayName("При выборе внутреннего автоконтрактора должно вернуть внутренний URL")
        void shouldReturnInternalAutoserviceUrlInExternalMode() {
            var result = config.getUrlByContractorTypeAndServiceType(AUTOSERVICE_INTERNAL, AUTOSERVICE);
            assertEquals("https://internal-autoservice.example.ru", result);
        }

        @Test
        @DisplayName("При выборе внутреннего автоконтрактора должно вернуть внутренний URL")
        void shouldReturnInternalDispatcherUrlInExternalMode() {
            var result = config.getUrlByContractorTypeAndServiceType(DISPATCHER_INTERNAL, EMPLOYEE_TRANSPORTATION);
            assertEquals("https://employee-dispatcher.example.ru", result);
        }

        @Test
        @DisplayName("Должен выбрать правильный URL для пассажирских перевозок от внешней диспетчерской")
        void shouldReturnExternalEmployeeDispatcherUrl() {
            assertThrows(IllegalArgumentException.class, () ->
                    config.getUrlByContractorTypeAndServiceType(DISPATCHER_EXTERNAL, EMPLOYEE_TRANSPORTATION));
        }

        @Test
        @DisplayName("Должен выбрать правильный URL для грузовых перевозок от внешней диспетчерской")
        void shouldReturnExternalCargoDispatcherUrl() {
            assertThrows(IllegalArgumentException.class, () ->
                    config.getUrlByContractorTypeAndServiceType(DISPATCHER_EXTERNAL, ServiceType.CARGO_TRANSPORTATION));
        }

    }

    @Test
    @DisplayName("Проверка режима неопределенности (null value of isInternal)")
    void shouldThrowExceptionIfIsInternalIsNull() {
        config.setIsInternal(null);
        assertThrows(IllegalStateException.class, () ->
                config.getUrlByContractorTypeAndServiceType(DISPATCHER_INTERNAL, EMPLOYEE_TRANSPORTATION));
    }

}