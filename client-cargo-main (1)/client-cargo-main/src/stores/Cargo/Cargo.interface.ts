import { EmployeeModel } from '@sber-sbertransport/mf-core';
import { Moment } from 'moment';

import { TariffCost } from 'stores/CargoTariff/CargoTariff.interface';
import {
  BackendCargoPersonalListItemType,
  CargoListItem, CargoPersonalListItemType, Expected, RequestResponseSingleOrder
} from 'types/Cargo';

import {
  CargoRegularRequestResponse,
  Pack, PackData, PeriodType
} from '../Cargos/types';
import {
  AddressListMultiForm, OrderRequestMulti, OrderRequestRegularMulti,
  WaypointMulti
} from '../Cargos/typesMulti';

export interface StepAddressValues {
  sender: EmployeeModel | null;
  recipient: EmployeeModel | null;
  senderAddress: string;
  desiredDate: Moment;
  senderName: string;
  senderPhone: string;
  senderOrganization: string;
  sourceLoaders: boolean;
  recipientAddress: string;
  recipientName: string;
  recipientPhone: string;
  recipientOrganization: string;
  destinationLoaders: boolean;
  sourceLoadersCount: number;
  destinationLoadersCount: number;
}

export interface StepAddressValuesMulti {
  sender: EmployeeModel | null;
  desiredDate: Moment;
  waypoints?: AddressListMultiForm[];
  internalNote: string;
}

export interface StepCargosValues {
  cargoList: CargoListItem[];
}

export interface StepTariffValues {
  tariffCost: TariffCost | null;
  expected: Expected | null;
  express: boolean;
  comment: string;
  distance?: number;
}

export interface StepTariffValuesMulti {
  tariffCost: TariffCost | null;
  expected: Expected;
  express?: boolean;
  comment?: string;
  distance?: number;
}

export interface StepFinalValues {
  cost: number;
  deliveryTime: number;
  occupiedPlacesCount: number;
  width: number;
  length: number;
  height: number;
  volume: number;
  weight: number;
  comment?: string;
  distance?: number;
}

export interface ExternalEmployee {
  fullName: string;
  mobilePhone?: string;
  fullNameWithCode?: string;
}

export interface PackageItem {
  packageTitle?: string;
  package: string;
  packageCount: number;
}

export interface PackageForm {
  loadersNeeded?: boolean;
  loadersCount?: number;
  packages?: PackageItem[];
}

export interface Package { /* Для CargoRequestModel */
  id: string;
  count: number;
  name: string;
}

export interface FurnitureItemType {
  id: string;
  name: string;
  count: number;
  type: string;
  category: string;
  universal: boolean;
  length: number;
  width: number;
  height: number;
  weight: number;
  volume: number;
}

export interface ICargoStore {
  stepAddressValues: StepAddressValues;
  stepCargosValues: StepCargosValues;
  stepTariffValues: StepTariffValues;
  stepTariffValuesMulti: StepTariffValuesMulti;
  stepFinalValues: StepFinalValues;
  periodValues: PeriodType;
  totalRegularCost: number;
  stepAddressValuesMulti: StepAddressValuesMulti;
  waypointsListMulti: AddressListMultiForm[];
  packs: Pack[];
  packageForm: PackageForm;
  comment: string;
  selectedFlat: { idx: number; type: string };
  lastLoadedFlatType: string | null;
  purpose: {
    REGULAR: boolean;
    RELOCATION: boolean;
    SINGLE: boolean;
  };
  additionalServices: {
    loaders: boolean;
    car: boolean;
    lift: boolean;
  };
  isRepeatOrder: boolean;
  setRepeatOrder(value: boolean): void;
  setComment(value: string): void;
  clearStepValues(): void;
  setStepAddressValues(data: Partial<StepAddressValues>): void; // используется в useFill()
  setStepCargosValues(data: StepCargosValues): void;
  setStepTariffValuesMulti(data: StepTariffValuesMulti): void;
  setStepFinalValues(data: StepFinalValues): void;
  setPeriodValues(data: PeriodType): void;
  postDataMulti(data: OrderRequestMulti): Promise<void>;
  postRegularCargoDataMulti(data: OrderRequestRegularMulti): Promise<void>;
  addAddress(address: WaypointMulti[]): void;
  getPack(): Promise<void>;
  savePackageForm(formValues: PackageForm | {}): void;
  setCheckLoaders(value: boolean): void;
  setRegularOrder(value: boolean): void;
  setRelocationOrder(value: boolean): void;
  getPeriodValuesMulti(data: Partial<PeriodType>): Promise<void>;
  setCargoList(list: CargoListItem[]): void;
  createCargoItemPersonal(cargoItem: CargoPersonalListItemType): Promise<BackendCargoPersonalListItemType>;
  setAdditionalServices(services: Record<string, boolean>): void;
  setLastLoadedFlatType(type: string): void;
  setSelectedFlatType(idx, value: { idx: number; type: string }): void;
}

export interface ICargoService {
  postDataMulti(data: OrderRequestMulti): Promise<RequestResponseSingleOrder | null>;
  getPack(): Promise<PackData>;
  postRegularCargoDataMulti(data: OrderRequestRegularMulti): Promise<CargoRegularRequestResponse>;
  getPeriodValuesMulti(data: Partial<PeriodType>): Promise<any>;
  createCargoItemPersonal(cargoItem: CargoPersonalListItemType): Promise<BackendCargoPersonalListItemType>;
}
