/* eslint-disable no-use-before-define */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { EmployeeStatus } from 'constants/constants.app';
import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

import {
  TransportTypeDescriptions,
  TransportTypes
} from 'stores/TransportTypes/TransportTypes.interface';
import {
  TaxiStatuses,
  CarsharingStatuses,
  PersonalStatuses,
  PublicStatuses,
  GroupTransferStatuses
} from 'constants/constants.statuses';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { LabeledValue } from 'utils/Types';
import { Route } from '../Geo/Geo.interface';

export interface ITripService {
  calculateTripCost: (data: ITripCalculateRequest) => Promise<ITripPrice[]>;
  saveTripRequest(data: TripRequestNew): Promise<TripRequest>;
  editTripRequest(data: TripRequestNew, requestId: string): Promise<number>;
  rateTripRequest(data: RequestRating, requestId: string): Promise<number>;
  getTripRequestList(authorId: string): Promise<TripRequest[]>;
  getTripRequest(requestId: string): Promise<TripRequest>;
  deleteRequest(id: string): Promise<any>;
  cancelRequest(id: string, reason: TripCancelReason): Promise<any>;
  getApprovementList(searchParams: Record<string, any>): Promise<TripRequest[]>;
  getAllPurposes(): Promise<Purpose[]>;
  getAllTariffsTaxi(): Promise<TTariffTaxi[]>;
  getAllTariffsPersonal(): Promise<TTariffPersonal[]>;

  approveTripRequest(reqId: string): Promise<TripRequest>;
  finishTripRequest(reqId: string): Promise<number>;
  declineTripRequest(reqId: string, data: DeclineReason): Promise<number>;
}

export type ITripServiceResponse = Record<string, any>;

export interface ITripCalculateRequest {
  distance: number;
  time: number;
  tripDate: number;
}

export enum TaxiClass {
  ECONOMY = 'ECONOMY',
  COMFORT = 'COMFORT',
  COMFORT_PLUS = 'COMFORT_PLUS',
  BUSINESS = 'BUSINESS',
  OFFICIAL = 'OFFICIAL',
  CARSHARING = 'CARSHARING',
  BICYCLE = 'BICYCLE',
  WALK = 'WALK',

  VIP_BUS = 'VIP_BUS',
  SMALL_BUS = 'SMALL_BUS',
  MIDDLE_BUS = 'MIDDLE_BUS',
  LARGE_BUS = 'LARGE_BUS',
}

export enum TaxiClassShort {
  ECONOMY = 'ECONOMY',
  COMFORT = 'COMFORT',
  COMFORT_PLUS = 'COMFORT_PLUS',
  BUSINESS = 'BUSINESS',

  VIP_BUS = 'VIP_BUS',
  SMALL_BUS = 'SMALL_BUS',
  MIDDLE_BUS = 'MIDDLE_BUS',
  LARGE_BUS = 'LARGE_BUS',
}

export const taxiType = ioTypeFromEnum<TaxiClass>('taxiClass', TaxiClass);
export type taxiType = t.TypeOf<typeof taxiType>;

export const taxiTypeShort = ioTypeFromEnum<TaxiClassShort>('taxiClassShort', TaxiClassShort);
export type taxiTypeShort = t.TypeOf<typeof taxiTypeShort>;

export const TaxiClassDescriptions: Record<TaxiClass, string> = {
  ECONOMY: 'Эконом',
  COMFORT: 'Комфорт',
  COMFORT_PLUS: 'Комфорт+',
  BUSINESS: 'Бизнес',
  OFFICIAL: 'Служебный',
  CARSHARING: 'Каршеринг',
  BICYCLE: 'Велосипед',
  WALK: 'Пешком',

  VIP_BUS: 'Автобус до 9 мест',
  SMALL_BUS: 'Автобус от 10 до 21 места',
  MIDDLE_BUS: 'Автобус от 22 до 41 мест',
  LARGE_BUS: 'Автобус от 42 до 55 мест',
};

export const TaxiClassShortDescriptions: Record<TaxiClassShort, string> = {
  ECONOMY: 'Эконом',
  COMFORT: 'Комфорт',
  COMFORT_PLUS: 'Комфорт+',
  BUSINESS: 'Бизнес',

  VIP_BUS: 'Автобус до 9 мест',
  SMALL_BUS: 'Автобус от 10 до 21 места',
  MIDDLE_BUS: 'Автобус от 22 до 41 мест',
  LARGE_BUS: 'Автобус от 42 до 55 мест',
};

