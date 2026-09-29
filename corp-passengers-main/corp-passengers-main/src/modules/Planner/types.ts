import * as t from 'io-ts';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { EmployeeStatus, OrgStructureType } from 'constants/constants.app';

export enum DeliveryUrgencyEnum {
  express = 'express',
  standard = 'standard',
}

export enum Tab {
  planner = 'planner',
  journal = 'journal',
}

export enum RouteStatusEnum {
  CARGO_PLANNING = 'CARGO_PLANNING',
  CARGO_PLANNING_FINISHED = 'CARGO_PLANNING_FINISHED',
  CARGO_AWAITING_DATA = 'CARGO_AWAITING_DATA',
  CARGO_AWAITING_TRANSFER = 'CARGO_AWAITING_TRANSFER',
  CARGO_TRANSFER_FINISHED = 'CARGO_TRANSFER_FINISHED',
  CARGO_SHIPMENT_FINISHED = 'CARGO_SHIPMENT_FINISHED',
  CARGO_DELIVERY_CONFIRMATION_FINISHED = 'CARGO_DELIVERY_CONFIRMATION_FINISHED',
  CARGO_CANCELED = 'CARGO_CANCELED',
}

export const RouteStatus = ioTypeFromEnum<RouteStatusEnum>('RouteStatusEnum', RouteStatusEnum);
export type RouteStatusType = t.TypeOf<typeof RouteStatus>;

export enum WaypointType {
  LOAD = 'LOAD',
  UNLOAD = 'UNLOAD',
}

export enum CreatorTypeEnum {
  HANDLE = 'HANDLE',
  AUTO = 'AUTO',
  INTEGRATION = 'INTEGRATION',
  SRM = 'SRM',
}

export type LatLngTuple = [number, number];

export const CreatorType = ioTypeFromEnum<CreatorTypeEnum>('CreatorTypeEnum', CreatorTypeEnum);
export type CreatorTypeType = t.TypeOf<typeof CreatorType>;

export const Address = t.intersection([
  t.type({
    id: t.string,
    country: t.string,
    region: t.string,
    city: t.string,
    street: t.string,
    house: t.string,
    latitude: t.number,
    longitude: t.number,
    _addressString: t.string,
    _regionData: t.string,
  }),
  t.partial({
    waitTime: t.number,
  }),
]);

export type AddressType = t.TypeOf<typeof Address>;

const RouteAddress = t.type({
  id: t.string,
  latitude: t.number,
  longitude: t.number,
  country: t.string,
  region: t.string,
  city: t.string,
  street: t.string,
  house: t.string,
  existInVspGosbTbRegistry: t.boolean,
});

export type RouteAddressType = t.TypeOf<typeof RouteAddress>;

const Contact = t.array(
  t.type({
    id: t.string,
    mobilePhone: t.string,
    employeeId: t.string,
    fullName: t.string,
  })
);
export type ContactType = t.TypeOf<typeof Contact>;

const OrderWaypoint = t.type({
  type: t.union([t.literal(WaypointType.LOAD), t.literal(WaypointType.UNLOAD)]),
  id: t.string,
  country: t.string,
  region: t.string,
  city: t.string,
  street: t.string,
  house: t.string,
  latitude: t.number,
  longitude: t.number,
  _addressString: t.string,
  _regionData: t.string,
  organization: t.string,
  contacts: Contact,
});

export type OrderWaypointType = t.TypeOf<typeof OrderWaypoint>;

const RequestPoints = t.record(t.string, t.string);

export const RouteWaypoint = t.intersection([
  t.type({
    id: t.string,
    type: t.union([t.literal(WaypointType.LOAD), t.literal(WaypointType.UNLOAD)]),
    orderingIndex: t.number,
    address: RouteAddress,
    loader: t.boolean,
    latitude: t.number,
    longitude: t.number,
    weight: t.number,
    volume: t.number,
    distance: t.number,
    requestPoints: RequestPoints,
    organization: t.string,
    contacts: Contact,
    humanReadbleIds: t.array(t.string),
  }),
  t.partial({ humanReadbleIds: t.array(t.string) }),
]);

