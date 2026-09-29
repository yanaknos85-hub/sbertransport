export enum CarsharingStatuses {
  CARSHARING_AWAITING_APPROVAL = 'CARSHARING_AWAITING_APPROVAL',
  CARSHARING_APPROVED = 'CARSHARING_APPROVED',
  CARSHARING_DECLINED = 'CARSHARING_DECLINED',
  CARSHARING_AWAITING_SEARCH = 'CARSHARING_AWAITING_SEARCH',
  CARSHARING_TRIP_IN_PROGRESS = 'CARSHARING_TRIP_IN_PROGRESS',
  CARSHARING_AWAITING_TRIP_APPROVAL = 'CARSHARING_AWAITING_TRIP_APPROVAL',
  CARSHARING_TRIP_FINISHED = 'CARSHARING_TRIP_FINISHED',
  CARSHARING_CANCELLED = 'CARSHARING_CANCELLED',
}

export enum GroupTransferStatuses {
  GROUP_TRANSFER_AWAITING_APPROVAL = 'GROUP_TRANSFER_AWAITING_APPROVAL',
  GROUP_TRANSFER_APPROVED = 'GROUP_TRANSFER_APPROVED',
  GROUP_TRANSFER_AWAITING_SEARCH = 'GROUP_TRANSFER_AWAITING_SEARCH',
  GROUP_TRANSFER_DRIVER_SEARCH = 'GROUP_TRANSFER_DRIVER_SEARCH',
  GROUP_TRANSFER_DRIVER_FOUND = 'GROUP_TRANSFER_DRIVER_FOUND',
  GROUP_TRANSFER_DRIVER_ON_THE_WAY = 'GROUP_TRANSFER_DRIVER_ON_THE_WAY',
  GROUP_TRANSFER_DRIVER_ARRIVED = 'GROUP_TRANSFER_DRIVER_ARRIVED',
  GROUP_TRANSFER_TRIP_IN_PROGRESS = 'GROUP_TRANSFER_TRIP_IN_PROGRESS',
  GROUP_TRANSFER_TRIP_FINISHED = 'GROUP_TRANSFER_TRIP_FINISHED',
  GROUP_TRANSFER_CANCELLED = 'GROUP_TRANSFER_CANCELLED',
}

export enum PersonalStatuses {
  PERSONAL_AWAITING_APPROVAL = 'PERSONAL_AWAITING_APPROVAL',
  PERSONAL_APPROVED = 'PERSONAL_APPROVED',
  PERSONAL_AWAITING_SHARED_RIDE_APPROVAL = 'PERSONAL_AWAITING_SHARED_RIDE_APPROVAL',
  PERSONAL_SHARED_RIDE_DECLINED = 'PERSONAL_SHARED_RIDE_DECLINED',
  PERSONAL_SHARED_RIDE_APPROVED = 'PERSONAL_SHARED_RIDE_APPROVED',
  PERSONAL_ADDITIONAL_WAYPOINTS_APPROVAL = 'PERSONAL_ADDITIONAL_WAYPOINTS_APPROVAL',
  PERSONAL_ADDITIONAL_WAYPOINTS_APPROVED = 'PERSONAL_ADDITIONAL_WAYPOINTS_APPROVED',
  PERSONAL_ADDITIONAL_WAYPOINTS_DECLINED = 'PERSONAL_ADDITIONAL_WAYPOINTS_DECLINED',
  PERSONAL_TRIP_START_REMIND = 'PERSONAL_TRIP_START_REMIND',
  PERSONAL_TRIP_IN_PROGRESS = 'PERSONAL_TRIP_IN_PROGRESS',
  PERSONAL_WAYPOINT_ARRIVED = 'PERSONAL_WAYPOINT_ARRIVED',
  PERSONAL_AWAITING_TRIP_APPROVAL = 'PERSONAL_AWAITING_TRIP_APPROVAL',
  PERSONAL_AWAITING_TRIP_APPROVED = 'PERSONAL_AWAITING_TRIP_APPROVED',
  PERSONAL_AWAITING_TRIP_DECLINED = 'PERSONAL_AWAITING_TRIP_DECLINED',
  PERSONAL_ORDER_PAYMENT_FORMATION = 'PERSONAL_ORDER_PAYMENT_FORMATION',
  PERSONAL_PAYMENT_AWAITING = 'PERSONAL_PAYMENT_AWAITING',
  PERSONAL_PAYMENT_DONE = 'PERSONAL_PAYMENT_DONE',
  PERSONAL_PAYMENT_DECLINED = 'PERSONAL_PAYMENT_DECLINED',
  PERSONAL_CANCELLED = 'PERSONAL_CANCELLED',
  PERSONAL_TRIP_FINISHED = 'PERSONAL_TRIP_FINISHED',
}

