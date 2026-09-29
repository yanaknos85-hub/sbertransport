import * as t from 'io-ts';
import { TransportTypes } from 'stores/TransportTypes/TransportTypes.interface';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { Waypoint, FactRoute } from '../../Geo/Geo.interface';
import { Passenger, RequestRating, TaxiClass } from '../../Trip/Trip.interface';

enum ItinerantType {
  FULL = 'FULL',
  PARTIAL = 'PARTIAL',
}

const Sorted = t.type({
  sorted: t.boolean,
  unsorted: t.boolean,
  empty: t.boolean,
});

export const DateRange = t.strict({
  start: tt.EpochMS,
  end: tt.EpochMS,
});

export const DateRangeISO = t.strict({
  start: t.string,
  end: t.string,
});

export type DateRangeISO = t.TypeOf<typeof DateRangeISO>;

export const Pageable = t.type({
  offset: t.number,
  pageNumber: t.number,
  pageSize: t.number,
  paged: t.boolean,
  unpaged: t.boolean,
  sort: Sorted,
});
export const InfoPassenger = t.intersection([
  t.strict({
    id: t.string,
    humanReadableId: tt.nullable(t.string),
    firstName: tt.nullable(t.string),
    lastName: tt.nullable(t.string),
    patronymic: tt.nullable(t.string),
  }),
  t.partial({
    personnelNumber: tt.nullable(t.string),
    itinerantType: tt.nullable(t.string),
  }),
]);
export const SumRange = t.strict({
  start: tt.money,
  end: tt.money,
});

export const WaypointsInfo = t.partial({
  country: tt.nullable(t.string),
  region: tt.nullable(t.string),
  city: tt.nullable(t.string),
  street: tt.nullable(t.string),
  house: tt.nullable(t.string),
  building: tt.nullable(t.string),
  structure: tt.nullable(t.string),
  checkinManual: tt.nullable(t.boolean),
  checkinAutomatic: tt.nullable(t.boolean),
  existInVspGosbTbRegistry: tt.nullable(t.boolean),
  waitTime: tt.nullable(t.number),
});

export const Purpose = t.type({
  id: t.string,
  label: t.string,
});

export type Purpose = t.TypeOf<typeof Purpose>;

export const Expected = t.type({
  cost: tt.money,
  distance: t.number,
  time: t.number,
  segments: t.array(
    t.type({
      cost: tt.money,
      distance: t.number,
      time: t.number,
      coordinates: t.array(
        t.type({
          latitude: t.number,
          longitude: t.number,
        })
      ),
    })
  ),
  waypoints: t.array(Waypoint),
});

export const VehicleInfo = t.strict({
  stateNumber: tt.nullable(t.string),
  brand: tt.nullable(t.string),
  model: tt.nullable(t.string),
});

export type VehicleInfo = t.TypeOf<typeof VehicleInfo>;

export const RegistryJournal = t.intersection([
  t.type({
    id: tt.uuid,
    approvalState: t.string,
    creationTime: t.number,
    coopTrip: t.boolean,
    desiredDate: t.number,
    expected: Expected,
    humanReadableId: t.string,
    passengerCount: t.number,
    purpose: Purpose,
    requestOptions: t.array(t.string),
    status: t.string,
    transportType: t.string,
  }),
  t.partial({
    approvalDate: tt.nullable(t.string),
    passenger: tt.nullable(Passenger),
    author: tt.nullable(Passenger),
    waypointsCount: tt.nullable(t.number),
    paidPeriod: tt.nullable(t.string),
    noCheckinWaypointsCount: tt.nullable(t.number),
    checkinWaypointsCount: tt.nullable(t.number),
    tariffId: tt.nullable(t.string),
    creationDate: t.number,
  }),
]);

export enum PublicCompensationType {
  CITY_TRIP_COMPENSATION = 'CITY_TRIP_COMPENSATION',
  SUBURB_TRIP_COMPENSATION = 'SUBURB_TRIP_COMPENSATION',
  TRAVEL_CARD_COMPENSATION = 'TRAVEL_CARD_COMPENSATION',
}

export const PublicCompensationTypeDescriptions: Record<PublicCompensationType, string> = {
  CITY_TRIP_COMPENSATION: 'Компенсация поездки по городу',
  SUBURB_TRIP_COMPENSATION: 'Компенсация междугородних поездок',
  TRAVEL_CARD_COMPENSATION: 'Компенсация проездного документа',
};

