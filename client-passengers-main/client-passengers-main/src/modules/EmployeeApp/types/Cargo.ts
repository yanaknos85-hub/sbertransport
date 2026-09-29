import * as t from 'io-ts';
// loggg - непонятно какого хуя это тут. Пока просто делитнул карго импорты чтоб не ругался тс. После запуска апп удалить
import { TransportTypeEnum as EmployeeTransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';

import { RequestRoute, Segment } from 'shared/models/geo/types';

import * as tt from 'utils/io-ts';

export enum TransportTypeEnum {
  DEDICATED = 'DEDICATED',
  COURIER = 'COURIER',
  INTERREGIONAL = 'INTERREGIONAL',
}

export const transportTypeTitles = {
  [TransportTypeEnum.DEDICATED]: 'Грузовик',
  [TransportTypeEnum.COURIER]: 'Курьер',
  [TransportTypeEnum.INTERREGIONAL]: 'Между регионами',
};

export enum CargoCategoryEnum {
  OTHER = 'OTHER',
}

export type TransportType = TransportTypeEnum;

export enum DeliveryUrgencyEnum {
  express = 'express',
  standart = 'standart',
}

export interface SelfEmployee {
  id: string;
  humanReadableId: string;
  userId?: string;
  firstName?: string;
  lastName?: string;
  patronymic?: string;
  personnelNumber?: string;
  itinerantType?: string;
  mvz?: string;
  marriageCertificateNumber?: string;
  delegatedById?: string;
  supervisorId?: string;
  positionId?: string;
  organizationId?: string;
  departmentId?: string;
  departmentName?: string;
  mobilePhone?: string;
}

export interface Recipient {
  id: string;
  humanReadableId: string;
  userId: string;
  firstName: string;
  lastName: string;
  patronymic: string;
  personnelNumber: string;
  departmentId: string;
  positionId: string;
  mobilePhone: string;
  email: string;
  supervisorId: string;
  delegatedById: string;
  availableTransportTypes: EmployeeTransportTypeEnum[];
  organizationId: string;
  status: 'ACTIVE' | 'INACTIVE';
  approvals: number;
}

export interface Subset {
  time: number;
  distance: number;
}

export interface Point {
  country: string;
  region: string;
  city: string;
  street: string;
  house: number;
  latitude: number;
  longitude: number;
  existInVspGosbTbRegistry?: string;
}

const CargoDetails = t.intersection([
  t.type({
    position: t.number,
    length: t.number,
    width: t.number,
    height: t.number,
    volume: t.number,
    weight: t.number,
    cargoName: t.string,
    occupiedPlacesCount: t.number,
    packageId: tt.uuid,
  }),
  t.partial({
    packageCount: t.number,
    fragile: t.boolean,
    needPackage: t.boolean,
  }),
]);

export type CargoDetails = t.TypeOf<typeof CargoDetails>;

const finalPrice = t.intersection([
  t.type({
    totalPrice: t.number,
    tariffName: t.string,
    cost: t.number,
  }),
  t.partial({ express: t.number, loaderCost: t.number }),
]);

export type FinalPrice = t.TypeOf<typeof finalPrice>;

const Sizes = t.type({
  width: t.number,
  length: t.number,
  height: t.number,
  volume: t.number,
  weight: t.number,
});

export interface Sizes {
  width: number;
  length: number;
  height: number;
  volume: number;
  weight: number;
}

export const totalSizes = t.intersection([
  Sizes,
  t.type({
    occupiedPlacesCount: t.number,
    // packageCost: t.number,
  }),
]);

export type TotalSizes = t.TypeOf<typeof totalSizes>;

export const cargoListItem = t.intersection([
  Sizes,
  t.type({
    id: t.string,
    position: t.number,
    cargoName: t.string,
    occupiedPlacesCount: t.number,
  }),
  t.partial({
    fragile: t.boolean,
    needPackage: t.boolean,
    packageCount: t.number,
    image: t.string,
    cargoTypeValue: t.string,
  }),
]);

export type CargoListItem = {
  id: string;
  position: number;
  cargoName: string;
  occupiedPlacesCount: number;

  fragile?: boolean;
  needPackage?: boolean;
  packageCount?: number;
  image?: string;
  cargoTypeValue?: string;
} & Sizes;

export interface Waypoints {
  country: string;
  region: string;
  city: string;
  street: string;
  house: string;
  building: string;
  structure: string;
  latitude: number;
  longitude: number;
  existInVspGosbTbRegistry?: string;
  waitTime?: string;
  checkinAutomatic: boolean;
  checkinManual: boolean;
  absenceReason: string;
  checkinOnlyManual: boolean;
  active: boolean;
}

export type Expected = RequestRoute | null;

// То что отправляется на сохраненение
export interface RequestObject {
  transportType: TransportType | undefined;
  transportTypeId?: string | undefined;
  desiredDate: number | null | undefined;
  cargoTypeId: string | undefined;
  fragile?: boolean;
  author: SelfEmployee;
  // sender: EmployeeModel | null;
  // recipient: EmployeeModel | null;
  sender: any;
  recipient: any;
  senderOrganization?: string;
  recipientOrganization?: string;
  length: number;
  width: number;
  height: number;
  volume: number;
  weight: number;
  occupiedPlacesCount: number;
  cargoDetails: CargoListItem[];
  sourceLoaders?: boolean;
  destinationLoaders?: boolean;
  tariffId?: string;
  expected: Omit<Expected, 'segments | cost'> & { cost?: number | undefined; segments?: Segment[] | {}[] | undefined };
  comment: string;
  express: boolean;
}
