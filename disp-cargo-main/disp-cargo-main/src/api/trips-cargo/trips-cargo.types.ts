import { DriverSpecialityTypes } from 'constants/driver.constants';
import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { UUID } from 'utils/io-ts';
import { CheckinType, DeadlineState, TRIP_STATUSES } from 'constants/trips.constants';
import { PaginationParams, createPagination } from 'utils/io-ts/pagination';
import { TripTypes } from 'constants/app.constants';

export const EmployeeShort = t.intersection([
  t.type({}),
  t.partial({
    patronymic: t.string,
    lastName: t.string,
    firstName: t.string,
    humanReadableId: t.string,
    mobilePhone: t.string,
    organization: t.string,
    email: t.string,
  }),
]);
export type EmployeeShort = t.TypeOf<typeof EmployeeShort>;

export const DriverShort = t.intersection([
  EmployeeShort,
  t.type({
    rating: t.number,
    id: t.string,
    humanReadableId: t.string,
  }),
  t.partial({
    contactPhone: t.string,
    shiftId: tt.uuid,
  }),
]);
export type DriverShort = t.TypeOf<typeof DriverShort>;

export const VehicleShort = t.intersection([
  t.type({
    stateNumber: t.string,
    brand: t.string,
  }),
  t.partial({
    model: t.string,
    color: t.string,
  }),
]);
export type VehicleShort = t.TypeOf<typeof VehicleShort>;

export const DispatcherShort = t.intersection([
  t.type({
    lastName: t.string,
    firstName: t.string,
  }),
  t.partial({
    phone: t.string,
    patronymic: t.string,
  }),
]);
export type DispatcherShort = t.TypeOf<typeof DispatcherShort>;

export const Author = t.partial({
  firstName: t.string,
  lastName: t.string,
  humanReadableId: t.string,
  userId: t.string,
  patronymic: t.string,
  personnelNumber: t.string,
  itinerantType: t.string,
  mvz: t.string,
  costCenter: t.string,
  marriageCertificateNumber: t.string,
  delegatedById: t.string,
  supervisorId: t.string,
  positionName: t.string,
  departmentName: t.string,
  phone: t.string,
  mobilePhone: t.string,
  id: tt.uuid,
  organizationId: tt.uuid,
  positionId: tt.uuid,
  departmentId: tt.uuid,
  organization: t.string,
});
export type Author = t.TypeOf<typeof Author>;

export const Address = t.type({
  building: t.string,
  city: t.string,
  country: t.string,
  house: t.string,
  region: t.string,
  street: t.string,
  structure: t.string,
});
export type Address = t.TypeOf<typeof Address>;

export interface TripsFilters extends PaginationParams {
  statuses?: TRIP_STATUSES[];
  humanReadableId?: string;
  requestHumanReadableId?: string;
  desireDateStart?: string;
  desireDateEnd?: string;
}

export const TripsStatistic = t.intersection([
  t.type({}),
  t.partial({
    assignCount: t.number,
    notAssignCount: t.number,
    totalCount: t.number,
  }),
]);

export type TripsStatistic = t.TypeOf<typeof TripsStatistic>;

/** Запланированный водитель */
export const Planned = t.type({
  driver: DriverShort,
  vehicle: VehicleShort,
});
export type Planned = t.TypeOf<typeof Planned>;

// =================== Грузы =====================

/** Данные о грузе */
export const Cargo = t.intersection([
  t.type({
    id: tt.uuid,
  }),
  t.partial({
    position: t.number,
    name: t.string,
    cargoName: t.string,
    type: t.string,
    category: t.string,
    length: t.number,
    width: t.number,
    height: t.number,
    volume: t.number,
    weight: t.number,
    occupiedPlacesCount: t.number,
    fragile: t.boolean,
    needPackage: t.boolean,
    packageCount: t.number,
  }),
]);
export type Cargo = t.TypeOf<typeof Cargo>;

export enum RequestType {
  Load = 'LOAD',
  Unload = 'UNLOAD',
}

