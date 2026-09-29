import YandexTaxiProduct from '../Products/YandexTaxiProduct';
import { IProduct } from '../types';
import AbstractCreator from './AbstractCreator';

class YandexTaxiCreator extends AbstractCreator {
  factoryMethod(product?: IProduct): IProduct {
    return new YandexTaxiProduct(product);
  }
}

export default YandexTaxiCreator;
