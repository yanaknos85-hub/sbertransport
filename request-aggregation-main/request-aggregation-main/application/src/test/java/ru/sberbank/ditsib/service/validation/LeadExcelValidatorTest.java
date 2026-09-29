package ru.sberbank.ditsib.service.validation;

import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.database.dao.EmployeeRepository;
import ru.sberbank.ditsib.database.model.Employee;
import ru.sberbank.ditsib.dto.GeoAddress;
import ru.sberbank.ditsib.dto.lead.LeadExcelDto;
import ru.sberbank.ditsib.service.GeoService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;


@ExtendWith(MockitoExtension.class)
@DisplayName("Валидация файла с пользовательскими лидами")
class LeadExcelValidatorTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private GeoService geoService;
    @InjectMocks
    private LeadExcelValidator validator;

    @Test
    void validateEmptyFields() {
        var emptyExcelDto = new LeadExcelDto();

        var errors = validator.validate(List.of(emptyExcelDto), 3).content();
        assertThat(errors).hasSize(1);

        var error = errors.get(0);
        var message = "Поле не заполнено";

        assertThat(error.getPersonnelNumber().isError()).isTrue();
        assertThat(error.getPersonnelNumber().getErrorMessage()).isEqualTo(message);
        assertThat(error.getFullName().isError()).isTrue();
        assertThat(error.getFullName().getErrorMessage()).isEqualTo(message);
        assertThat(error.getTripType().isError()).isTrue();
        assertThat(error.getTripType().getErrorMessage()).isEqualTo(message);
        assertThat(error.getTransportType().isError()).isTrue();
        assertThat(error.getTransportType().getErrorMessage()).isEqualTo(message);
        assertThat(error.getTransportClass().isError()).isTrue();
        assertThat(error.getTransportClass().getErrorMessage()).isEqualTo(message);
        assertThat(error.getAddressFrom().isError()).isTrue();
        assertThat(error.getAddressTo().isError()).isTrue();
        assertThat(error.getAddressFrom().getErrorMessage()).isEqualTo(message);
        assertThat(error.getAddressTo().getErrorMessage()).isEqualTo(message);
        assertThat(error.getAddressFromLatitude()).isNull();
        assertThat(error.getAddressFromLongitude()).isNull();
        assertThat(error.getAddressToLongitude()).isNull();
        assertThat(error.getAddressToLatitude()).isNull();
    }

    @Test
    void validateTransportType() {
        var excelDto = new LeadExcelDto();
        excelDto.setPersonnelNumber("123456");
        excelDto.setFullName("Иванов Иван Иванович");
        excelDto.setTransportType("Легковой");
        var employee = Instancio.of(Employee.class)
                .set(field(Employee::getFirstName), "Иван")
                .set(field(Employee::getLastName), "Иванов")
                .set(field(Employee::getPatronymic), "Иванович")
                .set(field(Employee::getPersonnelNumber), excelDto.getPersonnelNumber())
                .create();

        doReturn(Optional.of(employee))
                .when(employeeRepository).findByPersonnelNumber(excelDto.getPersonnelNumber());

        var errors = validator.validate(List.of(excelDto), 3).content();
        assertThat(errors).hasSize(1);
        var error = errors.get(0);

        assertThat(error.getTransportType().isError()).isTrue();
        assertThat(error.getTransportType().getErrorMessage()).isEqualTo("Введенный вид транспорта не существует");
    }

    @Test
    void validateTransportClass() {
        var excelDto = new LeadExcelDto();
        excelDto.setPersonnelNumber("123456");
        excelDto.setFullName("Иванов Иван Иванович");
        excelDto.setTransportType("TAXI");
        excelDto.setTransportClass("Легковой");
        var employee = Instancio.of(Employee.class)
                .set(field(Employee::getFirstName), "Иван")
                .set(field(Employee::getLastName), "Иванов")
                .set(field(Employee::getPatronymic), "Иванович")
                .set(field(Employee::getPersonnelNumber), excelDto.getPersonnelNumber())
                .create();

        doReturn(Optional.of(employee))
                .when(employeeRepository).findByPersonnelNumber(excelDto.getPersonnelNumber());

        var errors = validator.validate(List.of(excelDto), 3).content();
        assertThat(errors).hasSize(1);
        var error = errors.get(0);

        assertThat(error.getTransportClass().isError()).isTrue();
        assertThat(error.getTransportClass().getErrorMessage()).isEqualTo("Введенный тип транспорта не существует");
    }

    @Test
    void validateTripType() {
        var excelDto = new LeadExcelDto();
        excelDto.setPersonnelNumber("123456");
        excelDto.setFullName("Иванов Иван Иванович");
        excelDto.setTransportType("TAXI");
        excelDto.setTransportClass("ECONOMY");
        excelDto.setTripType("Легковой");
        var employee = Instancio.of(Employee.class)
                .set(field(Employee::getFirstName), "Иван")
                .set(field(Employee::getLastName), "Иванов")
                .set(field(Employee::getPatronymic), "Иванович")
                .set(field(Employee::getPersonnelNumber), excelDto.getPersonnelNumber())
                .create();

        doReturn(Optional.of(employee))
                .when(employeeRepository).findByPersonnelNumber(excelDto.getPersonnelNumber());

        var errors = validator.validate(List.of(excelDto), 3).content();
        assertThat(errors).hasSize(1);
        var error = errors.get(0);

        assertThat(error.getTripType().isError()).isTrue();
        assertThat(error.getTripType().getErrorMessage()).isEqualTo("Введенная цель поездки не существует");
    }

    @Test
    void validateEmployeeNotFound() {
        var excelDto = new LeadExcelDto();
        excelDto.setTransportType("TAXI");
        excelDto.setTransportClass("ECONOMY");
        excelDto.setTripType("DAYTIME_TRIP");
        excelDto.setPersonnelNumber("123456");
        excelDto.setFullName("Иванов Иван Иванович");

        doReturn(Optional.empty())
                .when(employeeRepository).findByPersonnelNumber(excelDto.getPersonnelNumber());

        var errors = validator.validate(List.of(excelDto), 3).content();
        assertThat(errors).hasSize(1);
        var error = errors.get(0);

        assertThat(error.getPersonnelNumber().isError()).isTrue();
        assertThat(error.getPersonnelNumber().getErrorMessage()).isEqualTo("Табельный номер не существует");
    }

    @Test
    void validateFullNameMismatch() {
        var excelDto = new LeadExcelDto();
        excelDto.setTransportType("TAXI");
        excelDto.setTransportClass("ECONOMY");
        excelDto.setTripType("DAYTIME_TRIP");
        excelDto.setPersonnelNumber("123456");
        excelDto.setFullName("Иванов Иван Иванович");
        var employee = Instancio.of(Employee.class)
                .set(field(Employee::getFirstName), "ЛжеДмитрий")
                .set(field(Employee::getPersonnelNumber), excelDto.getPersonnelNumber())
                .create();

        doReturn(Optional.of(employee))
                .when(employeeRepository).findByPersonnelNumber(excelDto.getPersonnelNumber());

        var errors = validator.validate(List.of(excelDto), 3).content();
        assertThat(errors).hasSize(1);
        var error = errors.get(0);

        assertThat(error.getFullName().isError()).isTrue();
        assertThat(error.getFullName().getErrorMessage()).isEqualTo("ФИО не совпадает с табельным номером");
    }

    @Test
    void validateAddressTooLong() {
        var excelDto = new LeadExcelDto();
        excelDto.setTransportType("TAXI");
        excelDto.setTransportClass("ECONOMY");
        excelDto.setTripType("DAYTIME_TRIP");
        excelDto.setPersonnelNumber("123456");
        excelDto.setFullName("Иванов Иван Иванович");
        excelDto.setAddressFrom(Instancio.gen().string().length(1000).get());
        excelDto.setAddressTo(Instancio.gen().string().length(1000).get());
        var employee = Instancio.of(Employee.class)
                .set(field(Employee::getFirstName), "Иван")
                .set(field(Employee::getLastName), "Иванов")
                .set(field(Employee::getPatronymic), "Иванович")
                .set(field(Employee::getPersonnelNumber), excelDto.getPersonnelNumber())
                .create();

        doReturn(Optional.of(employee))
                .when(employeeRepository).findByPersonnelNumber(excelDto.getPersonnelNumber());

        var errors = validator.validate(List.of(excelDto), 3).content();
        assertThat(errors).hasSize(1);
        var error = errors.get(0);

        assertThat(error.getAddressFrom().isError()).isTrue();
        assertThat(error.getAddressFrom().getErrorMessage()).isEqualTo("Введенный адрес слишком длинный");
        assertThat(error.getAddressTo().isError()).isTrue();
        assertThat(error.getAddressTo().getErrorMessage()).isEqualTo("Введенный адрес слишком длинный");
    }

    @Test
    void validateAddressNotFound() {
        var excelDto = new LeadExcelDto();
        excelDto.setTransportType("TAXI");
        excelDto.setTransportClass("ECONOMY");
        excelDto.setTripType("DAYTIME_TRIP");
        excelDto.setPersonnelNumber("123456");
        excelDto.setFullName("Иванов Иван Иванович");
        excelDto.setAddressFrom(Instancio.gen().string().length(100).get());
        excelDto.setAddressTo(Instancio.gen().string().length(100).get());
        var employee = Instancio.of(Employee.class)
                .set(field(Employee::getFirstName), "Иван")
                .set(field(Employee::getLastName), "Иванов")
                .set(field(Employee::getPatronymic), "Иванович")
                .set(field(Employee::getPersonnelNumber), excelDto.getPersonnelNumber())
                .create();

        doReturn(Optional.of(employee))
                .when(employeeRepository).findByPersonnelNumber(excelDto.getPersonnelNumber());

        doReturn(List.of()).doReturn(List.of())
                .when(geoService).getGeoAddress(anyString());

        var errors = validator.validate(List.of(excelDto), 3).content();
        assertThat(errors).hasSize(1);
        var error = errors.get(0);

        assertThat(error.getAddressFrom().isError()).isTrue();
        assertThat(error.getAddressFrom().getErrorMessage()).isEqualTo("Данный адрес не существует");
        assertThat(error.getAddressTo().isError()).isTrue();
        assertThat(error.getAddressTo().getErrorMessage()).isEqualTo("Данный адрес не существует");
    }

    @Test
    void validateWhenDateTimeExpired() {
        var excelDto = new LeadExcelDto();
        var now = LocalDateTime.now();
        excelDto.setPersonnelNumber("123456");
        excelDto.setFullName("Иванов Иван Иванович");
        excelDto.setTransportType("TAXI");
        excelDto.setTransportClass("ECONOMY");
        excelDto.setTripType("DAYTIME_TRIP");
        excelDto.setAddressFrom("Москва");
        excelDto.setAddressTo("Санкт-Петербург");
        excelDto.setOrderDate(now.toLocalDate());
        excelDto.setOrderTime(now.toLocalTime());
        var employee = Instancio.of(Employee.class)
                .set(field(Employee::getFirstName), "Иван")
                .set(field(Employee::getLastName), "Иванов")
                .set(field(Employee::getPatronymic), "Иванович")
                .set(field(Employee::getPersonnelNumber), excelDto.getPersonnelNumber())
                .create();

        var addressFrom = Instancio.create(GeoAddress.class);
        var addressTo = Instancio.create(GeoAddress.class);

        doReturn(Optional.of(employee))
                .when(employeeRepository).findByPersonnelNumber(excelDto.getPersonnelNumber());
        doReturn(List.of(addressFrom)).doReturn(List.of(addressTo)).when(geoService)
                .getGeoAddress(anyString());

        var dtos = validator.validate(List.of(excelDto), 3).content();
        assertThat(dtos).hasSize(1);
        var dto = dtos.get(0);

        assertThat(dto.getOrderDate().isError()).isTrue();
        assertThat(dto.getOrderDate().getErrorMessage()).isEqualTo(
                "Дата и время заказа должны быть с учетом задержки в 3 часа от текущего времени");
        assertThat(dto.getOrderTime().isError()).isTrue();
        assertThat(dto.getOrderTime().getErrorMessage()).isEqualTo(
                "Дата и время заказа должны быть с учетом задержки в 3 часа от текущего времени");
    }

    @Test
    void validateWhenSuccess() {
        var excelDto = new LeadExcelDto();
        var now = LocalDateTime.now().plusHours(5);
        excelDto.setPersonnelNumber("123456");
        excelDto.setFullName("Иванов Иван Иванович");
        excelDto.setTransportType("TAXI");
        excelDto.setTransportClass("ECONOMY");
        excelDto.setTripType("DAYTIME_TRIP");
        excelDto.setAddressFrom("Москва");
        excelDto.setAddressTo("Санкт-Петербург");
        excelDto.setOrderDate(now.toLocalDate());
        excelDto.setOrderTime(now.toLocalTime());
        var employee = Instancio.of(Employee.class)
                .set(field(Employee::getFirstName), "Иван")
                .set(field(Employee::getLastName), "Иванов")
                .set(field(Employee::getPatronymic), "Иванович")
                .set(field(Employee::getPersonnelNumber), excelDto.getPersonnelNumber())
                .create();

        var addressFrom = Instancio.create(GeoAddress.class);
        var addressTo = Instancio.create(GeoAddress.class);

        doReturn(Optional.of(employee))
                .when(employeeRepository).findByPersonnelNumber(excelDto.getPersonnelNumber());
        doReturn(List.of(addressFrom)).doReturn(List.of(addressTo)).when(geoService)
                .getGeoAddress(anyString());

        var dtos = validator.validate(List.of(excelDto), 3).content();
        assertThat(dtos).hasSize(1);
        var dto = dtos.get(0);

        assertThat(dto.getPersonnelNumber().isError()).isFalse();
        assertThat(dto.getFullName().isError()).isFalse();
        assertThat(dto.getTransportType().isError()).isFalse();
        assertThat(dto.getTransportClass().isError()).isFalse();
        assertThat(dto.getTripType().isError()).isFalse();
        assertThat(dto.getAddressFrom().isError()).isFalse();
        assertThat(dto.getAddressTo().isError()).isFalse();
        assertThat(dto.getOrderDate().isError()).isFalse();
        assertThat(dto.getOrderTime().isError()).isFalse();
        assertThat(dto.getAddressFromLatitude()).isEqualTo(addressFrom.latitude().doubleValue());
        assertThat(dto.getAddressToLatitude()).isEqualTo(addressTo.latitude().doubleValue());
        assertThat(dto.getAddressFromLongitude()).isEqualTo(addressFrom.longitude().doubleValue());
        assertThat(dto.getAddressToLongitude()).isEqualTo(addressTo.longitude().doubleValue());
    }
}