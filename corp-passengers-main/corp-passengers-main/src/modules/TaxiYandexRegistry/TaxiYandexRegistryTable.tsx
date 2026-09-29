import React, { useEffect, useMemo, useState } from 'react';
import type {
  FC,
  MutableRefObject,
  RefObject,
  SetStateAction,
  Dispatch
} from 'react';
import { observer } from 'mobx-react';
import { createPortal } from 'react-dom';
import type { FormInstance } from 'antd';

import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import { useAPIQueryCache } from 'api';
import type { YandexTaxiReportResponse } from 'api/yandexTaxiRegistry/yandex-taxi-registry.types';
import type { PageOptions, TaxiYandexRegistryFilter, RequestBodyParamsYandexTaxi } from './types/types';
import { processYandexTaxiRequestQueryOnXlsDownload } from './utils/data';
import { ExportXLSModal } from 'modules/Registry/components/Modals/ExportXLSModal/ExportXLSModal';
import { useSortingSettings } from './hooks/useSortingSettings';
import { Table } from '../Registry/components/Table/Table';
import { TableSettingsContainer } from '../Registry/components/TableSettingsContainer/TableSettingsContainer';
import { DefaultSorting } from '../Registry/components/DefaultSorting/DefaultSorting';
import { DisplayPeriod } from '../Registry/components/DisplayPeriod/DisplayPeriod';
import { useColumns } from './hooks/useColumns';
import { useYandexTaxiRegistry } from 'api/yandexTaxiRegistry/yandex-taxi-registry.api';
import { useTransformedData } from './hooks/useTransformedData';
import { useRowSelection } from './hooks/useRowSelection';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { RegistryUpload } from './components/RegistryUpload/RegistryUpload';
import { useProfile } from 'api/profile';
import { useTranslation } from 'i18n';

import { useColumnVisibilitySettings } from 'modules/TaxiRegistry/hooks/useColumnVisibilitySettings';
import { filter2searchQuery } from './utils/data';
import { useDisplayPeriod } from './hooks/useDisplayPeriod';

export const TaxiYandexRegistryTable: FC<{
  filters: TaxiYandexRegistryFilter;
  pageOptions: PageOptions;
  isStatusChangeActive: boolean;
  setStatusChangeActive: Dispatch<SetStateAction<boolean>>;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  buttonsRef?: MutableRefObject<any>;
  actionButtonsRef?: RefObject<HTMLDivElement | null>;
  onSubmit: (values: TaxiYandexRegistryFilter) => void;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  form?: FormInstance<any>;
}> = observer(({
  filters,
  pageOptions,
  buttonsRef,
  isStatusChangeActive,
  setStatusChangeActive,
  actionButtonsRef,
  onSubmit,
  form,
}) => {
  const apiCache = useAPIQueryCache();
  const { t } = useTranslation();
  const {
    [StoreNames.registryStore]: register,
  } = useAppStoreContext();
  const {
    isOrganization, userId,
  } = useProfile().data;

  const {
    userColumnVisibilitySettings,
  } = useColumnVisibilitySettings(userId!);

  const [actualData, setActualData] = useState<YandexTaxiReportResponse | null>(null);

  // Преобразует объект фильтров в объект запроса
  const filterValues = useMemo(() => filter2searchQuery(filters), [filters]);

  const { sortSetting, handleDefaultSort } = useSortingSettings();
  const { pageSetting, onPaginationChange } = pageOptions;

  const query = {
    filter: filterValues,
    page: pageSetting?.page || 0,
    size: pageSetting?.size || 20,
    sort: sortSetting.property,
    direction: sortSetting.directionAsc ? 'ASC' : 'DESC',
  };

  const {
    data: response,
    isLoading,
    refetch,
  } = useYandexTaxiRegistry(query);

  const columns = useColumns(sortSetting);
  const transformedData = useTransformedData(actualData);

  const handlePaymentFinish = async () => {
    if (pageSetting?.page > 0) {
      onPaginationChange(1, 20);
    }

    await apiCache.invalidateQueries(['yandexTaxiReport']);
    const data = await refetch();
    setActualData(data);
  };

  useEffect(() => {
    setActualData(response);
  }, [response]);

  const {
    setSelectedTripIds,
    rowSelection,
    selectedRowData,
    savePaymentStatuses,
    isSavingStatuses,
  } = useRowSelection(transformedData);

  useEffect(() => {
    if (actualData && actualData.content && isStatusChangeActive) {
      const initIds = transformedData.reduce((acc: string[], { requestStatus, id }) => {
        if (requestStatus === t.Forms.registryFilterFields.changeableStatus) {
          acc.push(id);
        }
        return acc;
      }, []);
      const initData = initIds.map(id => ({ requestId: id, payed: true }));
      setSelectedTripIds(initIds);
      register.setSelectedRowData(initData);
    }
  }, [actualData, setSelectedTripIds]);

  const applyFilters = (values: TaxiYandexRegistryFilter) => {
    onSubmit(values);
    if (JSON.stringify(values) === JSON.stringify(filters)) {
      refetch();
    }
  };

  const handleChangeStatusClick = () => {
    if (!isStatusChangeActive) {
      const initIds = transformedData.reduce((acc: string[], { requestStatus, id }) => {
        if (requestStatus === t.Forms.registryFilterFields.changeableStatus) {
          acc.push(id);
        }
        return acc;
      }, []);
      const initData = initIds.map(id => ({ requestId: id, payed: true }));
      setSelectedTripIds(initIds);
      register.setSelectedRowData(initData);
      setStatusChangeActive(true);
    }
  };

  useEffect(() => {
    !isStatusChangeActive && register.clearSelectedRowData();
  }, [isStatusChangeActive]);

  const displayPeriod = useDisplayPeriod(filters);
  return (
    <>
      {buttonsRef
      && buttonsRef.current
      && createPortal(
        // @ts-ignore
        <ExportXLSModal<TaxiYandexRegistryFilter, RequestBodyParamsYandexTaxi>
          transportType="external"
          filterParams={filters}
          mimeType={XLSX_MIME_TYPE}
          // @ts-ignore
          onRequestParamsOnXlsDownload={processYandexTaxiRequestQueryOnXlsDownload}
        />,
        buttonsRef.current
      )}

      {actionButtonsRef && actionButtonsRef.current && createPortal(
        <RegistryUpload
          onFinish={handlePaymentFinish}
          applyFilters={applyFilters}
          isOrgStructureRole={true}
          isOrganization={!!isOrganization}
          isStatusChangeActive={isStatusChangeActive}
          setStatusChangeActive={setStatusChangeActive}
          selectedRowData={selectedRowData}
          handleChangeStatusClick={handleChangeStatusClick}
          isSavingStatuses={isSavingStatuses}
          savePaymentStatuses={savePaymentStatuses}
          responseData={transformedData}
          filters={filters}
          userColumnVisibilitySettings={userColumnVisibilitySettings}
          form={form}
        />, actionButtonsRef.current
      )}

      <TableSettingsContainer>
        <DefaultSorting onClick={handleDefaultSort} disabled={isStatusChangeActive} />
        <DisplayPeriod period={displayPeriod} />
      </TableSettingsContainer>

      <Table
        isFetching={isLoading}
        dataSource={transformedData}
        rowSelection={isStatusChangeActive ? { ...rowSelection } : undefined}
        columns={columns}
        rowKey="id"
        pagination={{
          total: actualData?.page.total || 1,
          current: pageSetting.page + 1,
          onChange: onPaginationChange,
          pageSize: pageSetting.size,
        }}
      />
    </>
  );
});
