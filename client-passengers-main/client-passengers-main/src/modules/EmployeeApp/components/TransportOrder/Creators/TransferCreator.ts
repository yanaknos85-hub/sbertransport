import TransferProduct from '../Products/TransferProduct';
import { IProduct } from '../types';
import AbstractCreator from './AbstractCreator';

class TransferCreator extends AbstractCreator {
  factoryMethod(product: IProduct): IProduct {
    return new TransferProduct(product);
  }
}

export default TransferCreator;
