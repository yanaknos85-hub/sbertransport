package ru.sber.transport.notifications.enums;

public enum UserNotificationSettingsSortOption {
    NOTIFICATION_CLASS("Класс уведомления"),
    NAME("Название уведомления"),
    PUSH_ACTIVE("Активность push-уведомления в ЛК"),
    PUSH_ENABLED("Доступность push-уведомления для организации"),
    EMAIL_ACTIVE("Активность email-уведомления в ЛК"),
    EMAIL_ENABLED("Доступность email-уведомления для организации"),
    SMS_ACTIVE("Активность sms-уведомления в ЛК"),
    SMS_ENABLED("Доступность sms-уведомления для организации");

    private final String description;

    public String getDescription() {
        return this.description;
    }

    UserNotificationSettingsSortOption(String description) {
        this.description = description;
    }
}