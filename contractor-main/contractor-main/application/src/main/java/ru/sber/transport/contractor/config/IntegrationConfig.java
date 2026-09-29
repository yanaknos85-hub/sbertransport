package ru.sber.transport.contractor.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import ru.sber.transport.contractor.database.model.ContractorType;
import ru.sber.transport.contractor.database.model.ServiceType;
import ru.sber.transport.contractor.dto.EnumRusNameDTO;

import java.util.Arrays;
import java.util.List;

@ConfigurationProperties(prefix = "integration")
@Getter
@Setter
@Validated
public class IntegrationConfig {

    /**
     * true - мы находимся в Банке
     * false - мы находимся в ДЗО
     */
    @NotNull
    private Boolean isInternal;

    //--- Настройки для интеграции ---

    /**
     * Url для интеграции с внешним автосервисом
     */
    private String autoserviceExternalUrl;

    /**
     * Url для интеграции с внешней диспетчерской пассажиры
     */
    private String dispatcherPassengerExternalUrl;

    /**
     * Url для интеграции с внешней диспетчерской грузы
     */
    private String dispatcherCargoExternalUrl;

    /**
     * Url для интеграции с внешней диспетчерской - все типы услуг
     */
    private String dispatcherInternalAutoParkExternalUrl;

    /**
     * Url для интеграции с внутренним автосервисом
     */
    private String autoserviceInternalUrl;

    /**
     * Url для интеграции с внутренней диспетчерской пассажиры
     */
    private String dispatcherPassengerInternalUrl;

    /**
     * Url для интеграции с внутренней диспетчерской грузы
     */
    private String dispatcherCargoInternalUrl;

    /**
     * Url для интеграции с внутренней диспетчерской - все типы услуг
     */
    private String dispatcherInternalAutoParkInternalUrl;

    //--- Настройки клиентов для межсервисного взаимодействия ---

    /**
     * url до сервиса dispatcher internal
     */
    private String dispatcherInternalClientUrl = "http://dispatcher:8080";

    /**
     * url до сервиса dispatcher external
     */
    private String dispatcherExternalClientUrl;

    /**
     * url до сервиса autoservice internal
     */
    private String autoserviceInternalClientUrl = "http://autoservice:8080";

    /**
     * url до сервиса autoservice external
     */
    private String autoserviceExternalClientUrl;


    /**
     * Получить ссылку на сервис autoservice/dispatcher
     *
     * @param contractorType метод создания котрагента
     * @return url до нужного сервиса
     */
    public String getClientUrl(ContractorType contractorType) {
        return switch (contractorType) {
            case DISPATCHER_INTERNAL -> dispatcherInternalClientUrl;
            case AUTOSERVICE_INTERNAL -> autoserviceInternalClientUrl;
            case DISPATCHER_EXTERNAL -> dispatcherExternalClientUrl;
            case AUTOSERVICE_EXTERNAL -> autoserviceExternalClientUrl;
            default -> throw new IllegalArgumentException("Unexpected contractor type: " + contractorType);
        };
    }

    /**
     * Получить ссылку для интеграции
     *
     * @param contractorType метод создания контрагента
     * @param serviceType    тип сервиса
     * @return ссылка для интеграции
     */
    public String getUrlByContractorTypeAndServiceType(ContractorType contractorType, ServiceType serviceType) {
        if (isInternal == null) {
            throw new IllegalStateException("It is not specified whether the service is external or internal, set integration.isInternal boolean");
        }
        if (isInternal) {
            return switch (contractorType) {
                case DISPATCHER_EXTERNAL -> getExternalDispatcherUrlByServiceType(serviceType);
                case DISPATCHER_INTERNAL -> getInternalDispatcherUrlByServiceType(serviceType);
                case AUTOSERVICE_EXTERNAL -> autoserviceExternalUrl;
                case AUTOSERVICE_INTERNAL -> autoserviceInternalUrl;
                default -> throw new IllegalArgumentException("Unexpected contractor type " + contractorType);
            };
        } else {
            return switch (contractorType) {
                case DISPATCHER_INTERNAL -> getInternalDispatcherUrlByServiceType(serviceType);
                case AUTOSERVICE_INTERNAL -> autoserviceInternalUrl;
                default -> throw new IllegalArgumentException("Unexpected contractor type " + contractorType);
            };
        }
    }

    public List<EnumRusNameDTO> getContractorTypes(ServiceType serviceType) {
        if (Boolean.TRUE.equals(isInternal)) {
            return Arrays.stream(ContractorType.values())
                    .filter(c -> c.getServices().contains(serviceType))
                    .filter(c -> c.getInternalRusName() != null)
                    .map(c -> new EnumRusNameDTO(c.name(), c.getInternalRusName())).toList();
        } else {
            return Arrays.stream(ContractorType.values())
                    .filter(c -> c.getServices().contains(serviceType))
                    .filter(c -> c.getExternalRusName() != null)
                    .map(c -> new EnumRusNameDTO(c.name(), c.getExternalRusName())).toList();
        }
    }

    private String getInternalDispatcherUrlByServiceType(ServiceType serviceType) {
        if (ServiceType.EMPLOYEE_TRANSPORTATION.equals(serviceType)) {
            return dispatcherPassengerInternalUrl;
        } else if (ServiceType.CARGO_TRANSPORTATION.equals(serviceType)) {
            return dispatcherCargoInternalUrl;
        } else if (ServiceType.INTERNAL_AUTO_PARK.equals(serviceType)) {
            return dispatcherInternalAutoParkInternalUrl;
        }else {
            throw new IllegalArgumentException("Wrong service type for internal dispatcher " + serviceType);
        }

    }

    private String getExternalDispatcherUrlByServiceType(ServiceType serviceType) {
        if (ServiceType.EMPLOYEE_TRANSPORTATION.equals(serviceType)) {
            return dispatcherPassengerExternalUrl;
        } else if (ServiceType.CARGO_TRANSPORTATION.equals(serviceType)) {
            return dispatcherCargoExternalUrl;
        } else if (ServiceType.INTERNAL_AUTO_PARK.equals(serviceType)) {
            return dispatcherInternalAutoParkExternalUrl;
        } else {
            throw new IllegalArgumentException("Wrong service type for external dispatcher " + serviceType);
        }

    }

}
