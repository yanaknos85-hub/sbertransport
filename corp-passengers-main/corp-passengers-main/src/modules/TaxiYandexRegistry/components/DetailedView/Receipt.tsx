import React, { FC, useState, useEffect } from 'react';
import { FilePdfOutlined, EyeOutlined, EyeInvisibleOutlined } from '@ant-design/icons';
import { pdfjs } from 'react-pdf';
import { Document, Page } from 'react-pdf';

import { UUID } from 'utils/io-ts';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { GET_YANDEX_TAXI_REGISTRY_ITEM_RECEIPT } from 'api/yandexTaxiRegistry/yandex-taxi-registry.constants';
import { handleClick as downloadFile } from 'utils/downloads';

import styles from './styles.module.scss';

const MIME_TYPE = 'application/pdf';
export interface YandexTaxiReceiptProps {
  fileName: string;
  requestId: UUID;
}

export const Receipt: FC<YandexTaxiReceiptProps> = ({ fileName, requestId }) => {
  const [isVisible, setIsVisible] = useState(false);
  const [isLoad, setIsLoad] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [pdfBlob, setPdfBlob] = useState<ArrayBuffer | Blob | string | null>(null);
  const [pdfData, setPdfData] = useState<ArrayBuffer | null>(null);
  const { http } = useAppStoreContext();

  pdfjs.GlobalWorkerOptions.workerSrc = `//unpkg.com/pdfjs-dist@${pdfjs.version}/build/pdf.worker.min.mjs`;

  const downloadPDF = (pdfData: ArrayBuffer | null) => {
    if (pdfData) {
      const reader = new FileReader();
      reader.readAsText(new Blob([pdfData], { type: MIME_TYPE }));
      reader.onload = () => {
        if (typeof reader.result === 'string') {
          downloadFile(pdfData, MIME_TYPE, fileName);
        }
      };
    }
  };

  const loadReceipt = () => {
    setIsLoading(true);
    return http
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
          const pdfSrc = URL.createObjectURL(new Blob([data], { type: MIME_TYPE }));
          setPdfBlob(pdfSrc);
          setPdfData(data);
        }
      })
      .finally(() => setIsLoading(false));
  };

  const handleView = (e: React.MouseEvent) => {
    e.preventDefault();
    if (!pdfBlob && !isLoading) {
      loadReceipt().then(() => setIsVisible(true));
    } else {
      setIsVisible(prev => !prev);
    }
  };

  const handleDownload = (e: React.MouseEvent) => {
    e.preventDefault();
    if (!pdfData && !isLoading) {
      loadReceipt().then(() => setIsLoad(true));
    } else {
      setIsLoad(true);
    }
  };

  useEffect(
    () => {
      if (isLoad && pdfData) {
        downloadPDF(pdfData);
      }
    },
    [isLoad]
  );

  return (
    <div className={styles.receiptWrapper}>
      <div className={styles.iconTitle}>Чек ЯндексGo</div>
      <div className={styles.buttons}>

        <div className={styles.download} onClick={handleView}>
          {isVisible
            ? <EyeInvisibleOutlined className={styles.viewIcon} />
            : <EyeOutlined className={styles.viewIcon} />}
          <span className={styles.name}>Посмотреть</span>
        </div>

        <div className={styles.download} onClick={handleDownload}>
          <FilePdfOutlined className={styles.pdfIcon} />
          <span className={styles.name}>
            Скачать
            {fileName}
          </span>
        </div>

      </div>
      {pdfBlob && isVisible && (
        <Document file={pdfBlob} className={styles.pdf}>
          <Page pageNumber={1} />
        </Document>
      )}
    </div>
  );
};
