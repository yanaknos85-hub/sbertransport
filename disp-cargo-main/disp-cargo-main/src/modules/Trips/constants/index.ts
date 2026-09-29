import { TRIP_STATUSES } from 'constants/trips.constants';

export const AVAILABLE_CARGO_STATUSES: TRIP_STATUSES[] = [
  TRIP_STATUSES.SENT_TO_CONTRACTOR,
  TRIP_STATUSES.WAITING_FOR_ASSIGNMENT,
  TRIP_STATUSES.DRIVER_ASSIGNED,
  TRIP_STATUSES.DRIVER_ON_THE_WAY,
  TRIP_STATUSES.DRIVER_ARRIVED,
  TRIP_STATUSES.TRIP_IN_PROGRESS,
  TRIP_STATUSES.INTERMEDIATE_WAYPOINT_ARRIVED,
  TRIP_STATUSES.ORDER_FINISHED,
  TRIP_STATUSES.ORDER_CANCELLED_BY_CLIENT,
  TRIP_STATUSES.ORDER_CANCELLED_BY_DRIVER,
  TRIP_STATUSES.ORDER_EXPIRED,
];

export enum Columns {
  Icon = 'icon',
  Number = 'number',
  RouteHumanReadableId = 'routeHumanReadableId',
  HumanReadableId = 'humanReadableId',
  Requests = 'requests',
  Status = 'status',
  StartTime = 'startTime',
  Driver = 'driver',
  Sender = 'sender',
  SenderContact = 'senderContact',
  AddressFrom = 'addressFrom',
  Recipient = 'recipient',
  RecipientContact = 'recipientContact',
  AddressTo = 'addressTo',
  IntermediateAddresses = 'intermediateAddresses',
  CargoName = 'cargoName',
  CargoWeight = 'cargoWeight',
  CargoVolume = 'cargoVolume',
  CommentForDriver = 'commentForDriver',
  Places = 'places',
  Capacity = 'capacity',
  Loaders = 'loaders',
  ExpectedDistance = 'expectedDistance',
  FactDistance = 'factDistance',
  LoadersWorkTime = 'loadersWorkTime',
  ExpectedCost = 'expectedCost',
  Dispatcher = 'dispatcher',
  DriverWaitingTime = 'driverWaitingTime',
}

export const columnToSortMap: Record<string, string> = {
  [Columns.StartTime]: 'START_TIME',
};

export enum TripStatisticStatuses {
  TotalCount = 'totalCount',
  AssignCount = 'assignCount',
  NotAssignCount = 'notAssignCount',
}

export enum TripStatisticStatusesTitles {
  totalCount = 'Активные:',
  assignCount = 'Распределено:',
  notAssignCount = 'Не распределено:',
}

