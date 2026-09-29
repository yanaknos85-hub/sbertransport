import { useRouteMatch } from 'react-router-dom';

import { useGetTripFromCoop } from 'api/trip-requests';

import { useCurrentTripRequest } from 'shared/hooks/trip/useCurrentTripRequest';
import { TripFromCoopModel } from 'stores/Trip/models/TripFromCoop.model';
import { TTripFromCoop } from 'stores/Trip/Trip.interface';

import { UUID } from 'utils/io-ts';

export const useCurrentTripRequestCoop = (): {
  tripFromCoop: TripFromCoopModel | undefined;
  refetchTripFromCoop: (options?: any) => Promise<TTripFromCoop>;
  inProgress: boolean;
} => {
  const match = useRouteMatch<{ reqId: UUID }>();
  const { reqId } = match.params;
  const { currentTripRequest } = useCurrentTripRequest();

  const triggerRequest = Boolean(reqId && currentTripRequest?.coopTrip);
  const {
    data: tripFromCoop,
    refetch: refetchTripFromCoop,
    isLoading,
    isFetching,
  } = useGetTripFromCoop(reqId, {
    cacheTime: 0,
    enabled: triggerRequest,
  });

  const inProgress = isLoading || isFetching;

  return {
    tripFromCoop,
    refetchTripFromCoop,
    inProgress,
  };
};
