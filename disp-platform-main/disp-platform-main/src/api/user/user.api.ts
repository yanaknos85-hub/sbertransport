
import { MutationResultPair } from 'react-query';
import { useAPIMutation } from 'api';
import { UUID } from 'utils/io-ts';
import { RESET_USER_PASS } from './user.constants';

export const useResetUserPass = (): MutationResultPair<
  unknown,
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  any,
  {
    userId: UUID;
  },
  unknown
> => useAPIMutation(
  ({ http, process }, { userId }: { userId: UUID }) => (
    http.put(RESET_USER_PASS, {}, { urlParams: { userId } }).then(process.getResponseData)
  ),
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
