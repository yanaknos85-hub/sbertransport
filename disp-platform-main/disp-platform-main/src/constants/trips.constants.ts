export enum TRIP_STATUSES {
  SENT_TO_CONTRACTOR = 'SENT_TO_CONTRACTOR',
  WAITING_FOR_ASSIGNMENT = 'WAITING_FOR_ASSIGNMENT',
  DRIVER_ASSIGNED = 'DRIVER_ASSIGNED',
  DRIVER_ON_THE_WAY = 'DRIVER_ON_THE_WAY',
  DRIVER_ARRIVED = 'DRIVER_ARRIVED',
  TRIP_IN_PROGRESS = 'TRIP_IN_PROGRESS',
  INTERMEDIATE_WAYPOINT_ARRIVED = 'INTERMEDIATE_WAYPOINT_ARRIVED',
  ORDER_FINISHED = 'ORDER_FINISHED',
  ORDER_CANCELLED_BY_CLIENT = 'ORDER_CANCELLED_BY_CLIENT',
  ORDER_CANCELLED_BY_DRIVER = 'ORDER_CANCELLED_BY_DRIVER',
  ORDER_EXPIRED = 'ORDER_EXPIRED',
  UNDEFINED = 'UNDEFINED',
}

export const ALLOWED_NEXT_TRIP_STATUSES: Record<string, TRIP_STATUSES[]> = {
  [TRIP_STATUSES.SENT_TO_CONTRACTOR]: [
    TRIP_STATUSES.WAITING_FOR_ASSIGNMENT,
    TRIP_STATUSES.ORDER_CANCELLED_BY_CLIENT,
    TRIP_STATUSES.ORDER_CANCELLED_BY_DRIVER,
    TRIP_STATUSES.ORDER_EXPIRED,
  ],
  [TRIP_STATUSES.WAITING_FOR_ASSIGNMENT]: [
    TRIP_STATUSES.DRIVER_ASSIGNED,
    TRIP_STATUSES.ORDER_CANCELLED_BY_CLIENT,
    TRIP_STATUSES.ORDER_CANCELLED_BY_DRIVER,
    TRIP_STATUSES.ORDER_EXPIRED,
  ],
  [TRIP_STATUSES.DRIVER_ASSIGNED]: [
    TRIP_STATUSES.DRIVER_ON_THE_WAY,
    TRIP_STATUSES.ORDER_CANCELLED_BY_CLIENT,
    TRIP_STATUSES.ORDER_CANCELLED_BY_DRIVER,
    TRIP_STATUSES.ORDER_EXPIRED,
  ],
  [TRIP_STATUSES.DRIVER_ON_THE_WAY]: [
    TRIP_STATUSES.DRIVER_ARRIVED,
    TRIP_STATUSES.ORDER_CANCELLED_BY_CLIENT,
    TRIP_STATUSES.ORDER_CANCELLED_BY_DRIVER,
  ],
  [TRIP_STATUSES.DRIVER_ARRIVED]: [
    TRIP_STATUSES.TRIP_IN_PROGRESS,
    TRIP_STATUSES.ORDER_CANCELLED_BY_CLIENT,
    TRIP_STATUSES.ORDER_CANCELLED_BY_DRIVER,
  ],
  [TRIP_STATUSES.TRIP_IN_PROGRESS]: [
    TRIP_STATUSES.INTERMEDIATE_WAYPOINT_ARRIVED,
    TRIP_STATUSES.ORDER_FINISHED,
  ],
  [TRIP_STATUSES.INTERMEDIATE_WAYPOINT_ARRIVED]: [
    TRIP_STATUSES.TRIP_IN_PROGRESS,
  ],
};

type TRIP_STATUSES_INFO = {
  [key in TRIP_STATUSES]: {
    title: string;
    cargoTitle: string;
    isFinal: boolean;
    isCanceled: boolean;
    isActive: boolean;
    isEditable: boolean;
    isDistrib: boolean;
    isNotDistrib: boolean;
  };
};

