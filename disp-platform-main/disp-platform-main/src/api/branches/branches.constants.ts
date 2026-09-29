const DISPATCHER_ROOM = '/dispatcher-room';
const AUTOPARK = 'autopark';

export const ALL_AUTOPARK_BRANCH = `${DISPATCHER_ROOM}/:autoparkId/${AUTOPARK}/`;

export const AUTOPARK_BRANCH = `${DISPATCHER_ROOM}/:autoparkId/${AUTOPARK}/:branchId/`;
