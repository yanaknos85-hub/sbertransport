import { Pagination } from 'antd';
import { observer } from 'mobx-react';
import React, { Suspense, useEffect, useState } from 'react';
import { useHistory, useRouteMatch } from 'react-router-dom';
import moment from 'moment';

import { joinUrl, TRangePickerArg } from 'utils';

import { TripsTabsFilters } from 'modules/EmployeeApp/EmployeeApp.constants';

import { StoreNames } from 'stores/StoreNames.enum';
import { ITripRequestData } from 'stores/Trip/Trip.interface';

import { SpinWrapped } from 'shared/components';
import { TripsRequestFilter } from './TripRequestDetailedView/TripsRequestFilter/TripsRequestFilter';
import { useRequestFilterProps } from 'shared/components/RequestFilter/useRequestFilterProps';
import { TabsNav, TabsNavOption } from 'shared/components/TabsNav';
import { useTabsNavProps } from 'shared/components/TabsNav/useTabsNavProps';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { useRouteParamSub } from 'shared/hooks/useRouteParamSub';

import { RenderTripRequestListItem } from './TripRequestList/renderTripRequestListItem';
import Empty from './Empty/Empty';

import { TransportTypeEnum } from 'stores/TransportTypes/TransportTypes.interface';
import { useGetDelegates } from 'api/delegates';
import { CLEAR_QUERY_CONFIG } from 'constants/constants.app';
import { useDepartment } from 'api/departments';

import styles from './styles.module.scss';

const tabs: TabsNavOption[] = [
  {
    key: TripsTabsFilters.planned,
    label: 'Активные',
  },
  {
    key: TripsTabsFilters.final,
    label: 'Завершённые',
  },
];

const DEFAULT_DATE_RANGE: TRangePickerArg = [
  moment().add({ M: -3 }).startOf('date'),
  moment().add('2', 'weeks').endOf('date'),
];

const TripsPage: React.FC<any> = observer(() => {
  const {
    [StoreNames.tripStore]: tripStore, [StoreNames.corporateStore]: corporateStore, [StoreNames.selfStore]: selfStore,
  } = useAppStoreContext();
  const [current, setPage] = useState(tripStore.isFromDetailedView ? tripStore.pagination.number + 1 : 1);
  const [pageSize, setPageSize] = useState(10);
  const { data: department } = useDepartment(selfStore.orgId, selfStore.depId);
  const supervisorId = department?.departmentHead?.id;
  const { data: delegatesData } = useGetDelegates(
    {
      orgId: selfStore.orgId,
      depId: selfStore.depId,
      supId: supervisorId,
      size: 100,
    },
    {
      enabled: selfStore.orgId && selfStore.depId && supervisorId,
      ...CLEAR_QUERY_CONFIG,
      cacheTime: 1000,
    }
  );
  const [isLoading, setIsLoading] = useState(true);
  const filterProps = useRequestFilterProps();
  const tabProps = useTabsNavProps();

  const [tripTransportType, setTripTransportType] = useState<TransportTypeEnum | undefined>(tripStore.isFromDetailedView ? tripStore.transportType : undefined);
  const [tripDateRange, setTripDateRange] = useState<TRangePickerArg>(
    tripStore.isFromDetailedView && tripStore.dateRange ? tripStore.dateRange : DEFAULT_DATE_RANGE
  );

  const [statusFilter] = useRouteParamSub('filter', { options: Object.values(TripsTabsFilters), isStrict: true });

  const history = useHistory();

  const match = useRouteMatch();
  const itemClickHandler = (id: string): void => {
    history.push(joinUrl(`${match.url}/${id}`));
  };

  const {
    status, setStatusFilter,
  } = filterProps;
  const { activeTab } = tabProps;

  useEffect(() => {
    if (!tripStore.isFromDetailedView) {
      tripStore.clearPagination();
      tripStore.transportType = undefined;
      tripStore.dateRange = null;
    }

    return () => {
      tripStore.toggleIsFromDetailedView(false);
    };
  }, []);

  const handleClearFilters = () => {
    setTripTransportType(undefined);
    setTripDateRange(DEFAULT_DATE_RANGE);
    setPage(1);
  };

  useEffect(() => {
    tripStore.transportType = tripTransportType;
  }, [tripTransportType]);

  useEffect(() => {
    tripStore.dateRange = tripDateRange;
    filterProps.setDateRange(tripDateRange);
  }, [tripDateRange]);

  useEffect(() => {
    const fetchDataConfig = {
      [tabs[0].key]: (data: ITripRequestData) => tripStore.loadRequestListNonTerminal(data),
      [tabs[1].key]: (data: ITripRequestData) => tripStore.loadRequestListTerminal(data),
    };

    const fetchData = (data: ITripRequestData) => {
      setIsLoading(true);
      fetchDataConfig[activeTab](data).then(
        () => {
          setTimeout(() => setIsLoading(false), 2000);
        }
      );
    };

    const data: ITripRequestData = {
      pageSetting: {
        page: tripStore.isFromDetailedView ? tripStore.pagination.number : current - 1,
        size: pageSize,
      },
      sortSetting: { directionAsc: false },
      transportTypeEnum: tripTransportType,
      requestStatusSet: status && status !== 'ALL' ? [status] : undefined,
      desiredDate: tripDateRange?.[0] && tripDateRange?.[1] ? {
        start: Number(tripDateRange[0].utc()),
        end: Number(tripDateRange[1].utc()),
      } : undefined,
    };

    fetchData(data);
  }, [tripStore, tripStore.selfEmployee, activeTab, status, current, pageSize, tripTransportType, tripDateRange]);

  const data = tripStore.tripRequestList || [];
  const handleSizeChange = (_: number, sizePage: number) => setPageSize(sizePage);
  const { totalElements, size } = tripStore.pagination;

  useEffect(() => {
    setStatusFilter(statusFilter);
  });

  useEffect(() => {
    corporateStore.loadAllPositions(tripStore.selfEmployee.organizationId);
  }, []);

  return (
    <div className={styles.wrapper}>
      <TabsNav
        currentKey={statusFilter}
        options={tabs}
        defaultActiveTab={tabs[0]}
        onTabClick={handleClearFilters}
        {...tabProps}
      />
      <TripsRequestFilter
        {...filterProps}
        transportType={tripTransportType}
        setTransportType={setTripTransportType}
        dateRange={tripDateRange}
        setPage={setPage}
        setDateRange={setTripDateRange}
      />
      <>
        {isLoading ? (
          <div style={{ height: 200 }}>
            <SpinWrapped />
          </div>
        ) : !data.length ? (
          <Empty />
        ) : (
          <Suspense fallback={null}>
            {data.map(item => (
              <RenderTripRequestListItem
                key={item.id}
                request={item}
                onClickHandler={itemClickHandler}
                isFinished={activeTab === 'final'}
                delegates={delegatesData?.content ? delegatesData.content : []}
                supervisor={department?.departmentHead}
              />
            ))}
            <div className={styles.pagination}>
              <Pagination
                defaultCurrent={current}
                current={current}
                total={totalElements}
                onChange={setPage}
                hideOnSinglePage
                pageSize={size}
                onShowSizeChange={handleSizeChange}
                totalBoundaryShowSizeChanger={10}
              />
            </div>
          </Suspense>
        )}
      </>
    </div>
  );
});

export default TripsPage;
