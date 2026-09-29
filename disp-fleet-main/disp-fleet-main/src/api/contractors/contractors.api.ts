import { MutationResultPair, QueryConfig } from 'react-query';
import { Autopark, Autoparks, AutoparksFilters } from './contractors.types';
import { APIQueryResult, useAPI, useAPIMutation } from 'api';
import { AUTOPARKS } from './contractors.constants';
import { ignore } from 'utils/utils';
import { UUID } from 'utils/io-ts';
import { useProfile } from 'api/profile/profile.api';

export enum ContractorsKeys {
  Autopark = 'autopark',
}

declare module 'api' {
  interface Cache {
    autopark: { key: [typeof ContractorsKeys.Autopark, UUID]; value: Autopark };
  }
}

/** Запрос на получения автопарка (контрагента) */
export const useGetAutopark = (
  autoparkId: UUID,
  queryConfig?: QueryConfig<Autopark, Error>
): APIQueryResult<Autopark> => (
  useAPI(
    [ContractorsKeys.Autopark, autoparkId],
    ({ http, process }) => (
      http
        .get<Autopark>(`${AUTOPARKS}${autoparkId}/`)
        .then(process.decodeResponseData(Autopark))
    ), queryConfig)
);

/** Запрос на получения автопарка (контрагента) */
export const useAutopark = ():
MutationResultPair<Autopark, unknown, UUID, unknown> => (
  useAPIMutation(
    ({ http, process }, autoparkId: UUID) => (
      http
        .get<Autopark>(`${AUTOPARKS}${autoparkId}/`)
        .then(process.decodeResponseData(Autopark))
    ),
    {
      onSuccess: ignore,
    }
  )
);

/** Запрос списка автопарков (контрагентов) */
export const useAutoparks = (): MutationResultPair<Autoparks, unknown, AutoparksFilters, unknown> => (
  useAPIMutation(
    ({ http, process }, query: AutoparksFilters) => (
      http
        .get<Autoparks>(AUTOPARKS, { params: query })
        .then(process.decodeResponseData(Autoparks))
    ),
    {
      onSuccess: ignore,
    }
  )
);

/** Получение своего автопарка */
export const useSelfAutopark = (
  config?: QueryConfig<Autopark, Error>
) => {
  const { contractorId } = useProfile().data;

  return useGetAutopark(contractorId, {
    ...config,
    keepPreviousData: true,
    enabled: !!contractorId,
    staleTime: Infinity,
    cacheTime: Infinity,
  });
};
