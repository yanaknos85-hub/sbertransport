import { Button, Modal, Typography } from 'antd';
import React, { Dispatch, FC, SetStateAction } from 'react';
import { useTranslation } from 'i18n';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import {
  PaymentStateResponse,
  PublicRegistryFilters
} from 'stores/PublicRegistry/models/PublicRegistry.interface';
import styles from './Warning.module.scss';
import { SelectedRowData } from '../../../hooks/useRowSelection';

interface ModalProps {
  visible: boolean;
  tableChangeParams: PublicRegistryFilters;
  setVisibleChangeStatusModal: Dispatch<SetStateAction<boolean>>;
  setFilterParams: Dispatch<SetStateAction<PublicRegistryFilters | undefined>>;
  setStatusChangeActive: Dispatch<SetStateAction<boolean>>;
  savePaymentStatuses: (data: SelectedRowData[]) => Promise<PaymentStateResponse | undefined>;
  selectedRowData: SelectedRowData[];
  isSavingStatuses: boolean;
}

export const ActiveStatusChangeWarning: FC<ModalProps> = ({
  visible,
  setVisibleChangeStatusModal,
  tableChangeParams,
  setFilterParams,
  setStatusChangeActive,
  savePaymentStatuses,
  selectedRowData,
  isSavingStatuses,
}) => {
  const { t } = useTranslation();
  const { logger } = useAppStoreContext();
  const { Text } = Typography;

  const handleOk = () => {
    const paidSelectedRowData = selectedRowData.filter(x => x.payed);

    if (paidSelectedRowData.length) {
      savePaymentStatuses(paidSelectedRowData)
        .then(() => {
          setVisibleChangeStatusModal(false);
          setStatusChangeActive(false);
          setFilterParams(tableChangeParams);
        })
        .catch(() => {
          logger.toMessage('error', t.Tariffs.InternalServerError);
        });
    } else {
      logger.toMessage('warning', t.Forms.registryFilterFields.emptyStatusesWarning);
    }
  };

  const handleCancel = () => {
    setStatusChangeActive(false);
    setVisibleChangeStatusModal(false);
    setFilterParams(tableChangeParams);
  };

  return (
    <Modal
      className={styles.modal}
      centered
      visible={visible}
      onOk={handleOk}
      confirmLoading={isSavingStatuses}
      onCancel={handleCancel}
      closable={false}
      footer={[
        <Button
          key="submit"
          type="primary"
          loading={isSavingStatuses}
          onClick={handleOk}
        >
          {t.global.save}
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
      <Text>{t.Forms.RegistryXLSModal.saveChanges}</Text>
    </Modal>
  );
};
