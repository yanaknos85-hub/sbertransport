package ru.sber.transport.contractor.testutils;

import ru.sber.transport.contractor.database.model.Contractor;
import ru.sber.transport.contractor.database.model.ContractorType;
import ru.sber.transport.contractor.database.model.EmailIntegrationParams;
import ru.sber.transport.contractor.database.model.IntegrationType;
import ru.sber.transport.contractor.database.model.JsonIntegrationParams;
import ru.sber.transport.contractor.database.model.ServiceType;
import ru.sber.transport.contractor.dto.IntegrationTypeDto;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static ru.sber.transport.contractor.testutils.TestUtils.replaceEnd;

/**
 * Класс создан для того, чтобы вынести создание сущностей в одно место, что то вроде фабрики. Таким образом, при
 * изменении модели, править создание и связи надо будет только здесь
 */
public final class TestContractors {
    private static final String MSRN_FORMAT = "%013d";
    private static final String TIN_FORMAT = "%010d";
    private static final String PHONE_FORMAT = "8 (%03d) 123 12 12";

    public static Contractor createTestContractor() {
        return createTestContractor(0, ContractorType.DISPATCHER_EXTERNAL, ServiceType.EMPLOYEE_TRANSPORTATION, IntegrationType.EMAIL_XML_API);
    }

    public static Contractor createTestInternalAutoPark() {
        return createTestContractor(0, ContractorType.DISPATCHER_INTERNAL, ServiceType.INTERNAL_AUTO_PARK, IntegrationType.JSON_API_1_0);
    }

    public static Contractor createTestContractor(int increment, ContractorType contractorType, ServiceType serviceType, IntegrationType integrationType) {
        return Contractor.builder()
                .msrn(String.format(MSRN_FORMAT, increment))
                .integrationType(integrationType)
                .digitId(100L + increment)
                .name("Contractor" + increment)
                .tin(String.format(TIN_FORMAT, increment))
                .contactPersonFirstName("ContactFirstName" + increment)
                .contactPersonLastName("ContactLastName" + increment)
                .contactPersonPatronymic("ContactPatronymic" + increment)
                .contactPersonEmail("test@test.com" + increment)
                .contactPersonPhone(String.format(PHONE_FORMAT, increment))
                .integrationParams(EmailIntegrationParams.builder()
                        .contractorRusName("Русское")
                        .contractorName("latin")
                        .email("aaa@bbb.com")
                        .build())
                .jsonIntegrationParams(JsonIntegrationParams.builder()
                        .password("test")
                        .build())
                .rating(0)
                .organizations(new HashSet<>(Set.of(UUID.randomUUID())))
                .serviceType(serviceType)
                .contractorType(contractorType)
                .vehicleCountNorm(300)
                .build();
    }


    public static String createTestContractorDto(int increment) {
        return createContractorDto(
                "Contractor" + increment,
                String.format(MSRN_FORMAT, increment),
                String.format(TIN_FORMAT, increment),
                "John-" + increment,
                "Doe-" + increment,
                "Smith-" + increment,
                String.format(PHONE_FORMAT, increment),
                0,
                List.of(UUID.randomUUID()),
                createIntegrationParams("contractorName" + increment, "contrRusName" + increment, "email" + increment + "@email.ru"),
                IntegrationTypeDto.EMAIL_XML_API,
                "test@test.tu" + increment,
                300 + increment
        );
    }

    // Метод для обычного формата интеграционных параметров
    public static String createContractorDto(String name, String msrn, String tin,
                                             String contactPersonFirstName, String contactPersonLastName,
                                             String contactPersonPatronymic, String contactPersonPhone,
                                             int rating, List<UUID> regionIds,
                                             String integrationParams, IntegrationTypeDto type,
                                             String contactPersonEmail, Integer vehicleCountNorm) {
        return createContractorDto(name, msrn, tin, contactPersonFirstName, contactPersonLastName,
                contactPersonPatronymic, contactPersonPhone, rating, regionIds,
                integrationParams, createJsonData("https://test.ru", "login", "password"), type, null, null, contactPersonEmail, vehicleCountNorm);
    }

    // Метод для JSON-интеграционных параметров
    public static String createContractorJsonDto(String name, String msrn, String tin,
                                                 String contactPersonFirstName, String contactPersonLastName,
                                                 String contactPersonPatronymic, String contactPersonPhone,
                                                 int rating, List<UUID> regionIds,
                                                 String jsonIntegrationParams, IntegrationTypeDto type,
                                                 String contactPersonEmail, Integer vehicleCountNorm) {
        return createContractorDto(name, msrn, tin, contactPersonFirstName, contactPersonLastName,
                contactPersonPatronymic, contactPersonPhone, rating, regionIds,
                null, jsonIntegrationParams, type, null, null, contactPersonEmail, vehicleCountNorm);
    }

