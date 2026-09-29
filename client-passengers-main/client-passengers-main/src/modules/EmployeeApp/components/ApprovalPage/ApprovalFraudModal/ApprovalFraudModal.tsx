import React, { Dispatch } from 'react';
import { Modal } from 'antd';
import TButton from 'shared/ui/Button/Button';
import { ReactComponent as CloseIcon } from 'shared/components/Images/closeCross.svg';

import styles from './ApprovalFraudModal.module.scss';

interface ApprovalFraudModalProps {
  comment: string | string[];
  open: boolean;
  onClose: Dispatch<unknown>;
}

export const ApprovalFraudModal = ({
  comment, open, onClose,
}: ApprovalFraudModalProps) => {
  const commentsList = Array.isArray(comment) ? comment : [comment];

  return (
    <Modal
      onCancel={onClose}
      open={open}
      title="Нарушение правил оформления поездки"
      closeIcon={<CloseIcon />}
      width={640}
      className={styles.modal}
      footer={[
        <TButton
          $size="small"
          key="cancel"
          onClick={onClose}
          style={{
            cursor: 'pointer',
            backgroundColor: '#F2F3F6',
            color: '#4D4D4D',
            borderColor: '#F2F3F6',
          }}
        >
          Закрыть
        </TButton>,
      ]}
    >
      <div>
        <p>
          Система антифрод обнаружила подозрительные признаки
        </p>
        <div className={styles.content}>
          <ul className={styles.list}>
            {commentsList.map((text, index) => <li key={index}>{text}</li>)}
          </ul>
        </div>
      </div>
    </Modal>
  );
};
