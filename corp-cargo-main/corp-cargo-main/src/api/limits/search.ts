import { useAPIMutation } from 'api';
import { MutationResultPair } from 'react-query';
import { UUID } from 'utils/io-ts';

import { LIMITS_SEARCH } from 'constants/constants.api';
import { LimitsSearchQuery, LimitsSearchResponse } from 'stores/Limits/LimitStatuse.interface';

export const useSearchLimits = (
  orgId: UUID,
  query: LimitsSearchQuery
): MutationResultPair<LimitsSearchResponse, unknown, { query: LimitsSearchQuery }, unknown> => {
  const { pagination, ...rest } = query;

  return useAPIMutation(
    ({ http, process }) => http
      .post(`${LIMITS_SEARCH}`, rest, { urlParams: { orgId }, params: { ...pagination } })
    // @ts-ignore
      .then<LimitsSearchResponse>(process.getResponseData),
    {
      // eslint-disable-next-line @typescript-eslint/no-empty-function
      onSuccess: () => {},
      onError: ({ t, logger }) => {
        logger.toMessage('error', t.ErrorBoundary.defaultError);
      },
    }
  );
};