export const TransportTypeOptions: LabeledValue<TransportTypes>[] = [
  {
    label: TransportTypeDescriptions[TransportTypes.TAXI],
    value: TransportTypes.TAXI,
  },
  {
    label: TransportTypeDescriptions[TransportTypes.PERSONAL],
    value: TransportTypes.PERSONAL,
  },
  {
    label: TransportTypeDescriptions[TransportTypes.PUBLIC],
    value: TransportTypes.PUBLIC,
  },
];

export enum PersonalCarsEnum {
  PERSONAL = 'PERSONAL',
}

export enum PersonalCarsTitleEnum {
  PERSONAL = 'Личный',
}

export type PersonalCarsType = keyof typeof PersonalCarsEnum;

export interface ITaxi {
  name: string;
  transportType: TransportTypes;
  taxiClass?: TaxiClass;
  waitingTime: number;
  disabled?: boolean;
  info?: string;
  type?: 'corporate' | 'personal';
}

export interface ITripPrice {
  id: string;
  cost: number;
  transportType: TransportTypes;
  taxiClass?: TaxiClass;
}

export const IOFileEmployee = t.intersection([
  t.type({}),
  t.partial({
    id: t.string,
    userId: t.string,
    availableTransportTypes: t.UnknownArray,
    personalCars: t.UnknownArray,
    organizationId: t.string,
    firstName: t.string,
    lastName: t.string,
    patronymic: t.string,
    email: t.string,
    personnelNumber: t.string,
    mobilePhone: t.string,
    position: t.string,
    department: t.string,
    delegatedById: t.string,
    supervisorId: t.string,
    status: ioTypeFromEnum<EmployeeStatus>('EmployeeStatus', EmployeeStatus),
  }),
]);

export const IOTariffBase = t.intersection([
  t.type({
    name: t.string,
    region: t.string,
    rideCostPerKm: t.number,
    transportType: ioTypeFromEnum<TransportTypes>('TransportTypeEnum', TransportTypes),
  }),
  t.partial({
    // Сомнительное место. Паша должен исправить.
    organizationId: t.string,
  }),
]);

export const IOTariffTaxi = t.intersection([
  IOTariffBase,
  t.type({
    deptId: t.string,
    taxiClass: ioTypeFromEnum<TaxiClass>('TaxiClassEnum', TaxiClass),
    rideCostPerMin: t.number,
    waitCostPerMin: t.number,
    freeWaitingTime: t.number,
    carServiceCost: t.number,
  }),
  t.partial({
    id: t.string,
    priceDetails: t.unknown,
    deptStruct: t.string,
    deptName: t.string,
    vehicleType: t.string,
    minRideCost: t.number,
    savingsDeviationPct: t.number,
    distanceDeviationKm: t.number,
    timeDeviationMin: t.number,
    maxCapacity: t.number,
    minCancelTimeMin: t.number,
    tollRoads: t.boolean,
    historicalTraffic: t.boolean,
    minServiceTime: t.number,
    tariffId: t.string,
  }),
]);

export const IOTariffPriceDetails = t.partial({
  rewardForPassenger: t.number,
  season: t.partial({
    coefficient: t.number,
    start: t.string,
    end: t.string,
  }),
});

export const IOTariffPersonal = t.intersection([
  IOTariffBase,
  t.type({
    coefficient: t.number,
    seasonStart: t.string,
    seasonEnd: t.string,
  }),
  t.partial({
    id: t.string,
    priceDetails: IOTariffPriceDetails,
    rewardForPassenger: t.number,
  }),
]);

export type TTariffPriceDetails = t.TypeOf<typeof IOTariffPriceDetails>;

export type TTariffBase = t.TypeOf<typeof IOTariffBase>;

export type TTariffTaxi = t.TypeOf<typeof IOTariffTaxi>;

export type TTariffPersonal = t.TypeOf<typeof IOTariffPersonal>;

export type TTarifUnited = TTariffTaxi | TTariffPersonal;

export const Purpose = t.intersection([
  t.type({
    id: tt.uuid,
  }),
  t.partial({
    purpose: tt.nullable(t.string),
  }),
]);
export type Purpose = t.TypeOf<typeof Purpose>;