export enum PublicStatuses {
  PUBLIC_AWAITING_APPROVAL = 'PUBLIC_AWAITING_APPROVAL',
  PUBLIC_TRIP_CONFIRMATION = 'PUBLIC_TRIP_CONFIRMATION',
  PUBLIC_AWAITING_AFFIRMATIVE = 'PUBLIC_AWAITING_AFFIRMATIVE',
  PUBLIC_ORDER_PAYMENT_FORMATION = 'PUBLIC_ORDER_PAYMENT_FORMATION',
  PUBLIC_PAYMENT_AWAITING = 'PUBLIC_PAYMENT_AWAITING',
  PUBLIC_PAYMENT_DONE = 'PUBLIC_PAYMENT_DONE',
  PUBLIC_PAYMENT_NOT_DONE = 'PUBLIC_PAYMENT_NOT_DONE',
  PUBLIC_CANCELLED = 'PUBLIC_CANCELLED',
}

export enum TaxiStatuses {
  TAXI_AWAITING_APPROVAL = 'TAXI_AWAITING_APPROVAL',
  TAXI_APPROVED = 'TAXI_APPROVED',
  TAXI_AWAITING_SEARCH = 'TAXI_AWAITING_SEARCH',
  TAXI_DRIVER_SEARCH = 'TAXI_DRIVER_SEARCH',
  TAXI_DRIVER_FOUND = 'TAXI_DRIVER_FOUND',
  TAXI_DRIVER_ON_THE_WAY = 'TAXI_DRIVER_ON_THE_WAY',
  TAXI_DRIVER_ARRIVED = 'TAXI_DRIVER_ARRIVED',
  TAXI_TRIP_IN_PROGRESS = 'TAXI_TRIP_IN_PROGRESS',
  TAXI_TRIP_FINISHED = 'TAXI_TRIP_FINISHED',
  TAXI_CANCELLED = 'TAXI_CANCELLED',
}

export const CarsharingStatusNames: Record<CarsharingStatuses, string> = {
  [CarsharingStatuses.CARSHARING_AWAITING_APPROVAL]: 'На согласовании',
  [CarsharingStatuses.CARSHARING_APPROVED]: 'Согласована',
  [CarsharingStatuses.CARSHARING_DECLINED]: 'Не согласована',
  [CarsharingStatuses.CARSHARING_AWAITING_SEARCH]: 'Поездка запланирована',
  [CarsharingStatuses.CARSHARING_TRIP_IN_PROGRESS]: 'Поездка началась',
  [CarsharingStatuses.CARSHARING_AWAITING_TRIP_APPROVAL]: 'Утверждение маршрута',
  [CarsharingStatuses.CARSHARING_TRIP_FINISHED]: 'Поездка завершена',
  [CarsharingStatuses.CARSHARING_CANCELLED]: 'Отменено',
};

