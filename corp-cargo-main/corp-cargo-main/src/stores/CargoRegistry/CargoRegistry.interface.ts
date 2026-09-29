import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { Segment } from 'stores/Geo/Geo.interface';

const Sorted = t.type({
  sorted: t.boolean,
  unsorted: t.boolean,
  empty: t.boolean,
});

const Pageable = t.type({
  offset: t.number,
  pageNumber: t.number,
  pageSize: t.number,
  paged: t.boolean,
  unpaged: t.boolean,
  sort: Sorted,
});

const Person = t.partial({
  id: t.union([tt.nullable(t.string), t.undefined]),
  fio: tt.nullable(t.string),
  mobilePhone: tt.nullable(t.string),
  personnelNumber: tt.nullable(t.string),
});

const Contractor = t.intersection([
  t.strict({
    id: tt.uuid,
  }),
  t.partial({
    name: tt.nullable(t.string),
  }),
]);

const Waypoint = {
  latitude: t.number,
  longitude: t.number,
  country: tt.nullable(t.string),
  region: tt.nullable(t.string),
  city: tt.nullable(t.string),
  street: tt.nullable(t.string),
  house: tt.nullable(t.string),
  building: t.union([tt.nullable(t.string), t.undefined]),
  structure: t.union([tt.nullable(t.string), t.undefined]),
  district: t.union([tt.nullable(t.string), t.undefined]),
  checkinManual: tt.nullable(t.boolean),
  checkinAutomatic: tt.nullable(t.boolean),
  existInVspGosbTbRegistry: tt.nullable(t.boolean),
  waitTime: tt.nullable(t.number),
};

const Expected = t.partial({
  cost: t.number,
  segments: t.array(Segment),
  distance: t.number,
  time: tt.nullable(t.number),
  waypointsCount: tt.nullable(t.number),
});

const Evaluation = t.union([
  t.type({
    rating: t.number,
    requestId: t.string,
    comment: tt.nullable(t.string),
    reasons: t.unknown,
  }),
  t.null,
  t.undefined,
]);

const TripInfoProps = {
  id: tt.uuid,
  creationTime: t.number,
  humanReadableId: t.string,
  status: t.string,
  transportType: t.string,
  expected: t.intersection([Expected, t.type({ waypoints: t.array(t.partial(Waypoint)) })]),
  senderAddress: t.union([t.string, t.null]),
  recipientAddress: t.union([t.string, t.null]),
  weight: t.union([t.number, t.null]),
  volume: t.union([t.number, t.null]),
  source: t.union([t.string, t.null]),
  cargoTripId: t.union([t.string, t.null]),
  templateNumber: t.union([t.string, t.null]),
  senderOrganization: t.union([t.string, t.null, t.undefined]),
  recipientOrganization: t.union([t.string, t.null, t.undefined]),
  recipient: t.union([Person, t.null]),
  sender: t.union([Person, t.null]),
  costCenter: t.union([tt.nullable(t.string), t.undefined]),
  plannedDeliveryDate: t.union([tt.nullable(t.number), t.undefined]),
  shipmentTime: t.union([tt.nullable(t.number), t.undefined]),
  transferTime: t.union([tt.nullable(t.number), t.undefined]),
  economy: t.union([t.number, t.undefined, t.null]),
  deadlineDate: t.union([tt.nullable(t.string), t.undefined]),
  actualCost: t.union([t.number, t.null, t.undefined]),
  actualDistance: t.union([t.number, t.null, t.undefined]),
  actualShipmentTime: t.union([t.number, t.null, t.undefined]),
  evaluation: Evaluation,
};

const TripInfoPartialProps = {
  desiredDate: tt.nullable(t.union([t.string, t.number])),
  contractor: tt.nullable(Contractor),
  author: Person,
  department: tt.nullable(t.strict({ id: tt.uuid })),
};

export const TripInfo = t.intersection([t.strict(TripInfoProps), t.partial(TripInfoPartialProps)]);
export type TripInfo = t.TypeOf<typeof TripInfo>;

