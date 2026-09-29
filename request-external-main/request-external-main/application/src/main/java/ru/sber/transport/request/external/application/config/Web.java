package ru.sber.transport.request.external.application.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.business.providers.AvailableClasses;
import ru.sber.transport.business.providers.CurrentUser;
import ru.sber.transport.business.providers.DepartmentsProvider;
import ru.sber.transport.business.providers.TripOrderHistoriesProvider;
import ru.sber.transport.business.providers.TripOrdersMetaProvider;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.request.external.business.TripOrdersService;
import ru.sber.transport.request.external.mapper.DepartmentsMapper;
import ru.sber.transport.request.external.provider.web.CurrentUserImpl;
import ru.sber.transport.request.external.web.AllowedDelegateImpl;
import ru.sber.transport.request.external.web.RequestFileDelegateImpl;
import ru.sber.transport.request.external.web.command.RequestCommandDelegateImpl;
import ru.sber.transport.request.external.web.query.DepartmentsQueryDelegateImpl;
import ru.sber.transport.request.external.web.query.RequestQueryDelegateImpl;
import ru.sber.transport.web.api.ExternalDepartmentsQueryApi;
import ru.sber.transport.web.api.ExternalRequestCommandApi;
import ru.sber.transport.web.api.ExternalRequestFilesApi;
import ru.sber.transport.web.api.ExternalRequestQueryApi;
import ru.sber.transport.web.api.TemporaryApi;

/**
 * Веб-конфигурация приложения
 */
@Slf4j
@Configuration
public class Web {

    /**
     * Делегат заявок на поездки
     *
     * @param tripOrdersProvider     бизнес-логика заявок на поездки
     * @param tripOrdersMetaProvider поставщик метаданных заявок на поездки
     * @return делегат заявок на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public ExternalRequestQueryApi queryApi(EmployeeOrganizationFunction employeeFunction,
                                            TripOrdersProvider tripOrdersProvider,
                                            TripOrderHistoriesProvider tripOrderHistoriesProvider,
                                            TripOrdersMetaProvider tripOrdersMetaProvider,
                                            DepartmentsProvider departmentsProvider) {
        log.info("Creating request query");
        return new RequestQueryDelegateImpl(departmentsProvider, tripOrdersProvider, tripOrderHistoriesProvider, tripOrdersMetaProvider, employeeFunction);
    }

    /**
     * Делегат подразделений
     *
     * @return делегат подразделений
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public ExternalDepartmentsQueryApi departmentsQueryApi(
            DepartmentsMapper departmentsMapper,
            DepartmentsProvider departmentsProvider) {
        log.info("Creating departments request query");
        return new DepartmentsQueryDelegateImpl(departmentsProvider, departmentsMapper);
    }

    /**
     * Делегат заявок на поездки
     *
     * @param employeeFunctionProvider провайдер функции получения организации сотрудника
     * @param tripOrdersServiceProvider       провайдер бизнес-логики заявок на поездки
     * @param tripOrdersMetaProvider   провайдер поставщика метаданных заявок на поездки
     * @param mapper                   маппер
     * @return делегат заявок на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public ExternalRequestCommandApi commandApi(EmployeeOrganizationFunction employeeFunctionProvider, TripOrdersService tripOrdersServiceProvider, TripOrdersMetaProvider tripOrdersMetaProvider, ObjectMapper mapper) {
        log.info("Creating request command");
        return new RequestCommandDelegateImpl(tripOrdersServiceProvider, tripOrdersMetaProvider, employeeFunctionProvider, mapper);
    }

    /**
     * Делегат файлов заявок на поездки
     *
     * @param tripOrdersService бизнес-логика заявок на поездки
     * @return бизнес-логика заявок на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public ExternalRequestFilesApi externalRequestFilesApi(EmployeeOrganizationFunction employeeFunction, TripOrdersService tripOrdersService) {
        log.info("Creating request files");
        return new RequestFileDelegateImpl(tripOrdersService, employeeFunction);
    }

    /**
     * Делегат для доступа к временным ресурсам
     *
     * @param classes провайдер классов
     * @return делегат для доступа к временным ресурсам
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public TemporaryApi temporaryApiDelegate(AvailableClasses classes) {
        log.info("Creating temporary api");
        return new AllowedDelegateImpl(classes);
    }

    /**
     * Провайдер текущего пользователя
     *
     * @return провайдер текущего пользователя
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public CurrentUser currentUser() {
        return new CurrentUserImpl();
    }

}
