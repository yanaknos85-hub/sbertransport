import React from 'react';
import Modal from 'antd/lib/modal/Modal';
import { SpinWrapped } from 'shared/components';
import { useModalState } from 'shared/hooks/useModal';

import { SavedFileInfo } from 'stores/Trip/Trip.interface';

import styles from './styles.module.scss';

const CompensationImage = ({
  inProgress,
  imageUrl,
  document,
}: {
  inProgress: boolean;
  imageUrl: string;
  document?: SavedFileInfo;
}): JSX.Element => {
  const [modal, modalActions] = useModalState();

  return (
    <div className={styles.compensationImageWrapper}>
      {inProgress ? (
        <SpinWrapped className={styles.compensationImageSpinner} />
      ) : (
        <>
          <button
            type="button"
            onClick={modalActions.show}
            className={styles.compensationImageButton}
          >
            <img
              className={styles.compensationImage}
              src={imageUrl}
              alt={document?.fileName}
            />
          </button>
          <Modal
            title={document?.fileName}
            open={modal}
            onCancel={modalActions.hide}
            footer={null}
          >
            <img
              className={styles.compensationImageFullScreen}
              src={imageUrl}
              alt={document?.fileName}
            />
          </Modal>
        </>
      )}
    </div>
  );
};

export default CompensationImage;