export type RouteWaypointType = t.TypeOf<typeof RouteWaypoint>;

const Author = t.intersection([
  t.type({
    id: t.string,
    humanReadableId: t.string,
    userId: t.string,
    firstName: t.string,
    lastName: t.string,
    patronymic: t.string,
    personnelNumber: t.string,
    departmentId: t.string,
    positionId: t.string,
    mobilePhone: t.string,
    email: t.string,
    supervisorId: t.string,
    delegatedById: t.string,
    availableTransportTypes: t.array(t.any),
    organizationId: t.string,
    status: t.string,
    approvals: t.number,
  }),
  t.partial({
    attributes: t.array(t.string), managedDepartments: t.array(t.string), approvals: t.number,
  }),
]);

const RouteAuthor = t.type({
  id: t.string,
  firstName: t.string,
  lastName: t.string,
  patronymic: t.string,
  personnelNumber: t.string,
  departmentId: t.string,
  userId: t.string,
  positionId: t.string,
  supervisorId: t.string,
  organizationId: t.string,
  departmentName: t.string,
  humanReadableId: t.string,
  mobilePhone: t.string,
});

export const Coordinates = t.type({
  latitude: t.number,
  longitude: t.number,
});

export type CoordinatesType = t.TypeOf<typeof Coordinates>;

const Segment = t.type({
  distance: t.number,
  time: t.number,
  coordinates: t.array(Coordinates),
});

export type Segment = t.TypeOf<typeof Segment>;

const Expected = t.type({
  distance: t.number,
  segments: t.array(Segment),
  waypoints: t.array(OrderWaypoint),
  cost: t.number,
});

export type ExpectedType = t.TypeOf<typeof Expected>;

const TotalSizes = t.type({
  length: t.number,
  width: t.number,
  height: t.number,
  volume: t.number,
  weight: t.number,
  occupiedPlacesCount: t.number,
});

export type TotalSizesType = t.TypeOf<typeof TotalSizes>;

const PriceDetails = t.type({
  baseCost: t.number,
  baseTariff: t.number,
  loaderCost: t.number,
  loaderTariff: t.number,
});

const TransportType = t.type({
  id: t.string,
  name: t.string,
});

const CalculatedTariff = t.type({
  cost: t.number,
  deliveryTime: t.number,
  id: t.string,
  priceDetails: PriceDetails,
  transportType: TransportType,
});

export type CalculatedTariffType = t.TypeOf<typeof CalculatedTariff>;

const ContractorInfo = t.type({
  id: t.string,
  name: t.string,
  contractorName: t.string,
  contractorRusName: t.string,
  integrationType: t.string,
});

export type ContractorInfoType = t.TypeOf<typeof ContractorInfo>;

export const Order = t.intersection([
  t.type({
    id: t.string,
    approvalDate: t.string,
    approvalState: t.string,
    approvedBY: Author, // проверить
    author: Author,
    calculatedTariff: CalculatedTariff,
    comment: tt.nullable(t.string),
    creationTime: t.string,
    desiredDate: t.number,
    expected: Expected,
    express: t.boolean,
    humanReadableId: t.string,
    requestType: t.string,
    status: t.string,
    tariffId: t.string,
    totalSizes: TotalSizes,
    transportType: t.string,
  }),
  t.partial({
    contractorInfo: ContractorInfo,
    regular: t.boolean,
  }),
]);

export type OrderType = t.TypeOf<typeof Order>;

const RequestsForOto = t.type({
  addressFrom: t.string,
  addressTo: t.string,
  cargoType: t.string,
  desiredDate: t.number,
  humanReadableId: t.string,
  id: t.string,
  occupiedPlacesCount: t.number,
  weight: t.number,
  volume: t.number,
  transferTime: t.number,
  shipmentTime: t.number,
  deliveryTimeDate: t.number,
});

