package ru.sber.transport.request.external.application.config;

import static ru.sber.transport.request.external.application.config.Grpc.GRPC_DEPARTMENTS;
import static ru.sber.transport.request.external.application.config.Grpc.GRPC_EMPLOYEES;
import static ru.sber.transport.request.external.application.config.Grpc.GRPC_ORGANIZATIONS;

import jakarta.annotation.PostConstruct;
import java.time.Clock;
import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import ru.sber.transport.business.providers.AssessmnentProvider;
import ru.sber.transport.business.providers.AvailableClasses;
import ru.sber.transport.business.providers.DelegatesProvider;
import ru.sber.transport.business.providers.DepartmentsProvider;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.business.providers.FraudsProvider;
import ru.sber.transport.business.providers.OrganizationsProvider;
import ru.sber.transport.business.providers.PositionsProvider;
import ru.sber.transport.business.providers.TripOrderHistoriesProvider;
import ru.sber.transport.business.providers.TripOrdersMetaProvider;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.request.external.providers.available_classes.AvailableClassesImpl;
import ru.sber.transport.request.external.providers.delegates.DelegatesDataProviderImpl;
import ru.sber.transport.request.external.providers.department.DepartmentsDataProviderImpl;
import ru.sber.transport.request.external.providers.employees.EmployeesDataProviderImpl;
import ru.sber.transport.request.external.providers.fraud.FraudsDataProviderImpl;
import ru.sber.transport.request.external.providers.order.AssessmentDataProviderImpl;
import ru.sber.transport.request.external.providers.order.TripOrdersMetaDataProviderImpl;
import ru.sber.transport.request.external.providers.order.history.TripOrderHistoriesDataProviderImpl;
import ru.sber.transport.request.external.providers.organization.OrganizationsDataProviderImpl;
import ru.sber.transport.request.external.providers.position.PositionsDataProviderImpl;

/**
 * Конфигурация приложения для работы с базой данных
 */
@Slf4j
@Configuration
public class Database {

    /**
     * Инициализация конфигурации базы данных
     */
    @PostConstruct
    public void init() {
        log.info("Configuring database");
    }

    /**
     * Провайдер оценок
     *
     * @param context контекст базы
     * @return Провайдер оценок
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public AssessmnentProvider assessmnentProvider(ObjectProvider<DSLContext> context) {
        log.info("Creating trip assessment provider");
        return new AssessmentDataProviderImpl() {
            @Override
            public DSLContext context() {
                return context.getObject();
            }
        };
    }

    /**
     * Провайдер организаций
     *
     * @param grpcOrganizationsProvider gRPC-провайдер организаций
     * @return Провайдер организаций
     */
    @Primary
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    @DependsOn(GRPC_ORGANIZATIONS)
    public OrganizationsProvider organizationsProvider(@Qualifier(GRPC_ORGANIZATIONS) OrganizationsProvider grpcOrganizationsProvider, DSLContext context) {
        log.info("Creating organizations provider");
        return new OrganizationsDataProviderImpl(grpcOrganizationsProvider) {
            @Override
            public DSLContext context() {
                return context;
            }
        };
    }

    /**
     * Провайдер заявок на поездки
     *
     * @param context контекст базы
     * @return Провайдер заявок на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public TripOrderHistoriesProvider tripOrderHistoriesProvider(ObjectProvider<DSLContext> context) {
        log.info("Creating trip orders histories provider");
        return new TripOrderHistoriesDataProviderImpl() {
            @Override
            public DSLContext context() {
                return context.getObject();
            }
        };
    }

    /**
     * Провайдер заявок на поездки
     * <p>
     * }
     * <p>
     * /**
     * Провайдер департаментов
     *
     * @param grpcDepartmentsProvider   gRPC-провайдер департаментов
     * @param employeesProvider провайдер сотрудников
     * @return Провайдер департаментов
     */
    @Primary
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public DepartmentsProvider departmentsProvider(@Qualifier(GRPC_DEPARTMENTS) DepartmentsProvider grpcDepartmentsProvider,
                                           EmployeesProvider employeesProvider,
                                           DSLContext context,
                                           Executor departmentConsistencyCheckTaskExecutor) {
        log.info("Creating departments provider");
        return new DepartmentsDataProviderImpl(grpcDepartmentsProvider, employeesProvider, departmentConsistencyCheckTaskExecutor) {
            @Override
            public DSLContext context() {
                return context;
            }
        };
    }

