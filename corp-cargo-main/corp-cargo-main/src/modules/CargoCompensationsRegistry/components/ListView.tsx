import React, {
  FC, useCallback, useMemo, useState
} from 'react';
import { message } from 'antd';
import { Key, SorterResult, TablePaginationConfig } from 'antd/lib/table/interface';
import { useTranslation } from 'i18n';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { Table } from 'modules/Registry/components/Table/Table';
import { TableSettingsContainer } from 'modules/Registry/components/TableSettingsContainer/TableSettingsContainer';
import { DefaultSorting } from 'modules/Registry/components/DefaultSorting/DefaultSorting';
import { ColumnVisibilitySettings } from 'modules/Registry/components/ColumnVisibilitySettings/ColumnVisibilitySettings';
import { ActionsRegistry } from 'modules/Registry/components/ActionsRegistry/ActionsRegistry';
import { Button } from 'shared/components/Button/Button';
import { FilterPanel } from 'shared/components/FilterPanel';
import { usePagination } from 'shared/hooks/usePagination';
import { useColumns } from '../hooks/useColumns';
import { sortFields, VisibleFields } from '../constants';
import { useSorting } from '../hooks/useSorting';
import { useFilter } from '../hooks/useFilter';
import { FilterValues, Row } from '../types';
import { useDataSource } from '../hooks/useDataSource';
import { GenerateRegisterButton } from './GenerateRegisterButton/GenerateRegisterButton';
import { useCancelCompensations, usePaymentCompensation } from 'api/cargo-registry-compensations-search';

import styles from './ListView.module.scss';

interface ListViewProps {
  pathname: string;
}

const hasAppliedFilters = (filterValues?: FilterValues): boolean => {
  if (!filterValues) return false;

  // Исключаем поля, которые не являются реальными фильтрами
  const { ...actualFilters } = filterValues;

  return Object.values(actualFilters).some(value => {
    if (value === undefined || value === null) return false;
    if (Array.isArray(value)) return value.length > 0;
    if (typeof value === 'string') return value.trim().length > 0;
    if (typeof value === 'number') return true;
    if (typeof value === 'boolean') return true;
    return false;
  });
};

