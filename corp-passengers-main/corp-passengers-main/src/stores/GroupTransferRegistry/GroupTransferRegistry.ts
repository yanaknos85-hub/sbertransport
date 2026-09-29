import * as t from 'io-ts';
import * as tt from 'utils/io-ts';

export const RequestRating = t.partial({
  advantages: tt.nullable(t.array(t.string)),
  drawbacks: tt.nullable(t.array(t.string)),
  ratingComment: tt.nullable(t.string),
  rating: tt.nullable(t.number),
});
export type RequestRating = t.TypeOf<typeof RequestRating>;

export const Purpose = t.partial({
  id: tt.nullable(t.string),
  label: tt.nullable(t.string),
});
export type Purpose = t.TypeOf<typeof Purpose>;

export const Department = t.partial({
  id: t.string,
  code: t.string,
  departmentName: t.string,
  parent: tt.nullable(t.string),
});
export type Department = t.TypeOf<typeof Department>;

export const Position = t.partial({
  id: tt.nullable(t.string),
  positionName: tt.nullable(t.string),
  organizationId: tt.nullable(t.string),
});
export type Position = t.TypeOf<typeof Position>;

export const Limit = t.type({
  id: tt.nullable(t.string),
  humanReadableId: tt.nullable(t.string),
  departmentId: tt.nullable(t.string),
});
export type Limit = t.TypeOf<typeof Limit>;

export const Tariff = t.partial({
  id: tt.nullable(t.string),
  humanReadableId: tt.nullable(t.string),
  region: tt.nullable(t.string),
  transportType: tt.nullable(t.string),
  serviceType: tt.nullable(t.string),
  active: tt.nullable(t.boolean),
  organizationId: tt.nullable(t.string),
  departmentId: tt.nullable(t.string),
  contractId: tt.nullable(t.string),
  workGroup: tt.nullable(t.string),
});
export type Tariff = t.TypeOf<typeof Tariff>;

export const Vehicle = t.type({
  id: tt.nullable(t.string),
  brand: tt.nullable(t.string),
  model: tt.nullable(t.string),
  stateNumber: tt.nullable(t.string),
  color: tt.nullable(t.string),
  autoparkId: tt.nullable(t.string),
  deleted: tt.nullable(t.boolean),
});
export type Vehicle = t.TypeOf<typeof Vehicle>;

export const Driver = t.type({
  firstName: tt.nullable(t.string),
  lastName: tt.nullable(t.string),
  patronymic: tt.nullable(t.string),
  contactPhone: tt.nullable(t.string),
  rating: tt.nullable(t.number),
});
export type Driver = t.TypeOf<typeof Driver>;

export const Contractor = t.type({
  id: t.string,
  name: tt.nullable(t.string),
});
export type Contractor = t.TypeOf<typeof Contractor>;

export const GroupTransferRegistryFactData = t.partial({
  tripStartTime: tt.nullable(t.union([t.string, t.array(t.number)])),
  tripFactPrice: tt.nullable(t.number),
  tripFactWaitTime: tt.nullable(t.number),
  tripFactDistance: tt.nullable(t.number),
  tripFactDuration: tt.nullable(t.number),
  factSearchTime: tt.nullable(t.number),
});
export type GroupTransferRegistryFactData = t.TypeOf<typeof GroupTransferRegistryFactData>;

export const GroupTransferWaypoint = t.partial({
  country: tt.nullable(t.string),
  region: tt.nullable(t.string),
  city: tt.nullable(t.string),
  street: tt.nullable(t.string),
  house: tt.nullable(t.string),
  building: tt.nullable(t.string),
  structure: tt.nullable(t.string),
  existInVspGosbTbRegistry: tt.nullable(t.boolean),
  waitTime: tt.nullable(t.union([t.string, t.number])),
  checkinAutomatic: tt.nullable(t.boolean),
  checkinManual: tt.nullable(t.boolean),
});
export type GroupTransferWaypoint = t.TypeOf<typeof GroupTransferWaypoint>;

export const Point = t.type({
  latitude: t.number,
  longitude: t.number,
});
export type Point = t.TypeOf<typeof Point>;

export const Segment = t.partial({
  cost: t.number,
  distance: t.number,
  time: t.string,
  coordinates: t.array(Point),
});
export type Segment = t.TypeOf<typeof Segment>;

export const Expected = t.partial({
  cost: t.number,
  distance: t.number,
  time: t.number,
  waypointsCount: t.number,
  waypointsCountWithCheckIn: t.number,
  waypointsCountWithoutCheckIn: t.number,
  segments: t.array(Segment),
  waypoints: t.array(GroupTransferWaypoint),
});
export type Expected = t.TypeOf<typeof Expected>;

export const Person = t.intersection([
  t.type({
    id: tt.uuid,
    humanReadableId: t.string,
    firstName: t.string,
    lastName: t.string,
  }),
  t.partial({
    fio: tt.nullable(t.string),
    organizationId: tt.nullable(tt.uuid),
    positionName: tt.nullable(t.string),
    phone: tt.nullable(t.string),
    userId: tt.nullable(tt.uuid),
    patronymic: tt.nullable(t.string),
    personnelNumber: tt.nullable(t.string),
    itinerantType: tt.nullable(t.string),
    marriageCertificateNumber: t.union([tt.nullable(t.number), tt.nullable(t.string)]),
    departmentId: tt.nullable(tt.uuid),
  }),
]);
export type Person = t.TypeOf<typeof Person>;

