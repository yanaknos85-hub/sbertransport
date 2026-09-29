import React, { FC, Suspense, useMemo } from 'react';
import { TableProps } from 'antd';
import { ColumnType, SorterResult } from 'antd/lib/table/interface';
import moment from 'moment';

import { useTranslation } from 'i18n';
import { useProfile } from 'api/profile/profile.api';
import { useTripsReports } from 'api/trips-reports/trips-reports.api';
import { TripsReport } from 'api/trips-reports/trips-reports.types';
import { TRIPS_REPORT, TRIPS_REPORT_SETTINGS, TRIPS_REPORTS } from 'api/trips-reports/trips-reports.constants';

import { SortOrderToDirectionMap } from 'constants/app.constants';
import { TableStyled } from 'components/TableStyled';
import Flex from 'components/Flex/Flex';
import ErrorBoundary from 'components/ErrorBoundary';
import SpinWrapped from 'components/SpinWrapped/SpinWrapped';
import TableSettings from 'components/TableSettings/TableSettings';
import { convertCamelToSnakeCase } from 'utils/convertStringCase';
import { useTableSettings } from 'hooks/useTableSettings';

import { Columns } from './constants/TripsTab.constants';
import Filters from './components/Filters/Filters';
import ExportBtn from './components/ExportBtn/ExportBtn';
import DefaultSort from './components/DefaultSort/DefaultSort';
import { useTripsTabQuery } from './context/TripsTab.queryContext';
import useColumns from './hooks/useColumns';
import { isValidDate } from './utils';

interface ReportsTableProps {
  getOptimizeColumns: (columns: ColumnType<TripsReport>[]) => ColumnType<TripsReport>[];
}

const Table: FC<ReportsTableProps> = ({ getOptimizeColumns }) => {
  const columns = useColumns();
  const {
    query, setPagination, setSort,
  } = useTripsTabQuery();
  const { contractorId } = useProfile().data;
  const { content: trips, totalElements } = useTripsReports({
    contractorId,
    query: {
      ...query,
      startTimeFrom: isValidDate(query.startTimeFrom?.[0])
        ? moment(query.startTimeFrom![0]).startOf('day').utc().format()
        : undefined,
      startTimeTo: isValidDate(query.startTimeFrom?.[1])
        ? moment(query.startTimeFrom![1]).endOf('day').utc().format()
        : undefined,
    },
  }).data;

  const onTableChange: TableProps<TripsReport>['onChange'] = (pagination, filters, sorter) => {
    const {
      columnKey, field, order,
    } = sorter as SorterResult<TripsReport>;

    setSort({
      field: convertCamelToSnakeCase(columnKey as string ?? field!),
      direction: order ? SortOrderToDirectionMap[order] : undefined,
    });
  };

  return (
    <TableStyled
      dataSource={trips}
      columns={getOptimizeColumns(columns)}
      paginationParams={query}
      total={totalElements}
      setPagination={setPagination}
      scroll={{ y: 'auto' }}
      size="small"
      secondStyle
      onChange={onTableChange}
    />
  );
};

const Passenger: FC = () => {
  const { t } = useTranslation();

  const columns = useMemo(
    () => Object.fromEntries(
      Object.values(Columns).map(column => [column, t.Reports[column[0].toUpperCase() + column.slice(1)] ?? column])
    ),
    [t]
  );

  const {
    tableSettings, onSaveTableSettings, getOptimizeColumns,
  } = useTableSettings(columns, TRIPS_REPORT_SETTINGS);

  return (
    <>
      <Flex justifyContent="space-between">
        <Filters />
        <ExportBtn url={TRIPS_REPORT} directoryUrl={TRIPS_REPORTS} />
      </Flex>

      <Flex justifyContent="space-between" alignItems="center">
        <DefaultSort />
        <TableSettings
          columns={columns}
          tableSettings={tableSettings}
          onSaveTableSettings={onSaveTableSettings}
        />
      </Flex>

      <ErrorBoundary>
        <Suspense fallback={<SpinWrapped />}>
          <Table getOptimizeColumns={getOptimizeColumns} />
        </Suspense>
      </ErrorBoundary>
    </>
  );
};

export default Passenger;
