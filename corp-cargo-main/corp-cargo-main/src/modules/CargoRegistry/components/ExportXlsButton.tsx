import React, { FC, useState } from 'react';
import { Checkbox, Modal } from 'antd';
import { useTranslation } from 'i18n';
import { downloadRegistryXLSAsync } from 'api/reports';
import { useProfile } from 'api/profile';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import { ExportXlsButton as DefaultExportXlsButton } from 'modules/Registry/components/ExportXlsButton/ExportXlsButton';
import { ExportKceButton as DefaultExportKceButton } from 'modules/Registry/components/ExportKceButton/ExportKceButton';
import { ColumnVisibilitySettings, TransformedFilterValues } from '../types';

interface ExportXlsButtonProps {
  disabled?: boolean;
  filterValues?: TransformedFilterValues;
  columnVisibility?: ColumnVisibilitySettings;
  formatType?: string;
}

export const ExportXlsButton: FC<ExportXlsButtonProps> = ({
  disabled, filterValues, formatType = 'xls',
}) => {
  const { t } = useTranslation();
  const { http, logger } = useAppStoreContext();
  const { organizationId } = useProfile().data;
  const [isVisible, setIsVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [withFilters, setWithFilters] = useState(false);
  const [withView] = useState(false);

  const handleOk = () => {
    setIsLoading(true);
    downloadRegistryXLSAsync(
      http,
      'cargo',
      XLSX_MIME_TYPE,
      {
        withView,
        // cargoUIVisibilityDTO: withView ? columnVisibility : undefined,
        withFilters,
        ...(withFilters ? filterValues : {}),
      },
      organizationId,
      logger,
      setIsLoading,
      formatType
    )
      .then(() => {
        setIsVisible(false);
        setIsLoading(false);
      })
      .catch(e => {
        console.log(e);
        setIsLoading(false);
        logger.toMessage('error', t.Tariffs.InternalServerError);
      });
  };

  return (
    <>
      {formatType === 'cse' ? (
        <DefaultExportKceButton onClick={() => setIsVisible(true)} />
      ) : (
        <DefaultExportXlsButton onClick={() => setIsVisible(true)} />
      )}
      <Modal
        visible={isVisible}
        onOk={handleOk}
        onCancel={() => setIsVisible(false)}
        confirmLoading={isLoading}
      >
        <p>{t.Forms.RegistryXLSModal.xlsImportQuestion}</p>
        <Checkbox checked={withFilters} onClick={() => setWithFilters(prev => !prev)}>
          {t.Forms.RegistryXLSModal.withFilters}
        </Checkbox>
      </Modal>
    </>
  );
};
