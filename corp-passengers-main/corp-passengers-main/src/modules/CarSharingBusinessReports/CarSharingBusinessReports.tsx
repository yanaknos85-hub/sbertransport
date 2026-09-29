import React, {
  useState, FC, Suspense, useEffect
} from 'react';
import { FormInstance } from 'antd/lib/form/Form';
import { useTranslation } from 'i18n';

import { initialFormValues } from './constants/CarSharingRegistry.constants';

import { downloadBusinessReportsXLSAsync } from 'api/reports';
import { useProfile } from 'api/profile';

import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { usePagination } from 'shared/hooks/usePagination';
import { CarSharingReportFilters, CarSharingSearchQuery } from 'stores/CarSharingTrip/CarSharingTrip.interface';
import { clearButtonActive } from 'utils/reportsUtils';
import { FilterPanelBusinessReports } from 'shared/components/FilterPanelBusinessReports';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';

import { ActionsRegistry } from './components/ActionsRegistry/ActionsRegistry';
import { CarSharingBusinessReportsTable } from './components/CarSharingBusinessReportsTable';
import { useFilterFields } from './hooks/useFilterFields';
import { useRegistryFilters } from '../Registry/RegistryFilterContext';
import { processRequestParamsOnXlsDownloadCarSharing, processSearchSubmitJson } from './utils';

const CarSharingBusinessReports: FC = () => {
  const { t } = useTranslation();
  const { filterValues, setFilterValues } = useRegistryFilters<CarSharingReportFilters>();
  const {
    pageSetting, onPaginationChange, resetPagination,
  } = usePagination(true);
  const [isStatusChangeActiveTable, setStatusChangeActiveTable] = useState(true);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [form, setForm] = useState<FormInstance<any>>();
  const [idValue, setIdValue] = useState<string>(form?.getFieldValue('requestHumanId'));
  const [isVisible, setIsVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const transportType = 'carsharing';
  const { http, logger } = useAppStoreContext();
  const mimeType = XLSX_MIME_TYPE;
  const { organizationId } = useProfile().data;

  const pageOptions = {
    pageSetting,
    onPaginationChange,
  };

  const { filterValues: filters } = useRegistryFilters<CarSharingReportFilters>();

  const countActiveFilters = Object.keys(filters).length;

  const onFinish = (data: CarSharingSearchQuery) => setFilterValues(processSearchSubmitJson(data));

  const onClearButtonActivator = () => {
    const values = form?.getFieldsValue(true);
    setStatusChangeActiveTable(clearButtonActive(values));
  };

  const onClearDepartments = () => {
    form?.setFieldsValue({
      department1: undefined,
      department2: undefined,
      department3: undefined,
      department4: undefined,
      department5: undefined,
      department6: undefined,
    });
  };

  const fields = useFilterFields(onClearButtonActivator, onClearDepartments, form);

  const handleSearchId = (id: string) => {
    form?.setFieldsValue({ requestHumanId: id });

    // eslint-disable-next-line @stylistic/no-mixed-operators
    if (id.length > 2 && id.length < 20 || !id) {
      resetPagination();
      onFinish({ ...form?.getFieldsValue(true), requestHumanId: id });
    }
  };

  const handleChangeId = (id: string) => {
    if (id.length <= 16) {
      setIdValue(id);
    }
  };

  useEffect(() => {
    setIdValue(form?.getFieldValue('requestHumanId'));
  }, [form?.getFieldValue('requestHumanId')]);

  const onExport = () => {
    setIsLoading(true);

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    downloadBusinessReportsXLSAsync<any>(
      http,
      transportType,
      mimeType,
      processRequestParamsOnXlsDownloadCarSharing(
        {
          withFilters: true,
          filters: processSearchSubmitJson({ ...form?.getFieldsValue(true) }),
        },
        form?.getFieldValue('organizationId') ?? organizationId
      ),
      form?.getFieldValue('organizationId') ?? organizationId,
      logger,
      setIsLoading
    )
      .then()
      .catch(() => {
        setIsLoading(false);
        logger.toMessage('error', t.Tariffs.InternalServerError);
      });
  };

  const handleResetFilters = () => {
    if (form) {
      form.resetFields();
      onFinish(form.getFieldsValue(true));
      setStatusChangeActiveTable(false);
    }
  };

  return (
    <>
      <ActionsRegistry
        isNewDesign
        idValue={idValue}
        handleChangeId={handleChangeId}
        handleSearchId={handleSearchId}
        isVisible={isVisible}
        setIsVisible={setIsVisible}
        countActiveFilters={countActiveFilters}
        handleResetFilters={handleResetFilters}
        filters={(
          <FilterPanelBusinessReports
            withFormSection
            isNewDesign
            submitBtnName={t.global.confirm}
            fields={fields}
            filterValues={initialFormValues}
            setForm={setForm}
            onApplyFilters={onFinish}
            resetPagination={resetPagination}
            isStatusChangeActive={isStatusChangeActiveTable}
            setStatusChangeActive={setStatusChangeActiveTable}
            onClearButtonActivator={onClearButtonActivator}
            initialSearch
            setIsVisible={setIsVisible}
            onSave={onExport}
          />
        )}
      />
      <Suspense fallback={<SpinWrapped />}>
        {filterValues && (
          <CarSharingBusinessReportsTable
            pageOptions={pageOptions}
            isStatusChangeActive={isStatusChangeActiveTable}
            setStatusChangeActive={setStatusChangeActiveTable}
            isLoading={isLoading}
            setIsLoading={setIsLoading}
            form={form}
          />
        )}
      </Suspense>
    </>
  );
};

export default CarSharingBusinessReports;
