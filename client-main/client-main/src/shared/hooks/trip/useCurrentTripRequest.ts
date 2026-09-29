import { useGetRequestTripInfoById } from 'api/trip-requests';

import { TripRequestModel } from 'stores/Trip/models';

import { useGetTripRequest } from './useGetTripRequest';

export const useCurrentTripRequest = (): {
  refetchRequestTrip: (options?: any) => Promise<TripRequestModel>;
  currentTripRequest: TripRequestModel | undefined;
  inProgress: boolean;
} => {
  const {
    refetchRequestTrip,
    tripRequest: currentTripRequest,
    inProgress,
  } = useGetTripRequest({
    fetchTripRequest: useGetRequestTripInfoById,
  });

  return {
    refetchRequestTrip,
    currentTripRequest,
    inProgress,
  };
};
