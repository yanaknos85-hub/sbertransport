import { AUTOPARK } from 'api/autopark/autopark.constants';
import { SEARCH } from 'constants/api.constants';

export const VEHICLE = 'vehicle';
export const DISPATCHER_ROOM = '/dispatcher-room';

export const GET_ALL_VEHICLES_BY_CONTRACTOR = `${DISPATCHER_ROOM}/:contractorId/${VEHICLE}/`;
export const GET_ALL_VEHICLES_BY_AUTOPARK = `${DISPATCHER_ROOM}/:contractorId/${AUTOPARK}/:autoparkId/${VEHICLE}/`;
export const GET_VEHICLE_BY_AUTOPARK = `${DISPATCHER_ROOM}/:contractorId/${AUTOPARK}/:autoparkId/${VEHICLE}/:vehicleId/`;
export const VEHICLES_SEARCH = `${DISPATCHER_ROOM}/:contractorId/${VEHICLE}/${SEARCH}/`;
export const VEHICLES_LOAD_FILE = `${DISPATCHER_ROOM}/files/vehicles`;

export const CREATE_VEHICLE = GET_ALL_VEHICLES_BY_AUTOPARK;
export const UPDATE_VEHICLE = GET_VEHICLE_BY_AUTOPARK;
export const DELETE_VEHICLE = GET_VEHICLE_BY_AUTOPARK;

export enum Eco {
  EURO_0 = 'EURO_0',
  EURO_1 = 'EURO_1',
  EURO_2 = 'EURO_2',
  EURO_3 = 'EURO_3',
  EURO_4 = 'EURO_4',
  EURO_5 = 'EURO_5',
  EURO_6 = 'EURO_6',
  EURO_7 = 'EURO_7',
}

export const ecoClasses = [
  {
    name: 'EURO_0',
    rusName: 'EURO_0',
  },
  {
    name: 'EURO_1',
    rusName: 'EURO_1',
  },
  {
    name: 'EURO_2',
    rusName: 'EURO_2',
  },
  {
    name: 'EURO_3',
    rusName: 'EURO_3',
  },
  {
    name: 'EURO_4',
    rusName: 'EURO_4',
  },
  {
    name: 'EURO_5',
    rusName: 'EURO_5',
  },
  {
    name: 'EURO_6',
    rusName: 'EURO_6',
  },
  {
    name: 'EURO_7',
    rusName: 'EURO_7',
  },
];
