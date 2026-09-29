import React, {
  Dispatch, FC, SetStateAction, useEffect, useMemo
} from 'react';

import { useCarSharingReport } from 'api/car-sharing-report';
import { useProfile } from 'api/profile';
import { CarSharingReportFilters, DateRange, DateRangeISO } from 'stores/CarSharingTrip/CarSharingTrip.interface';

import { useTable, useSortingSettings, useTableData } from '../hooks';
import { useRegistryFilters } from 'modules/Registry/RegistryFilterContext';
import { Table } from 'modules/Registry/components/Table/Table';
import { PageSetting, RequestBodyCarSharingParams } from '../types';

import { TableSettingsContainer } from 'modules/Registry/components/TableSettingsContainer/TableSettingsContainer';
import { DefaultSorting } from 'modules/Registry/components/DefaultSorting/DefaultSorting';
import { ExportXLSModal } from 'modules/Registry/components/Modals/ExportXLSModal/ExportXLSModal';
import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import { processRequestParamsOnXlsDownloadCarSharing } from '../utils';
import { DisplayPeriod } from 'modules/Registry/components/DisplayPeriod/DisplayPeriod';

interface CarSharingRegistryTable {
  pageSetting: PageSetting;
  onPaginationChange: (page: number, size?: number | undefined) => void;
  isStatusChangeActive: boolean;
  setStatusChangeActive: Dispatch<SetStateAction<boolean>>;
}

export const CarSharingRegistryTable: FC<CarSharingRegistryTable> = ({
  pageSetting,
  onPaginationChange,
}) => {
  const { organizationId } = useProfile().data;
  const {
    sortSetting, setSortingSetting, deleteSortSettings, handleDefaultSort,
  } = useSortingSettings();
  const { filterValues: filters } = useRegistryFilters<CarSharingReportFilters>();
  const {
    dataSource, setSearchResult, totalElements, searchResult,
  } = useTableData();
  const {
    isFetching, refetch, data,
  } = useCarSharingReport(
    {
      ...filters,
      sortSetting,
      pageSetting,
    },
    // @ts-ignore
    organizationId
  );

  const period = useMemo(
    () => [filters.desiredDate, filters.creationDate].filter(Boolean) as (DateRange | DateRangeISO)[],
    [filters.desiredDate, filters.creationDate]
  );

  const { onTableMetaChange, tableColumns } = useTable(setSortingSetting, deleteSortSettings, sortSetting);

  useEffect(() => {
    setSearchResult(data);
  }, [data, setSearchResult]);

  useEffect(() => {
    refetch();
  }, [refetch, organizationId, filters, pageSetting, sortSetting]);

  const filterParams = useMemo(() => ({
    ...filters, sortSetting, pageSetting,
  }), [filters, sortSetting, pageSetting]);

  return (
    <>
      {/* @ts-ignore */}
      <ExportXLSModal<CarSharingReportFilters, RequestBodyCarSharingParams>
        transportType="carsharing"
        filterParams={filterParams}
        mimeType={XLSX_MIME_TYPE}
        onRequestParamsOnXlsDownload={processRequestParamsOnXlsDownloadCarSharing}
      />

      <TableSettingsContainer>
        <DefaultSorting onClick={handleDefaultSort} />
        <DisplayPeriod period={period} />
      </TableSettingsContainer>

      <Table
        sticky
        isFetching={isFetching}
        columns={tableColumns}
        dataSource={dataSource}
        rowKey="id"
        style={{ overflow: 'scroll' }}
        scroll={{ x: 400 }}
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
};
