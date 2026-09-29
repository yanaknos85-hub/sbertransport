import { FormInstance } from 'antd/lib/form';
import { useTranslation } from 'i18n';
import React, {
  Dispatch, FC, SetStateAction, useEffect, useState
} from 'react';

import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import {
  PublicRegistryFilters
} from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { useSettingsContext } from 'stores/SettingsContext';
import { clearButtonActive } from 'utils/reportsUtils';
import { ExportXLSModal } from 'shared/components/ModalsExportXLSBusinessReports/ExportXLSModal/ExportXLSModal';
import { FilterPanelBusinessReports } from 'shared/components/FilterPanelBusinessReports';
import { downloadBusinessReportsXLSAsync } from 'api/reports';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { useProfile } from 'api/profile';

import { useFilterFields } from '../../hooks/useFilterFields';
import { Filters, RequestBodyPublicParams } from '../../types/types';
import { initialFormValues } from '../../constants/PublicBusinessReports.constants';
import { processRequestParamsOnXlsDownloadPublic, processSearchSubmitJson } from '../../utils/utils';
import { ActionsRegistry } from '../ActionsRegistry/ActionsRegistry';

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

export const PublicBusinessReportsTable: FC<{
  filters: PublicRegistryFilters;
  setFilterParams: Dispatch<SetStateAction<PublicRegistryFilters | undefined>>;
}> = ({
  filters, setFilterParams,
}) => {
  const { t } = useTranslation();
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [form, setForm] = useState<FormInstance<any>>();
  const [idValue, setIdValue] = useState<string>(form?.getFieldValue('requestHumanId'));
  const [isStatusChangeActiveFilter, setStatusChangeActiveFilter] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const transportType = 'public';
  const { http, logger } = useAppStoreContext();
  const mimeType = XLSX_MIME_TYPE;
  const { organizationId } = useProfile().data;
  const withView = false;
  const countActiveFilters = Object.keys(filters).length - 1;
  const [isVisible, setIsVisible] = useState(false);

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

  const { sortSettings } = useSettingsContext().Public;

  const onFinish = (data: Filters) => setFilterParams(processSearchSubmitJson(data, sortSettings()));

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
      processRequestParamsOnXlsDownloadPublic(
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
    <div style={journalStyles.tableWrapper}>
      <ExportXLSModal<PublicRegistryFilters, Partial<RequestBodyPublicParams>>
        transportType="public"
        filterParams={filters}
        mimeType={XLSX_MIME_TYPE}
        onRequestParamsOnXlsDownload={processRequestParamsOnXlsDownloadPublic}
        isLoading={isLoading}
        setIsLoading={setIsLoading}
        form={form}
      />

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
    </div>
  );
};
