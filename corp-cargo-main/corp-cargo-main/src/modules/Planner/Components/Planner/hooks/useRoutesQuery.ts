import { useSearchRoutes } from 'api/planner';
import { usePlanner } from 'modules/Planner/context/PlannerContext';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { useOrganizationContext } from 'context/Organization.context';
import { EXECUTOR_GROUP_ALL_ID } from 'constants/constants.app';
import { Tab } from 'modules/Planner/types';

export const useRoutesQuery = () => {
  const { plannerStore } = useAppStoreContext();
  const {
    routeFilters,
    sortingRouteProperty,
    directionRouteAsc
  } = usePlanner();

  const { organizationId, executorGroupId, isOrganization, emptyExecutorGroup } = useOrganizationContext();

  const isActive = plannerStore.activeTab === Tab.planner;

  const query = isOrganization
    ? { organizationId }
    : {
        executorGroupIds: (executorGroupId ?? []).filter((id) => id !== EXECUTOR_GROUP_ALL_ID),
        emptyExecutorGroup,
      };

  return useSearchRoutes({
    ...routeFilters,
    ...query,
    sortSetting: {
      property: sortingRouteProperty,
      directionAsc: directionRouteAsc,
    },
    pageSetting: {
      page: plannerStore.setRoutesListPageSize.page,
      size: plannerStore.setRoutesListPageSize.size,
    },
  }, { enabled: isActive });
};
