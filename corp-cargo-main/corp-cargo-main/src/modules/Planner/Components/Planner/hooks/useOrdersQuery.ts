import { useSearchOrders } from 'api/planner';
import { usePlanner } from 'modules/Planner/context/PlannerContext';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { useOrganizationContext } from 'context/Organization.context';
import { EXECUTOR_GROUP_ALL_ID } from 'constants/constants.app';
import { Tab } from 'modules/Planner/types';

export const useOrdersQuery = () => {
  const { plannerStore } = useAppStoreContext();
  const {
    orderFilters,
    sortingOrderProperty,
    directionOrderAsc,
  } = usePlanner();

  const { organizationId, executorGroupId, isOrganization, emptyExecutorGroup } = useOrganizationContext();

  const isActive = plannerStore.activeTab === Tab.planner;

  const query = isOrganization
    ? { organizationId }
    : {
        executorGroupIds: (executorGroupId ?? []).filter((id) => id !== EXECUTOR_GROUP_ALL_ID),
        emptyExecutorGroup,
      };

  return useSearchOrders({
    ...orderFilters,
    ...query,
    sortSetting: {
      property: sortingOrderProperty,
      directionAsc: directionOrderAsc,
    },
    pageSetting: {
      page: plannerStore.setOrdersListPageSize.page,
      size: plannerStore.setOrdersListPageSize.size,
    },
  }, { enabled: isActive });
};
