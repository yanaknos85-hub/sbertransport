import { GET_COORDINATES_BY_ADDRESS } from 'constants/constants.api';

import * as R from 'ramda';
import * as t from 'io-ts';
import { APIQueryResult, useAPI } from 'api';
import { addressString, Waypoint } from 'stores/Geo/Geo.interface';
import { QueryConfig } from 'react-query';

declare module 'api' {
  interface Cache {
    coordByAddress: {
      key: ['coordByAddress', string];
      value: Waypoint[];
    };
  }
}

export const useCoordinatesByAddress = (
  location: string,
  options: QueryConfig<Waypoint[], unknown>
): APIQueryResult<Waypoint[], unknown> => useAPI(
  ['coordByAddress', location],
  ({ http, process }) => http
    .get<Waypoint[]>(GET_COORDINATES_BY_ADDRESS, { params: { location } })

    .then(process.decodeResponseData(t.array(Waypoint)))
    .catch(e => {
      if (e.response.status === 417) {
        return [];
      }
      throw e;
    })
    .then(waypoints => R.uniqBy(addressString, waypoints).filter(x => addressString(x) !== '')),
  { suspense: false, ...options }
);
