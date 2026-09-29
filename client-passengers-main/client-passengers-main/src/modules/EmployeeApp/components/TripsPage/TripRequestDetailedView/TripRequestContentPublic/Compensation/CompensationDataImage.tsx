import Modal from 'antd/lib/modal/Modal';
import React from 'react';

import { SpinWrapped } from 'shared/components';
import { useModalState } from 'shared/hooks/useModal';
import { SavedFileInfo, UsedFileFormatEnum } from 'stores/Trip/Trip.interface';
import DocumentUpload from 'shared/components/Images/documentUpload.svg';

import styles from './styles.module.scss';

const CompensationDataImage = ({
  inProgress,
  imageUrl,
  documentData,
}: {
  inProgress: boolean;
  imageUrl: string;
  documentData?: SavedFileInfo;
}): JSX.Element => {
  const [modal, modalActions] = useModalState();

  const handleDownload = () => {
    if (imageUrl && documentData?.fileFormat === UsedFileFormatEnum.pdf && documentData.fileName) {
      const url = imageUrl;
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', documentData.fileName);
      document.body.appendChild(link);
      link.click();
    } else {
      modalActions.show();
    }
  };

  return (
    <>
      {imageUrl
      && (
      <div className={styles.compensationImageWrapper}>
        {inProgress ? (
          <SpinWrapped className={styles.compensationImageSpinner} />
        ) : (
          <>
            <button
              type="button"
              onClick={handleDownload}
              className={styles.compensationImageButton}
            >
              <img src={DocumentUpload} alt="DocumentUpload" />
              <span>Подтверждающий документ</span>
            </button>
            <Modal
              title={documentData?.fileName}
              visible={modal}
              onCancel={modalActions.hide}
              footer={null}
            >
              <img
                className={styles.compensationImageFullScreen}
                src={imageUrl}
                alt={documentData?.fileName}
              />
            </Modal>
          </>
        )}
      </div>
      )}
    </>
  );
};

export default CompensationDataImage;
