import React, { FC, useEffect, useState } from 'react';
import { Modal } from 'antd';
import { useTranslation } from 'i18n';

import { useAPIQueryCache } from 'api';
import { useUploadVehicles } from 'api/vehicles/vehicles.api';
import { DISPATCHER_ROOM, VEHICLES_LOAD_FILE } from 'api/vehicles/vehicles.constants';

import { useModalForm } from '../../context/ModalForm';

import { Icon } from 'components/Icon/Icon';
import DownloadButton from 'components/DownloadButton';
import { Button } from 'components/Button';
import UploadFrame from 'components/UploadFrame/UploadFrame';

import { ImportReportSummary } from '../ImportReportSummary/ImportReportSummary';

import { ReactComponent as DraftArrow } from 'assets/icons/draft-arrow.svg';
import styles from './UploadModal.module.scss';

export const UploadModal: FC = () => {
  const { t } = useTranslation();
  const {
    stateShowModal, handleClose, handleOpenResult,
  } = useModalForm();

  const cache = useAPIQueryCache();

  const [uploadVehicles, { isLoading }] = useUploadVehicles();

  const [file, setFile] = useState<File | undefined>();
  const [uploadFinished, setUploadFinished] = React.useState(false);
  const [busy, setBusy] = React.useState(false);
  const [error, setError] = React.useState<Error | null>(null);

  const handleCancel = () => {
    handleClose();
    setError(null);
    setFile(undefined);
    setUploadFinished(false);
    setBusy(false);
  };

  const handleUpload = React.useCallback(() => {
    if (!file) return;

    setBusy(true);

    uploadVehicles({ file })
      .then(() => {
        setError(null);
      })
      .catch(err => {
        setError(err);
        setBusy(false);
      })
      .finally(() => {
        setUploadFinished(true);
      });
  }, [cache, file, uploadVehicles]);

  useEffect(() => {
    if (file) {
      handleUpload();
    } else if (uploadFinished && error) {
      setUploadFinished(false);
      setError(null);
    }
  }, [file]);

  const onResult = () => {
    handleCancel();
    handleOpenResult();
  };

  return (
    <Modal
      centered
      title={t.global.addXls}
      className={styles.modal}
      visible={stateShowModal.isOpen && stateShowModal.type === 'upload'}
      closeIcon={<Icon type="closeModal" className={styles.ModalIcon} />}
      onCancel={handleCancel}
      onOk={handleUpload}
      footer={null}
    >
      <div className={styles.content}>
        <div className={styles.banner}>
          <p>
            Сервис позволяет добавить множество автомобилей в пару кликов. Загрузите заполненный шаблон, и
            автомобили будут добавлены
          </p>
          <DraftArrow />
        </div>
      </div>
      <div className={styles.template}>
        <DownloadButton
          url={VEHICLES_LOAD_FILE}
          query={{ isSample: true }}
          directoryUrl={DISPATCHER_ROOM}
          customElement={(
            <Button type="text">
              <Icon color="#909090" type="import" />
              Скачать шаблон для заполнения
            </Button>
              )}
        />
      </div>

      {uploadFinished ? (
        error ? (
          <>
            <div className={styles.error}>
              {`Ошибка импорта: ${error.toString()}`}
            </div>
            <UploadFrame
              file={file}
              setFile={setFile}
              isLoading={isLoading}
            />
          </>
        ) : (
          <div>
            <ImportReportSummary setBusy={setBusy} />
          </div>
        )
      ) : (
        <UploadFrame
          file={file}
          setFile={setFile}
          isLoading={isLoading}
        />
      )}

      {uploadFinished && !error && (
        <Button
          className={styles.resultBtn}
          loading={busy}
          onClick={onResult}
        >
          Просмотреть результаты
        </Button>
      )}
    </Modal>
  );
};
