import React, { FC, useState } from 'react';
import moment from 'moment';
import { useAppStoreContext } from 'shared/hooks/useAppStoreContext';
import { useTranslation } from 'i18n';

import { RcFile } from 'antd/lib/upload/interface';
import { Modal } from 'antd';
import {
  DataForm, updateUploadRegistryXLS, uploadRegistryXLS, useCheckUploadedFile
} from 'api/reports';
import { capitalize } from '../../utils';

import styles from './Modal.module.scss';
import { Footer } from './Footer/Footer';
import { UploadResultButton } from './Footer/UploadResultButton ';

interface OwnProps {
  visible: boolean;
  setVisible(visible: boolean): void;
  setMenuVisible(visible: boolean): void;
  modalData: { contractorId: string; monthName: string; contractorName: string };
}

export const UploadXLSModal: FC<OwnProps> = ({
  visible, setVisible, setMenuVisible, modalData,
}) => {
  const { http, process } = useAppStoreContext();

  const {
    contractorId, monthName, contractorName,
  } = modalData;

  const [uploadButtonDisable, setUploadButtonDisable] = useState<boolean>(false);

  const [currentUploadStatus, setCurrentUploadStatus] = useState({
    upload: false, success: false, error: false,
  });

  const [invalidCell, setInvalidCell] = useState<string[]>([]);

  const [fileList, setFileList] = useState<RcFile[]>([]);

  const { t } = useTranslation();

  const currentYear = Number(moment().format('YYYY'));

  const dateGeneration = `${currentYear}-${moment().month(monthName).format('MM')}-01`;

  const status: DataForm | null = useCheckUploadedFile(modalData.contractorId, dateGeneration).data;

  const handlerCancel = () => {
    setVisible(false);
    setTimeout(() => setMenuVisible(true), 300);
  };

  const modalTitle = (length?: number, status?: DataForm | null): string => {
    if (status !== null && !currentUploadStatus.upload && !currentUploadStatus.success) {
      return t.Forms.RegistryXLSModal.showReplace;
    }

    if (currentUploadStatus.upload && currentUploadStatus.success) {
      return t.Forms.RegistryXLSModal.textDownloadSuccess;
    }

    if (!length) {
      return t.Forms.RegistryXLSModal.selectFileToDownload;
    }

    return `${t.Forms.RegistryXLSModal.showDownload} "${contractorName}"/${capitalize(`${monthName}${currentYear}`)}`;
  };

  const handleUpload = () => {
    setUploadButtonDisable(true);

    const formData = new FormData();
    formData.append(
      'request',
      new Blob(
        [
          JSON.stringify({
            contractorId,
            date: new Date(dateGeneration).toISOString(),
          }),
        ],
        {
          type: 'application/json',
        }
      )
    );
    if (status !== null) {
      formData.delete('request');
      formData.append('file', fileList[0] as Blob);
      return updateUploadRegistryXLS(http, process, status.id, formData);
    }
    formData.append('file', fileList[0] as Blob);
    return uploadRegistryXLS(http, process, formData);
  };

  const handlerClick = () => {
    handleUpload()
      .then(res => !res.valid
        ? (setCurrentUploadStatus({
          ...currentUploadStatus, upload: true, success: true,
        }),
        setInvalidCell(res.incorrectTypeCells))
        : handlerCancel()
      )
      .catch(() => setCurrentUploadStatus({ ...currentUploadStatus, error: true }));
  };

  const modalSuccessTitle = () => (
    <>
      В ячейках:
      {' '}
      <span className={styles.showModalSubtitle}>{invalidCell.join(',')}</span>
      {' '}
      содержание не соответствует
      ожидаемому типу данных
    </>
  );

  const modalErrorTitle = () => (
    <>
      В шапке или названиях ячеек были выявлены не соответствия заявленному шаблону
      <a href="/Реестр TEST.xlsx" download>
        &nbsp; Скачать шаблон
      </a>
    </>
  );
  return (
    <Modal
      visible={visible}
      className={(invalidCell.length && currentUploadStatus.upload) || currentUploadStatus.error ? '' : styles.modal}
      onCancel={handlerCancel}
      footer={[
        !currentUploadStatus.error ? (
          <Footer
            key={`upload-${modalData.contractorId}`}
            uploadStatus={currentUploadStatus}
            status={status}
            fileList={fileList}
            setFileList={setFileList}
            handlerClick={handlerClick}
            uploadButtonDisable={uploadButtonDisable}
            handlerCancel={handlerCancel}
          />
        ) : (
          <UploadResultButton key={`upload-${modalData.contractorId}`} handlerCancel={handlerCancel} />
        ),
      ]}
    >
      <p className={styles.showModalTitle}>
        {invalidCell.length && currentUploadStatus.upload
          ? modalSuccessTitle()
          : currentUploadStatus.error
            ? modalErrorTitle()
            : modalTitle(fileList.length, status)}
      </p>
    </Modal>
  );
};
