import { MutationResultPair, QueryConfig } from 'react-query';
import { AxiosError } from 'axios';
import * as t from 'io-ts';
import { UUID } from 'utils/io-ts';
import { getErrorMessage, ignore } from 'utils';
import {
  APIQueryResult, TypeAtKey, updateQueryCache, useAPI, useAPIMutation
} from 'api';
import {
  DELETE_CONTRACT,
  GET_CONTRACTS,
  SEARCH_CONTRACTS,
  CONTRACT_ADD_PARAMS,
  CONTRACT_PARAMS,
  SEARCH_CONTRACTS_CARGO,
  GET_CONTRACTS_CARGO,
  CONTRACT_PARAMS_CARGO,
  DELETE_CONTRACT_CARGO,
  CONTRACT_ADD_PARAMS_CARGO
} from 'constants/constants.api';
import { Contract, ContractsSearchQuery, ContractsSearchResponse } from 'stores/Contracts/Contracts.interface';
import indexById from 'utils/indexById';
import { mkUseUploadEntity } from './upload';

declare module 'api' {
  interface Cache {
    contracts: {
      key: ['contracts'];
      value: {
        contracts: Contract[];
        byId: Record<string, Contract | undefined>;
      };
    };
    contract: { key: ['contract', UUID]; value: Contract };
    contractCargo: { key: ['contract-cargo', UUID]; value: Contract };
    searchContracts: {
      key: ['search-contracts', ContractsSearchQuery];
      value: ContractsSearchResponse;
    };
    searchCargoContracts: {
      key: ['search-cargo-contracts', ContractsSearchQuery];
      value: ContractsSearchResponse;
    };
  }
}

type CacheItem = TypeAtKey<['contracts']>;

const raw2cache = (contracts: Contract[]): CacheItem => ({
  contracts,
  byId: indexById(contracts),
});

export const useContracts = (): APIQueryResult<CacheItem> => useAPI(['contracts'], ({ http, process }) => http
  .get<Contract[]>(`${GET_CONTRACTS}`)
  .then(process.decodeResponseData(t.array(Contract)))
  .then(raw2cache)
);

export const useContractsById = (id: UUID, enabled?: boolean, suspense?: boolean): APIQueryResult<Contract, Error> => (
  useAPI(
    ['contract', id],
    ({ http, process }) => http.get<Contract>(`${GET_CONTRACTS}${id}`).then(process.decodeResponseData(Contract)),
    { enabled, suspense }
  )
);

export const useContractsByIdCargo = (
  id: UUID,
  enabled?: boolean,
  suspense?: boolean
): APIQueryResult<Contract, Error> => useAPI(
  ['contract-cargo', id],
  ({ http, process }) => http.get<Contract>(`${GET_CONTRACTS_CARGO}${id}`).then(process.decodeResponseData(Contract)),
  { enabled, suspense }
);

export const useSearchContracts = (
  query: ContractsSearchQuery
): MutationResultPair<ContractsSearchResponse, unknown, { query: ContractsSearchQuery }, unknown> => {
  const { pagination, ...rest } = query;

  return useAPIMutation(
    ({ http, process }) => (
      // @ts-ignore
      http.post(SEARCH_CONTRACTS, rest, { params: pagination }).then<ContractsSearchResponse>(process.getResponseData)
    ), {
      onSuccess: ignore,
      onError: ({ t, logger }) => {
        logger.toMessage('error', t.ErrorBoundary.defaultError);
      },
    }
  );
};

// eslint-disable-next-line @stylistic/max-len
export const useContractsMutation = (): MutationResultPair<ContractsSearchResponse, unknown, ContractsSearchQuery, unknown> => (
  useAPIMutation(
    ({ http, process }, { pagination, ...filters }) => http
      .post<ContractsSearchResponse>(SEARCH_CONTRACTS, filters, { params: pagination })
      .then(process.decodeResponseData(ContractsSearchResponse)),
    {
      onSuccess: ignore,
    }
  )
);

export const useContractsWithParams = (
  query: ContractsSearchQuery,
  config?: QueryConfig<ContractsSearchResponse>
): APIQueryResult<ContractsSearchResponse> => {
  const { pagination, ...body } = query;

  return useAPI(
    ['search-contracts', query],
    ({ http, process }) => http
      .post<ContractsSearchResponse>(SEARCH_CONTRACTS, body, { params: pagination })
      .then(process.decodeResponseData(ContractsSearchResponse)),
    config
  );
};

export const useSearchContractsCargo = (
  query: ContractsSearchQuery
): MutationResultPair<ContractsSearchResponse, unknown, { query: ContractsSearchQuery }, unknown> => {
  const { pagination, ...rest } = query;

  return useAPIMutation(
    ({ http, process }) => http
      .post(SEARCH_CONTRACTS_CARGO, rest, { params: pagination })
    // @ts-ignore
      .then<ContractsSearchResponse>(process.getResponseData),
    {
      onSuccess: ignore,
      onError: ({ t, logger }) => {
        logger.toMessage('error', t.ErrorBoundary.defaultError);
      },
    }
  );
};

// eslint-disable-next-line @stylistic/max-len
export const useCargoContractsMutation = (): MutationResultPair<ContractsSearchResponse, unknown, ContractsSearchQuery, unknown> => (
  useAPIMutation(
    ({ http, process }, { pagination, ...filters }) => http
      .post<ContractsSearchResponse>(SEARCH_CONTRACTS_CARGO, filters, { params: pagination })
      .then(process.decodeResponseData(ContractsSearchResponse)),
    {
      onSuccess: ignore,
    }
  )
);

