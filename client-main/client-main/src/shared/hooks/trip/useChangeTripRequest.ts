import { MutationResultPair } from 'react-query';

import { useEditApprovedTripRequest, useEditTripRequest } from 'api/trip-requests';

import { TripRequestModel } from 'stores/Trip/models';
import { TripRequest } from 'stores/Trip/Trip.interface';

import { ActualRoute } from './useTripRequestCost';

interface ChangeTripRequestHook {
  request: TripRequestModel | undefined;
  onChangeRequest?: () => void;
}

export const useChangeTripRequest = <T>({
  request,
  onChangeRequest,
  editMethod,
}: ChangeTripRequestHook & {
  editMethod(): MutationResultPair<unknown, any, { data: T; reqId: string }, unknown>;
}): { changeTripRequest: (data: T) => void } => {
  const [editTripRequest] = editMethod();

  const changeTripRequest = (data: T): void => {
    if (!request) {
      return;
    }

    editTripRequest({ data, reqId: request.id }).then(() => {
      if (onChangeRequest) {
        onChangeRequest();
      }
    });
  };

  return { changeTripRequest };
};

export const useDefaultChangeTripRequest = ({
  request,
  onChangeRequest,
}: ChangeTripRequestHook): {
    changeTripRequest: (data: Partial<TripRequest>) => void;
  } => useChangeTripRequest<Partial<TripRequest>>({
    request, onChangeRequest, editMethod: useEditTripRequest,
  });

export const useChangeApprovedTripRequest = ({
  request,
  onChangeRequest,
}: ChangeTripRequestHook): {
    changeTripRequest: (data: ActualRoute) => void;
  } => useChangeTripRequest<ActualRoute>({
    request, onChangeRequest, editMethod: useEditApprovedTripRequest,
  });
