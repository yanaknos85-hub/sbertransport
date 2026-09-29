package ru.sber.transport.dispatcher.testutils;

import ru.sber.transport.dispatcher.database.model.Contractor;

import java.util.Optional;
import java.util.UUID;

/**
 * Класс создан для того, чтобы вынести создание сущностей в одно место, что то вроде фабрики. Таким образом, при
 * изменении модели, править создание и связи надо будет только здесь
 */
public final class TestContractors {

    public static Contractor createTestContractor() {
        return createTestContractor(0);
    }

    public static Contractor createTestContractor(int increment) {
        return Contractor.builder()
                .digitId(100L + increment)
                .name("Contractor" + increment)
                .tin("123456789" + increment)
                .msrn("123456789012" + increment)
                .technicalAccountOwnerEmail("email" + increment + "@mail.ru")
                .technicalAccountOwner("Owner" + increment)
                .vehicleCountNorm(1000)
                .build();
    }


    public static String createTestContractorDto(int increment) {
        return createContractorDto(
                "Contractor" + increment,
                "123456789" + increment,
                createDispatcherData(
                        null,
                        "lastName" + increment,
                        "firstName" + increment,
                        "patronymic" + increment,
                        "+7972118995" + increment,
                        "email" + increment + "@list.ru"),
                "ownerEmail" + increment + "@mail.ru",
                "technicalAccountOwner" + increment
        );
    }

    public static String createDispatcherData(UUID id, String lastName, String firstName, String patronymic, String phone, String email) {
        return
                """
                        {
                            "id": %s,
                            "lastName": %s,
                            "firstName": %s,
                            "patronymic": %s,
                            "phone": %s,
                            "email": %s
                        }
                        """.formatted(
                        toJsonString(id),
                        toJsonString(lastName),
                        toJsonString(firstName),
                        toJsonString(patronymic),
                        toJsonString(phone),
                        toJsonString(email)
                );
    }

    public static String createContractorDto(String name, String tin, String dispatcherData, String technicalAccountOwnerEmail, String technicalAccountOwner) {
        return """
                {
                 "name": %s,
                 "tin": %s,
                 "msrn": %s,
                 "mainDispatcher": %s,
                 "technicalAccountOwnerEmail": %s,
                 "technicalAccountOwner": %s,
                 "technicalAccountLogin": %s,
                 "technicalAccountPassword": %s
                }
                """.formatted(
                toJsonString(name),
                toJsonString(tin),
                toJsonString("1234567890123"),
                dispatcherData,
                toJsonString(technicalAccountOwnerEmail),
                toJsonString(technicalAccountOwner),
                toJsonString("ddd5bcb0-0135-4431-89b0-ae91823bdb73"),
                toJsonString("password")
        );
    }

    private static String toJsonString(UUID source) {
        return Optional.ofNullable(source).map("\"%s\""::formatted).orElse(null);
    }

    private static String toJsonString(String source) {
        return Optional.ofNullable(source).map("\"%s\""::formatted).orElse(null);
    }

    public static Contractor incrementContractor(Contractor c, int i) {
        var incrementString = String.valueOf(i);
        return Contractor.builder()
                .name(c.getName() + i)
                .tin(c.getTin() + i)
                .technicalAccountOwnerEmail("email" + incrementString + "@mail.ru")
                .technicalAccountOwner("Owner" + incrementString)
                .build();
    }


}
