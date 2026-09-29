import React, { FC } from 'react';
import TModal from 'shared/ui/Modal/Modal';

import { ReactComponent as CloseIcon } from '../static/closeIcon.svg';
import { ReactComponent as OkIcon } from '../static/okIcon.svg';

import styles from './AcceptedModal.module.scss';

interface Props {
  visible: boolean;
  onCancel: () => void;
}

export const AcceptedModal: FC<Props> = props => {
  const { visible, onCancel } = props;

  return (
    <TModal
      width={400}
      visible={visible}
      onCancel={onCancel}
      closeIcon={<CloseIcon />}
      footer={null}
    >
      <div className={styles.wrapper}>
        <OkIcon />
        <p className={styles.title}>Заявка взята в работу</p>
        <p className={styles.text}>
          Отслеживайте изменение статуса
          в разделе «Заявки в работе»
        </p>
      </div>
    </TModal>
  );
};
