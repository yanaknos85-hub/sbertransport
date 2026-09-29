/* eslint-disable no-use-before-define */
import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

/*
The main feature of this module is using of backend templates.
It is marked by {XXX}
*/

export enum NotificationsChannel {
  PUSH = 'PUSH',
  SMS = 'SMS',
  EMAIL = 'EMAIL',
}

export type EnabledChannels = { channel: NotificationsChannel; enabled: boolean; text?: string }[];

export enum NotificationsEventTypes {
  AT_EVENT = 'AT_EVENT',
  DEADLINE = 'DEADLINE',
}

export enum ApprovementFrequencyNotificationsTitles {
  AT_EVENT = 'После регистрации заявки',
  DEADLINE = 'За определенное время до конца контрольного срока:',
}

export enum SelectStatusFrequencyNotificationsTitles {
  AT_EVENT = 'По факту действия',
  DEADLINE = 'За определенное время до конца контрольного срока:',
}

export enum NotificationsCountingsTypes {
  EXACT = 'EXACT',
  REMAINS = 'REMAINS',
  PERCENT = 'PERCENT',
}

export enum NotificationsCountingsTypesTitles {
  EXACT = 'Сумма',
  REMAINS = 'Осталось',
  PERCENT = 'Процент',
}

export enum NotificationsRestrictionsTypes {
  ALLOW_ALL = 'ALLOW_ALL',
  ALLOW_BUT = 'ALLOW_BUT',
  DENY_BUT = 'DENY_BUT',
  DENY_ALL = 'DENY_ALL',
}

export enum NotificationsRestrictionsTypesHeaders {
  ALLOW_ALL = 'Отправлять всем',
  ALLOW_BUT = 'Отправлять некоторым:',
  DENY_BUT = 'Отправлять всем кроме:',
  DENY_ALL = 'Не отправлять никому',
}

export enum NotificationsEventValue {
  TenMinutes = 10,
  ThirtyMinutes = 30,
  OneHour = 60,
}

export enum NotificationsEventValueTitle {
  ThirtyMinutes = 'за 30 минут',
  TenMinutes = 'за 10 минут',
  OneHour = 'за 60 минут',
}

export enum NotificationTypeSettings {
  PASSENGERS = 'PASSENGERS',
  CARGO = 'CARGO',
  FLEET = 'FLEET',
  SHARED = 'SHARED',
}

export enum NotificationClass {
  REQUEST_TAXI = 'REQUEST_TAXI',
  REQUEST_CARGO = 'REQUEST_CARGO',
  REQUEST_PUBLIC = 'REQUEST_PUBLIC',
  REQUEST_PERSONAL = 'REQUEST_PERSONAL',
  REQUEST_CAR_SHARING = 'REQUEST_CAR_SHARING',
  REQUEST_BICYCLE = 'REQUEST_BICYCLE',
  REQUEST_SCOOTER = 'REQUEST_SCOOTER',
  REQUEST_LIMIT_PERSON = 'REQUEST_LIMIT_PERSON',
  REQUEST_LIMIT_DEPARTMENT = 'REQUEST_LIMIT_DEPARTMENT',
  LIMIT_DEPARTMENT = 'LIMIT_DEPARTMENT',
  LIMIT_PERSON = 'LIMIT_PERSON',
  USER_DELEGATE = 'USER_DELEGATE',
  USER_OWNER_LIMIT = 'USER_OWNER_LIMIT',
  USER_ASSIGNMENT = 'USER_ASSIGNMENT',
  CONTRACTOR = 'CONTRACTOR',
  DISPATCHER_NOTIFICATION = 'DISPATCHER_NOTIFICATION',
}