export const useCargoContractsWithParams = (
  query: ContractsSearchQuery,
  config?: QueryConfig<ContractsSearchResponse>
): APIQueryResult<ContractsSearchResponse> => {
  const { pagination, ...body } = query;

  return useAPI(
    ['search-cargo-contracts', query],
    ({ http, process }) => http
      .post<ContractsSearchResponse>(SEARCH_CONTRACTS_CARGO, body, { params: pagination })
      .then(process.decodeResponseData(ContractsSearchResponse)),
    config
  );
};

export const useCreateContract = (): MutationResultPair<Contract, AxiosError, Contract, unknown> => useAPIMutation(
  ({ http, process }, contract: Contract) => http
    .post<Contract>(CONTRACT_ADD_PARAMS, Contract.encode(contract), { hush: [409] })
    .then<Contract>(process.decodeResponseData(Contract)),
  {
    onSuccess: ({
      cache, result: contract, process, t,
    }) => {
      process.processStatus(200, t.Contracts.addSuccess);
      updateQueryCache(cache, ['contracts'], ({ contracts }) => raw2cache([...contracts, contract]));
      cache.refetchQueries(['search-contracts']);
    },
    onError: ({ error, logger }) => {
      if (error?.request?.status === 409) {
        logger.toNotify('error', getErrorMessage(error), 'Ошибка');
      }
    },
  }
);

export const useCreateContractCargo = (): MutationResultPair<Contract, unknown, Contract, unknown> => useAPIMutation(
  ({ http, process }, contract: Contract) => http
    .post<Contract>(CONTRACT_ADD_PARAMS_CARGO, Contract.encode(contract))
    .then<Contract>(process.decodeResponseData(Contract)),
  {
    onSuccess: ({
      cache, result: contract, process, t,
    }) => {
      process.processStatus(200, t.Contracts.addSuccess);
      updateQueryCache(cache, ['contracts'], ({ contracts }) => raw2cache([...contracts, contract]));
      cache.refetchQueries(['search-cargo-contracts']);
    },
  }
);

export const useUpdateContract = (): MutationResultPair<void, unknown, Contract, unknown> => useAPIMutation(
  ({ http }, contract) => (
    http.put(CONTRACT_PARAMS, Contract.encode(contract), { urlParams: { contractId: contract.id } }).then(ignore)
  ), {
    onSuccess: ({
      cache, variables: contract, process, t,
    }) => {
      process.processStatus(200, t.Contracts.editSuccess);
      updateQueryCache(cache, ['contracts'], ({ contracts }) => raw2cache(contracts.map(c => (c.id === contract.id ? contract : c)))
      );

      cache.refetchQueries(['search-contracts']);
      cache.refetchQueries(['contract', contract.id]);
    },
  }
);

export const useUpdateContractCargo = (): MutationResultPair<void, unknown, Contract, unknown> => useAPIMutation(
  ({ http }, contract) => http
    .put(CONTRACT_PARAMS_CARGO, Contract.encode(contract), { urlParams: { contractId: contract.id } })
    .then(ignore),
  {
    onSuccess: ({
      cache, variables: contract, process, t,
    }) => {
      process.processStatus(200, t.Contracts.editSuccess);
      updateQueryCache(cache, ['contracts'], ({ contracts }) => raw2cache(contracts.map(c => (c.id === contract.id ? contract : c)))
      );

      cache.refetchQueries(['search-cargo-contracts']);
      cache.refetchQueries(['contract-cargo', contract.id]);
    },
  }
);

export const useDeleteContract = (): MutationResultPair<boolean, AxiosError, { contractId: UUID }, unknown> => (
  useAPIMutation(
    ({ http }, { contractId }: { contractId: UUID }) => http
      .delete<number>(DELETE_CONTRACT, { urlParams: { contractId }, hush: [409] })
      .then(r => r.status === 200),
    {
      onSuccess: ({
        cache, variables: { contractId }, process, t,
      }) => {
        process.processStatus(200, t.Contracts.deleteSuccess);
        updateQueryCache(cache, ['contracts'], ({ contracts }) => raw2cache(contracts.filter(contract => contract.id !== contractId))
        );

        cache.refetchQueries(['search-contracts']);
        cache.refetchQueries(['contract', contractId]);
      },
      onError: ({ error, logger }) => {
        if (error?.request?.status === 409) {
          const message = getErrorMessage(error).replace('409 CONFLICT', '').replaceAll(`"`, '');
          logger.toNotify('error', message, 'Ошибка');
        }
      },
    }
  )
);

export const useDeleteContractCargo = (): MutationResultPair<boolean, unknown, { contractId: UUID }, unknown> => (
  useAPIMutation(
    ({ http }, { contractId }: { contractId: UUID }) => http
      .delete<number>(DELETE_CONTRACT_CARGO, { urlParams: { contractId } })
      .then(r => r.status === 200),
    {
      onSuccess: ({
        cache, variables: { contractId }, process, t,
      }) => {
        process.processStatus(200, t.Contracts.deleteSuccess);
        updateQueryCache(cache, ['contracts'], ({ contracts }) => raw2cache(contracts.filter(contract => contract.id !== contractId))
        );

        cache.refetchQueries(['search-cargo-contracts']);
        cache.refetchQueries(['contract-cargo', contractId]);
      },
    }
  )
);

export const useUploadContracts = mkUseUploadEntity('contract', {
  onSuccess: ignore,
  suspense: false,
});

export const useUploadContractsCargo = mkUseUploadEntity('contractCargo', {
  onSuccess: ignore,
  suspense: false,
});
