import React, { useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'i18n';

import {
  useCreateNotificationsMass,
  useNotifications,
  useSaveNotifications,
  useUpdateNotificationsMass
} from 'api/notifications';
import { useProfile } from 'api/profile';
import { EditableTableStore } from 'shared/components/EditableTable';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import {
  Notification,
  NotificationClass,
  NotificationsChannel,
  NotificationType
} from 'stores/Notifications/Notifications.interface';
import { useDefaultNotificationsByClasses } from './useDefaultNotifications';
import { notifications2Records, records2Notifications, TableRecord } from '../utils';
import { ignore } from 'utils';

const defaultNotificationType = NotificationClass.REQUEST_TAXI;

export const useNotificationsSettings = (): {
  store: EditableTableStore<TableRecord>;
  setNotificationClass: React.Dispatch<React.SetStateAction<NotificationClass>>;
  setToDefault: () => void;
  notificationClass: NotificationClass;
  updateOrCreateNotificationMass(): void;
} => {
  const { t } = useTranslation();
  const { logger } = useAppStoreContext();
  const [notificationClass, setNotificationClass] = useState<NotificationClass>(defaultNotificationType);

  const profile = useProfile().data;
  const { data: notifications } = useNotifications(profile.organizationId);

  const store = useMemo(
    () => new EditableTableStore<TableRecord>({
      rowKeyFn: item => item?.description,
      isDeletable: () => false,
    }),
    []
  );

  const [saveNotifications] = useSaveNotifications(profile.organizationId);
  const [createNotificationsMass] = useCreateNotificationsMass(profile.organizationId);
  const [updateNotificationsMass] = useUpdateNotificationsMass(profile.organizationId);

  // TODO: Delete default settings after creating module SMD (Analytics).

  const defaultNotifications = useDefaultNotificationsByClasses();

  const setToDefault = () => {
    const defaultData = notifications2Records(defaultNotifications[notificationClass], profile.organizationId);
    const notificationsWithIds = store.data.filter(storeNotification => !!storeNotification.id);

    store.data = defaultData.map(defaultItem => {
      const finnedWithId = notificationsWithIds.find(({ description }) => description === defaultItem.description);
      return { ...defaultItem, id: finnedWithId?.id };
    });
    updateNotificationsMass(records2Notifications(store.data)).catch(ignore);
  };

  const updateOrCreateNotificationMass = () => {
    /* у уведомлений пришедших с сервера есть присвоенный id - обновляем настройки уведомления */
    const created = store.data.filter(({ id }) => !!id);
    /* если нет id - создаём новое уведомление */
    const notCreated = store.data.filter(({ id }) => !id);

    createNotificationsMass(records2Notifications(notCreated)).catch(error => {
      if (error.response.data?.message?.includes(t.NotificationsSettings.duplicationError)) {
        logger.toMessage(
          'error',
          `Уведомление ${error.response.data.problems && error.response.data.problems[0].value} уже существует`
        );
      }
    });
    updateNotificationsMass(records2Notifications(created)).catch(ignore);
  };

  useEffect(() => {
    store.handleSave = async (record: TableRecord) => {
      const countings
        = record?.notificationType === NotificationType.LOW_REMAINS
          ? record.lowLimitNotifications ?? record.excessWaitingInIntermediate
          : record.excessWaitingInIntermediate ?? record.lowLimitNotifications;

      return (
        (
          await saveNotifications({
            id: record.id,
            notificationClass,
            notificationType: record?.notificationType,
            name: record.notification,
            restrictions: record.restrictions,
            description: record.description,
            countings,
            timings:
              record?.notificationType === NotificationType.APPROVE
                ? record.approvementFrequency
                : record.statusFrequency,
            channels: [
              {
                channel: NotificationsChannel.PUSH,
                text: record.pushNotification,
                enabled: record.enabledChannels.some(c => c.channel === NotificationsChannel.PUSH),
              },
              {
                channel: NotificationsChannel.SMS,
                text: record.smsNotification,
                enabled: record.enabledChannels.some(c => c.channel === NotificationsChannel.SMS),
              },
              {
                channel: NotificationsChannel.EMAIL,
                text: record.outlookNotification,
                enabled: record.enabledChannels.some(c => c.channel === NotificationsChannel.EMAIL),
              },
            ],
          }).catch(ignore)
        )?.status === 200
      );
    };
  }, [store, saveNotifications, notificationClass]);

  useEffect(() => {
    const uniq = new Map<string, Notification>();
    const merged = [...defaultNotifications[notificationClass], ...notifications[notificationClass]];

    merged.forEach(notification => uniq.set(notification.description, notification));

    /* в строки таблицы попадают уведомления с сервера или настройки по умолчанию, если на сервере не сохранены
    настройки для данного уведомления */
    store.data = notifications2Records(Array.from(uniq.values()), profile.organizationId);
  }, [store, notifications, defaultNotifications, notificationClass, profile.organizationId]);

  return {
    store,
    setNotificationClass,
    setToDefault,
    notificationClass,
    updateOrCreateNotificationMass,
  };
};
