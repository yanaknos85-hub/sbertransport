import { useEffect, useState } from 'react';
import { RouteModel } from 'shared/models/geo/Route.model';
import { RequestRoute, Segment } from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';

import { TripRequestModel } from 'stores/Trip/models';
import { TripPriceModel } from 'stores/Trip/models/TripPrice.model';

import { useGeoCalcRoute } from '../geo';
import useGeoWaypoints, { GeoWaypoints } from '../geo/useGeoWaypoints';
import { useTripRequestCost } from './useTripRequestCost';

const getActualRouteData = ({
  isCalculate,
  calculatedRoute,
  request,
  waypoints: localWaypoints,
}: {
  isCalculate: boolean;
  calculatedRoute: RouteModel | undefined;
  request: TripRequestModel | undefined;
  waypoints: WaypointModel[];
}): {
  waypoints: WaypointModel[];
  segments: Segment[];
  time: number;
  distance: number;
} => {
  const waypoints = isCalculate ? calculatedRoute?.waypoints || [] : localWaypoints;
  const segments = isCalculate ? calculatedRoute?.segments || [] : request?.expected.segments || [];
  const time = isCalculate ? calculatedRoute?.time || 0 : request?.expected.time || 0;
  const distance = isCalculate ? calculatedRoute?.distance || 0 : request?.expected.distance || 0;

  return {
    waypoints, segments, time, distance,
  };
};

export type ActualRoute = RequestRoute & {
  costString: string;
  waypoints: WaypointModel[];
};

export interface TripRequestRoute {
  request: TripRequestModel | undefined;
  routeIsCalculate: boolean;
  inProgress: boolean;
  requestReadyForUpdate: boolean;
  calculatedRoute: RouteModel | undefined;
  actualRoute: ActualRoute;
  geoWaypoints: GeoWaypoints;
  tariffsCosts: TripPriceModel[];
}

const useInitRoute = (request: TripRequestModel | undefined) => {
  const [initRoute, setInitRoute] = useState<RouteModel | undefined>(undefined);

  // update & init
  useEffect(() => {
    setInitRoute(request?.expected);
  }, [request]);

  // unmount
  useEffect(
    () => (): void => {
      setInitRoute(undefined);
    },
    []
  );

  return { initRoute };
};

/**
 * Хук для управления заявкой на поездку.
 * Позволяет поддерживать актуальное состояние на
 * основе переданной заявки.
 * @param request TripRequestModel
 */
export const useTripRequestRoute = (request: TripRequestModel | undefined): TripRequestRoute => {
  const geoWaypoints = useGeoWaypoints({
    initWaypoints: request?.expected.waypoints,
  });
  const { waypoints, prevWaypoints } = geoWaypoints;

  const { initRoute } = useInitRoute(request);
  const {
    inProgress: calcRouteInProgress,
    stateReady: routeStateReady,
    calculatedRoute,
  } = useGeoCalcRoute({
    initRoute,
    waypoints,
    prevWaypoints,
  });

  const {
    waypoints: actualWaypoints,
    segments,
    time,
    distance,
  } = getActualRouteData({
    isCalculate: routeStateReady,
    calculatedRoute,
    request,
    waypoints,
  });

  const {
    costString,
    cost,
    tariffsCosts,
    inProgress: costsInProgress,
    stateReady: requestReadyForUpdate,
  } = useTripRequestCost({
    request,
    calculatedRoute,
    actualWaypoints,
    routeStateReady,
  });

  const actualRoute = {
    waypoints: actualWaypoints,
    segments,
    time,
    distance,
    cost,
    costString,
  };

  // Все ли запросы завершены
  const inProgress = calcRouteInProgress || costsInProgress;

  return {
    request,
    inProgress,
    routeIsCalculate: routeStateReady,
    requestReadyForUpdate,
    calculatedRoute,
    tariffsCosts,
    actualRoute,
    geoWaypoints,
  };
};
