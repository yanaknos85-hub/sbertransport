import { useChangeRequestStatus } from 'api/trip-requests';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripRequestModel } from 'stores/Trip/models';

import { useCurrentTripRequest } from 'shared/hooks/trip';

import { UUID } from 'utils/io-ts';
import { TripStatusesEnum } from 'modules/EmployeeApp/TripRequestStatuses.constants';

interface UseCarsharingTripResult {
  isCarsharingTransport: boolean;
  carsharingTripInProgress: boolean;
  carsharingRequestIsApproved: boolean;
  startCarsharingTrip: () => void;
}

export const useCarsharingTrip = (request: TripRequestModel): UseCarsharingTripResult => {
  const {
    transportType, status, id,
  } = request;
  const isCarsharingTransport = transportType === TransportTypeEnum.CARSHARING;
  const carsharingTripInProgress = status === 'CARSHARING_TRIP_IN_PROGRESS';
  const carsharingRequestIsApproved = status !== TripStatusesEnum.CARSHARING_AWAITING_APPROVAL;

  const { currentTripRequest, refetchRequestTrip } = useCurrentTripRequest();
  const [changeRequestStatus] = useChangeRequestStatus();

  const startCarsharingTrip = () => {
    if (!currentTripRequest) {
      return;
    }

    changeRequestStatus({
      reqId: id as UUID,
      reqStatus: 'CARSHARING_TRIP_IN_PROGRESS',
    }).then(() => refetchRequestTrip());
  };

  return {
    isCarsharingTransport,
    carsharingTripInProgress,
    carsharingRequestIsApproved,
    startCarsharingTrip,
  };
};
