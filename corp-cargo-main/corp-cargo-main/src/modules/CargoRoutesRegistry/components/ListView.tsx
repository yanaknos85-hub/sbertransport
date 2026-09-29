import React, {
  FC, useCallback, useMemo, useState
} from 'react';
import { Key, SorterResult, TablePaginationConfig } from 'antd/lib/table/interface';
import { FilterPanel } from 'shared/components/FilterPanel';
import { Table } from 'modules/Registry/components/Table/Table';
import { TableSettingsContainer } from 'modules/Registry/components/TableSettingsContainer/TableSettingsContainer';
import { DefaultSorting } from 'modules/Registry/components/DefaultSorting/DefaultSorting';
import { ColumnVisibilitySettings } from 'modules/Registry/components/ColumnVisibilitySettings/ColumnVisibilitySettings';
import { ActionsRegistry } from 'modules/Registry/components/ActionsRegistry/ActionsRegistry';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { useTranslation } from 'i18n';
import { usePagination } from 'shared/hooks/usePagination';
import { useColumns } from '../hooks/useColumns';
import { sortFields, VisibleFields } from '../constants';
import { useSorting } from '../hooks/useSorting';
import { useFilter } from '../hooks/useFilter';
import { FilterValues, Row } from '../types';
import { useDataSource } from '../hooks/useDataSource';
import { ExportXlsButton } from './ExportXlsButton/ExportXlsButton';

import styles from './ListView.module.scss';

interface ListViewProps {
  pathname: string;
}

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

  const { t } = useTranslation();

  const {
    dataSource, isLoading, metadata,
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
    setFilterValues(copiedValues);
  };

  return (
    <div className={styles.listView}>
      <ActionsRegistry
        isNewDesign
        handleSearchId={handleSearchId}
        isVisible={isVisible}
        setIsVisible={setIsVisible}
        placeholder={t.Forms.registryFilterFields.searchByRouteId}
        filters={(
          <FilterPanel
            {...filterProps}
            isNewDesign
            withFormSection
            onApplyFilters={setFiltersWithAdditionalCheck}
            initialSearch
            setIsVisible={setIsVisible}
          />
        )}
        buttons={[
          <ExportXlsButton
            disabled={!dataSource.length}
            filterValues={filterValues}
            columnVisibility={visibilitySettings.columnVisibility}
          />,
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
          settingsEntries={t.Forms.registryCargoRoutesSettings}
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