export const Pack = t.type({
  count: t.number,
  name: t.string,
  unit: t.string,
});
export type Pack = t.TypeOf<typeof Pack>;

/** Грузовая заявка */
export const CargoRequest = t.intersection([
  t.type({
    humanReadableId: t.string,
  }),
  t.partial({
    id: tt.uuid,
    transportType: t.string,
    status: t.string,
    author: Author,
    approvalState: t.string,
    approvalDate: t.string,
    approvedBy: Author,
    addresses: t.array(Address),
    creationTime: t.string,
    desiredDate: t.string,
    deadlineState: ioTypeFromEnum('DeadlineState', DeadlineState),
    deadline: t.union([t.number, t.string]),
    sender: Author,
    recipient: Author,
    transferTime: t.string,
    shipmentTime: t.string,
    cargoTransportType: t.string,
    volume: t.number,
    weight: t.number,
    width: t.number,
    height: t.number,
    lenght: t.number,
    cargoData: t.array(Cargo),
    cargo: t.array(Cargo),
    occupiedPlacesCount: t.number,
    sourceLoaders: t.number,
    destinationLoaders: t.number,
    timeZone: t.string,
    comment: t.string,
    commentForDriver: t.string,
    type: ioTypeFromEnum('RequestType', RequestType),
    organization: t.string,
    pack: t.array(Pack),
  }),
]);
export type CargoRequest = t.TypeOf<typeof CargoRequest>;

export const Contact = t.type({
  contact: t.type({
    fullName: t.string,
    phone: t.string,
  }),
  requests: t.array(CargoRequest),
});
export type Contact = t.TypeOf<typeof Contact>;

export const Waypoint = t.intersection([
  t.type({
    id: tt.uuid,
    latitude: t.number,
    longitude: t.number,
  }),
  t.partial({
    country: t.string,
    street: t.string,
    house: t.string,
    building: t.string,
    structure: t.string,
    existInVspGosbTbRegistry: t.boolean,
    region: t.string,
    city: t.string,
    waitingTime: t.string,
    fullAddress: t.string,
    contacts: t.array(Contact),
    index: t.number,
  }),
]);
export type Waypoint = t.TypeOf<typeof Waypoint>;

/** Грузовая поездка */
export const CargoTrip = t.intersection([
  t.type({
    id: tt.uuid,
    status: ioTypeFromEnum<TRIP_STATUSES>('TRIP_STATUSES', TRIP_STATUSES),
    requests: t.array(CargoRequest),
    waypoints: t.array(Waypoint),
    isNew: t.boolean,
  }),
  t.partial({
    planned: Planned,
    humanReadableId: t.string,
    routeHumanReadableId: t.string,
    // да, startTime параметр не обязателен,
    // т к например, для грузов в случае удаления записи он чистится, но запись продолжает отображаться в списке
    startTime: t.string,
    dispatcherStartTime: t.string,
    loaders: t.number,
    driverWaitingTime: t.number,
    endTime: t.string,
    driver: DriverShort,
    vehicle: VehicleShort,
    dispatcher: DispatcherShort,
    factDistance: t.number,
    capacity: t.number,
    loadersWorkTime: t.number,
    factCost: t.number,
    finishTime: t.string,
  }),
]);
export type CargoTrip = t.TypeOf<typeof CargoTrip>;

/** Пагинированный список грузовых поездок */
export const CargoTrips = createPagination(CargoTrip);
export type CargoTrips = t.TypeOf<typeof CargoTrips>;

export type EditCargoTripData = ({
  field: 'status';
  value: TRIP_STATUSES;
}
| {
  field: 'driverId';
  value: UUID;
}
| {
  field: 'planningShiftId';
  value: UUID;
}
| {
  field: 'factDistance';
  value: number | null;
}
| {
  field: 'factCost';
  value: number | null;
}
| {
  field: 'loadersWorkTime';
  value: number | null;
}
| {
  field: 'dispatcherStartTime';
  value: string | null;
}
| {
  field: 'loaders';
  value: number | null;
}
| {
  field: 'driverWaitingTime';
  value: number | null;
}
| {
  field: 'finishTime';
  value: string | null;
})[];

