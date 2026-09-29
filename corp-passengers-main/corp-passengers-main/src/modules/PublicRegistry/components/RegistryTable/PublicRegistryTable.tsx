import React, {
  Dispatch, FC, SetStateAction, useEffect, useLayoutEffect, useState, useMemo
} from 'react';
import { useTranslation } from 'i18n';
import { observer } from 'mobx-react';

import { DefaultSorting } from 'modules/Registry/components/DefaultSorting/DefaultSorting';
import { Table } from 'modules/Registry/components/Table/Table';
import { TableSettingsContainer } from 'modules/Registry/components/TableSettingsContainer/TableSettingsContainer';
import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import {
  DateRange,
  DateRangeISO,
  PublicRegistryFilters
} from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { useSettingsContext } from 'stores/SettingsContext';
import { useRegistryJournalDataDeffered, useRegistryJournalDataExecutorDeffered } from 'api/public-register-search';

import { useColumnVisibilitySettings } from '../../hooks/useColumnVisibilitySettings';
import { useJournalData } from '../../hooks/useJournalData';
import { useRowSelection } from '../../hooks/useRowSelection';
import { useSortingSettings } from '../../hooks/useSortingSettings';
import { useTable } from '../../hooks/useTable';
import { Filters, IResponseData, RequestBodyPublicParams } from '../../types/types';
import { processRequestParamsOnXlsDownloadPublicRegistry, processSearchSubmitJson } from '../../utils/utils';
import { ExportXLSModal } from 'modules/Registry/components/Modals/ExportXLSModal/ExportXLSModal';
import { DisplayPeriod } from 'modules/Registry/components/DisplayPeriod/DisplayPeriod';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { ModalFilter } from '../ModalFilter/ModalFilter';
import { TripRegistryColumnProps, useColumns } from 'modules/PublicRegistry/hooks/useColumns';
import { ColumnVisibilitySettings } from 'modules/Registry/components/ColumnVisibilitySettings/ColumnVisibilitySettings';

const journalStyles = {
  tableWrapper: {
    margin: '0 auto',
    width: '100%',
  },
  table: {
    overflow: 'scroll',
    whiteSpace: 'break-spaces' as const,
  },
  tablePadding: {
    padding: '0px 124px',
  },
  a: {
    fontSize: 'calc(var(--font-size-base) * 1.3)',
  },
  tableScroll: {
    x: 400,
  },
  titleDiv: {
    margin: '0 auto',
    padding: '0 100px',
    width: '100%',
  },
};