export enum PublicTransportType {
  CITY_BUS = 'CITY_BUS',
  CITY_TROLLEYBUS = 'CITY_TROLLEYBUS',
  CITY_TRAM = 'CITY_TRAM',
  CITY_METRO = 'CITY_METRO',
  SUBURB_BUS = 'SUBURB_BUS',
  SUBURB_TRAIN = 'SUBURB_TRAIN',
  SUBURB_FERRY_CROSSING = 'SUBURB_FERRY_CROSSING',
  SUBURB_TROLLEYBUS = 'SUBURB_TROLLEYBUS',
  TRAVEL_CARD_BUS = 'TRAVEL_CARD_BUS',
  TRAVEL_CARD_TROLLEYBUS = 'TRAVEL_CARD_TROLLEYBUS',
  TRAVEL_CARD_TRAM = 'TRAVEL_CARD_TRAM',
  TRAVEL_CARD_METRO = 'TRAVEL_CARD_METRO',
  TRAVEL_CARD_ALL_CITY_TRANSPORT = 'TRAVEL_CARD_ALL_CITY_TRANSPORT',
}

export const PublicTransportTypeDescriptions: Record<PublicTransportType, string> = {
  CITY_BUS: 'Автобус',
  CITY_TROLLEYBUS: 'Троллейбус',
  CITY_TRAM: 'Трамвай',
  CITY_METRO: 'Метро',
  SUBURB_BUS: 'Междугородний автобус',
  SUBURB_TRAIN: 'Пригородный поезд',
  SUBURB_FERRY_CROSSING: 'Паромная переправа',
  SUBURB_TROLLEYBUS: 'Междугородний троллейбус',
  TRAVEL_CARD_BUS: 'Автобус',
  TRAVEL_CARD_TROLLEYBUS: 'Троллейбус',
  TRAVEL_CARD_TRAM: 'Трамвай',
  TRAVEL_CARD_METRO: 'Метро',
  TRAVEL_CARD_ALL_CITY_TRANSPORT: 'Единый проездной',
};

export enum PublicPaymentTypeCode {
  CODE_4666 = 'CODE_4666',
  CODE_4667 = 'CODE_4667',
  CODE_4661 = 'CODE_4661',
  CODE_4665 = 'CODE_4665',
  CODE_4664 = 'CODE_4664',
}

export const TransportCompensation = t.intersection([
  t.type({
    id: tt.nullable(tt.uuid),
    compensationType: tt.nullable(
      ioTypeFromEnum<PublicCompensationType>('PublicCompensationType', PublicCompensationType)
    ),
    ticketsCost: tt.nullable(tt.money),
  }),
  t.partial({
    transportType: tt.nullable(ioTypeFromEnum<PublicTransportType>('PublicTransportType', PublicTransportType)),
    ticketsCount: tt.nullable(t.number),
    ticketsExpirationStart: tt.nullable(t.string),
    ticketsExpirationEnd: tt.nullable(t.string),
    attachedDocumentId: tt.nullable(tt.uuid),
    paymentTypeCode: tt.nullable(ioTypeFromEnum<PublicPaymentTypeCode>('PublicPaymentTypeCode', PublicPaymentTypeCode)),
  }),
]);

export type TransportCompensation = t.TypeOf<typeof TransportCompensation>;

export const DetailedCompensationType = t.partial({
  name: tt.nullable(ioTypeFromEnum<PublicCompensationType>('PublicCompensationType', PublicCompensationType)),
  rusName: tt.nullable(t.string),
  attachmentDocumentRequired: tt.nullable(t.boolean),
  expirationDatesRequired: tt.nullable(t.boolean),
});

export const DetailedPublicTransportType = t.partial({
  name: tt.nullable(ioTypeFromEnum<PublicTransportType>('PublicTransportType', PublicTransportType)),
  rusName: tt.nullable(t.string),
  publicCompensationType: tt.nullable(
    ioTypeFromEnum<PublicCompensationType>('PublicCompensationType', PublicCompensationType)
  ),
});