    /**
     * Провайдер сотрудников
     *
     * @param departmentsProvider провайдер департаментов
     * @param grpcEmployeesProvider       gRPC-провайдер сотрудников
     * @return Провайдер сотрудников
     */
    @Primary
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public EmployeesProvider employeesProvider(OrganizationsProvider organizationsProvider, ObjectProvider<DepartmentsProvider> departmentsProvider, @Qualifier(GRPC_EMPLOYEES) EmployeesProvider grpcEmployeesProvider, DSLContext context) {
        log.info("Creating employees provider");
        return new EmployeesDataProviderImpl(organizationsProvider, departmentsProvider, grpcEmployeesProvider) {
            @Override
            public DSLContext context() {
                return context;
            }
        };
    }

    /**
     * Провайдер заявок на поездки
     *
     * @param organizationsProvider провайдер организаций
     * @param employeesProvider     провайдер сотрудников
     * @return Провайдер заявок на поездки
     */
    @Primary
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public TripOrdersProvider tripOrdersProvider(OrganizationsProvider organizationsProvider, EmployeesProvider employeesProvider, DSLContext context) {
        log.info("Creating trip orders provider");
        return new TripOrdersMetaDataProviderImpl(organizationsProvider, employeesProvider) {
            @Override
            public DSLContext context() {
                return context;
            }
        };
    }

    /**
     * Провайдер метаданных заявок на поездки
     *
     * @param organizationsProvider поставщик организаций
     * @param employeesProvider     поставщик сотрудников
     * @return Провайдер метаданных заявок на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public TripOrdersMetaProvider tripOrdersMetaProvider(OrganizationsProvider organizationsProvider, EmployeesProvider employeesProvider, DSLContext context) {
        log.info("Creating trip orders meta");
        return new TripOrdersMetaDataProviderImpl(organizationsProvider, employeesProvider) {
            @Override
            public DSLContext context() {
                return context;
            }
        };
    }

    /**
     * Провайдер должностей
     *
     * @param context контекст работы с базой данных
     * @return Провайдер должностей
     */
    @Primary
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public PositionsProvider positionsProvider(DSLContext context) {
        log.info("Creating positions provider");
        return new PositionsDataProviderImpl() {
            @Override
            public DSLContext context() {
                return context;
            }

        };
    }

    /**
     * Провайдер данных о фроде
     *
     * @return Провайдер данных о фроде
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public FraudsProvider fraudsProvider(DSLContext context) {
        log.info("Creating frauds provider");
        return new FraudsDataProviderImpl() {
            @Override
            public DSLContext context() {
                return context;
            }
        };
    }

    /**
     * Провайдер доступных классов транспорта
     *
     * @param context контекст работы с базой данных
     * @return Провайдер доступных классов транспорта
     */
    @Primary
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public AvailableClasses availableClassesProvider(EmployeesProvider employeesProvider, DSLContext context, @Value("${mvp:false}") boolean mvp, @Value("${allow.enabled:false}") boolean allowEnabled, Clock clock) {
        log.info("Creating available classes provider");
        if (mvp) {
            log.info("!!! MVP mode enabled !!!");
        }
        if (!allowEnabled) {
            log.info("Method GET /allow will always return TRUE");
        }
        return new AvailableClassesImpl(employeesProvider, mvp, allowEnabled, clock) {
            @Override
            public DSLContext context() {
                return context;
            }
        };
    }

    /**
     * Провайдер делегатов
     *
     * @param employeesProvider поставщик сотрудников
     * @param context           контекст работы с базой данных
     * @return Провайдер делегатов
     */
    @Primary
    @Bean
    public DelegatesProvider delegatesProvider(EmployeesProvider employeesProvider, DSLContext context, DelegatesProvider grpcDelegatesProvider) {
        log.info("Creating delegates provider");
        return new DelegatesDataProviderImpl(employeesProvider, grpcDelegatesProvider) {
            @Override
            public DSLContext context() {
                return context;
            }

        };
    }

}
