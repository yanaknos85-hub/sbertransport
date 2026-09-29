import React from 'react';
import type { FC } from 'react';
import { Table } from 'antd';

import { useFraudMonitoringSearch } from 'api/fraud-monitoring';

import ErrorBoundary from 'shared/components/ErrorBoundary';
import { Pagination } from 'shared/components/PaginationWithPageSelect';

import { SortDirection } from 'constants/constants.app';
import { TableField } from '../../fraudMonitoring.constants';

import { useColumns } from './useColumns';
import { useFraudParams } from './hooks/useFraudParams';

import { ActionSection } from './components/ActionSection/ActionSection';

import { getRowClassName } from './utils';

import styles from './styles.module.scss';

const FraudTable: FC = () => {
  const {
    filters, set, resetFilters, filtersCount, pagination, setPagination, requestFilters,
  } = useFraudParams();
  const tableFields = useColumns();

  const { data, isLoading } = useFraudMonitoringSearch(
    {
      filter: requestFilters,
      ...pagination,
      sort: TableField.humanReadableId,
      direction: SortDirection.ASC,
    },
    { suspense: false }
  );

  return (
    <ErrorBoundary>
      <div className={styles.container}>
        <ActionSection
          setFilters={set}
          resetFilters={resetFilters}
          filtersCount={filtersCount}
          filters={filters}
        />
        <Table
          rowKey={TableField.humanReadableId}
          loading={isLoading}
          columns={tableFields}
          dataSource={data?.content}
          className={styles.table}
          rowClassName={getRowClassName}
          scroll={{ x: '100%' }}
          pagination={false}
        />
      </div>
      <Pagination
        pagination={pagination}
        total={data?.page.total}
        setPagination={setPagination}
      />
    </ErrorBoundary>
  );
};

export default FraudTable;
