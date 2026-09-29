import { MutationResultPair, QueryConfig } from 'react-query';
import { AxiosError } from 'axios';

import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import { CONTRACTOR_ALL_AUTOPARKS, CONTRACTOR_AUTOPARK } from 'api/contractors/contractors.constants';
import { UUID } from 'utils/io-ts';

import {
  AutoPark,
  AutoParks,
  AutoParksQuery,
  CreateAutoparkParams,
  DeleteAutoparkParams,
  EditAutoparkParams,
  GetAutoparkParams
} from './autopark.types';
import { getErrorMessage } from 'utils/utils';

declare module 'api' {
  interface Cache {
    autoparkBranches: { key: ['autopark-branches', UUID, AutoParksQuery]; value: AutoParks };
    autoparkBranch: { key: ['autopark-branch', UUID]; value: AutoPark };
  }
}

export const useGetAutoParks = ({
  contractorId,
  query = { page: 0, size: 10 },
}: {
  contractorId: UUID;
  query?: AutoParksQuery;
}): APIQueryResult<AutoParks, Error> => (
  useAPI(['autopark-branches', contractorId, query], ({ http, process }) => (
    http
      .get<AutoParks>(CONTRACTOR_ALL_AUTOPARKS, { urlParams: { contractorId }, params: query })
      .then(process.decodeResponseData(AutoParks))
  ), {
    keepPreviousData: true,
  })
);

export const useAutopark = (
  { contractorId, autoparkId }: GetAutoparkParams,
  queryConfig?: QueryConfig<AutoPark>
): APIQueryResult<AutoPark> => (
  useAPI(['autopark-branch', autoparkId as UUID], ({ http, process }) => (
    http
      .get<AutoPark>(CONTRACTOR_AUTOPARK, { urlParams: { contractorId, autoparkId } })
      .then(process.decodeResponseData(AutoPark))
  ), queryConfig)
);

export const useGetAutopark = ({
  contractorId,
  autoparkId,
}: GetAutoparkParams): MutationResultPair<AutoPark, unknown, never, unknown> => (
  useAPIMutation(
    ({ http }) => (
      http.get<AutoPark>(CONTRACTOR_AUTOPARK, { urlParams: { contractorId, autoparkId } }).then(res => res.data)
    ),
    { onSuccess: async () => ({} as AutoPark) }
  )
);

export const useCreateAutopark = ({
  contractorId,
}: {
  contractorId: string;
}): MutationResultPair<unknown, unknown, CreateAutoparkParams, unknown> => (
  useAPIMutation(
    ({ http }, { autopark }: CreateAutoparkParams) => (
      http.post(CONTRACTOR_ALL_AUTOPARKS, autopark, { urlParams: { contractorId } })
    ),
    {
      onSuccess: ({
        cache, process, t,
      }) => {
        process.processStatus(200, t.AutoparkTable.updateComplete);
        cache.refetchQueries(['autopark-branches']);
      },
    }
  ));

export const useEditAutopark = ({
  contractorId,
}: {
  contractorId: string;
}): MutationResultPair<unknown, unknown, EditAutoparkParams, unknown> => (
  useAPIMutation(
    ({ http }, { autoparkId, autopark }: EditAutoparkParams) => (
      http.put(CONTRACTOR_AUTOPARK, autopark, { urlParams: { contractorId, autoparkId } })
    ),
    {
      onSuccess: ({
        cache, process, t, variables,
      }) => {
        process.processStatus(200, t.AutoparkTable.updateComplete);
        cache.refetchQueries(['autopark-branches']);
        cache.refetchQueries(['autopark-branch', variables.autoparkId]);
      },
    }
  )
);

export const useDeleteAutopark = ({
  contractorId,
}: {
  contractorId: string;
}): MutationResultPair<unknown, unknown, DeleteAutoparkParams, unknown> => (
  useAPIMutation(
    ({ http }, { autoparkId }: DeleteAutoparkParams) => (
      http.delete(CONTRACTOR_AUTOPARK, { urlParams: { contractorId, autoparkId } })
    ),
    {
      onSuccess: ({
        cache, process, t,
      }) => {
        process.processStatus(200, t.AutoparkTable.deleteComplete);
        cache.refetchQueries(['autopark-branches']);
      },
      onError: ({
        error, logger, t,
      }) => {
        logger.toMessage('error', getErrorMessage(error as AxiosError) ?? t.AutoparkTable.deleteError);
      },
    }
  )
);
