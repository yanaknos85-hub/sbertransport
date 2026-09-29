export const CONTRACTORS = 'contractors';
const DISPATCHER_ROOM = '/dispatcher-room';

export const AUTOPARKS = `${DISPATCHER_ROOM}/`;
export const AUTOPARK = `${DISPATCHER_ROOM}/:autoparkId/`;

export const CONTRACTOR_ALL_AUTOPARKS = `${DISPATCHER_ROOM}/:contractorId/autopark/`;
export const CONTRACTOR_AUTOPARK = `${DISPATCHER_ROOM}/:contractorId/autopark/:autoparkId/`;
