/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable no-console */
import { action } from 'mobx';
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import * as types from '../types';
import AbstractProduct from './AbstractProduct';

class CarsharingProduct extends AbstractProduct {
  constructor(product?: types.IProduct) {
    super(product);

    this.transportType = TransportTypeEnum.CARSHARING;
  }

  setPersonalCar(): void {
    console.log('Используйте класс конструктор личного транспорта');
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
  async onFinish(onFinish: any, updatedData: types.FormValues): Promise<void> {
    onFinish(updatedData);

    return Promise.resolve();
  }
}

export default CarsharingProduct;
