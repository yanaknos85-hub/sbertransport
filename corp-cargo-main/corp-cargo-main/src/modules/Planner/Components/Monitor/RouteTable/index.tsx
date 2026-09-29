import React, { FC, useRef, useState, useEffect } from 'react';
import { observer } from 'mobx-react';
import { useHistory } from '@sber-sbertransport/mf-core';
import { useLocation } from 'react-router-dom';
import { Table } from 'antd';
import moment from 'moment';
import { Pagination } from 'modules/Planner/Pagination/Pagination';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { useTableConfig } from 'shared/hooks/useTableConfig';
import { useForm } from 'antd/lib/form/Form';
import { useTableFields } from './useTableFields';
import { useMonitorQuery } from './hooks/useMonitorQuery';
import { tableScrollConfiguration } from '../utils';
import { usePlanner } from '../../../context/PlannerContext';
import { Filters } from '../Filters';
import { RouteCancelModal } from './RouteCancelModal';
import { MonitorFilters } from '../../Modals/MonitorFilters';
import { MonitorFiltersType, Tab } from '../../../types';

import styles from '../styles.module.scss';

const CargoRoutesTable: FC = () => {
  const [form] = useForm();
  const [filtersForm] = useForm();
  const [filterIdForm] = useForm();
  const location = useLocation();
  const tableRef = useRef<HTMLDivElement>(null);
  const tableConfig = useTableConfig(tableRef, tableScrollConfiguration);
  const { plannerStore } = useAppStoreContext();

  const [reason, setReason] = useState('');
  const [routeId, setRouteId] = useState('');
  const [modalVisible, setModalVisible] = useState(false);
  const [textAreaVisible, setTextAreaVisible] = useState(false);

  const fields = useTableFields({ setRouteId, setModalVisible });
  const history = useHistory();

  const {
    directionRouteAsc,
    sortingRouteProperty,
    isVisibleMonitorFilters,
    handleCloseMonitorFilters,
    defaultSortRoute,
  } = usePlanner();

  const { data } = useMonitorQuery();

  const resetUrlFilters = () => {
    const searchParams = new URLSearchParams(location.search);
    searchParams.delete('routeNumber');
    history.replace({
      pathname: location.pathname,
      search: searchParams.toString()
    });
  };

  useEffect(() => {
    if (plannerStore.activeTab !== Tab.journal) return;

    const searchParams = new URLSearchParams(location.search);
    const routeNumber = searchParams.get('routeNumber');
    const tab = searchParams.get('tab');

    if (routeNumber && tab === 'journal') {
      plannerStore.setMonitorFilters({
        ...plannerStore.monitorFilters,
        humanReadableId: routeNumber
      });
      form.setFieldsValue({ humanReadableId: routeNumber });
      filtersForm.setFieldsValue({ humanReadableId: routeNumber });
    } else if (plannerStore.monitorFilters.humanReadableId) {
      resetUrlFilters();
    }
  }, [location.search, plannerStore.activeTab]);

  useEffect(() => {
    if (plannerStore.monitorFilters.humanReadableId) {
      const searchParams = new URLSearchParams(location.search);
      searchParams.set('routeNumber', plannerStore.monitorFilters.humanReadableId);
      history.replace({
        pathname: location.pathname,
        search: searchParams.toString()
      });
    }
  }, [plannerStore.monitorFilters.humanReadableId]);

  const getFilteredRoutes = (params: MonitorFiltersType) => {
    const {
      regionFrom,
      regionTo,
      creationDateRange,
      desiredDateRange,
      statusSet,
      contractors,
      humanReadableId,
      ...restParams
    } = params;

    if (!humanReadableId) {
      resetUrlFilters();
    }
    const creationDateRangeRequest =
      Array.isArray(creationDateRange) &&
        creationDateRange.length === 2 &&
        creationDateRange[0] &&
        creationDateRange[1]
        ? {
          start: moment(creationDateRange[0]).utc().startOf('day').toISOString(),
          end: moment(creationDateRange[1]).utc().endOf('day').toISOString(),
        }
        : {};

    const desiredDateRangeRequest =
      Array.isArray(desiredDateRange) &&
        desiredDateRange.length === 2 &&
        desiredDateRange[0] &&
        desiredDateRange[1]
        ? {
          start: moment(desiredDateRange[0]).utc().startOf('day').toISOString(),
          end: moment(desiredDateRange[1]).utc().endOf('day').toISOString(),
        }
        : {};

    const request = {
      ...restParams,
      regionFrom: regionFrom || [],
      regionTo: regionTo || [],
      statusSet: statusSet || plannerStore.monitorFilters.statusSet,
      creationDateRange: creationDateRangeRequest,
      desiredDateRange: desiredDateRangeRequest,
      contractors: contractors || [],
      ...(humanReadableId && { humanReadableId }),
    };

    plannerStore.setMonitorFilters(request);
    handleCloseMonitorFilters();
    defaultSortRoute();
  };

  const handleCancel = () => {
    setReason('');
    setTextAreaVisible(false);
    setModalVisible(false);
    form.setFieldsValue({ reason: '' });
  };

  const handleReset = () => {
    plannerStore.setMonitorFilters({
      ...plannerStore.monitorFilters,
      humanReadableId: undefined,
    });
    form.resetFields();
    filterIdForm.resetFields();
    form.setFieldsValue({
      ...plannerStore.initialMonitorFilters,
      creationDateRange: [],
      desiredDateRange: [],
    });
  };

  return (
    <React.Suspense fallback={<SpinWrapped />}>
      <Filters
        getFilteredRoutes={getFilteredRoutes}
        filtersForm={form}
        filterIdForm={filterIdForm}
      />
      {data?.content ? (
        <div ref={tableRef}>
          <Table
            rowKey="id"
            columns={fields}
            dataSource={data.content}
            className={styles.repairOrdersTable}
            rowClassName={styles.repairOrdersTable__row}
            scroll={tableConfig}
            pagination={false}
          />
          <Pagination
            pagination={{
              page: plannerStore.setMonitorListPageSize.page,
              size: plannerStore.setMonitorListPageSize.size,
            }}
            total={data.totalElements}
            setPagination={plannerStore.setMonitorPageSettings}
          />
          <MonitorFilters
            form={form}
            visible={isVisibleMonitorFilters}
            handleOk={getFilteredRoutes}
            handleReset={handleReset}
          />
        </div>
      ) : (
        <SpinWrapped />
      )}
      <RouteCancelModal
        form={form}
        routeId={routeId}
        reason={reason}
        setReason={setReason}
        onCancel={handleCancel}
        visible={modalVisible}
        textAreaVisible={textAreaVisible}
        setTextAreaVisible={setTextAreaVisible}
        setModalVisible={setModalVisible}
      />
    </React.Suspense>
  );
};

export default observer(CargoRoutesTable);
