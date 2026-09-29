import {
  Settings,
  TResponse,
  Response,
  TNotification,
  Notification
} from '../modules/ProfilePage/components/Notifications/Notifications.interface';

import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import { MutationResultPair } from 'react-query';
import { GET_NOTIFICATIONS, POST_NOTIFICATIONS } from '../constants/constants.env';

declare module 'api' {
  interface Cache {
    notifications: {
      key: ['notifications', Settings];
      value: TResponse;
    };
    notificationsPassengers: {
      key: ['notificationsPassengers', Settings];
      value: TResponse;
    };
  }
}

export const useNotificationsCargo = (settings: Settings): APIQueryResult<TResponse, unknown> => useAPI(['notifications', settings], ({ http, process }) => http
  .get<TResponse>(GET_NOTIFICATIONS, { urlParams: { ...settings } as any })
  .then(process.decodeResponseData(Response))
  .catch(err => {
    process.processStatus(404, err);
    return [] as any;
  }),
{
  retry: 3, // в случае неудачного запроса, будет повторяться 3 раза (по просьбе бэка)
  keepPreviousData: true,
}
);

export const useNotificationsPassengers = (settings: Settings): APIQueryResult<TResponse, unknown> => useAPI(['notificationsPassengers', settings], ({ http, process }) => http
  .get<TResponse>(GET_NOTIFICATIONS, { urlParams: { ...settings } as any })
  .then(process.decodeResponseData(Response))
  .catch(err => {
    process.processStatus(404, err);
    return [] as any;
  }),
{
  retry: 3, // в случае неудачного запроса, будет повторяться 3 раза (по просьбе бэка)
  keepPreviousData: true,
}
);

export const useUpdateNotifications = (): MutationResultPair<TNotification, unknown, { id: string; settings: { smsActive: boolean; emailActive: boolean; pushActive: boolean } }, unknown> => useAPIMutation(({ http, process }, { id, settings }) => {
  return (
    http
      .put<TNotification>(POST_NOTIFICATIONS, { ...settings }, { urlParams: { id } })
      .then(process.decodeResponseData(Notification))
      .catch(process.getResponseData));
},
{
  onSuccess: ({ cache }) => {
    cache.refetchQueries(['notifications']);
  },
  onError: ({ logger }) => {
    logger.toMessage('error', 'Произошла ошибка при изменении настроек уведомлений!');
  },
});
