import React, { ReactNode } from 'react';
import { Modal } from 'antd';

import styles from './CargoModal.module.scss';

interface ModalFormProps {
  visible: boolean;
  onCancel: () => void;
  title: string;
  children: ReactNode;
}

const CargoModal: React.FC<ModalFormProps> = ({
  visible, onCancel, title, children,
}) => (
  <Modal
    title={title}
    open={visible}
    onCancel={onCancel}
    centered
    closable
    keyboard
    width={400}
    className={styles.modal}
    footer={null}
  >
    {children}
  </Modal>
);

export default CargoModal;

