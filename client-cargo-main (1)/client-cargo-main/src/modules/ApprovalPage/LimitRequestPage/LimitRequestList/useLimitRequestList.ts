import { useEffect } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import { TabsNavOption } from 'shared/components/TabsNav';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { LimitRequestStatusEnum } from 'stores/Limits/LimitsRequest.interface';
import { LimitRequestModel } from 'stores/Limits/Models/LimitRequest.model';
import { StoreNames } from 'stores/StoreNames.enum';
import { LimitRequestTabsFilters } from 'constants/EmployeeApp.constants';
import { joinUrl } from 'utils/Misc';

const statuses = {
  [LimitRequestTabsFilters.active]: [LimitRequestStatusEnum.AWAITING_APPROVAL],
  [LimitRequestTabsFilters.final]: [
    LimitRequestStatusEnum.APPROVED,
    LimitRequestStatusEnum.CANCELLED,
    LimitRequestStatusEnum.DONE,
  ],
};

export const useLimitRequestList = (): {
  requestList: LimitRequestModel[];
  itemClickHandler: (id: string) => void;
  tabOptions: TabsNavOption[];
  tabKeys: LimitRequestTabsFilters[];
  statuses: typeof statuses;
} => {
  const history = History();
  const match = useRouteMatch();

  const { [StoreNames.limitsRequestStore]: limitsRequestStore } = useAppStoreContext();

  useEffect(() => {
    limitsRequestStore.getList();
  }, [limitsRequestStore]);

  const itemClickHandler = (id: string): void => {
    history.push(joinUrl(`${match.url}/${id}`));
  };

  const tabOptions: TabsNavOption[] = [
    {
      key: LimitRequestTabsFilters.active,
      label: 'Активные',
    },
    {
      key: LimitRequestTabsFilters.final,
      label: 'Завершённые',
    },
  ];

  const tabKeys = Object.values(LimitRequestTabsFilters);

  return {
    requestList: limitsRequestStore.list,
    itemClickHandler,
    tabOptions,
    tabKeys,
    statuses,
  };
};
