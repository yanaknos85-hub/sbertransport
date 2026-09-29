import { Button } from 'antd';
import { usePersonalSearch } from 'api/personal-search';
import { useProfile } from 'api/profile';
import { useTranslation } from 'i18n';
import React, {
  Dispatch, FC, Fragment, SetStateAction, useEffect, useState, useMemo
} from 'react';
import { TripRequestStatuses } from 'constants/constants.app';
import {
  DateRange,
  DateRangeISO,
  PaymentStateResponse
} from 'stores/PublicRegistry/models/PublicRegistry.interface';
import {
  PersonalRegistryFilters,
  PersonalSearchQuery,
  PersonalUIVisibilityDTO,
  TripRequestReport
} from 'stores/PersonalSearch/PersonalSearch.interface';
import { Table } from 'modules/Registry/components/Table/Table';
import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import { ActionsRegistry } from 'modules/Registry/components/ActionsRegistry/ActionsRegistry';
import { FilterPanel } from 'shared/components/FilterPanel';
import { FormInstance } from 'antd/lib/form';
import { clearButtonActive } from 'utils/reportsUtils';
import { useSettingsContext } from 'stores/SettingsContext';
import { DefaultSorting } from 'modules/Registry/components/DefaultSorting/DefaultSorting';
import { TableSettingsContainer } from 'modules/Registry/components/TableSettingsContainer/TableSettingsContainer';
import { ColumnVisibilitySettings } from 'modules/Registry/components/ColumnVisibilitySettings/ColumnVisibilitySettings';
import { useJournalData } from '../hooks/useJournalData';
import { useSortingSettings } from '../hooks/useSortingSettings';
import { ActiveStatusChangeButtons } from './ActiveStatusChangeButtons/ActiveStatusChangeButtons';
import { useRowSelection } from '../hooks/useRowSelection';
import { ActiveStatusChangeWarning } from './Modal/ActiveStatusChangeWarning/ActiveStatusChangeWarning';
import { CompensationModal } from 'modules/Registry/components/Modals/CompensationModal/CompensationModal';
import { ExportXLSModal } from 'modules/Registry/components/Modals/ExportXLSModal/ExportXLSModal';
import { useTable } from '../hooks/useTable';
import { useColumnVisibilitySettings } from '../hooks/useColumnVisibilitySettings';
import { useFilterFields } from '../hooks/useFilterFields';
import { processSearchSubmitJson, processRequestParamsOnXlsDownloadPersonal } from '../utils';
import { RequestBodyPersonalParams } from '../types/types';
import { DisplayPeriod } from 'modules/Registry/components/DisplayPeriod/DisplayPeriod';
import { initialFormValues } from '../constants/PersonalRegistry.constants';

