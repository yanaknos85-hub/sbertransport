/* eslint-disable @stylistic/implicit-arrow-linebreak */
import { Divider, List } from 'antd';
import { observer } from 'mobx-react';
import moment from 'moment';
import React, { ReactNode, useEffect } from 'react';
import { useRouteMatch } from 'react-router-dom';

import { sortByTime } from 'utils';

import { EmployeeAppLinks, EmployeeAppLinksTitles } from 'modules/EmployeeApp/EmployeeApp.constants';

import { LIMIT_REQUEST_STATUS, LimitRequestInfo } from 'stores/Limits/Limit.interface';
import { StoreNames } from 'stores/StoreNames.enum';

import PageLayout from 'shared/components/PageLayout/PageLayout';
import { RequestFilter } from 'shared/components/RequestFilter/RequestFilter';
import { useRequestFilterProps } from 'shared/components/RequestFilter/useRequestFilterProps';
import { TabsNav, TabsNavOption } from 'shared/components/TabsNav';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';

import { filterByTransportType } from 'utils/filterTripsByTransportType';
import { isMomentTuple, TRangePickerArg } from 'utils/Types';

import { EmptyApprovementList } from '../../../../shared/EmptyFactory';
import { isRealArray } from '../../../LimitsPage/Components/limitRequests/limitRequestUtils';
import { LimitRequestListItem } from './LimitRequestListItem';

import styles from './list.module.scss';

const getAuthorRequestsWithFilters = (
  dataByTransportType: LimitRequestInfo[],
  range: TRangePickerArg
): LimitRequestInfo[] =>
  // eslint-disable-next-line consistent-return, array-callback-return
  dataByTransportType.filter(x => {
    // FIXME consistent-return, array-callback-return
    if (isMomentTuple(range)) {
      return moment(x.creationTime).isBetween(range[0].startOf('day'), range[1].endOf('day'), undefined, '[)');
    }
  });

const LimitRequestList = observer(() => {
  const {
    [StoreNames.configStore]: configStore,
    [StoreNames.limitsStore]: limitsStore,
    [StoreNames.selfStore]: selfStore,
  } = useAppStoreContext();

  useEffect(() => {
    limitsStore.getLimitRequest();
    limitsStore.getAllLimitRequests();
  }, [limitsStore]);

  const filterProps = useRequestFilterProps();
  const { history } = configStore;
  const match = useRouteMatch<{ filter: string }>();

  const { filter } = match.params;

  const isActiveFilter = filter === 'active';

  const getActiveAuthorRequests = (): LimitRequestInfo[] => sortByTime(limitsStore.limitRequests.filter(x => x.status === LIMIT_REQUEST_STATUS.INIT));

  const getUnActiveAuthorRequests = (): LimitRequestInfo[] => sortByTime(
    limitsStore.allLimitRequests.filter(x => x.approverDtoList.some(y => y.employeeId === selfStore.empId && y.approvalState !== 'AWAITING_APPROVAL')
    )
  );

  // TODO эта логика должна быть на бэке. будет перенесена в рамках работы с TRANSPORT-2014
  const activeRequests = getActiveAuthorRequests();
  const unActiveRequests = getUnActiveAuthorRequests();
  const fullData = isActiveFilter ? activeRequests : unActiveRequests;

  const dataByTransportType = filterProps.transportType
    ? filterByTransportType(fullData, filterProps.transportType)
    : fullData;

  const actualDataSource
    = filterProps.dateRange && isRealArray(filterProps.dateRange)
      ? getAuthorRequestsWithFilters(dataByTransportType, filterProps.dateRange)
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
          dataSource={actualDataSource}
          locale={{ emptyText: <EmptyApprovementList /> }}
          renderItem={(request: LimitRequestInfo): ReactNode => (
            <LimitRequestListItem
              request={request}
              limitsStore={limitsStore}
              itemClickHandler={itemClickHandler}
              viewDisabled={!isActiveFilter}
            />
          )}
          pagination={{
            position: 'bottom',
          }}
        />
      </div>
    </PageLayout>
  );
});

export default LimitRequestList;
