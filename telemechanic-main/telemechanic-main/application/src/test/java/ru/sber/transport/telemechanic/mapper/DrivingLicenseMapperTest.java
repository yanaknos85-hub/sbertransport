package ru.sber.transport.telemechanic.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import ru.sber.transport.telemechanic.database.model.Category;
import ru.sber.transport.telemechanic.database.model.DrivingLicense;
import ru.sber.transport.telemechanic.dto.driver.DrivingLicenseInfo;

import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class DrivingLicenseMapperTest {
    
    private final DrivingLicenseMapper mapper = new DrivingLicenseMapperImpl();
    
    @Test
    void drivingLicenseInfoToDrivingLicense() {
        var drivingLicenseInfo = Instancio.of(DrivingLicenseInfo.class).create();
        var mapped = mapper.drivingLicenseInfoToDrivingLicense(drivingLicenseInfo);
        assertThat(mapped).isNotNull();
        assertThat(mapped)
                .extracting(
                        DrivingLicense::getSeries,
                        DrivingLicense::getNumber,
                        DrivingLicense::getIssueDate,
                        DrivingLicense::getExpiryDate,
                        el -> el.getCategories().stream()
                                .map(Category::getId)
                                .collect(Collectors.toSet())
                           )
                .containsExactly(
                        drivingLicenseInfo.series(),
                        drivingLicenseInfo.number(),
                        drivingLicenseInfo.issueDate(),
                        drivingLicenseInfo.expiryDate(),
                        drivingLicenseInfo.categoryIds()
                                );
    }
}