export const GroupTransferReportItem = t.intersection([
  t.type({
    id: tt.uuid,
    humanReadableId: t.string,
    status: t.string,
    passenger: Person,
    author: Person,
    creationTime: tt.nullable(t.number),
    purpose: Purpose,
    transportType: t.string,
    groupTransferClass: tt.nullable(t.string),
  }),
  t.partial({
    tariffId: tt.uuid,
    humanReadableLimitId: tt.nullable(t.string),
    statusCode: tt.nullable(t.number),
    itinerantType: tt.nullable(t.string),
    costCenter: tt.nullable(t.string),
    approvedBy: tt.nullable(Person),
    desiredDate: tt.nullable(t.number),
    requestRating: tt.nullable(RequestRating),
    commentForDriver: tt.nullable(t.string),
    passengerCount: tt.nullable(t.number),
    factData: tt.nullable(GroupTransferRegistryFactData),
    department: tt.nullable(Department),
    position: tt.nullable(Position),
    approvalState: tt.nullable(t.string),
    driver: tt.nullable(Driver),
    vehicle: tt.nullable(Vehicle),
    limit: tt.nullable(Limit),
    expected: tt.nullable(Expected),
    tariff: tt.nullable(Tariff),
    contractor: tt.nullable(Contractor),
    tripHumanReadableID: tt.nullable(t.string),
    finishedTime: tt.nullable(t.union([t.number, t.string])),
    timeZone: tt.nullable(t.string),
    passengerDepartment1: tt.nullable(t.string),
    passengerDepartment2: tt.nullable(t.string),
    passengerDepartment3: tt.nullable(t.string),
    passengerDepartment4: tt.nullable(t.string),
    passengerDepartment5: tt.nullable(t.string),
    passengerDepartment6: tt.nullable(t.string),
    departureAddress: tt.nullable(t.string),
    intermediateAddresses: tt.nullable(t.string),
    destinationAddress: tt.nullable(t.string),
    resolution: tt.nullable(t.string),
    organizationId: tt.nullable(t.string),
    deadline: tt.nullable(t.number),
    driverArrivedDatetime: tt.nullable(t.number),
    deadlineViolation: tt.nullable(t.string),
    organizationOfficialName: tt.nullable(t.string),
    approveDate: tt.nullable(t.number),
    requestClosedDatetime: tt.nullable(t.number),
    vip: t.boolean,
    tripId: tt.nullable(t.string),
  }),
]);
export type GroupTransferReportItem = t.TypeOf<typeof GroupTransferReportItem>;

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
export const GroupTransferReportResponse = t.type({
  content: t.array(GroupTransferReportItem),
  pageable: Pageable,
  last: t.boolean,
  first: t.boolean,
  sort: Sorted,
  empty: t.boolean,
  totalPages: t.number,
  totalElements: t.number,
  numberOfElements: t.number,
  size: t.number,
  number: t.number,
});
export type GroupTransferReportResponse = t.TypeOf<typeof GroupTransferReportResponse>;

const DateNumberRange = t.partial({
  start: t.number,
  end: t.number,
});

const RequestPurpose = t.partial({
  id: t.string,
  purpose: t.string,
});

const organizationSettings = t.partial({
  employeeOrganizationSet: t.array(t.string),
});

const sortSettings = t.partial({
  property: t.string,
  directionAsc: t.boolean,
});

const pageSettings = t.partial({
  page: t.number,
  size: t.number,
});

const waypointWaitTimes = t.partial({
  start: t.string,
  end: t.string,
});

export const RegistrySearchQuery = t.partial({
  organizationId: t.string,
  requestHumanId: t.string,
  requestStatusSet: t.array(t.string),
  empty: t.boolean,
  creationDate: DateNumberRange,
  desiredDate: DateNumberRange,
  expectedCost: DateNumberRange,
  expectedDistance: DateNumberRange,
  factCost: DateNumberRange,
  factDistance: DateNumberRange,
  actualDepartureDate: DateNumberRange,
  desiredDateRange: DateNumberRange,
  purposeSet: t.array(RequestPurpose),
  employeeFIO: t.string,
  personnelNumber: t.string,
  costCenter: t.string,
  balanceUnitSet: t.array(t.number),
  departmentCode: t.string,
  tariffIdSet: t.array(t.string),
  contractorSet: t.array(t.string),
  employeePositionSet: t.array(t.string),
  employeeDepartmentSet: t.array(t.string),
  departureAddress: t.string,
  destinationAddress: t.string,
  employeeItinerantTypeSet: t.array(t.string),
  ratingMarkSet: t.array(t.number),
  passengerCountSet: t.array(t.number),
  groupTransferClassList: t.array(t.string),
  regions: t.array(t.string),
  deadlineState: t.boolean,
  organizationSetting: organizationSettings,
  sortSetting: sortSettings,
  pageSetting: pageSettings,
  waypointWaitTime: waypointWaitTimes,
  requestClosedDatetime: DateNumberRange,
});
export type RegistrySearchQuery = t.TypeOf<typeof RegistrySearchQuery>;

