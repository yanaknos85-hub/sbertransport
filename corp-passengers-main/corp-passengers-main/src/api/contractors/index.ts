import * as t from 'io-ts';
import { stringify } from 'qs';
import indexById from 'utils/indexById';
import { ignore, getErrorMessage } from 'utils';
import { UUID } from 'utils/io-ts';
import { Contractor, ContractorSelect } from 'stores/Contractors/Contractors.interface';
import { AxiosError } from 'axios';
import { MutationResultPair, QueryConfig } from 'react-query';
import {
  ContractorDispatcher,
  ContractorDispatcherBase,
  ContractorDispatcherResponse
} from 'stores/ContractorDispatchers/ContractorDispatchers.interface';
import { DispatcherProjections } from 'constants/constants.app';
import { useProfile } from 'api/profile';
import {
  useAPI, useAPIMutation, updateQueryCache, APIQueryResult
} from '../index';
import {
  CONTRACTORS,
  DELETE_CONTRACTOR_DISPATCHER,
  GET_ALL_CONTRACTORS,
  GET_CONTRACTOR_DISPATCHERS,
  GET_CONTRACTOR_DISPATCHER
} from '../../constants/constants.api';

import { mkUseUploadEntity } from '../upload';

interface ContractorsSelectQueryResult {
  contractors: ContractorSelect[];
  byId: Record<string, ContractorSelect | undefined>;
}

declare module 'api' {
  interface Cache {
    contractors: {
      key: ['contractors'];
      value: {
        contractors: Contractor[];
        byId: Record<string, Contractor | undefined>;
      };
    };
    contractorsSelect: {
      key: ['contractors-select'];
      value: ContractorsSelectQueryResult;
    };
    singleContractor: {
      key: ['singleContractor', UUID | null | undefined];
      value: Contractor | undefined;
    };
    contractorDispatchers: {
      key: ['contractorDispatchers', UUID | null | undefined];
      value: ContractorDispatcherResponse | undefined;
    };
    contractorDispatchersFull: {
      key: ['contractorDispatchersFull', UUID | null | undefined];
      value: ContractorDispatcherBase[];
    };
    singleDispatcher: {
      key: ['singleDispatcher', UUID | null | undefined, UUID | null | undefined];
      value: ContractorDispatcher | undefined;
    };
  }
}

interface ContractorsQueryResult {
  contractors: Contractor[];
  byId: Record<string, Contractor | undefined>;
}

const serializeContractor = <T extends Omit<Contractor, 'id'> = Contractor>(serialize: (url: string) => string) => (
  contractor: T
) => ({
  ...contractor,
  jsonIntegrationParams: contractor.jsonIntegrationParams
    ? {
      ...contractor.jsonIntegrationParams,
      url: serialize(contractor.jsonIntegrationParams.url ?? ''),
    }
    : undefined,
});

const encodeContractor = <T extends Omit<Contractor, 'id'>>(contractor: T) => serializeContractor<T>(encodeURI)(contractor);
const decodeContractor = <T extends Omit<Contractor, 'id'>>(contractor: T) => serializeContractor<T>(decodeURI)(contractor);

const decodeContractors = (contractors: Contractor[]) => contractors.map(decodeContractor);

const raw2cache = (contractors: Contractor[]): ContractorsQueryResult => {
  const _contractors = decodeContractors(contractors);

  return {
    contractors: _contractors,
    byId: indexById<Contractor>(_contractors),
  };
};

const raw2cacheSelect = (contractors: ContractorSelect[]): ContractorsSelectQueryResult => ({
  contractors,
  byId: indexById<ContractorSelect>(contractors),
});

export const useContractors = ({
  params = {},
  config,
}: {
  params?: Record<string, unknown>;
  config?: QueryConfig<ContractorsQueryResult, AxiosError>;
} = {}): APIQueryResult<
  ContractorsQueryResult,
  AxiosError
> => useAPI(
  ['contractors'],
  ({ http, process }) => http
    .get<Contractor[]>(GET_ALL_CONTRACTORS, {
      params,
      paramsSerializer: params => stringify(params, { arrayFormat: 'repeat' }),
    })
    .then(process.decodeResponseData(t.array(Contractor)))
    .then(raw2cache),
  config
);

export const useSelectContractors = (
  config?: QueryConfig<ContractorsSelectQueryResult, AxiosError>
): APIQueryResult<ContractorsSelectQueryResult, AxiosError> => useAPI(
  ['contractors-select'],
  ({ http, process }) => http
    .get<ContractorSelect[]>(GET_ALL_CONTRACTORS, { params: { projection: 'SELECT' } })
    .then(process.decodeResponseData(t.array(ContractorSelect)))
    .then(raw2cacheSelect),
  config
);

export const useSingleContractor = (
  contractorId: UUID | null | undefined,
  config?: QueryConfig<Contractor | undefined, AxiosError>
): APIQueryResult<Contractor | undefined, AxiosError> => useAPI(
  ['singleContractor', contractorId],
  ({ http, process }) => contractorId
    ? http
      .get<Contractor>(`/${CONTRACTORS}/${contractorId}/`)
      .then(process.decodeResponseData(Contractor))
      .then(decodeContractor)
      .catch(err => {
        err.response.data.entity.name === 'Контрагент'
        && process.processStatus(404, 'Этот контрагент удален из системы');
        return undefined;
      })
    : undefined,
  config
);

