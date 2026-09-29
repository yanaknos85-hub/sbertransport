const DISPATCHER_ROOM = '/dispatcher-room';

export const SCHEDULE = `${DISPATCHER_ROOM}/:contractorId/vehicle-shift/`;
export const DRIVER_STATUSES = `${DISPATCHER_ROOM}/:contractorId/vehicle-shift/status/`;
export const SHIFTS = `${DISPATCHER_ROOM}/:contractorId/shift/`;
export const SHIFT = `${DISPATCHER_ROOM}/:contractorId/shift/:shiftId/`;
export const SHIFT_ROW = `${DISPATCHER_ROOM}/:contractorId/shift/row/:rowId/`;

const TRIPS = '/trips';

export const PASS_VEHICLE_BUSYNESS = `${TRIPS}/self/dispatcher/vehicle/busyness/`;

const TRIPS_CARGO = '/trips-cargo';

export const CARGO_VEHICLE_BUSYNESS = `${TRIPS_CARGO}/self/dispatcher/vehicle/busyness/`;
