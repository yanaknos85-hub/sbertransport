import type { Roles } from 'constants/constants.app';
import type { Tab, Category } from '../constants/Tabs';
import type { UUID } from 'utils/io-ts';
import { OrderWaypointType, RouteWaypointType } from 'modules/Planner/types';
import { Segment } from 'stores/Geo/Geo.interface';

export type TKeys<T> = {
  [key in string]: T;
};

export enum Source {
  WEB = 'WEB',
  HOME_CLICK = 'HOME_CLICK',
  WEB_MULTIPLE = 'WEB_MULTIPLE',
}

export interface FeedSearchQuery {
  transportTypeEnum?: string;
  requestId?: UUID;
  requestHumanId?: string;
  limitId?: UUID;
  requestStatusSet?: string[];
  creationDate?: {
    start: number;
    end: number;
  };
  terminalStatus?: boolean;
  dispatcherRequest?: boolean;
  sortSetting?: {
    property: string;
    directionAsc: boolean;
  };
  page: number;
  pageSize: number;
  requestType?: string[];
  statuses?: string[];
  emptyExecutorGroup?: boolean;
}

export interface Packages {
  name: string;
  unit: string;
  count: number;
}

export interface CargoDetails {
  position: number;
  cargoName: string;
  cargoType: string;
  cargoCategory: string;
  length: number;
  width: number;
  height: number;
  volume: number;
  weight: number;
  occupiedPlacesCount: number;
  fragile: boolean;
  needPackage: boolean;
  packageCount: number;
}

// TODO: Выпилить в 2026-м году не используемые типы
export interface Order {
  country?: string;
  region?: string;
  city?: string;
  street?: string;
  house?: string;
  waitTime: string;
  latitude: string;
  longitude: string;
  checkinAutomatic: string;
  checkinManual: string;
  checkinOnlyManual: string;
  active: string;
  existInVspGosbTbRegistry: string;
  segments: Segment[];
  editable: boolean;
  cargoDetails: CargoDetails;
  cargoCategory: string;
  totalSizes: string | any;
  source: Source;
  calculatedTariff: TariffCost;
  priceDetails: PriceDetails;
  loaderCost: string;
  qrs?: string[];
  firstName?: string;
  lastName?: string;
  patronymic?: string;
  personnelNumber?: string;
  departmentId?: string;
  userId?: string;
  positionId?: string;
  organizationId?: string;
  departmentName?: string;
  mobilePhone?: string;
  id: UUID;
  humanReadableId: string;
  author: any;
  authorPhone: string;
  cargoTransportType: string;
  transportType: string;
  transportTypeRus: string;
  tariffId: UUID;
  desiredDate: number;
  desireDate: string | number;
  deliveryTimeDate: number;
  comment: string;
  commentEng: string;
  approvedBy: string | unknown;
  approvalState: string;
  approvalDate: string;
  status: string | unknown;
  creationTime: number;
  finishedTime: number;
  commentForDriver?: string;
  sender: any;
  senderPhone: string;
  senderAddress: string;
  recipient: any;
  recipientPhone: string;
  recipientAddress: string;
  deadlineTime?: number;
  shipmentTime: string;
  actualShipmentTime: string;
  sourceLoadersCount: number;
  destinationLoadersCount: number;
  contractorInfo: any;
  carInfo: any;
  expected?: any;
  cargoTitle: string;
  cargoTypes: string;
  volume: string;
  weight: string;
  cargoSpace: string;
  routeNumber: string;
  totalWeight: string;
  totalVolume: string;
  plannedDistance: string;
  plannedCost: string;
  totalCost: string;
  transferTime: string;
  loaders: number;
  packages: Packages;
  waypoints: RouteWaypointType[] | OrderWaypointType[];
  additionalSenderContacts: Contact[];
}

interface Auto {
  capacity: {
    capacity: number;
    id: string;
  };
  height: number;
  id: string ;
  length: number;
  name: string;
  volume: number;
  width: number;
}

export interface PriceDetails {
  baseCost: number;
  expressCost: number;
  loaderCost: number;
  distance?: number;
  hourTariff?: number;
  includedHours?: number;
  packageCost?: number;
}

export interface Package {
  id?: string;
  count: number;
}
export interface Contact {
    mobilePhone: string,
    employeeId: string,
    fullName: string,
    waypointId?: string,
}

export interface AddDelegatePayload {
    fullName: string;
    mobilePhone: string;
    employeeId: string;
    waypointId?: string;
}
export interface Point {
  addressStringRepresentation: string;
  city: string;
  contact: Contact
  country: string;
  house: number;
  latitude: number;
  longitude: number;
  organization: string;
  region: string;
  street: string;
  type?: string;
}

export interface OrderField {
  field: string;
  value: string | AddDelegatePayload[];
}
export interface OrderFieldResult {
  field: string;
  status: string;
}

export type AvailableOrderField = 'commentEng' | 'calculatedTariff' | 'loaders' | 'desireDate';

export type OrderParams = Record<AvailableOrderField, string | number | TariffCost>;

export interface TariffRequestMulti {
  organizationId: string;
  time: number;
  weight: number;
  volume: number;
  distance: number;
  startPoint: Point;
  stopPoint: Point;
  express?: boolean;
  countPoint: number;
  loaders: number;
  packages?: Package[];
  tripDate?: string;
  orderId?: UUID;
  cargoCategory?: string;
}
export interface TariffCost {
  auto?: Auto;
  active?: boolean;
  contragent?: string;
  contragentId?: string;
  cost: number;
  deliveryTime: number;
  distance: number;
  humanReadableId?: string;
  id: string;
  idx?: number;
  priceDetails: PriceDetails;
  workGroup?: string;
  transportType: {
    id: string;
    name: string;
    nameRus: string;
  };
}

export interface RouteLoadersPayload {
  field: string;
  value: number;
}

interface Passenger {
  costCenter?: number;
  departmentId?: UUID;
  firstName?: string;
  humanReadableId?: string;
  id: UUID;
  itinerantType?: string;
  lastName?: string;
  marriageCertificateNumber?: string;
  organizationId?: UUID;
  patronymic?: string;
  personnelNumber?: string;
  phone?: string;
  positionId?: UUID;
  positionName?: string;
  userId?: UUID;
}

export interface SearchResponse {
  totalPages: number;
  totalElements: number;
  size: number;
  content: Order[];
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  numberOfElements: number;
  pageable: {
    offset: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    pageNumber: number;
    pageSize: number;
    paged: boolean;
    unpaged: boolean;
  };
  first: boolean;
  last: boolean;
  empty: boolean;
}

export interface CategoryItem {
  type: Tab;
  category: Category;
  name: string;
  available?: boolean;
  allowedRoles?: Roles[];
}

export interface OrderDetailedProps {
  data: Order;
  className?: string;
}

export type CargoEngineerComment = {
  field: string; value: string
};

export interface CargoEngineerCommentPayload {
  field: string;
  value: string;
}

export type SortDirection = 'asc' | 'desc' | null;

export enum SortableFields {
  HUMAN_READABLE_ID = 'humanReadableId',
}
