/* eslint-disable no-console */
/* eslint-disable @typescript-eslint/no-explicit-any */
import { action, observable } from 'mobx';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import * as types from '../types';
import AbstractProduct from './AbstractProduct';

class BusProduct extends AbstractProduct {
  @observable
    busCount?: number;

  @observable
    busRentDuration?: number;

  @observable
    busCreateEnabled = false;

  @action.bound
  setBusCreateEnabled(value: boolean) {
    this.busCreateEnabled = value;
  }

  constructor(product?: types.IProduct) {
    super(product);

    this.transportType = TransportTypeEnum.BUS;
  }

  setPersonalCar(): void {
    console.log('Используйте класс конструктор личного транспорта');
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
  async onFinish(onFinish: any, updatedData: types.FormValues): Promise<void> {
    onFinish(updatedData);

    return Promise.resolve();
  }
}

export default BusProduct;
