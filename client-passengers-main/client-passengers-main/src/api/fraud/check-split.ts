import { AxiosError } from 'axios';

import { APIMutationConfig, useAPIMutation } from 'api';

import { CHECK_PERSONAL_CAR_SPLIT } from 'constants/constants.env';

import { CheckSplitMutationErrorPayload, CheckSplitMutationVariables } from './types';

export const useCheckSplitMutation = (
  config?: APIMutationConfig<
    unknown,
    AxiosError<CheckSplitMutationErrorPayload> | AxiosError,
    CheckSplitMutationVariables,
    unknown
  >
) => useAPIMutation(({ http }, variables) => http.post(CHECK_PERSONAL_CAR_SPLIT, variables), config);
