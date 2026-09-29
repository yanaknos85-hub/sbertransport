import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import {
  DetailedPublicCompensation,
  Expected,
  Purpose
} from '../PublicRegistry/models/PublicRegistry.interface';
import {
  Passenger, RequestRating, TaxiClass, TaxiOptions
} from '../Trip/Trip.interface';
import { TransportTypes } from '../TransportTypes/TransportTypes.interface';
import { InfoContractor } from 'api/reports';
import { Employee, EmployeeModel } from '@sber-sbertransport/mf-core';

interface Settings {
  nameSetting: string;
  valueSetting: string;
  sort: number;
}

interface Controls {
  typeControl: string;
  value: string;
  settings: Settings[];
}

export interface IUiPreferences {
  userID: string;
  nameForm: string;
  statusCode: number;
  statusDescription: string;
  controls: Controls[];
}

export interface SelectedRowData {
  requestId: string;
  payed: boolean;
}
export interface IRegistryStore {
  selectedRowData: SelectedRowData[];
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  saveSettings: any;
}
export interface IRegistryService {
  getUiPreferences(userID: string | undefined, nameForm: string): Promise<IUiPreferences>;
  setUiPreferences(userID: string | undefined, data: IUiPreferences): Promise<IUiPreferences>;

  getUiPreferencesOto(userID: string | undefined, nameForm: string): Promise<IUiPreferences>;
  setUiPreferencesOto(userID: string | undefined, data: IUiPreferences): Promise<IUiPreferences>;
}

export const PersonalCarDetailed = t.partial({
  id: tt.nullable(t.string),
  engineVolume: tt.nullable(t.number),
  ownerInfo: tt.nullable(t.string),
  registrationNumber: tt.nullable(t.string),
  brandName: tt.nullable(t.string),
  model: tt.nullable(t.string),
  insuranceNumber: tt.nullable(t.string),
  registrationCertificate: tt.nullable(t.string),
});

export const DriverInfo = t.partial({
  vehicleInfo: tt.nullable(t.string),
  registrationNumber: tt.nullable(t.string),
  driverName: tt.nullable(t.string),
  driverPhone: tt.nullable(t.string),
});

export type PersonalCarDetailed = t.TypeOf<typeof PersonalCarDetailed>;
export type DriverInfo = t.TypeOf<typeof DriverInfo>;

export const GroupTransferChildSeatDetails = t.partial({
  group1: t.number,
  group2: t.number,
  booster: t.number,
  newborn: t.number,
});
export type GroupTransferChildSeatDetails = t.TypeOf<typeof GroupTransferChildSeatDetails>;

export const AdditionalGroupTransferInformation = t.partial({
  addContact: t.string,
  animal: t.boolean,
  animalComment: t.string,
  bugs: t.boolean,
  bugsComment: t.string,
  bugsOversized: t.boolean,
  bugsOversizedComment: t.string,
  childSeat: t.boolean,
  childSeatDetails: GroupTransferChildSeatDetails,
  dateFlight: t.number,
  numberFlight: t.string,
  phoneHotel: t.string,
  typeVehicle: t.string,
  addContactFIO: t.string,
  addContactPhone: t.string,
});
export type AdditionalGroupTransferInformation = t.TypeOf<typeof AdditionalGroupTransferInformation>;

export const TripInfo = t.partial({
  rentId: t.number,
  rentCreatedAt: t.number,
  rentFinishedAt: t.number,
  reserveTime: t.number,
  drivingTime: t.number,
  parkingTime: t.number,
  drivingLength: t.number,
  startAddress: t.string,
  finishAddress: t.string,
  reserveTimeCost: t.number,
  drivingTimeCost: t.number,
  parkingTimeCost: t.number,
  drivingLengthCost: t.number,
  totalCost: t.number,
  carModel: t.string,
  carNumber: t.string,
});

export type TripInfo = t.TypeOf<typeof TripInfo>;