export const GroupTransferStatusNames: Record<GroupTransferStatuses, string> = {
  [GroupTransferStatuses.GROUP_TRANSFER_AWAITING_APPROVAL]: 'На согласовании',
  [GroupTransferStatuses.GROUP_TRANSFER_APPROVED]: 'Согласована',
  [GroupTransferStatuses.GROUP_TRANSFER_AWAITING_SEARCH]: 'Ожидайте назначения водителя',
  [GroupTransferStatuses.GROUP_TRANSFER_DRIVER_SEARCH]: 'Поиск водителя',
  [GroupTransferStatuses.GROUP_TRANSFER_DRIVER_FOUND]: 'Водитель назначен',
  [GroupTransferStatuses.GROUP_TRANSFER_DRIVER_ON_THE_WAY]: 'Водитель в пути',
  [GroupTransferStatuses.GROUP_TRANSFER_DRIVER_ARRIVED]: 'Водитель ожидает в точке отправления',
  [GroupTransferStatuses.GROUP_TRANSFER_TRIP_IN_PROGRESS]: 'Поездка началась',
  [GroupTransferStatuses.GROUP_TRANSFER_TRIP_FINISHED]: 'Поездка завершена',
  [GroupTransferStatuses.GROUP_TRANSFER_CANCELLED]: 'Отменено',
};

export const PersonalStatusNames: Record<PersonalStatuses, string> = {
  [PersonalStatuses.PERSONAL_AWAITING_APPROVAL]: 'На согласовании',
  [PersonalStatuses.PERSONAL_APPROVED]: 'Согласована',
  [PersonalStatuses.PERSONAL_AWAITING_SHARED_RIDE_APPROVAL]: 'Согласование присоединения к СП',
  [PersonalStatuses.PERSONAL_SHARED_RIDE_DECLINED]: 'Присоединение не согласовано',
  [PersonalStatuses.PERSONAL_SHARED_RIDE_APPROVED]: 'Присоединение согласовано',
  [PersonalStatuses.PERSONAL_ADDITIONAL_WAYPOINTS_APPROVAL]: 'Согласование доп. точек в маршруте на личном транспорте',
  [PersonalStatuses.PERSONAL_ADDITIONAL_WAYPOINTS_APPROVED]: 'Добавление дополнительных точек в маршрут было согласовано',
  [PersonalStatuses.PERSONAL_ADDITIONAL_WAYPOINTS_DECLINED]: 'Добавление дополнительных точек в маршрут было отклонено',
  [PersonalStatuses.PERSONAL_TRIP_START_REMIND]: 'Напоминание о начале поездки',
  [PersonalStatuses.PERSONAL_TRIP_IN_PROGRESS]: 'Поездка',
  [PersonalStatuses.PERSONAL_WAYPOINT_ARRIVED]: 'Прибытие в промежуточный пункт на личном транспорте',
  [PersonalStatuses.PERSONAL_AWAITING_TRIP_APPROVAL]: 'Утверждение маршрута',
  [PersonalStatuses.PERSONAL_AWAITING_TRIP_APPROVED]: 'Утверждено',
  [PersonalStatuses.PERSONAL_AWAITING_TRIP_DECLINED]: 'Не утверждено',
  [PersonalStatuses.PERSONAL_ORDER_PAYMENT_FORMATION]: 'Формирование приказа на выплату',
  [PersonalStatuses.PERSONAL_PAYMENT_AWAITING]: 'Ожидание выплаты',
  [PersonalStatuses.PERSONAL_PAYMENT_DONE]: 'Выплата произведена',
  [PersonalStatuses.PERSONAL_PAYMENT_DECLINED]: 'Выплата не произведена',
  [PersonalStatuses.PERSONAL_CANCELLED]: 'Отменено',
  [PersonalStatuses.PERSONAL_TRIP_FINISHED]: 'Поездка завершена',
};

