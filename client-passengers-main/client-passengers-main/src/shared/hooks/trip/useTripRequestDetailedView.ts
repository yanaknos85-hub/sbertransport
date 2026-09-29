import { useHistory, useRouteMatch } from 'react-router-dom';

import { useCancelTripRequest } from 'api/trip-requests';

import { TripCancelReason } from 'stores/Trip/Trip.interface';

import { UUID } from 'utils/io-ts';
import * as routes from 'constants/constants.routes';

export const useTripRequestDetailedView = ({
  requestIsApproved,
}: {
  requestIsApproved: boolean;
}): {
    goToPlanned: () => void;
    cancelHandler: (reason: string, id: string) => void;
    justCancelHandler: (reason: string, id: string) => void;
    editHandler: () => void;
  } => {
  const match = useRouteMatch<{ reqId: UUID }>();
  const history = useHistory();
  const { reqId } = match.params;

  const [cancelTripRequest] = useCancelTripRequest();

  const goToPlanned = (): void => {
    if (match.url === `${routes.TRIPS_LIST}/planned`) {
      return window.location.reload();
    }

    history.push(`${routes.TRIPS_LIST}/planned`);
  };

  const cancelHandler = (reasonValue: string, id: string): void => {
    const reason: TripCancelReason = {
      reason: reasonValue,
      code: 0,
    };

    cancelTripRequest({ reqId: reqId ? reqId : id, reason }).then(() => {
      goToPlanned();
    });
  };

  const justCancelHandler = (reasonValue: string, id: string): void => {
    const reason: TripCancelReason = {
      reason: reasonValue,
      code: 0,
    };

    cancelTripRequest({ reqId: reqId ? reqId : id, reason });
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
    justCancelHandler,
    editHandler,
  };
};
