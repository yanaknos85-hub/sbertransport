import { StateNumberMasks } from 'constants/app.constants';

export const firstTwoCharsPattern = /^[A-Za-zА-Яа-я]{2}/;

export enum VehiclesTypes {
  Passenger = 'PASSENGER',
  Bus = 'BUS',
}

export const VEHICLE_TYPES = {
  PASSENGER: {
    title: 'Легковой',
    mask: StateNumberMasks.Passenger,
    example: 'А123АА 123 RUS',
  },
  BUS: {
    title: 'Автобус',
    mask: StateNumberMasks.Bus,
    example: 'АА123 123 RUS',
  },
};
