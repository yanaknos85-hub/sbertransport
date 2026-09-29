/* eslint-disable no-console */
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import AbstractProduct from './AbstractProduct';
import { action } from 'mobx';
import * as types from '../types';

class TransferProduct extends AbstractProduct {
  constructor(product?: types.IProduct) {
    super(product);

    this.transportType = TransportTypeEnum.GROUP_TRANSFER;
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

  setPersonalCar(): void {
    console.log('Используйте класс конструктор личного транспорта');
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

export default TransferProduct;
