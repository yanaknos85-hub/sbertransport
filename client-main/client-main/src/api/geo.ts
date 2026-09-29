import { QueryConfig } from 'react-query';

import { APIQueryResult, useAPI } from 'api';

import { CALC_ROUTE, GET_ADDRESS_BY_COORDINATES, GET_COORDINATES_BY_ADDRESS } from 'constants/constants.env';

import {
  GeoZoneInfo, LatLngTuple, RequestRoute, TGeoZone, TWaypoint
} from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

declare module 'api' {
  interface Cache {
    waypointByCoordinates: {
      key: ['waypointByCoordinates', LatLngTuple];
      value: WaypointModel[] | undefined;
    };
    coordinateByAddress: {
      key: ['coordinateByAddress', string | undefined];
      value: TWaypoint[];
    };
    calculatedRoute: {
      key: ['calculatedRoute', WaypointModel[]];
      value: RequestRoute | undefined;
    };
    geoZone: {
      key: ['geoZone', TGeoZone];
      value: GeoZoneInfo | undefined;
    };
  }
}

export const useGetWaypointsByCoordinates = (
  coordinates: LatLngTuple,
  options: QueryConfig<WaypointModel[] | undefined, unknown>
): APIQueryResult<WaypointModel[] | undefined, unknown> => {
  const latitude = coordinates[0].toFixed(6);
  const longitude = coordinates[1].toFixed(6);

  return useAPI(
    ['waypointByCoordinates', coordinates],
    ({ http, process }) => http
      .get<WaypointModel[] | undefined>(GET_ADDRESS_BY_COORDINATES, { params: { latitude, longitude } })
      .then(process.getResponseData),
    options
  );
};

export const useGetCoordinateByAddress = (
  location: string | undefined,
  options: QueryConfig<TWaypoint[], unknown>
): APIQueryResult<TWaypoint[], unknown> => useAPI(
  ['coordinateByAddress', location],
  ({ http, process }) => http.get<TWaypoint[]>(GET_COORDINATES_BY_ADDRESS, { params: { location } }).then(process.getResponseData),
  options
);

export const useCalcRoute = (
  {
    waypoints,
    previousRoute,
  }: {
    waypoints: WaypointModel[];
    previousRoute: RequestRoute | undefined;
  },
  options: QueryConfig<RequestRoute | undefined, unknown>
): APIQueryResult<RequestRoute | undefined, unknown> => useAPI(
  ['calculatedRoute', waypoints],
  ({ http, process }) => http
    .post<RequestRoute>(CALC_ROUTE, { coordinates: waypoints })
    .then(x => process.getResponseData(x, RequestRoute))
    .catch(() => previousRoute),
  options
);
export const useSearchGeoZone = (
  geoZone: TGeoZone,
  options: QueryConfig<GeoZoneInfo | undefined, unknown>
): APIQueryResult<GeoZoneInfo | undefined, unknown> => useAPI(
  ['geoZone', geoZone],
  ({ http, process }) => http.post<GeoZoneInfo>('geo-zones/search', { ...geoZone }).then(process.getResponseData),
  options
);
