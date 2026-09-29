import React, { useState, Dispatch } from 'react';
import {
  Button, Checkbox, Col, Modal, Row
} from 'antd';
import { useTranslation } from 'i18n';
import { FormInstance } from 'antd/es/form';

import { downloadCompensationsXLSAsync, downloadYandexTaxiCompensationsXLSAsync } from 'api/reports';
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
  transportType: 'personal' | 'taxi' | 'cargo' | 'public' | 'external';
  filterParams: T;
  columnsVisibility?: C | undefined;
  mimeType: typeof XLSX_MIME_TYPE | typeof XLS_MIME_TYPE | typeof XLSM_MIME_TYPE;
  onRequestParamsOnXlsDownload: (params: ReportParams<T, C>, organizationId: string, purposes: TripPurpose[]) => P;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onFinish: Dispatch<any>;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  form?: FormInstance<any>;
}

export const CompensationModal = <T, P extends Record<string, unknown>, C = undefined>({
  transportType,
  filterParams,
  columnsVisibility,
  mimeType,
  onRequestParamsOnXlsDownload,
  onFinish,
  form,
}: ModalProps<T, P, C>) => {
  const { t } = useTranslation();
  const { http, logger } = useAppStoreContext();
  const [visible, setVisible] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [withFilters, setWithFilters] = useState<boolean>(false);
  const { organizationId, isOrganization } = useProfile().data;
  // @ts-ignore
  const { purposes } = useAllTripPurposes(organizationId).data;
  const downloadService = transportType === 'external'
    ? downloadYandexTaxiCompensationsXLSAsync
    : downloadCompensationsXLSAsync;

  const handleOk = () => {
    setIsLoading(true);
    downloadService<P>(
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
        if (form) {
          setTimeout(() => withFilters ? onFinish(form?.getFieldsValue(true)) : onFinish({}), 1000);
        }
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
      <Button
        type="primary"
        onClick={() => setVisible(true)}
        disabled={!isOrganization}
      >
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
