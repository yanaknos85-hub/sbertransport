import * as t from 'io-ts';
import { Purpose, Passenger, RequestRating } from 'stores/Trip/Trip.interface';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import { Route, FactRoute } from 'stores/Geo/Geo.interface';
import { Value } from 'shared/components/DateInput/types';
import { UUID } from 'utils/io-ts';
import { RangeNumber } from 'api/register-search';
import { DateRangeISO } from '../PublicRegistry/models/PublicRegistry.interface';

export const PageSettings = t.type({
  page: t.number,
  size: t.number,
});
export type PageSettings = t.TypeOf<typeof PageSettings>;

enum ItinerantType {
  FULL = 'FULL',
  PARTIAL = 'PARTIAL',
}

interface SortSetting {
  property: string;
  directionAsc: boolean;
}

const Sort = t.partial({
  sorted: t.boolean,
  unsorted: t.boolean,
  empty: t.boolean,
});

const Pageable = t.type({
  offset: t.number,
  sort: Sort,
  pageNumber: t.number,
  pageSize: t.number,
  paged: t.boolean,
  unpaged: t.boolean,
});

export const Waypoints = t.partial({
  country: tt.nullable(t.string),
  region: tt.nullable(t.string),
  city: tt.nullable(t.string),
  street: tt.nullable(t.string),
  house: tt.nullable(t.string),
  building: tt.nullable(t.string),
  structure: tt.nullable(t.string),
  existInVspGosbTbRegistry: tt.nullable(t.boolean),
  checkinAutomatic: tt.nullable(t.boolean),
  checkinManual: tt.nullable(t.boolean),
});

export type Waypoints = t.TypeOf<typeof Waypoints>;

const Segment = t.type({
  cost: t.number,
  distance: t.number,
  time: t.number,
  coordinates: t.array(t.type({ latitude: t.number, longitude: t.number })),
});

export enum OwnerInfo {
  USER = 'USER',
  THIRD_PARTY = 'THIRD_PARTY',
  SPOUSE = 'SPOUSE',
}

export const OwnerInfoDescription: Record<OwnerInfo, string> = {
  USER: 'В собственности пользователя',
  THIRD_PARTY: 'В собственности третьих лиц',
  SPOUSE: 'В собственности супруга/супруги пользователя',
};

export type PaymentPeriods = 1 | 2 | 3 | 4;

export const PersonalCar = t.intersection([
  t.type({
    id: tt.nullable(t.string),
  }),
  t.partial({
    brandName: tt.nullable(t.string),
    engineVolume: tt.nullable(t.number),
    insuranceNumber: tt.nullable(t.string),
    model: tt.nullable(t.string),
    ownerInfo: tt.nullable(ioTypeFromEnum<OwnerInfo>('OwnerInfo', OwnerInfo)),
    registrationCertificate: tt.nullable(t.string),
    registrationNumber: tt.nullable(t.string),
  }),
]);

export type PersonalCar = t.TypeOf<typeof PersonalCar>;

t.union([
  Route,
  t.partial({
    segments: tt.nullable(t.array(Segment)),
    distance: tt.nullable(t.number),
    time: tt.nullable(t.number),
  }),
]);
const PositionInfo = t.partial({
  id: tt.nullable(tt.uuid),
  organizationId: tt.nullable(tt.uuid),
  positionName: tt.nullable(t.string),
});

export const Expected = t.type({
  cost: tt.money,
  distance: t.number,
  time: t.number,
  segments: t.array(Segment),
  waypoints: t.array(Waypoints),
  waypointsCountWithCheckIn: t.number,
  waypointsCountWithoutCheckIn: t.number,
  waypointsCount: t.number,
});

export type Expected = t.TypeOf<typeof Expected>;

export const SumRange = t.partial({
  start: tt.money,
  end: tt.money,
});

export type SumRange = t.TypeOf<typeof SumRange>;

export const ValueRange = t.partial({
  start: t.number,
  end: t.number,
});

export type ValueRange = t.TypeOf<typeof ValueRange>;

