import PersonalProduct from '../Products/PersonalProduct';
import { IProduct } from '../types';
import AbstractCreator from './AbstractCreator';

class PersonalCreator extends AbstractCreator {
  factoryMethod(product?: IProduct): IProduct {
    return new PersonalProduct(product);
  }
}

export default PersonalCreator;