export const PersonalRegistryTable: FC<{
  filters: PersonalRegistryFilters;
  setFilterParams: Dispatch<SetStateAction<PersonalRegistryFilters | undefined>>;
  isStatusChangeActive: boolean;
  setStatusChangeActive: Dispatch<SetStateAction<boolean>>;
}> = ({
  filters, setFilterParams, isStatusChangeActive, setStatusChangeActive,
}) => {
  const { t } = useTranslation();
  const { userId, organizationId } = useProfile().data;
  const {
    isFetching, refetch, data: responseData,
  } = usePersonalSearch(filters, organizationId);
  const { dataSource } = useJournalData(responseData.content as TripRequestReport[]);

  const [isStatusChangeActiveFilter, setStatusChangeActiveFilter] = useState(true);
  const [form, setForm] = useState<FormInstance<any>>();
  const onClearButtonActivator = () => {
    const values = form?.getFieldsValue(true);

    setStatusChangeActiveFilter(clearButtonActive(values));
  };
  const fields = useFilterFields(onClearButtonActivator);

  const [isVisible, setIsVisible] = useState(false);

  const { sortSettings } = useSettingsContext().Personal;

  const period = useMemo(
    () => [filters.orderPaymentFormationStartRange, filters.creationDate].filter(Boolean) as (DateRange | DateRangeISO)[],
    [filters.orderPaymentFormationStartRange, filters.creationDate]
  );

  const onFinish = (data: PersonalSearchQuery) => setFilterParams(processSearchSubmitJson(data, sortSettings()));

  // todo check role === Сотрудник транспортного подразделения
  const isOrgStructureRole = true;

  const {
    defaultColumns,
    userSettings,
    isSaving,
    userColumnVisibilitySettings,
    setUserColumnVisibilitySettings,
    handleSaveColumnVisibilitySettings,
  } = useColumnVisibilitySettings(userId!);

  const {
    userSortSettings, onDefaultSortClick, applySorter,
  } = useSortingSettings(responseData, setFilterParams);

  const {
    setSelectedTripIds,
    rowSelection,
    selectedRowData,
    setSelectedRowData,
    savePaymentStatuses,
    isSavingStatuses,
  } = useRowSelection(dataSource);

  const {
    onTableChange, tableColumns, isVisibleChangeStatusModal, setVisibleChangeStatusModal, tableChangeParams,
  }
    = useTable(
      filters,
      setFilterParams,
      userSortSettings,
      userColumnVisibilitySettings,
      isStatusChangeActive,
      applySorter
    );

  useEffect(() => {
    refetch();
  }, [filters, refetch, organizationId]);

  useEffect(() => {
    setUserColumnVisibilitySettings(userSettings);
  }, [userSettings, setUserColumnVisibilitySettings]);

  useEffect(() => {
    if (responseData && responseData.content) {
      const initElements = responseData.content.reduce((acc: string[], { status, id }) => {
        if (status === TripRequestStatuses.PAYMENT_AWAITING) {
          acc.push(id);
        }
        return acc;
      }, []);
      setSelectedTripIds(initElements);
    }
  }, [responseData, setSelectedTripIds]);

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
      setSelectedRowData(initData);
      setStatusChangeActive(true);
    }
  };

  const handleStatusUpdate = (data: PaymentStateResponse) => responseData.content?.forEach(item => {
    const row = data.find(el => el.requestId === item.id);
    if (row) {
      item.status = row.status;
    }
  });

  const renderChangeStatusButton = (): JSX.Element | boolean => isOrgStructureRole
    && !isStatusChangeActive && (
      <Button
        type="primary"
        danger={isStatusChangeActive}
        onClick={handleChangeStatusClick}
      >
        {t.Forms.registryFilterFields.paidStatusButton}
      </Button>
  );

  const renderActiveStatusChangeButtons = (): JSX.Element | boolean => isStatusChangeActive && (
  <ActiveStatusChangeButtons
    setStatusChangeActive={setStatusChangeActive}
    selectedRowData={selectedRowData}
    savePaymentStatuses={savePaymentStatuses}
    isSavingStatuses={isSavingStatuses}
    handleStatusUpdate={handleStatusUpdate}
  />
  );

  const handleSearchId = (id: string) => {
    form?.setFieldsValue({ requestHumanId: id });

    if (id.length > 2 && id.length < 20 || !id) {
      onFinish({ ...form?.getFieldsValue(true), requestHumanId: id });
    }
  };

  const transportType = 'personal';

  return (
    <>
      <ExportXLSModal<PersonalRegistryFilters, Partial<RequestBodyPersonalParams>>
        transportType={transportType}
        filterParams={filters}
        mimeType={XLSX_MIME_TYPE}
        onRequestParamsOnXlsDownload={processRequestParamsOnXlsDownloadPersonal}
      />

      <ActionsRegistry
        isNewDesign
        idValue={form?.getFieldValue('requestHumanId')}
        handleSearchId={handleSearchId}
        isVisible={isVisible}
        setIsVisible={setIsVisible}
        filters={(
          <FilterPanel
            isNewDesign
            withFormSection
            filterValues={initialFormValues}
            submitBtnName={t.global.confirm}
            fields={fields}
            onApplyFilters={onFinish}
            setForm={setForm}
            isStatusChangeActive={isStatusChangeActiveFilter}
            setStatusChangeActive={setStatusChangeActiveFilter}
            onClearButtonActivator={onClearButtonActivator}
            initialSearch
            setIsVisible={setIsVisible}
          />
        )}
        buttons={[
          <Fragment key="isOrgStructureRole">
            {isOrgStructureRole && (
              <CompensationModal<PersonalRegistryFilters, Partial<RequestBodyPersonalParams>, PersonalUIVisibilityDTO>
                transportType={transportType}
                filterParams={filters}
                columnsVisibility={userColumnVisibilitySettings?.personalUIVisibility}
                mimeType={XLSX_MIME_TYPE}
                onRequestParamsOnXlsDownload={processRequestParamsOnXlsDownloadPersonal}
              />
            )}
          </Fragment>,
          renderActiveStatusChangeButtons(),
          renderChangeStatusButton(),
        ]}
      />

      <TableSettingsContainer>
        <DefaultSorting onClick={onDefaultSortClick} disabled={isStatusChangeActive} />
        <DisplayPeriod period={period} />
        {/* <ColumnVisibilitySettings
          disabled={isStatusChangeActive}
          setting={userColumnVisibilitySettings?.personalUIVisibility}
          saveSettingChange={handleSaveColumnVisibilitySettings}
          isButtonDisabled={isSaving}
          defaultColumns={defaultColumns.personalUIVisibility}
        /> */}
      </TableSettingsContainer>

      <ActiveStatusChangeWarning
        visible={isVisibleChangeStatusModal}
        tableChangeParams={tableChangeParams}
        setFilterParams={setFilterParams}
        setVisibleChangeStatusModal={setVisibleChangeStatusModal}
        setStatusChangeActive={setStatusChangeActive}
        savePaymentStatuses={savePaymentStatuses}
        selectedRowData={selectedRowData}
        isSavingStatuses={isSavingStatuses}
      />
      <Table
        sticky
        isFetching={isFetching}
        rowSelection={isStatusChangeActive ? { ...rowSelection } : undefined}
        pagination={{
          total: responseData.totalElements,
          current: responseData.pageable.pageNumber + 1,
          pageSize: filters.pageSetting.size,
        }}
        onChange={onTableChange}
        columns={tableColumns}
        dataSource={dataSource}
        rowKey="id"
        scroll={{ x: 400 }}
      />
    </>
  );
};
