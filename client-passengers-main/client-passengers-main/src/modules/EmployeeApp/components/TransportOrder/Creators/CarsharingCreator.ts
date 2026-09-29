import CarsharingProduct from '../Products/CarsharingProduct';
import { IProduct } from '../types';
import AbstractCreator from './AbstractCreator';

class CarsharingCreator extends AbstractCreator {
  factoryMethod(state?: IProduct): IProduct {
    return new CarsharingProduct(state);
  }
}

export default CarsharingCreator;
