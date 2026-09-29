import {
  Employee, EmployeeModel, IOHumanReadable, IOPersonalCar
} from '@sber-sbertransport/mf-core';
import * as t from 'io-ts';
import { Dictionary } from 'lodash';

import { TTripRequestStatuses } from 'constants/TripRequestStatuses.constants';
import { EmployeeStatus, IOEmployeeStatusType } from 'constants/constants.app';

import { IOWaypoint, RequestRoute, Segment } from 'shared/models/geo/types';
import { ApprovalStateStatuses } from 'shared/models/types';
import { DelegateModel } from 'stores/Delegates/Delegates.interface';
import { LIMIT_TYPE, LimitSharing } from 'stores/Limits/Limit.interface';
import { TransportCompensations, TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

import * as tt from 'utils/io-ts';
import { UUID } from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { LabeledValue } from 'utils/Types';

import { AvailablePublicTTCompensationType, AvailablePublicTransportType } from '../../api/trip-requests';

import { TripRequestModel } from './models';
import { TripPaginationModel } from './models/TripPagination.model';
import { TripSuitableModel } from './models/TripSuitable.model';

export interface ITripStore {
  classCosts: ITripTariff[];
  externalPrices: TExternalPrices[];
  personalCarsCosts: ITripTariff[];
  selfEmployee?: EmployeeModel;
  currentTripRequest?: TripRequestModel | null;
  currentTripRequestId?: string | null;
  tripRequestList: TripRequestModel[];
  upcomingApplicationsList: TripRequestModel[];
  pagination: TripPaginationModel;
  savedSuccessfully: boolean;
  purposes: TripPurpose[];
  tariffsTaxi: TTariffTaxi[];
  tariffsPersonal: TTariffPersonal[];
  tariffsPublic?: TTariffPublic;
  purposesMapped: Dictionary<TripPurpose>;
  delegatesByEmployeeId: Record<string, DelegateModel[]>;
  coopTripsSettings: TCoopTripsSettings[];
  suitableCooperativeTrips: TripSuitableModel[];
  nonTerminalTotalElements: number | null;
  isSearchingSuitableTrip: boolean;
  isSearchedSuitableTrip: boolean;
  actualTariff?: TTariffPublic;
  tripStatusChanged?: boolean;
  vehicles?: Vehicle[];

  getCosts(data: ITripCalculateRequest): Promise<void>;
  getPersonalCarCost(data: ITripCalculateRequest): Promise<ITripTariff[] | undefined>;
  getTripRequestById(id: string): TripRequestModel | undefined;
  getExternalPrices(data: TTripRequestNew): Promise<void>;

  loadRequestList(): Promise<void>;
  loadRequestListTerminal(data: ITripRequestData): Promise<void>;
  loadRequestListNonTerminal(data: ITripRequestData): Promise<void>;
  loadUpcomingApplicationsList(data: ITripRequestData): Promise<void>;
  loadPurposesList(orgId: string): Promise<void>;
  loadCoopTripsSettings(organizationId: string): Promise<void>;
  loadActualTariff(transTypeId: string, tariffId: UUID): Promise<void>;
  loadCountActiveApprovals(): void;

  saveTripRequest(data: TTripRequestNew): Promise<void>;
  saveFile(file: File, folder: UUID | string): Promise<SavedFileInfo>;
  saveConfirmSuburbTripFile(requestId: string | UUID, filesData: SavedFileInfo[]): Promise<number>;
  saveConfirmCardTripFile(requestId: string | UUID, filesData: SavedFileInfo[]): Promise<number>;
  saveTravelCardRequest(data: InnerCityTransportRequestInfo): Promise<number>;
  saveSuburbCompensation(data: InnerCityTransportRequestInfo): Promise<number>;
  saveCityTripCompensation(data: CityTripCompensation): Promise<number>;
  savePublicTripRequest(data: PublicTripCompensation): Promise<number>;

  clearCoopTrips(): void;
  clearRequestList(): void;
  clearCurrentRequest(): void;
  clearClassCosts(): void;
  clearPersonalCarsCosts(): void;

  initStore(): void;

  //

  rateTripRequest(data: IRequestRating, requestId: string): Promise<void>;
  setCurrentTripRequest(reqId: string): void;

  searchCoopTrips(requestTrip: TTripRequestNew): Promise<void>;

  updateTripStateById({ id, newState }: { id: string; newState: Partial<TripRequestModel> }): void;

  joinCoopTrip(sharedId: string, data: TTripRequestNew): Promise<void>;
  addPersonalCost(tariff: ITripTariff): void;

  finishTripRequest(id: string): Promise<void>;
}
export interface ITripRequestData {
  pageSetting: { page: number; size: number };
  desiredDate?: { start?: number | null; end?: number | null };
  creationDate?: { start?: number | null; end?: number | null };
  page?: { sort: { sorted: boolean } };
  sortSetting?: { directionAsc: boolean };
  transportTypeEnum?: TransportTypeEnum;
  transportTypeSet?: TransportTypeEnum[];
}

export interface ITripService {
  calculateTripCost: (
    data: ITripCalculateRequest,
    callBack: (transportTypeId: string, tariffId: UUID) => {}
  ) => Promise<ITripTariff[]>;
  calculateCostForPersonalCarTrip: (data: ITripCalculateRequest) => Promise<ITripTariff[]>;
  calculateExternalPrices: (data: TTripRequestNew) => Promise<TExternalPrices[]>;

  saveTripRequest(data: TTripRequestNew): Promise<TripRequest | string>;

  editTripRequest(data: TTripRequestNew, requestId: string): Promise<number>;
  rateTripRequest(data: IRequestRating, requestId: string): Promise<number>;
  getTripRequestList(passengerId: string): Promise<TripRequest[]>;
  getTripRequestListNonTerminal(data: ITripRequestData): Promise<TripPaginationModel & { content: TripRequest[] }>;
  getTripRequestListTerminal(data: ITripRequestData): Promise<TripPaginationModel & { content: TripRequest[] }>;
  getTripRequest(requestId: string): Promise<TripRequest>;

  getAllPurposesByEmployee(orgId: string): Promise<TripPurpose[]>;
  getAllTariffsTaxi(): Promise<TTariffTaxi[]>;
  getAllTariffsPersonal(): Promise<TTariffPersonal[]>;
  getAllTariffsPublic(): Promise<TTariffPublic>;
  getTariffById(transTypeId: string, tariffId: UUID): Promise<TTariffPublic>;
  getCountActiveApprovals(transTypeId: string, tariffId: UUID): Promise<TCountActiveApprovals>;

  getAllCoopTrips(data: TripRequest): Promise<TTripCoop[]>;
  joinCoopTrip(sharedId: string, data: TTripRequestNew): Promise<TripRequest>;

  getUserAvatar(userId: string | undefined): string | PromiseLike<string>;
  setUserAvatar(userId: string | undefined, fileData: File): boolean | PromiseLike<boolean>;

  getInstructions(): Promise<any>;

  finishTripRequest(reqId: string): Promise<number>;

  saveFile(file: any, folder: UUID | string): Promise<SavedFileInfo>;
  saveConfirmSuburbTripFile(requestId: string | UUID, filesData: SavedFileInfo[]): Promise<number>;
  saveConfirmCardTripFile(requestId: string | UUID, filesData: SavedFileInfo[]): Promise<number>;
  saveTravelCardRequest(data: InnerCityTransportRequestInfo): Promise<number>;
  saveSuburbCompensation(data: InnerCityTransportRequestInfo): Promise<number>;
  saveCityTripCompensation(data: CityTripCompensation): Promise<number>;
  savePublicTripRequest(data: PublicTripCompensation): Promise<number>;
  loadCoopTripsSettings(organizationId: string): Promise<TCoopTripsSettings[]>;
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
  intermediateWaitingTime: number;
}

export type Transport = TaxiClassEnum | TransportTypeEnum;

export enum TaxiClassEnum {
  ECONOMY = 'ECONOMY',
  COMFORT = 'COMFORT',
  COMFORT_PLUS = 'COMFORT_PLUS',
  PERSONAL = 'PERSONAL',
  BUSINESS = 'BUSINESS',
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
}

export enum TaxiClassTitlesEnum {
  TAXI = 'Такси',
  ECONOMY = 'Эконом',
  COMFORT = 'Комфорт',
  COMFORT_PLUS = 'Комфорт+',
  PERSONAL = 'Личный',
  BUSINESS = 'Бизнес',
  CARSHARING = 'Каршеринг',
  BICYCLE = 'Велосипед',
  WALK = 'Пешком',
  PUBLIC = 'Общественный',
  SCOOTER = 'Самокат',
  YANDEX = 'Yandex Go',
  CITYMOBIL = 'Ситимобил',
  UBER = 'Uber',
  OFFICIAL = 'Служебный',
  VIP_BUS = 'Автобус до 9 мест',
  SMALL_BUS = 'Автобус от 10 до 21 места',
  MIDDLE_BUS = 'Автобус от 22 до 41 мест',
  LARGE_BUS = 'Автобус от 42 до 55 мест',
}

export type TTaxiClass = keyof typeof TaxiClassEnum;

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
    label: TransportTypeTitlesEnum[TransportTypeEnum.DEDICATED],
    value: TransportTypeEnum.DEDICATED,
  },
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
}

