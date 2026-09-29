package ru.sberbank.ditsib.transport.request.mappers;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.transport.integrations.messaging.InContractorTaxiTripInProgressMessage;
import ru.sberbank.ditsib.transport.constants.DriverLicenseClass;
import ru.sberbank.ditsib.transport.request.database.model.driversData.Driver;
import ru.sberbank.ditsib.transport.request.dto.GetDriverRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.mapper.DriverDTOMapper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@UnitTest
@Isolated
@Feature("app_passenger_request")
class DriverDTOMapperTest {

    private final DriverDTOMapper mapper = Mappers.getMapper(DriverDTOMapper.class);

    @Test
    void mapDriverFromDTO() {
        Set<DriverLicenseClass> classes = new HashSet<>();
        classes.add(DriverLicenseClass.B);
        Driver driver = Driver.builder()
                .id(UUID.randomUUID())
                .autoparkId(UUID.randomUUID())
                .contactPhone("89277000000")
                .contractorId(UUID.randomUUID())
                .driverLicenseNumber("1234 567890")
                .experience("LESS_THEN_FIVE")
                .firstName("Michail")
                .lastName("Viktor")
                .patronymic("Sergeevich")
                .rating(400)
                .passport("6312 123567")
                .licenseClasses(classes).build();
        GetDriverRequestDTO mappedDTO = mapper.requestToGetDriverDTO(driver);

        assertThat(mappedDTO).isNotNull();
        assertThat(mappedDTO.getDriver().getId()).isEqualTo(driver.getId());
        assertThat(mappedDTO.getDriver().getContactPhone()).isEqualTo(driver.getContactPhone());
        assertThat(mappedDTO.getDriver().getLicenseClasses()).isEqualTo(driver.getLicenseClasses());
        assertThat(mappedDTO.getDriver().getRating()).isEqualTo(driver.getRating());
    }

    @ParameterizedTest
    @MethodSource("driverSource")
    void map(String id, String rating) {
        var source = Instancio.of(InContractorTaxiTripInProgressMessage.Driver.class)
                .set(field(InContractorTaxiTripInProgressMessage.Driver::id), id)
                .set(field(InContractorTaxiTripInProgressMessage.Driver::rating), rating)
                .create();
        var actual = mapper.map(source);
        assertThat(actual)
                .extracting(
                        Driver::getContractorId,
                        Driver::getAutoparkId,
                        Driver::getTags,
                        Driver::getLastName,
                        Driver::getFirstName,
                        Driver::getPatronymic,
                        Driver::getHumanReadableId,
                        Driver::getPassport,
                        Driver::getContactPhone,
                        Driver::getActive,
                        Driver::getRating,
                        Driver::getServiceProviderLicenseNumber,
                        Driver::getExperience,
                        Driver::getDriverLicenseNumber,
                        Driver::getLicenseClasses
                )
                .containsExactly(
                        null,
                        null,
                        Collections.emptySet(),
                        source.secName(),
                        source.name(),
                        source.patronymic(),
                        null,
                        null,
                        source.phone(),
                        true,
                        getExpectedRating(source.rating()),
                        null,
                        "LESS_THEN_FIVE",
                        null,
                        Collections.emptySet()
                );
        var expectedId = getExpectedId(source.id());
        if (expectedId == null) {
            assertThat(actual.getId()).isNull();
        } else {
            assertThat(actual.getId()).isEqualTo(expectedId);
        }
    }

    private static UUID getExpectedId(String source) {
        try {
            return UUID.fromString(source);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Integer getExpectedRating(String source) {
        try {
            return Double.valueOf(Double.parseDouble(source) * 100).intValue();
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Stream<Arguments> driverSource() {
        return Stream.of(
                Arguments.of(Instancio.create(UUID.class).toString(), Instancio.create(Double.class).toString()),
                Arguments.of(Instancio.create(UUID.class).toString(), Instancio.create(String.class)),
                Arguments.of(Instancio.create(String.class), Instancio.create(Double.class).toString()),
                Arguments.of(Instancio.create(Long.class).toString(), Instancio.create(Double.class).toString())
        );
    }
}