// ===================== Водители ==========================

export const ShiftsDriver = t.intersection([
  t.strict({
    id: tt.uuid,
    humanReadableId: t.string,
    firstName: t.string,
    lastName: t.string,
    online: t.boolean,
  }),
  t.partial({
    patronymic: t.string,
    driverSpeciality: ioTypeFromEnum<DriverSpecialityTypes>('DriverSpeciality', DriverSpecialityTypes),
  }),
]);

export type ShiftsDriver = t.TypeOf<typeof ShiftsDriver>;

export const ShiftsVehicle = t.intersection([
  t.type({
    stateNumber: t.string,
    id: tt.uuid,
  }),
  t.partial({
    active: t.boolean,
    vehicleType: ioTypeFromEnum<TripTypes>('TripTypes', TripTypes),
  }),
]);

export type ShiftsVehicle = t.TypeOf<typeof ShiftsVehicle>;

export const Shift = t.intersection([
  t.type({
    id: tt.uuid,
    vehicle: ShiftsVehicle,
    startDate: t.string,
    endDate: t.string,
  }),
  t.partial({
    active: t.boolean,
  }),
]);

export type Shift = t.TypeOf<typeof Shift>;

/** Сокращенная геолокация водителя для вебсокетов */
export const DriversLocationWebsocket = t.intersection([
  t.type({
    id: t.string,
    humanReadableId: t.string,
  }),
  t.partial({
    currentShift: t.type({ endDate: t.string, id: tt.uuid }),
    latitude: t.number,
    longitude: t.number,
    azimuth: t.number,
    online: t.boolean,
  }),
]);
export type DriversLocationWebsocket = t.TypeOf<typeof DriversLocationWebsocket>;

/** Геолокация водителя */
export const DriversLocation = t.intersection([
  DriverShort,
  t.partial({
    activeTripId: tt.uuid,
    serving: t.boolean,
    shiftId: tt.uuid,
    currentShift: Shift,
    latitude: t.number,
    longitude: t.number,
    azimuth: t.number,
    distanceInKilometer: t.number,
    online: t.boolean,
  }),
]);
export type DriversLocation = t.TypeOf<typeof DriversLocation>;

/** Пагинированный список геолокаций водителя */
export const DriversLocations = createPagination(DriversLocation);
export type DriversLocations = t.TypeOf<typeof DriversLocations>;

export interface SetDriversData {
  tripId: UUID;
  driverId: UUID;
  planningShiftId?: UUID;
}

export interface UseDriversLocationsParams {
  contractorId: UUID;
  query: PaginationParams & {
    latitude: number;
    longitude: number;
    deadline?: string;
    fullSearch?: boolean;
    enableShiftFilter?: boolean;
    forPlanning?: boolean;
  };
}

export type SearchDriverParams = Omit<UseDriversLocationsParams['query'], keyof PaginationParams>;

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
export const DriverCurrentLocation = t.type({
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
    driverLocation: DriverCurrentLocation,
    tripDuration: TripDuration,
  }),
]);
export type CheckinInfo = t.TypeOf<typeof CheckinInfo>;

/** Поездка в занятости водителей */
const DriverBusynessTrip = t.type({
  id: tt.uuid,
  humanReadableId: t.string,
  status: ioTypeFromEnum<TRIP_STATUSES>('TRIP_STATUSES', TRIP_STATUSES),
  startTime: t.string,
  endTime: t.string,
});
export type DriverBusynessTrip = t.TypeOf<typeof DriverBusynessTrip>;

/** Занятость водителей */
export const DriverBusyness = t.type({
  driver: t.type({
    id: tt.uuid,
    humanReadableId: t.string,
  }),
  trips: t.array(DriverBusynessTrip),
});
export type DriverBusyness = t.TypeOf<typeof DriverBusyness>;

/** Тело для запроса занятости */
export const DriverBusynessData = t.type({
  startTime: t.string,
  endTime: t.string,
  driverIds: t.array(tt.uuid),
});
export type DriverBusynessData = t.TypeOf<typeof DriverBusynessData>;
