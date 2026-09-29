import { QueryConfig } from 'react-query';

import { APIQueryResult, useAPI } from 'api';
import { Delegate } from 'stores/Delegates/Delegates.interface';
import { GET_DELEGATES } from 'constants/constants.env';

declare module 'api' {
  interface Cache {
    delegateList: {
      key: ['delegateList', string, string, string | undefined];
      value: Delegate[];
    };
  }
}

export const useGetDelegates = (
  {
    depId,
    orgId,
    supId,
  }: {
    depId: string;
    orgId: string;
    supId: string | undefined;
  },
  options: QueryConfig<Delegate[], unknown>
): APIQueryResult<Delegate[], unknown> => useAPI(
  ['delegateList', depId, orgId, supId],
  ({ http, process }) => {
    const supIdStr = supId || '';
    return http
      .get<Delegate[]>(GET_DELEGATES, {
        urlParams: {
          orgId, depId, supId: supIdStr,
        },
      })
      .then(process.getResponseData);
  },
  options
);
