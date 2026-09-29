import { AxiosError, AxiosResponse } from 'axios';

import { APIMutationConfig, useAPIMutation } from 'api';

import { CHECK_ABSENCE } from 'constants/constants.env';

import { CheckAbsenceMutationVariables, CheckAbsenceResponse } from './types';

export const useCheckAbsenceMutation = (
  config?: APIMutationConfig<
    AxiosResponse<CheckAbsenceResponse>,
    AxiosError<unknown>,
    CheckAbsenceMutationVariables,
    unknown
  >
) => useAPIMutation(({ http }, variables) => http.post(CHECK_ABSENCE, variables), config);