// Табы в табе
export const NotificationClassTypes = {
  [NotificationTypeSettings.PASSENGERS]: [
    NotificationClass.REQUEST_TAXI,
    NotificationClass.REQUEST_PUBLIC,
    NotificationClass.REQUEST_PERSONAL,
    NotificationClass.REQUEST_CAR_SHARING,
    NotificationClass.REQUEST_BICYCLE,
    NotificationClass.REQUEST_SCOOTER,
  ],
  [NotificationTypeSettings.CARGO]: [
    NotificationClass.REQUEST_CARGO,
  ],
  [NotificationTypeSettings.SHARED]: [
    NotificationClass.LIMIT_DEPARTMENT,
    NotificationClass.LIMIT_PERSON,
    NotificationClass.USER_ASSIGNMENT,
    NotificationClass.USER_OWNER_LIMIT,
  ],
};

export enum NotificationType {
  // Общие типы уведомлений
  APPROVE = 'APPROVE',
  APPROVE_STATUS = 'APPROVE_STATUS',

  WAITING_FOR_DRIVER = 'WAITING_FOR_DRIVER',
  DRIVER_ASSIGNED = 'DRIVER_ASSIGNED',
  DRIVER_ASSIGNED_XML = 'DRIVER_ASSIGNED_XML',
  DRIVER_ASSIGNED_DISPATCHER = 'DRIVER_ASSIGNED_DISPATCHER',
  DRIVER_ARRIVED = 'DRIVER_ARRIVED',
  FREE_TIME_EXPIRED = 'FREE_TIME_EXPIRED',
  TRIP_STARTED = 'TRIP_STARTED',
  WAYPOINT_ARRIVED = 'WAYPOINT_ARRIVED',
  WAYPOINT_WAITING_EXPIRED = 'WAYPOINT_WAITING_EXPIRED',
  TRIP_FINISHED = 'TRIP_FINISHED',
  COOP_TRIP_ATTACHMENT = 'COOP_TRIP_ATTACHMENT',
  COOP_TRIP_CHANGES = 'COOP_TRIP_CHANGES',

  TRIP_CONFIRM = 'TRIP_CONFIRM',
  TRIP_AFFIRM = 'TRIP_AFFIRM',
  TRIP_AFFIRM_STATUS = 'TRIP_AFFIRM_STATUS',
  PAYMENT_STATUS = 'PAYMENT_STATUS',

  COOP_TRIP_ATTACHMENT_APPROVE = 'COOP_TRIP_ATTACHMENT_APPROVE',
  COOP_TRIP_ATTACHMENT_STATUS = 'COOP_TRIP_ATTACHMENT_STATUS',

  TRIP_START_REMIND = 'TRIP_START_REMIND',

  FINAL_ROUTE_AFFIRM = 'FINAL_ROUTE_AFFIRM',
  FINAL_ROUTE_AFFIRM_STATUS = 'FINAL_ROUTE_AFFIRM_STATUS',

  ADDITIONAL_WAYPOINTS_APPROVAL = 'ADDITIONAL_WAYPOINTS_APPROVAL',
  ADDITIONAL_WAYPOINTS_STATUS = 'ADDITIONAL_WAYPOINTS_STATUS',

  TRANSPORT_BOOKING = 'TRANSPORT_BOOKING',
  TRANSPORT_OVERVIEW = 'TRANSPORT_OVERVIEW',
  TRANSPORT_BOOKING_FINISHED = 'TRANSPORT_BOOKING_FINISHED',

