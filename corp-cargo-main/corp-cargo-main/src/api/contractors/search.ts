import { QueryConfig } from 'react-query';
import { APIQueryResult, useAPI } from 'api';
import * as t from 'io-ts';
import { ContractorsFilters, ContractorsResponse, PaginationParams } from 'stores/Contractors/Contractors.interface';
import { GET_ALL_CONTRACTORS } from 'constants/constants.api';

export const ContractorsSearchQuery = t.partial({
  page: t.number,
  size: t.number,
});
export type ContractorsSearchQuery = t.TypeOf<typeof ContractorsSearchQuery>;

declare module 'api' {
  interface Cache {
    searchContractors: {
      key: ['searchContractors', ContractorsFilters & { pagination?: PaginationParams }];
      value: ContractorsResponse;
    };
  }
}

export const useSearchContractors = (
  {
    pagination,
    ...filters
  }: ContractorsFilters & {
    pagination?: PaginationParams;
  } = {},
  config?: QueryConfig<ContractorsResponse, Error>
): APIQueryResult<ContractorsResponse, Error> => useAPI(
  ['searchContractors', { ...pagination, ...filters }],
  ({ http, process }) => http
    .get<ContractorsResponse>(GET_ALL_CONTRACTORS, {
      params: { ...pagination, ...filters },
      headers: { 'X-Paged': true },
    })
    .then(process.decodeResponseData(ContractorsResponse)),
  config
);
