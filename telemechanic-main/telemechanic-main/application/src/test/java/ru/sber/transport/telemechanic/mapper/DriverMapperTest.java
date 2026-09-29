package ru.sber.transport.telemechanic.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import ru.sber.transport.telemechanic.database.projection.DriverByFioProjection;
import ru.sber.transport.telemechanic.dto.driver.DriverByFioResponse;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DriverMapperTest {
    
    private final DriverMapper driverMapper = new DriverMapperImpl();
    
    
    @Test
    void driverByFioToDriverByFioResponse() {
        var expected = Instancio.create(DriverByFioResponse.class);
        var projection = createDriverByFioProjection(expected);
        var actual = driverMapper.driverByFioToDriverByFioResponse(projection);
        assertThat(actual)
                .usingRecursiveAssertion()
                .isEqualTo(expected);
    }
    
    DriverByFioProjection createDriverByFioProjection(DriverByFioResponse driverByFioResponse) {
        var factory = new SpelAwareProxyProjectionFactory();
        var projection = factory.createProjection(DriverByFioProjection.class);
        projection.setId(driverByFioResponse.driver().id());
        projection.setPersonnelNumber(driverByFioResponse.driver().personnelNumber());
        projection.setFullName(driverByFioResponse.driver().fullName());
        projection.setOrganizationName(driverByFioResponse.driver().organizationName());
        projection.setDepartmentId(driverByFioResponse.driver().departmentId());
        projection.setDepartmentName(driverByFioResponse.driver().departmentName());
        projection.setTin(driverByFioResponse.driver().tin());
        projection.setDrivingLicenceId(driverByFioResponse.drivingLicense().id());
        projection.setSeries(driverByFioResponse.drivingLicense().series());
        projection.setNumber(driverByFioResponse.drivingLicense().number());
        projection.setIssueDate(driverByFioResponse.drivingLicense().issueDate());
        return projection;
    }
}
