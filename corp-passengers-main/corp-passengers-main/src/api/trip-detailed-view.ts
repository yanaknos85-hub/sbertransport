import { AxiosError, AxiosResponse } from 'axios';
import { MutationResultPair } from 'react-query';

import { APIQueryResult, useAPI, useAPIMutation } from '.';
import {
  CANCEL_STATUS,
  CLEAR_QUERY_CONFIG,
  DRIVER_ASSIGNED_STATUS,
  GET_FEED_BY_REQUEST_ID,
  REQUEST_START_TRIP,
  UPDATE_STATUS
} from '../constants/constants.api';
import { StartPersonalTripRequest } from 'modules/TripDetailed/types/types';
import { FeedContent } from '../stores/Engineer/Models/Feed/Feed.content';
import { TransportTypes } from '../stores/TransportTypes/TransportTypes.interface';
import { DecoderInput, UUID } from '../utils/io-ts';
import { useCargoStatuses, useCarsharingStatuses } from './engineer';
import { useGetPersonalTripStatuses } from './personal-search';
import { usePublicTripStatuses } from './public-register-search';
import { useGetTaxiTripStatus } from './register-search';
import { TripStatus } from './travel-status';

declare module 'api' {
  interface Cache {
    trip: { key: ['trip', UUID]; value: FeedContent };
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    statusChange: { key: ['statusChange']; value: any };
    tripRequestInfo: { key: ['tripRequestInfo', UUID]; value: FeedContent };
  }
}

export const useTrip = (requestId: UUID): APIQueryResult<FeedContent, AxiosError> => (
  useAPI(['trip', requestId], ({ http, process }) => http
    .get<DecoderInput<FeedContent>>(GET_FEED_BY_REQUEST_ID, { urlParams: { requestId } })
    .then(process.decodeResponseData(FeedContent))
  )
);

export const chooseCorrectStatusesList = (
  transportType: string | undefined
): (() => APIQueryResult<TripStatus[], Error>) => (transportType === TransportTypes.TAXI && useGetTaxiTripStatus)
|| (transportType === TransportTypes.PERSONAL && useGetPersonalTripStatuses)
|| (transportType === TransportTypes.PUBLIC && usePublicTripStatuses)
|| (transportType === TransportTypes.COURIER && useCargoStatuses)
|| (transportType === TransportTypes.DEDICATED && useCargoStatuses)
|| (transportType === TransportTypes.INTERREGIONAL && useCargoStatuses)
|| (transportType === TransportTypes.CARSHARING && useCarsharingStatuses)
|| useGetTaxiTripStatus;

export const useChangeRequestStatus = (): MutationResultPair<
  FeedContent,
  unknown,
  { requestId: UUID; status: string },
  unknown
> => (
  useAPIMutation(({ http, process }, { requestId, status }) => (
    http.post<FeedContent>(UPDATE_STATUS, {}, { urlParams: { requestId, status } }).then(process.getResponseData)
  ),
  {
    ...CLEAR_QUERY_CONFIG,
    onSuccess: ({
      cache, result: trip, process, t,
    }) => {
      cache.refetchQueries(['trip', trip.id], { exact: true, active: true });
      cache.refetchQueries(['tripRequest', trip.id], { exact: true, active: true });
      process.processStatus(200, t.DetailedView.StatusEditMessages.isEditedSuccessfully);
    },
    onError: ({ process, t }) => {
      process.processStatus(409, t.DetailedView.StatusEditMessages.isEditedWithErrors);
    },
  })
);

export const useCancelStatusWithCancelCode = (): MutationResultPair<
  AxiosResponse<unknown>,
  unknown,
  {
    requestId: UUID;
    cancelCode: { reason: string; code: number };
  },
  unknown
> => (
  useAPIMutation((
    { http }, { requestId, cancelCode }: { requestId: UUID; cancelCode: { reason: string; code: number } }
  ) => (
    http.put(CANCEL_STATUS, cancelCode, { urlParams: { requestId } })
  ),
  {
    ...CLEAR_QUERY_CONFIG,
    onSuccess: ({
      cache, process, t,
    }) => {
      process.processStatus(200, t.DetailedView.StatusEditMessages.isEditedSuccessfully);
      cache.refetchQueries(['trip']);
    },
    onError: ({ process, t }) => {
      process.processStatus(409, t.DetailedView.StatusEditMessages.isEditedWithErrors);
    },
  })
);

export const useDriverAssignedStatusWithCarInfo = (): MutationResultPair<
  FeedContent,
  unknown,
  { requestId: UUID; vehicleInfo: { vehicleInfo: string } },
  unknown
> => (
  useAPIMutation((
    { http, process },
    {
      requestId,
      vehicleInfo,
    }: {
      requestId: UUID;
      vehicleInfo: { vehicleInfo: string };
    }
  ) => (
    http
      .put<FeedContent>(DRIVER_ASSIGNED_STATUS, vehicleInfo, { urlParams: { requestId } })
      .then(process.getResponseData)
  ),
  {
    ...CLEAR_QUERY_CONFIG,
    onSuccess: ({
      cache, result: trip, process, t,
    }) => {
      cache.refetchQueries(['trip', trip.id], { exact: true, active: true });
      cache.refetchQueries(['tripRequest', trip.id], { exact: true, active: true });
      process.processStatus(200, t.DetailedView.StatusEditMessages.isEditedSuccessfully);
    },
    onError: ({ process, t }) => {
      process.processStatus(409, t.DetailedView.StatusEditMessages.isEditedWithErrors);
    },
  })
);

export const useStartPersonalTrip = (): MutationResultPair<unknown, unknown, StartPersonalTripRequest, unknown> => (
  useAPIMutation(
    ({ http, process }, params) => http.put(REQUEST_START_TRIP, { ...params }).then(process.getResponseData),
    {
      onSuccess: ({
        process, cache, variables,
      }) => {
        cache.refetchQueries(['trip', variables.requestId]);
        cache.refetchQueries(['tripRequest', variables.requestId]);
        process.processStatus(200, 'Поездка началась');
      },
      onError: ({ logger }) => {
        logger.toMessage('error', 'Произошла ошибка');
      },
    }
  )
);
