import {
  CheckinType,
  GroupTransferClass, TaxiClass, TransportTypes, TRIP_STATUSES, TripTypes, TypeOfAction
} from 'constants/trips.constants';
import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { createPagination } from 'utils/io-ts/pagination';
import { ioTypeFromEnum } from 'utils/io-ts/ioTypeFromEnum';

/** Данные для вывода себя на линию/с линии */
export interface DriverStatusData {
  state: boolean;
}

export interface PageSetting {
  page: number;
  size: number;
}

/** Фильтры для получения списка поездок */
export interface DriverTripsFilters extends Partial<PageSetting> {
  statuses?: TRIP_STATUSES[];
}

/** Данные для получения списка поездок */
export interface DriverTripsData extends DriverTripsFilters {
  driverId: tt.UUID;
  contractorId: tt.UUID;
}

/** Краткая информация о пассажире */
export const EmployeeShort = t.partial({
  patronymic: tt.nullable(t.string),
  lastName: tt.nullable(t.string),
  firstName: tt.nullable(t.string),
  humanReadableId: tt.nullable(t.string),
  mobilePhone: tt.nullable(t.string),
  organization: tt.nullable(t.string),
  email: tt.nullable(t.string),
});
export type EmployeeShort = t.TypeOf<typeof EmployeeShort>;

/** Краткая информация о водителе */
export const DriverShort = t.intersection([
  EmployeeShort,
  t.type({
    rating: t.number,
    id: tt.uuid,
    humanReadableId: t.string,
  }),
  t.partial({
    contactPhone: tt.nullable(t.string),
    shiftId: tt.nullable(tt.uuid),
  }),
]);
export type DriverShort = t.TypeOf<typeof DriverShort>;

/** Контакт на точке маршрута */
export const WaypointContact = t.type({
  name: tt.nullable(t.string),
  phone: tt.nullable(t.string),
});
export type WaypointContact = t.TypeOf<typeof WaypointContact>;

/** Пассажир на точке */
export const WaypointPassenger = t.partial({
  patronymic: t.string,
  lastName: t.string,
  firstName: t.string,
  type: ioTypeFromEnum<TypeOfAction>('TypeOfAction', TypeOfAction),
  phone: tt.nullable(t.string),
});
export type WaypointPassenger = t.TypeOf<typeof WaypointPassenger>;

/** Точка маршрута */
export const Waypoint = t.intersection([
  t.type({
    latitude: t.number,
    longitude: t.number,
    index: t.number,
  }),
  t.partial({
    id: tt.uuid,
    country: t.string,
    street: t.string,
    house: t.string,
    building: t.string,
    structure: t.string,
    existInVspGosbTbRegistry: t.boolean,
    region: t.string,
    city: t.string,
    waitingTime: t.string,
    contact: WaypointContact,
    fullAddress: t.string,
    passengers: t.array(WaypointPassenger),
  }),
]);
export type Waypoint = t.TypeOf<typeof Waypoint>;

/** Краткая информация об авто */
export const VehicleShort = t.intersection([
  t.type({
    id: tt.uuid,
    stateNumber: t.string,
    brand: t.string,
  }),
  t.partial({
    model: tt.nullable(t.string),
    color: tt.nullable(t.string),
    vehicleType: ioTypeFromEnum<TripTypes>('TripTypes', TripTypes),
  }),
]);
export type VehicleShort = t.TypeOf<typeof VehicleShort>;

/** Краткая информация о диспетчере */
export const DispatcherShort = t.intersection([
  t.type({
    lastName: t.string,
    firstName: t.string,
  }),
  t.partial({
    phone: tt.nullable(t.string),
    patronymic: tt.nullable(t.string),
  }),
]);
export type DispatcherShort = t.TypeOf<typeof DispatcherShort>;

/** Данные о поездке */
export const RequestData = t.intersection([
  t.type({
    distance: t.number,
    cost: t.number,
  }),
  t.partial({
    time: tt.nullable(t.number),
  }),
]);
export type RequestData = t.TypeOf<typeof RequestData>;

