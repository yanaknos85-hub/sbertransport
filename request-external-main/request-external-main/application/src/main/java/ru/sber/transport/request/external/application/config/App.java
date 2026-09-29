package ru.sber.transport.request.external.application.config;

import jakarta.annotation.PostConstruct;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.jooq.DSLContext;
import org.jooq.ExecuteListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.sber.transport.authorization.service.EmployeeOrganizationFunction;
import ru.sber.transport.authorization.utils.ControllerUtils;
import ru.sber.transport.business.providers.AssessmnentProvider;
import ru.sber.transport.business.providers.AvailableClasses;
import ru.sber.transport.business.providers.DelegatesProvider;
import ru.sber.transport.business.providers.DepartmentsProvider;
import ru.sber.transport.business.providers.EmployeesProvider;
import ru.sber.transport.business.providers.FilesProvider;
import ru.sber.transport.business.providers.FraudsProvider;
import ru.sber.transport.business.providers.GeoZonesProvider;
import ru.sber.transport.business.providers.LimitsProvider;
import ru.sber.transport.business.providers.DurationRequestCheckProvider;
import ru.sber.transport.business.providers.OverrunCheckProvider;
import ru.sber.transport.business.providers.PricesProvider;
import ru.sber.transport.business.providers.TripOrderHistoriesProvider;
import ru.sber.transport.business.providers.TripOrdersProvider;
import ru.sber.transport.file_works.exporter.DataExporter;
import ru.sber.transport.files.grpc.annotation.FileExchange;
import ru.sber.transport.files.grpc.exchange.Deleter;
import ru.sber.transport.files.grpc.exchange.Downloader;
import ru.sber.transport.files.grpc.exchange.Uploader;
import ru.sber.transport.request.external.business.TripOrdersService;
import ru.sber.transport.request.external.business.impl.TripOrdersServiceImpl;
import ru.sber.transport.request.external.messaging.mapper.ReceiptMapper;
import ru.sber.transport.request.external.messaging.mapper.RequestMapper;
import ru.sber.transport.request.external.messaging.senders.*;
import ru.sber.transport.request.external.providers.employees.EmployeeListener;
import ru.sber.transport.request.external.providers.grpc.FilesGrpcProviderImpl;
import ru.sber.transport.request.external.providers.order.TripOrderListener;
import ru.sber.transport.request.external.resolver.RequestsExporter;
import ru.sber.transport.request.external.resolver.RequestsExporterPayment;
import ru.sber.transport.request.external.resolver.RequestsExporterPaymentAggregation;
import ru.sber.transport.request.external.resolver.model.TripOrderRegistry;
import ru.sber.transport.request.external.resolver.model.TripOrderRegistryPayment;
import ru.sber.transport.request.external.resolver.model.TripOrderRegistryPaymentAggregation;

/**
 * Конфигурация веб-приложения
 */
@Slf4j
@Configuration
@FileExchange
@EnableSchedulerLock(defaultLockAtMostFor = "PT30S")
public class App {
    /**
     * Инициализация конфигурации веб-приложения
     */
    @PostConstruct
    public void init() {
        log.info("Configuring web application");
    }

    /**
     * Получение организации сотрудника
     *
     * @param employees провайдер сотрудников
     * @return Функция получения организации сотрудника
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public EmployeeOrganizationFunction employeeOrganizationFunction(ObjectProvider<EmployeesProvider> employees) {
        log.info("Creating employee organization function");
        return userId -> employees.getObject().get(userId).getOrganizationId();
    }

    /**
     * Экспортер данных заявок на транспорт
     *
     * @param orders             заявки на транспорт
     * @param tripOrderHistoriesProvider история заявок на транспорт
     * @param employeeFunction   функция получения организации сотрудника
     * @return Экспортер данных заявок на транспорт
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public DataExporter<TripOrderRegistry> tripOrderDataExporter(TripOrdersProvider orders, TripOrderHistoriesProvider tripOrderHistoriesProvider, EmployeeOrganizationFunction employeeFunction) {
        log.info("Creating trip order data exporter");
        return new RequestsExporter(orders, tripOrderHistoriesProvider, employeeFunction);
    }

    /**
     * Экспортер данных заявок на транспорт
     *
     * @param orders             заявки на транспорт
     * @param tripOrderHistoriesProvider история заявок на транспорт
     * @param employeeFunction   функция получения организации сотрудника
     * @return Экспортер данных заявок на транспорт
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public DataExporter<TripOrderRegistryPayment> tripOrderRegistryPaymentDataExporter(TripOrdersProvider orders, TripOrderHistoriesProvider tripOrderHistoriesProvider, EmployeeOrganizationFunction employeeFunction) {
        log.info("Creating trip order registry payment data exporter");
        return new RequestsExporterPayment(orders, tripOrderHistoriesProvider, employeeFunction);
    }

    /**
     * Экспортер данных заявок на транспорт
     *
     * @param orders             заявки на транспорт
     * @param tripOrderHistoriesProvider история заявок на транспорт
     * @param employeeFunction   функция получения организации сотрудника
     * @return Экспортер данных заявок на транспорт
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public DataExporter<TripOrderRegistryPaymentAggregation> tripOrderRegistryPaymentAggregationDataExporter(
        TripOrdersProvider orders, TripOrderHistoriesProvider tripOrderHistoriesProvider, EmployeeOrganizationFunction employeeFunction) {
        log.info("Creating trip order registry payment aggregation data exporter");
        return new RequestsExporterPaymentAggregation(orders, tripOrderHistoriesProvider, employeeFunction);
    }

    /**
     * Слушатель изменений сотрудников
     *
     * @return Слушатель изменений сотрудников
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public ExecuteListener employeesListener(ObjectProvider<DSLContext> context) {
        log.info("Creating employees listener");
        return new EmployeeListener(context);
    }

    /**
     * Слушатель изменений заявок на поездки
     *
     * @return Слушатель изменений заявок на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public ExecuteListener tripOrdersListener(TripOrderHistoriesProvider tripOrderHistoriesProvider, ObjectProvider<DSLContext> context) {
        log.info("Creating trip orders listener");
        return new TripOrderListener(tripOrderHistoriesProvider, ControllerUtils::currentUser, context);
    }

    /**
     * Фоновый загрузчик бинов
     *
     * @return Фоновый загрузчик бинов
     */
    @Bean
    public Executor bootstrapExecutor() {
        log.info("Creating bootstrap executor");
        return Executors.newCachedThreadPool();
    }