export interface IPriceDetails {
  taxiClass?: string;
  minCancelTimeMin?: number;
  carSharingCompId?: string;
  carSharingCompName: string;
}

export interface ITripTariff {
  id: string;
  cost: number;
  limitAvailable?: boolean | undefined;
  transportType: { id: string; name: TransportTypeEnum };
  taxiClass?: TTaxiClass;
  contractorId?: string;

  bonusCost?: number | undefined;
  priceDetails?: IPriceDetails;
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

export const TripPurpose = t.intersection([
  t.type({
    // id: tt.uuid,
    id: t.string,
    label: t.string,
  }),
  t.partial({
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

// Trip Requests

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
    approvedBy: Employee,
    coopTrip: t.boolean,
  }),
  t.partial({
    commentForDriver: t.string,
    humanReadableId: t.string,
    restOfLimit: t.number,
    transportType: ioTypeFromEnum<TransportTypeEnum>('TransportTypeEnum', TransportTypeEnum),
    status: TTripRequestStatuses,
    approvalState: ApprovalStateStatuses,
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
  }),
]);

export type TripRequest = t.TypeOf<typeof TripRequest>;

export type TTripRequestNew = Pick<TripRequest, 'author' | 'passenger' | 'transportType' | 'expected' | 'purpose'> &
  Partial<TripRequest>;

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

export const IOTripCoop = t.type({
  id: t.string,
  passengers: t.number,
  employeePassengers: t.array(Employee),
  newOrdersIds: t.array(t.string),
  stops: t.array(IOTripStops),
  kpi: IOTripKpi,
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
  DOC = 'DOC',
  DOCX = 'DOCX',
  HEIC = 'HEIC',
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

export const SavedFileInfo = t.strict({
  fileFormat: ioTypeFromEnum<UsedFileFormatEnum>('UsedFileFormatEnum', UsedFileFormatEnum),
  fileName: t.string,
  fileSize: t.number,
  folder: tt.uuid,
  id: tt.uuid,
});

export type SavedFileInfo = t.TypeOf<typeof SavedFileInfo>;

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

export const TransportCompensation = t.intersection([
  t.type({
    compensationType: AvailablePublicTTCompensationType,
    ticketsCost: tt.money,
  }),
  t.partial({
    transportType: AvailablePublicTransportType,
    ticketsCount: t.number,
    ticketsExpirationStart: t.string,
    ticketsExpirationEnd: t.string,
    attachedDocumentId: t.string,
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

export const IOCountActiveApprovals = t.type({
  count: t.number,
});

export type TCountActiveApprovals = t.TypeOf<typeof IOCountActiveApprovals>;

enum Ownership {
  USER = 'USER', // в собственности пользователя"
  SPOUSE = 'SPOUSE', // в собственности супруга/супруги пользователя
  THIRD_PARTY = 'THIRD_PARTY', // в собственности третьих лиц
}

const IOFile = t.partial({
  fileName: t.string,
  fileSize: t.number,
  fileFormat: t.string,
});

const IODriverLic = t.intersection([
  t.type({
    id: tt.uuid,
    employeeId: tt.uuid,
    number: t.string,
    seria: t.string,
    issue: t.string,
    placeIssue: t.string,
    categoria: t.string,
    issueDateDocument: t.string,
    finalTimeDocument: t.string,
  }),
  IOFile,
]);

const IOOSago = t.intersection([
  t.type({
    id: tt.uuid,
    employeeId: tt.uuid,
    seria: t.string,
    number: t.string,
    startTimeDocument: t.string,
    finalTimeDocument: t.string,
  }),
  IOFile,
]);

const IOPassportTS = t.intersection([
  t.type({
    id: tt.uuid,
    employeeId: tt.uuid,
    vin: t.string,
    engineVolume: t.number,
    enginePower: t.string,
  }),
  IOFile,
]);

const IOMarriageCert = t.intersection([
  t.type({
    id: tt.uuid,
    employeeId: tt.uuid,
    number: t.string,
    seria: t.string,
    issueDateDocument: t.string,
  }),
  IOFile,
]);

const IOAgreementPdn = t.intersection([
  t.type({
    id: tt.uuid,
    carId: tt.uuid,
    employeeId: tt.uuid,
  }),
  IOFile,
]);

const IODocuments = t.partial({
  driverLic: IODriverLic,
  osago: IOOSago,
  passportTs: IOPassportTS,
  marriageCertificate: IOMarriageCert,
  agreementPdn: IOAgreementPdn,
});

const IOVehicle = t.intersection([
  t.type({
    id: t.string,
    color: t.string,
    transportType: t.string,
    brandName: t.string,
    model: t.string,
    registrationNumber: t.string,
    engineVolume: t.number,
    insuranceNumber: t.string,
    passengerSeatsCount: t.number,
    ownerInfo: ioTypeFromEnum<Ownership>('Ownership', Ownership),
    persDataAccept: t.boolean,
    documents: IODocuments,
  }),
  t.partial({
    employeeId: t.string,
    registrationCertificate: t.string,
  }),
]);

export type Vehicle = t.TypeOf<typeof IOVehicle>;
