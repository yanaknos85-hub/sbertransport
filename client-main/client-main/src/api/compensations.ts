import { useAPIMutation } from 'api';

import {
  GET_CARGO_COMPENSATION_STATISTICS
} from 'constants/constants.env';
import { MutationResultPair } from 'react-query';

import {
  CompensationResponse,
  CompensationResponseType,
  CompensationStatisticsQuery
} from 'shared/models/Approval.interface';

declare module 'api' {
  interface Cache {
    cargoCompensationStatistics: {
      key: ['cargoCompensationStatistics', CompensationStatisticsQuery];
      value: CompensationResponseType;
    };
  }
}

/* Статистика выплат компенсаций */
export const useCargoCompensationStatistics = (
  query: CompensationStatisticsQuery
): MutationResultPair<CompensationResponseType, unknown, CompensationStatisticsQuery, unknown> => useAPIMutation(
  ({ http, process }) => http
    .post<CompensationResponseType>(GET_CARGO_COMPENSATION_STATISTICS, query)
    .then(process.decodeResponseData(CompensationResponse)),
  {
    onSuccess: ({ cache }) => {
      cache.invalidateQueries(['cargoCompensationStatistics']);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', 'Ошибка при получении статистики по компенсациям');
    },
  }
);
