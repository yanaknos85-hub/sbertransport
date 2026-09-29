import { useTranslation } from 'i18n';
import React, {
  Dispatch, FC, SetStateAction, useEffect, useState
} from 'react';
import { FormInstance } from 'antd/lib/form';

import {
  PersonalRegistryFilters,
  PersonalSearchQuery
} from 'stores/PersonalSearch/PersonalSearch.interface';
import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import { clearButtonActive } from 'utils/reportsUtils';
import { useSettingsContext } from 'stores/SettingsContext';
import { ExportXLSModal } from 'shared/components/ModalsExportXLSBusinessReports/ExportXLSModal/ExportXLSModal';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { useProfile } from 'api/profile';
import { FilterPanelBusinessReports } from 'shared/components/FilterPanelBusinessReports';
import { downloadBusinessReportsXLSAsync } from 'api/reports';

import { RequestBodyPersonalParams } from '../types/types';
import { initialFormValues } from '../constants/PersonalRegistry.constants';
import { useFilterFields } from '../hooks/useFilterFields';
import { processSearchSubmitJson, processRequestParamsOnXlsDownloadPersonal } from '../utils';
import { ActionsRegistry } from './ActionsRegistry/ActionsRegistry';

export const PersonalBusinessReportTables: FC<{
  filters: PersonalRegistryFilters;
  setFilterParams: Dispatch<SetStateAction<PersonalRegistryFilters | undefined>>;
}> = ({
  filters, setFilterParams,
}) => {
  const { t } = useTranslation();

  const [isStatusChangeActiveFilter, setStatusChangeActiveFilter] = useState(true);
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [form, setForm] = useState<FormInstance<any>>();
  const [idValue, setIdValue] = useState<string>(form?.getFieldValue('requestHumanId'));
  const onClearButtonActivator = () => {
    const values = form?.getFieldsValue(true);

    setStatusChangeActiveFilter(clearButtonActive(values));
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
  const [isLoading, setIsLoading] = useState(false);
  const [isVisible, setIsVisible] = useState(false);
  const { sortSettings } = useSettingsContext().Personal;
  const transportType = 'personal';
  const { http, logger } = useAppStoreContext();
  const mimeType = XLSX_MIME_TYPE;
  const { organizationId } = useProfile().data;
  const withView = false;
  const countActiveFilters = Object.keys(filters).length - 1;

  const onFinish = (data: PersonalSearchQuery) => setFilterParams(processSearchSubmitJson(data, sortSettings()));

  const handleSearchId = (id: string) => {
    form?.setFieldsValue({ requestHumanId: id });

    // eslint-disable-next-line @stylistic/no-mixed-operators
    if (id.length > 2 && id.length < 20 || !id) {
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
      processRequestParamsOnXlsDownloadPersonal(
        {
          withFilters: true,
          filters: processSearchSubmitJson({ ...form?.getFieldsValue(true) }, sortSettings()),
          reportType: 'REGISTRY',
          withView,
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
      setStatusChangeActiveFilter(false);
    }
  };

  return (
    <>
      <ExportXLSModal<PersonalRegistryFilters, Partial<RequestBodyPersonalParams>>
        transportType={transportType}
        filterParams={filters}
        mimeType={XLSX_MIME_TYPE}
        onRequestParamsOnXlsDownload={processRequestParamsOnXlsDownloadPersonal}
        isLoading={isLoading}
        setIsLoading={setIsLoading}
        form={form}
      />

      <ActionsRegistry
        isNewDesign
        idValue={idValue}
        handleSearchId={handleSearchId}
        handleChangeId={handleChangeId}
        isVisible={isVisible}
        setIsVisible={setIsVisible}
        countActiveFilters={countActiveFilters}
        handleResetFilters={handleResetFilters}
        filters={(
          <FilterPanelBusinessReports
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
            onSave={onExport}
          />
        )}
      />
    </>
  );
};
