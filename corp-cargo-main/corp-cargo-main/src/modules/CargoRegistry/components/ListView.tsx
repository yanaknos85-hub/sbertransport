import React, {
  FC, useCallback, useMemo, useState, useRef, useEffect
} from 'react';
import { Key, SorterResult, TablePaginationConfig } from 'antd/lib/table/interface';
import { useTranslation } from 'i18n';
import { useOrganizationContext } from 'context/Organization.context';
import { RegisterSearchQuery } from "api/register-search";
import { useRegistryJournalDataDeffered, useRegistryJournalDataExecutorDeffered } from 'api/cargo-registry-search';
import { SearchResponse } from "stores/CargoRegistry/CargoRegistry.interface";
import { Table } from 'modules/Registry/components/Table/Table';
import { TableSettingsContainer } from 'modules/Registry/components/TableSettingsContainer/TableSettingsContainer';
import { DefaultSorting } from 'modules/Registry/components/DefaultSorting/DefaultSorting';
import { ColumnVisibilitySettings } from 'modules/Registry/components/ColumnVisibilitySettings/ColumnVisibilitySettings';
import { ActionsRegistry } from 'modules/Registry/components/ActionsRegistry/ActionsRegistry';
import { FilterPanel } from './FilterPanel';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { sortFields, VisibleFields } from '../constants';
import { useFilter } from '../hooks/useFilter';
import { useColumns } from '../hooks/useColumns';
import { useSorting } from '../hooks/useSorting';
import { usePagination } from 'shared/hooks/usePagination';
import { useTransformedData } from "../hooks/useTransformedData";
import { useDeferredSearch } from '../hooks/useDefferedSearch';
import { FilterValues, Row } from '../types';
import { hasAnyActiveFilter } from './utils';
import { ExportXlsButton } from './ExportXlsButton';

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
  const [showTable, setShowTable] = useState(false);

  // Проверка наличия выбранных фильтров
  const hasSelectedFilters = useCallback(() => {
    const values = filterProps.form?.getFieldsValue(true);
    return hasAnyActiveFilter(values);
  }, [filterProps.form]);

  const { t } = useTranslation();

  const {
    organizationId,
    isOrganization,
    executorGroupId,
    emptyExecutorGroup,
    allExecutorGroups,
  } = useOrganizationContext();

  const { executeSearch, data: responseData, isLoadingOrg, isLoadingExec } = useDeferredSearch<
    RegisterSearchQuery,
    SearchResponse
  >({
    orgHook: useRegistryJournalDataDeffered,
    executorHook: useRegistryJournalDataExecutorDeffered,
    organizationId,
    executorGroupId,
    emptyExecutorGroup,
    allExecutorGroups,
    isOrganization: isOrganization ?? false,
    initialData: undefined,
  });

  const transformedData = useTransformedData(responseData?.content || []);

  // Ref для отслеживания предыдущих параметров
  const prevParamsRef = useRef<any>(null);
  const hasSearchedRef = useRef(false);

  // Формируем параметры для сравнения
  const searchParams = useMemo(() => ({
    filterValues,
    pageSetting,
    sortSetting,
    organizationId,
    isOrganization,
    executorGroupId,
    emptyExecutorGroup,
    allExecutorGroups,
  }), [filterValues, pageSetting, sortSetting, organizationId, isOrganization, executorGroupId, emptyExecutorGroup, allExecutorGroups]);

  // Выполняем поиск при изменении параметров
  useEffect(() => {
    // Ждём пока загрузятся данные из контекста
    if (!organizationId) {
      return;
    }

    // Проверяем, есть ли фильтры (пустой объект {} считаем валидным)
    const hasValidFilters = filterValues !== undefined && filterValues !== null;

    if (!hasValidFilters) {
      return;
    }

    // Сравниваем параметры
    const paramsChanged = JSON.stringify(prevParamsRef.current) !== JSON.stringify(searchParams);

    // При изменении организации/исполнителей показываем таблицу и выполняем поиск
    const shouldSearch = (!hasSearchedRef.current || paramsChanged);

    if (shouldSearch) {
      setShowTable(true);
      executeSearch({ ...filterValues, pageSetting, sortSetting } as RegisterSearchQuery)
        .catch((error) => {
          console.error('Search failed:', error);
        });

      prevParamsRef.current = searchParams;
      hasSearchedRef.current = true;
    }
  }, [searchParams, executeSearch, filterValues, organizationId, showTable]);

  const pagination = useMemo(
    () => ({
      total: responseData?.totalElements || 0,
      current: pageSetting.page + 1,
      showSizeChanger: true,
      pageSize: pageSetting.size,
      onChange: (page: number, size?: number) => {
        setPageSetting({ page: page - 1, size: size || pageSetting.size });
      },
    }),
    [responseData?.totalElements, pageSetting, setPageSetting]
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
      filterProps.form?.setFieldsValue({ requestHumanId: undefined });
      setFilterValues({ ...filterProps.form?.getFieldsValue(true), requestHumanId: undefined });
    }
    if (id.length < 3 || id.length > 20) {
      return;
    }
    filterProps.form?.setFieldsValue({ requestHumanId: id });
    setFilterValues({ ...filterProps.form?.getFieldsValue(true), requestHumanId: id });
  };

  const setFiltersWithAdditionalCheck = (values: FilterValues) => {
    // Проверяем, что хотя бы один фильтр выбран
    if (!hasAnyActiveFilter(values)) {
      // Не устанавливаем showTable и не выполняем поиск, если фильтры не выбраны
      return;
    }
    const copiedValues = { ...values };
    if (Array.isArray(values.cargoTransportType) && values.cargoTransportType.length === 0) {
      delete copiedValues.cargoTransportType;
      filterProps.form?.setFieldsValue({ cargoTransportType: undefined });
    }
    setFilterValues(copiedValues);
    setShowTable(true);
  };

  return (
    <div className={styles.listView}>
      <ActionsRegistry
        showSearch={showTable}
        isNewDesign
        handleSearchId={handleSearchId}
        isVisible={isVisible}
        setIsVisible={setIsVisible}
        filters={(
          <FilterPanel
            {...filterProps}
            isNewDesign
            withFormSection
            onApplyFilters={setFiltersWithAdditionalCheck}
            initialSearch
            setIsVisible={setIsVisible}
            isVisible={isVisible}
            disabledSubmit={!hasSelectedFilters()}
          />
        )}
        buttons={[
          <ExportXlsButton
            key="export-cse"
            disabled={!responseData?.content?.length}
            filterValues={filterValues}
            columnVisibility={visibilitySettings.columnVisibility}
            formatType="cse"
          />,
          <ExportXlsButton
            key="export-xls"
            disabled={!responseData?.content?.length}
            filterValues={filterValues}
            columnVisibility={visibilitySettings.columnVisibility}
          />
        ]}
      />

      {showTable && (
        <TableSettingsContainer>
          <DefaultSorting onClick={handleDefaultSort} />

          <ColumnVisibilitySettings
            setting={visibilitySettings.columnVisibility}
            saveSettingChange={
              visibilitySettings.setColumnVisibility as (key: Record<string, boolean | undefined> | undefined) => void
            }
            defaultColumns={visibilitySettings.defaultVisibility}
            isButtonDisabled={visibilitySettings.isSaving}
            settingsEntries={t.Forms.registryCargoSettings}
          />
        </TableSettingsContainer>
      )}
      {showTable ? (
        <Table
          isFetching={isOrganization ? isLoadingOrg : isLoadingExec}
          dataSource={transformedData || []}
          columns={columns}
          bordered
          size="small"
          rowKey={VisibleFields.humanReadableId}
          tableLayout="auto"
          onChange={onTableChange}
          pagination={pagination}
          scroll={{ x: true }}
        />
      ) : (
        <div className={styles.placeholder}>
          <div className={styles.placeholderText}>
            Для получения данных по заявкам задайте параметры фильтрации и нажмите «Поиск».
          </div>
          <div className={styles.placeholderText}>
            Нажмите на кнопку «Фильтры»
          </div>
        </div>
      )}
    </div>
  );
});