export const PublicStatusNames: Record<PublicStatuses, string> = {
  [PublicStatuses.PUBLIC_AWAITING_APPROVAL]: 'На согласовании',
  [PublicStatuses.PUBLIC_TRIP_CONFIRMATION]: 'Подтверждение поездки',
  [PublicStatuses.PUBLIC_AWAITING_AFFIRMATIVE]: 'Утверждение',
  [PublicStatuses.PUBLIC_ORDER_PAYMENT_FORMATION]: 'Формирование приказа на выплату',
  [PublicStatuses.PUBLIC_PAYMENT_AWAITING]: 'Ожидание выплаты',
  [PublicStatuses.PUBLIC_PAYMENT_DONE]: 'Выплата произведена',
  [PublicStatuses.PUBLIC_PAYMENT_NOT_DONE]: 'Выплата не произведена',
  [PublicStatuses.PUBLIC_CANCELLED]: 'Отменено',
};

export const TaxiStatusNames: Record<TaxiStatuses, string> = {
  [TaxiStatuses.TAXI_AWAITING_APPROVAL]: 'На согласовании',
  [TaxiStatuses.TAXI_APPROVED]: 'Согласована',
  [TaxiStatuses.TAXI_AWAITING_SEARCH]: 'Ожидайте назначения водителя',
  [TaxiStatuses.TAXI_DRIVER_SEARCH]: 'Поиск водителя',
  [TaxiStatuses.TAXI_DRIVER_FOUND]: 'Водитель назначен',
  [TaxiStatuses.TAXI_DRIVER_ON_THE_WAY]: 'Водитель в пути',
  [TaxiStatuses.TAXI_DRIVER_ARRIVED]: 'Водитель ожидает в точке отправления',
  [TaxiStatuses.TAXI_TRIP_IN_PROGRESS]: 'Поездка началась',
  [TaxiStatuses.TAXI_TRIP_FINISHED]: 'Поездка завершена',
  [TaxiStatuses.TAXI_CANCELLED]: 'Отменено',
};

export const StatusNames = {
  TAXI: TaxiStatusNames,
  PUBLIC: PublicStatusNames,
  PERSONAL: PersonalStatusNames,
  GROUP_TRANSFER: GroupTransferStatusNames,
  CARSHARING: CarsharingStatusNames,
} as const;

type TStatuses =
  | TaxiStatuses
  | CarsharingStatuses
  | GroupTransferStatuses
  | PublicStatuses
  | PersonalStatuses;

export const CanceledStatuses: TStatuses[] = [
  CarsharingStatuses.CARSHARING_CANCELLED,
  CarsharingStatuses.CARSHARING_DECLINED,
  GroupTransferStatuses.GROUP_TRANSFER_CANCELLED,
  TaxiStatuses.TAXI_CANCELLED,
  PublicStatuses.PUBLIC_CANCELLED,
  PublicStatuses.PUBLIC_PAYMENT_NOT_DONE,
  PersonalStatuses.PERSONAL_CANCELLED,
  PersonalStatuses.PERSONAL_SHARED_RIDE_DECLINED,
  PersonalStatuses.PERSONAL_ADDITIONAL_WAYPOINTS_DECLINED,
  PersonalStatuses.PERSONAL_AWAITING_TRIP_DECLINED,
  PersonalStatuses.PERSONAL_PAYMENT_DECLINED,
];

export const FinishedStatuses: TStatuses[] = [
  CarsharingStatuses.CARSHARING_TRIP_FINISHED,
  GroupTransferStatuses.GROUP_TRANSFER_TRIP_FINISHED,
  TaxiStatuses.TAXI_TRIP_FINISHED,
  PublicStatuses.PUBLIC_PAYMENT_DONE,
  PersonalStatuses.PERSONAL_PAYMENT_DONE,
  PersonalStatuses.PERSONAL_TRIP_FINISHED,
];

