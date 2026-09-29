import React, { FC } from 'react';

import { UUID } from 'utils/io-ts';
import { useAppStoreContext } from 'shared/hooks/useEmpContext';
import { GET_YANDEX_TAXI_REGISTRY_ITEM_RECEIPT } from 'api/yandexTaxi/yandex-taxi.constants';
import { handleClick as downloadFile } from '../../../utils';
import { Icon } from '../../Icon/Icon';

import styles from './styles.module.scss';

const MIME_TYPE = 'application/pdf';

export interface YandexTaxiReceiptProps {
  fileName: string;
  requestId: UUID;
}

export const Receipt: FC<YandexTaxiReceiptProps> = ({ fileName, requestId }) => {
  const { http } = useAppStoreContext();

  const downloadReceipt = (e: React.MouseEvent) => {
    e.preventDefault();
    http
      .get<ArrayBuffer>(
        GET_YANDEX_TAXI_REGISTRY_ITEM_RECEIPT,
        {
          urlParams: { id: requestId },
          responseType: 'arraybuffer',
        }
      )
      .then(response => {
        if (response) {
          const { data } = response;
          const reader = new FileReader();
          reader.readAsText(new Blob([data], { type: MIME_TYPE }));
          reader.onload = () => {
            if (typeof reader.result === 'string') {
              downloadFile(data, MIME_TYPE, fileName);
            }
          };
        }
      });
  };

  return (
    <div className={styles.receiptWrapper} onClick={downloadReceipt}>
      <div className={styles.receiptIcon}>
        <Icon type="pdf" />
      </div>
      <div className={styles.receiptName}>
        {`Чек ${fileName}`}
      </div>
    </div>
  );
};
