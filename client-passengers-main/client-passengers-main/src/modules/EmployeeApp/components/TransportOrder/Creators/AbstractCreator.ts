import { IProduct } from '../types';

abstract class AbstractCreator implements AbstractCreator {
  abstract factoryMethod(product?: IProduct): IProduct;
}

export default AbstractCreator;