export const DistanceRange = t.partial({
  start: t.number,
  end: t.number,
});

export type DistanceRange = t.TypeOf<typeof DistanceRange>;

export enum SortFields {
  PASSENGER_FULL_NAME = 'PASSENGER_FULL_NAME',
  REQUEST_HUMAN_ID = 'REQUEST_HUMAN_ID',
  DESIRED_DATE = 'DESIRED_DATE',
  EXPECTED_COST = 'EXPECTED_COST',
  REQUEST_ID = 'REQUEST_ID',
  CREATION_DATE = 'CREATION_DATE',
}

export const User = t.type({
  lastName: t.string,
  firstName: t.string,
  patronymic: t.string,
  id: tt.uuid,
});

export type User = t.TypeOf<typeof User>;

export const UsersAttributes = t.partial({
  id: tt.uuid,
  user: User,
  taxiUIVisibility: t.record(t.string, t.boolean),
  personalUIVisibility: t.record(t.string, t.boolean),
  publicUIVisibility: t.record(t.string, t.boolean),
});

export type UsersAttributes = t.TypeOf<typeof UsersAttributes>;

export const SessionSortProperties = {
  SortProperty: 'sortProperty',
  DirectionAsc: 'directionAsc',
};

export const SortSettings = t.type({
  property: t.string,
  directionAsc: t.boolean,
});

export type SortSettings = t.TypeOf<typeof SortSettings>;

const CoopTrip = t.type({
  value: t.number,
  label: t.string,
});

export type CoopTrip = t.TypeOf<typeof CoopTrip>;

export interface PersonalSearchQuery {
  requestHumanId?: UUID;
  coopTrip?: CoopTrip;
  requestStatusSet?: string[];
  paymentPeriod?: PaymentPeriods;
  economyPercent?: number | RangeNumber;
  passengerCountSet?: number[];
  passenger?: boolean;
  tariffIdSet?: string[];
  expectedDistance: number | RangeNumber;
  ratingMarkSet: number[];
  creationDate?: Value;
  orderPaymentFormationStartRange?: Value;
  costCenter?: string;
  employeeFIO?: string;
  personnelNumber?: string;
  sortSetting?: SortSetting;
  pageSetting?: PageSettings;
}

export const PersonalReportKpi = t.type({
  totalCost: t.number,
  totalDistanceKm: t.number,
  totalTimeMin: t.number,
});

export type PersonalReportKpi = t.TypeOf<typeof PersonalReportKpi>;

export const TripRequestReport = t.intersection([
  t.type({
    id: tt.uuid,
    humanReadableId: tt.nullable(t.string),
    passenger: tt.nullable(Passenger),
    author: tt.nullable(Passenger),
    transportType: ioTypeFromEnum<TransportTypes>('TransportTypeEnum', TransportTypes),
    creationTime: tt.nullable(t.number),
    status: tt.nullable(t.string),
    position: tt.nullable(PositionInfo),
    purpose: tt.nullable(Purpose),
    expected: Expected,
  }),
  t.partial({
    department: tt.nullable(
      t.intersection([
        t.strict({ id: tt.uuid }),
        t.partial({ departmentName: t.string }),
        t.partial({ code: t.string }),
      ])
    ),
    desiredDate: tt.nullable(t.number),
    creationDate: tt.nullable(t.number),
    orderPaymentFormationStartDate: tt.nullable(t.union([t.string, t.number])),
    approveDate: tt.nullable(t.number),

    humanReadableLimitId: tt.nullable(t.string),
    costCenter: tt.nullable(t.string),
    coopTrip: tt.nullable(t.boolean),
    passengers: tt.nullable(t.array(Passenger)),
    itinerantType: tt.nullable(ioTypeFromEnum<ItinerantType>('ItinerantType', ItinerantType)),
    personalCar: tt.nullable(PersonalCar),
    factData: tt.nullable(FactRoute),
    magentaOrderId: tt.nullable(t.number),
    sharedRideId: tt.nullable(t.string),
    passengerCount: t.number,
    paymentDataList: t.array(t.partial({ paymentTypeCode: t.string, paymentPrice: t.number })),
    paymentQuarter: tt.nullable(t.number),
    departmentName: tt.nullable(t.string),
    kpi: tt.nullable(PersonalReportKpi),
    requestRating: tt.nullable(RequestRating),
    paymentTime: tt.nullable(t.string),

    sharedRideOwner: tt.nullable(t.boolean),
    paymentCost: tt.nullable(t.number),
    paymentPeriod: tt.nullable(t.number),
  }),
]);

