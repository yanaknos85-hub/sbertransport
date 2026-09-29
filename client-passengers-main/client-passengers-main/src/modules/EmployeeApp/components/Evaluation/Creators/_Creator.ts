import { ITripStore, TripRequest } from 'stores/Trip/Trip.interface';
import { IProduct } from '../types';

/**
 * 2 из 3
 *
 * В классах создателях заложена исключительно логика по созданию сущностей обьектов.
 * В принципе логику конкретных создателей можно было оставить здесь, уместив все сущности в одной, но это нарушает принцип выбранного фабричного метода.
 **/

abstract class Creator implements Creator {
  abstract factoryMethod(tripStore: ITripStore, request: TripRequest): IProduct;
}

export default Creator;
