/* eslint-disable @typescript-eslint/no-explicit-any */
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
  transportType?: string;
  status?: string | string[];
  transportClass?: string;
}

export interface PersonQueryFilters {
  id?: string;
  status?: string[];
  transportType?: string;
  contractor?: UUID;
  departure?: string;
  waypoint?: string;
  destination?: string;
  passengerName?: string;
  transportClass?: string;
  creationTimeFrom?: string;
  creationTimeTo?: string;
  startTimeFrom?: string;
  startTimeTo?: string;

  finishTimeFrom?: string;
  finishTimeTo?: string;
  desiredTimeFrom?: string;
  desiredTimeTo?: string;
  deadlineFrom?: string;
  deadlineTo?: string;
  // deadlineState?: string;
  passengerPhone?: string;
  passengerPosition?: UUID;
  passengerDepartment?: UUID;
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

// TODO: Выпилить в 5-м релиза не используемые типы, после стабилизации 43-го релиза
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

  listCargo: any; // deprecated
  cargoDetails: CargoDetails;
  cargoCategory: string;
  totalSizes: string | any;

  calculatedTariff: any;
  priceDetails: string;
  loaderCost: string;

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
  aprover: string;
  cargoTransportType: string;
  transportType: string;
  transportTypeRus: string;
  tariffId: UUID;
  pickupTime?: number;
  desiredDate: number;
  desireDate: number;
  deliveryTimeDate: number;
  comment: string;
  commentEng: string;
  approvedBy: any;
  approvalState: string;
  approvalDate: string;
  status: any;
  creationTime: number;
  vehicle: any;
  address: any;
  problems: any;
  expediency: string;
  finishedTime: number;
  takeToWorkTime: number;
  driver: any;
  // eslint-disable-next-line no-use-before-define
  passenger: Passenger;
  commentForDriver?: string;
  sender: any;
  senderPhone: string;
  senderAddress: string;
  recipient: any;
  recipientPhone: string;
  recipientAddress: string;
  deadlineTime?: number;
  shipmentTime: string;
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
  moverDepartureAddress: number;
  moverReceivingAddress: number;
  routeNumber: string;
  totalWeight: string;
  totalVolume: string;
  plannedDistance: string;
  plannedCost: string;
  moverCost: string;
  totalCost: string;
  commonComment: string;
  transferTime: string;
  loaders: number;
  packages: Packages;
  waypoints: RouteWaypointType[] | OrderWaypointType[];
}

export interface CargoEngineerCommentPayload {
  field: string;
  value: string;
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

export type CargoEngineerComment = string;
