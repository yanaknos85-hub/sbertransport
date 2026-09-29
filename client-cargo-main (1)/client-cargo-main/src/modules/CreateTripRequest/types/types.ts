import { Dispatch, SetStateAction } from 'react';
import { PersonalCar } from '@sber-sbertransport/mf-core';
import { FormInstance } from 'antd/lib/form';
import { Moment } from 'moment';

import { IGeoStore } from 'stores/Geo/Geo.interface';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripRequestModel } from 'stores/Trip/models';
import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';
import {
  ITripTariff, TaxiClassEnum, TExternalPrices, Transport, TripPurpose
} from 'stores/Trip/Trip.interface';

import { TripRequestField } from '../../EditTripRequestForm';

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

export interface WaypointsProps {
  setCurrentAddressInput: Dispatch<SetStateAction<number>>;
  geo: IGeoStore;
  getGeolocation: () => void;
  currentAddressInput: number;
}

export interface TransportTypesProps {
  tariffsInfo: ITripTariff[];
  externalPrices?: TExternalPrices[];
  request?: TripRequestModel | null;
  form?: FormInstance;
  purpose?: string;
  setCurrentTransportType?: Dispatch<SetStateAction<Transport | undefined>>;
  isExternal?: boolean;
}

export interface AdditionalProps {
  form: any;
  transport?: TaxiClassEnum | TransportTypeEnum;
  submitDisabled: boolean;
  coopTripData: CoopTrip;
  onCommonFinish: (data: any, isTripSearching?: boolean, currentJoiningTrip?: TripSuitableModel) => void;
  disabledFields?: TripRequestField[];
  personalCar?: PersonalCar;
  isExternal?: boolean;
}

export interface CoopTrip {
  applyCoopTrip: (
    data: FormValues,
    isTripSearching: boolean,
    coopTripId: string,
    tariffsInfo: ITripTariff[],
    currentJoiningTrip?: TripSuitableModel
  ) => void | undefined;
  searchingMessage: () => string;
  suitableCooperativeTrips: TripSuitableModel[];
  isSearchingSuitableTrip: boolean;
  isSearchedSuitableTrip: boolean;
}

export interface FormValues {
  coopTrip: boolean;
  coopTripId: string;
  date: Moment;
  passenger: 'me' | 'notme';
  passengerCount: number;
  preferences: string[];
  purpose: string;
  taxiClass: keyof typeof TransportTypeEnum;
  waypoints: { waypoint: string; waitTime: any }[];
  when: 'notnow' | 'now';
  tariffId: string;
  employee: string;
  commentary: string;
  personalCar: PersonalCar;
  contractorId?: string;
}
