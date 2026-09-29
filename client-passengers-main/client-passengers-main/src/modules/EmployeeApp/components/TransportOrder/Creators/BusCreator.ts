import BusProduct from '../Products/BusProduct';
import { IProduct } from '../types';
import AbstractCreator from './AbstractCreator';

class BusCreator extends AbstractCreator {
  factoryMethod(state: IProduct): IProduct {
    return new BusProduct(state);
  }
}

export default BusCreator;
