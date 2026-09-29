import { useCallback } from 'react';
import { RouteModel } from 'shared/models/geo/Route.model';
import { RequestRoute } from 'shared/models/geo/types';
import { WaypointModel } from 'shared/models/geo/Waypoint.model';
import { formatRubles } from 'utils';

import { TripRequestModel } from 'stores/Trip/models';
import { TripPriceModel } from 'stores/Trip/models/TripPrice.model';
import { calculateTaxiClassCost } from 'modules/CreateTripRequest/utils/utils';

import { useAppStoreContext } from '../useEmpContext';
import { useTariffsCosts } from './useTariffsCosts';

export type ActualRoute = RequestRoute & {
  costString: string;
  waypoints: WaypointModel[];
};

export interface TripRequestCost {
  cost: number;
  costString: string;
  tariffsCosts: TripPriceModel[];
  inProgress: boolean;
  stateReady: boolean;
}

/**
 * Хук для управления заявкой на поездку.
 * Позволяет поддерживать актуальное состояние на
 * основе переданной заявки.
 * @param request TripRequestModel
 */
export const useTripRequestCost = ({
  request,
  calculatedRoute,
  actualWaypoints,
  routeStateReady,
}: {
  request: TripRequestModel | undefined;
  calculatedRoute: RouteModel | undefined;
  actualWaypoints: WaypointModel[];
  routeStateReady: boolean;
}): TripRequestCost => {
  const { selfStore } = useAppStoreContext();

  // FIXME подумать, как упростить логику связанную с этой частью
  const {
    tariffsCosts, inProgress, isFetched,
  } = useTariffsCosts({
    distance: calculatedRoute?.distance,
    time: calculatedRoute?.time,
    tripDate: request?.desiredDate,
    startPoint: { ...actualWaypoints[0] },
    organizationId: selfStore.orgId,
    fetchEnabled: routeStateReady,
  });

  const stateReady = !inProgress && isFetched;

  const calculateCost = useCallback(
    (costs: TripPriceModel[]) => calculateTaxiClassCost(costs, request?.transportType, request?.taxiClass)?.cost,
    [request]
  );

  const cost = routeStateReady && tariffsCosts ? calculateCost(tariffsCosts) : request?.costInRubles || 0;
  const costString = formatRubles(cost);

  return {
    cost,
    costString,
    tariffsCosts: [],
    inProgress,
    stateReady,
  };
};