export const PublicRegistryTable: FC<{
  filters: PublicRegistryFilters;
  setFilterParams: Dispatch<SetStateAction<PublicRegistryFilters>>;
  userId: string | undefined;
  organizationId: string | null | undefined;
  isOrganization: boolean | undefined;
  executorGroupId: string[] | undefined;
}> = observer(({
  filters,
  setFilterParams,
  userId,
  organizationId,
  isOrganization,
  executorGroupId,
}) => {
  const [responseData, setResponseData] = useState<IResponseData>();
  const [fetchRegistryJournal, { isLoading }] = useRegistryJournalDataDeffered(organizationId);
  const [
    fetchRegistryJournalExec,
    { isLoading: isLoadingExecutor },
  ] = useRegistryJournalDataExecutorDeffered(executorGroupId);
  const [filter, setFilter] = useState(filters);
  const [orgId, setOrgId] = useState(organizationId);
  const [isOrg, setIsOrg] = useState(isOrganization);
  const [execId, setExecId] = useState(executorGroupId);
  const { t } = useTranslation();
  const [isStatusChangeActive, setStatusChangeActive] = useState(false);
  const [hashTariffs, setHashTariffs] = useState<Record<string, string>>({});

  useLayoutEffect(() => {
    const filtersModal = processSearchSubmitJson(filters as Filters, sortSettings());

    if (isOrganization) {
      // @ts-ignore
      fetchRegistryJournal(filtersModal).then(data => setResponseData(data));
    } else if (executorGroupId?.length) {
      // @ts-ignore
      fetchRegistryJournalExec(filtersModal).then(data => setResponseData(data));
    }
    setFilter(filters);
    setOrgId(organizationId);
    setIsOrg(isOrganization);
    setExecId(executorGroupId);
  }, []);

  useEffect(() => {
    if (JSON.stringify(filters) !== JSON.stringify(filter)
      || organizationId !== orgId
      || isOrganization !== isOrg
      || JSON.stringify(executorGroupId) !== JSON.stringify(execId)) {
      const filtersModal = processSearchSubmitJson(filters as Filters, sortSettings());

      if (isOrganization) {
        // @ts-ignore
        fetchRegistryJournal(filtersModal).then(data => setResponseData(data));
      } else if (executorGroupId?.length) {
        // @ts-ignore
        fetchRegistryJournalExec(filtersModal).then(data => setResponseData(data));
      } else {
        setResponseData(undefined);
      }
      setFilter(filters);
      setOrgId(organizationId);
      setIsOrg(isOrganization);
      setExecId(executorGroupId);
    }
  }, [filters, organizationId, isOrganization, executorGroupId]);

  const { processedJournalData } = useJournalData(responseData ? responseData?.responseData?.content : []);
  const isOrgStructureRole = true;
  const { [StoreNames.registryStore]: register } = useAppStoreContext();

  const period = useMemo(
    () => [
      filters.orderPaymentFormationStartDate,
      filters.creationDate,
    ].filter(Boolean) as (DateRange | DateRangeISO)[],
    [filters.orderPaymentFormationStartDate, filters.creationDate]
  );

  const {
    userSettings,
    userColumnVisibilitySettings,
    setUserColumnVisibilitySettings,
    handleSaveColumnVisibilitySettings,
  } = useColumnVisibilitySettings(userId!);

  const {
    userSortSettings, onDefaultSortClick, applySorter,
  } = useSortingSettings(
    responseData ? responseData?.responseData.pageable.pageNumber : 0,
    setFilterParams
  );

  const {
    setSelectedTripIds,
    rowSelection,
    selectedRowData,
    savePaymentStatuses,
    isSavingStatuses,
  } = useRowSelection(processedJournalData);

  const {
    onTableChange, tableColumns,
  }
    = useTable(
      filters,
      setFilterParams,
      userSortSettings,
      isStatusChangeActive,
      applySorter,
      userId
    );

  useEffect(() => {
    setUserColumnVisibilitySettings(userSettings);
  }, [userSettings, setUserColumnVisibilitySettings]);

  useEffect(() => {
    if (responseData && responseData.responseData.content && isStatusChangeActive) {
      const initIds = processedJournalData.reduce((acc: string[], { requestStatusVisible, id }) => {
        if (requestStatusVisible === t.Forms.registryFilterFields.changeableStatus) {
          acc.push(id);
        }
        return acc;
      }, []);
      const initData = initIds.map(id => ({ requestId: id, payed: true }));
      setSelectedTripIds(initIds);
      register.setSelectedRowData(initData);
    }
  }, [responseData, setSelectedTripIds]);

  useEffect(() => {
    !isStatusChangeActive && register.clearSelectedRowData();
  }, [isStatusChangeActive]);

  const handleChangeStatusClick = () => {
    if (!isStatusChangeActive) {
      const initIds = processedJournalData.reduce((acc: string[], { requestStatusVisible, id }) => {
        if (requestStatusVisible === t.Forms.registryFilterFields.changeableStatus) {
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

  const { sortSettings } = useSettingsContext().Public;

  const onFinish = (data: Filters) => setFilterParams(data as PublicRegistryFilters);

  return (
    <div style={journalStyles.tableWrapper}>
      <ExportXLSModal<PublicRegistryFilters, Partial<RequestBodyPublicParams>>
        transportType="public"
        filterParams={processSearchSubmitJson(filters as Filters)}
        mimeType={XLSX_MIME_TYPE}
        onRequestParamsOnXlsDownload={processRequestParamsOnXlsDownloadPublicRegistry}
      />
      {responseData
      && (
      <ModalFilter
        onFinish={onFinish}
        isOrgStructureRole={isOrgStructureRole}
        filters={filters}
        userColumnVisibilitySettings={userColumnVisibilitySettings}
        isStatusChangeActive={isStatusChangeActive}
        setStatusChangeActive={setStatusChangeActive}
        selectedRowData={selectedRowData}
        savePaymentStatuses={savePaymentStatuses}
        isSavingStatuses={isSavingStatuses}
        handleChangeStatusClick={handleChangeStatusClick}
        responseData={responseData}
        hashTariffs={hashTariffs}
        setHashTariffs={setHashTariffs}
      />
      )}

      <TableSettingsContainer>
        <DefaultSorting onClick={onDefaultSortClick} disabled={isStatusChangeActive} />
        <DisplayPeriod period={period} />
        <ColumnVisibilitySettings
          setting={tableColumns}
          saveSettingChange={handleSaveColumnVisibilitySettings as (key?: Record<string, boolean | undefined>) => void}
          defaultColumns={useColumns(false, userSortSettings)}
          transportType="public"
        />
      </TableSettingsContainer>

      <Table
        rowSelection={isStatusChangeActive ? { ...rowSelection } : undefined}
        pagination={{
          total: responseData?.responseData.totalElements,
          current: responseData ? responseData.responseData.pageable.pageNumber + 1 : 1,
          pageSize: filters?.pageSetting?.size,
        }}
        onChange={onTableChange}
        columns={tableColumns as TripRegistryColumnProps[]}
        dataSource={processedJournalData}
        style={journalStyles.table}
        scroll={journalStyles.tableScroll}
        rowKey="id"
        isFetching={isOrganization ? isLoading : isLoadingExecutor}
      />
    </div>
  );
});
