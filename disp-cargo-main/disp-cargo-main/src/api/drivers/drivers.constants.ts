import { DISPATCHER_ROOM } from 'api/dispatchers/dispatchers.constants';

export const DRIVER = 'driver';
export const DRIVERS = 'drivers';

export const CONTRACTOR_DRIVER = `${DISPATCHER_ROOM}/:contractorId/${DRIVERS}/:driverId/`;

