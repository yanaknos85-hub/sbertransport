import { useGetUpdatedRequestTripInfoById } from 'api/trip-request-updated';

import { TripRequestHook, useGetTripRequest } from 'shared/hooks/trip/useGetTripRequest';

export const useUpdatedTripRequest = (): TripRequestHook => useGetTripRequest({
  fetchTripRequest: useGetUpdatedRequestTripInfoById,
});
