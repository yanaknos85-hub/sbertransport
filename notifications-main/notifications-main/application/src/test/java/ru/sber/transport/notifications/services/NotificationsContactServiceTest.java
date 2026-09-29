package ru.sber.transport.notifications.services;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.database.model.contractor.Dispatcher;
import ru.sber.transport.notifications.database.model.contractor.Driver;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.postgres.EmbeddedPostgres;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;

@EmbeddedPostgres
@DisplayName("Поиск контактов")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
@SpringBootTest
class NotificationsContactServiceTest {

    @Autowired
    private NotificationContactService notificationsContactService;

    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private DriverService driverService;

    @Autowired
    private DispatcherService dispatcherService;

    @DisplayName("Получение контактных данных из view")
    @Test
    void testFindContactView() {
        // given
        var dispatcher = dispatcherService.save(Instancio.create(Dispatcher.class));
        var employee = Instancio.create(Employee.class);
        employeeService.save(employee);
        var employeeId = employee.getId();
        var driver = driverService.save(Instancio.of(Driver.class).set(field(Driver::getRating), 0).create());

        // when
        var dispatcherFromView = notificationsContactService.get(dispatcher.getId());
        var employeeFromView = notificationsContactService.get(employeeId);
        var driverFromView = notificationsContactService.get(driver.getId());

        // then
        assertThat(dispatcherFromView)
                .isPresent();

        assertThat(employeeFromView)
                .isPresent();

        assertThat(driverFromView)
                .isPresent();
    }

}