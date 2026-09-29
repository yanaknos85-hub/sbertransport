import { EmployeeModel } from '@sber-sbertransport/mf-core';

export enum CargoTypeCategoryNameEnum {
  REGULAR = 'REGULAR',
  LIQUID = 'LIQUID',
  BULK = 'BULK',
  CORRESPONDENCE = 'CORRESPONDENCE',
}

export enum SortProperty {
  CREATION_DATE = 'CREATION_DATE',
}

export enum ContractsTabs {
  AVAILABLE = 'available',
  NON_TERMINAL = 'non_terminal',
  TERMINAL = 'terminal',
}

export enum TransportTypeEnum {
  DEDICATED = 'DEDICATED',
  COURIER = 'COURIER',
  INTERREGIONAL = 'INTERREGIONAL',
  DOMESTIC_COURIER = 'DOMESTIC_COURIER',
  INDIVIDUAL = 'INDIVIDUAL',
}

export enum Statuses {
  CARGO_ACCEPTED = 'CARGO_ACCEPTED',
  CARGO_AWAITING_APPROVAL = 'CARGO_AWAITING_APPROVAL',
  CARGO_APPROVED = 'CARGO_APPROVED',
  CARGO_PLANNING = 'CARGO_PLANNING',
  CARGO_AWAITING_TRANSFER = 'CARGO_AWAITING_TRANSFER',
  CARGO_AWAITING_DATA = 'CARGO_AWAITING_DATA',
  CARGO_TRANSFER_FINISHED = 'CARGO_TRANSFER_FINISHED',
  CARGO_SHIPMENT_FINISHED = 'CARGO_SHIPMENT_FINISHED',
  CARGO_DELIVERY_CONFIRMATION_FINISHED = 'CARGO_DELIVERY_CONFIRMATION_FINISHED',
  CARGO_CANCELED = 'CARGO_CANCELED',
}

export enum CargoTypeNameEnum {
  OTHER = 'OTHER',
  TECHNIQUE = 'TECHNIQUE',
  DOCUMENT = 'DOCUMENT',
  DOCUMENT_CARS = 'DOCUMENT_CARS',
  TABLEWARE = 'TABLEWARE',
  CLOTHES = 'CLOTHES',
  FURNITURE = 'FURNITURE',
  FOOD_PRODUCTS = 'FOOD_PRODUCTS',
  TOOLS = 'TOOLS',
  MATERIALS = 'MATERIALS',
  HOUSEHOLD_GOODS = 'HOUSEHOLD_GOODS',
}

export interface Sizes {
  width: number;
  length: number;
  height: number;
  volume: number;
  weight: number;
}

export type CargoListItem = {
  id: string;
  position: number;
  cargoName: string;
  cargoType: CargoTypeNameEnum;
  cargoCategory: CargoTypeCategoryNameEnum;
  category: CargoTypeCategoryNameEnum;
  orderingIndex: number;
  occupiedPlacesCount: number;
  fragile?: boolean;
  needPackage?: boolean;
  packageCount?: number;
  image?: string;
  cargoTypeValue?: string;
} & Sizes;

export interface Filters {
  addressFrom: string;
  addressTo: string;
  desiredDate: {
    start: string;
    end: string;
  };
}

export interface Coordinates {
  latitude: number;
  longitude: number;
}

export interface Segment {
  distance: number;
  time: number;
  coordinates: Coordinates[];
}

export interface Evaluation {
  routelistId: string;
  reasons: string[];
  rating: number;
  comment: string;
}

interface Request {
  humanReadableId: string;
  waypointType: string;
  organization: string;
  cargo: CargoListItem[];
  comment: string;
}

export interface Contacts {
  requests: Request[];
  contact: {
    phone: string;
    name: string;
  };
}

export interface WaypointExchange {
  id: string;
  type: string;
  address: {
    addressStringRepresentation: string;
    coordinates: Coordinates;
  };
  orderingIndex: number;
  contacts: Contacts[];
}

export interface ListItemExchange {
  id: string;
  humanReadableId: string;
  transportType: TransportTypeEnum;
  author: EmployeeModel;
  executor: EmployeeModel;
  creationTime: string;
  desiredDate: string;
  timeZone: string;
  active: boolean;
  transportTypeRus: string;
  comment: string;
  shipmentTime: string;
  waypoints: WaypointExchange[];
  segments: Segment[];
  organizations: string;
  regionId: string;
  cost: number;
  distance: number;
  volume: number;
  weight: number;
  status: string;
  addRequests: string;
  deliveryTime: string;
  workgroup: string;
  evaluation: Evaluation;
}

export interface ExchangeResponse {
  content: ListItemExchange[];
  pageable: {
    sort: { sorted: boolean; unsorted: boolean; empty: boolean };
    offset: number;
    pageNumber: number;
    pageSize: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: { sorted: boolean; unsorted: boolean; empty: boolean };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

export interface DetailedViewExchange {
  id: string;
  humanReadableId: string;
  transportType: TransportTypeEnum;
  transportTypeRus: string;
  author: EmployeeModel;
  desiredDate: string;
  waypoints: WaypointExchange[];
  segments: Segment[];
  weight: number;
  volume: number;
  cargoDetails: CargoListItem[];
  comment: string;
  status: Statuses;
  creationTime: string;
  distance: number;
  evaluation: Evaluation;
  commentEng: string;
  timezone: string;

  cost: number;
}
