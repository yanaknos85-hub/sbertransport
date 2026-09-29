import { AUTOPARK } from 'api/autopark/autopark.constants';
import { EXCEL, IMPORT, SEARCH } from 'constants/api.constants';

export const VEHICLE = 'vehicle';
const DISPATCHER_ROOM = '/dispatcher-room';

export const GET_ALL_VEHICLES_BY_CONTRACTOR = `${DISPATCHER_ROOM}/:contractorId/${VEHICLE}/`;
export const GET_ALL_VEHICLES_BY_AUTOPARK = `${DISPATCHER_ROOM}/:contractorId/${AUTOPARK}/:autoparkId/${VEHICLE}/`;
export const GET_VEHICLE_BY_AUTOPARK = `${DISPATCHER_ROOM}/:contractorId/${AUTOPARK}/:autoparkId/${VEHICLE}/:vehicleId/`;
export const VEHICLES_SEARCH = `${DISPATCHER_ROOM}/:contractorId/${VEHICLE}/${SEARCH}/`;
export const VEHICLES_IMPORT = `${DISPATCHER_ROOM}/${IMPORT}/${VEHICLE}/${EXCEL}/`;
export const VEHICLES_IMPORT_RESULT = `${DISPATCHER_ROOM}/${IMPORT}/${VEHICLE}/`;

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
