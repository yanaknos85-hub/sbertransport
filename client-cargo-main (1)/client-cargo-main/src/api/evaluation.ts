import { MutationResultPair } from 'react-query';

import { useAPIMutation } from 'api';
import {
  CARGO_EVALUATION_POST,
  MOCKED_API_PREFIX
} from 'constants/constants.api';
import { TEvaluationRequest } from 'modules/Evaluation/types';

interface evaluationData {
  rate: number;
  reasons?: any;
}

declare module 'api' {
  interface Cache {
    evaluationCargo: {
      key: ['evaluationCargo', string];
      value: TEvaluationRequest | null;
    };
  }
}

export const usePostEvaluation = (): MutationResultPair<
  unknown,
  unknown,
  { requestId: string; rating: number; reasons?: string[]; comment?: string },
  unknown
> => useAPIMutation(
  ({ http, process }, {
    requestId, rating, reasons, comment,
  }): Promise<any> => http
    .post<evaluationData>(`${MOCKED_API_PREFIX}${CARGO_EVALUATION_POST}/`, {
      requestId,
      rating,
      reasons,
      comment,
    })
    .then(status => process.getResponseStatus(status)),
  {
    onSuccess: ({ cache }) => {
      cache.invalidateQueries(['evaluationCargo']);
    },
    onError: ({ logger }) => {
      logger.toMessage('error', 'Не удалось отправить сообщение. Попробуйте отправить сообщение ещё раз');
    },
  }
);
