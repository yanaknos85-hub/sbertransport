/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable no-console */
import { action, observable } from 'mobx';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripSuitableModel } from 'stores/Trip/models/TripSuitable.model';
import { ExternalProviderEnum, ITripTariff } from 'stores/Trip/Trip.interface';
import * as useCooperativeTrip from '../../CreateTripRequest/hooks/useCooperativeTrip';
import * as types from '../types';
import { IProduct } from '../types';
import AbstractProduct from './AbstractProduct';

class YandexTaxiProduct extends AbstractProduct {
  @observable
    isExternal = false;

  @action.bound
  setExternal(value: boolean) {
    this.isExternal = value;
  }

  @observable
    isExternalPriceLoading = false;

  @action.bound
  setExternalPriceLoading(value: boolean) {
    this.isExternalPriceLoading = value;
  }

  @observable
    externalProvider?: ExternalProviderEnum;

  @action.bound
  setExternalProvider(value: ExternalProviderEnum) {
    this.externalProvider = value;
  }

  @action.bound
  async onFinish(
    onFinish: any,
    updatedData: types.FormValues,
    externalLinks: any,
    applyCoopTrip: (
      data: types.FormValues,
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

    const {
      deeplinkCitymobil, deeplinkUber, deeplinkYandex,
    } = externalLinks;
    this.setCreating(true);

    if (this.isExternal) {
      // Создание поездки через контрагента
      this.setCreating(false);
      window.location.href = deeplinkCitymobil || deeplinkUber || deeplinkYandex || window.location.href;
      return;
    }

    if (!coopTrip) {
      // Создание индивидуальной поездки
      await onFinish(updatedData);
      this.setCreating(false);
      return;
    }

    if (!coopTripId && step === 3 && isSuitabelTrip) {
      // Поиск совместной поезки
      applyCoopTrip(updatedData, true, coopTripId, tariffsInfo);
    } else if (coopTripId && (step === 4 || step === 3) && isSuitabelTrip) {
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

  constructor(product?: IProduct) {
    super(product);

    this.transportType = TransportTypeEnum.YANDEX;
  }

  setPersonalCar(): void {
    console.log('Используйте класс конструктор личного транспорта');
  }

  setBusCreateEnabled(): void {
    console.log('Используйте класс конструктор автобусов');
  }
}

export default YandexTaxiProduct;
