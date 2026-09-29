import { ITripStore, TripRequest } from 'stores/Trip/Trip.interface';
import TaxiProduct from '../Products/TaxiProduct';
import { IProduct } from '../types';
import Creator from './_Creator';

class TaxiCreator extends Creator {
  factoryMethod(tripStore: ITripStore, request: TripRequest): IProduct {
    return new TaxiProduct(tripStore, request);
  }
}

export default TaxiCreator;
