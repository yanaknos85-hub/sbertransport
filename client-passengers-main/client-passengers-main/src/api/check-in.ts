import { MutationResultPair } from 'react-query';

import { useAPIMutation } from 'api';

import { REQUEST_ABSENCE_REASON } from 'constants/constants.env';
import { REQUEST_DELETE_WAYPOINT } from '../constants/constants.env';

export interface AbsenceRequestData {
  requestId: string;
  latitude: number | undefined;
  longitude: number | undefined;
  absenceReason: string;
  orderingIndex: number;
}

export interface DeleteWaypointData {
  requestId: string;
  latitude: number | undefined;
  longitude: number | undefined;
  orderingIndex: number;
}

export const useAbsenceReason = (): MutationResultPair<unknown, unknown, AbsenceRequestData, unknown> => useAPIMutation(
  ({ http, process }, {
    requestId, latitude, longitude, absenceReason, orderingIndex,
  }) => http
    .put(REQUEST_ABSENCE_REASON, {
      requestId,
      latitude,
      longitude,
      absenceReason,
      orderingIndex,
    })
    .then(process.getResponseData),
  {
    onSuccess: ({ process }) => {
      process.processStatus(200, 'Причина отсутствия отправлена');
    },
    onError: ({ logger }) => {
      logger.toMessage('error', 'Произошла ошибка');
    },
  }
);

export const useDeleteWaypoint = (): MutationResultPair<unknown, unknown, DeleteWaypointData, unknown> => useAPIMutation(
  ({ http, process }, {
    requestId, latitude, longitude, orderingIndex,
  }) => http
    .put(REQUEST_DELETE_WAYPOINT, {
      requestId,
      latitude,
      longitude,
      orderingIndex,
    })
    .then(process.getResponseData),
  {
    onSuccess: ({ process }) => {
      process.processStatus(200, 'Причина отсутствия отправлена');
    },
    onError: ({ logger }) => {
      logger.toMessage('error', 'Произошла ошибка');
    },
  }
);
