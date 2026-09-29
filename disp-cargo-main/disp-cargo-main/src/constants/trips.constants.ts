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

export enum CheckinType {
  MANUAL = 'MANUAL',
  AUTO = 'AUTO',
}

export enum DeadlineState {
  NONE = 'NONE',
  YELLOW = 'YELLOW',
  RED = 'RED',
}
