import { EmployeeModel } from '@sber-sbertransport/mf-core';
import * as t from 'io-ts';
import { Moment } from 'moment';

import { ioTypeFromEnum } from '../../utils/ioTypeFromEnum';
import { CargoTypeCategoryNameEnum, CargoTypeNameEnum } from '../CargoType/CargoType.interface';
import { Periodicity } from './types';

export enum AddressLoadType {
  LOAD = 'LOAD',
  UNLOAD = 'UNLOAD',
}

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
  }),
]);

export type TotalSizes = t.TypeOf<typeof totalSizes>;

export const CargoListItem = t.intersection([
  Sizes,
  t.type({
    position: t.number,
    cargoName: t.string,
    cargoType: ioTypeFromEnum('cargoType', CargoTypeNameEnum),
    cargoCategory: ioTypeFromEnum('cargoCategory', CargoTypeCategoryNameEnum),
    occupiedPlacesCount: t.number,
  }),
  t.partial({
    id: t.string,
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
  cargoType: CargoTypeNameEnum;
  cargoCategory: CargoTypeCategoryNameEnum;
  occupiedPlacesCount: number;

  fragile?: boolean;
  needPackage?: boolean;
  packageCount?: number;
  image?: string;
  cargoTypeValue?: string;
} & Sizes;

export const AddressMulti = t.intersection([
  t.type({
    label: t.string,
    name: t.string,
    type: t.string,
    rules: t.array(t.record(t.string, t.union([t.string, t.boolean]))),
  }),
  t.partial({
    id: t.string,
    index: t.number,
    params: t.record(t.string, t.unknown),
  }),
]);

export const ContactMulti = t.intersection([
  t.type({
    fullName: t.string,
    mobilePhone: t.string,
  }),
  t.partial({
    id: t.string,
    organizationId: t.string,
    firstName: t.string,
    lastName: t.string,
    patronymic: t.string,
    employeeId: t.string,
  }),
]);

export type ContactMulti = t.TypeOf<typeof ContactMulti>;

export const PackageMulti = t.intersection([
  t.partial({
    id: t.string,
  }),
  t.type({
    count: t.number,
  }),
]);

export type PackageMulti = t.TypeOf<typeof PackageMulti>;

const PackageTariffMulti = t.type({
  id: t.string,
  cost: t.number,
});

const PackageFormValues = t.type({
  loadersForm: t.number,
  packagesFormList: t.array(PackageMulti),
});

export type PackageFormValues = t.TypeOf<typeof PackageFormValues>;

export const PointMulit = t.intersection([
  t.type({
    country: t.string,
    region: t.string,
    city: t.string,
    street: t.string,
    house: t.string,
    latitude: t.number,
    longitude: t.number,
  }),
  t.partial({
    existInVspGosbTbRegistry: t.string,
    gosb: t.string,
  }),
]);

export const TariffRequestMulti = t.intersection([
  t.type({
    organizationId: t.string,
    time: t.number,
    weight: t.number,
    volume: t.number,
    distance: t.number,
    startPoint: PointMulit,
    countPoint: t.number,
    express: t.boolean,
  }),
  t.partial({
    stopPoint: PointMulit,
    priceDetails: t.string,
  }),
]);

const PriceDetailsMulti = t.intersection([
  t.type({
    baseTariff: t.number,
    baseCost: t.number,
    includedHours: t.number,
    hourTariff: t.number,
  }),
  t.partial({
    loaderTariff: t.number,
    loaderCost: t.number,
    packageTariff: t.array(PackageTariffMulti),
    packageCost: t.number,
    expressCost: t.number,
  }),
]);

export const CalculatedTariffMulti = t.type({
  id: t.string,
  cost: t.number,
  deliveryTime: t.number,
  contragent: t.string,
  contragentId: t.string,
  distance: t.number,
  priceDetails: PriceDetailsMulti,
});

export const CoordinatesMulti = t.partial({
  latitude: t.number,
  longitude: t.number,
});

export type CoordinatesMulti = t.TypeOf<typeof CoordinatesMulti>;

export const SegmentMulti = t.intersection([
  t.type({
    distance: t.number,
    coordinates: t.array(CoordinatesMulti),
  }),
  t.partial({
    time: t.number,
  }),
]);

const Kic = t.type({
  id: t.string,
  name: t.string,
  code: t.string,
  address: t.string,
});

export const WaypointMulti = t.intersection([
  t.type({
    type: t.union([t.literal(AddressLoadType.LOAD), t.literal(AddressLoadType.UNLOAD)]),
    orderingIndex: t.number,
    distance: t.number,
    addressStringRepresentation: t.string,
    country: t.string,
    region: t.string,
    city: t.string,
    street: t.string,
    house: t.string,
    building: t.string,
    structure: t.string,
    latitude: t.number,
    longitude: t.number,
    contacts: t.array(ContactMulti),
    existInVspGosbTbRegistry: t.boolean,
    addressType: t.union([t.literal('ORDINARY'), t.literal('VSP'), t.literal('VSP_KIC')]),
  }),
  t.partial({
    gosb: t.string,
    organization: t.string, // организация участника => организация контакта из поля ФИО
    kic: Kic,
    entrance: t.number,
    floor: t.number,
    flat: t.number,
  }),
]);

export type WaypointMulti = t.TypeOf<typeof WaypointMulti>;

export const OrderRequestMulti = t.intersection([
  t.type({
    desiredDate: t.string,
    author: ContactMulti,
    organizationId: t.string,
    cargoDetails: t.array(CargoListItem),
    calculatedTariff: CalculatedTariffMulti,
    waypoints: t.array(WaypointMulti),
    source: t.literal('WEB'),
    packages: t.array(PackageMulti),
  }),
  t.partial({
    deliveryTime: t.number,
    distance: t.number,
    segments: t.array(SegmentMulti),
    time: t.number,
    cost: t.number,
    loadersNeeded: t.boolean,
    loaders: t.number,
    comment: t.string,
    internalNote: t.string,
    requestType: t.string,
  }),
]);

export type OrderRequestMulti = t.TypeOf<typeof OrderRequestMulti>;

const Period = t.intersection([
  t.type({
    periodType: ioTypeFromEnum<Periodicity>('Periodicity', Periodicity),
    dayOfWeek: t.array(t.number),
    weekOfMonth: t.array(t.number),
    monthOfQuartal: t.array(t.number),
    beginDate: t.string,
    endDate: t.string,
  }),
  t.partial({
    cron: t.string,
    countRequest: t.number,
    cost: t.number,
  }),
]);

export type PeriodType = t.TypeOf<typeof Period>;

export const OrderRequestRegularMulti = t.type({
  active: t.boolean,
  period: Period,
  source: t.literal('WEB'),
  template: OrderRequestMulti,
});

export type OrderRequestRegularMulti = t.TypeOf<typeof OrderRequestRegularMulti>;

const AddressListMultiForm = t.intersection([
  t.type({
    address: t.string,
    name: t.string,
    phone: t.string,
    type: t.string,
  }),
  t.partial({
    organization: t.string,
    entrance: t.number,
    floor: t.number,
    flat: t.number,
    extraContactName: t.union([t.string, t.undefined]),
    extraContactPhone: t.union([t.string, t.undefined]),
  }),
]);

export type AddressListMultiForm = t.TypeOf<typeof AddressListMultiForm>;

export interface StepAddressValuesMulti {
  sender: EmployeeModel | null;
  desiredDate: Moment;
  loaders?: number;
  waypoints: AddressListMultiForm[];
}
