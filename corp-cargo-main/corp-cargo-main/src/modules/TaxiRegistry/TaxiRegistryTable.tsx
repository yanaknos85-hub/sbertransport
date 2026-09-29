import React, {
  FC, useEffect, memo, MutableRefObject, useMemo
} from 'react';
import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import { useProfile } from 'api/profile';
import { RegisterSearchQuery, useSearchTaxiRegister } from 'api/register-search';
import { createPortal } from 'react-dom';
import { FilterValues, PageOptions, RequestBodyParamsTaxi } from './types/types';

import { ExportXLSModal } from 'modules/Registry/components/Modals/ExportXLSModal/ExportXLSModal';
import { useSortingSettings } from './hooks/useSortingSettings';
import { useUserAttributes } from './hooks/useUserAttributes';
import { useTable } from './hooks/useTable';
import { useTableData } from './hooks/useTableData';
import { Table } from '../Registry/components/Table/Table';
import { TableSettingsContainer } from '../Registry/components/TableSettingsContainer/TableSettingsContainer';
import { DefaultSorting } from '../Registry/components/DefaultSorting/DefaultSorting';
import { ColumnVisibilitySettings } from '../Registry/components/ColumnVisibilitySettings/ColumnVisibilitySettings';
import { processTaxiRequestBodyOnXlsDownload } from './utils/data';
import { DateRange } from 'stores/CarSharingTrip/CarSharingTrip.interface';
import { DateRangeISO } from 'stores/FleetManagment/General.interface';
import { DisplayPeriod } from '../Registry/components/DisplayPeriod/DisplayPeriod';

export const TaxiRegistryTable: FC<{
  filterValues: FilterValues;
  pageOptions: PageOptions;
  buttonsRef?: MutableRefObject<any>;
}> = memo(({
  filterValues, pageOptions, buttonsRef,
}) => {
  const { organizationId } = useProfile().data;
  const {
    sortSetting, setSortingSetting, deleteSortSettings, handleDefaultSort,
  } = useSortingSettings();
  const { pageSetting, onPaginationChange } = pageOptions;
  const {
    data, refetch, isLoading,
  } = useSearchTaxiRegister(
    {
      ...filterValues,
      sortSetting,
      pageSetting,
    },
    { enabled: false },
    organizationId
  );
  const {
    usersAttributes, settingAttributes, setSettingAttributes, defaultColumns, handleSave,
  } = useUserAttributes();

  const { onTableMetaChange, columnsRegistryTaxi } = useTable(
    setSortingSetting,
    deleteSortSettings,
    sortSetting,
    settingAttributes
  );
  const {
    dataSource, setSearchResult, totalElements, searchResult,
  } = useTableData();

  useEffect(() => {
    setSettingAttributes(usersAttributes);
  }, [usersAttributes, setSettingAttributes]);

  useEffect(() => {
    setSearchResult(data);
  }, [data, setSearchResult]);

  useEffect(() => {
    refetch();
  }, [refetch, filterValues, pageSetting, sortSetting]);

  const period = useMemo(
    () => [filterValues.desiredDateRange, filterValues.creationDate].filter(Boolean) as (DateRange | DateRangeISO)[],
    [filterValues.desiredDateRange, filterValues.creationDate]
  );

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
        {/* <ColumnVisibilitySettings
          setting={settingAttributes?.taxiUIVisibility}
          saveSettingChange={handleSave as (key?: Record<string, boolean | undefined>) => void}
          defaultColumns={defaultColumns.taxiUIVisibility}
        /> */}
      </TableSettingsContainer>

      <Table
        sticky
        isFetching={isLoading}
        dataSource={dataSource}
        columns={columnsRegistryTaxi}
        style={{ overflow: 'scroll' }}
        scroll={{ x: 400 }}
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