export const TripStatuses: TRIP_STATUSES_INFO = {
  [TRIP_STATUSES.SENT_TO_CONTRACTOR]: {
    title: 'Опубликовано в системе исполнителя',
    cargoTitle: 'Опубликовано в системе исполнителя',
    isFinal: false,
    isCanceled: false,
    isActive: true,
    isEditable: true,
    isDistrib: false,
    isNotDistrib: true,
  },
  [TRIP_STATUSES.WAITING_FOR_ASSIGNMENT]: {
    title: 'Ожидает назначения',
    cargoTitle: 'Ожидает назначения',
    isFinal: false,
    isCanceled: false,
    isActive: true,
    isEditable: true,
    isDistrib: false,
    isNotDistrib: true,
  },
  [TRIP_STATUSES.DRIVER_ASSIGNED]: {
    title: 'Закреплен за водителем',
    cargoTitle: 'Закреплен за водителем',
    isFinal: false,
    isCanceled: false,
    isActive: true,
    isEditable: true,
    isDistrib: true,
    isNotDistrib: false,
  },
  [TRIP_STATUSES.DRIVER_ON_THE_WAY]: {
    title: 'Водитель выехал',
    cargoTitle: 'Водитель выехал',
    isFinal: false,
    isCanceled: false,
    isActive: true,
    isEditable: true,
    isDistrib: true,
    isNotDistrib: false,
  },
  [TRIP_STATUSES.DRIVER_ARRIVED]: {
    title: 'Водитель ожидает клиента',
    cargoTitle: 'Водитель прибыл на погрузку',
    isFinal: false,
    isCanceled: false,
    isActive: true,
    isEditable: true,
    isDistrib: true,
    isNotDistrib: false,
  },
  [TRIP_STATUSES.TRIP_IN_PROGRESS]: {
    title: 'Водитель везет клиента',
    cargoTitle: 'Водитель везет груз',
    isFinal: false,
    isCanceled: false,
    isActive: true,
    isEditable: true,
    isDistrib: true,
    isNotDistrib: false,
  },
  [TRIP_STATUSES.INTERMEDIATE_WAYPOINT_ARRIVED]: {
    title: 'Водитель на промежуточной точке',
    cargoTitle: 'Водитель на промежуточной точке',
    isFinal: false,
    isCanceled: false,
    isActive: true,
    isEditable: false,
    isDistrib: true,
    isNotDistrib: false,
  },
  [TRIP_STATUSES.ORDER_FINISHED]: {
    title: 'Заказ выполнен',
    cargoTitle: 'Заказ выполнен',
    isFinal: true,
    isCanceled: false,
    isActive: false,
    isEditable: true,
    isDistrib: false,
    isNotDistrib: false,
  },
  [TRIP_STATUSES.ORDER_CANCELLED_BY_CLIENT]: {
    title: 'Заказ отменен клиентом',
    cargoTitle: 'Заказ отменен клиентом',
    isFinal: true,
    isCanceled: true,
    isActive: false,
    isEditable: true,
    isDistrib: false,
    isNotDistrib: false,
  },
  [TRIP_STATUSES.ORDER_CANCELLED_BY_DRIVER]: {
    title: 'Заказ отменен водителем',
    cargoTitle: 'Заказ отменен водителем',
    isFinal: true,
    isCanceled: true,
    isActive: false,
    isEditable: true,
    isDistrib: false,
    isNotDistrib: false,
  },
  [TRIP_STATUSES.ORDER_EXPIRED]: {
    title: 'Заказ просрочен',
    cargoTitle: 'Заказ просрочен',
    isFinal: false,
    isCanceled: false,
    isActive: false,
    isEditable: true,
    isDistrib: false,
    isNotDistrib: false,
  },
  [TRIP_STATUSES.UNDEFINED]: {
    title: 'Не определено',
    cargoTitle: 'Не определено',
    isFinal: false,
    isCanceled: false,
    isActive: false,
    isEditable: false,
    isDistrib: false,
    isNotDistrib: false,
  },
};