export const CarsharingStatusesOrder = [
  CarsharingStatuses.CARSHARING_AWAITING_APPROVAL,
  CarsharingStatuses.CARSHARING_APPROVED,
  CarsharingStatuses.CARSHARING_AWAITING_SEARCH,
  CarsharingStatuses.CARSHARING_TRIP_IN_PROGRESS,
  CarsharingStatuses.CARSHARING_AWAITING_TRIP_APPROVAL,
  CarsharingStatuses.CARSHARING_TRIP_FINISHED,
] as const;

export const GroupTransferStatusesOrder = [
  GroupTransferStatuses.GROUP_TRANSFER_AWAITING_APPROVAL,
  GroupTransferStatuses.GROUP_TRANSFER_APPROVED,
  GroupTransferStatuses.GROUP_TRANSFER_AWAITING_SEARCH,
  GroupTransferStatuses.GROUP_TRANSFER_DRIVER_SEARCH,
  GroupTransferStatuses.GROUP_TRANSFER_DRIVER_FOUND,
  GroupTransferStatuses.GROUP_TRANSFER_DRIVER_ON_THE_WAY,
  GroupTransferStatuses.GROUP_TRANSFER_DRIVER_ARRIVED,
  GroupTransferStatuses.GROUP_TRANSFER_TRIP_IN_PROGRESS,
  GroupTransferStatuses.GROUP_TRANSFER_TRIP_FINISHED,
] as const;

export const TaxiStatusesOrder = [
  TaxiStatuses.TAXI_AWAITING_APPROVAL,
  TaxiStatuses.TAXI_APPROVED,
  TaxiStatuses.TAXI_AWAITING_SEARCH,
  TaxiStatuses.TAXI_DRIVER_SEARCH,
  TaxiStatuses.TAXI_DRIVER_FOUND,
  TaxiStatuses.TAXI_DRIVER_ON_THE_WAY,
  TaxiStatuses.TAXI_DRIVER_ARRIVED,
  TaxiStatuses.TAXI_TRIP_IN_PROGRESS,
  TaxiStatuses.TAXI_TRIP_FINISHED,
] as const;

export const PublicStatusesOrder = [
  PublicStatuses.PUBLIC_AWAITING_APPROVAL,
  PublicStatuses.PUBLIC_TRIP_CONFIRMATION,
  PublicStatuses.PUBLIC_AWAITING_AFFIRMATIVE,
  PublicStatuses.PUBLIC_ORDER_PAYMENT_FORMATION,
  PublicStatuses.PUBLIC_PAYMENT_AWAITING,
  PublicStatuses.PUBLIC_PAYMENT_DONE,
] as const;

export const PersonalStatusesOrder = [
  PersonalStatuses.PERSONAL_AWAITING_APPROVAL,
  PersonalStatuses.PERSONAL_APPROVED,
  PersonalStatuses.PERSONAL_TRIP_IN_PROGRESS,
  PersonalStatuses.PERSONAL_AWAITING_TRIP_APPROVAL,
  PersonalStatuses.PERSONAL_ORDER_PAYMENT_FORMATION,
  PersonalStatuses.PERSONAL_PAYMENT_AWAITING,
  PersonalStatuses.PERSONAL_PAYMENT_DONE,
  PersonalStatuses.PERSONAL_TRIP_FINISHED,
] as const;

export const PersonalSharedStatusesOrder = [
  PersonalStatuses.PERSONAL_AWAITING_APPROVAL,
  PersonalStatuses.PERSONAL_APPROVED,
  PersonalStatuses.PERSONAL_AWAITING_SHARED_RIDE_APPROVAL,
  PersonalStatuses.PERSONAL_TRIP_IN_PROGRESS,
  PersonalStatuses.PERSONAL_TRIP_FINISHED,
] as const;

export const StatusesOrder = {
  TAXI: TaxiStatusesOrder,
  PUBLIC: PublicStatusesOrder,
  GROUP_TRANSFER: GroupTransferStatusesOrder,
  CARSHARING: CarsharingStatusesOrder,
} as const;

