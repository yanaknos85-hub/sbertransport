/* eslint-disable @typescript-eslint/no-explicit-any */
import { MutationResultPair } from 'react-query';

import { useAPIMutation } from 'api';

interface Comment {
  comment: string;
}

type ComplaintData = Comment & {
  id: string;
};

const COMPLAINT_URL = 'url';

export const useComplaintPost = (): MutationResultPair<unknown, unknown, { comment: Comment; id: string }, unknown> => useAPIMutation(
  ({ http, process }, { comment, id }): Promise<any> => http.post<ComplaintData>(COMPLAINT_URL, { comment, id }).then(status => process.getResponseStatus(status)),
  {
    onSuccess: ({ process }) => {
      process.processStatus(200, 'Ваше сообщение отправлено');
    },
    onError: ({ logger }) => {
      logger.toMessage('error', 'Что-то пошло не так. Попробуйте отправить сообщение ещё раз');
    },
  }
);
