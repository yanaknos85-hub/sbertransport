import { useTranslation } from 'i18n';
import React, {
  Dispatch, FC, SetStateAction, useEffect, useMemo,
  useState, useLayoutEffect
} from 'react';
import { observer } from 'mobx-react';

import {
  DateRange,
  DateRangeISO
} from 'stores/PublicRegistry/models/PublicRegistry.interface';
import {
  PersonalRegistryFilters,
  PersonalSearchQuery,
  PersonalSearchResponse,
  TripRequestReport
} from 'stores/PersonalSearch/PersonalSearch.interface';
import { usePersonalSearchDeffered, usePersonalSearchExecutorDeffered } from 'api/personal-search';
import { Table } from 'modules/Registry/components/Table/Table';
import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import { useSettingsContext } from 'stores/SettingsContext';
import { DefaultSorting } from 'modules/Registry/components/DefaultSorting/DefaultSorting';
import { TableSettingsContainer } from 'modules/Registry/components/TableSettingsContainer/TableSettingsContainer';
import { useJournalData } from '../hooks/useJournalData';
import { useSortingSettings } from '../hooks/useSortingSettings';
import { useRowSelection } from '../hooks/useRowSelection';
import { ExportXLSModal } from 'modules/Registry/components/Modals/ExportXLSModal/ExportXLSModal';
import { useTable } from '../hooks/useTable';
import { useColumnVisibilitySettings } from '../hooks/useColumnVisibilitySettings';
import { processSearchSubmitJson, processRequestParamsOnXlsDownloadPersonalRegistry } from '../utils';
import { RequestBodyPersonalParams } from '../types/types';
import { DisplayPeriod } from 'modules/Registry/components/DisplayPeriod/DisplayPeriod';
import { ModalFilter } from './ModalFilter/ModalFilter';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { StoreNames } from 'stores';
import { ColumnVisibilitySettings } from 'modules/Registry/components/ColumnVisibilitySettings/ColumnVisibilitySettings';
import { PersonalRegistryColumnProps, useColumns } from '../hooks/useColumns';

export const PersonalRegistryTable: FC<{
  filters: PersonalRegistryFilters;
  setFilterParams: Dispatch<SetStateAction<PersonalRegistryFilters>>;
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
  const { t } = useTranslation();
  const [responseData, setResponseData] = useState<PersonalSearchResponse>();
  const [searchPersonalRegister, { isLoading }] = usePersonalSearchDeffered(organizationId);
  const [
    searchPersonalRegisterExec, { isLoading: isLoadingExecutor },
  ] = usePersonalSearchExecutorDeffered(executorGroupId);
  const [filter, setFilter] = useState(filters);
  const [orgId, setOrgId] = useState(organizationId);
  const [isOrg, setIsOrg] = useState(isOrganization);
  const [execId, setExecId] = useState(executorGroupId);
  const { dataSource } = useJournalData(responseData ? responseData?.content as TripRequestReport[] : []);
  const [isStatusChangeActive, setStatusChangeActive] = useState(false);
  const [hashTariffs, setHashTariffs] = useState<Record<string, string>>({});
  // eslint-disable-next-line @typescript-eslint/no-explicit-any

  const { sortSettings } = useSettingsContext().Personal;
  const { [StoreNames.registryStore]: register } = useAppStoreContext();

  const period = useMemo(
    () => [
      filters.orderPaymentFormationStartRange,
      filters.creationDate,
    ].filter(Boolean) as (DateRange | DateRangeISO)[],
    [filters.orderPaymentFormationStartRange, filters.creationDate]
  );

  const onFinish = (data: PersonalSearchQuery) => setFilterParams(data as PersonalRegistryFilters);

  // todo check role === Сотрудник транспортного подразделения
  const isOrgStructureRole = true;

  const {
    userSettings,
    userColumnVisibilitySettings,
    setUserColumnVisibilitySettings,
    handleSaveColumnVisibilitySettings,
  } = useColumnVisibilitySettings(userId!);

  const {
    userSortSettings, onDefaultSortClick, applySorter,
  } = useSortingSettings(responseData ? responseData?.pageable.pageNumber : 0, setFilterParams);

  const {
    setSelectedTripIds,
    rowSelection,
    selectedRowData,
    savePaymentStatuses,
    isSavingStatuses,
  } = useRowSelection(dataSource);

  useLayoutEffect(() => {
    const filtersModal = processSearchSubmitJson(filters as PersonalSearchQuery, sortSettings());

    if (isOrganization) {
      // @ts-ignore
      searchPersonalRegister(filtersModal).then(data => setResponseData(data));
    } else if (executorGroupId?.length) {
      // @ts-ignore
      searchPersonalRegisterExec(filtersModal).then(data => setResponseData(data));
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
      const filtersModal = processSearchSubmitJson(filters as PersonalSearchQuery, sortSettings());

      if (isOrganization) {
        // @ts-ignore
        searchPersonalRegister(filtersModal).then(data => setResponseData(data));
      } else if (executorGroupId?.length) {
        // @ts-ignore
        searchPersonalRegisterExec(filtersModal).then(data => setResponseData(data));
      } else {
        setResponseData(undefined);
      }
      setFilter(filters);
      setOrgId(organizationId);
      setIsOrg(isOrganization);
      setExecId(executorGroupId);
    }
  }, [filters, organizationId, isOrganization, executorGroupId]);

  useEffect(() => {
    setUserColumnVisibilitySettings(userSettings);
  }, [userSettings, setUserColumnVisibilitySettings]);

  useEffect(() => {
    if (responseData && responseData.content && isStatusChangeActive) {
      const initIds = dataSource.reduce((acc: string[], { requestStatusVisible, id }) => {
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
      const initIds = dataSource.reduce((acc: string[], { requestStatusVisible, id }) => {
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

  const transportType = 'personal';

  return (
    <>
      <ExportXLSModal<PersonalRegistryFilters, Partial<RequestBodyPersonalParams>>
        transportType={transportType}
        filterParams={processSearchSubmitJson(filters as PersonalSearchQuery)}
        mimeType={XLSX_MIME_TYPE}
        onRequestParamsOnXlsDownload={processRequestParamsOnXlsDownloadPersonalRegistry}
      />
      {responseData
      && (
      <ModalFilter
        onFinish={onFinish}
        isOrgStructureRole={isOrgStructureRole}
        transportType={transportType}
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
          transportType="personal"
        />
      </TableSettingsContainer>

      <Table
        isFetching={isOrganization ? isLoading : isLoadingExecutor}
        rowSelection={isStatusChangeActive ? { ...rowSelection } : undefined}
        pagination={{
          total: responseData?.totalElements,
          current: responseData ? responseData.pageable.pageNumber + 1 : 1,
          pageSize: filters?.pageSetting?.size,
        }}
        onChange={onTableChange}
        columns={tableColumns as PersonalRegistryColumnProps[]}
        dataSource={dataSource}
        rowKey="id"
      />
    </>
  );
});