export const TripResponse = t.intersection([
  t.type({
    id: tt.uuid,
    expected: Expected,
    transportType: ioTypeFromEnum<TransportTypes>('TransportTypes', TransportTypes),
    passenger: Passenger,
  }),
  t.partial({
    author: Passenger,
    driverInfo: DriverInfo,
    passengers: t.array(Passenger),
    taxiClass: ioTypeFromEnum<TaxiClass>('TaxiClass', TaxiClass),
    passengerCount: t.number,
    tariffId: tt.uuid,
    deadline: t.number,
    desiredDate: t.number,
    creationDate: t.number,
    tripConfirmationDate: t.number,
    purpose: Purpose,
    commentForDriver: t.string,
    humanReadableId: t.string,
    magentaOrderId: t.number,
    sharedRideId: t.string,
    sharedRideOwner: t.boolean,
    passenger: Employee,
    approvedBy: Passenger,
    approvalState: t.string,
    approvalDate: t.number,
    orderPaymentFormationStartDate: t.number,
    status: t.string,
    creationTime: t.number,
    occupiedPlacesCount: t.number,
    requestOptions: t.array(ioTypeFromEnum<TaxiOptions>('TaxiOptions', TaxiOptions)),
    requestRating: tt.nullable(RequestRating),
    coopTrip: t.boolean,
    contractor: InfoContractor, // так приходит для такси
    contractorId: t.string, // а так приходит для group transfer
    compensationType: t.string,
    personalCar: PersonalCarDetailed,
    tariffName: t.string,
    transportCompensation: t.array(DetailedPublicCompensation),
    payRequestIds: t.array(t.string),
    joinedPassengers: t.array(Employee),
    minTaxiTariffCost: t.number,
    totalSharedRequestCount: t.number,
    departmentEconomy: t.number,
    limitDebit: t.number,
    financialImpact: t.number,
    financialImpactShared: t.number,
    financialImpactJoined: t.number,
    trip: TripInfo,
    statusCodeDescription: t.string,
  }),
  t.partial({
    groupTransferClass: t.string,
    vip: t.boolean,
    timeZone: t.string,
    information: AdditionalGroupTransferInformation,
  }),
]);

export type TripResponse = t.TypeOf<typeof TripResponse>;

export const IOOrdersKpi = t.intersection([
  t.type({
    orderId: t.string,
    costSharePart: t.number,
    rideTimeMin: t.number,
    savings: t.number,
    savingsPct: t.number,
    orderDistanceKm: t.number,
  }),
  t.partial({
    state: t.string,
    orderPriceKop: t.number,
    candidate: t.boolean,
  }),
]);

export const IOTripKpi = t.intersection([
  t.type({
    totalCost: tt.money,
    totalDistanceKm: t.number,
    totalTimeMin: t.number,
    ordersKpi: t.array(IOOrdersKpi),
  }),
  t.partial({
    state: t.string,
  }),
]);

export const IOWaypointTripFromCoop = t.partial({
  country: t.string,
  region: t.string,
  city: t.string,
  street: t.string,
  house: t.string,
  latitude: t.number,
  longitude: t.number,
  building: t.string,
  structure: t.string,
  existInVspGosbTbRegistry: t.boolean,
  waitTime: t.number,
  checkinAutomatic: t.boolean,
  checkinManual: t.boolean,
  absenceReason: t.string,
});

export interface ICar {
  brandName: string;
  engineVolume: number;
  id: string;
  insuranceNumber: string;
  model: string;
  ownerInfo: string;
  registrationCertificate: string;
  registrationNumber: string;
}

export interface IRequestsModal extends EmployeeModel {
  car?: ICar;
  sharedRideOwner?: boolean;
  employee?: Employee;
  commentForDriver?: string;
  statusDescription?: string;
  statusCode?: number;
}

export const IOTripFromCoopStops = t.intersection([
  t.type({
    orderId: t.number,
  }),
  t.partial({
    requestId: t.string,
    active: t.boolean,
    eventType: t.string,
    startTime: t.union([t.number, t.string]),
    endTime: t.union([t.number, t.string]),
    waypoint: IOWaypointTripFromCoop,
  }),
]);

const IOEmployeeWithoutOrg = t.intersection([
  Employee,
  t.partial({
    organisationId: t.string,
  }),
]);

const IORequestsSharedModal = t.type({
  employee: Employee,
  humanReadableId: t.string,
  id: t.string,
  sharedRideOwner: t.boolean,
  status: t.string,
  statusCode: t.number,
});

export const IOTripFromCoop = t.type({
  magentaId: t.number,
  passengers: t.number,
  employeePassengers: t.array(IOEmployeeWithoutOrg),
  tariffId: t.string,
  active: t.boolean,
  stops: t.array(IOTripFromCoopStops),
  kpi: IOTripKpi,
  requests: t.array(IORequestsSharedModal),
});

export type TTripFromCoop = t.TypeOf<typeof IOTripFromCoop>;

export interface IRequestsSharedModal extends Omit<EmployeeModel, 'status'> {
  car?: ICar;
  sharedRideOwner?: boolean;
  employee?: Employee;
  commentForDriver?: string;
  statusDescription?: string;
  statusCode?: number;
  status?: string;
}
