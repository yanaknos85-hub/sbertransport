import { RESET_USER_PASS } from 'constants/constants.api';
import { MutationResultPair } from 'react-query';
import { UUID } from 'utils/io-ts';
import { useAPIMutation } from './index';

export const useResetUserPass = (): MutationResultPair<
  unknown,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  any,
  {
    userId: UUID;
  },
  unknown
> => useAPIMutation(
  // eslint-disable-next-line @stylistic/max-len
  ({ http, process }, { userId }: { userId: UUID }) => http.put(RESET_USER_PASS, {}, { urlParams: { userId } }).then(process.getResponseData),
  {
    onSuccess: ({ process, t }) => {
      process.processStatus(200, t.crudMessages.resetPassSuccess);
    },
    onError: ({
      logger, t, error,
    }) => {
      const errorMessage = error ? error.message : t.crudMessages.resetPassFailed;
      logger.toMessage('error', errorMessage);
    },
  }
);
