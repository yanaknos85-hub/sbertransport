/* eslint-disable @typescript-eslint/no-explicit-any */
import {
  APIQueryResult, updateQueryCache, useAPI, useAPIMutation
} from 'api';
import {
  CREATE_NOTIFICATIONS_SETTINGS,
  CREATE_NOTIFICATIONS_SETTINGS_MASS,
  GET_ALL_NOTIFICATIONS_SETTINGS,
  UPDATE_NOTIFICATIONS_SETTINGS,
  UPDATE_NOTIFICATIONS_SETTINGS_MASS
} from 'constants/constants.api';
import * as t from 'io-ts';
import { Notification, NotificationClass } from 'stores/Notifications/Notifications.interface';
import { UUID } from 'utils/io-ts';

const NotificationsCacheValue = t.record(t.string, t.array(Notification));

export type NotificationsCacheValue = t.TypeOf<typeof NotificationsCacheValue>;

declare module 'api' {
  interface Cache {
    notifications: {
      key: ['notifications', UUID];
      value: NotificationsCacheValue;
    };
  }
}

const raw2Cache = (notifications: Notification[]): NotificationsCacheValue => {
  const notificationsByClass = Object.values(NotificationClass).reduce<NotificationsCacheValue>(
    (byClass, notificationClass) => ({ ...byClass, [notificationClass]: [] }),
    {}
  );

  notifications.forEach((notification: Notification) => {
    notificationsByClass[notification.notificationClass]?.push(notification);
  });

  return notificationsByClass;
};

export const useNotifications = (orgId: UUID): APIQueryResult<NotificationsCacheValue, unknown> => useAPI(['notifications', orgId], ({ http, process }) => http
  .get<Notification[]>(GET_ALL_NOTIFICATIONS_SETTINGS, { urlParams: { orgId } })
  .then(process.decodeResponseData(t.array(Notification)))
  .then(raw2Cache)
);

export const useSaveNotifications = (orgId: UUID) => useAPIMutation(
  ({ http }, notification: PartialBy<Notification, 'id'>) => {
    const isUUID = isNaN(Number(notification.id));

    if (notification.id && isUUID) {
      return http.put(UPDATE_NOTIFICATIONS_SETTINGS, notification, {
        urlParams: { orgId, notId: notification.id },
      });
    }

    return http.post(CREATE_NOTIFICATIONS_SETTINGS, notification, {
      urlParams: { orgId },
    });
  },
  {
    onSuccess: ({
      cache, result: response, variables: notification,
    }) => updateQueryCache(cache, ['notifications', orgId], (cache: NotificationsCacheValue) => {
      const notifications = [...cache[notification.notificationClass], notification].map((other: Notification) => {
        // 1 case: sent 'post' -> change it in cache
        if (response.data && other.id === notification.id) {
          return response.data as any;
        }

        // 2 case: sent 'put' -> change it in cache
        if (other.id === notification.id) {
          return notification;
        }

        // 3 case: sent 'post' or 'put' but it's not our record -> leave without changes
        return other;
      });

      return {
        ...cache,
        [notification.notificationClass]: notifications,
      };
    }),
  }
);

export const useCreateNotificationsMass = (orgId: UUID) => useAPIMutation<Notification[], any, Notification[]>(
  ({ http }, notifications): any => (
    http.post<Promise<Notification[]>>(CREATE_NOTIFICATIONS_SETTINGS_MASS, notifications, { urlParams: { orgId } })
  ),
  {
    onSuccess: ({ cache, result: response }) => updateQueryCache(cache, ['notifications', orgId], (cache: NotificationsCacheValue) => {
      const notificationClass = response[0]?.notificationClass;

      if (notificationClass) {
        return {
          ...cache,
          [notificationClass]: [...cache[notificationClass], ...response],
        };
      }

      return {
        ...cache,
      };
    }),
  }
);

export const useUpdateNotificationsMass = (orgId: UUID) => useAPIMutation<never, any, Notification[]>(
  ({ http }, notifications): any => (
    http.put<Promise<Notification[]>>(UPDATE_NOTIFICATIONS_SETTINGS_MASS, notifications, { urlParams: { orgId } })
  ),
  {
    onSuccess: ({
      cache, process, variables: notifications,
    }) => {
      updateQueryCache(cache, ['notifications', orgId], (cache: NotificationsCacheValue) => {
        const notificationClass = notifications[0]?.notificationClass;
        return { ...cache, [notificationClass]: [...cache[notificationClass], ...notifications] };
      });
      process.processStatus(200, 'Настройки уведомлений успешно сохранены');
    },
  }
);
