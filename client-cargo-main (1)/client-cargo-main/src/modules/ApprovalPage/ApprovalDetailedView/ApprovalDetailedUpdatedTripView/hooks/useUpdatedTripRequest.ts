import { TripRequestHook, useGetTripRequest } from 'shared/hooks/trip/useGetTripRequest';

import { useGetUpdatedRequestTripInfoById } from 'api/trip-request-updated';

export const useUpdatedTripRequest = (): TripRequestHook => useGetTripRequest({
  fetchTripRequest: useGetUpdatedRequestTripInfoById,
});
