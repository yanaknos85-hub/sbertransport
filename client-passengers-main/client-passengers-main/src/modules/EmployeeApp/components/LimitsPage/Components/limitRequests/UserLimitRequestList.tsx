/* eslint-disable @typescript-eslint/no-explicit-any */
import { Divider, List } from 'antd';
import { observer } from 'mobx-react';
import React, { ReactNode } from 'react';
import { useRouteMatch } from 'react-router-dom';

import { EmployeeAppLinks, EmployeeAppLinksTitles } from 'modules/EmployeeApp/EmployeeApp.constants';

import PageLayout from 'shared/components/PageLayout/PageLayout';

import { RequestFilter } from 'shared/components/RequestFilter/RequestFilter';
import { useRequestFilterProps } from 'shared/components/RequestFilter/useRequestFilterProps';
import { TabsNav, TabsNavOption } from 'shared/components/TabsNav';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { LimitRequestInfo } from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores/StoreNames.enum';

import { filterByTransportType } from 'utils/filterTripsByTransportType';

import { EmptyApprovementList } from '../../../../shared/EmptyFactory';
import styles from '../../../ApprovalPage/LimitRequestPage/LimitRequestList/list.module.scss';
import {
  getActiveRequests, getDataWithFilters, getUnActiveRequests, isRealArray
} from './limitRequestUtils';
import { UserLimitRequestItem } from './UserLimitRequestItem';

const UserLimitRequestList = observer(() => {
  const {
    [StoreNames.limitsStore]: limits,
    [StoreNames.selfStore]: selfStore,
    [StoreNames.configStore]: configStore,
  } = useAppStoreContext();

  const { history } = configStore;
  const match = useRouteMatch<{ filter: string }>();
  const { filter } = match.params;
  const isActiveFilter = filter === 'active';

  const filterProps = useRequestFilterProps();

  const fullData = isActiveFilter
    ? getActiveRequests(limits.limitRequestsStats)
    : getUnActiveRequests(limits.limitRequestsStats, selfStore.empId);

  const dataByTransportType = filterProps.transportType
    ? filterByTransportType(fullData, filterProps.transportType)
    : fullData;

  const actualDataSource
    = filterProps.dateRange && isRealArray(filterProps.dateRange)
      ? getDataWithFilters(dataByTransportType, filterProps.dateRange)
      : dataByTransportType;

  const itemClickHandler = (id: string): void => {
    history.push(`${match.url}/${id}`);
  };

  const tabs: TabsNavOption[] = [
    {
      key: 'active',
      label: 'Активные',
    },
    {
      key: 'closed',
      label: 'Завершённые',
    },
  ];

  return (
    <PageLayout title={EmployeeAppLinksTitles[EmployeeAppLinks.approvementLimit]}>
      <div className={styles.content}>
        <TabsNav
          currentKey={filter}
          options={tabs}
          defaultActiveTab={tabs[0]}
        />
        <RequestFilter {...filterProps} />
        <div className={styles.requestsCountInfo}>{`Всего заявок: ${actualDataSource.length}`}</div>
        <Divider />
        <List
          className={styles.list}
          dataSource={actualDataSource as any}
          locale={{ emptyText: <EmptyApprovementList /> }}
          renderItem={(request: LimitRequestInfo): ReactNode => (
            <UserLimitRequestItem request={request as any} itemClickHandler={itemClickHandler} />
          )}
          pagination={{
            position: 'bottom',
          }}
        />
      </div>
    </PageLayout>
  );
});

export default UserLimitRequestList;