export const DeclineReason = t.type({
  reason: t.string,
  code: t.number,
});
export const ApprovedBy = t.partial({
  id: tt.uuid,
  humanReadableId: t.string,
  userId: tt.uuid,
  firstName: t.string,
  lastName: t.string,
  patronymic: t.string,
  personnelNumber: t.string,
  itinerantType: t.string,
  mvz: t.string,
  delegatedById: tt.uuid,
  supervisorId: tt.uuid,
  positionId: tt.uuid,
  organizationId: tt.uuid,
  departmentId: tt.uuid,
});
export type ApprovedBy = t.TypeOf<typeof ApprovedBy>;

export type DeclineReason = t.TypeOf<typeof DeclineReason>;

export const RequestRating = t.partial({
  advantages: tt.nullable(t.array(t.string)),
  drawbacks: tt.nullable(t.array(t.string)),
  ratingComment: tt.nullable(t.string),
  rating: tt.nullable(t.number),
});
export type RequestRating = t.TypeOf<typeof RequestRating>;

export const FeedPassenger = t.intersection([
  t.type({
    humanReadableId: t.string,
    id: tt.uuid,
    firstName: t.string,
    lastName: t.string,
  }),
  t.partial({
    positionId: t.union([tt.uuid, t.null]),
    positionName: t.string,
    mobilePhone: t.string,
    userId: t.union([tt.uuid, t.null]),
    patronymic: t.string,
    personnelNumber: t.string,
    itinerantType: t.union([t.string, t.null]),
    mvz: t.string,
    delegatedById: t.string,
    supervisorId: t.string,
    organizationId: t.union([tt.uuid, t.null]),
    departmentId: t.union([tt.uuid, t.null]),
  }),
]);
export type FeedPassenger = t.TypeOf<typeof FeedPassenger>;

export const Passenger = t.intersection([
  t.type({
    humanReadableId: t.string,
    id: tt.uuid,
    firstName: t.string,
    lastName: t.string,
  }),
  t.partial({
    organizationId: tt.nullable(tt.uuid),
    positionId: tt.nullable(tt.uuid),
    constCenter: tt.nullable(t.string),
    positionName: tt.nullable(t.string),
    mobilePhone: tt.nullable(t.string),
    phone: tt.nullable(t.string),
    userId: tt.nullable(tt.uuid),
    patronymic: tt.nullable(t.string),
    personnelNumber: tt.nullable(t.string),
    itinerantType: tt.nullable(t.string),
    mvz: tt.nullable(t.string),
    marriageCertificateNumber: t.union([tt.nullable(t.number), tt.nullable(t.string)]),
    delegatedById: tt.nullable(t.string),
    supervisorId: tt.nullable(t.string),
    departmentId: tt.nullable(tt.uuid),
    departmentName: tt.nullable(t.string),
    organizationName: tt.nullable(t.string),
  }),
]);
export type Passenger = t.TypeOf<typeof Passenger>;

export const TripRequest = t.intersection([
  t.type({
    id: t.string,
    humanReadableId: t.string,
    author: Passenger,
    passenger: Passenger,
    transportType: ioTypeFromEnum<TransportTypes>('TransportTypeEnum', TransportTypes),
    expected: Route,
    purpose: Purpose,
    passengerCount: t.number,
    status: t.string,
    creationTime: t.number,
    coopTrip: t.boolean,
    approvalState: t.string,
  }),
  t.partial({
    department: t.strict({ id: tt.uuid, departmentName: t.string }),
    position: t.intersection([t.type({ id: tt.uuid }), t.partial({ positionName: t.string })]),
    desiredDate: t.number,
    taxiClass: ioTypeFromEnum<TaxiClass>('TaxiClassEnum', TaxiClass),
    tariffId: t.string,
    approvedBy: ApprovedBy,
    requestRating: tt.nullable(RequestRating),
    passengers: t.array(Passenger),
    sharedRideId: t.string,
    humanReadableLimitId: t.string,
    commentForDriver: t.string,
  }),
]);

export type TripRequest = t.TypeOf<typeof TripRequest>;

