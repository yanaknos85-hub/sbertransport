package ru.sber.transport.notifications.services.impl.commands.personal;

import ru.sber.transport.notifications.database.model.request.TripRequest;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.services.DepartmentService;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.notifications.services.NotificationSettingsService;
import ru.sber.transport.notifications.services.impl.NotificationGlobalProcessor;
import ru.sber.transport.notifications.services.impl.commands.common.TripRequestAbstractCommand;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;


public abstract class PersonalAbstractCommand extends TripRequestAbstractCommand {

    protected PersonalAbstractCommand(DepartmentService departmentService, NotificationSettingsService notificationSettingsService, NotificationGlobalProcessor<TripRequest> processor, EmployeeService employeeService) {
        super(departmentService, notificationSettingsService, processor, employeeService);
    }

    @Override
    protected  TransportTypeEnum getExpectedTransportType() {
        return TransportTypeEnum.PERSONAL;
    }

    @Override
    protected NotificationClass getNotificationClass() {
        return NotificationClass.REQUEST_PERSONAL;
    }

    @Override
    protected String getCommandName(){return this.getClass().getSimpleName();}
}