export const ListView: FC<ListViewProps> = withErrorBoundary(({ pathname }) => {
  const { pageSetting, setPageSetting } = usePagination();
  const {
    sortSetting, setSortSetting, handleDefaultSort,
  } = useSorting();
  const {
    filterValues, setFilterValues, ...filterProps
  } = useFilter();

  const { columns, settings: visibilitySettings } = useColumns(pathname, sortSetting);
  const [isVisible, setIsVisible] = useState(false);
  const [paymentCompensations, { isLoading: isPaymentLoading }] = usePaymentCompensation();
  const [cancelCompensations, { isLoading: isCancelling }] = useCancelCompensations();

  const { t } = useTranslation();

  const {
    dataSource, isLoading, metadata, refetch,
  } = useDataSource({
    filterValues,
    sortSetting,
    pageSetting,
    setPageSetting,
  });

  const pagination = useMemo(
    () => ({
      total: metadata?.total || 0,
      current: (metadata?.page || 0) + 1,
      showSizeChanger: true,
      pageSize: metadata?.pageSize,
      onChange: (page: number, size?: number) => {
        setPageSetting({ page: page - 1, size: size || pageSetting.size });
      },
    }),
    [metadata, setPageSetting, pageSetting.size]
  );

  const onTableChange = useCallback(
    <RecordType extends Row>(
      _pagination: TablePaginationConfig,
      _filters: Record<string, (Key | boolean)[] | null>,
      sorter: SorterResult<RecordType> | SorterResult<RecordType>[]
    ) => {
      const sorterElement = Array.isArray(sorter) ? sorter[0] : sorter;
      const sortProperty = sortFields[sorterElement?.column?.dataIndex as VisibleFields];
      if (sortProperty) {
        setSortSetting({
          property: sortProperty,
          directionAsc: sorterElement.order === 'ascend',
        });
      } else {
        setSortSetting(undefined);
      }
    },
    [setSortSetting]
  );
  const handleSearchId = (id: string) => {
    if (id.length === 0) {
      filterProps.form?.setFieldsValue({ humanReadableId: undefined });
      setFilterValues({ ...filterProps.form?.getFieldsValue(true), humanReadableId: undefined });
    }
    if (id.length < 3 || id.length > 20) {
      return;
    }
    filterProps.form?.setFieldsValue({ humanReadableId: id });
    setFilterValues({ ...filterProps.form?.getFieldsValue(true), humanReadableId: id });
  };
  const setFiltersWithAdditionalCheck = (values: FilterValues) => {
    const copiedValues = { ...values };
    if (Array.isArray(values.transportType) && values.transportType.length === 0) {
      delete copiedValues.transportType;
      filterProps.form?.setFieldsValue({ cargoTransportType: undefined });
    }

    setPageSetting(setting => {
      if (setting.page === 0) return setting;
      return { page: 0, size: setting.size };
    });
    setFilterValues(copiedValues);
  };

  const handlePayment = useCallback(async () => {
    if (!hasAppliedFilters(filterValues)) {
      message.error(t.Forms.registryCargoCompensationsMessages.filtersPaymentRequired);
      return;
    }
    await paymentCompensations(filterValues);
    refetch();
  }, [filterValues, paymentCompensations, refetch, t]);

  const handleCancel = useCallback(async () => {
    if (!hasAppliedFilters(filterValues)) {
      message.error(t.Forms.registryCargoCompensationsMessages.filtersCancelRequired);
      return;
    }
    await cancelCompensations(filterValues);
    refetch();
  }, [filterValues, cancelCompensations, t]);

  return (
    <div className={styles.listView}>
      <ActionsRegistry
        isNewDesign
        handleSearchId={handleSearchId}
        isVisible={isVisible}
        setIsVisible={setIsVisible}
        filters={(
          <FilterPanel
            {...filterProps}
            withFormSection
            isNewDesign
            onApplyFilters={setFiltersWithAdditionalCheck}
            initialSearch
            setIsVisible={setIsVisible}
          />
        )}
        buttons={[
          <GenerateRegisterButton
            key="generate-register"
            disabled={!dataSource.length}
            filterValues={filterValues}
            columnVisibility={visibilitySettings.columnVisibility}
          />,
          <Button
            key="pay-compensation"
            type="primary"
            onClick={handlePayment}
            loading={isPaymentLoading}
            disabled={dataSource.length === 0 || isPaymentLoading}
          >
            Выплатить
          </Button>,
          <Button
            key="cancel-compensation"
            type="primary"
            danger
            onClick={handleCancel}
            loading={isCancelling}
            disabled={dataSource.length === 0 || isCancelling}
          >
            Отменить выплату
          </Button>,
        ]}
      />

      <TableSettingsContainer>
        <DefaultSorting onClick={handleDefaultSort} />

        <ColumnVisibilitySettings
          setting={visibilitySettings.columnVisibility}
          saveSettingChange={
            visibilitySettings.setColumnVisibility as (key: Record<string, boolean | undefined> | undefined) => void
          }
          defaultColumns={visibilitySettings.defaultVisibility}
          isButtonDisabled={visibilitySettings.isSaving}
          settingsEntries={t.Forms.registryCargoCompensationsSettings}
        />
      </TableSettingsContainer>

      {filterValues && (
        <Table
          isFetching={isLoading}
          dataSource={dataSource}
          columns={columns}
          bordered
          size="small"
          rowKey={VisibleFields.humanReadableId}
          tableLayout="auto"
          onChange={onTableChange}
          pagination={pagination}
          scroll={{ x: true }}
        />
      )}
    </div>
  );
});
