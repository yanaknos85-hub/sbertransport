import { MutationResultPair } from 'react-query';

import { useAPIMutation } from 'api';

import { REQUEST_ABSENCE_REASON } from 'constants/constants.env';

export interface AbsenceRequestData {
  requestId: string;
  latitude: number | undefined;
  longitude: number | undefined;
  absenceReason: string;
}

export const useAbsenceReason = (): MutationResultPair<unknown, unknown, AbsenceRequestData, unknown> => useAPIMutation(
  ({ http, process }, {
    requestId, latitude, longitude, absenceReason,
  }) => http
    .put(REQUEST_ABSENCE_REASON, {
      requestId,
      latitude,
      longitude,
      absenceReason,
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
