/* eslint-disable @typescript-eslint/no-explicit-any */

import { PersonalCar } from '@sber-sbertransport/mf-core';
import * as personalTypes from '@sber-sbertransport/mf-core';
import { Moment } from 'moment';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';
import {
  BusEnum, ExternalProviderEnum, GroupTransferClassesEnum, ITripTariff, TaxiClassCleanEnum, TaxiEnum
} from 'stores/Trip/Trip.interface';
import { CoopTrip } from '../CreateTripRequest/hooks/useCooperativeTrip';

export interface FormValues {
  coopTrip: boolean;
  coopTripId?: string;
  date?: Moment;
  passenger: 'me' | 'notme';
  passengerCount: number;
  preferences?: string[];
  purpose?: string;
  transportType?: TransportTypeEnum;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
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
  taxiClass?: TransportTypeEnum;
  isSuitabelTrip?: boolean;
  timeZone?: string;
}

export interface IRunParams {
  data?: FormValues;
  isTripSearching?: boolean;
  isSuitabelTrip?: boolean;
}

export enum Passenger {
  ME = 'me',
  NOTME = 'notme',
}

export enum TripEvent {
  NOW = 'now',
  NOTNOW = 'notnow',
}

export interface IProduct extends FormValues {
  step: number;
  isCreating: boolean;
  personalCar?: PersonalCar;
  busCreateEnabled?: boolean;
  currentAddressInput?: number;
  isExternal?: boolean;
  isExternalPriceLoading?: boolean;
  isExternalTaxiClass?: TaxiClassCleanEnum;
  externalProvider?: ExternalProviderEnum;
  subClass?: TaxiEnum | BusEnum | GroupTransferClassesEnum;

  addWaypoint(waypoint: any): void;
  setStep(step: number): void;
  setCreating(value: boolean): void;
  setPersonalCar(car: personalTypes.PersonalCar): void;
  setBusCreateEnabled(value: boolean): void;
  setCurrentAddressInput(value: number): void;
  setExternal(value: boolean): void;
  setExternalPriceLoading(value: boolean): void;
  setExternalProvider(value: ExternalProviderEnum): void;
  setSubClass(value?: TaxiEnum | BusEnum | GroupTransferClassesEnum): void;

  onFinish(
    onFinish: any,
    updatedData: any,
    externalLinks: any,
    applyCoopTrip: (
      data: FormValues,
      isTripSearching: boolean,
      coopTripId: string | undefined,
      tariffsInfo: ITripTariff[],
      currentJoiningTrip?: TripSuitableModel
    ) => void | undefined,
    tariffsInfo: any,
    coopTrip: CoopTrip,
    step?: number,
    isSuitabelTrip?: boolean
  ): Promise<void>;
}
