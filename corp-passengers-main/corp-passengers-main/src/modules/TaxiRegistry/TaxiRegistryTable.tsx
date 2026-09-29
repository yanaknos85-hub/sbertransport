import React, { useEffect, useMemo, useState } from 'react';
import type { FC, MutableRefObject } from 'react';
import { observer } from 'mobx-react';
import { createPortal } from 'react-dom';

import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import { useProfile } from 'api/profile';
import {
  RegisterSearchQuery, RegisterSearchResponse, useSearchTaxiRegisterDeffered, useSearchTaxiRegisterExecutorDeffered
} from 'api/register-search';
import { FilterValues, PageOptions, RequestBodyParamsTaxi } from './types/types';
import { processTaxiRequestBodyOnXlsDownload } from './utils/data';
import { ExportXLSModal } from 'modules/Registry/components/Modals/ExportXLSModal/ExportXLSModal';
import { useSortingSettings } from './hooks/useSortingSettings';
import { useUserAttributes } from './hooks/useUserAttributes';
import { useTable } from './hooks/useTable';
import { useTableData } from './hooks/useTableData';
import { Table } from '../Registry/components/Table/Table';
import { TableSettingsContainer } from '../Registry/components/TableSettingsContainer/TableSettingsContainer';
import { DefaultSorting } from '../Registry/components/DefaultSorting/DefaultSorting';
import { DateRange } from 'stores/CarSharingTrip/CarSharingTrip.interface';
import { DateRangeISO } from 'stores/FleetManagment/General.interface';
import { DisplayPeriod } from '../Registry/components/DisplayPeriod/DisplayPeriod';
import { useColumnVisibilitySettings } from './hooks/useColumnVisibilitySettings';
import { ColumnVisibilitySettings } from 'modules/Registry/components/ColumnVisibilitySettings/ColumnVisibilitySettings';
import { TaxiRegistryColumnProps, useColumns } from './hooks/useColumns';

export const TaxiRegistryTable: FC<{
  filterValues: FilterValues;
  pageOptions: PageOptions;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  buttonsRef?: MutableRefObject<any>;
}> = observer(({
  filterValues, pageOptions, buttonsRef,
}) => {
  const [data, setData] = useState<RegisterSearchResponse>();
  const {
    organizationId, userId, isOrganization, executorGroupId,
  } = useProfile().data;

  const {
    sortSetting, setSortingSetting, deleteSortSettings, handleDefaultSort,
  } = useSortingSettings();
  const { pageSetting, onPaginationChange } = pageOptions;
  const [prevPageSett, setPrevPageSett] = useState(pageSetting);
  const [prevSortSett, setPrevSortSett] = useState(sortSetting);
  const [prevFilter, setPrevFilter] = useState(filterValues);
  const [orgId, setOrgId] = useState(organizationId);
  const [isOrg, setIsOrg] = useState(isOrganization);
  const [execId, setExecId] = useState(executorGroupId);
  // @ts-ignore
  const [searchTaxiRegister, { isLoading }] = useSearchTaxiRegisterDeffered(organizationId);
  // @ts-ignore
  const [
    searchTaxiRegisterExecutor,
    { isLoading: isLoadingExecutor },
  ] = useSearchTaxiRegisterExecutorDeffered(executorGroupId);
  const {
    usersAttributes, setSettingAttributes,
  } = useUserAttributes();

  const {
    // defaultColumns,
    userSettings,
    // isSaving,
    setUserColumnVisibilitySettings,
    handleSaveColumnVisibilitySettings,
  } = useColumnVisibilitySettings(userId!);

  const { onTableMetaChange, columnsRegistryTaxi } = useTable(
    setSortingSetting,
    deleteSortSettings,
    sortSetting,
    userId
  );

  const {
    dataSource, setSearchResult, totalElements, searchResult,
  } = useTableData();

  useEffect(() => {
    setSettingAttributes(usersAttributes);
  }, [usersAttributes, setSettingAttributes]);

  useEffect(() => {
    if (isOrganization) {
      searchTaxiRegister({
        ...filterValues,
        sortSetting,
        pageSetting,
        // @ts-ignore
      }).then(data => setData(data));
    } else if (executorGroupId?.length) {
      searchTaxiRegisterExecutor({
        ...filterValues,
        sortSetting,
        pageSetting,
        // @ts-ignore
      }).then(data => setData(data));
    }
  }, []);

  useEffect(() => {
    if (JSON.stringify(pageSetting) !== JSON.stringify(prevPageSett)
      || JSON.stringify(sortSetting) !== JSON.stringify(prevSortSett)
      || JSON.stringify(filterValues) !== JSON.stringify(prevFilter)
      || isOrganization !== isOrg
      || organizationId !== orgId
      || JSON.stringify(executorGroupId) !== JSON.stringify(execId)
    ) {
      if (isOrganization) {
        searchTaxiRegister({
          ...filterValues,
          sortSetting,
          pageSetting,
          // @ts-ignore
        }).then(data => setData(data));
      } else if (executorGroupId?.length) {
        searchTaxiRegisterExecutor({
          ...filterValues,
          sortSetting,
          pageSetting,
          // @ts-ignore
        }).then(data => setData(data));
      } else {
        setData(undefined);
      }
      setPrevPageSett(pageSetting);
      setPrevSortSett(sortSetting);
      setPrevFilter(filterValues);
      setOrgId(organizationId);
      setExecId(executorGroupId);
      setIsOrg(isOrganization);
    }
  }, [filterValues, pageSetting, sortSetting, isOrganization, organizationId, executorGroupId]);

  useEffect(() => {
    if (data) {
      setSearchResult(data);
    }
  }, [data, setSearchResult]);

  useEffect(() => {
    setUserColumnVisibilitySettings(userSettings);
  }, [userSettings, setUserColumnVisibilitySettings]);

  const period = useMemo(
    () => [filterValues.desiredDateRange, filterValues.creationDate].filter(Boolean) as (DateRange | DateRangeISO)[],
    [filterValues.desiredDateRange, filterValues.creationDate]
  );

  const formattedVisibleColumns = useMemo(() => {
    if (!columnsRegistryTaxi) return columnsRegistryTaxi;

    return [...columnsRegistryTaxi].sort(a => a.dataIndex === 'requestIdVisible' || a.dataIndex === 'humanReadableId' ? -1 : 0);
  }, [columnsRegistryTaxi]);

  return (
    <>
      {buttonsRef
      && buttonsRef.current
      && createPortal(
        // @ts-ignore
        <ExportXLSModal<RegisterSearchQuery, RequestBodyParamsTaxi>
          transportType="taxi"
          filterParams={filterValues}
          mimeType={XLSX_MIME_TYPE}
          onRequestParamsOnXlsDownload={processTaxiRequestBodyOnXlsDownload}
        />,
        buttonsRef.current
      )}

      <TableSettingsContainer>
        <DefaultSorting onClick={handleDefaultSort} />
        <DisplayPeriod period={period} />
        <ColumnVisibilitySettings
          setting={columnsRegistryTaxi}
          saveSettingChange={handleSaveColumnVisibilitySettings as (key?: Record<string, boolean | undefined>) => void}
          defaultColumns={useColumns(sortSetting)}
          transportType="taxi"
        />
      </TableSettingsContainer>

      <Table
        isFetching={isOrganization ? isLoading : isLoadingExecutor}
        dataSource={dataSource}
        columns={formattedVisibleColumns as TaxiRegistryColumnProps[]}
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
