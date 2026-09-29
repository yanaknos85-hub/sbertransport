/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-duplicate-enum-values */

import {
  EmployeeModel,
  Employee,
  IOHumanReadable,
  IOPersonalCar,
  TWaypoint,
  PersonalCar
} from '@sber-sbertransport/mf-core';
import * as t from 'io-ts';
import { Dictionary } from 'lodash';
import { Moment } from 'moment';

import { EmployeeStatus, IOEmployeeStatusType } from 'modules/EmployeeApp/EmployeeApp.constants';
import { TripStatusesEnum, TTripRequestStatuses } from 'modules/EmployeeApp/TripRequestStatuses.constants';

import { DelegateModel } from 'stores/Delegates/Delegates.interface';
import { LIMIT_TYPE, LimitSharing } from 'stores/Limits/Limit.interface';
import { TransportCompensations, TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

import { ApprovalStateStatuses } from 'shared/models/types';
import { IOWaypoint, RequestRoute, Segment } from 'shared/models/geo/types';

import * as tt from 'utils/io-ts';
import { UUID } from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { LabeledValue } from 'utils/Types';

import { AvailablePublicTransportType, AvailablePublicTTCompensationType } from 'api/trip-requests';

import { TripRequestModel } from './models';
import { TripPaginationModel } from './models/TripPagination.model';
import { TripSuitableModel } from './models/TripSuitable.model';

export enum TestA {
  test = 'test',
}

export interface ITripStore {
  classCosts: ITripTariff[];
  busCosts: ITripTariff[];
  externalPrices: TExternalPrices[];
  personalCarsCosts: ITripTariff[];
  selfEmployee?: EmployeeModel;
  currentTripRequest?: TripRequestModel | null;
  currentTripRequestId?: string | null;
  tripRequestList: TripRequestModel[];
  pagination: TripPaginationModel;
  savedSuccessfully: boolean;
  purposes: TripPurpose[];
  tariffsTaxi: TTariffTaxi[];
  // tariffsPersonal: TTariffPersonal[];
  // tariffsPublic?: TTariffPublic;
  purposesMapped: Dictionary<TripPurpose>;
  delegatesByEmployeeId: Record<string, DelegateModel[]>;
  coopTripsSettings: TCoopTripsSettings[];
  suitableCooperativeTrips: TripSuitableModel[];
  nonTerminalTotalElements: number | null;
  isSearchingSuitableTrip: boolean;
  isSearchedSuitableTrip: boolean;
  actualTariff?: TTariffPublic;
  tripStatusChanged?: boolean;
  isSaveFile?: boolean;
  isFromDetailedView: boolean;
  yandexRedirectLink: string;
  fraud: string | null;

  getCosts(data: ITripCalculateRequest): Promise<void>;
  getPersonalCarCost(data: ITripCalculateRequest): Promise<ITripTariff[] | undefined>;
  getTripRequestById(id: string): TripRequestModel | undefined;
  getExternalPrices(data: TTripRequestNew): Promise<void>;

  loadRequestList(): Promise<void>;
  loadRequestListTerminal(data: ITripRequestData): Promise<void>;
  loadRequestListNonTerminal(data: ITripRequestData): Promise<void>;
  loadPurposesList(orgId: string): Promise<void>;
  loadCoopTripsSettings(organizationId: string): Promise<void>;
  loadActualTariff(transTypeId: string, tariffId: UUID): Promise<void>;

  saveTripRequest(data: TTripRequestNew): Promise<void>;
  saveFile(file: File, folder: UUID | string): Promise<SavedFileInfo>;
  saveConfirmSuburbTripFile(requestId: string | UUID, filesData: SavedFileInfo[]): Promise<number>;
  saveConfirmCardTripFile(requestId: string | UUID, filesData: SavedFileInfo[]): Promise<number>;
  saveTravelCardRequest(data: InnerCityTransportRequestInfo): Promise<number>;
  saveSuburbCompensation(data: InnerCityTransportRequestInfo): Promise<number>;
  saveCityTripCompensation(data: CityTripCompensation): Promise<number>;
  savePublicTripRequest(data: PublicTripCompensation): Promise<unknown>;

  clearCoopTrips(): void;
  clearRequestList(): void;
  clearCurrentRequest(): void;
  clearClassCosts(): void;
  clearPersonalCarsCosts(): void;
  clearPagination(): void;
  clearYandexRequest(): void;

  initStore(): void;

  //

  rateTripRequest(data: IRequestRating, requestId: string): Promise<void>;
  setCurrentTripRequest(reqId: string): void;

  setIsSearchedSuitableTrip(value: boolean): void;

  setGroupTransferInformation(value: any): void;

  setGroupTransferLoadWaypoint(value: boolean): void;

  setIsSaveFile(value: boolean): void;

  searchCoopTrips(requestTrip: TTripRequestNew): Promise<void>;

  updateTripStateById({ id, newState }: { id: string; newState: Partial<TripRequestModel> }): void;

  joinCoopTrip(sharedId: string, data: TTripRequestNew): Promise<void>;
  addPersonalCost(tariff: ITripTariff): void;

  setPersonalCars(cars: PersonalCar[]): void;

  finishTripRequest(id: string): Promise<void>;
  toggleIsFromDetailedView(value: boolean): Promise<void>;

  setAvailableDispatcherTransport(value: IContentDispatcherTransport): Promise<void>;
  setAvailableChoosingBookingTransport(value: IContractorDispatcherTransport): Promise<void>;

  setRangeDispatcherTransportDate(value: any): Promise<void>;
  clearFraud(): void;
}
export interface ITripRequestData {
  pageSetting: { page: number; size: number };
  desiredDate?: { start?: number | null; end?: number | null };
  creationDate?: { start?: number | null; end?: number | null };
  page?: { sort: { sorted: boolean } };
  sortSetting?: { directionAsc: boolean };
  transportTypeEnum?: TransportTypeEnum;
  requestStatusSet?: [TripStatusesEnum | undefined | string];
}

export interface ITripService {
  getUserAvatart(userId: string | undefined): string | PromiseLike<string>;
  getTarrifCarsharing(tariffId: tt.UUID): unknown;
  getContractors(contractorid: tt.UUID): unknown;
  calculateTripCost: (
    data: ITripCalculateRequest,
    callBack: (transportTypeId: string, tariffId: UUID) => Record<string, unknown>
  ) => Promise<ITripTariff[]>;
  calculateCostForPersonalCarTrip: (data: ITripCalculateRequest) => Promise<ITripTariff[]>;
  calculateExternalPrices: (data: TTripRequestNew) => Promise<TExternalPrices[]>;

  saveTripRequest(data: TTripRequestNew): Promise<TripRequest | string>;

  editTripRequest(data: TTripRequestNew, requestId: string): Promise<number>;
  rateTripRequest(data: IRequestRating, requestId: string): Promise<TripRequest>;
  getTripRequestList(passengerId: string): Promise<TripRequest[]>;
  getTripRequestListNonTerminal(data: ITripRequestData): Promise<TripPaginationModel & { content: TripRequest[] }>;
  getTripRequestListTerminal(data: ITripRequestData): Promise<TripPaginationModel & { content: TripRequest[] }>;
  getTripRequest(requestId: string): Promise<TripRequest>;
  getTripRequestForTransport(requestId: string, transportType: TransportTypeEnum): Promise<TripRequest>;

  getTripRequestHistory(reqId: string): Promise<ITripHistory>;
  getAllPurposesByEmployee(orgId: string): Promise<TripPurpose[]>;
  getAllTariffsTaxi(): Promise<TTariffTaxi[]>;
  getAllTariffsPersonal(): Promise<TTariffPersonal[]>;
  getAllTariffsPublic(): Promise<TTariffPublic>;
  getTariffById(transTypeId: string, tariffId: UUID): Promise<TTariffPublic>;

  getContractorDispatcherTransports(
    data: ITripCalculateRequest,
    searchParams: ISearchDispatcherTransport
  ): Promise<IContractorDispatcherTransports>;
  getContractorDispatcherTransport(
    transportId: string,
    data: ITripCalculateRequest,
    searchParams: ISearchDispatcherTransport
  ): Promise<IContractorDispatcherTransport>;

  getAllCoopTrips(data: TripRequest): Promise<TTripCoop[]>;
  joinCoopTrip(sharedId: string, data: TTripRequestNew): Promise<TripRequest>;

  finishTripRequest(reqId: string): Promise<number>;

  saveFile(file: any, folder: UUID | string): Promise<SavedFileInfo>;
  saveConfirmSuburbTripFile(requestId: string | UUID, filesData: SavedFileInfo[]): Promise<number>;
  saveConfirmCardTripFile(requestId: string | UUID, filesData: SavedFileInfo[]): Promise<number>;
  saveTravelCardRequest(data: InnerCityTransportRequestInfo): Promise<number>;
  saveSuburbCompensation(data: InnerCityTransportRequestInfo): Promise<number>;
  saveCityTripCompensation(data: CityTripCompensation): Promise<number>;
  savePublicTripRequest(data: PublicTripCompensation): Promise<unknown>;
  loadCoopTripsSettings(organizationId: string): Promise<TCoopTripsSettings[]>;

  formatErrorWithRubles(error: string): string;
}

export type ITripServiceResponse = Record<string, any>;

export interface ITripCalculateRequest {
  distance: number;
  time: number;
  tripDate: number;
  startPoint: Record<string, any>;
  organizationId: string;
  engineVolume?: number;
  waitingTime: number;
  timeZone?: string;
  waypoints?: TWaypoint;
}

export type Transport = TaxiClassEnum | TransportTypeEnum;

export enum TaxiClassEnum {
  ECONOMY = 'ECONOMY',
  COMFORT = 'COMFORT',
  COMFORT_PLUS = 'COMFORT_PLUS',
  PERSONAL = 'PERSONAL',
  BUSINESS = 'BUSINESS',
  TAXI = 'TAXI',
  OFFICIAL = 'OFFICIAL',
  CARSHARING = 'CARSHARING',
  BICYCLE = 'BICYCLE',
  WALK = 'WALK',
  PUBLIC = 'PUBLIC',
  SCOOTER = 'SCOOTER',
  YANDEX = 'YANDEX',
  CITYMOBIL = 'CITYMOBIL',
  UBER = 'UBER',
  YANDEX_ECONOMY = 'YANDEX_ECONOMY',
  YANDEX_COMFORT = 'YANDEX_COMFORT',

  VIP_BUS = 'VIP_BUS',
  SMALL_BUS = 'SMALL_BUS',
  MIDDLE_BUS = 'MIDDLE_BUS',
  LARGE_BUS = 'LARGE_BUS',
  BUS = 'BUS', // stub for all buses

  GROUP_TRANSFER = 'GROUP_TRANSFER',
  TRANSFER = 'TRANSFER',
  TRANSFER_ECONOMY = 'TRANSFER_ECONOMY',
  TRANSFER_COMFORT = 'TRANSFER_COMFORT',
  TRANSFER_COMFORT_PLUS = 'TRANSFER_COMFORT_PLUS',
  TRANSFER_BUSINESS = 'TRANSFER_BUSINESS',
  TRANSFER_VIP = 'TRANSFER_VIP',
  TRANSFER_CAR_CHOICE = 'TRANSFER_CAR_CHOICE',
}

export enum TaxiClassTitlesEnum {
  TAXI = 'Такси',
  ECONOMY = 'Эконом',
  COMFORT = 'Комфорт',
  COMFORT_PLUS = 'Комфорт+',
  PERSONAL = 'Личный',
  BUSINESS = 'Бизнес',
  OFFICIAL = 'Служебный',
  CARSHARING = 'Каршеринг',
  BICYCLE = 'Велосипед',
  WALK = 'Пешком',
  PUBLIC = 'Общественный',
  SCOOTER = 'Самокат',
  YANDEX = 'Yandex Go',
  CITYMOBIL = 'Ситимобил',
  UBER = 'Uber',

  VIP_BUS = 'Автобус до 9 мест',
  SMALL_BUS = 'Автобус от 10 до 21 места',
  MIDDLE_BUS = 'Автобус от 22 до 41 мест',
  LARGE_BUS = 'Автобус от 42 до 55 мест',
  BUS = 'Автобус', // stub for all buses

  GROUP_TRANSFER = 'Трансфер',
  TRANSFER = 'Эконом',
  VIP = 'VIP',
  CHOOSING_CAR_TRANSFER = 'Выбор автомобиля',
}

export const busClasses = ['VIP_BUS', 'SMALL_BUS', 'MIDDLE_BUS', 'LARGE_BUS', 'BUS'];

export type TTaxiClass = keyof typeof TaxiClassEnum;

export enum TaxiClassCleanEnum {
  ECONOMY = 'ECONOMY',
  COMFORT = 'COMFORT',
  COMFORT_PLUS = 'COMFORT_PLUS',
  PERSONAL = 'PERSONAL',
  BUSINESS = 'BUSINESS',
}

export enum TransportTypeTitlesEnum {
  TAXI = 'Такси',
  PERSONAL = 'Личный',
  PUBLIC = 'Общественный',
  SCOOTER = 'Самокат',
  CARSHARING = 'Каршеринг',
  BICYCLE = 'Велосипед',
  WALK = 'Пешком',
  DEDICATED = 'Грузовик',
  COURIER = 'Курьер',
  INTERREGIONAL = 'Межрегиональная',
  PRIVATE = 'Частная',

  BUS = 'Автобус',

  GROUP_TRANSFER = 'Трансфер',
  TRANSFER = 'Трансфер',
}

export const TransportTypeOptions: LabeledValue<TransportTypeEnum>[] = [
  {
    label: TransportTypeTitlesEnum[TransportTypeEnum.TAXI],
    value: TransportTypeEnum.TAXI,
  },
  {
    label: TransportTypeTitlesEnum[TransportTypeEnum.PERSONAL],
    value: TransportTypeEnum.PERSONAL,
  },
  {
    label: TransportTypeTitlesEnum[TransportTypeEnum.PUBLIC],
    value: TransportTypeEnum.PUBLIC,
  },
  {
    label: TransportTypeTitlesEnum[TransportTypeEnum.CARSHARING],
    value: TransportTypeEnum.CARSHARING,
  },
  {
    label: TransportTypeTitlesEnum[TransportTypeEnum.GROUP_TRANSFER],
    value: TransportTypeEnum.GROUP_TRANSFER,
  },
  // {
  //   label: TransportTypeTitlesEnum[TransportTypeEnum.DEDICATED],
  //   value: TransportTypeEnum.DEDICATED,
  // },
];

export enum PersonalCarsEnum {
  PERSONAL = 'PERSONAL',
}

export enum PersonalCarsTitleEnum {
  PERSONAL = 'Личный',
}

export type PersonalCarsType = keyof typeof PersonalCarsEnum;

export const IOExternalPrices = t.partial({
  available: t.boolean,
  provider: t.string,
  tariffId: t.string,
  taxiClass: ioTypeFromEnum<TaxiClassEnum>('TaxiClassEnum', TaxiClassEnum),
  price: t.number,
  calcHash: t.string,
});

export type TExternalPrices = t.TypeOf<typeof IOExternalPrices>;

export interface ITaxi {
  name: string;
  transportType: TransportTypeEnum;
  taxiClass?: TTaxiClass;
  waitingTime: number;
  disabled?: boolean;
  info?: string;
  type?: LIMIT_TYPE;
  contractorId?: string;
  maxPassengers?: number;
  limitRest?: number;
  limitPercentage?: number;
}

export interface IPriceDetails {
  taxiClass?: string;
  minCancelTimeMin?: number;
  carSharingCompId?: string;
  carSharingCompName?: string;
}

export interface ITripTariff {
  id: string;
  cost: number;
  limitAvailable?: boolean | undefined;
  transportType: { id: string; name: TransportTypeEnum };
  taxiClass?: TTaxiClass;
  contractorId?: string;
  groupTransferClass?: GroupTransferEnum;
  bonusCost?: number | undefined;
  priceDetails?: IPriceDetails;
  limitPercentage?: number;
  limitAuthor?: string;
  outcomeTariffId?: string;
  outcomeCost?: number;
}

export interface ITripHistory {
  changeDate: number;
  code: number;
  initiator: string;
  status: TTripRequestStatuses;
}

export interface ICalculatedDispatcherTransport {
  id: string;
  cost: number;
  priceDetails: IPriceDetails;
  limitAvailable: boolean;
  outcomeTariffId: string;
}

export interface ITripsDispatcherTransport {
  start: string;
  end: string;
}

export interface IContentDispatcherTransport {
  id: string;
  brand: string;
  model: string;
  stateNumber: string;
  trips: ITripsDispatcherTransport[];
  calculated: ICalculatedDispatcherTransport;
  availableOnly: boolean;
}

export interface IContractorDispatcherTransports {
  content: IContentDispatcherTransport[];
}

export interface IContractorDispatcherTransport {
  trips: ITripsDispatcherTransport[];
  calculated: ICalculatedDispatcherTransport;
}

export interface ISearchDispatcherTransport {
  startDate?: number;
  endDate?: number;
  search?: string;
}

// Files

export const IOFileEmployee = t.partial({
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
});

// Tariffs

export const IOTariffBase = t.intersection([
  t.type({
    rideCostPerKm: t.number,
  }),
  t.partial({
    name: t.string,
    regionId: t.string,
    // Сомнительное место. Паша должен исправить.
    organizationId: t.string,
    transportType: ioTypeFromEnum<TransportTypeEnum>('TransportTypeEnum', TransportTypeEnum),
  }),
]);

export const IOTariffTaxi = t.intersection([
  IOTariffBase,
  t.type({
    taxiClass: ioTypeFromEnum<TaxiClassEnum>('TaxiClassEnum', TaxiClassEnum),
    rideCostPerMin: t.number,
    waitCostPerMin: t.number,
    freeWaitingTime: t.number,
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
    active: t.boolean,
    contractorId: t.string,
  }),
]);

export const IOTariffPriceDetails = t.partial({
  rewardForPassenger: t.number,
  season: t.partial({
    start: t.string,
    end: t.string,
  }),
});

export const IOTariffPersonal = t.intersection([
  IOTariffBase,
  t.type({
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

export const IOTariffPublicDetails = t.type({
  description: t.string,
});

export const IOTariffPublic = t.intersection([
  t.type({
    id: t.string,
    humanReadableId: t.string,
    active: t.boolean,
    regionId: t.string,
    transportType: ioTypeFromEnum<TransportTypeEnum>('TransportTypeEnum', TransportTypeEnum),
    serviceType: t.string,
    organizationId: t.string,
    metroTicketCost: tt.money,
    tramTicketCost: tt.money,
    trolleybusTicketCost: tt.money,
    busTicketCost: tt.money,
    metroAvailability: t.boolean,
    tramAvailability: t.boolean,
    trolleybusAvailability: t.boolean,
    busAvailability: t.boolean,
    cityLocalTrainAvailability: t.boolean,
    cityLocalTrainCost: tt.money,
    // priceDetails: Proxy
    travelCardAllCityTransportAvailability: t.boolean,
    travelCardAllCityTransportCost: tt.money,
    travelCardBusAvailability: t.boolean,
    travelCardBusCost: tt.money,
    travelCardLocalTrainAvailability: t.boolean,
    travelCardLocalTrainCost: tt.money,
    travelCardMetroAvailability: t.boolean,
    travelCardMetroCost: tt.money,
    travelCardTramAvailability: t.boolean,
    travelCardTramCost: tt.money,
    travelCardTrolleybusAvailability: t.boolean,
    travelCardTrolleybusCost: tt.money,
  }),
  t.partial({
    compensationType: ioTypeFromEnum<TransportCompensations>('TransportCompensations', TransportCompensations),
    // deprecated
    region: t.string,
  }),
]);

export type TTariffPublic = t.TypeOf<typeof IOTariffPublic>;

// TODO: Change to UUID

export const IOTripPurposeAttribute = t.intersection([
  t.type({
    name: t.string,
  }),
  t.partial({
    status: IOEmployeeStatusType,
    id: t.string,
  }),
]);

export const IOTripPurposeAttributes = t.type({
  attribute: IOTripPurposeAttribute,
});

export const IOTripPurposeDates = t.intersection([
  t.strict({
    // id: tt.uuid,
    id: t.string,
  }),
  t.partial({
    startDate: t.string,
    endDate: t.string,
  }),
]);

export const IOTripPurposeTimes = t.intersection([
  t.strict({
    // id: tt.uuid,
    id: t.string,
  }),
  t.partial({
    startTime: t.string,
    endTime: t.string,
  }),
]);

export enum Weekdays {
  MONDAY = 'MONDAY',
  TUESDAY = 'TUESDAY',
  WEDNESDAY = 'WEDNESDAY',
  THURSDAY = 'THURSDAY',
  FRIDAY = 'FRIDAY',
  SATURDAY = 'SATURDAY',
  SUNDAY = 'SUNDAY',
}

export enum WeekdaysTitle {
  MONDAY = 'Понедельник',
  TUESDAY = 'Вторник',
  WEDNESDAY = 'Среда',
  THURSDAY = 'Четверг',
  FRIDAY = 'Пятница',
  SATURDAY = 'Суббота',
  SUNDAY = 'Воскресенье',
}

export type WeekdaysType = keyof typeof Weekdays;

export const IOWeekdaysType = ioTypeFromEnum<WeekdaysType>('weekDays', Weekdays);

export const IOTripPurposeWeekdays = t.intersection([
  t.strict({
    // id: tt.uuid,
    id: t.string,
  }),
  t.partial({
    weekday: IOWeekdaysType,
  }),
]);

export const TripPurposeIconValues = [
  'PURPOSE_CM',
  'PURPOSE_V_ACCIDENT',
  'PURPOSE_V_GOV',
  'PURPOSE_V_GROUPS',
  'PURPOSE_V_DEBPTORS',
  'PURPOSE_GEMBA',
  'PURPOSE_D_CARD',
  'PURPOSE_D_VSP',
  'PURPOSE_DE_NIGHT',
  'PURPOSE_C_CMP',
  'PURPOSE_A_PROD',
  'PURPOSE_MV_VSP',
  'PURPOSE_C_CP',
  'PURPOSE_CM_VSP',
  'PURPOSE_CH_COLLATERAL',
  'PURPOSE_INVEST',
  'PURPOSE_CM_MMGN',
  'PURPOSE_M_CONTR',
] as const;
function keyObject<T extends readonly string[]>(arr: T): { [K in T[number]]: null } {
  return Object.fromEntries(arr.map(v => [v, null])) as any;
}
const TripPurposeIcon = t.keyof(keyObject(TripPurposeIconValues));
export type TripPurposeIcon = t.TypeOf<typeof TripPurposeIcon>;

export const TripPurpose = t.intersection([
  t.type({
    // id: tt.uuid,
    id: t.string,
    label: t.string,
  }),
  t.partial({
    icon: TripPurposeIcon,
    tripPurposeAttributes: t.array(IOTripPurposeAttributes),
    tripPurposeDates: t.array(IOTripPurposeDates),
    tripPurposeTimes: t.array(IOTripPurposeTimes),
    tripPurposeWeekdays: t.array(IOTripPurposeWeekdays),
  }),
]);

export type TripPurpose = t.TypeOf<typeof TripPurpose>;

export const IODeclineReason = t.type({
  reason: t.string,
  code: t.number,
});

export type IDeclineReason = t.TypeOf<typeof IODeclineReason>;

// Ratings

export interface IRequestRating {
  rating?: number;
  advantages?: string[];
  drawbacks?: string[];
  ratingComment?: string;
}

export const IORequestRating = t.partial({
  rating: t.number,
  advantages: t.array(t.string),
  drawbacks: t.array(t.string),
  ratingComment: t.string,
});

export type TRequestRating = t.TypeOf<typeof IORequestRating>;

// Suitable Trips

export const IOTripCancelReason = t.type({
  reason: t.string,
  code: t.number,
});

export type TripCancelReason = t.TypeOf<typeof IOTripCancelReason>;

export const IOTripStops = t.intersection([
  t.type({
    orderId: t.number,
    address: t.string,
    latitude: t.number,
    longitude: t.number,
    startTime: t.union([t.number, t.string]),
    endTime: t.union([t.number, t.string]),
    eventType: t.string,
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

const SharedTransportType = t.union([t.literal('TAXI'), t.literal('PERSONAL')]);
export type SharedTransportType = t.TypeOf<typeof SharedTransportType>;

export const IOTripCoop = t.type({
  id: t.string,
  passengers: t.number,
  employeePassengers: t.array(Employee),
  newOrdersIds: t.array(t.string),
  stops: t.array(IOTripStops),
  kpi: IOTripKpi,
  transportType: SharedTransportType,
});

export type TTripStops = t.TypeOf<typeof IOTripStops>;
export type TTripFromCoopStops = t.TypeOf<typeof IOTripFromCoopStops>;
export type TTripKpi = t.TypeOf<typeof IOTripKpi>;
export type TTripCoop = t.TypeOf<typeof IOTripCoop>;

const IOEmployeeWithoutOrg = t.intersection([
  Employee,
  t.partial({
    organisationId: t.string,
  }),
]);

export const IOTripFromCoop = t.type({
  magentaId: t.number,
  passengers: t.number,
  employeePassengers: t.array(IOEmployeeWithoutOrg),
  tariffId: t.string,
  active: t.boolean,
  stops: t.array(IOTripFromCoopStops),
  kpi: IOTripKpi,
});

export type TTripFromCoop = t.TypeOf<typeof IOTripFromCoop>;
export type TEmployeeWithoutOrg = t.TypeOf<typeof IOEmployeeWithoutOrg>;

export const TestFileType = t.strict({
  uid: t.string,
  name: t.string,
  status: t.string,
  response: t.string,
  url: t.string,
});

export type TestFileType = t.TypeOf<typeof TestFileType>;

export enum UsedFileFormatEnum {
  JPG = 'JPG',
  JPEG = 'JPEG',
  PNG = 'PNG',
  TIFF = 'TIFF',
  PDF = 'PDF',
  jpg = 'jpg',
  jpeg = 'jpeg',
  png = 'png',
  tiff = 'tiff',
  pdf = 'pdf',
  heif = 'heif',
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
  pdf = 'application/pdf',
  jpg = 'image/jpg',
  jpeg = 'image/jpeg',
  png = 'image/png',
  tiff = 'image/tiff',
  heic = 'heic',
  doc = 'application/msword',
  docx = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
  heif = 'heif',
}

export const SavedFileInfo = t.intersection([
  t.type({
    id: tt.uuid,
  }),
  t.partial({
    fileFormat: t.string,
    fileName: t.string,
    fileSize: t.number,
    folder: tt.uuid,
  }),
]);

export type SavedFileInfo = t.TypeOf<typeof SavedFileInfo>;

export const MinTariffTaxi = t.strict({
  cost: t.number,
  tariffId: t.string,
});

export type MinTariffTaxi = t.TypeOf<typeof MinTariffTaxi>;

export const InnerCityTransportRequestInfo = t.strict({
  author: Employee,
  passenger: Employee,
  transportType: ioTypeFromEnum<TransportTypeEnum>('TransportTypeEnum', TransportTypeEnum),
  desiredDate: t.number,
  expected: RequestRoute,
  segments: t.array(Segment),
  waypoints: t.array(IOWaypoint),
  purpose: TripPurpose,
  compensationType: ioTypeFromEnum<TransportCompensations>('TransportCompensations', TransportCompensations),
  compensationDocuments: t.array(SavedFileInfo),
});

export type InnerCityTransportRequestInfo = t.TypeOf<typeof InnerCityTransportRequestInfo>;

const TripData = t.strict({
  metroTicketsQuantity: t.number,
  tramTicketsQuantity: t.number,
  trolleybusTicketsQuantity: t.number,
  busTicketsQuantity: t.number,
});

export type TripData = t.TypeOf<typeof TripData>;

const tariffData = t.strict({
  metroTicketCost: t.number,
  tramTicketCost: t.number,
  trolleybusTicketCost: t.number,
  busTicketCost: t.number,
  metroAvailability: t.boolean,
  tramAvailability: t.boolean,
  trolleybusAvailability: t.boolean,
  busAvailability: t.boolean,
});

export const CityTripCompensation = t.strict({
  author: Employee,
  passenger: Employee,
  transportType: ioTypeFromEnum<TransportTypeEnum>('TransportTypeEnum', TransportTypeEnum),
  desiredDate: t.number,
  expected: RequestRoute,
  segments: t.array(Segment),
  waypoints: t.array(IOWaypoint),
  purpose: TripPurpose,
  compensationType: ioTypeFromEnum<TransportCompensations>('TransportCompensations', TransportCompensations),
  compensationDocuments: t.array(SavedFileInfo),

  tariffId: t.string,
  tripData: TripData,
  tariffData,
});

export type CityTripCompensation = t.TypeOf<typeof CityTripCompensation>;

export const CompensationDocument = t.type({
  id: t.string,
  folder: t.string,
  fileName: t.string,
  fileFormat: t.string,
  fileSize: t.number,
});

export const TransportCompensation = t.intersection([
  t.type({
    compensationType: tt.optional(AvailablePublicTTCompensationType),
    ticketsCost: tt.money,
  }),
  t.partial({
    id: t.string,
    transportType: AvailablePublicTransportType,
    ticketsCount: t.number,
    ticketsExpirationStart: t.string,
    ticketsExpirationEnd: t.string,
    attachedDocumentId: t.string,
    sum: t.number,
    quantity: t.number,
    cost: t.number,
    compensationDocumentDTO: CompensationDocument,
  }),
]);

export type TransportCompensation = t.TypeOf<typeof TransportCompensation>;

export const PublicTripCompensation = t.intersection([
  t.type({
    author: Employee,
    passenger: Employee,
    purpose: TripPurpose,
    transportCompensation: t.array(TransportCompensation),
  }),
  t.partial({
    expected: RequestRoute,
    segments: t.array(Segment),
    waypoints: t.array(IOWaypoint),
    transportType: ioTypeFromEnum<TransportTypeEnum>('TransportTypeEnum', TransportTypeEnum),
    desiredDate: t.number,
    compensationDocuments: t.array(SavedFileInfo),
    tariffId: t.string,
    timeZone: t.string,
    source: t.string,
    payRequestId: t.string,
    minTariffTaxi: MinTariffTaxi,
    commentForPurpose: t.string,
  }),
]);

export type PublicTripCompensation = t.TypeOf<typeof PublicTripCompensation>;

export const IOCoopTripsSettings = t.type({
  id: t.string,
  transportType: ioTypeFromEnum<TransportTypeEnum>('TransportTypeEnum', TransportTypeEnum),
  organizationId: t.string,
  economyIndicationYellowRangeLowerBorder: t.number,
  economyIndicationYellowRangeUpperBorder: t.number,
  settings: t.unknown,
});

export type TCoopTripsSettings = t.TypeOf<typeof IOCoopTripsSettings>;

const Sorted = t.type({
  sorted: t.boolean,
  unsorted: t.boolean,
  empty: t.boolean,
});

export const Pageable = t.type({
  offset: t.number,
  pageNumber: t.number,
  pageSize: t.number,
  paged: t.boolean,
  unpaged: t.boolean,
  sort: Sorted,
});

const SearchNumberTripContent = t.type({
  desiredDate: t.number,
  humanReadableId: t.string,
  id: t.string,
  status: t.string,
  timeZone: t.string,
});

export const IOSearchNumberTrip = t.type({
  content: t.array(SearchNumberTripContent),
  empty: t.boolean,
  first: t.boolean,
  last: t.boolean,
  number: t.number,
  numberOfElements: t.number,
  pageable: Pageable,
  size: t.number,
  sort: Sorted,
  totalElements: t.number,
  totalPages: t.number,
});

export type TSearchNumberTrip = t.TypeOf<typeof IOSearchNumberTrip>;

export const IODriverInfo = t.partial({
  driverName: t.string,
  driverPhone: t.string,
  registrationNumber: t.string,
  vehicleInfo: t.string,
});

export type DriverInfo = t.TypeOf<typeof IODriverInfo>;

const IOChildSeatDetails = t.type({
  group1: t.number,
  group2: t.number,
  booster: t.number,
  newborn: t.number,
});

export type ChildSeatDetailsnfo = t.TypeOf<typeof IOChildSeatDetails>;

export const IOInformation = t.intersection([
  t.type({
    bugsComment: t.string,
  }),
  t.partial({
    bugsComment: t.string,
    phoneHotel: t.string,
    childSeatDetails: IOChildSeatDetails,
    childSeat: t.boolean,
    bugsOversized: t.boolean,
    bugsOversizedComment: t.string,
    bugs: t.boolean,
    animal: t.boolean,
    animalComment: t.string,
    numberFlight: t.string,
    dateFlight: t.string,
    addContact: t.string,
    typeVehicle: t.string,
    clientFullName: t.string,
    clientInfoPhone: t.string,
    addContactFIO: t.string,
    addContactPhone: t.string,
    passengerCount: t.number,
  }),
]);

export type Information = t.TypeOf<typeof IOInformation>;
// Trip Requests

export const FraudCommentResponseSchema = t.intersection([
  t.type({
    text: t.string,
  }),
  t.partial({
    id: t.string,
    humanReadableId: t.string,
  }),
]);

export type FraudComment = t.TypeOf<typeof FraudCommentResponseSchema>;

export const TripRequest = t.intersection([
  IOHumanReadable,
  t.type({
    id: t.string,
    author: Employee,
    passenger: Employee,
    purpose: TripPurpose,
    passengerCount: t.number,
    desiredDate: t.number,
    creationTime: t.union([t.string, t.number]),
    coopTrip: t.boolean,
  }),
  t.partial({
    approvedBy: Employee,
    commentForDriver: t.string,
    humanReadableId: t.string,
    restOfLimit: t.number,
    transportType: ioTypeFromEnum<TransportTypeEnum>('TransportTypeEnum', TransportTypeEnum),
    status: TTripRequestStatuses,
    approvalState: ApprovalStateStatuses,
    fraud: t.partial({
      comment: t.string,
    }),
    fraudComment: t.array(FraudCommentResponseSchema),
    expected: RequestRoute,
    taxiClass: ioTypeFromEnum<TaxiClassEnum>('TaxiClassEnum', TaxiClassEnum),
    tariffId: t.string,
    requestRating: IORequestRating,
    requestOptions: t.array(t.string),
    passengerLimitRemainder: t.array(LimitSharing),
    magentaOrderId: t.number,
    sharedRideId: t.string,
    occupiedPlacesCount: t.number,
    personalCar: IOPersonalCar,
    compensationType: ioTypeFromEnum<TransportCompensations>('TransportCompensations', TransportCompensations),
    timeZone: t.string,
    requestPrice: t.number,
    transportCompensation: t.array(TransportCompensation),
    driverInfo: IODriverInfo,
    sharedRideOwner: t.boolean,
    statusCodeDescription: t.string,
    information: IOInformation,
    additionalSum: t.number,
    joinedPassengers: t.array(Employee),
    payRequestIds: t.array(t.string),
    commentForPurpose: t.string,
    costSharePart: t.number,
    outcomeTariffId: t.string,
    outcomeCost: t.number,
  }),
]);

export type TripRequest = t.TypeOf<typeof TripRequest>;

export type TTripRequestNew = Pick<TripRequest, 'author' | 'passenger' | 'transportType' | 'expected' | 'purpose'> &
  Partial<TripRequest>;

export const YandexTripRequest = t.type({
  tripDate: t.string,
  waypoints: t.array(IOWaypoint),
  purposeId: t.string,
  tariff: t.string,
});

export type YandexTripRequest = t.TypeOf<typeof YandexTripRequest>;

export const YandexTripResponse = t.type({
  assessments: t.unknown,
  comment: t.string,
  factCost: t.number,
  humanReadableId: t.string,
  id: t.string,
  link: t.string,
  passengerId: t.string,
  plannedCost: t.number,
  plannedDuration: t.string,
  purposeId: t.string,
  reason: t.string,
  receipt: t.string,
  status: t.string,
  tariff: t.string,
  tripDate: t.string,
  waypoints: t.array(IOWaypoint),
});

export type YandexTripResponse = t.TypeOf<typeof YandexTripResponse>;

// Subclasses for transports

export enum TaxiEnum {
  ECONOMY = 'ECONOMY',
  COMFORT = 'COMFORT',
  COMFORT_PLUS = 'COMFORT_PLUS',
  BUSINESS = 'BUSINESS',
}

export enum GroupTransferClassesEnum {
  TRANSFER = 'TRANSFER',
  TRANSFER_CAR_CHOICE = 'TRANSFER_CAR_CHOICE',
}

export enum GroupTransferEnum {
  GROUP_TRANSFER = 'GROUP_TRANSFER',
  TRANSFER = 'TRANSFER',
}

export enum TaxiEnumTitle {
  ECONOMY = 'Эконом',
  COMFORT = 'Комфорт',
  COMFORT_PLUS = 'Комфорт+',
  BUSINESS = 'Бизнес',
}

export enum BusEnum {
  VIP_BUS = 'VIP_BUS',
  SMALL_BUS = 'SMALL_BUS',
  MIDDLE_BUS = 'MIDDLE_BUS',
  LARGE_BUS = 'LARGE_BUS',
}

export enum BusEnumTitle {
  VIP_BUS = 'Автобус до 9 мест',
  SMALL_BUS = 'Автобус от 10 до 21 места',
  MIDDLE_BUS = 'Автобус от 22 до 41 мест',
  LARGE_BUS = 'Автобус от 42 до 55 мест',
}

export enum ExternalProviderEnum {
  YANDEX = 'YANDEX',
  CITYMOBIL = 'CITYMOBIL',
  UBER = 'UBER',
}

export enum ExternalTaxiEnumTitle {
  YANDEX = 'Yandex Go',
  CITYMOBIL = 'Ситимобил',
  UBER = 'Uber',
}

export type SubClass = TaxiEnum | BusEnum | GroupTransferClassesEnum;

export type TaxiType = keyof typeof TaxiEnum;
export type BusType = keyof typeof BusEnum;
export type ExternalTaxiType = keyof typeof ExternalProviderEnum;

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

export interface IEmployee {
  firstName: string;
  humanReadableId: string;
  id: string;
  lastName: string;
  mobilePhone: string;
  patronymic: string;
  personnelNumber: string;
  positionId: string;
  positionName: string;
  userId: string;
}

export interface IRequestsModal extends EmployeeModel {
  car?: ICar;
  sharedRideOwner?: boolean;
  employee?: IEmployee;
  commentForDriver?: string;
  statusDescription?: string;
  statusCode?: number;
}

export interface IRequestsSharedModal extends Omit<EmployeeModel, 'status'> {
  car?: ICar;
  sharedRideOwner?: boolean;
  employee?: IEmployee;
  commentForDriver?: string;
  statusDescription?: string;
  statusCode?: number;
  status?: string;
}

export interface IRequestsPassengers {
  firstName: string;
  humanReadableId: string;
  id: string;
  lastName: string;
  mobilePhone: string;
  patronymic: string;
  personnelNumber: string;
  positionId: string;
  positionName: string;
  userId: string;
}

export interface ITimedTariffParams {
  coefWorkDayMorning: number;
  coefWorkDayNoon: number;
  coefWorkDayEvening: number;
  coefWorkDayNight: number;
  coefDayOff: number;
}

export interface ITarrifCarsharing {
  active: boolean;
  coefCasko: number;
  coefChildSeat: number;
  coefPetTransport: number;
  coefTraffic: number;
  contractId: string;
  contractNumber: string;
  contractorId: string;
  humanReadableId: string;
  id: string;
  organizationId: string;
  priceDetails: IPriceDetails;
  region: string;
  regionId: string[];
  rideCostPerKm: number;
  rideCostPerMin: number;
  serviceType: string;
  timedTariffParams: ITimedTariffParams;
  transportType: TransportTypeEnum;
  waitCostPerMin: number;
}

export interface IContractors {
  contractorId: string;
  email: string;
  firstName: string;
  humanReadableId: string;
  id: string;
  lastName: string;
  patronymic: string;
  phone: string;
}

export interface IContractors {
  autoassign: boolean;
  contactPersonInfo: string;
  contactPersonPhone: string;
  digitId: number;
  humanReadableId: string;
  id: string;
  integrationType: string;
  mainDispatcher: IContractors;
  msrn: string;
  name: string;
  regionIds: string[];
  tin: string;
}

export interface IGroupTransferInformation {
  information: Information | any;
  commentForPurpose: string | undefined;
  choosingTime: Moment | null;
  passengerCount: number;
}

export interface ILocation {
  longitude: number;
  latitude: number;
}

export interface ICarActualInfo {
  requestStatus: string;
  location: ILocation;
  duration: number;
}
