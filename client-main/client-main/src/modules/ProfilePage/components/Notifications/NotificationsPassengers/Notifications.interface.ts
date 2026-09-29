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
  ],
  [NotificationTypeSettings.CARGO]: [
    NotificationClass.REQUEST_CARGO,
  ],
};

export enum NotificationsTypeHeaders {
  REQUEST_TAXI = 'Заявка на такси',
  REQUEST_CARGO = 'Заявка на доставку',
  REQUEST_PUBLIC = 'Заявка на общественный транспорт',
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
