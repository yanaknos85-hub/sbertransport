import * as R from 'ramda';
import {
  Notification,
  NotificationsChannel,
  NotificationsCountingsTypes,
  NotificationsEventTypes,
  NotificationsRestrictionsTypes,
  NotificationClass,
  NotificationType,
  NotificationMapper
} from 'stores/Notifications/Notifications.interface';
import { Notifications } from 'i18n/ru/notifications';
import { NotificationInfo } from 'i18n';

export const useDefaultNotificationsByClasses = (): Record<string, Notification[]> => {
  const classes = Object.values(NotificationClass) as NotificationClass[];

  const byTypes = classes.map(nclass => {
    const notificationsByType: Record<string, NotificationInfo> = Notifications[nclass] ?? {};

    const notifications: Notification[] = Object.values(notificationsByType).map((notificationByType, key) => ({
      notificationClass: nclass as NotificationClass,
      notificationType: NotificationMapper[nclass][key] as NotificationType,
      name: notificationByType.name,
      description: notificationByType.description,
      timings: [
        {
          timeBefore: 30,
          eventType: NotificationsEventTypes.AT_EVENT,
        },
      ],
      countings: [
        {
          value: 200,
          type: NotificationsCountingsTypes.EXACT,
          property: 'Спасибо',
        },
      ],
      restrictions: [
        {
          value: 20,
          type: NotificationsRestrictionsTypes.ALLOW_ALL,
          roles: [],
        },
      ],
      channels: [
        {
          channel: NotificationsChannel.EMAIL,
          text: notificationByType.text,
          enabled: true,
        },
        {
          channel: NotificationsChannel.PUSH,
          text: notificationByType.text,
          enabled: true,
        },
        {
          channel: NotificationsChannel.SMS,
          text: notificationByType.text,
          enabled: true,
        },
      ],
    }));

    return {
      [nclass]: notifications,
    };
  });

  return R.mergeAll(byTypes);
};
