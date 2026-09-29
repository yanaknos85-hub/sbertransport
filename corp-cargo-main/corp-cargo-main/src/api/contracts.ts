import { MutationResultPair, QueryConfig } from 'react-query';
import { UUID } from 'utils/io-ts';
import { getErrorMessage, ignore } from 'utils';
import {
  APIQueryResult, TypeAtKey, updateQueryCache, useAPI, useAPIMutation
} from 'api';
import {
  SEARCH_CONTRACTS_CARGO,
  GET_CONTRACTS_CARGO,
  CONTRACT_PARAMS_CARGO,
  DELETE_CONTRACT_CARGO,
  CONTRACT_ADD_PARAMS_CARGO
} from 'constants/constants.api';
import { Contract, ContractsSearchQuery, ContractsSearchResponse } from 'stores/Contracts/Contracts.interface';
import indexById from 'utils/indexById';
import { mkUseUploadEntity } from './upload';
import { AxiosError } from 'axios';

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

export const useContractsByIdCargo = (
  id: UUID,
  enabled?: boolean,
  suspense?: boolean
): APIQueryResult<Contract, Error> => useAPI(
  ['contract-cargo', id],
  ({ http, process }) => http.get<Contract>(`${GET_CONTRACTS_CARGO}${id}`).then(process.decodeResponseData(Contract)),
  { enabled, suspense }
);

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
export const useCargoContractsMutation = (): MutationResultPair<ContractsSearchResponse, unknown, ContractsSearchQuery, unknown> => useAPIMutation(
  ({ http, process }, { pagination, ...filters }) => http
    .post<ContractsSearchResponse>(SEARCH_CONTRACTS_CARGO, filters, { params: pagination })
    .then(process.decodeResponseData(ContractsSearchResponse)),
  {
    onSuccess: ignore,
  }
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

export const useCreateContractCargo = (): MutationResultPair<Contract, unknown, Contract, unknown> => useAPIMutation(
  ({ http, process }, contract: Contract) => http
    .post<Contract>(CONTRACT_ADD_PARAMS_CARGO, Contract.encode(contract),
      { hush: [406, 409] }
    )
    .then<Contract>(process.decodeResponseData(Contract)),
  {
    onSuccess: ({
      cache, result: contract, process, t,
    }) => {
      process.processStatus(200, t.Contracts.addSuccess);
      updateQueryCache(cache, ['contracts'], ({ contracts }) => raw2cache([...contracts, contract]));
      cache.refetchQueries(['search-cargo-contracts]']);
    },
    onError: ({ error, logger }) => {
      logger.toNotify('error', getErrorMessage(error as AxiosError), 'Ошибка');
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
    onError: ({ error, logger }) => {
      logger.toNotify('error', getErrorMessage(error as AxiosError), 'Ошибка');
    },
  }
);

// eslint-disable-next-line @stylistic/max-len
export const useDeleteContractCargo = (): MutationResultPair<boolean, unknown, { contractId: UUID }, unknown> => useAPIMutation(
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
);

export const useUploadContractsCargo = mkUseUploadEntity('contractCargo', {
  onSuccess: ignore,
  suspense: false,
});