export const tripStatuses = Object.values(TRIP_STATUSES);
export const activeTripStatuses = Object.values(TRIP_STATUSES).filter(status => TripStatuses[status].isActive);
export const distribTripStatuses = Object.values(TRIP_STATUSES).filter(status => TripStatuses[status].isDistrib);
export const notDistribTripStatuses = Object.values(TRIP_STATUSES).filter(status => TripStatuses[status].isNotDistrib);
export const finalTripStatuses = Object.values(TRIP_STATUSES).filter(status => TripStatuses[status].isFinal);

export enum CheckinType {
  MANUAL = 'MANUAL',
  AUTO = 'AUTO',
}

export enum GroupTransferClass {
  TRANSFER = 'TRANSFER',
}

export const GroupTransferClassTitles: Record<GroupTransferClass, string> = {
  [GroupTransferClass.TRANSFER]: 'Трансфер',
};

export enum ChildSeatTypes {
  NewBorn = 'newborn',
  Group1 = 'group1',
  Group2 = 'group2',
  Booster = 'booster',
}

export const ChildSeatTitles: Record<ChildSeatTypes, string> = {
  [ChildSeatTypes.NewBorn]: 'Люлька до 1 года',
  [ChildSeatTypes.Group1]: 'Кресло от 9 мес. до 4 лет',
  [ChildSeatTypes.Group2]: 'Кресло 3-7 лет',
  [ChildSeatTypes.Booster]: 'Бустер 6-12 лет',
};

export enum ServiceType {
  TAXI = 'TAXI',
  TRANSFER = 'TRANSFER',
}

export enum TaxiClass {
  ECONOMY = 'ECONOMY',
  COMFORT = 'COMFORT',
  COMFORT_PLUS = 'COMFORT_PLUS',
  BUSINESS = 'BUSINESS',
  VIP_BUS = 'VIP_BUS',
  SMALL_BUS = 'SMALL_BUS',
  MIDDLE_BUS = 'MIDDLE_BUS',
  LARGE_BUS = 'LARGE_BUS',
  OFFICIAL = 'OFFICIAL',
  GROUP_TRANSFER = 'GROUP_TRANSFER',
}

export enum TAXI_REQUEST_STATUSES {
  TAXI_AWAITING_APPROVAL = 'TAXI_AWAITING_APPROVAL',
  TAXI_CANCELLED = 'TAXI_CANCELLED',
  TAXI_APPROVED = 'TAXI_APPROVED',
  TAXI_AWAITING_SEARCH = 'TAXI_AWAITING_SEARCH',
  TAXI_DRIVER_SEARCH = 'TAXI_DRIVER_SEARCH',
  TAXI_DRIVER_FOUND = 'TAXI_DRIVER_FOUND',
  TAXI_DRIVER_ON_THE_WAY = 'TAXI_DRIVER_ON_THE_WAY',
  TAXI_DRIVER_ARRIVED = 'TAXI_DRIVER_ARRIVED',
  TAXI_FREE_TIME_EXPIRED = 'TAXI_FREE_TIME_EXPIRED',
  TAXI_WAYPOINT_ARRIVED = 'TAXI_WAYPOINT_ARRIVED',
  TAXI_TRIP_IN_PROGRESS = 'TAXI_TRIP_IN_PROGRESS',
  TAXI_TRIP_FINISHED = 'TAXI_TRIP_FINISHED',
}

export const TaxiClassDescriptions: { [key in TaxiClass]: string } = {
  ECONOMY: 'Эконом',
  COMFORT: 'Комфорт',
  COMFORT_PLUS: 'Комфорт+',
  BUSINESS: 'Бизнес',
  VIP_BUS: 'VIP атобус',
  SMALL_BUS: 'Автобус от 10 до 21 места',
  MIDDLE_BUS: 'Автобус от 22 до 41 места',
  LARGE_BUS: 'Автобус от 42 до 55 места',
  OFFICIAL: 'Служебный',
  GROUP_TRANSFER: 'Трансфер',
};

