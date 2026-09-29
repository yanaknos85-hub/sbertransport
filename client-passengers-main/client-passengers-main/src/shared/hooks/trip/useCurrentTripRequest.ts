import { UUID } from 'utils/io-ts';
import { useGetRequestTripInfoById } from 'api/trip-requests';

import { TripRequestModel } from 'stores/Trip/models';

import { useGetTripRequest } from './useGetTripRequest';

export const useCurrentTripRequest = (outReqId?: UUID): {
  refetchRequestTrip: (options?: any) => Promise<TripRequestModel>;
  currentTripRequest: TripRequestModel | undefined;
  inProgress: boolean;
  outReqId?: UUID;
} => {
  const {
    refetchRequestTrip,
    tripRequest: currentTripRequest,
    inProgress,
  } = useGetTripRequest({
    fetchTripRequest: useGetRequestTripInfoById,
    outReqId,
  });

  return {
    refetchRequestTrip,
    currentTripRequest,
    inProgress,
  };
};
