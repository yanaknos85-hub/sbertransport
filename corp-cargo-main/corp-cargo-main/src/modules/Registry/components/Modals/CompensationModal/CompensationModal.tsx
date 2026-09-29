import React, { useState } from 'react';
import {
  Button, Checkbox, Col, Modal, Row
} from 'antd';
import { useTranslation } from 'i18n';

import { downloadCompensationsXLSAsync } from 'api/reports';
import { useProfile } from 'api/profile';
import { useAllTripPurposes } from 'api/purposes';
import { XLS_MIME_TYPE, XLSM_MIME_TYPE, XLSX_MIME_TYPE } from 'shared/constants/reports.constants';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { TripPurpose } from 'stores/TripPurposes/TripPurpose.interface';
import styles from './Modal.module.scss';

interface ReportParams<T, C> {
  filters: T;
  withFilters: boolean;
  reportType: 'REGISTRY' | 'COMPENSATIONS';
  withView: boolean;
  columnsVisibility: C | undefined;
}

interface ModalProps<T, P, C> {
  transportType: 'personal' | 'public' | 'taxi' | 'cargo';
  filterParams: T;
  columnsVisibility?: C | undefined;
  mimeType: typeof XLSX_MIME_TYPE | typeof XLS_MIME_TYPE | typeof XLSM_MIME_TYPE;
  onRequestParamsOnXlsDownload: (params: ReportParams<T, C>, organizationId: string, purposes: TripPurpose[]) => P;
}

export const CompensationModal = <T, P extends Record<string, unknown>, C = undefined>({
  transportType,
  filterParams,
  columnsVisibility,
  mimeType,
  onRequestParamsOnXlsDownload,
}: ModalProps<T, P, C>) => {
  const { t } = useTranslation();
  const { http, logger } = useAppStoreContext();
  const [visible, setVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [withFilters, setWithFilters] = useState<boolean>(false);
  const { organizationId } = useProfile().data;
  // @ts-ignore
  const { purposes } = useAllTripPurposes(organizationId).data;

  const handleOk = () => {
    setIsLoading(true);
    downloadCompensationsXLSAsync<P>(
      http,
      transportType,
      mimeType,
      onRequestParamsOnXlsDownload(
        {
          reportType: 'COMPENSATIONS',
          withFilters,
          withView: false,
          filters: filterParams,
          columnsVisibility,
        },
        organizationId,
        purposes
      )
    )
      .then(() => {
        setVisible(false);
        setIsLoading(false);
      })
      .catch(() => {
        setIsLoading(false);
        logger.toMessage('error', t.Tariffs.InternalServerError);
      });
  };

  const handleCancel = () => {
    setVisible(false);
  };

  return (
    <>
      <Button type="primary" onClick={() => setVisible(true)}>
        {t.Forms.RegistryXLSModal.xlsCompensations}
      </Button>
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
        <p className={styles.warning}>{t.Forms.RegistryXLSModal.importWarning}</p>
        <p>{t.Forms.RegistryXLSModal.proceedImport}</p>
        <Row>
          <Col span={24}>
            <Checkbox value={withFilters} onChange={() => setWithFilters(!withFilters)}>
              {t.Forms.RegistryXLSModal.withFilters}
            </Checkbox>
          </Col>
        </Row>
      </Modal>
    </>
  );
};
