import { IMPORT, EXCEL } from 'constants/api.constants';

export const DRIVER = 'driver';
export const DRIVERS = 'drivers';
const DISPATCHER_ROOM = '/dispatcher-room';

export const CONTRACTOR_ALL_DRIVERS = `${DISPATCHER_ROOM}/:contractorId/${DRIVERS}/`;
export const CONTRACTOR_DRIVER = `${DISPATCHER_ROOM}/:contractorId/${DRIVERS}/:driverId/`;
export const DRIVERS_IMPORT = `${DISPATCHER_ROOM}/${IMPORT}/${DRIVER}/${EXCEL}/`;
export const DRIVERS_IMPORT_RESULT = `${DISPATCHER_ROOM}/${IMPORT}/${DRIVER}/`;
