import { Employee, IOHumanReadable } from '@sber-sbertransport/mf-core';
import * as t from 'io-ts';
import { RouteModel } from 'shared/models/geo/Route.model';
import { RequestRoute, TWaypoint } from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import { tariffCost, TariffType } from 'stores/CargoTariff/CargoTariff.interface';
import {
  CargoListItem, cargoListItem,
  RequestObject, TransportType
} from 'types/Cargo';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';
import { SortType, SortTypeOrder } from 'modules/CargoMassMultiple/components/SortOrder/SortOrder';
import { RequestObjectMulti } from 'modules/CargoMassMultiple/types';

import { RequestModel } from './models/CargoMassRequestMultiple.model';

export enum RequestStatus {
  SUCCESS = 'SUCCESS',
  ERROR = 'ERROR',
  IN_PROGRESS = 'IN_PROGRESS',
}

export interface RequestServer {
  id: string;
  count: number;
  countAll: number;
  date: string;
  status: RequestStatus;
  data: RequestListItem[];
  description?: string;
}

export interface Request {
  id: string;
  count: number;
  countAll: number;
  date: string;
  status: RequestStatus;
  data: RequestModel[];
  description?: string;
}

export interface ICargoMassStoreMultiple {
  uploadRequestId: string;
  regularMassId: string;
  request: Request | undefined;
  requestList: RequestListItem[];
  loadRequest(): Promise<Request | undefined>;
  prepareRequest(): void;
  saveRequestItem(requestId: string, data: RequestListItem): void;
  deleteRequestItem(requestId: string): number;
  sortRequestList(type: SortType, order: SortTypeOrder): void;
  selectTariffCargo(requestId: string, tariffType: TariffType): void;
  selectAllTariffCargo(tariffType: TariffType): void;
  saveFile(file: File): Promise<void>;
  saveFileRegular(file: File): Promise<void>;
  postData(data: RequestObjectMulti[]): Promise<void>;
  postRegularData(data: RequestObject[]): Promise<void>;
  loadRegularRequest(): Promise<Request | undefined>;
  loadRegularRequestMulti(): Promise<Request | undefined>;
  totalData: TotalData;
  totalSizes: TotalSizes;
  totalExpected: RouteModel;
}

export interface ICargoMassServiceMultiple {
  getRegularRequest(id: string): Promise<RequestServer | undefined>;
  getRequest(id: string): Promise<RequestServer | undefined>;
  saveFile(file: File): Promise<SavedFileInfo | void>;
  saveFileRegular(file: File): Promise<SavedFileInfo | void>;
  calcRoute(route: WaypointModel[]): Promise<RequestRoute>;
  postData(data: RequestObjectMulti[]): Promise<void>;
  postRegularData(data: RequestObject[]): Promise<SavedFileInfo>;
}

export interface TariffCost {
  totalCost: number;
  totalTime: number;
  details: Record<TransportType, { cost: number; time: number; count: number }>;
}

export type TotalData = Pick<RequestListItem, 'deliveryTimeDate'> & {
  expected: Pick<RouteModel, 'cost' | 'time' | 'distance'>;
  sizes: TotalSizes;
  tariffCost: TariffCost;
};

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

export const SavedFileInfo = t.strict({
  requestId: t.string,
  started: t.boolean,
});

export type SavedFileInfo = t.TypeOf<typeof SavedFileInfo>;

const User = t.intersection([
  t.type({
    id: t.string,
    fullName: t.string,
    address: t.string,
    mobilePhone: t.string,
    firstName: t.string,
    lastName: t.string,
    patronymic: t.string,
    humanReadableId: t.string,
    personnelNumber: t.string,
    positionId: t.string,
    userId: t.string,
  }),
  t.partial({
    organizationName: t.string,
    organizationId: t.string,
  }),
]);

export type User = t.TypeOf<typeof User>;

const Sizes = t.type({
  width: t.number,
  length: t.number,
  height: t.number,
  volume: t.number,
  weight: t.number,
});

export type Sizes = t.TypeOf<typeof Sizes>;

const TotalSizes = t.intersection([
  Sizes,
  t.type({
    occupiedPlacesCount: t.number,
    packageCost: t.number,
  }),
]);

export type TotalSizes = t.TypeOf<typeof TotalSizes>;

const RequestListItem = t.intersection([
  IOHumanReadable,
  t.type({
    status: ioTypeFromEnum('RequestStatus', RequestStatus),
    id: t.string,
    creationTime: t.union([t.string, t.number]),
    author: Employee, // нужен ли?
    sender: User,
    recipient: User,
    senderOrganization: t.string,
    recipientOrganization: t.string,
    sourceLoaders: t.boolean,
    destinationLoaders: t.boolean,
    desiredDate: t.number,
    deliveryTimeDate: t.number,
    listCargo: t.array(cargoListItem),
    tariffs: t.array(tariffCost),
    totalSizes: TotalSizes,
    expected: RequestRoute,
    express: t.boolean,
    description: t.string,
    sourceLoadersCount: t.number,
    destinationLoadersCount: t.number,
    desiredDateTime: t.number,
  }),
  t.partial({
    externalId: t.string,
    senderOrganization: t.string,
    recipientOrganization: t.string,
    comment: t.string,
    requestNumber: t.number,
    approver: t.string,
  }),
]);

export type RequestListItem = t.TypeOf<typeof RequestListItem>;

export type CargoItemFromRequests = CargoListItem & { requestID: string; waypoints: TWaypoint[] };
