import BicycleProduct from '../Products/BicycleProduct';
import { IProduct } from '../types';
import AbstractCreator from './AbstractCreator';

class BicycleCreator extends AbstractCreator {
  factoryMethod(state?: IProduct): IProduct {
    return new BicycleProduct(state);
  }
}

export default BicycleCreator;
