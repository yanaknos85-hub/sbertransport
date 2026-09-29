import { MutationResultPair } from 'react-query';

import { UPDATE_USER_PASS } from 'constants/constants.env';
import { getErrorMessage } from 'utils';

import { useAPIMutation } from './index';

export const useUpdatePass = (): MutationResultPair<
  unknown,
  any,
  {
    credentials: string;
  },
  unknown
> => useAPIMutation(
  ({ http, process }, { credentials }: { credentials: string }) => http
    .post(
      `${UPDATE_USER_PASS}`,
      {},
      { headers: { 'Authorization': 'Basic ', 'x-changePassword': `Basic ${credentials}` } }
    )
    .then(process.getResponseData),
  {
    onSuccess: ({ process }) => {
      process.processStatus(200, 'Обновление пароля прошло успешно!');
    },
    onError: ({ logger, error }) => {
      const errorMessage = error ? getErrorMessage(error) : 'При обновлении пароля произошла ошибка!';
      logger.toMessage('error', errorMessage);
      throw new Error(errorMessage);
    },
  }
);
