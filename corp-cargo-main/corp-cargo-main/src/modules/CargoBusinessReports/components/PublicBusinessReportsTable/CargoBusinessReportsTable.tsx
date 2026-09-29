import React, {
  FC, useEffect, useState
} from 'react';
import { FormInstance } from 'antd/lib/form';
import { useTranslation } from 'i18n';
import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import { useSettingsContext } from 'stores/SettingsContext';
import { clearButtonActive } from 'utils/reportsUtils';
import { FilterPanelBusinessReports } from 'shared/components/FilterPanelBusinessReports';
import { downloadBusinessReportsXLSAsync } from 'api/reports';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { useProfile } from 'api/profile';

import { useFilterFields } from '../../hooks/useFilterFields';
import { Filters, RequestBodyPublic } from '../../types/types';
import { initialFormValues } from '../../constants/constants';
import { processRequestParamsOnXlsDownloadPublic, processCreateSubmitJson } from '../../utils/utils';
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

export const CargoBusinessReportsTable: FC = () => {
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

  const onFinish = (data: Filters) => {
    processCreateSubmitJson(data, sortSettings());
    form?.resetFields();
    setStatusChangeActiveFilter(false);
  };

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
    isLoading;
    setIsLoading(true);

    downloadBusinessReportsXLSAsync<RequestBodyPublic>(
      http,
      transportType,
      mimeType,
      processRequestParamsOnXlsDownloadPublic(
        {
          withFilters: true,
          filters: processCreateSubmitJson({ ...form?.getFieldsValue(true) }, sortSettings()),
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

  return (
    <div style={journalStyles.tableWrapper}>
      <ActionsRegistry
        isNewDesign
        idValue={idValue}
        handleChangeId={handleChangeId}
        handleSearchId={handleSearchId}
        isVisible={isVisible}
        setIsVisible={setIsVisible}
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