  REQUEST_EXECUTION = 'REQUEST_EXECUTION',
  ALLOCATION = 'ALLOCATION',
  CHANGE = 'CHANGE',
  STATUS = 'STATUS',
  LOW_REMAINS = 'LOW_REMAINS',
  LOW_REMAINS_FROM_EMPLOYEE = 'LOW_REMAINS_FROM_EMPLOYEE',
  ASSIGNMENT = 'ASSIGNMENT',
  RESTRICTIONS_EDITED_DATE = 'RESTRICTIONS_EDITED_DATE',
  RESTRICTIONS_EDITED_TYPE = 'RESTRICTIONS_EDITED_TYPE',
  RESTRICTIONS_EDITED = 'RESTRICTIONS_EDITED',
  PAYMENT = 'PAYMENT',
  PUSH_DECLINED_BY_DRIVER = 'PUSH_DECLINED_BY_DRIVER',
  PUSH_PASSENGER_COMMUNICATION_REQUEST = 'PUSH_PASSENGER_COMMUNICATION_REQUEST',
  PUSH_DRIVER_NOT_ASSIGNED = 'PUSH_DRIVER_NOT_ASSIGNED',
  PUSH_DECLINED_BY_PASSENGER = 'PUSH_DECLINED_BY_PASSENGER',
  PUSH_LOW_GRADE_TRIP = 'PUSH_LOW_GRADE_TRIP',
  PUSH_INDICATION_TRIP_REQUEST = 'PUSH_INDICATION_TRIP_REQUEST',
  DECLINED_BY_DRIVER = 'DECLINED_BY_DRIVER',
  PASSENGER_COMMUNICATION_REQUEST = 'PASSENGER_COMMUNICATION_REQUEST',
  DRIVER_NOT_ASSIGNED = 'DRIVER_NOT_ASSIGNED',
  DECLINED_BY_PASSENGER = 'DECLINED_BY_PASSENGER',
  LOW_GRADE_TRIP = 'LOW_GRADE_TRIP',
  INDICATION_TRIP_REQUEST = 'INDICATION_TRIP_REQUEST',

  DRIVER_REJECTED_REQUEST = 'DRIVER_REJECTED_REQUEST',
  PASSENGER_LOOKING_FOR_DRIVER = 'PASSENGER_LOOKING_FOR_DRIVER',
  PASSENGER_REJECTED_REQUEST = 'PASSENGER_REJECTED_REQUEST',
  TRIP_WITH_LOW_RATE = 'TRIP_WITH_LOW_RATE',
  REQUEST_WITH_INDICATION_CREATED = 'REQUEST_WITH_INDICATION_CREATED',

  // Типы уведомлений для грузов
  CARGO_AWAITING_TRANSFER = 'CARGO_AWAITING_TRANSFER',
  CARGO_TRANSFER_FINISHED = 'CARGO_TRANSFER_FINISHED',
  CARGO_SHIPMENT_FINISHED = 'CARGO_SHIPMENT_FINISHED',
  CARGO_CANCELED_BY_ENGINEER = 'CARGO_CANCELED_BY_ENGINEER',
  CARGO_CANCELED_BY_CONTRACTOR = 'CARGO_CANCELED_BY_CONTRACTOR',
  CARGO_DRIVER_AWAITING_DATA = 'CARGO_DRIVER_AWAITING_DATA',
  CARGO_COURIER_AWAITING_DATA = 'CARGO_COURIER_AWAITING_DATA',
  CARGO_DELIVERY_CONFIRMATION_FINISHED = 'CARGO_DELIVERY_CONFIRMATION_FINISHED',
  CARGO_PLANNING = 'CARGO_PLANNING',
}

/** хук useDefaultNotificationsByClasses создаёт настройки по умолчанию основываясь на индексе элементов
 * NotificationType из массивов NotificationClass. Если поменять местами, добавить или удалить элемент из массива -
 * изменятся данные уведомлений по умолчанию и при попытке создать или обновить настройки либо упадёт ошибка,
 * либо сохранятся неверные данные */
