import PublicProduct from '../Products/PublicProduct';
import { IProduct } from '../types';
import AbstractCreator from './AbstractCreator';

class PublicCreator extends AbstractCreator {
  factoryMethod(product: IProduct): IProduct {
    return new PublicProduct(product);
  }
}

export default PublicCreator;