export const TripInfoDetailed = t.partial({
  id: tt.uuid,
  creationTime: t.number,
  humanReadableId: t.string,
  status: t.string,
  approvalState: t.string,
  transportType: t.string,
  expected: t.intersection([Expected, t.type({ waypoints: t.array(t.type(Waypoint)) })]),
  senderOrganization: t.string,
  recipientOrganization: t.string,
  recipient: Person,
  sender: Person,
  desiredDate: t.number,
  author: Person,
});
export type TripInfoDetailed = t.TypeOf<typeof TripInfoDetailed>;

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
  content: t.array(TripInfo),
});
export type SearchResponse = t.TypeOf<typeof SearchResponse>;

const PageSetting = t.strict({
  page: t.number,
  size: t.number,
});
export type PageSetting = t.TypeOf<typeof PageSetting>;

const NumberRange = t.strict({
  start: t.number,
  end: t.number,
});
export type NumberRange = t.TypeOf<typeof NumberRange>;

const SortSetting = t.strict({
  property: t.string,
  directionAsc: t.boolean,
});
export type SortSetting = t.TypeOf<typeof SortSetting>;

export const SearchRequest = t.partial({
  requestHumanId: t.string,
  desiredDate: t.strict({
    start: t.string,
    end: t.string,
  }),
  expectedCost: NumberRange,
  transportType: t.array(t.string),
  authorFIO: t.string,
  requestStatusSet: t.array(t.string),
  sortSetting: SortSetting,
  pageSetting: PageSetting,
});
export type SearchRequest = t.TypeOf<typeof SearchRequest>;

export const SearchRequestRoutes = t.partial({
  organizationId: t.string,
  executorGroupIds: t.array(t.string),
  emptyExecutorGroup: t.boolean,
  humanReadableId: t.string,
  transportType: t.string,
  statusSet: t.array(t.string),
  desiredDateRange: t.strict({
    start: t.string,
    end: t.string,
  }),
  creationDateRange: t.strict({
    start: t.string,
    end: t.string,
  }),
  regionFrom: t.array(t.string),
  regionTo: t.array(t.string),
  contractorSet: t.array(t.string),
  authorEmployeeId: t.array(t.string),
  sortSetting: SortSetting,
  pageSetting: PageSetting,
});
export type SearchRequestRoutes = t.TypeOf<typeof SearchRequestRoutes>;

export const SearchRequestCompensations = t.partial({
  organizationId: t.string,
  humanReadableId: t.string,
  transportType: t.string,
  statusSet: t.array(t.string),
  desiredDateRange: t.strict({
    start: t.string,
    end: t.string,
  }),
  creationDateRange: t.strict({
    start: t.string,
    end: t.string,
  }),
  regionFrom: t.array(t.string),
  regionTo: t.array(t.string),
  contractorSet: t.array(t.string),
  authorEmployeeId: t.array(t.string),
  sortSetting: SortSetting,
  pageSetting: PageSetting,
});
export type SearchRequestCompensations = t.TypeOf<typeof SearchRequestCompensations>;

export const TransportType = t.type({
  name: t.string,
  rusName: t.string,
});
export type TransportType = t.TypeOf<typeof TransportType>;

export const DateRangeISO = t.strict({
  start: t.string,
  end: t.string,
});

export type DateRangeISO = t.TypeOf<typeof DateRangeISO>;

export const SortSettings = t.type({
  property: t.string,
  directionAsc: t.boolean,
});
export type SortSettings = t.TypeOf<typeof SortSettings>;

export const Filters = t.intersection([
  t.type({
  }),
  t.partial({
    requestHumanId: t.string,
    transportType: t.array(t.string),
    contractorSet: t.array(t.string),
    authorFIO: t.string,
    requestStatusSet: t.array(t.string),
    deadlineDate: t.string,
    desiredDate: DateRangeISO,
    creationDate: DateRangeISO,
    pageSetting: t.type({ page: t.number, size: t.number }),
    sortSetting: SortSettings,
    organizationId: tt.uuid,
    changeDate: DateRangeISO,
    organizationSet: t.array(t.string),
    executorGroupIds: t.array(tt.uuid),
    emptyExecutorGroup: t.boolean,
    department1: t.array(t.string),
    department2: t.array(t.string),
    department3: t.array(t.string),
    department4: t.array(t.string),
    department5: t.array(t.string),
    department6: t.array(t.string),
  }),
]);
export type CargoRegistryFilters = t.TypeOf<typeof Filters>;
