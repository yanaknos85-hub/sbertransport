import { useEditTripRequest } from 'api/trip-requests';
import { TripRequestModel } from 'stores/Trip/models';

import { TripRequestRoute } from './useTripRequestRoute';
import { prepareRequestTripData } from './utils';

interface TripRequest {
  updateTripRequest: () => void;
}

export const useUpdateTripRequest = ({
  request,
  tripRequestRoute,
}: {
  request: TripRequestModel | undefined;
  tripRequestRoute: TripRequestRoute;
}): TripRequest => {
  const [editTripRequest] = useEditTripRequest();
  const {
    actualRoute: { cost },
    calculatedRoute,
  } = tripRequestRoute;

  const updateTripRequest = (): void => {
    if (!request) {
      return;
    }

    editTripRequest({
      data: prepareRequestTripData({
        request,
        expected: calculatedRoute,
        cost,
      }),
      reqId: request.id,
    }).then(() => {
      window.location.reload();
    });
  };

  return {
    updateTripRequest,
  };
};
