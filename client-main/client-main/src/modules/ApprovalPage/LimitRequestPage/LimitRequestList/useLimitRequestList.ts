import { useEffect } from 'react';
import { useRouteMatch } from 'react-router-dom';

import { LimitRequestTabsFilters } from 'constants/constants.app';
import { TabsNavOption } from 'shared/components/TabsNav';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { PageSetting, usePagination } from 'shared/hooks/usePagination';
import { RequestFilterProps, useRequestFilterProps } from 'shared/components/RequestFilter/useRequestFilterProps';
import { StoreNames } from 'stores/StoreNames.enum';
import { ActiveLimitRequestInfo, OldLimitRequestInfo } from 'stores/Limits/Limit.interface';

const tabOptions: TabsNavOption[] = [
  {
    key: LimitRequestTabsFilters.active,
    label: 'Активные',
  },
  {
    key: LimitRequestTabsFilters.closed,
    label: 'Завершённые',
  },
];

export const useLimitRequestList = (): {
  requestList: ActiveLimitRequestInfo | OldLimitRequestInfo;
  tabOptions: TabsNavOption[];
  tabKey: string;
  filterProps: RequestFilterProps;
  isActiveFilter: boolean;
  pageSetting: PageSetting;
  refetch: () => void;
  setPageSetting(setting: PageSetting): void;
} => {
  const match = useRouteMatch<{ filter: string }>();
  const { pageSetting, setPageSetting } = usePagination({ page: 0, size: 10 });
  const { [StoreNames.limitsStore]: limitsStore } = useAppStoreContext();
  const filterProps = useRequestFilterProps();

  const { filter } = match.params;
  const isActiveFilter = filter === 'active';
  const { activeLimitsRequestByApprover, oldLimitsRequestByApprover } = limitsStore;
  const requestList = isActiveFilter ? activeLimitsRequestByApprover : oldLimitsRequestByApprover;

  useEffect(() => {
    if (!limitsStore.currentDepartmentSharing.length) {
      limitsStore.initStore();
    }
  }, [limitsStore]);

  const getLimitsRequestByApprover = () => {
    const params: PageSetting & { transportType?: string } = { ...pageSetting };

    if (filterProps.transportType) {
      params.transportType = filterProps.transportType;
    }

    if (isActiveFilter) {
      limitsStore.getActiveLimitsRequestByApprover(params);
    } else {
      limitsStore.getOldLimitsRequestByApprover(params);
    }
  };

  useEffect(() => {
    getLimitsRequestByApprover();
  }, [limitsStore, isActiveFilter, pageSetting, filterProps.transportType]);

  return {
    requestList,
    tabOptions,
    tabKey: filter,
    filterProps,
    isActiveFilter,
    pageSetting,
    refetch: getLimitsRequestByApprover,
    setPageSetting,
  };
};
