import * as t from 'io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

export enum NotificationClass {
  REQUEST_CARGO = 'REQUEST_CARGO',
  REQUEST_PASSENGERS = 'REQUEST_PASSENGERS',
  REQUEST_TAXI = 'REQUEST_TAXI',
  REQUEST_FLEET = 'REQUEST_FLEET',
  REQUEST_COMMON = 'REQUEST_COMMON',
}

export const Notification = t.type({
  id: t.string,
  userId: t.string,
  notificationId: t.string,
  notificationClass: ioTypeFromEnum<NotificationClass>('NotificationClass', NotificationClass),
  name: t.string,
  parentId: t.string,
  pushActive: t.boolean,
  pushEnabled: t.boolean,
  emailActive: t.boolean,
  emailEnabled: t.boolean,
  smsActive: t.boolean,
  smsEnabled: t.boolean,
});

export const Response = t.type({
  content: t.array(Notification),
  pageable: t.type({
    pageNumber: t.number,
    pageSize: t.number,
    sort: t.type({
      empty: t.boolean,
      sorted: t.boolean,
      unsorted: t.boolean,
    }),
    offset: t.number,
    paged: t.boolean,
    unpaged: t.boolean,
  }),
  totalPages: t.number,
  totalElements: t.number,
  last: t.boolean,
  size: t.number,
  number: t.number,
  sort: t.type({
    empty: t.boolean,
    sorted: t.boolean,
    unsorted: t.boolean,
  }),
  numberOfElements: t.number,
  first: t.boolean,
  empty: t.boolean,
});

export const SettingsRequest = t.type({
  smsActive: t.boolean,
  emailActive: t.boolean,
  pushActive: t.boolean,
});

export const NotificationListRequest = t.type({
  settings: SettingsRequest,
  page: t.number,
  pageSize: t.number,
  directionAsc: t.boolean,
  sortField: t.string,
  class: ioTypeFromEnum<NotificationClass>('NotificationClass', NotificationClass),
});

export type TResponse = t.TypeOf<typeof Response>;
export type TNotification = t.TypeOf<typeof Notification>;
export type TSettingsRequest = t.TypeOf<typeof SettingsRequest>;

export interface Settings {
  page: number;
  pageSize: number;
  directionAsc: boolean;
  sortField: string;
  class: string;
}
