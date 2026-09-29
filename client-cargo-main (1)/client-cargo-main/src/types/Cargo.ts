import { EmployeeModel } from '@sber-sbertransport/mf-core';
import * as t from 'io-ts';
import { RequestRoute } from 'shared/models/geo/types';

import { PriceDetails, TariffType } from 'stores/CargoTariff/CargoTariff.interface';
import { CargoTypeCategoryNameEnum, CargoTypeNameEnum } from 'stores/CargoType/CargoType.interface';
import { TransportTypeEnum as EmployeeTransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import * as tt from 'utils/io-ts';
import { ioTypeFromEnum } from 'utils/ioTypeFromEnum';

export enum TransportTypeEnum {
  DEDICATED = 'DEDICATED',
  COURIER = 'COURIER',
  INTERREGIONAL = 'INTERREGIONAL',
  DOMESTIC_COURIER = 'DOMESTIC_COURIER',
  INDIVIDUAL = 'INDIVIDUAL',
}

export const transportTypeTitles = {
  [TransportTypeEnum.DEDICATED]: 'Доставка сборным грузом',
  [TransportTypeEnum.COURIER]: 'Курьерская доставка',
  [TransportTypeEnum.INTERREGIONAL]: 'Межрегиональная доставка',
  [TransportTypeEnum.DOMESTIC_COURIER]: 'Внутренний курьер',
  [TransportTypeEnum.INDIVIDUAL]: 'Доставка выделенным транспортом',
};

export enum CargoCategoryEnum {
  OTHER = 'OTHER',
}

export enum AccessControlEnum {
  PUBLIC = 'PUBLIC',
  PERSONAL = 'PERSONAL',
  ORGANIZATION = 'ORGANIZATION',
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
  gosb?: string;
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
    cargoType: ioTypeFromEnum('cargoType', CargoTypeNameEnum),
    cargoCategory: ioTypeFromEnum('cargoCategory', CargoTypeCategoryNameEnum),
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
    cargoType: ioTypeFromEnum('cargoType', CargoTypeNameEnum),
    cargoCategory: ioTypeFromEnum('cargoCategory', CargoTypeCategoryNameEnum),
    category: ioTypeFromEnum('cargoCategory', CargoTypeCategoryNameEnum),
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

export const cargoPersonalListItem = t.type({
  name: t.string,
  type: ioTypeFromEnum('type', CargoTypeNameEnum),
  category: ioTypeFromEnum('category', CargoTypeCategoryNameEnum),
  length: t.number,
  width: t.number,
  height: t.number,
  weight: t.number,
  volume: t.number,
  accessLevel: ioTypeFromEnum('accessLevel', AccessControlEnum),
});

export type CargoPersonalListItemType = t.TypeOf<typeof cargoPersonalListItem>;

export const BackendCargoPersonalListItem = t.intersection([
  cargoPersonalListItem,
  t.type({
    id: tt.uuid,
    employeeId: tt.uuid,
    universal: t.boolean,
    active: t.boolean,
  }),
]);

export type BackendCargoPersonalListItemType = t.TypeOf<typeof BackendCargoPersonalListItem>;

export type CargoListItem = {
  id: string;
  position: number;
  cargoName: string;
  cargoType: CargoTypeNameEnum;
  cargoCategory: CargoTypeCategoryNameEnum;
  category: CargoTypeCategoryNameEnum;
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

export interface CalculatedTariff {
  id: string;
  cost: number;
  distance: number;
  priceDetails: PriceDetails;
  deliveryTime: number;
  active?: boolean;
  transportType: {
    id: string;
    name: TariffType;
    nameRus: string;
  };
  loaders: number;
}

// То что отправляется на сохранение
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
  expected: RequestRoute;
  comment: string;
  express: boolean;
  calculatedTariff: CalculatedTariff;
  source: string;
}

interface ApprovedBy {
  departmentId: string;
  email: string;
  firstName: string;
  humanReadableId: string;
  id: string;
  lastName: string;
  mobilePhone: string;
  organizationId: string;
  patronymic: string;
  personnelNumber: string;
  positionId: string;
  userId: string;
}

interface ContractorInfo {
  contactPersonInfo: string;
  contactPersonPhone: string;
  contractorLogin: string;
  contractorPassword: string;
  contractorUrl: string;
  id: string;
  inn: string;
  integrationType: string;
  name: string;
}

export type RequestResponseSingleOrder = {
  approvalDate: string;
  approvalState: string;
  status: string;
  approvedBy: ApprovedBy;
  contractorInfo: ContractorInfo;
  creationTime: string;
  destinationLoadersCount: number;
  humanReadableId: string;
  id: string;
  listCargo: CargoListItem[];
  recipient: EmployeeModel;
  sender: EmployeeModel;
  sourceLoadersCount: number;
  totalSizes: TotalSizes;
  transportType: TransportTypeEnum;
  transportTypeRus: typeof transportTypeTitles;
} & RequestObject;
