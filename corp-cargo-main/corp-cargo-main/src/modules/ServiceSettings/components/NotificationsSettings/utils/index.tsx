import {
  Notification,
  NotificationsChannel,
  NotificationsHeaders,
  NotificationsRecord
} from 'stores/Notifications/Notifications.interface';
import { UUID } from 'utils/io-ts';

export const getChannelText = (notification: Notification, notificationChannel: NotificationsChannel) => notification.channels?.find(({ channel }) => channel === notificationChannel)?.text ?? '';

export type TableRecord = PartialBy<NotificationsRecord, 'id'>;

export const notifications2Records = (records: Notification[], organizationId: UUID): TableRecord[] => records.map((elem: Notification) => ({
  ...elem,
  organizationId,
  [NotificationsHeaders.notification]: elem.name,
  [NotificationsHeaders.pushNotification]: getChannelText(elem, NotificationsChannel.PUSH),
  [NotificationsHeaders.outlookNotification]: getChannelText(elem, NotificationsChannel.EMAIL),
  [NotificationsHeaders.smsNotification]: getChannelText(elem, NotificationsChannel.SMS),
  [NotificationsHeaders.enabledChannels]: elem.channels ?? [],
  [NotificationsHeaders.approvementFrequency]: elem.timings,
  [NotificationsHeaders.statusFrequency]: elem.timings,
  [NotificationsHeaders.excessWaitingInIntermediate]: elem.countings,
  [NotificationsHeaders.lowLimitNotifications]: elem.countings,
}));

export const records2Notifications = (records: TableRecord[]): Notification[] => records.map((elem: TableRecord) => ({
  notificationClass: elem.notificationClass,
  notificationType: elem.notificationType,
  name: elem.notification,
  description: elem.description ?? '',
  id: elem.id,
  channels: elem.enabledChannels,
  timings: elem.timings,
  countings: elem.countings,
  restrictions: elem.restrictions,
}));