export type RequestsForOtoType = t.TypeOf<typeof RequestsForOto>;

export const OrderDetailed = t.type({
  id: t.string,
  humanReadableId: t.string,
  author: t.string,
  autoVolume: t.number,
  capacity: t.number,
  contractorName: t.string,
  cost: t.number,
  creationTime: t.number,
  creationType: t.string,
  desiredDate: t.number,
  distance: t.number,
  shipmentTime: t.number,
  deliveryTimeDate: t.number,
  status: t.string,
  weight: t.number,
  volume: t.number,
  requestsForOto: t.array(RequestsForOto),
  driver: t.string,
  driverPhone: t.string,
  registrationNumber: t.string,
  waitingTime: t.number,
  plannedCost: t.number,
  factCost: t.number,
  factDistance: t.number,
});

export type OrderDetailedType = t.TypeOf<typeof OrderDetailed>;

const Capacity = t.type({
  id: t.string,
  capacity: t.number,
});

export const Auto = t.type({
  id: t.string,
  name: t.string,
  volume: t.number,
  height: t.number,
  length: t.number,
  width: t.number,
  capacity: Capacity,
});

export type AutoType = t.TypeOf<typeof Auto>;

export const Requests = t.intersection([
  t.type({
    id: t.string,
  }),
  t.partial({ humanReadableId: t.string, express: t.boolean }),
]);

const Contractor = t.intersection([
  t.type({
    name: t.string,
    tariffId: t.string,
  }),
  t.partial({
    integrationEmail: t.string,
  }),
]);

export type ContractorType = t.TypeOf<typeof Contractor>;

export const DataRoute = t.type({
  auto: Auto,
  contractors: t.array(Contractor),
});

export type DataRouteType = t.TypeOf<typeof DataRoute>;

export const Route = t.type({
  id: t.string,
  humanReadableId: t.string,
  status: t.union([t.literal(RouteStatusEnum.CARGO_PLANNING), t.literal(RouteStatusEnum.CARGO_PLANNING_FINISHED)]),
  author: RouteAuthor,
  contractorInfo: ContractorInfo,
  auto: Auto,
  transportType: t.string,
  creationTime: t.number,
  desiredDate: t.number,
  active: t.boolean,
  comment: t.string,
  waypoints: t.array(RouteWaypoint),
  tariffId: t.string,
  regionId: t.string,
  cost: t.number,
  distance: t.number,
  volume: t.number,
  weight: t.number,
  addRequests: t.array(t.string),
  createrTypeEnum: t.union([
    t.literal(CreatorTypeEnum.HANDLE),
    t.literal(CreatorTypeEnum.AUTO),
    t.literal(CreatorTypeEnum.INTEGRATION),
  ]),
  creatorType: ioTypeFromEnum<CreatorTypeEnum>('CreatorTypeEnum', CreatorTypeEnum),
});

export type RouteType = t.TypeOf<typeof Route>;

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

const PageableUnion = t.union([Pageable, t.string]);

export const RouteResponse = t.type({
  empty: t.boolean,
  first: t.boolean,
  last: t.boolean,
  number: t.number,
  numberOfElements: t.number,
  pageable: PageableUnion,
  size: t.number,
  sort: Sort,
  totalElements: t.number,
  totalPages: t.number,
  content: t.array(Route),
});

export type RouteResponseType = t.TypeOf<typeof RouteResponse>;

export const OrderResponse = t.type({
  empty: t.boolean,
  first: t.boolean,
  last: t.boolean,
  number: t.number,
  numberOfElements: t.number,
  pageable: PageableUnion,
  size: t.number,
  sort: Sort,
  totalElements: t.number,
  totalPages: t.number,
  content: t.array(Order),
});

export type OrderResponseType = t.TypeOf<typeof OrderResponse>;

