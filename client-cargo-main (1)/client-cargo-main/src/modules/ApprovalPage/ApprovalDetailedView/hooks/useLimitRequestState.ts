import type { RefetchOptions } from 'react-query/types/core/query.d';

import { useGetLimitByRequestId } from 'api/limits';
import { LimitRequestInfo, LimitTypeTitles } from 'stores/Limits/Limit.interface';
import { CLEAR_QUERY_CONFIG } from 'constants/constants.app';

const getRequestLimitType = (requestLimit: LimitRequestInfo | undefined, requestLimitLoading: boolean): string => {
  const limitTypeText = requestLimit?.limitType
    ? LimitTypeTitles[requestLimit?.limitType]
    : 'Не удалось загрузить информацию о лимите';
  return requestLimitLoading ? 'Загрузка информации о лимите...' : limitTypeText;
};

export function useLimitRequestState(
  reqId: string,
  triggerRequest: boolean
): {
    requestLimit: LimitRequestInfo | undefined;
    requestLimitLoading: boolean;
    limitTypeString: string;
    refetchLimitRequest: (options?: RefetchOptions | undefined) => Promise<LimitRequestInfo>;
  } {
  const {
    data: requestLimit,
    isFetching,
    isLoading,
    refetch: refetchLimitRequest,
  } = useGetLimitByRequestId(reqId, {
    enabled: triggerRequest,
    ...CLEAR_QUERY_CONFIG,
  });

  const requestLimitLoading = isFetching || isLoading;
  const limitTypeString = getRequestLimitType(requestLimit, requestLimitLoading);

  return {
    requestLimit,
    requestLimitLoading,
    limitTypeString,
    refetchLimitRequest,
  };
}
