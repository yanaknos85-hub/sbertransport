import { MutationResultPair } from 'react-query';

import { useAPIMutation } from 'api';
import { ignore } from 'utils/utils';

import { AutoParkBranch, AutoParkBranches, AutoParkBranchesFilters } from './branches.types';
import { ALL_AUTOPARK_BRANCH, AUTOPARK_BRANCH } from './branches.constants';

/** Запрос на получения филиала */
export const useAutoparkBranch = (autoparkId: string):
MutationResultPair<AutoParkBranch, unknown, string, unknown> => (
  useAPIMutation(
    ({ http, process }, branchId: string) => (
      http
        .get<AutoParkBranch>(AUTOPARK_BRANCH, { urlParams: { autoparkId, branchId } })
        .then(process.decodeResponseData(AutoParkBranch))
    ),
    {
      onSuccess: ignore,
    }
  )
);

/** Запрос на получение списка филиалов */
export const useAutoparkBranches = ():
MutationResultPair<AutoParkBranches, unknown, AutoParkBranchesFilters, unknown> => (
  useAPIMutation(
    ({ http, process }, { autoparkId = '', ...query }) => (
      http
        .get<AutoParkBranches>(ALL_AUTOPARK_BRANCH, {
          urlParams: { autoparkId },
          params: { active: true, ...query },
        })
        .then(process.decodeResponseData(AutoParkBranches))
    ),
    {
      onSuccess: ignore,
    }
  )
);