/** Пассажирский тариф */
export const PassTariff = t.partial({
  transport_type: t.string, // Пока в диспетчерской другого не предусмотрено
  id: tt.uuid,
  humanReadableId: t.string,
  serviceType: t.string,
  organizationId: tt.uuid,
  regionId: tt.uuid,
  region: t.string,
  active: t.boolean,
  contractId: t.string,
  contractorId: tt.uuid,
  taxiClass: ioTypeFromEnum<TaxiClass>('TaxiClass', TaxiClass),
  rideCostPerKm: t.number,
  distanceIncluded: t.number,
  minRideDistanceCost: t.number,
  rideCostPerMin: t.number,
  timeIncluded: t.number,
  minRideTimeCost: t.number,
  waitCostPerMin: t.number,
  waitCostPerMinIntermediate: t.number,
  freeWaitingTime: t.number,
  carServiceCost: t.number,
  timedTariffParams: t.type({
    coefWorkDayMorning: t.number,
    coefWorkDayNoon: t.number,
    coefWorkDayEvening: t.number,
    coefWorkDayNight: t.number,
    coefDayOff: t.number,
  }),
  coopTariffParams: t.type({
    savingsDeviationPct: t.number,
    distanceDeviationKm: t.number,
    timeDeviationMin: t.number,
    minCancelTimeMin: t.number,
  }),
  suburbTariffParams: t.type({
    costPerKmSuburb: t.number,
    costPerMinSuburb: t.number,
    suburbServiceCostPerKm: t.number,
    suburbServiceCostPerMin: t.number,
    costPerKmInterRegion: t.number,
    costPerMinInterRegion: t.number,
  }),
  contractorDeviationParams: t.type({
    maxDiffComputedDistancePercent: t.number,
    maxDiffFactDistancePercent: t.number,
    maxDiffComputedCostPercent: t.number,
    maxDiffContractorCostPercent: t.number,
    maxDiffComputedWaitingPercent: t.number,
  }),
  coefTraffic: t.number,
  coefChildSeat: t.number,
  coefPetTransport: t.number,
  coefBicycle: t.number,
  coefOrg: t.number,
  workGroup: t.string,
  triggerTime: t.number,
  isNightTariff: t.boolean,
});
export type PassTariff = t.TypeOf<typeof PassTariff>;

/** Доп информация по трансферу */
export const TransferInformation = t.partial({
  childSeat: tt.nullable(t.boolean),
  childSeatDetails: tt.nullable(t.type({
    group1: t.number,
    group2: t.number,
    booster: t.number,
    newborn: t.number,
  })),
  bugsOversized: tt.nullable(t.boolean),
  bugsOversizedComment: tt.nullable(t.string),
  bugs: tt.nullable(t.boolean),
  bugsComment: tt.nullable(t.string),
  animal: tt.nullable(t.boolean),
  animalComment: tt.nullable(t.string),
  phoneHotel: tt.nullable(t.string),
  numberFlight: tt.nullable(t.string),
  dateFlight: tt.nullable(t.string),
  addContactFIO: tt.nullable(t.string),
  addContactPhone: tt.nullable(t.string),
  typeVehicle: tt.nullable(t.string),
});
export type TransferInformation = t.TypeOf<typeof TransferInformation>;

/** Пассажирская заявка */
export const PassRequest = t.intersection([
  t.type({
    id: tt.uuid,
    passengerCount: t.number,
    /** Признак сп */
    coop: t.boolean,
    sharedRideOwner: t.boolean,
    transportType: tt.nullable(ioTypeFromEnum<TransportTypes>('TransportTypes', TransportTypes)),
  }),
  t.partial({
    humanReadableId: tt.nullable(t.string),
    author: tt.nullable(EmployeeShort),
    passenger: tt.nullable(EmployeeShort),
    expected: tt.nullable(RequestData),
    requestOptions: tt.nullable(t.array(t.string)),
    tariff: tt.nullable(PassTariff),
    waypoints: tt.nullable(t.array(Waypoint)),
    commentForDriver: tt.nullable(t.string),
    comment: tt.nullable(t.string),
    finishedTime: tt.nullable(t.string),
    status: tt.nullable(t.string),
    timeZone: tt.nullable(t.string),
    /** Признак межгорода */
    suburb: tt.nullable(t.boolean),
    creationTime: tt.nullable(t.string),

    // =========== Трансфер ===============
    update_time: tt.nullable(t.string),
    update_user: tt.nullable(tt.uuid),
    min_order_time: tt.nullable(t.number),
    groupTransferClass: tt.nullable(ioTypeFromEnum<GroupTransferClass>('GroupTransferClass', GroupTransferClass)),
    vip: tt.nullable(t.boolean),
    information: tt.nullable(TransferInformation),
    // =========== /Трансфер ===============
  }),
]);
export type PassRequest = t.TypeOf<typeof PassRequest>;

/** Запланированный водитель */
export const Planned = t.type({
  driver: DriverShort,
  vehicle: VehicleShort,
});
export type Planned = t.TypeOf<typeof Planned>;

/** Пассажирская поездка */
export const PassTrip = t.intersection([
  t.type({
    id: tt.uuid,
    status: ioTypeFromEnum<TRIP_STATUSES>('TRIP_STATUSES', TRIP_STATUSES),
    requests: t.array(PassRequest),
    waypoints: t.array(Waypoint),
    passengerCount: t.number,
  }),
  t.partial({
    planned: tt.nullable(Planned),
    taxiClass: tt.nullable(ioTypeFromEnum<TaxiClass>('TaxiClass', TaxiClass)),
    humanReadableId: tt.nullable(t.string),
    externalHumanReadableId: tt.nullable(t.string),
    // да, startTime параметр не обязателен,
    // т к например, для грузов, в случае удаления записи он чистится, но запись продолжает отображаться в списке
    expectedStartTime: tt.nullable(t.string),
    factStartTime: tt.nullable(t.string),
    expectedEndTime: tt.nullable(t.string),
    factEndTime: tt.nullable(t.string),
    driver: tt.nullable(DriverShort),
    vehicle: tt.nullable(VehicleShort),
    dispatcher: tt.nullable(DispatcherShort),
    /** Фактический километраж, км */
    factDistance: tt.nullable(t.number),
    /** Время ожидания, секунды */
    driverWaitingTime: tt.nullable(t.number),
    isNew: tt.nullable(t.boolean),
    creationTime: tt.nullable(t.string),
    expectedCost: tt.nullable(t.number),
    expectedDistance: tt.nullable(t.number),
    expectedTime: tt.nullable(t.number),
    contractorId: tt.nullable(t.string),
    comment: t.string,
    information: TransferInformation,
  }),
]);
export type PassTrip = t.TypeOf<typeof PassTrip>;

