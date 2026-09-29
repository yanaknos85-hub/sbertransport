import { useSearchMonitorRoutes } from 'api/planner';
import { usePlanner } from 'modules/Planner/context/PlannerContext';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { useOrganizationContext } from 'context/Organization.context';
import { EXECUTOR_GROUP_ALL_ID } from 'constants/constants.app';
import { Tab } from 'modules/Planner/types';

export const useMonitorQuery = () => {
  const { plannerStore } = useAppStoreContext();
  const {
    sortingRouteProperty,
    directionRouteAsc,
  } = usePlanner();

  const {
    organizationId,
    executorGroupId,
    isOrganization,
    emptyExecutorGroup
  } = useOrganizationContext();

  const isJournalActive = plannerStore.activeTab === Tab.journal;

  const orgOrExecutorGroupQuery = isOrganization
    ? { organizationId }
    : {
      executorGroupIds: (executorGroupId ?? []).filter((id) => id !== EXECUTOR_GROUP_ALL_ID),
      emptyExecutorGroup,
    };

  return useSearchMonitorRoutes({
    ...plannerStore.monitorFilters,
    ...orgOrExecutorGroupQuery,
    sortSetting: {
      property: sortingRouteProperty,
      directionAsc: directionRouteAsc,
    },
    pageSetting: {
      page: plannerStore.setMonitorListPageSize.page,
      size: plannerStore.setMonitorListPageSize.size,
    },
    statusSet: plannerStore.monitorFilters.statusSet,
  }, { enabled: isJournalActive });
};
