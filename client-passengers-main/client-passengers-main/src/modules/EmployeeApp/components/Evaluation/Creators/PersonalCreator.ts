import { ITripStore, TripRequest } from 'stores/Trip/Trip.interface';
import PersonalProduct from '../Products/PersonalProduct';
import { IProduct } from '../types';
import Creator from './_Creator';

class PersonalCreator extends Creator {
  factoryMethod(tripStore: ITripStore, request: TripRequest): IProduct {
    return new PersonalProduct(tripStore, request);
  }
}

export default PersonalCreator;
