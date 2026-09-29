/* eslint-disable @typescript-eslint/no-explicit-any */
import type { PersonalCar } from '@sber-sbertransport/mf-core';
import { action, observable } from 'mobx';
import moment from 'moment';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';
import {
  BusEnum, ExternalProviderEnum, GroupTransferClassesEnum, ITripTariff, TaxiEnum
} from 'stores/Trip/Trip.interface';
import { CoopTrip } from '../../CreateTripRequest/hooks/useCooperativeTrip';
import {
  FormValues, IProduct, Passenger, TripEvent
} from '../types';

abstract class AbstractProduct implements IProduct {
  @observable
    coopTrip = true;

  @observable
    coopTripId?: string;

  @observable
    date?: moment.Moment;

  @observable
    passenger = Passenger.ME;

  @observable
    passengerCount = 1;

  @observable
    preferences?: string[];

  @observable
    purpose?: string;

  @observable
    transportType?: TransportTypeEnum;

  @observable
    when = TripEvent.NOW;

  @observable
    tariffId?: string;

  @observable
    employee?: string;

  @observable
    commentary?: string;

  @observable
    personalCar?: PersonalCar;

  @observable
    contractorId?: string;

  constructor(product?: IProduct) {
    this.step = product?.step || 1;
    this.subClass = product?.subClass;
  }

  @observable
    waypoints: { waypoint: string; waitTime: any }[] = [];

  @action.bound
  addWaypoint(waypoint: { waypoint: string; waitTime: any }) {
    this.waypoints = [...this.waypoints, waypoint];
  }

  @observable
    step = 1;

  @action.bound
  setStep(step: number) {
    this.step = step;
  }

  @observable
    isCreating = false;

  @action.bound
  setCreating(value: boolean) {
    this.isCreating = value;
  }

  @observable
    currentAddressInput = 0;

  @action.bound
  setCurrentAddressInput(value: number) {
    this.currentAddressInput = value;
  }

  @observable
    subClass?: TaxiEnum | BusEnum | GroupTransferClassesEnum = TaxiEnum.ECONOMY;

  @action.bound
  setSubClass(value?: TaxiEnum | BusEnum | GroupTransferClassesEnum) {
    this.subClass = value;
  }

  abstract setPersonalCar(car: PersonalCar): void;

  abstract setBusCreateEnabled(value: boolean): void;

  abstract setExternal(value: boolean): void;

  abstract setExternalPriceLoading(value: boolean): void;

  abstract setExternalProvider(value: ExternalProviderEnum): void;

  abstract onFinish(
    onFinish: any,
    updatedData: FormValues,
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
    step?: number
  ): Promise<void>;
}

export default AbstractProduct;