/** Пагинированный список пассажирских поездок */
export const PassTrips = createPagination(PassTrip);
export type PassTrips = t.TypeOf<typeof PassTrips>;

export interface TripData {
  contractorId: tt.UUID;
  driverId: tt.UUID;
  tripId: tt.UUID;
}

/** Плановая поездка */
export const BusynessTrip = t.type({
  id: tt.uuid,
  humanReadableId: t.string,
  status: ioTypeFromEnum<TRIP_STATUSES>('TRIP_STATUSES', TRIP_STATUSES),
  expectedStartTime: t.string,
  expectedEndTime: t.string,
  isPlanning: t.boolean,
  vehicle: VehicleShort,
  passenger: t.array(t.string),
  planning: t.boolean,
  startAddress: t.type({
    name: t.string,
    latitude: t.number,
    longitude: t.number,
  }),

});
export type BusynessTrip = t.TypeOf<typeof BusynessTrip>;

export const Busyness = t.type({
  busyness: t.array(t.type({
    driver: t.type({
      id: tt.uuid,
      humanReadableId: t.string,
    }),
    trips: t.array(BusynessTrip),
  })),
  orders: t.unknown,
});
export type Busyness = t.TypeOf<typeof Busyness>;

export interface BusynessFilters {
  startTime: string;
  endTime: string;
  onlyPlanning: boolean;
  driverIds: tt.UUID[];
}

export interface CurrentTripData {
  contractorId: tt.UUID;
  driverId: tt.UUID;
}

export interface PatchStatus {
  field: 'status';
  value: TRIP_STATUSES;
}

export interface PatchChangedByDiver {
  field: 'changedByDriver';
  value: boolean;
}

export interface PatchLatitude {
  field: 'latitude';
  value: number;
}

export interface PatchLongitude {
  field: 'longitude';
  value: number;
}

export interface PatchAzimuth {
  field: 'azimuth';
  value: number;
}

export interface PatchTimezone {
  field: 'timezone';
  value: string;
}

export interface PatchType {
  field: 'type';
  value: CheckinType;
}

export interface PatchDateTime {
  field: 'dateTime';
  value: string;
}

export interface PatchTripData {
  contractorId: tt.UUID;
  tripId: tt.UUID;
  changes: (
    PatchStatus |
    PatchChangedByDiver |
    PatchChangedByDiver |
    PatchLatitude |
    PatchLongitude |
    PatchAzimuth |
    PatchTimezone |
    PatchType |
    PatchDateTime
  )[];
}

export interface ChangeTripStatusData {
  tripId: tt.UUID;
  status: TRIP_STATUSES;
  longitude: number;
  latitude: number;
  checkinType: CheckinType;
  /** Нужно передавать на старте поездки, когда в кэше еще нет трипа */
  trip?: PassTrip;
}

export interface CheckinData {
  contractorId: tt.UUID;
  tripId: tt.UUID;
}

/** Чекины */
export const Checkin = t.type({
  longitude: t.number,
  latitude: t.number,
  time: t.string,
  status: ioTypeFromEnum<TRIP_STATUSES>('TRIP_STATUSES', TRIP_STATUSES),
  type: ioTypeFromEnum<CheckinType>('CheckinType', CheckinType),
});
export type Checkin = t.TypeOf<typeof Checkin>;

/** Длительность */
export const TripDuration = t.type({
  days: t.number,
  hours: t.number,
  minutes: t.number,
  seconds: t.number,
});
export type TripDuration = t.TypeOf<typeof TripDuration>;

/** Текущее местоположение водителя */
export const DriverCurrentLocation = t.partial({
  longitude: t.number,
  latitude: t.number,
});
export type DriverCurrentLocation = t.TypeOf<typeof DriverCurrentLocation>;

/** Чекины (отметки водителя на точках) */
export const CheckinInfo = t.intersection([
  t.type({
    chekins: t.array(Checkin),
    complete: t.boolean,
  }),
  t.partial({
    driverLocation: tt.nullable(DriverCurrentLocation),
    tripDuration: tt.nullable(TripDuration),
  }),
]);
export type CheckinInfo = t.TypeOf<typeof CheckinInfo>;