export const useCreateContractor = (): MutationResultPair<Contractor, AxiosError, Omit<Contractor, 'id'>, unknown> => {
  // Используется для админа СМД, в остальных ролях бэк игнорирует. Нужно для создания контрагента на выбранную организацию
  const { organizationId } = useProfile().data;

  return useAPIMutation(
    ({ http, process }, contractor: Omit<Contractor, 'id'>) => http
      .post(`/${CONTRACTORS}/`, encodeContractor(contractor), { headers: { 'X-Organization-Id': organizationId } })
    // @ts-ignore
      .then<Contractor>(process.getResponseData),
    {
      onSuccess: ({
        cache, result: contractor, process, t: tt,
      }) => {
        process.processStatus(200, tt.contractors.addSuccess);
        updateQueryCache(cache, ['contractors'], ({ contractors }) => raw2cache([...contractors, contractor]));

        cache.refetchQueries(['searchContractors']);
        cache.refetchQueries(['singleContractor', contractor.id]);
      },
      onError: ({ error, logger }) => {
        logger.toMessage('error', getErrorMessage(error));
      },
    }
  );
};

export const useUpdateContractor = (): MutationResultPair<void, AxiosError, Contractor, unknown> => useAPIMutation(
  ({ http }, contractor: Contractor) => http.put(`/${CONTRACTORS}/${contractor.id}/`, encodeContractor(contractor), {}).then(ignore),
  {
    onSuccess: ({
      cache, variables: contractor, process, t: tt,
    }) => {
      process.processStatus(200, tt.contractors.editSuccess);
      updateQueryCache(cache, ['contractors'], ({ contractors }) => raw2cache(contractors.map(c => (c.id === contractor.id ? contractor : c)))
      );

      cache.refetchQueries(['searchContractors']);
      cache.refetchQueries(['singleContractor', contractor.id]);
      cache.refetchQueries(['contractorDispatchersFull', contractor.id]);
    },
  }
);

export const useDeleteContractor = () => useAPIMutation(
  ({ http }, contractorId: UUID | string) => http.delete<number>(`/${CONTRACTORS}/${contractorId}/`, {}).then(ignore),
  {
    onSuccess: ({
      cache, variables: contractorId, process, t: tt,
    }) => {
      process.processStatus(200, tt.contractors.deleteSuccess);
      updateQueryCache(cache, ['contractors'], ({ contractors }) => raw2cache(contractors.filter(contractor => contractor.id !== contractorId))
      );

      cache.refetchQueries(['searchContractors']);
      cache.refetchQueries(['singleContractor', contractorId]);
    },
  }
);

export const useUploadContractors = mkUseUploadEntity('contractor', {
  onSuccess: ({ cache }) => cache.invalidateQueries(['employeeResponse']),
});

export const useDeleteContractorDispatcher = () => useAPIMutation(
  ({ http }, contractorId: UUID) => (
    http.delete(DELETE_CONTRACTOR_DISPATCHER, { urlParams: { contractorId } }).then(ignore)
  ),
  {
    onSuccess: ({
      cache, variables: contractorId, process, t: tt,
    }) => {
      process.processStatus(200, tt.contractors.deleteDispatcherSuccess);
      updateQueryCache(cache, ['contractors'], ({ contractors }) => {
        const contractorIndex = contractors.findIndex(contractor => contractor.id === contractorId);
        if (contractorIndex !== -1) {
          contractors[contractorIndex].mainDispatcher = undefined;
        }
        return raw2cache(contractors);
      });

      cache.refetchQueries(['searchContractors']);
      cache.refetchQueries(['singleContractor', contractorId]);
      cache.refetchQueries(['contractorDispatchersFull', contractorId]);
    },
  }
);

// TODO: добавить параметры для пагинации, пока не используется, не доделал
export const useContractorDispatchers = (
  contractorId: UUID | null | undefined
): APIQueryResult<ContractorDispatcherResponse | undefined, AxiosError> => useAPI(['contractorDispatchers', contractorId], ({ http, process }) => contractorId
  ? http
    .get<ContractorDispatcherResponse>(GET_CONTRACTOR_DISPATCHERS, {
      urlParams: { contractorId },
      params: { projection: DispatcherProjections.FULL },
    })
    .then(process.decodeResponseData(ContractorDispatcherResponse))
  : undefined
);

export const useContractorDispatchersFull = (
  contractorId: UUID | null | undefined,
  config?: QueryConfig<ContractorDispatcherBase[], AxiosError>
): APIQueryResult<ContractorDispatcherBase[], AxiosError> => useAPI(
  ['contractorDispatchersFull', contractorId],
  ({ http, process }) => contractorId
    ? http
      .get<ContractorDispatcherBase[]>(GET_CONTRACTOR_DISPATCHERS, {
        urlParams: { contractorId },
        params: { projection: DispatcherProjections.SELECT },
      })
      .then(process.decodeResponseData(t.array(ContractorDispatcherBase)))
    : [],
  config
);

export const useContractorDispatcher = (
  contractorId: UUID | null | undefined,
  dispId: UUID | null | undefined,
  queryConfig: QueryConfig<ContractorDispatcher | undefined, AxiosError> = {}
): APIQueryResult<ContractorDispatcher | undefined, AxiosError> => useAPI(
  ['singleDispatcher', contractorId, dispId],
  ({ http, process }) => contractorId && dispId
    ? http
      .get<ContractorDispatcher>(GET_CONTRACTOR_DISPATCHER, { urlParams: { contractorId, dispId } })
      .then(process.decodeResponseData(ContractorDispatcher))
    : undefined,
  {
    ...queryConfig,
  }
);