const RoutesListStore = t.type({
  content: t.array(Route),
  totalElements: t.number,
  totalPages: t.number,
});

export type RoutesListStoreType = t.TypeOf<typeof RoutesListStore>;

const OrdersListStore = t.type({
  content: t.array(Order),
  totalElements: t.number,
  totalPages: t.number,
});

export type OrdersListStoreType = t.TypeOf<typeof OrdersListStore>;

const PageSetting = t.type({
  page: t.number,
  size: t.number,
});

const SortSetting = t.type({
  property: t.string,
  directionAsc: t.boolean,
});

export const RoutesFilters = t.partial({
  humanReadableId: t.string,
  statusSet: tt.nullable(t.string),
  regionFrom: t.array(t.string),
  regionTo: t.array(t.string),
  creationDateRange: t.array(t.string),
  desiredDateRange: t.array(t.string),
  autoId: t.string,
  authorEmployeeId: t.string,
  departmentId: t.string,
  sortSetting: SortSetting,
  pageSetting: PageSetting,
});

export type RoutesFiltersType = t.TypeOf<typeof RoutesFilters>;

const OrdersFilters = t.partial({
  humanReadableId: t.string,
  express: t.boolean,
  creationDateRange: t.array(t.string),
  desiredDateRange: t.array(t.string),
  departmentOrderId: t.string,
  departmentSenderId: t.string,
  departmentEmployeeId: t.string,
  regionFrom: t.array(t.string),
  regionTo: t.array(t.string),
  emploeeId: t.string,
  autoId: t.string,
  transportType: tt.nullable(t.string),
  typeRequest: tt.nullable(t.string),
  sortSetting: SortSetting,
  pageSetting: PageSetting,
});

export type OrdersFiltersType = t.TypeOf<typeof OrdersFilters>;

export const EmployeeSearchQuery = t.partial({
  humanReadableId: t.string,
  personnelNumber: t.string,
  status: t.string,
  email: t.string,
  page: t.number,
  size: t.number,
  mobilePhone: tt.mobilePhone,
  fullName: t.string,
});

export type EmployeeSearchQuery = t.TypeOf<typeof EmployeeSearchQuery>;

export const PaginationParams = t.type({
  page: t.number,
  size: t.number,
});
export type PaginationParams = t.TypeOf<typeof PaginationParams>;

const EmployeesAttribute = t.type({
  id: tt.uuid,
  name: t.string,
  status: t.union([t.literal('ACTIVE'), t.literal('INACTIVE')]),
});

export type EmployeesAttribute = t.TypeOf<typeof EmployeesAttribute>;

const Employee = t.intersection([
  t.partial({
    organizationName: t.string,
    departmentName: t.string,
    availableTransportTypes: t.UnknownArray,
    userId: t.string,
    personnelNumber: t.string,
    roles: t.array(t.string),
    patronymic: t.string,
    mobilePhone: t.string,
    email: t.string,
    supervisorId: t.string,
    delegatedById: t.string,
    personalCars: t.UnknownArray,
    positionName: t.string,
    gender: t.string,
    orgStructureType: ioTypeFromEnum<OrgStructureType>('OrgStructureType', OrgStructureType),
  }),
  t.type({
    humanReadableId: t.string,
    id: tt.uuid,
    firstName: t.string,
    lastName: t.string,
    organizationId: tt.uuid,
    departmentId: tt.uuid,
    positionId: tt.uuid,
    attributes: t.array(EmployeesAttribute),
    status: t.keyof(EmployeeStatus),
  }),
]);
export type Employee = t.TypeOf<typeof Employee>;

const RequestRoute = t.type({
  author: Employee,
  desiredDate: t.number,
  startDate: t.number,
  transportType: t.string,
  tariffId: t.string,
  contragentId: t.string,
  auto: Auto,
  addRequests: t.array(t.string),
});

export type RequestRouteType = t.TypeOf<typeof RequestRoute>;

