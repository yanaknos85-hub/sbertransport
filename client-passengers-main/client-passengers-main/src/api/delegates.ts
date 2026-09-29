import { QueryConfig } from 'react-query';

import { APIQueryResult, useAPI } from 'api';

import { GET_DELEGATES } from 'constants/constants.env';

import { TDelegateList } from 'stores/Delegates/Delegates.interface';

declare module 'api' {
  interface Cache {
    delegateList: {
      key: ['delegateList', string, string, string | undefined];
      value: TDelegateList;
    };
  }
}

export const useGetDelegates = (
  {
    depId,
    orgId,
    supId,
    size,
  }: {
    depId: string;
    orgId: string;
    supId: string | undefined;
    size?: number;
  },
  options: QueryConfig<TDelegateList, unknown>
): APIQueryResult<TDelegateList, unknown> => useAPI(
  ['delegateList', depId, orgId, supId],
  ({ http, process }) => {
    const supIdStr = supId || '';
    return http
      .get<TDelegateList>(GET_DELEGATES, {
        urlParams: {
          orgId, depId, supId: supIdStr,
        },
        params: {
          size,
        },
      })
      .then(process.getResponseData);
  },
  options
);