export type TripRequestReport = t.TypeOf<typeof TripRequestReport>;

export const PersonalSearchResponse = t.type({
  totalElements: t.number,
  totalPages: t.number,
  size: t.number,
  number: t.number,
  sort: Sort,
  first: t.boolean,
  pageable: Pageable,
  numberOfElements: t.number,
  last: t.boolean,
  empty: t.boolean,
  content: t.array(TripRequestReport),
});

export type PersonalSearchResponse = t.TypeOf<typeof PersonalSearchResponse>;

export const PersonalUIVisibilityDTO = t.partial({
  requestIdVisible: t.boolean,
  requestStatusVisible: t.boolean,
  passengerFioVisible: t.boolean,
  tripPurposeVisible: t.boolean,
  creationTimeVisible: t.boolean,
  tripFactStartTimeVisible: t.boolean,
  tripTypeVisible: t.boolean,
  tripFactPriceVisible: t.boolean,
  mvzVisible: t.boolean,
  approveDateVisible: t.boolean,
  actualRangeVisible: t.boolean,
  kkPersonalNumberVisible: t.boolean,
  sharedRideIdVisible: t.boolean,
  waypointFromVisible: t.boolean,
  waypointToVisible: t.boolean,
  intermediateAddressVisible: t.boolean,
  passengerDepartmentOneVisible: t.boolean,
  passengerDepartmentTwoVisible: t.boolean,
  passengerDepartmentThreeVisible: t.boolean,
  passengerDepartmentFourVisible: t.boolean,
  passengerDepartmentFiveVisible: t.boolean,
  passengerDepartmentSixVisible: t.boolean,
  itinerantTypeVisible: t.boolean,
  ownershipOfCarVisible: t.boolean,
  marriageCertificateNumberVisible: t.boolean,
  carRegistrationNumberVisible: t.boolean,
  carBrandNameVisible: t.boolean,
  carEngineVolumeVisible: t.boolean,
  osagoNumberVisible: t.boolean,
  waypointsCountVisible: t.boolean,
  waypointsCountWithCheckInVisible: t.boolean,
  paymentPeriodVisible: t.boolean,

  paymentCostVisible: t.boolean,
  sharedRideOwnerVisible: t.boolean,
});

export type PersonalUIVisibilityDTO = t.TypeOf<typeof PersonalUIVisibilityDTO>;

export const Filters = t.intersection([
  t.type({
    pageSetting: t.type({ page: t.number, size: t.number }),
  }),
  t.partial({
    requestHumanId: t.string,
    coopTrip: t.boolean,
    requestStatusSet: t.array(t.string),
    paymentPeriod: t.number,
    economyPercent: RangeNumber,
    passengerCountSet: t.array(t.number),
    passenger: t.boolean,
    tariffIdSet: t.array(t.string),
    expectedDistance: DistanceRange,
    ratingMarkSet: t.array(t.number),
    creationDate: DateRangeISO,
    orderPaymentFormationStartRange: DateRangeISO,
    costCenter: t.string,
    employeeFIO: t.string,
    personnelNumber: t.string,
    sortSetting: SortSettings,
    withView: t.boolean,
    personalUIVisibilityDTO: PersonalUIVisibilityDTO,
    sharedRideId: t.string,
    empty: t.boolean,
    organizationId: tt.uuid,
  }),
]);

export type PersonalRegistryFilters = t.TypeOf<typeof Filters>;

export interface SearchedPersonalCoopTrip { sharedRideId: string; trips: TripRequestReport[] }

export type SearchedPersonalCoopTrips = SearchedPersonalCoopTrip[];
