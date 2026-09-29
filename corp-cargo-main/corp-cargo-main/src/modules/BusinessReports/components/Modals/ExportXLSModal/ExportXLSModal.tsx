import React, { useState } from 'react';
import {
  Button, Checkbox, Col, Modal, Row
} from 'antd';
import { CheckboxValueType } from 'antd/es/checkbox/Group';
import { useTranslation } from 'i18n';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { XLS_MIME_TYPE, XLSM_MIME_TYPE, XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import { TripPurpose } from 'stores/TripPurposes/TripPurpose.interface';
import { useProfile } from 'api/profile';
import { downloadBusinessReportsXLSAsync } from 'api/reports';
import { ExportButton } from '../../ExportButton/ExportButton';

import styles from './Modal.module.scss';

interface ReportParams<T, C> {
  filters: T;
  withFilters: boolean;
  reportType: 'REGISTRY' | 'COMPENSATIONS';
  withView: boolean;
  columnsVisibility?: C;
}

interface ModalProps<T, P, C> {
  transportType: string;
  filterParams: T;
  isLoading: boolean;
  setIsLoading: React.Dispatch<React.SetStateAction<boolean>>;
  mimeType: typeof XLSX_MIME_TYPE | typeof XLS_MIME_TYPE | typeof XLSM_MIME_TYPE;
  widthFields?: boolean;
  columnsVisibility?: C;
  onRequestParamsOnXlsDownload: (params: ReportParams<T, C>, organizationId: string, purposes?: TripPurpose[]) => P;
}

export const ExportXLSModal = <T, P extends Record<string, unknown>, C = undefined>({
  transportType,
  widthFields = false,
  filterParams,
  columnsVisibility,
  mimeType,
  onRequestParamsOnXlsDownload,
  isLoading,
  setIsLoading,
}: ModalProps<T, P, C>) => {
  const { t } = useTranslation();
  const { http, logger } = useAppStoreContext();
  const { organizationId } = useProfile().data;
  const [visible, setVisible] = useState(false);
  const [withView, setWithView] = useState(false);
  // @ts-ignore
  // const { purposes } = useAllTripPurposes(organizationId).data;

  const setParams = (params: string[]) => {
    setWithView(params.includes('withView'));
  };

  const handleOk = () => {
    setIsLoading(true);

    downloadBusinessReportsXLSAsync<P>(
      http,
      transportType,
      mimeType,
      onRequestParamsOnXlsDownload(
        {
          withFilters: true,
          filters: filterParams,
          reportType: 'REGISTRY',
          withView,
          columnsVisibility,
        },
        organizationId
        // purposes
      ),
      organizationId,
      logger,
      setIsLoading
    )
      .then(() => {
        setVisible(false);
      })
      .catch(() => {
        setIsLoading(false);
        logger.toMessage('error', t.Tariffs.InternalServerError);
      });
  };

  const handleOpen = () => setVisible(true);

  const handleCancel = () => {
    setVisible(false);
  };

  return (
    <>
      <div className={styles.buttons}>
        <ExportButton onClick={handleOpen} loading={isLoading} />
      </div>

      <Modal
        className={styles.modal}
        centered
        visible={visible}
        onOk={handleOk}
        confirmLoading={isLoading}
        onCancel={handleCancel}
        footer={[
          <Button
            key="submit"
            type="primary"
            loading={isLoading}
            onClick={handleOk}
          >
            {t.global.closingConfirmation}
          </Button>,
          <Button
            key="back"
            danger
            onClick={handleCancel}
          >
            {t.Forms.RegistryXLSModal.cancel}
          </Button>,
        ]}
      >
        <p>{t.Forms.RegistryXLSModal.xlsImportQuestion}</p>

        {widthFields && (
          <Checkbox.Group
            style={{ width: '100%' }}
            onChange={(params: CheckboxValueType[]) => setParams(params as string[])}
          >
            <Row>
              <Col span={24}>
                <Checkbox value="withView">{t.Forms.RegistryXLSModal.withView}</Checkbox>
              </Col>
            </Row>
          </Checkbox.Group>
        )}
      </Modal>
    </>
  );
};
