import { VEHICLE } from 'api/vehicles/vehicles.constants';
import { MOCKED_API_PREFIX } from 'constants/env.constants';

export const TRANSPORT = 'transport';

export const VEHICLE_QUERY = `${MOCKED_API_PREFIX}/${VEHICLE}`;

export const TRANSPORT_QUERY = `${VEHICLE_QUERY}/${TRANSPORT}`;

export const TRANSPORT_SEARCH = `${TRANSPORT_QUERY}/search`;

export const TRANSPORT_FILES = `${VEHICLE_QUERY}/files/${TRANSPORT}`;

export const TRANSPORT_ONE = `${TRANSPORT_QUERY}/:transportId`;

export const TRANSPORT_DEACTIVATE = `${TRANSPORT_QUERY}/deactivate/:transportId`;
