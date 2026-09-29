import React from 'react';
import { Modal } from 'antd';

import styles from '../static/Cargos.module.scss';

interface ModalFormProps {
  visible: boolean;
  onCancel: () => void;
  cancelOrder: () => void;
  title: string;
}

const CancelModal: React.FC<ModalFormProps> = ({
  visible, onCancel, cancelOrder, title,
}) => (
  <Modal
    title={title}
    open={visible}
    onOk={cancelOrder}
    onCancel={onCancel}
    centered={true}
    closable={true}
    keyboard={true}
    width={400}
    className={styles.modal}
    okText="Удалить"
    cancelText="Отмена"
  >
    После удаления запрос на доставку будет отменён, а списанные средства вернутся обратно на счёт лимита.
  </Modal>
);

export default CancelModal;
