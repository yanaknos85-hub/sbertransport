import React, {
  Dispatch, FC, SetStateAction, useMemo
} from 'react';
import { FormInstance } from 'antd';

import { CarSharingReportFilters } from 'stores/CarSharingTrip/CarSharingTrip.interface';

import { useSortingSettings } from '../hooks';
import { useRegistryFilters } from 'modules/Registry/RegistryFilterContext';
import { PageOptions, RequestBodyCarSharingParams } from '../types';

import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import { ExportXLSModal } from 'shared/components/ModalsExportXLSBusinessReports/ExportXLSModal/ExportXLSModal';

import { processRequestParamsOnXlsDownloadCarSharing } from '../utils';

interface CarSharingRegistryTable {
  pageOptions: PageOptions;
  isStatusChangeActive: boolean;
  setStatusChangeActive: Dispatch<SetStateAction<boolean>>;
  isLoading: boolean;
  setIsLoading: React.Dispatch<React.SetStateAction<boolean>>;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  form?: FormInstance<any>;
}

export const CarSharingBusinessReportsTable: FC<CarSharingRegistryTable> = ({
  pageOptions, isLoading, setIsLoading, form,
}) => {
  const {
    sortSetting,
  } = useSortingSettings();
  const { filterValues: filters } = useRegistryFilters<CarSharingReportFilters>();
  const { pageSetting } = pageOptions;

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
        isLoading={isLoading}
        setIsLoading={setIsLoading}
        form={form}
      />
    </>
  );
};
