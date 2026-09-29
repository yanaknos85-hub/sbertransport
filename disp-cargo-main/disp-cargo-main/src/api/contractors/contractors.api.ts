import { UUID } from 'utils/io-ts';
import { MutationResultPair, QueryConfig } from 'react-query';
import { AxiosError } from 'axios';
import { useAPI, useAPIMutation, APIQueryResult } from 'api';
import { AUTOPARKS, GET_CONTRACTOR } from './contractors.constants';
import { Autoparks, AutoparksFilters, Contractor } from './contractors.types';
import { ignore } from 'utils/utils';

declare module 'api' {
  interface Cache {
    contractor: { key: ['contractor', UUID]; value: Contractor };
  }
}

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
