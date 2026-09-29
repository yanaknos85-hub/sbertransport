import * as R from 'ramda';

import { useCalculateTariffCost } from 'api/trip-requests';
import { TripPriceModel } from 'stores/Trip/models/TripPrice.model';
import { ITripCalculateRequest } from 'stores/Trip/Trip.interface';

interface HookProps {
  distance?: number;
  time?: number;
  tripDate?: number;
  startPoint: Record<string, any>;
  organizationId: string;
  fetchEnabled: boolean;
}

export const useTariffsCosts = (
  props: HookProps
): {
  tariffsCosts?: TripPriceModel[];
  inProgress: boolean;
  isFetched: boolean;
} => {
  const params = R.omit(['fetchEnabled'], props) as ITripCalculateRequest;
  const triggerRequest = params.distance && params.time && params.tripDate && props.fetchEnabled;

  const {
    data, isFetching, isLoading, isFetched,
  } = useCalculateTariffCost(params, {
    enabled: triggerRequest,
    suspense: false,
  });

  const inProgress = isLoading || isFetching;

  return {
    tariffsCosts: data, inProgress, isFetched,
  };
};