export const NotificationMapper: Record<NotificationClass, NotificationType[]> = {
  [NotificationClass.REQUEST_TAXI]: [
    NotificationType.APPROVE,
    NotificationType.APPROVE_STATUS,
    NotificationType.WAITING_FOR_DRIVER,
    NotificationType.DRIVER_ASSIGNED,
    NotificationType.DRIVER_ASSIGNED_XML,
    NotificationType.DRIVER_ASSIGNED_DISPATCHER,
    NotificationType.DRIVER_ARRIVED,
    NotificationType.FREE_TIME_EXPIRED,
    NotificationType.TRIP_STARTED,
    NotificationType.WAYPOINT_ARRIVED,
    NotificationType.WAYPOINT_WAITING_EXPIRED,
    NotificationType.TRIP_FINISHED,
    NotificationType.COOP_TRIP_ATTACHMENT,
    NotificationType.COOP_TRIP_CHANGES,
  ],
  [NotificationClass.REQUEST_CARGO]: [
    NotificationType.CARGO_AWAITING_TRANSFER,
    NotificationType.CARGO_TRANSFER_FINISHED,
    NotificationType.CARGO_SHIPMENT_FINISHED,
    NotificationType.CARGO_CANCELED_BY_ENGINEER,
    NotificationType.CARGO_CANCELED_BY_CONTRACTOR,
    NotificationType.CARGO_DRIVER_AWAITING_DATA,
    NotificationType.CARGO_COURIER_AWAITING_DATA,
    NotificationType.CARGO_DELIVERY_CONFIRMATION_FINISHED,
    NotificationType.APPROVE,
    NotificationType.APPROVE_STATUS,
    NotificationType.CARGO_PLANNING,
  ],
  [NotificationClass.REQUEST_PUBLIC]: [
    NotificationType.APPROVE,
    NotificationType.APPROVE_STATUS,
    NotificationType.TRIP_CONFIRM,
    NotificationType.TRIP_AFFIRM,
    NotificationType.TRIP_AFFIRM_STATUS,
    NotificationType.PAYMENT_STATUS,
  ],
  [NotificationClass.REQUEST_PERSONAL]: [
    NotificationType.APPROVE,
    NotificationType.APPROVE_STATUS,
    NotificationType.COOP_TRIP_ATTACHMENT_APPROVE,
    NotificationType.COOP_TRIP_ATTACHMENT_STATUS,
    NotificationType.TRIP_START_REMIND,
    NotificationType.WAYPOINT_ARRIVED,
    NotificationType.TRIP_FINISHED,
    NotificationType.FINAL_ROUTE_AFFIRM,
    NotificationType.FINAL_ROUTE_AFFIRM_STATUS,
    NotificationType.PAYMENT,
    NotificationType.PAYMENT_STATUS,
    NotificationType.COOP_TRIP_ATTACHMENT,
    NotificationType.COOP_TRIP_CHANGES,
    NotificationType.ADDITIONAL_WAYPOINTS_APPROVAL,
    NotificationType.ADDITIONAL_WAYPOINTS_STATUS,
  ],
  [NotificationClass.REQUEST_CAR_SHARING]: [
    NotificationType.APPROVE,
    NotificationType.APPROVE_STATUS,
    NotificationType.TRANSPORT_BOOKING,
    NotificationType.TRANSPORT_OVERVIEW,
    NotificationType.TRIP_STARTED,
    NotificationType.WAYPOINT_ARRIVED,
    NotificationType.TRANSPORT_BOOKING_FINISHED,
    NotificationType.TRIP_FINISHED,
    NotificationType.COOP_TRIP_ATTACHMENT,
    NotificationType.COOP_TRIP_CHANGES,
  ],
  [NotificationClass.REQUEST_BICYCLE]: [
    NotificationType.APPROVE,
    NotificationType.APPROVE_STATUS,
    NotificationType.TRANSPORT_BOOKING,
    NotificationType.TRANSPORT_OVERVIEW,
    NotificationType.TRIP_STARTED,
    NotificationType.WAYPOINT_ARRIVED,
    NotificationType.TRANSPORT_BOOKING_FINISHED,
    NotificationType.TRIP_FINISHED,
  ],
  [NotificationClass.REQUEST_SCOOTER]: [
    NotificationType.APPROVE,
    NotificationType.APPROVE_STATUS,
    NotificationType.TRANSPORT_BOOKING,
    NotificationType.TRANSPORT_OVERVIEW,
    NotificationType.TRIP_STARTED,
    NotificationType.WAYPOINT_ARRIVED,
    NotificationType.TRANSPORT_BOOKING_FINISHED,
    NotificationType.TRIP_FINISHED,
  ],
  [NotificationClass.REQUEST_LIMIT_PERSON]: [
    NotificationType.APPROVE,
    NotificationType.APPROVE_STATUS,
    NotificationType.REQUEST_EXECUTION,
  ],
  [NotificationClass.REQUEST_LIMIT_DEPARTMENT]: [
    NotificationType.APPROVE,
    NotificationType.APPROVE_STATUS,
    NotificationType.REQUEST_EXECUTION,
  ],

  [NotificationClass.LIMIT_PERSON]: [
    NotificationType.ALLOCATION,
    NotificationType.CHANGE,
    NotificationType.STATUS,
    NotificationType.LOW_REMAINS,
  ],
  [NotificationClass.LIMIT_DEPARTMENT]: [
    NotificationType.ALLOCATION,
    NotificationType.CHANGE,
    NotificationType.STATUS,
    NotificationType.LOW_REMAINS,
    NotificationType.LOW_REMAINS_FROM_EMPLOYEE,
  ],
  [NotificationClass.USER_DELEGATE]: [
    NotificationType.ASSIGNMENT,
    NotificationType.RESTRICTIONS_EDITED_DATE,
    NotificationType.RESTRICTIONS_EDITED_TYPE,
  ],
  [NotificationClass.USER_OWNER_LIMIT]: [NotificationType.ASSIGNMENT, NotificationType.RESTRICTIONS_EDITED],
  [NotificationClass.USER_ASSIGNMENT]: [NotificationType.ASSIGNMENT],

  [NotificationClass.DISPATCHER_NOTIFICATION]: [
    NotificationType.PUSH_DECLINED_BY_DRIVER,
    NotificationType.PUSH_PASSENGER_COMMUNICATION_REQUEST,
    NotificationType.PUSH_DRIVER_NOT_ASSIGNED,
    NotificationType.PUSH_DECLINED_BY_PASSENGER,
    NotificationType.PUSH_LOW_GRADE_TRIP,
    NotificationType.PUSH_INDICATION_TRIP_REQUEST,
    NotificationType.DECLINED_BY_DRIVER,
    NotificationType.PASSENGER_COMMUNICATION_REQUEST,
    NotificationType.DRIVER_NOT_ASSIGNED,
    NotificationType.DECLINED_BY_PASSENGER,
    NotificationType.LOW_GRADE_TRIP,
    NotificationType.INDICATION_TRIP_REQUEST,
  ],

  [NotificationClass.CONTRACTOR]: [
    NotificationType.DRIVER_ASSIGNED,
    NotificationType.DRIVER_REJECTED_REQUEST,
    NotificationType.PASSENGER_LOOKING_FOR_DRIVER,
    NotificationType.PASSENGER_REJECTED_REQUEST,
    NotificationType.TRIP_WITH_LOW_RATE,
    NotificationType.REQUEST_WITH_INDICATION_CREATED,
  ],
};

