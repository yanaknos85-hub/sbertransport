import indexById from 'utils/indexById';
import { UUID } from 'utils/io-ts';
import { MutationResultPair, QueryConfig } from 'react-query';
import { AxiosError } from 'axios';
import {
  useAPI, useAPIMutation, APIQueryResult, TypeAtKey
} from 'api';
import { AUTOPARK, AUTOPARKS, GET_CONTRACTOR } from './contractors.constants';
import {
  Autopark, Autoparks, AutoparksFilters, Contractor
} from './contractors.types';
import { ignore } from 'utils/utils';
import { useProfile } from 'api/profile/profile.api';

type CacheContractor = TypeAtKey<['contractors']>;
interface CacheContractors { contractors: Contractor[]; byId: Record<string, Contractor> }

export enum ContractorsKeys {
  Autopark = 'autopark',
}

declare module 'api' {
  interface Cache {
    contractors: { key: ['contractors']; value: CacheContractors };
    contractor: { key: ['contractor', UUID]; value: Contractor };
    autopark: { key: [typeof ContractorsKeys.Autopark, UUID]; value: Autopark };
  }
}

const raw2cache = (contractors: Contractor[]): CacheContractor => ({ contractors, byId: indexById(contractors) });

export const useContractors = (): APIQueryResult<CacheContractor, Error> => useAPI(
  ['contractors'],
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  ({ http, process }) => raw2cache([] as Contractor[])
  // http
  //   .get<Contractor[]>(GET_ALL_CONTRACTORS)
  //   .then(process.decodeResponseData(t.array(Contractor)))
  //   .then(raw2cache),
);

export const useContractor = (
  contractorId: UUID,
  config?: QueryConfig<Contractor, Error>
): APIQueryResult<Contractor, Error> => useAPI(
  ['contractor', contractorId],
  ({ http, process }) => http
    .get<Contractor>(GET_CONTRACTOR, { urlParams: { contractorId } })
    .then(process.decodeResponseData(Contractor)),
  config
);

export const useToggleAutoAssign = (contractorId: UUID): MutationResultPair<unknown, AxiosError, boolean, unknown> => (
  useAPIMutation(
    ({ http }, isOn) => http.patch(GET_CONTRACTOR, [{ field: 'autoassign', value: isOn }], { urlParams: { contractorId } }).then(),
    {
      onSuccess: ({ cache }) => cache.refetchQueries(['contractor', contractorId]),
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

/** Запрос на получения автопарка (контрагента) */
export const useAutopark = ():
MutationResultPair<Autopark, unknown, UUID, unknown> => (
  useAPIMutation(
    ({ http, process }, autoparkId: UUID) => (
      http
        .get<Autopark>(AUTOPARK, { urlParams: { autoparkId } })
        .then(process.decodeResponseData(Autopark))
    ),
    {
      onSuccess: ignore,
    }
  )
);

/** Получение данных автопарка */
export const useAutoparkApi = (
  id: UUID,
  config?: QueryConfig<Autopark, Error>
) => useAPI(
  [ContractorsKeys.Autopark, id],
  ({ http, process }) => http
    .get<Autopark>(AUTOPARK, { urlParams: { autoparkId: id } })
    .then(process.decodeResponseData(Autopark)),
  config
);

/** Получение своего автопарка */
export const useSelfAutopark = (
  config?: QueryConfig<Autopark, Error>
) => {
  const { contractorId } = useProfile().data;

  return useAutoparkApi(contractorId, {
    ...config,
    keepPreviousData: true,
    enabled: !!contractorId,
    staleTime: Infinity,
    cacheTime: Infinity,
  });
};
