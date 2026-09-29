// @ts-nocheck
import { useRouteMatch } from 'react-router-dom';

import { useChangeRequestStatus } from 'api/trip-requests';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { TripRequestModel } from 'stores/Trip/models';
import { TripFromCoopModel } from 'stores/Trip/models/TripFromCoop.model';

import { useCurrentTripRequest } from 'shared/hooks/trip';

import { UUID } from 'utils/io-ts';

/* eslint-disable no-unused-expressions */

const useStartPersonalTrip = (
  reqId: UUID
): {
    startPersonalTrip: () => void;
  } => {
  const { currentTripRequest, refetchRequestTrip } = useCurrentTripRequest();
  const [changeRequestStatus] = useChangeRequestStatus();

  // Ищу поездки всех участников, чтобы найти у них атрибут sharedRideId и начать эти поездку - мозг взрывается?

  const participantReqIds: UUID[] = [];

  // Убрано в рамках TRANSPORT-11518. Запрос не поддерживается беком.
  // participantIds?.forEach(id => {
  //   // eslint-disable-next-line react-hooks/rules-of-hooks
  //   const { data: trips } = useGetTripsByEmployees(id as UUID);

  //   participantReqIds.push(...(trips.filter(t => t.sharedRideId === tripFromCoop?.magentaId).map(t => t.id) as UUID[]));
  // });

  const reqIds = [reqId, ...participantReqIds];

  const startPersonalTrip = (): void => {
    if (!currentTripRequest) {
      return;
    }

    reqIds.forEach(id => {
      changeRequestStatus({
        reqId: id,
        reqStatus: 'PERSONAL_TRIP_IN_PROGRESS',
      }).then(() => {
        refetchRequestTrip();
      });
    });
  };

  return { startPersonalTrip };
};

export const usePersonalTrip = ({
  request,
  tripFromCoop,
}: {
  request: TripRequestModel;
  tripFromCoop?: TripFromCoopModel;
}): {
    isPersonalTransport: boolean;
    personalTripInProgress: boolean;
    personalRequestIsApproved: boolean;
    isNotSharedOwner: boolean;
    startPersonalTrip: () => void;
  } => {
  const match = useRouteMatch<{ reqId: UUID }>();

  const {
    transportType, status, id, coopTrip, sharedRideOwner,
  } = request;
  const isPersonalTransport = transportType === TransportTypeEnum.PERSONAL;
  const personalTripInProgress = status === 'PERSONAL_TRIP_IN_PROGRESS';
  const personalRequestIsApproved = status === 'PERSONAL_APPROVED';
  const isNotSharedOwner = sharedRideOwner === true || !coopTrip;

  const { reqId } = match.params;
  const { employeePassengers } = tripFromCoop ?? {};

  const checkReqId = reqId ? reqId : id;
  const checkCoopTrip = tripFromCoop ? tripFromCoop : coopTrip;

  const participants = employeePassengers && employeePassengers?.filter(e => e.id !== request.authorId).map(e => e.id);

  // Отключил логику начала поездки в рамках TRANSPORT-10563
  const { startPersonalTrip } = useStartPersonalTrip(checkReqId, checkCoopTrip, participants);

  return {
    isPersonalTransport, personalTripInProgress, personalRequestIsApproved, isNotSharedOwner, startPersonalTrip,
  };
};