const TTaxiStatuses = ioTypeFromEnum<TaxiStatuses>('TaxiStatuses', TaxiStatuses);
const TCarsharingStatuses = ioTypeFromEnum<TaxiStatuses>('CarsharingStatuses', CarsharingStatuses);
const TGroupTransferStatuses = ioTypeFromEnum<GroupTransferStatuses>('GroupTransferStatuses', GroupTransferStatuses);
const TPublicStatuses = ioTypeFromEnum<PublicStatuses>('PublicStatuses', PublicStatuses);
const TPersonalStatuses = ioTypeFromEnum<PersonalStatuses>('PersonalStatuses', PersonalStatuses);

const TripRequestStatuses = t.union([
  TTaxiStatuses,
  TCarsharingStatuses,
  TGroupTransferStatuses,
  TPublicStatuses,
  TPersonalStatuses,
]);

export const TripRequestHistory = t.array(t.type({
  changeDate: t.number,
  code: t.number,
  initiator: t.string,
  status: TripRequestStatuses,
}));
export type TripRequestHistory = t.TypeOf<typeof TripRequestHistory>;

export type TripRequestNew = Pick<TripRequest, 'transportType' | 'expected' | 'purpose'> & Partial<TripRequest>;

export const TripCancelReason = t.type({
  reason: t.string,
  code: t.number,
});

export type TripCancelReason = t.TypeOf<typeof TripCancelReason>;

export enum TaxiOptions {
  CHILD_SEAT = 'CHILD_SEAT',
  PET_TRANSPORTATION = 'PET_TRANSPORTATION',
  BICYCLE_SKI_TRANSPORTATION = 'BICYCLE_SKI_TRANSPORTATION',
  NON_SMOKING_DRIVER = 'NON_SMOKING_DRIVER',
  YELLOW_REG_PLATES = 'YELLOW_REG_PLATES',
  MATERIAL_ASSETS_TRANSPORTATION = 'MATERIAL_ASSETS_TRANSPORTATION',
  PERSONAL_USAGE = 'PERSONAL_USAGE',
}

export const TaxiOptionsDescriptions: Record<TaxiOptions, string> = {
  CHILD_SEAT: 'Детское кресло',
  PET_TRANSPORTATION: 'Перевозка животных',
  BICYCLE_SKI_TRANSPORTATION: 'Перевозка велосипедов и лыж',
  NON_SMOKING_DRIVER: 'Некурящий водитель',
  YELLOW_REG_PLATES: 'Желтые номера',
  MATERIAL_ASSETS_TRANSPORTATION: 'Материальная ценность',
  PERSONAL_USAGE: 'Личное пользование',
};

export interface IRequestRating {
  rating?: number;
  advantages?: string[];
  drawbacks?: string[];
  ratingComment?: string;
}

export enum TaxiClassEnum {
  ECONOMY = 'ECONOMY',
  COMFORT = 'COMFORT',
  COMFORT_PLUS = 'COMFORT_PLUS',
  PERSONAL = 'PERSONAL',
  BUSINESS = 'BUSINESS',
  OFFICIAL = 'OFFICIAL',
  TAXI = 'TAXI',
  CARSHARING = 'CARSHARING',
  BICYCLE = 'BICYCLE',
  WALK = 'WALK',
  PUBLIC = 'PUBLIC',
  SCOOTER = 'SCOOTER',
  YANDEX = 'YANDEX',
  CITYMOBIL = 'CITYMOBIL',
  UBER = 'UBER',

  VIP_BUS = 'VIP_BUS',
  SMALL_BUS = 'SMALL_BUS',
  MIDDLE_BUS = 'MIDDLE_BUS',
  LARGE_BUS = 'LARGE_BUS',
  BUS = 'BUS', // stub for all buses
}

export enum UsedMIMEFileFormatEnum {
  JPG = 'image/jpg',
  JPEG = 'image/jpeg',
  PNG = 'image/png',
  TIFF = 'image/tiff',
  PDF = 'application/pdf',
  DOC = 'application/msword',
  DOCX = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  HEIC = 'heic',
}

export enum UsedFileFormatEnum {
  JPG = 'JPG',
  JPEG = 'JPEG',
  PNG = 'PNG',
  TIFF = 'TIFF',
  PDF = 'PDF',
  DOC = 'DOC',
  DOCX = 'DOCX',
  HEIC = 'HEIC',
  jpg = 'jpg',
  jpeg = 'jpeg',
  png = 'png',
  tiff = 'tiff',
  pdf = 'pdf',
  heif = 'heif',
}
