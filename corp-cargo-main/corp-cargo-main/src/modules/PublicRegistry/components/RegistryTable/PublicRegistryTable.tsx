import { Button } from 'antd';
import { FormInstance } from 'antd/lib/form';
import { useProfile } from 'api/profile';
import { useRegistryJournalData } from 'api/public-register-search';
import { TripRequestStatuses } from 'constants/constants.app';
import { useTranslation } from 'i18n';
import { CompensationModal } from 'modules/Registry/components/Modals/CompensationModal/CompensationModal';
import { ActionsRegistry } from 'modules/Registry/components/ActionsRegistry/ActionsRegistry';
import { DefaultSorting } from 'modules/Registry/components/DefaultSorting/DefaultSorting';
import { Table } from 'modules/Registry/components/Table/Table';
import { TableSettingsContainer } from 'modules/Registry/components/TableSettingsContainer/TableSettingsContainer';
import React, {
  Dispatch, FC, Fragment, SetStateAction, useEffect, useState, useMemo
} from 'react';
import { FilterPanel } from 'shared/components/FilterPanel';
import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import {
  DateRange,
  DateRangeISO,
  PaymentStateResponse,
  PublicRegistryFilters,
  PublicUIVisibilityDTO
} from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { useSettingsContext } from 'stores/SettingsContext';
import { clearButtonActive } from 'utils/reportsUtils';

import { useColumnVisibilitySettings } from '../../hooks/useColumnVisibilitySettings';
import { useFilterFields } from '../../hooks/useFilterFields';
import { useJournalData } from '../../hooks/useJournalData';
import { useRowSelection } from '../../hooks/useRowSelection';
import { useSortingSettings } from '../../hooks/useSortingSettings';
import { useTable } from '../../hooks/useTable';
import { Filters, RequestBodyPublicParams } from '../../types/types';
import { processRequestParamsOnXlsDownloadPublic, processSearchSubmitJson } from '../../utils/utils';
import { ActiveStatusChangeButtons } from '../ActiveStatusChangeButtons/ActiveStatusChangeButtons';
import { ActiveStatusChangeWarning } from '../Modal/ActiveStatusChangeWarning/ActiveStatusChangeWarning';
import { ExportXLSModal } from 'modules/Registry/components/Modals/ExportXLSModal/ExportXLSModal';
import styles from './RegistryTable.module.scss';
import { DisplayPeriod } from 'modules/Registry/components/DisplayPeriod/DisplayPeriod';
import { initialFormValues } from '../../constants/PublicRegistry.constants';

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
  setFilterParams: Dispatch<SetStateAction<PublicRegistryFilters | undefined>>;
  isStatusChangeActive: boolean;
  setStatusChangeActive: Dispatch<SetStateAction<boolean>>;
}> = ({
  filters, setFilterParams, isStatusChangeActive, setStatusChangeActive,
}) => {
  const { t } = useTranslation();
  const { userId, organizationId } = useProfile().data;
  const {
    refetch, data: responseData, isFetching,
  } = useRegistryJournalData(filters, organizationId);
  const { processedJournalData } = useJournalData(responseData.responseData.content);
  const [form, setForm] = useState<FormInstance<any>>();
  const [isStatusChangeActiveFilter, setStatusChangeActiveFilter] = useState(true);
  const isOrgStructureRole = true;

  const [isVisible, setIsVisible] = useState(false);

  const period = useMemo(
    () => [filters.orderPaymentFormationStartDate, filters.creationDate].filter(Boolean) as (DateRange | DateRangeISO)[],
    [filters.orderPaymentFormationStartDate, filters.creationDate]
  );

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
  } = useSortingSettings(
    responseData.responseData,
    setFilterParams
  );

  const {
    setSelectedTripIds,
    rowSelection,
    selectedRowData,
    setSelectedRowData,
    savePaymentStatuses,
    isSavingStatuses,
  } = useRowSelection(processedJournalData);

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
    const initElements = responseData.responseData.content.reduce((acc: string[], { status, id }) => {
      if (status === TripRequestStatuses.PAYMENT_AWAITING) {
        acc.push(id);
      }
      return acc;
    }, []);
    setSelectedTripIds(initElements);
  }, [responseData, setSelectedTripIds]);

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
      setSelectedRowData(initData);
      setStatusChangeActive(true);
    }
  };

  const handleStatusUpdate = (data: PaymentStateResponse) => responseData.responseData.content.forEach(item => {
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

  const onClearButtonActivator = () => {
    const values = form?.getFieldsValue(true);
    setStatusChangeActiveFilter(clearButtonActive(values));
  };
  const fields = useFilterFields(onClearButtonActivator);

  const { sortSettings } = useSettingsContext().Public;

  const onFinish = (data: Filters) => setFilterParams(processSearchSubmitJson(data, sortSettings()));

  const handleSearchId = (id: string) => {
    form?.setFieldsValue({ requestHumanId: id });

    if (id.length > 2 && id.length < 20 || !id) {
      onFinish({ ...form?.getFieldsValue(true), requestHumanId: id });
    }
  };

  return (
    <div style={journalStyles.tableWrapper}>
      <ExportXLSModal<PublicRegistryFilters, Partial<RequestBodyPublicParams>>
        transportType="public"
        filterParams={filters}
        mimeType={XLSX_MIME_TYPE}
        onRequestParamsOnXlsDownload={processRequestParamsOnXlsDownloadPublic}
      />

      <ActionsRegistry
        isNewDesign
        idValue={form?.getFieldValue('requestHumanId')}
        handleSearchId={handleSearchId}
        isVisible={isVisible}
        setIsVisible={setIsVisible}
        filters={(
          <FilterPanel
            withFormSection
            isNewDesign
            submitBtnName={t.global.confirm}
            fields={fields}
            filterValues={initialFormValues}
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
              <CompensationModal<PublicRegistryFilters, Partial<RequestBodyPublicParams>, PublicUIVisibilityDTO>
                transportType="public"
                filterParams={filters}
                columnsVisibility={userColumnVisibilitySettings?.publicUIVisibility}
                mimeType={XLSX_MIME_TYPE}
                onRequestParamsOnXlsDownload={processRequestParamsOnXlsDownloadPublic}
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
          setting={userColumnVisibilitySettings?.publicUIVisibility}
          saveSettingChange={handleSaveColumnVisibilitySettings}
          isButtonDisabled={isSaving}
          defaultColumns={defaultColumns.publicUIVisibility}
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
        rowSelection={isStatusChangeActive ? { ...rowSelection } : undefined}
        pagination={{
          total: responseData.responseData.totalElements,
          current: responseData.responseData.pageable.pageNumber + 1,
          pageSize: filters.pageSetting.size,
        }}
        onChange={onTableChange}
        columns={tableColumns}
        dataSource={processedJournalData}
        style={journalStyles.table}
        scroll={journalStyles.tableScroll}
        rowKey="id"
        isFetching={isFetching}
      />
    </div>
  );
};
