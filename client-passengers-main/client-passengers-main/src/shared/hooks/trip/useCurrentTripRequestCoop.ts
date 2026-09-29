import { useRouteMatch } from 'react-router-dom';

import { useGetTripFromCoop } from 'api/trip-requests';

import { useCurrentTripRequest } from 'shared/hooks/trip/useCurrentTripRequest';
import { TripFromCoopModel } from 'stores/Trip/models/TripFromCoop.model';

import { UUID } from 'utils/io-ts';

export const useCurrentTripRequestCoop = (outReqId?: UUID): {
  tripFromCoop?: TripFromCoopModel;
  isFetched: boolean;
} => {
  const match = useRouteMatch<{ reqId: UUID }>();
  const { reqId } = match.params;

  const finalReqId = reqId ?? outReqId;
  const { currentTripRequest } = useCurrentTripRequest(finalReqId);

  const triggerRequest = Boolean(finalReqId && currentTripRequest?.coopTrip);

  const {
    data: tripFromCoop,
    isFetched,
  } = useGetTripFromCoop(finalReqId, {
    // cacheTime: 10,  // zero cacheTime main lead to infinite loop in requests and component re-rendering
    enabled: triggerRequest,
    retry: false,
    refetchOnWindowFocus: false,
    refetchOnReconnect: false,
  }) || {};

  return {
    tripFromCoop,
    isFetched,
  };
};