const DepartmentSearchQuery = t.partial({
  humanReadableId: t.string,
  departmentName: t.string,
  code: t.string,
  status: t.string,
  location: t.string,
  page: t.number,
  size: t.number,
});
export type DepartmentSearchQuery = t.TypeOf<typeof DepartmentSearchQuery>;

const DepartmentHead = t.partial({
  id: t.string,
  lastName: t.string,
  firstName: t.string,
  patronymic: t.string,
});

const Parent = t.partial({
  id: t.string,
  departmentName: t.string,
});

export const DepartmentEmployee = t.type({
  id: t.string,
  firstName: t.string,
  lastName: t.string,
  personnelNumber: t.string,
});

export type DepartmentEmployee = t.TypeOf<typeof DepartmentEmployee>;

export const Department = t.intersection([
  t.partial({
    fullStructurePath: t.string,
    humanReadableId: t.string,
    departmentHead: DepartmentHead,
    parent: Parent,
    location: t.string,
    geozoneId: tt.uuid,
    easupId: t.string,
    level: t.number,
  }),

  t.type({
    id: tt.uuid,
    organizationId: tt.uuid,
    code: t.string,
    departmentName: t.string,
    employees: t.array(DepartmentEmployee),
    children: t.array(
      t.strict({
        id: t.string,
        departmentName: t.string,
        status: ioTypeFromEnum<EmployeeStatus>('EmployeeStatus', EmployeeStatus),
      })
    ),
    status: ioTypeFromEnum<EmployeeStatus>('EmployeeStatus', EmployeeStatus),
  }),
]);

export type DepartmentType = t.TypeOf<typeof Department>;

export const DepartmentsResponse = t.type({
  empty: t.boolean,
  first: t.boolean,
  last: t.boolean,
  number: t.number,
  numberOfElements: t.number,
  pageable: PageableUnion,
  size: t.number,
  sort: Sort,
  totalElements: t.number,
  totalPages: t.number,
  content: t.array(Department),
});

export type DepartmentsResponseType = t.TypeOf<typeof DepartmentsResponse>;

const CarInfo = t.type({
  carDriver: t.string,
  carDriverPhone: t.string,
  brandName: t.string,
  model: t.string,
  registrationNumber: t.string,
});

const MonitorRoute = t.type({
  id: tt.uuid,
  humanReadableId: t.string,
  status: t.string,
  author: t.string,
  creationTime: t.string,
  desiredDate: t.string,
  distance: t.number,
  shipmentTime: t.string,
  weight: t.number,
  volume: t.number,
  capacity: t.number,
  cost: t.number,
  plannedRange: t.string,
  plannedPrice: t.string,
  waypointCount: t.string,
  contractor: t.string,
  cargoTransportType: t.string,
  carInfo: CarInfo,
});

export type MonitorRouteType = t.TypeOf<typeof MonitorRoute>;

const MonitorFilters = t.type({
  humanReadableId: t.string,
  statusSet: t.string,
  desiredDateRange: t.array(t.string),
  creationDateRange: t.array(t.string),
  regionFrom: t.array(t.string),
  regionTo: t.array(t.string),
  employeeId: t.string,
  autoId: t.string,
  authorEmployeeId: t.string,
  departmentId: t.string,
  organizationId: t.string,
  sortSetting: SortSetting,
  pageSetting: PageSetting,
});

export type MonitorFiltersType = t.TypeOf<typeof MonitorFilters>;

const SearchMonitorRouteResponse = t.type({
  totalPages: t.number,
  totalElements: t.number,
  size: t.number,
  content: t.array(MonitorRoute),
  number: t.number,
  sort: Sort,
  numberOfElements: t.number,
  pageable: Pageable,
  first: t.boolean,
  last: t.boolean,
  empty: t.boolean,
});

export type MonitorRouteResponseType = t.TypeOf<typeof SearchMonitorRouteResponse>;
