import { MutationResultPair } from 'react-query';
import { AxiosError } from 'axios';
import moment from 'moment';

import { useAPIMutation } from 'api';

import { SYSTEM_MESSAGES } from 'constants/constants.app';
import { PHONE_CONFIRMATION, BLOCK_PHONE_CONFIRMATION } from 'constants/constants.env';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores';
import { PhoneConfirmationRequest, BlockPhoneConfirmation } from 'stores/Confirmation/Confirmation.interface';

export const usePhoneConfirmation = (): MutationResultPair<unknown, AxiosError, PhoneConfirmationRequest, unknown> => {
  const {
    [StoreNames.selfStore]: selfStore,
  } = useAppStoreContext();

  return useAPIMutation(
    ({ http }, { code }) => (
      http.put<unknown>(PHONE_CONFIRMATION, undefined, { headers: { 'x-code': code } })
    ),
    {
      onSuccess: ({ process }) => {
        process.processStatus(200, SYSTEM_MESSAGES.phoneConfirmationSuccess);
        selfStore.getSelfEmployee();
      },
      onError: ({ logger, error }) => {
        let message = error.response?.data?.message;

        if (message?.includes('Wrong code')) {
          message = 'Неверный код подтверждения';
        }

        logger.toMessage('error', message ?? SYSTEM_MESSAGES.phoneConfirmationError);
      },
    }
  );
};

export const useBlockPhoneConfirmation = (): MutationResultPair<BlockPhoneConfirmation, AxiosError, unknown, unknown> => (
  useAPIMutation(
    ({ http, process }) => (
      http.get<BlockPhoneConfirmation>(BLOCK_PHONE_CONFIRMATION)
        .then(process.getResponseData)
    ),
    {
      onSuccess: ({ result, logger }) => {
        if (result.blocked) {
          const minutesLeft = moment(result.nextAllowTime).diff(moment(), 'minutes');

          logger.toMessage(
            'warning',
            `Вы превысили количество попыток на запрос кода подтверждения. Повторите действие через ${minutesLeft} минут`
          );
        }
      },
      onError: ({ logger }) => {
        logger.toMessage('error', SYSTEM_MESSAGES.blockPhoneConfirmationError);
      },
    }
  )
);
