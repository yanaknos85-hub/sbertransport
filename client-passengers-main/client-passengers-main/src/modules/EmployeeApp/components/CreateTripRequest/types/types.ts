/* eslint-disable @typescript-eslint/no-explicit-any */

import { Employee, PersonalCar } from '@sber-sbertransport/mf-core';
import { FormInstance } from 'antd/lib/form';
import { Moment } from 'moment';
import { Dispatch, SetStateAction } from 'react';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripRequestModel } from 'stores/Trip/models';
import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';
import {
  BusEnum,
  ExternalProviderEnum,
  GroupTransferClassesEnum,
  GroupTransferEnum,
  ITripTariff,
  TExternalPrices,
  TaxiClassEnum,
  TaxiEnum,
  Transport,
  TripPurpose
} from 'stores/Trip/Trip.interface';

import { TripRequestField } from '../../EditTripRequestForm';
import Process from '../../Evaluation/Constants/Process';
import { IProduct } from '../../TransportOrder/types';

export interface DatePurposeValidationObject {
  purposeId: string;
  isValid: boolean;
}

export interface SuitableTrip {
  date: string;
  time: string;
  name: string;
  adress: string;
}

export interface PurposesProps {
  isLoadingPurposes: boolean | string | undefined;
  setCurrentDateAndPurpose: Dispatch<SetStateAction<DatePurposeValidationObject>>;
  currentDateAndPurpose: DatePurposeValidationObject;
  purposes: TripPurpose[];
  form: any;
}

export interface AddressViewItem { value: string; visible: boolean }

export type AddressViewList = Record<string, AddressViewItem>;

export interface WaypointsProps {
  setCurrentAddressInput(value: number): void;
  currentAddressInput: number;
  addWaypoint?: any;
  style?: React.CSSProperties;
}

export interface TariffExternal {
  type: TaxiClassEnum;
  key: string;
}

export interface TransportTypesProps {
  tariffsInfo: ITripTariff[];
  tariffExternal?: TariffExternal;
  externalPrices?: TExternalPrices[];
  request?: TripRequestModel | null;
  form?: FormInstance;
  purpose?: string;
  isExternal?: boolean;
  availableTaxiClasses?: TaxiClassEnum[];
  product?: IProduct;

  setCurrentTransportType?: Dispatch<SetStateAction<Transport | undefined>>;
  setProduct?: Dispatch<TransportTypeEnum>;
  setProcess?: Dispatch<Process>;
  setStep?: Dispatch<number>;
  setSubClass?: (value?: TaxiEnum | BusEnum | GroupTransferClassesEnum) => void;
  setExternalProvider?: (value: ExternalProviderEnum) => void;
  loadExternalPrices?: () => void;
  handleNextStep?: () => void;
}

export interface AdditionalProps {
  form: any;
  transport?: TaxiClassEnum | TransportTypeEnum;
  submitDisabled: boolean;
  coopTripData: CoopTrip;
  onCommonFinish: (
    data: any,
    isTripSearching?: boolean,
    isSuitabelTrip?: boolean,
    currentJoiningTrip?: TripSuitableModel
  ) => void;
  disabledFields?: TripRequestField[];
  personalCar?: PersonalCar;
  isExternal?: boolean;
  step?: number;
  disabled?: boolean;
  productTransportType?: TransportTypeEnum;
  selectedQuantity?: string;
  setSelectedQuantity?: React.Dispatch<React.SetStateAction<string>>;
  passengerCount?: number;
}

export interface CoopTrip {
  applyCoopTrip: (
    data: FormValues,
    isTripSearching: boolean,
    coopTripId: string | undefined,
    tariffsInfo: ITripTariff[],
    currentJoiningTrip?: TripSuitableModel
  ) => void | undefined;
  searchingMessage: () => string;
  suitableCooperativeTrips: TripSuitableModel[];
  isSearchingSuitableTrip: boolean;
  isSearchedSuitableTrip: boolean;
}

export interface TChildSeatDetails {
  group1: number;
  group2: number;
  booster: number;
  newborn: number;
}

export interface TInformation {
  childSeat?: true;
  childSeatDetails: TChildSeatDetails;
  bugsOversized?: true;
  bugsOversizedComment?: string;
  bugs?: true;
  bugsComment: string;
  animal?: true;
  animalComment?: string;
  phoneHotel: string;
  numberFlight?: string;
  dateFlight?: number;
  addContact?: string;
  typeVehicle?: string;
  clientFullName?: string;
  clientInfoPhone?: string;
}

export interface FormValues {
  coopTrip?: boolean;
  coopTripId?: string;
  date?: Moment;
  passenger: 'me' | 'notme';
  passengerCount?: number;
  preferences?: string[];
  purpose?: string;
  taxiClass?: TransportTypeEnum;
  waypoints: { waypoint: string; waitTime: any }[];
  when: 'notnow' | 'now';
  tariffId?: string;
  employee?: string;
  commentary?: string;
  personalCar?: PersonalCar;
  contractorId?: string;
  busCount?: number;
  busRentDuration?: number;
  busClass?: string;
  information?: TInformation;
  groupTransferClass?: GroupTransferEnum;
  listEmployees?: Employee[];
  commentForPurpose?: string;
}

export enum FileStatus {
  UPLOADING = 'uploading',
  DONE = 'done',
}
export interface FileCompensation extends File {
  originFileObj: File;
  status: FileStatus;
}