export const DetailedPublicCompensation = t.partial({
  id: tt.nullable(tt.uuid),
  ticketsCost: tt.nullable(tt.money),
  ticketsCount: tt.nullable(t.number),
  compensationType: tt.nullable(DetailedCompensationType),
  transportType: tt.nullable(DetailedPublicTransportType),
});

export type DetailedPublicCompensation = t.TypeOf<typeof DetailedPublicCompensation>;

const TripInfoProps = {
  id: tt.uuid,
  humanReadableId: t.string,
  passenger: InfoPassenger,
  author: InfoPassenger,
  transportType: ioTypeFromEnum<TransportTypes>('TransportTypeEnum', TransportTypes),
  creationTime: t.number,
  department: t.intersection([
    t.strict({
      id: tt.uuid,
    }),
    t.partial({
      departmentName: tt.nullable(t.string),
    }),
    t.partial({
      code: tt.nullable(t.string),
    }),
  ]),
  status: t.string,
  position: t.intersection([
    t.type({
      id: tt.uuid,
    }),
    t.partial({
      positionName: tt.nullable(t.string),
    }),
  ]),
  purpose: t.strict({
    id: tt.uuid,
    purpose: tt.nullable(t.string),
  }),
  expected: t.strict({
    distance: t.number,
    time: t.number,
    cost: tt.money,
    waypoints: t.array(WaypointsInfo),
    waypointsCount: tt.nullable(t.number),
    waypointsCountWithCheckIn: tt.nullable(t.number),
    waypointsCountWithoutCheckIn: tt.nullable(t.number),
  }),
};

const TripInfoPartialProps = {
  humanReadableLimitId: tt.nullable(t.string),
  coopTrip: tt.nullable(t.boolean),
  /* TaxiClass  наллейбл для того, чтобы старые заявки не ломались. Потом убрать! */
  taxiClass: tt.nullable(ioTypeFromEnum<TaxiClass>('TaxiClassEnum', TaxiClass)),
  passengers: tt.nullable(t.array(InfoPassenger)),
  approvalState: tt.nullable(t.string),
  itinerantType: tt.nullable(ioTypeFromEnum<ItinerantType>('ItinerantType', ItinerantType)),
  tariff: t.partial({
    id: tt.nullable(tt.uuid),
    humanReadableId: tt.nullable(t.string),
    serviceType: tt.nullable(t.string),
    active: tt.nullable(t.boolean),
  }),

  orderPaymentFormationStartDate: tt.nullable(t.union([t.string, t.number])),
  desiredDate: tt.nullable(t.number),
  creationTime: tt.nullable(t.number),
  approveDate: tt.nullable(t.number),

  magentaOrderId: tt.nullable(t.number),
  costCenter: tt.nullable(t.string),
  sharedRideId: tt.nullable(t.string),
  kpiSavings: tt.nullable(tt.money),
  passengerCount: tt.nullable(t.number),
  contractor: tt.nullable(t.intersection([t.type({ id: tt.uuid }), t.partial({ name: tt.nullable(t.string) })])),
  commentForDriver: tt.nullable(t.string),
  requestRating: tt.nullable(RequestRating),
  approvedBy: tt.nullable(InfoPassenger),
  // PublicTypeParams
  transportCompensation: tt.nullable(t.array(TransportCompensation)),
  paymentQuarter: tt.nullable(t.number),
  publicCompensationDocumentExist: tt.nullable(t.boolean),
  // ------------------
  factParametersSettingTime: tt.nullable(t.string),
  factData: tt.nullable(FactRoute),
  tripId: tt.nullable(tt.uuid),
  tripHumanReadableID: tt.nullable(t.string),
  finishedTime: tt.nullable(t.string),
  paymentTime: tt.nullable(t.string),

  deadline: tt.nullable(t.number),
  driverArrivedDatetime: tt.nullable(t.number),
  deadlineViolation: tt.nullable(t.string),
  vehicle: tt.nullable(VehicleInfo),
};
// Todo:Исправить когда сделают бэк
export const TripInfoForReporting = t.intersection([t.strict(TripInfoProps), t.partial(TripInfoPartialProps)]);

export const SearchResponse = t.type({
  totalElements: t.number,
  totalPages: t.number,
  size: t.number,
  number: t.number,
  numberOfElements: t.number,
  first: t.boolean,
  last: t.boolean,
  empty: t.boolean,
  sort: Sorted,
  pageable: Pageable,
  content: t.array(TripInfoForReporting),
});

