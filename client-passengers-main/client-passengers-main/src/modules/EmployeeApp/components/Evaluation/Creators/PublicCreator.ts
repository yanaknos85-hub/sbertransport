import { ITripStore, TripRequest } from 'stores/Trip/Trip.interface';
import PublicProduct from '../Products/PublicProduct';
import { IProduct } from '../types';
import Creator from './_Creator';

class PublicCreator extends Creator {
  factoryMethod(tripStore: ITripStore, request: TripRequest): IProduct {
    return new PublicProduct(tripStore, request);
  }
}

export default PublicCreator;
