import * as t from 'io-ts';
import { MutationResultPair } from 'react-query';

import { APIQueryResult, useAPI, useAPIMutation } from 'api';

import { SYSTEM_MESSAGES } from 'constants/constants.app';
import {
  APPROVEMENT_UPDATED_TRIP_ACTIVE,
  APPROVEMENT_UPDATED_TRIP_APPROVE,
  APPROVEMENT_UPDATED_TRIP_CLOSED,
  APPROVEMENT_UPDATED_TRIP_DECLINE
} from 'constants/constants.env';

import { ApprovalTypeEnum, IOApprovement, TApprovement } from 'shared/models/Approval.interface';
import { ApprovalModel } from 'shared/models/Approval.model';
import { IDeclineReason } from 'stores/Trip/Trip.interface';
import { plainToNew } from 'utils';

declare module 'api' {
  interface Cache {
    activeUpdatedTripApprovals: {
      key: ['activeUpdatedTripApprovals'];
      value: ApprovalModel[];
    };
    closedUpdatedTripApprovals: {
      key: ['closedUpdatedTripApprovals'];
      value: ApprovalModel[];
    };
  }
}

const transformToUpdatedApprovalTrip = (approvals: TApprovement[]): ApprovalModel[] => plainToNew<ApprovalModel[]>(
  ApprovalModel,
  approvals.map(approval => ({ ...approval, approvalType: ApprovalTypeEnum.UPDATED_TRIP }))
);

export const useActiveUpdatedTripApprovals = (): APIQueryResult<ApprovalModel[], unknown> => useAPI(['activeUpdatedTripApprovals'], ({ http, process }) => http
  .get<TApprovement[]>(APPROVEMENT_UPDATED_TRIP_ACTIVE)
  .then(process.decodeResponseData(t.array(IOApprovement)))
  .then(transformToUpdatedApprovalTrip)
  .catch(err => {
    process.processStatus(404, err);
    return [];
  })
);

export const useClosedUpdatedTripApprovals = (): APIQueryResult<ApprovalModel[], unknown> => useAPI(['closedUpdatedTripApprovals'], ({ http, process }) => http
  .get<TApprovement[]>(APPROVEMENT_UPDATED_TRIP_CLOSED)
  .then(process.decodeResponseData(t.array(IOApprovement)))
  .then(transformToUpdatedApprovalTrip)
  .catch(err => {
    process.processStatus(404, err);
    return [];
  })
);

export const useUpdatedTripApprove = (): MutationResultPair<unknown, any, { id: string }, unknown> => useAPIMutation(
  ({ http, process }, { id }) => http.put(APPROVEMENT_UPDATED_TRIP_APPROVE, {}, { urlParams: { id } }).then(process.getResponseData),
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

export const useUpdatedTripDecline = (): MutationResultPair<
  unknown,
  any,
  { id: string; reason: IDeclineReason },
  unknown
> => useAPIMutation(
  ({ http, process }, { id, reason }) => http
    .put(APPROVEMENT_UPDATED_TRIP_DECLINE, { reason: reason.reason }, { urlParams: { id } })
    .then(process.getResponseData),
  {
    onSuccess: ({ process }) => {
      process.processStatus(200, SYSTEM_MESSAGES.routeIsNotApproved);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', SYSTEM_MESSAGES.errorOccurred);
    },
  }
);
