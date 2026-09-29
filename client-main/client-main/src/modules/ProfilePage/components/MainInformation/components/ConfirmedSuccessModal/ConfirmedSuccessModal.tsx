import React, { FC } from 'react';
import { Modal } from '@sber-sbertransport/ui-kit/src';

import { ReactComponent as SuccessIcon } from 'shared/icons/success.svg';
import styles from './ConfirmedSuccessModal.module.scss';

interface ConfirmedSuccessModalProps {
  visible: boolean;
  onClose: () => void;
}

const ConfirmedSuccessModal: FC<ConfirmedSuccessModalProps> = ({ visible, onClose }) => (
  <Modal
    open={visible}
    onCancel={onClose}
    closeIcon={null}
    width={400}
    footer={null}
    className={styles.modal}
  >
    <div className={styles.content}>
      <SuccessIcon />
      <p className={styles.title}>Номер телефона изменен</p>
    </div>
  </Modal>
);

export default ConfirmedSuccessModal;
