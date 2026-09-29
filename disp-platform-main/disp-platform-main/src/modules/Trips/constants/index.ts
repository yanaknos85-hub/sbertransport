import { TRIP_STATUSES } from 'constants/trips.constants';

export const AVAILABLE_PASSENGER_STATUSES: TRIP_STATUSES[] = [
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
  HumanReadableId = 'humanReadableId',
  Requests = 'requests',
  Status = 'status',
  StartTime = 'expectedStartTime',
  ExpectedTime = 'expectedTime',
  Vehicle = 'vehicle',
  Driver = 'driver',
  Passenger = 'passenger',
  AddressFrom = 'addressFrom',
  AddressTo = 'addressTo',
  IntermediateAddresses = 'intermediateAddresses',
  CommentForDriver = 'commentForDriver',
  PassengerCount = 'passengerCount',
  ExpectedDistance = 'expectedDistance',
  FactDistance = 'factDistance',
  ExpectedCost = 'expectedCost',
  DriverWaitingTime = 'driverWaitingTime',
  TaxiClass = 'taxiClass',
  Dispatcher = 'dispatcher',
  FlightNumber = 'flightNumber',
  AdditionalContact = 'additionalContact',
  HotelNumber = 'hotelNumber',
  ChildSeat = 'childSeat',
  AnimalTransport = 'animalTransport',
  OversizedLuggage = 'oversizedLuggage',
}

export const staticColumns = [Columns.Icon, Columns.Number];

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

/* Продолжительность поездки для быстрого фильтра */
export const EXPECTED_TIME = 6;
