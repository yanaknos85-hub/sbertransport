package ru.sber.transport.notifications.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.sber.transport.notifications.services.EmployeeService;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sber.transport.notifications.database.dao.NotificationRepository;
import ru.sber.transport.notifications.database.dao.messages.limits.LimitRepository;
import ru.sber.transport.notifications.database.model.Notification;
import ru.sber.transport.notifications.database.model.coprorate.Employee;
import ru.sber.transport.notifications.database.model.limits.Limit;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationClass;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationSettings;
import ru.sber.transport.notifications.database.model.settings.notification.NotificationType;
import ru.sber.transport.notifications.services.NotificationSettingsService;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component("limitProcessor")
class LimitProcessorImpl extends BaseProcessor<Limit> {

    private final NotificationRepository notificationRepository;

    private final LimitRepository limitRepository;

    private final EmployeeService employeeService;

    private final NotificationSettingsService notificationSettingsService;

    private final ObjectMapper objectMapper;

    @Async
    @Transactional
    @Override
    @SuppressWarnings("java:S3958")
    public void process(@NonNull Limit limit) {
        limitRepository.findById(Objects.requireNonNull(limit.getId())).ifPresent(this::processLimit);
    }

    @SneakyThrows(JsonProcessingException.class)
    private void processLimit(Limit limit) {
        UUID id = limit.getId();
        UUID ownerId = limit.getOwnerId();

        var employee = employeeService.get(limit.getOwnerId()).orElseThrow();

        var limitNotificationList =
                notificationRepository.findAll().stream().filter(m -> {
                            try {
                                return m.getEntityFromJson(Limit.class).getId() == id;
                            } catch (JsonProcessingException e) {
                                return false;
                            }
                        })
                        .toList();

        // начальные значения по умолчанию
        var limitData = new LimitData(ownerId, limitNotificationList.isEmpty());

        if (!limitData.isLimitNew) {
            editLimitData(limit, limitData, limitNotificationList);
        }
        // проверка - сообщение от Limit ?
        sendMessage(limit, limitData, employee);
    }

    private void sendMessage(@NotNull Limit limit, LimitData limitData, Employee employee) throws JsonProcessingException {
        if (limit.getTransportType() == null) {
            processNoTransportType(limit, limitData, employee);
        } else {
            if (limit.getBalance() != limitData.balanceEntity) {
                selectDepartmentOrPerson(limit, NotificationType.LOW_REMAINS);
            }
        }
        // на лимит изменена сумма, также проверяем распределение лимита по периоду
        if (limit.getSum() != limitData.sumEntity && limitData.sumEntity != 0) {
            selectDepartmentOrPerson(limit, NotificationType.CHANGE);
        }
    }

    private void processNoTransportType(@NotNull Limit limit, LimitData limitData, Employee employee) throws JsonProcessingException {
        var limitStatus = limit.getLimitStatus();
        var organizationId = limit.getOrganizationId();
        var notificationClass = NotificationClass.USER_OWNER_LIMIT;
        var ownerId = limit.getOwnerId();
        if (limitData.isLimitNew) {
            // создан новый лимит, назначен владелец
            process(employee.getId(), notificationSettingsService.get(organizationId, notificationClass,
                    NotificationType.ASSIGNMENT), limit);
        } else {
            // владелец сменился
            if (!ownerId.equals(limitData.ownerIdEntity)) {
                process(employee.getId(), notificationSettingsService.get(organizationId, notificationClass,
                        NotificationType.RESTRICTIONS_EDITED), limit);
            }
            // статус лимита поменялся
            if (!limitStatus.equals(limitData.limitStatusEntity)) {
                selectDepartmentOrPerson(limit, NotificationType.STATUS);
            }
        }
        // на лимит распределена сумма
        if (limitStatus.equals("SHARED") && limit.getSum() != 0 && limitData.sumEntity == 0) {
            selectDepartmentOrPerson(limit, NotificationType.ALLOCATION);
        }
    }

    private void editLimitData(@NotNull Limit limit, LimitData limitData, List<Notification> limitNotificationList) throws JsonProcessingException {
        var limitNotificationLast = limitNotificationList.get(limitNotificationList.size() - 1);
        var entity = objectMapper.readValue(limitNotificationLast.getEntity(), Limit.class);
        limitData.setEntity(entity);

        // если задан новый период без изменения суммы, меняем прежнее сообщение, устанавливаем период
        if (limit.getSum() == limitData.sumEntity && limit.getPeriodNumber() != limitData.periodNumberEntity) {
            limitNotificationLast.setEntity(objectMapper.writeValueAsString(limit));
            notificationRepository.save(limitNotificationLast);
        }
    }

    @SuppressWarnings("java:S6205")
    private void selectDepartmentOrPerson(Limit limit, NotificationType notificationType) throws JsonProcessingException {
        NotificationSettings notificationSettings;
        var employee = employeeService.get(limit.getOwnerId()).orElseThrow();
        var limitType = limit.getLimitType();
        switch (limitType) {
            case "DEPARTMENT" -> {
                try {
                    notificationSettings =
                            notificationSettingsService.get(limit.getOrganizationId(),
                                    NotificationClass.LIMIT_DEPARTMENT,
                                    notificationType);
                    process(employee.getId(), notificationSettings, limit);
                } catch (EntityNotFoundException e) {
                    log.warn("Settings for {} at organization {} for event {} and type {} not found",
                            limit.getOwnerId(), limit.getOrganizationId(), NotificationClass.LIMIT_DEPARTMENT,
                            notificationType);
                }
            }
            case "EMPLOYEE" -> {
                try {
                    notificationSettings = notificationSettingsService.get(limit.getOrganizationId(),
                            NotificationClass.LIMIT_PERSON,
                            notificationType);
                    process(employee.getId(), notificationSettings, limit);
                } catch (EntityNotFoundException e) {
                    log.warn("Settings for {} at organization {} for event {} and type {} not found",
                            limit.getOwnerId(), limit.getOrganizationId(), NotificationClass.LIMIT_DEPARTMENT,
                            notificationType);
                }
            }
            default -> log.info("Unknown type '{}'", limitType);
        }
    }

    @RequiredArgsConstructor
    private static class LimitData {

        private long sumEntity;

        private long balanceEntity;

        private String limitStatusEntity = "";

        @NonNull
        private UUID ownerIdEntity;

        private final boolean isLimitNew;

        private int periodNumberEntity;

        public void setEntity(Limit entity) {
            sumEntity = entity.getSum();
            balanceEntity = entity.getBalance();
            limitStatusEntity = entity.getLimitStatus();
            ownerIdEntity = entity.getOwnerId();
            periodNumberEntity = entity.getPeriodNumber();
        }
    }
}

