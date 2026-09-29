import React, {
  Suspense, useEffect, useState
} from 'react';
import moment from 'moment';

import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';

import { useProfile } from 'api/profile';
import { downloadBusinessReportsXLSAsync } from 'api/reports';

import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import { FormInstance } from 'antd/lib/form/Form';
import { clearButtonActive } from 'utils/reportsUtils';
import { filter2searchQuery, processTaxiRequestBodyOnXlsDownload } from './utils';
import { useTranslation } from 'i18n';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { FilterPanelBusinessReports } from 'shared/components/FilterPanelBusinessReports';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';

import { useFilterFields } from './hooks/useFilterFields';
import { TaxiRegistryFilter } from './types/types';
import { TaxiBusinessReportsTable } from './TaxiBusinessReportsTable';
import { ActionsRegistry } from './components/ActionsRegistry/ActionsRegistry';

export const TaxiBusinessReports = withErrorBoundary(() => {
  const onSubmit = (values: TaxiRegistryFilter) => setFilterValues(values);
  const [isStatusChangeActive, setStatusChangeActive] = useState(true);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [form, setForm] = useState<FormInstance<any>>();
  const [idValue, setIdValue] = useState<string>(form?.getFieldValue('requestHumanId'));
  const { t } = useTranslation();
  const defaultFilters = {
    desiredDateRange: {
      mode: 'range',
      value: [moment().startOf('month'), moment()],
    },
  } as TaxiRegistryFilter;
  const [isLoading, setIsLoading] = useState(false);
  const transportType = 'taxi';
  const { http, logger } = useAppStoreContext();
  const mimeType = XLSX_MIME_TYPE;
  const { organizationId } = useProfile().data;
  const [filterValues, setFilterValues] = useState<TaxiRegistryFilter | undefined>(defaultFilters);
  const countActiveFilters = filterValues && Object.keys(filter2searchQuery(filterValues)).length - 1;

  const onClearButtonActivator = () => {
    const values = form?.getFieldsValue(true);
    setStatusChangeActive(clearButtonActive(values));
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
      onSubmit({ ...form?.getFieldsValue(true), requestHumanId: id });
    }
  };

  const [isVisible, setIsVisible] = useState(false);

  const buttonsRef = React.useRef<HTMLDivElement>(null);

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
      processTaxiRequestBodyOnXlsDownload(
        {
          withFilters: true,
          filters: filter2searchQuery({ ...form?.getFieldsValue(true) }),
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
      onSubmit(form.getFieldsValue(true));
      setStatusChangeActive(false);
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
        buttonsRef={buttonsRef}
        countActiveFilters={countActiveFilters}
        handleResetFilters={handleResetFilters}
        filters={(
          <FilterPanelBusinessReports
            withFormSection
            isNewDesign
            submitBtnName={t.global.confirm}
            fields={fields}
            setForm={setForm}
            onApplyFilters={onSubmit}
            filterValues={filterValues}
            isStatusChangeActive={isStatusChangeActive}
            setStatusChangeActive={setStatusChangeActive}
            onClearButtonActivator={onClearButtonActivator}
            initialSearch
            setIsVisible={setIsVisible}
            onSave={onExport}
          />
        )}
      />
      <Suspense fallback={<SpinWrapped />}>
        {filterValues && (
          <TaxiBusinessReportsTable
            filterValues={filter2searchQuery(filterValues)}
            buttonsRef={buttonsRef}
            isLoading={isLoading}
            setIsLoading={setIsLoading}
            form={form}
          />
        )}
      </Suspense>
    </>
  );
});
