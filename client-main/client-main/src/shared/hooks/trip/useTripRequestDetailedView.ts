import { useHistory, useRouteMatch } from 'react-router-dom';

import { useCancelTripRequest } from 'api/trip-requests';

import { TripsTabsFilters } from 'constants/constants.app';

import { TripCancelReason } from 'stores/Trip/Trip.interface';

import { UUID } from 'utils/io-ts';

export const useTripRequestDetailedView = ({
  requestIsApproved,
}: {
  requestIsApproved: boolean;
}): {
    goToPlanned: () => void;
    cancelHandler: (reason: string) => void;
    editHandler: () => void;
  } => {
  const match = useRouteMatch<{ reqId: UUID }>();
  const history = useHistory();
  const { reqId } = match.params;

  const [cancelTripRequest] = useCancelTripRequest();

  const goToPlanned = (): void => {
    history.push(`../${TripsTabsFilters.planned}`);
  };

  const cancelHandler = (reasonValue: string): void => {
    const reason: TripCancelReason = {
      reason: reasonValue,
      code: 0,
    };

    cancelTripRequest({ reqId, reason }).then(() => {
      goToPlanned();
    });
  };

  const editHandler = (): void => {
    if (requestIsApproved) {
      history.push(`${match.url}/approved/edit`);
    } else {
      history.push(`${match.url}/edit`);
    }
  };

  return {
    goToPlanned,
    cancelHandler,
    editHandler,
  };
};
