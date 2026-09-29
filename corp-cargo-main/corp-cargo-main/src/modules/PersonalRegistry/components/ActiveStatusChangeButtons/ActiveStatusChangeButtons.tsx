import { Button } from 'antd';
import React, { Dispatch, FC, SetStateAction } from 'react';
import { useTranslation } from 'i18n';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { PaymentStateResponse } from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { SelectedRowData } from '../../hooks/useRowSelection';

interface Props {
  setStatusChangeActive: Dispatch<SetStateAction<boolean>>;
  selectedRowData: SelectedRowData[];
  savePaymentStatuses: (data: SelectedRowData[]) => Promise<PaymentStateResponse | undefined>;
  isSavingStatuses: boolean;
  handleStatusUpdate: (data: PaymentStateResponse) => void;
}

export const ActiveStatusChangeButtons: FC<Props> = ({
  setStatusChangeActive,
  selectedRowData,
  savePaymentStatuses,
  isSavingStatuses,
  handleStatusUpdate,
}) => {
  const { t } = useTranslation();
  const { logger } = useAppStoreContext();

  const handleOk = () => {
    const paidSelectedRowData = selectedRowData.filter(x => x.payed);

    if (paidSelectedRowData.length) {
      savePaymentStatuses(paidSelectedRowData)
        .then(data => data && handleStatusUpdate(data))
        .then(() => setStatusChangeActive(false))
        .catch(() => logger.toMessage('error', t.Tariffs.InternalServerError));
    } else {
      logger.toMessage('warning', t.Forms.registryFilterFields.emptyStatusesWarning);
    }
  };

  const handleCancel = () => {
    setStatusChangeActive(false);
  };

  return (
    <>
      <Button
        type="primary"
        loading={isSavingStatuses}
        onClick={handleOk}
      >
        {t.global.save}
      </Button>
      <Button danger onClick={handleCancel}>
        {t.global.cancel}
      </Button>
    </>
  );
};
