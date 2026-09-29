package ru.sber.transport.request.external.application.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import ru.sber.transport.business.providers.*;
import ru.sber.transport.http.request.check.annotations.NoAuthorize;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.request.external.messaging.listeners.*;
import ru.sber.transport.request.external.messaging.listeners.avro.*;
import ru.sber.transport.request.external.messaging.message.UpdateTripRequestStatusMessage;
import ru.sber.transport.request.external.messaging.mapper.NotificationMapper;
import ru.sber.transport.request.external.messaging.mapper.RequestMapper;
import ru.sber.transport.request.external.messaging.message.FraudMessagePlain;
import ru.sber.transport.request.external.messaging.senders.*;
import ru.sber.transport.request.external.messaging.senders.impl.*;
import ru.sberbank.ditsib.transport.messaging.messages.*;

import java.util.function.Consumer;

/**
 * Конфигурация приложения для обмена сообщениями между микросервисами
 */
@Slf4j
@Configuration
@NoAuthorize("/ws/requests")
public class Messaging {

    /**
     * Слушатель организаций
     *
     * @param organizationsProvider поставщик организаций
     * @return Слушатель организаций
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "true")
    public Consumer<Message<ru.sber.transport.messages.corporate.avro.OrganizationMessage>> organizations(
            OrganizationsProvider organizationsProvider) {
        log.info("Creating organizations Avro listener");
        return new OrganizationAvroListener(organizationsProvider);
    }

    /**
     * Слушатель организаций
     *
     * @param organizationsProvider поставщик организаций
     * @return Слушатель организаций
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<OrganizationMessage>> organizationInput(OrganizationsProvider organizationsProvider) {
        log.info("Creating organizations listener");
        return new OrganizationListener(organizationsProvider);
    }

    /**
     * Слушатель организаций
     *
     * @param organizationsProvider поставщик организаций
     * @return Слушатель организаций
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<OrganizationMessage>> organizationInputSsl(OrganizationsProvider organizationsProvider) {
        log.info("Creating organizations SSL listener");
        return new OrganizationListener(organizationsProvider);
    }

    /**
     * Слушатель департаментов
     *
     * @param departmentsProvider поставщик департаментов
     * @return Слушатель департаментов
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "true")
    public Consumer<Message<ru.sber.transport.messages.corporate.avro.DepartmentMessage>> departments(
            DepartmentsProvider departmentsProvider) {
        log.info("Creating departments Avro listener");
        return new DepartmentAvroListener(departmentsProvider);
    }

    /**
     * Слушатель департаментов
     *
     * @param departmentsProvider поставщик департаментов
     * @return Слушатель департаментов
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<DepartmentMessage>> departmentInput(DepartmentsProvider departmentsProvider) {
        log.info("Creating departments listener");
        return new DepartmentListener(departmentsProvider);
    }

    /**
     * Слушатель департаментов
     *
     * @param departmentsProvider поставщик департаментов
     * @return Слушатель департаментов
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<DepartmentMessage>> departmentInputSsl(DepartmentsProvider departmentsProvider) {
        log.info("Creating departments Ssl listener");
        return new DepartmentListener(departmentsProvider);
    }

    /**
     * Слушатель сотрудников
     *
     * @param employeesProvider поставщик сотрудников
     * @return Слушатель сотрудников
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "true")
    public Consumer<Message<ru.sber.transport.messages.corporate.avro.EmployeeMessage>> employees(
            EmployeesProvider employeesProvider) {
        log.info("Creating employees Avro listener");
        return new EmployeeAvroListener(employeesProvider);
    }

    /**
     * Слушатель сотрудников
     *
     * @param employeesProvider поставщик сотрудников
     * @return Слушатель сотрудников
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<EmployeeMessage>> employeeInput(EmployeesProvider employeesProvider) {
        log.info("Creating employees listener");
        return new EmployeeListener(employeesProvider);
    }

    /**
     * Слушатель сотрудников
     *
     * @param employeesProvider поставщик сотрудников
     * @return Слушатель сотрудников
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<EmployeeMessage>> employeeInputSsl(EmployeesProvider employeesProvider) {
        log.info("Creating employees SSL listener");
        return new EmployeeListener(employeesProvider);
    }

    /**
     * Слушатель должностей
     *
     * @param positionsProvider поставщик должностей
     * @return слушатель должностей
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "true")
    public Consumer<Message<ru.sber.transport.messages.corporate.avro.PositionMessage>> positions(
            PositionsProvider positionsProvider) {
        log.info("Creating positions Avro listener");
        return new PositionAvroListener(positionsProvider);
    }

    /**
     * Слушатель должностей
     *
     * @param positionsProvider поставщик должностей
     * @return слушатель должностей
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<PositionMessage>> positionInput(PositionsProvider positionsProvider) {
        log.info("Creating positions listener");
        return new PositionListener(positionsProvider);
    }

    /**
     * Слушатель должностей
     *
     * @param positionsProvider поставщик должностей
     * @return слушатель должностей
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<PositionMessage>> positionInputSsl(PositionsProvider positionsProvider) {
        log.info("Creating positions SSL listener");
        return new PositionListener(positionsProvider);
    }

    /**
     * Слушатель делегатов
     *
     * @param delegatesProvider поставщик делегатов
     * @return Слушатель делегатов
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "true")
    public Consumer<Message<ru.sber.transport.messages.corporate.avro.DelegateData>> delegates(
            DelegatesProvider delegatesProvider) {
        log.info("Creating delegates Avro listener");
        return new DelegateAvroListener(delegatesProvider);
    }

    /**
     * Слушатель делегатов
     *
     * @param delegatesProvider поставщик делегатов
     * @return Слушатель делегатов
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<DelegateMessage>> delegateInput(DelegatesProvider delegatesProvider) {
        log.info("Creating delegates listener");
        return new DelegateListener(delegatesProvider);
    }

    /**
     * Слушатель делегатов
     *
     * @param delegatesProvider поставщик делегатов
     * @return Слушатель делегатов
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<DelegateMessage>> delegateInputSsl(DelegatesProvider delegatesProvider) {
        log.info("Creating delegates SSL listener");
        return new DelegateListener(delegatesProvider);
    }

    /**
     * Слушатель информации о фроде
     *
     * @return Слушатель информации о фроде
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "true")
    public Consumer<Message<ru.sber.transport.messages.ai_receipt_scanner.avro.FraudMessage>> frauds(
        TripOrdersProvider tripOrdersProvider, FraudsProvider fraudsProvider, FraudMonitoringSender fraudMonitoringSender) {
        log.info("Creating fraud Avro listener");
        return new FraudAvroListener(tripOrdersProvider, fraudsProvider);
    }

    /**
     * Слушатель информации о фроде
     *
     * @return Слушатель информации о фроде
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<FraudMessagePlain>> fraudInput(TripOrdersProvider tripOrdersProvider, FraudsProvider fraudsProvider, FraudMonitoringSender fraudMonitoringSender) {
        log.info("Creating fraud listener");
        return new FraudListener(tripOrdersProvider, fraudsProvider);
    }

    /**
     * Слушатель информации о фроде
     *
     * @return Слушатель информации о фроде
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    @ConditionalOnProperty(name = "avro.messaging.enabled", havingValue = "false")
    public Consumer<Message<FraudMessagePlain>> fraudInputSsl(TripOrdersProvider tripOrdersProvider, FraudsProvider fraudsProvider, FraudMonitoringSender fraudMonitoringSender) {
        log.info("Creating fraud SSL listener");
        return new FraudListener(tripOrdersProvider, fraudsProvider);
    }

    /**
     * Отправитель сообщений с заявками на поездки
     *
     * @param kafkaBridge    связующая между сообщениями и топиками kafka binder
     * @param kafkaSslBridge связующая между сообщениями и топиками kafka-ssl binder
     * @param avroBridge     связующая между сообщениями и топиками kafka-avro binder
     * @return Отправитель сообщений с заявками на поездки
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public RequestSender requestSender(@Qualifier("requestExternal") ObjectProvider<OutputBridge> kafkaBridge,
                                       @Qualifier("requestExternalSsl") ObjectProvider<OutputBridge> kafkaSslBridge,
                                       @Qualifier("requestExternalAvro") ObjectProvider<OutputBridge> avroBridge,
                                       RequestMapper requestMapper) {
        log.info("Creating request sender");
        return new RequestSenderImpl(kafkaBridge, kafkaSslBridge, avroBridge, requestMapper);
    }

    /**
     * Отправитель уведомлений
     *
     * @param kafkaBridge    связь между сообщениями и топиками kafka binder
     * @param kafkaSslBridge связь между сообщениями и топиками kafka-ssl binder
     * @param avroBridge     связь между сообщениями и топиками kafka-avro binder
     * @param mapper         поставщик сотрудников
     * @return Отправитель уведомлений
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public NotificationSender notificationSender(@Qualifier("notificationsOutput") ObjectProvider<OutputBridge> kafkaBridge,
                                                 @Qualifier("notificationsOutputSsl") ObjectProvider<OutputBridge> kafkaSslBridge,
                                                 @Qualifier("notifications") ObjectProvider<OutputBridge> avroBridge,
                                                 NotificationMapper mapper) {
        log.info("Creating notification sender");
        return new NotificationSenderImpl(kafkaBridge, kafkaSslBridge, avroBridge, mapper);
    }

    /**
     * Отправитель уведомлений по веб-сокету
     *
     * @return Отправитель уведомлений по веб-сокету
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public NotificationSender notificationWebSocketSender() {
        log.info("Creating notification web socket sender");
        return new NotificationWebSocketSenderImpl();
    }

    /**
     * Слушатель обновлений статуса заявки извне (например, от ai_payout_check)
     *
     * @param tripOrdersProvider поставщик заявок
     * @return Слушатель обновлений статуса заявки
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    public Consumer<UpdateTripRequestStatusMessage> updateStatusInput(
            TripOrdersProvider tripOrdersProvider
    ) {
        log.info("Creating update status listener");
        return new UpdateStatusListener(tripOrdersProvider);
    }

    /**
     * Слушатель обновлений статуса заявки извне (для SSL)
     *
     * @param tripOrdersProvider поставщик заявок
     * @return Слушатель обновлений статуса заявки
     */
    @Bean(bootstrap = Bean.Bootstrap.BACKGROUND)
    public Consumer<UpdateTripRequestStatusMessage> updateStatusInputSsl(
            TripOrdersProvider tripOrdersProvider
    ) {
        log.info("Creating update status SSL listener");
        return new UpdateStatusListener(tripOrdersProvider);
    }

