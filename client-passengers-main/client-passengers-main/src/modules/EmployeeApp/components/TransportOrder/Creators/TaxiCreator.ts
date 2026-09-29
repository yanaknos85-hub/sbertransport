import TaxiProduct from '../Products/TaxiProduct';
import { IProduct } from '../types';
import AbstractCreator from './AbstractCreator';

class TaxiCreator extends AbstractCreator {
  factoryMethod(product?: IProduct): IProduct {
    return new TaxiProduct(product);
  }
}

export default TaxiCreator;
