import { QueryConfig } from 'react-query';

import { APIQueryResult, useAPI } from 'api';
import { FactRoute, FactRouteQuery } from './track.types';
import { DRIVER_TRACK } from './track.constants';

export const DRIVER_PLAN_TRACK_KEY = 'DRIVER_PLAN_TRACK_KEY';
export const DRIVER_FACT_TRACK_KEY = 'DRIVER_FACT_TRACK_KEY';

declare module 'api' {
  interface Cache {
    driverPlanTrack: {
      key: [typeof DRIVER_PLAN_TRACK_KEY, FactRouteQuery];
      value: FactRoute;
    };
    driverFactTrack: {
      key: [typeof DRIVER_FACT_TRACK_KEY, FactRouteQuery];
      value: FactRoute;
    };
  }
}

/** Получение планового маршрута поезки для карты */
export const usePlanRoute = (
  query: FactRouteQuery,
  config: QueryConfig<FactRoute>
): APIQueryResult<FactRoute> => (
  useAPI(
    [DRIVER_PLAN_TRACK_KEY, query],
    ({ http, process }) => (
      http
        .get<FactRoute>(DRIVER_TRACK, { params: query })
        .then(process.decodeResponseData(FactRoute))
    ),
    config
  )
);

/** Получение фактического маршрута поезки для карты */
export const useFactRoute = (
  query: FactRouteQuery,
  config: QueryConfig<FactRoute>
): APIQueryResult<FactRoute> => (
  useAPI(
    [DRIVER_FACT_TRACK_KEY, query],
    ({ http, process }) => (
      http
        .get<FactRoute>(DRIVER_TRACK, { params: query })
        .then(process.decodeResponseData(FactRoute))
    ),
    config
  )
);
