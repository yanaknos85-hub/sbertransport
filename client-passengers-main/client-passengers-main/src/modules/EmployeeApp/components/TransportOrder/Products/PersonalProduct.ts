/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable no-console */
import type { PersonalCar } from '@sber-sbertransport/mf-core';
import { action, observable } from 'mobx';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';
import { ITripTariff } from 'stores/Trip/Trip.interface';
import * as useCooperativeTrip from '../../CreateTripRequest/hooks/useCooperativeTrip';
import type { FormValues, IProduct } from '../types';
import AbstractProduct from './AbstractProduct';

class PersonalProduct extends AbstractProduct {
  @observable
    personalCar?: PersonalCar;

  @action.bound
  setPersonalCar(car: PersonalCar) {
    this.personalCar = car;
  }

  constructor(product?: IProduct) {
    super(product);

    this.transportType = TransportTypeEnum.PERSONAL;
  }

  setBusCreateEnabled(): void {
    console.log('Используйте класс конструктор автобусов');
  }

  setExternal(): void {
    console.log('Используйте класс конструктор такси');
  }

  setExternalPriceLoading(): void {
    console.log('Используйте класс конструктор такси');
  }

  setExternalProvider(): void {
    console.log('Используйте класс конструктор такси');
  }

  @action.bound
  async onFinish(
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
    coopTrip: useCooperativeTrip.CoopTrip,
    step?: number,
    isSuitabelTrip?: boolean
  ) {
    const { coopTripId } = updatedData;

    this.setCreating(true);

    if (!coopTrip) {
      // Создание индивидуальной поездки
      await onFinish(updatedData);
      this.setCreating(false);
      return;
    }

    if (!coopTripId && step === 3 && isSuitabelTrip) {
      // Поиск совместной поезки
      applyCoopTrip(updatedData, true, coopTripId, tariffsInfo);
    } else if (coopTripId && step === 3 && isSuitabelTrip) {
      // Присоединиться к поездке
      applyCoopTrip(updatedData, false, coopTripId, tariffsInfo, coopTrip.suitableCooperativeTrips[0]);
    } else {
      // Создание совместной поездки
      await onFinish(updatedData);
    }

    this.setCreating(false);

    // eslint-disable-next-line consistent-return
    return Promise.resolve();
  }
}

export default PersonalProduct;