// Названия табов в табе
export enum NotificationsTypeHeaders {
  REQUEST_TAXI = 'Заявка на такси',
  REQUEST_CARGO = 'Заявка на доставку',
  REQUEST_PUBLIC = 'Заявка на общественный транспорта',
  REQUEST_PERSONAL = 'Заявка на личный транспорт',
  REQUEST_CAR_SHARING = 'Заявка на каршеринг',
  REQUEST_BICYCLE = 'Заявка на велосипед',
  REQUEST_SCOOTER = 'Заявка на самокат',
  REQUEST_LIMIT_PERSON = 'Заявка на личный лимит',
  REQUEST_LIMIT_DEPARTMENT = 'Заявка на лимит подразделения',
  LIMIT_DEPARTMENT = 'Лимит подразделения',
  LIMIT_PERSON = 'Личный лимит',
  USER_DELEGATE = 'Делегаты',
  USER_OWNER_LIMIT = 'Владелец лимита подразделения',
  USER_ASSIGNMENT = 'Назначения',
}

export enum NotificationsHeaders {
  notification = 'notification',
  pushNotification = 'pushNotification',
  smsNotification = 'smsNotification',
  outlookNotification = 'outlookNotification',
  enabledChannels = 'enabledChannels',
  restrictions = 'restrictions',
  approvementFrequency = 'approvementFrequency',
  statusFrequency = 'statusFrequency',
  lowLimitNotifications = 'lowLimitNotifications',
  excessWaitingInIntermediate = 'excessWaitingInIntermediate',
}

