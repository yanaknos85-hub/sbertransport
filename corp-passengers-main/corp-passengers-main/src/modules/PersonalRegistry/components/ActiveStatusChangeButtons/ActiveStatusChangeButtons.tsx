import { Button } from 'antd';
import React, { Dispatch, FC, SetStateAction } from 'react';
import { useTranslation } from 'i18n';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { PaymentStateResponse } from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { SelectedRowData } from '../../hooks/useRowSelection';
import { FormInstance } from 'antd/es/form';
import { StoreNames } from 'stores';

import styles from './ActiveStatusChangeButtons.module.scss';

interface Props {
  setStatusChangeActive: Dispatch<SetStateAction<boolean>>;
  selectedRowData: SelectedRowData[];
  savePaymentStatuses: (data: SelectedRowData[]) => Promise<PaymentStateResponse | undefined>;
  isSavingStatuses: boolean;
  handleStatusUpdate: (data: PaymentStateResponse) => void;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  onFinish: Dispatch<any>;
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  form?: FormInstance<any>;
}

export const ActiveStatusChangeButtons: FC<Props> = ({
  setStatusChangeActive,
  selectedRowData,
  savePaymentStatuses,
  isSavingStatuses,
  handleStatusUpdate,
  onFinish,
  form,
}) => {
  const { t } = useTranslation();
  const { [StoreNames.registryStore]: register, logger } = useAppStoreContext();

  const handleOk = () => {
    const paidSelectedRowData = selectedRowData.filter(x => x.payed);

    if (paidSelectedRowData.length) {
      savePaymentStatuses(paidSelectedRowData)
        .then(data => {
          data && handleStatusUpdate(data);
          setTimeout(() => onFinish(form?.getFieldsValue(true)), 1000);
        })
        .then(() => setStatusChangeActive(false))
        .catch(() => logger.toMessage('error', t.Tariffs.InternalServerError));
    } else {
      logger.toMessage('warning', t.Forms.registryFilterFields.emptyStatusesWarning);
    }
  };

  const handleCancelPayment = () => {
    const paidSelectedRowData = selectedRowData.filter(x => x.payed).map(el => ({ ...el, payed: false }));

    if (paidSelectedRowData.length) {
      savePaymentStatuses(paidSelectedRowData)
        .then(data => {
          data && handleStatusUpdate(data);
          setTimeout(() => onFinish(form?.getFieldsValue(true)), 1000);
        })
        .then(() => setStatusChangeActive(false))
        .catch(() => logger.toMessage('error', t.Tariffs.InternalServerError));
    } else {
      logger.toMessage('warning', t.Forms.registryFilterFields.emptyStatusesWarning);
    }
  };

  const handleCancel = () => {
    setStatusChangeActive(false);
    register.clearSelectedRowData();
  };

  return (
    <>
      <Button
        loading={isSavingStatuses}
        onClick={handleOk}
        className={styles.savingStatuses}
      >
        {t.global.pay}
      </Button>
      <Button
        loading={isSavingStatuses}
        onClick={handleCancelPayment}
        className={styles.cancelPayment}
        danger
      >
        {t.global.cancelPayment}
      </Button>
      <Button
        onClick={handleCancel}
        className={styles.cancelStatuses}
      >
        {t.global.reset}
      </Button>
    </>
  );
};
