/* eslint-disable no-console */
import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { IProduct } from '../types';
import AbstractProduct from './AbstractProduct';

class BicycleProduct extends AbstractProduct {
  constructor(product?: IProduct) {
    super(product);

    this.transportType = TransportTypeEnum.BICYCLE;
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

  onFinish(): Promise<void> {
    console.log('Пока не реализовано');

    return Promise.resolve();
  }
}

export default BicycleProduct;