    /**
     * Бин времени
     *
     * @return Время
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public Clock clock() {
        final var clock = Clock.systemDefaultZone();
        log.info("Time zone configured to {}", clock.getZone());
        return clock;
    }

    /**
     * Провайдер файлов
     *
     * @param tripOrdersProvider поставщик заявок на поездки
     * @param uploader   загрузчик файлов
     * @param downloader выгрузчик
     * @param deleter    удалитель файлов
     * @return Провайдер файлов
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public FilesProvider filesProvider(TripOrdersProvider tripOrdersProvider, Uploader uploader, Downloader downloader, Deleter deleter) {
        log.info("Creating files provider");
        return new FilesGrpcProviderImpl(tripOrdersProvider, uploader, downloader, deleter);
    }

    /**
     * Бизнес-логика заявок на поездки
     *
     * @param employeesProvider             провайдер сотрудников
     * @param tripOrdersProvider            поставщик заявок на поездки
     * @param pricesProvider                поставщик тарифов
     * @param sender                отправитель сообщений с заявками на поездки
     * @param notificationSender    отправитель уведомлений
     * @param clock                 бин времени
     * @param filesProvider                 провайдер файлов
     * @param availableClasses      доступные классы
     * @param limits                лимиты
     * @param tripOrderHistoriesProvider    исторические данные заявок на поездки
     * @param requestPayoutSender   отправитель заявок на выплату
     * @param requestMapper         маппер заявок
     * @return Бизнес-логика заявок на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public TripOrdersService tripOrdersBusiness(
            EmployeesProvider employeesProvider,
            TripOrdersProvider tripOrdersProvider,
            AssessmnentProvider assessmnentProvider,
            PricesProvider pricesProvider,
            RequestSender sender,
            List<NotificationSender> notificationSender,
            Clock clock,
            FilesProvider filesProvider,
            AvailableClasses availableClasses,
            ObjectProvider<LimitsProvider> limits,
            DepartmentsProvider departmentsProvider,
            DelegatesProvider delegateProvider,
            GeoZonesProvider geoZonesProvider,
            TripOrderHistoriesProvider tripOrderHistoriesProvider,
            RequestPayoutSender requestPayoutSender,
            RequestMapper requestMapper,
            DurationRequestCheckProvider durationRequestCheckProvider,
            FraudMonitoringSender fraudMonitoringSender,
            OverrunCheckProvider overrunCheckProvider,
            FraudsProvider fraudsProvider,
            @Value("${fraud.single-trip-duration.threshold-millis:28800000}") long singleTripDurationThresholdMillis,
            ReceiptMapper receiptMapper,
            ReceiptSender receiptScannerSender
    ) {
        log.info("Creating trip orders business");
        if (limits.getIfAvailable() == null) {
            log.warn("!!! Limits provider is disabled. Limit checks will be skipped !!!");
        }
        return new TripOrdersServiceImpl(employeesProvider, tripOrdersProvider, assessmnentProvider, pricesProvider, sender, notificationSender, clock,
            filesProvider, availableClasses, limits,
            departmentsProvider, delegateProvider, geoZonesProvider, tripOrderHistoriesProvider, requestPayoutSender,
                requestMapper, durationRequestCheckProvider, fraudMonitoringSender, overrunCheckProvider, fraudsProvider,
                singleTripDurationThresholdMillis, receiptMapper, receiptScannerSender
        );
    }

    /**
     * Провайдер блокировок
     *
     * @param dataSource связь с БД
     * @return Провайдер блокировок
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public LockProvider lockProvider(@Value("${spring.application.name}") String appName, DataSource dataSource) {
        log.info("Creating lock provider");
        return new JdbcTemplateLockProvider(JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .withTableName("\"%s\".\"shedlock\"".formatted(appName))
                .build());
    }

}
