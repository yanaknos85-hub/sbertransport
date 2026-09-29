import { DISPATCHER_ROOM } from 'api/dispatchers/dispatchers.constants';

const SHIFT = 'shift';

export const SHIFT_ROOT = `${DISPATCHER_ROOM}/:contractorId/${SHIFT}/`;
export const SHIFT_ONE = `${SHIFT_ROOT}:shiftId/`;