export const SortSettings = t.type({
  property: t.string,
  directionAsc: t.boolean,
});

export const PublicTransportTypes = t.type({
  name: t.string,
  rusName: t.string,
  publicCompensationType: t.string,
});

export const Filters = t.intersection([
  t.type({
    pageSetting: t.type({ page: t.number, size: t.number }),
  }),
  t.partial({
    requestHumanId: t.string,
    tariffIdSet: t.array(t.string),
    requestStatusSet: t.array(t.string),
    paymentPeriod: t.number,
    compensationType: t.array(t.string),
    purposeSet: t.array(t.type({ id: t.string })),
    ratingMarkSet: t.array(t.string),
    creationDate: DateRangeISO,
    orderPaymentFormationStartDate: DateRangeISO,
    balanceUnitSet: t.array(t.number),
    costCenter: t.string,
    employeeFIO: t.string,
    personnelNumber: t.string,
    departmentCode: t.string,
    sortSetting: SortSettings,
    organizationId: tt.uuid,
    desiredDateRange: DateRangeISO,
    deadlineState: t.boolean,
    savings: t.boolean,
    requestClosedDatetime: DateRangeISO,
    publicCompensationDocumentExist: t.boolean,
    employeeOrganizationSet: t.array(t.string),
    publicTransportType: t.array(t.string),
    executorGroupIds: t.array(tt.uuid),
    department1: t.array(t.string),
    department2: t.array(t.string),
    department3: t.array(t.string),
    department4: t.array(t.string),
    department5: t.array(t.string),
    department6: t.array(t.string),
  }),
]);

export const PaymentStateResponse = t.array(
  t.type({
    requestId: t.string,
    status: t.string,
  })
);

export const EmployeeSearchRequest = t.type({
  organizationId: tt.uuid,
  departmentId: tt.uuid,
  id: tt.uuid,
});

export const PublicUIVisibilityDTO = t.partial({
  requestIdVisible: t.boolean,
  requestStatusVisible: t.boolean,
  passengerFioVisible: t.boolean,
  tripPurposeVisible: t.boolean,
  creationTimeVisible: t.boolean,
  desiredDateVisible: t.boolean,
  costVisible: t.boolean,
  mvzVisible: t.boolean,
  approveDateVisible: t.boolean,
  itinerantTypeVisible: t.boolean,
  transportTypeVisible: t.boolean,
  compensationTypeVisible: t.boolean,
  waypointFromVisible: t.boolean,
  waypointToVisible: t.boolean,
  intermediateAddressVisible: t.boolean,
  passengerDepartmentOneVisible: t.boolean,
  passengerDepartmentTwoVisible: t.boolean,
  passengerDepartmentThreeVisible: t.boolean,
  passengerDepartmentFourVisible: t.boolean,
  passengerDepartmentFiveVisible: t.boolean,
  passengerDepartmentSixVisible: t.boolean,
  hasAttachmentVisible: t.boolean,
  waypointsCountVisible: t.boolean,
  waypointsCountWithCheckInVisible: t.boolean,
  waypointsCountWithoutCheckInVisible: t.boolean,
  paymentPeriodVisible: t.boolean,
  personelNumberVisible: t.boolean,
});

export type PublicUIVisibilityDTO = t.TypeOf<typeof PublicUIVisibilityDTO>;
export type EmployeeSearchRequest = t.TypeOf<typeof EmployeeSearchRequest>;
export type SumRange = t.TypeOf<typeof SumRange>;
export type WaypointsInfo = t.TypeOf<typeof WaypointsInfo>;
export type InfoPassenger = t.TypeOf<typeof InfoPassenger>;
export type PublicTransportTypes = t.TypeOf<typeof PublicTransportTypes>;
export type DateRange = t.TypeOf<typeof DateRange>;
export type TripInfoForReporting = t.TypeOf<typeof TripInfoForReporting>;
export type SearchResponse = t.TypeOf<typeof SearchResponse>;
export type RegistryJournal = t.TypeOf<typeof RegistryJournal>;
export type PublicRegistryFilters = t.TypeOf<typeof Filters>;
export type SortSettings = t.TypeOf<typeof SortSettings>;
export type PaymentStateResponse = t.TypeOf<typeof PaymentStateResponse>;