export interface NotificationsRecordCommon {
  id: tt.UUID;
  organizationId: tt.UUID;
  notificationType: NotificationType;
  notificationClass: NotificationClass;
  description: string;
  [NotificationsHeaders.notification]: string;
  [NotificationsHeaders.pushNotification]: string;
  [NotificationsHeaders.outlookNotification]: string;
  [NotificationsHeaders.smsNotification]: string;
  [NotificationsHeaders.enabledChannels]: EnabledChannels;
  [NotificationsHeaders.restrictions]?: NotificationsRestrictions;
  timings?: NotificationsTimings;
  countings?: NotificationsCountings;
}

export interface NotificationsRecord extends NotificationsRecordCommon {
  [NotificationsHeaders.approvementFrequency]?: NotificationsTimings;
  [NotificationsHeaders.statusFrequency]?: NotificationsTimings;
  [NotificationsHeaders.excessWaitingInIntermediate]?: NotificationsCountings;
  [NotificationsHeaders.lowLimitNotifications]?: NotificationsCountings;
}

export const NotificationsChannels = t.array(
  t.intersection([
    t.type({
      channel: ioTypeFromEnum<NotificationsChannel>('NotificationsSettingsChannel', NotificationsChannel),
      enabled: t.boolean,
    }),
    t.partial({
      text: t.string,
    }),
  ])
);

// По истечению времени
export const NotificationsTimings = t.array(
  t.intersection([
    t.type({
      eventType: ioTypeFromEnum<NotificationsEventTypes>('NotificationsSettingsEventTypes', NotificationsEventTypes),
    }),
    t.partial({
      timeBefore: t.number,
      timeFieldName: t.string,
    }),
  ])
);

// Вычисляемые
export const NotificationsCountings = t.array(
  t.type({
    value: t.number,
    type: ioTypeFromEnum<NotificationsCountingsTypes>(
      'NotificationsSettingsCountingsTypes',
      NotificationsCountingsTypes
    ),
    property: t.string, // remaining limit by default like this {spring.application.limit}
    // initialProperty: t.string, // start, common limit by default like this {spring.application.limit}
  })
);

export const NotificationsRestrictions = t.array(
  t.type({
    type: ioTypeFromEnum<NotificationsRestrictionsTypes>(
      'NotificationsRestrictionsTypes',
      NotificationsRestrictionsTypes
    ),
    roles: t.array(t.string),
  })
);

export const Notification = t.intersection([
  t.type({
    notificationClass: ioTypeFromEnum<NotificationClass>('NotificationClass', NotificationClass),
    notificationType: ioTypeFromEnum<NotificationType>('notificationType', NotificationType),
    name: t.string,
    description: t.string,
  }),
  t.partial({
    id: tt.uuid,
    channels: NotificationsChannels,
    timings: NotificationsTimings,
    countings: NotificationsCountings,
    restrictions: NotificationsRestrictions,
  }),
]);

export type Notification = t.TypeOf<typeof Notification>;

export type NotificationsChannels = t.TypeOf<typeof NotificationsChannels>;
export type NotificationsTimings = t.TypeOf<typeof NotificationsTimings>;
export type NotificationsCountings = t.TypeOf<typeof NotificationsCountings>;
export type NotificationsRestrictions = t.TypeOf<typeof NotificationsRestrictions>;

export interface NotificationsQueryInput {
  settings: Record<string, NotificationClass[]>;
}
