import React, { useEffect } from 'react';
import { useRouteMatch } from 'react-router-dom';
import { observer } from 'mobx-react';

import { useRequestFilterProps } from 'shared/components/RequestFilter/useRequestFilterProps';
import { TabsNav, TabsNavOption } from 'shared/components/TabsNav';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { StoreNames } from 'stores/StoreNames.enum';
import { filterByTransportType } from 'utils/filterTripsByTransportType';
import {
  getActiveRequests, getDataWithFilters, getUnActiveRequests, isRealArray
} from './limitRequestUtils';
import { LimitRequestFilter } from '../Components/LimitRequestFilter/LimitRequestFilter';
import UserLimitRequestList from '../Components/UserLimitRequestList/UserLimitRequestList';
import { ISpentActionsType } from '../useDetailedLimitsMapper';
import styles from './list.module.scss';

const limitRequestTabs: TabsNavOption[] = [
  {
    key: 'active',
    label: 'Активные',
  },
  {
    key: 'closed',
    label: 'Завершённые',
  },
];

const LimitRequests = observer(() => {
  const match = useRouteMatch<{ filter: string }>();
  const { filter } = match.params;
  const isActiveFilter = filter === 'active';

  const { [StoreNames.limitsStore]: limitsStore, [StoreNames.selfStore]: selfStore } = useAppStoreContext();

  const { limitsRequestsByAuthor } = limitsStore;

  const filterProps = useRequestFilterProps();

  const requestsByStatus = isActiveFilter
    ? getActiveRequests(limitsRequestsByAuthor)
    : getUnActiveRequests(limitsRequestsByAuthor, selfStore.empId);

  const requestsByTransportType = filterProps.transportType
    ? filterByTransportType(requestsByStatus, filterProps.transportType)
    : requestsByStatus;

  const getActualDataSource = (requests: ISpentActionsType[]) => {
    if (filterProps.dateRange && isRealArray(filterProps.dateRange)) {
      return getDataWithFilters(requests, filterProps.dateRange);
    }

    return requests;
  };

  const dataSource = getActualDataSource(requestsByTransportType);

  useEffect(() => {
    if (filterProps.dateRange && filterProps.transportType && requestsByStatus.length && !dataSource.length) {
      filterProps.setTransportType(undefined);
    }
  }, [filterProps]);

  return (
    <div className={styles.pageWrapper}>
      <div className={styles.content}>
        <TabsNav
          currentKey={filter}
          options={limitRequestTabs}
          defaultActiveTab={limitRequestTabs[0]}
        />
        {!!requestsByStatus.length && (
          <LimitRequestFilter limitsRequests={getActualDataSource(requestsByStatus)} {...filterProps} />
        )}
        <UserLimitRequestList dataSource={dataSource} />
      </div>
    </div>
  );
});

export default LimitRequests;
