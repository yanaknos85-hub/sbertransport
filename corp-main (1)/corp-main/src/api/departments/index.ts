/* eslint-disable @typescript-eslint/no-explicit-any */
/* eslint-disable @typescript-eslint/no-unused-vars */
import {
  useAPIMutation
} from 'api';
import {
  GET_DEPARTMENT_LEVEL
} from 'constants/constants.api';
import { MutationResultPair } from 'react-query';
import { UUID } from 'utils/io-ts';

import { ignore } from '../../utils';
import { DepartmentLevels } from 'constants/constants.app';

export const useSelectDepartmentsForDepartmentLevel = (
  orgId: UUID,
  departments: DepartmentLevels
): MutationResultPair<string[], Error, unknown, unknown> => useAPIMutation(
  async ({ http, process }) => http
    .post<string[]>(GET_DEPARTMENT_LEVEL, departments, {
      urlParams: { orgId },
    })
    .then(process.getResponseData),
  {
    onSuccess: ignore,
  }
);
