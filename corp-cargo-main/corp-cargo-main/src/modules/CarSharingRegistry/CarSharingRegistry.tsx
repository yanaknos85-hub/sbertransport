import React, {
  useState, FC, Suspense, useCallback
} from 'react';
import { FormInstance } from 'antd/lib/form/Form';
import { useTranslation } from 'i18n';
import { FilterPanel } from 'shared/components/FilterPanel';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { usePagination } from 'shared/hooks/usePagination';
import { CarSharingReportFilters, CarSharingSearchQuery } from 'stores/CarSharingTrip/CarSharingTrip.interface';
import { CarSharingRegistryTable } from './components/CarSharingRegistryTable';
import { useFilterFields } from './hooks/useFilterFields';
import { ActionsRegistry } from '../Registry/components/ActionsRegistry/ActionsRegistry';
import { useRegistryFilters } from '../Registry/RegistryFilterContext';
import { clearButtonActive } from 'utils/reportsUtils';
import { processSearchSubmitJson } from './utils';
import { initialFormValues } from './constants/CarSharingRegistry.constants';

const CarSharingRegistry: FC = () => {
  const { t } = useTranslation();
  const { filterValues, setFilterValues } = useRegistryFilters<CarSharingReportFilters>();
  const { pageSetting, setPageSetting } = usePagination({ page: 0, size: 100 });
  const [isStatusChangeActiveTable, setStatusChangeActiveTable] = useState(true);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [form, setForm] = useState<FormInstance<any>>();
  const [isVisible, setIsVisible] = useState(false);

  const handlePaginationChange = useCallback((page: number, size?: number) => {
    setPageSetting({ page: page - 1, size: size || 100 });
  }, [setPageSetting]);

  const handleResetPagination = useCallback(() => {
    setPageSetting({ page: 0, size: 100 });
  }, [setPageSetting]);

  const onFinish = (data: CarSharingSearchQuery) => setFilterValues(processSearchSubmitJson(data));

  const onClearButtonActivator = () => {
    const values = form?.getFieldsValue(true);
    setStatusChangeActiveTable(clearButtonActive(values));
  };

  const fields = useFilterFields(onClearButtonActivator);

  const handleSearchId = (id: string) => {
    form?.setFieldsValue({ requestHumanId: id });

    // eslint-disable-next-line @stylistic/no-mixed-operators
    if (id.length > 2 && id.length < 20 || !id) {
      handleResetPagination();
      onFinish({ ...form?.getFieldsValue(true), requestHumanId: id });
    }
  };

  return (
    <>
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
            setForm={setForm}
            onApplyFilters={onFinish}
            filterValues={initialFormValues}
            resetPagination={handleResetPagination}
            isStatusChangeActive={isStatusChangeActiveTable}
            setStatusChangeActive={setStatusChangeActiveTable}
            onClearButtonActivator={onClearButtonActivator}
            initialSearch
            setIsVisible={setIsVisible}
          />
        )}
      />
      <Suspense fallback={<SpinWrapped />}>
        {filterValues && (
          <CarSharingRegistryTable
            pageSetting={pageSetting}
            onPaginationChange={handlePaginationChange}
            isStatusChangeActive={isStatusChangeActiveTable}
            setStatusChangeActive={setStatusChangeActiveTable}
          />
        )}
      </Suspense>
    </>
  );
};

export default CarSharingRegistry;