    // Метод для передачи диспетчера
    public static String createContractorDto(String name, String msrn, String tin,
                                             String contactPersonFirstName, String contactPersonLastName,
                                             String contactPersonPatronymic, String contactPersonPhone,
                                             int rating, List<UUID> regionIds,
                                             String dispatcherData, UUID dispatcherId, IntegrationTypeDto type,
                                             String contactPersonEmail, Integer vehicleCountNorm) {
        return createContractorDto(name, msrn, tin, contactPersonFirstName, contactPersonLastName,
                contactPersonPatronymic, contactPersonPhone, rating, regionIds,
                null, null, type, dispatcherData, dispatcherId, contactPersonEmail, vehicleCountNorm);
    }

    public static String createDispatcherData(String lastName, String firstName, String patronymic, String phone, String email) {
        return
                """
                        {
                            "lastName": %s,
                            "firstName": %s,
                            "patronymic": %s,
                            "phone": %s,
                            "email": %s
                        }
                        """.formatted(
                        toJsonString(lastName),
                        toJsonString(firstName),
                        toJsonString(patronymic),
                        toJsonString(phone),
                        toJsonString(email)
                );
    }

    public static String createJsonData(String url, String login, String password) {
        return
                """
                        {
                            "url": %s,
                            "login": %s,
                            "password": %s
                        }
                        """.formatted(
                        toJsonString(url),
                        toJsonString(login),
                        toJsonString(password)
                );
    }

    public static String createContractorDto(String name, String msrn, String tin,
                                             String contactPersonFirstName, String contactPersonLastName,
                                             String contactPersonPatronymic, String contactPersonPhone,
                                             Integer rating, List<UUID> regionIds,
                                             String integrationParams, String jsonIntegrationParams,
                                             IntegrationTypeDto integrationType, String dispatcherData, UUID dispatcherId,
                                             String contactPersonEmail, Integer vehicleCountNorm) {
        return """
                {
                 "name": %s,
                 "msrn": %s,
                 "tin": %s,
                 "contactPersonFirstName": %s,
                 "contactPersonLastName": %s,
                 "contactPersonPatronymic": %s,
                 "contactPersonPhone": %s,
                 "contactPersonEmail": %s,
                 "rating": %s,
                 "regionIds": %s,
                 "integrationParams": %s,
                 "jsonIntegrationParams": %s,
                 "integrationType": %s,
                 "mainDispatcher": %s,
                 "mainDispatcherId": %s,
                 "serviceType": %s,
                 "contractorType": %s,
                 "vehicleCountNorm" : %s
                }
                """.formatted(
                toJsonString(name),
                toJsonString(msrn),
                toJsonString(tin),
                toJsonString(contactPersonFirstName),
                toJsonString(contactPersonLastName),
                toJsonString(contactPersonPatronymic),
                toJsonString(contactPersonPhone),
                toJsonString(contactPersonEmail),
                toJsonString(rating),
                toJsonString(regionIds),
                integrationParams,
                jsonIntegrationParams,
                toJsonString(integrationType),
                dispatcherData,
                toJsonString(dispatcherId),
                toJsonString(ServiceType.EMPLOYEE_TRANSPORTATION),
                toJsonString(ContractorType.API),
                toJsonString(vehicleCountNorm)
        );
    }

    private static List<String> toJsonString(List<UUID> regionIds) {
        return Optional.ofNullable(regionIds).orElseGet(List::of).stream().map(TestContractors::toJsonString).toList();
    }

    private static String toJsonString(Integer source) {
        return Optional.ofNullable(source).map("%d"::formatted).orElse(null);
    }

    private static String toJsonString(UUID source) {
        return Optional.ofNullable(source).map("\"%s\""::formatted).orElse(null);
    }

    private static String toJsonString(String source) {
        return Optional.ofNullable(source).map("\"%s\""::formatted).orElse(null);
    }

    private static String toJsonString(Enum<?> source) {
        return Optional.ofNullable(source).map(Enum::name).map("\"%s\""::formatted).orElse(null);
    }

    public static String createIntegrationParams(String contractorName, String contractorRusName, String email) {
        return
                """
                        {
                            "contractorName": %s,
                            "contractorRusName": %s,
                            "email":%s
                        }
                        """.formatted(
                        toJsonString(contractorName),
                        toJsonString(contractorRusName),
                        toJsonString(email)
                );
    }

    public static String createTestContractorDto() {
        return createTestContractorDto(0);
    }

    public static Contractor incrementContractor(Contractor c, int i) {
        var incrementString = String.valueOf(i);
        return Contractor.builder()
                .msrn(replaceEnd(c.getMsrn(), incrementString))
                .tin(replaceEnd(c.getTin(), incrementString))
                .integrationType(IntegrationType.EMAIL_XML_API)
                .serviceType(ServiceType.EMPLOYEE_TRANSPORTATION)
                .rating(c.getRating() + i)
                .name(c.getName() + i)
                .contactPersonPhone(replaceEnd(c.getContactPersonPhone(), incrementString))
                .contactPersonFirstName(c.getContactPersonFirstName() + i)
                .contactPersonLastName(c.getContactPersonLastName() + i)
                .contactPersonPatronymic(c.getContactPersonPatronymic() + i)
                .integrationParams(EmailIntegrationParams.builder().email("email" + i).contractorName("name" + i).contractorRusName("rusname" + i).build())
                .build();
    }


}