    /**
     * Отправитель сообщений в реестр выплат
     *
     * @param kafkaBridge    связующая между сообщениями и топиками kafka binder
     * @param kafkaSslBridge связующая между сообщениями и топиками kafka-ssl binder
     * @return Отправитель сообщений в реестр выплат
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public RequestPayoutSender requestPayoutSender(
            @Qualifier("requestPayoutOutput") ObjectProvider<OutputBridge> kafkaBridge,
            @Qualifier("requestPayoutOutputSsl") ObjectProvider<OutputBridge> kafkaSslBridge
    ) {
        log.info("Creating request-payout sender");
        return new RequestPayoutSenderImpl(kafkaBridge, kafkaSslBridge);
    }

    /**
     * Отправитель сообщений в фрод-мониторинг
     *
     * @param kafkaBridge связующая между сообщениями и топиками kafka binder
     * @return Отправитель сообщений в фрод-мониторинг
     */
    @Bean(bootstrap = Bean.Bootstrap.DEFAULT)
    public FraudMonitoringSender fraudMonitoringSender(
            @Qualifier("fraudMonitoringOutput") ObjectProvider<OutputBridge> kafkaBridge
    ) {
        log.info("Creating fraud monitoring sender");
        return new FraudMonitoringSenderImpl(kafkaBridge);
    }

    /**
     * Создать отправителя сообщений в сканер чеков
     *
     * @param kafkaBridge    мост между адаптером брокера и местом отправки
     * @param kafkaSslBridge мост между адаптером брокера и местом отправки
     * @return отправитель
     */
    @Bean
    public ReceiptSender receiptScannerSender(
            @Qualifier("receiptScannerOutput") ObjectProvider<OutputBridge> kafkaBridge,
            @Qualifier("receiptScannerOutputSsl") ObjectProvider<OutputBridge> kafkaSslBridge
    ) {
        log.info("Creating receipt scanner sender");
        return new ReceiptScannerSenderImpl(kafkaBridge, kafkaSslBridge);
    }
}
