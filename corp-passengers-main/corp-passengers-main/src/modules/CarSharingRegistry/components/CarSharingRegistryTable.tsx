import React, { useEffect, useMemo, useState } from 'react';
import type { FC } from 'react';
import { observer } from 'mobx-react';
import { ColumnsType } from 'antd/lib/table';

import { useCarSharingReportDeffered, useCarSharingReportExecutorDeffered } from 'api/car-sharing-report';
import { useProfile } from 'api/profile';
import {
  CarSharingReportFilters, DateRange, DateRangeISO, CarSharingSearchResponse, CarSharingSearchQuery
} from 'stores/CarSharingTrip/CarSharingTrip.interface';

import {
  useTable, useSortingSettings, useTableData, useColumns
} from '../hooks';
import { useRegistryFilters } from 'modules/Registry/RegistryFilterContext';
import { Table } from 'modules/Registry/components/Table/Table';
import { PageOptions, RequestBodyCarSharingParams, TableRecordCarSharing } from '../types';

import { TableSettingsContainer } from 'modules/Registry/components/TableSettingsContainer/TableSettingsContainer';
import { DefaultSorting } from 'modules/Registry/components/DefaultSorting/DefaultSorting';
import { ExportXLSModal } from 'modules/Registry/components/Modals/ExportXLSModal/ExportXLSModal';
import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import { processRequestParamsOnXlsDownloadCarSharing, processSearchSubmitJson } from '../utils';
import { DisplayPeriod } from 'modules/Registry/components/DisplayPeriod/DisplayPeriod';
import { ColumnVisibilitySettings } from 'modules/Registry/components/ColumnVisibilitySettings/ColumnVisibilitySettings';
import { useColumnVisibilitySettings } from '../hooks/useColumnVisibilitySettings';

interface CarSharingRegistryTable {
  pageOptions: PageOptions;
  userId: string | undefined;
}

export const CarSharingRegistryTable: FC<CarSharingRegistryTable> = observer(({
  pageOptions, userId,
}) => {
  const {
    organizationId, isOrganization, executorGroupId,
  } = useProfile().data;
  const {
    sortSetting, setSortingSetting, deleteSortSettings, handleDefaultSort,
  } = useSortingSettings();
  const { filterValues: filters } = useRegistryFilters<CarSharingReportFilters>();
  const {
    dataSource, setSearchResult, totalElements, searchResult,
  } = useTableData();
  const [data, setData] = useState<CarSharingSearchResponse>();
  const { pageSetting, onPaginationChange } = pageOptions;
  const [prevPageSett, setPrevPageSett] = useState(pageSetting);
  const [prevSortSett, setPrevSortSett] = useState(sortSetting);
  const [prevFilter, setPrevFilter] = useState(filters);
  const [orgId, setOrgId] = useState(organizationId);
  const [isOrg, setIsOrg] = useState(isOrganization);
  const [execId, setExecId] = useState(executorGroupId);

  // @ts-ignore
  const [searchCarSharingReport, { isLoading }] = useCarSharingReportDeffered(organizationId);
  // @ts-ignore
  const [
    searchCarSharingReportExecutor,
    { isLoading: isLoadingExecutor },
  ] = useCarSharingReportExecutorDeffered(executorGroupId);

  const period = useMemo(
    () => [filters.desiredDate, filters.creationDate].filter(Boolean) as (DateRange | DateRangeISO)[],
    [filters.desiredDate, filters.creationDate]
  );

  useEffect(() => {
    if (data) {
      setSearchResult(data);
    }
  }, [data, setSearchResult]);

  useEffect(() => {
    const filtersModal = processSearchSubmitJson(filters as CarSharingSearchQuery);
    if (isOrganization) {
      searchCarSharingReport({
        ...filtersModal,
        sortSetting,
        pageSetting,
        // @ts-ignore
      }).then(data => setData(data));
    } else if (executorGroupId?.length) {
      searchCarSharingReportExecutor({
        ...filtersModal,
        sortSetting,
        pageSetting,
        // @ts-ignore
      }).then(data => setData(data));
    }
  }, []);

  useEffect(() => {
    if (JSON.stringify(pageSetting) !== JSON.stringify(prevPageSett)
      || JSON.stringify(sortSetting) !== JSON.stringify(prevSortSett)
      || JSON.stringify(filters) !== JSON.stringify(prevFilter)
      || isOrganization !== isOrg
      || organizationId !== orgId
      || JSON.stringify(executorGroupId) !== JSON.stringify(execId)
    ) {
      const filtersModal = processSearchSubmitJson(filters as CarSharingSearchQuery);

      if (isOrganization) {
        searchCarSharingReport({
          ...filtersModal,
          sortSetting,
          pageSetting,
          // @ts-ignore
        }).then(data => setData(data));
      } else if (executorGroupId?.length) {
        searchCarSharingReportExecutor({
          ...filtersModal,
          sortSetting,
          pageSetting,
          // @ts-ignore
        }).then(data => setData(data));
      } else {
        setData(undefined);
      }
      setPrevPageSett(pageSetting);
      setPrevSortSett(sortSetting);
      setPrevFilter(filters);
      setOrgId(organizationId);
      setExecId(executorGroupId);
      setIsOrg(isOrganization);
    }
  }, [filters, pageSetting, sortSetting, isOrganization, organizationId, executorGroupId]);

  const filterParams = useMemo(() => ({
    ...filters, sortSetting, pageSetting,
  }), [filters, sortSetting, pageSetting]);

  const { onTableMetaChange, tableColumns } = useTable(
    setSortingSetting,
    deleteSortSettings,
    sortSetting,
    userId,
    filterParams
  );

  const {
    handleSaveColumnVisibilitySettings,
  } = useColumnVisibilitySettings(userId!);

  return (
    <>
      {/* @ts-ignore */}
      <ExportXLSModal<CarSharingReportFilters, RequestBodyCarSharingParams>
        transportType="carsharing"
        filterParams={processSearchSubmitJson(filters as CarSharingSearchQuery)}
        mimeType={XLSX_MIME_TYPE}
        onRequestParamsOnXlsDownload={processRequestParamsOnXlsDownloadCarSharing}
      />

      <TableSettingsContainer>
        <DefaultSorting onClick={handleDefaultSort} />
        <DisplayPeriod period={period} />
        <ColumnVisibilitySettings
          setting={tableColumns}
          saveSettingChange={handleSaveColumnVisibilitySettings as (key?: Record<string, boolean | undefined>) => void}
          defaultColumns={useColumns(sortSetting)}
          transportType="carsharing"
        />
      </TableSettingsContainer>

      <Table
        isFetching={isOrganization ? isLoading : isLoadingExecutor}
        columns={tableColumns as ColumnsType<TableRecordCarSharing>}
        dataSource={dataSource}
        rowKey="id"
        onChange={onTableMetaChange}
        pagination={{
          total: totalElements,
          current: searchResult ? searchResult.pageable.pageNumber + 1 : 1,
          onChange: onPaginationChange,
          pageSize: pageSetting.size,
        }}
      />
    </>
  );
});
