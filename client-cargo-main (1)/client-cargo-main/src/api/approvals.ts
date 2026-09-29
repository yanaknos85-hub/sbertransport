import { MutationResultPair } from 'react-query';
import QueryString from 'qs';
import {
  ApprovalSetting,
  IOApprovement,
  ResponseApprovalSearch,
  TApprovement,
  TResponseApprovalSearch
} from 'shared/models/Approval.interface';
import { ApprovalModel } from 'shared/models/Approval.model';
import { plainToNew } from 'utils';

import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import { IDeclineReason } from 'stores/Trip/Trip.interface';
import { SYSTEM_MESSAGES } from 'constants/constants.app';
import {
  APPROVEMENT,
  APPROVEMENT_FINAL_TRIP_APPROVE,
  APPROVEMENT_FINAL_TRIP_DECLINE,
  APPROVEMENT_REQUEST_TRIP_APPROVE,
  APPROVEMENT_REQUEST_TRIP_DECLINE,
  APPROVEMENT_SHARED_RIDE_APPROVE,
  APPROVEMENT_SHARED_RIDE_DECLINE,
  GET_APPROVAL_BY_REQUEST_ID,
  MOCKED_API_PREFIX
} from 'constants/constants.env';

declare module 'api' {
  interface Cache {
    activeTripApprovals: {
      key: ['activeTripApprovals'];
      value: ApprovalModel[];
    };
    closedTripApprovals: {
      key: ['closedTripApprovals'];
      value: ApprovalModel[];
    };
    activeRequestApprovals: {
      key: ['activeRequestApprovals'];
      value: ApprovalModel[];
    };
    closedRequestApprovals: {
      key: ['closedRequestApprovals'];
      value: ApprovalModel[];
    };
    activeSharedRidesApprovals: {
      key: ['activeSharedRidesApprovals'];
      value: ApprovalModel[];
    };
    closedSharedRidesApprovals: {
      key: ['closedSharedRidesApprovals'];
      value: ApprovalModel[];
    };
    approvalByRequestId: {
      key: ['approvalByRequestId', string];
      value: ApprovalModel | null;
    };
    responseApprovalLists: {
      key: ['responseApprovalLists', string | string[], number | undefined];
      value: TResponseApprovalSearch | null;
    };
  }
}
const transformToApprovalModel = (approval: TApprovement): ApprovalModel => plainToNew<ApprovalModel>(ApprovalModel, approval);

export const useFinalTripApprove = (): MutationResultPair<unknown, any, { id: string }, unknown> => useAPIMutation(
  ({ http, process }, { id }) => http.put(APPROVEMENT_FINAL_TRIP_APPROVE, {}, { urlParams: { id } }).then(process.getResponseData),
  {
    onSuccess: ({ process, cache }) => {
      process.processStatus(200, SYSTEM_MESSAGES.approveSuccessfull);
      cache.invalidateQueries(['responseApprovalLists']);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', 'Произошла ошибка при согласовании маршрута');
    },
  }
);

export const useFinalTripDecline = (): MutationResultPair<
  unknown,
  any,
  { id: string; reason: IDeclineReason },
  unknown
> => useAPIMutation(
  ({ http, process }, { id, reason }) => http
    .put(APPROVEMENT_FINAL_TRIP_DECLINE, { reason: reason.reason }, { urlParams: { id } })
    .then(process.getResponseData),
  {
    onSuccess: ({ process, cache }) => {
      process.processStatus(200, SYSTEM_MESSAGES.routeIsNotApproved);
      cache.invalidateQueries(['responseApprovalLists']);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', SYSTEM_MESSAGES.errorOccurred);
    },
  }
);

export const useRequestApprove = (): MutationResultPair<unknown, any, { id: string }, unknown> => useAPIMutation(
  ({ http, process }, { id }) => http.put(APPROVEMENT_REQUEST_TRIP_APPROVE, {}, { urlParams: { id } }).then(process.getResponseData),
  {
    onSuccess: ({ process, cache }) => {
      process.processStatus(200, SYSTEM_MESSAGES.approveSuccessfull);
      cache.invalidateQueries(['responseApprovalLists']);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', 'Произошла ошибка при согласовании');
    },
  }
);

export const useRequestDecline = (): MutationResultPair<
  unknown,
  any,
  { id: string; reason: IDeclineReason },
  unknown
> => useAPIMutation(
  ({ http, process }, { id, reason }) => http
    .put(APPROVEMENT_REQUEST_TRIP_DECLINE, { reason: reason.reason }, { urlParams: { id } })
    .then(process.getResponseData),
  {
    onSuccess: ({ process, cache }) => {
      process.processStatus(200, SYSTEM_MESSAGES.routeIsNotApproved);
      cache.invalidateQueries(['responseApprovalLists']);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', SYSTEM_MESSAGES.errorOccurred);
    },
  }
);

export const useSharedRideRequestApprove = (): MutationResultPair<unknown, any, { id: string }, unknown> => useAPIMutation(
  ({ http, process }, { id }) => http.put(APPROVEMENT_SHARED_RIDE_APPROVE, {}, { urlParams: { id } }).then(process.getResponseData),
  {
    onSuccess: ({ process, cache }) => {
      process.processStatus(200, SYSTEM_MESSAGES.approveSuccessfull);
      cache.invalidateQueries(['responseApprovalLists']);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', 'Произошла ошибка при согласовании');
    },
  }
);

export const useSharedRideRequestDecline = (): MutationResultPair<unknown, any, { id: string }, unknown> => useAPIMutation(
  ({ http, process }, { id }) => http.put(APPROVEMENT_SHARED_RIDE_DECLINE, {}, { urlParams: { id } }).then(process.getResponseData),
  {
    onSuccess: ({ process, cache }) => {
      process.processStatus(200, SYSTEM_MESSAGES.routeIsNotApproved);
      cache.invalidateQueries(['responseApprovalLists']);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', SYSTEM_MESSAGES.errorOccurred);
    },
  }
);

/**
 * Работает только для согласованной заявки
 */
export const useGetApprovalByRequestId = (reqId: string): APIQueryResult<ApprovalModel | null, undefined> => useAPI(['approvalByRequestId', reqId], ({ http, process }) => http
  .get<TApprovement>(GET_APPROVAL_BY_REQUEST_ID, { urlParams: { reqId } })
  .then(process.decodeResponseData(IOApprovement))
  .then(transformToApprovalModel)
  .catch(err => {
    process.processStatus(404, err);
    return null;
  })
);
export const useGetRequestSearchApproval = (
  approvalSetting: ApprovalSetting
): APIQueryResult<TResponseApprovalSearch | null, unknown> => useAPI(['responseApprovalLists', approvalSetting.status, approvalSetting.page], ({ http, process }) => http
  .get<TResponseApprovalSearch>(`${MOCKED_API_PREFIX}${APPROVEMENT}/`, {
    params: approvalSetting,
    paramsSerializer: params1 => QueryString.stringify(params1, { arrayFormat: 'repeat' }),
  })
  .then(process.decodeResponseData(ResponseApprovalSearch))
  .catch(err => {
    process.processStatus(404, err);
    return null;
  })
);
