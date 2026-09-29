import React, { Dispatch, SetStateAction } from 'react';
import styles from './ActiveStatusChangeButtons.module.scss';
import { Button } from 'antd';
import { useTranslation } from 'i18n';
import { StoreNames } from 'stores';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import type { PaymentStateResponse, YandexTaxiUpdateStatusesParams } from 'api/yandexTaxiRegistry/yandex-taxi-registry.types';
import { YandexTaxiRequestStatus, YandexTaxiRequestStatusTitles } from 'api/yandexTaxiRegistry/yandex-taxi-registry.constants';

export interface SelectedRowData {
  requestId: string;
  payed: boolean;
}

export const ActiveStatusChangeButtons = ({
  setStatusChangeActive,
  isSavingStatuses,
  savePaymentStatuses,
  handleStatusUpdate,
  selectedRowData,
  onFinish,
}: {
  setStatusChangeActive: Dispatch<SetStateAction<boolean>>;
  selectedRowData: SelectedRowData[];
  // savePaymentStatuses: (data: { status: string; uuid: string }) => Promise<PaymentStateResponse | undefined>;
  savePaymentStatuses: (data: YandexTaxiUpdateStatusesParams) => Promise<unknown>;
  isSavingStatuses: boolean;
  //   handleStatusUpdate: (data: PaymentStateResponse) => void;
  handleStatusUpdate: (data: PaymentStateResponse) => void;
  onFinish: () => void;
}) => {
  const { t } = useTranslation();
  const { [StoreNames.registryStore]: register, logger } = useAppStoreContext();

  const handleOk = () => {
    const paidSelectedRowData = selectedRowData
      .filter(x => x.payed);

    const paymentStatuses = paidSelectedRowData
      .map(x => savePaymentStatuses({ requestId: x.requestId, status: YandexTaxiRequestStatus.PAYMENT_DONE })
      );

    if (paidSelectedRowData.length) {
      Promise.all(paymentStatuses)
        .then(() => {
          !!paidSelectedRowData.length && handleStatusUpdate(
            paidSelectedRowData
              .map(item => ({ requestId: item.requestId, status: YandexTaxiRequestStatusTitles.PAYMENT_DONE }))
          );

          setTimeout(() => {
            onFinish();
          }, 1000);
        })
        .then(() => setStatusChangeActive(false))
        .catch(() => logger.toMessage('error', t.Tariffs.InternalServerError));
    } else {
      logger.toMessage('warning', t.Forms.registryFilterFields.emptyStatusesWarning);
    }
  };

  const handleCancelPayment = () => {
    const paidSelectedRowData = selectedRowData.filter(x => x.payed);
    const paymentStatuses = paidSelectedRowData
      .map(x => savePaymentStatuses({ requestId: x.requestId, status: YandexTaxiRequestStatus.PAYMENT_NOT_DONE })
      );

    if (paidSelectedRowData.length) {
      Promise.all(paymentStatuses)
        .then(() => {
          !!paidSelectedRowData.length && handleStatusUpdate(
            paidSelectedRowData
              .map(item => ({ requestId: item.requestId, status: YandexTaxiRequestStatusTitles.PAYMENT_NOT_DONE }))
          );

          setTimeout(() => {
            onFinish();
          }, 1000);
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
      <Button onClick={handleCancel} className={styles.cancelStatuses}>
        {t.global.reset}
      </Button>
    </>
  );
};
