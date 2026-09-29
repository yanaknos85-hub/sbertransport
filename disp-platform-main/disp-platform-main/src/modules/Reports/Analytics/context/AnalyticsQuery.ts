import { useProfile } from 'api/profile/profile.api';
import { AutoAnalyticsFilters } from 'api/analytics/analytics.types';
import { createCallableCtx } from 'utils/createCallableContext';
import { useRoleMap } from 'hooks/useRoleMap';
import { useQuery } from 'hooks/useQuery';

const useHook = () => {
  const { isAdmin, isManager } = useRoleMap();

  const { contractorId, autoparkId } = useProfile().data;

  const today = new Date();

  const { query, setQuery } = useQuery<AutoAnalyticsFilters>({
    contractorIds: isAdmin ? undefined : [contractorId],
    autoparkIds: (isAdmin || isManager || !autoparkId) ? undefined : [autoparkId],
    year: today.getFullYear(),
    months: [today.getMonth() + 1],
  }, {
    noPagination: true,
  });

  return {
    query,
    setQuery,
  };
};

export const [useAnalyticsQuery, AnalyticsQueryProvider] = createCallableCtx(useHook, { name: 'AnalyticsQueryProvider' });
