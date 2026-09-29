import React, { FC } from 'react';
import { useParams } from 'react-router-dom';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import cn from 'classnames';
import TButton from 'shared/ui/Button/Button';
import TModal from 'shared/ui/Modal/Modal';

import { useDeny } from '../../../api/exchange';
import { ReactComponent as CloseIcon } from '../static/closeIcon.svg';

import styles from './DeclineModal.module.scss';

interface Props {
  visible: boolean;
  onCancel: () => void;
}

export const DeclineModal: FC<Props> = props => {
  const { visible, onCancel } = props;
  const { id } = useParams<{ type: string; id: string }>();
  const [deny] = useDeny();

  const history = History();

  const handleDeny = () => {
    deny({ id }).then(() => {
      onCancel();
      history.goBack();
    });
  };

  return (
    <TModal
      width={480}
      height={150}
      visible={visible}
      onCancel={onCancel}
      footer={null}
      title={null}
      closeIcon={<CloseIcon />}
    >
      <div className={styles.wrapper}>
        <p className={styles.title}>
          Отказаться от исполнения заявки?
        </p>
        <div className={styles.buttonsWrapper}>
          <TButton
            className={cn(styles.button, styles.cancel)}
            $size="small"
            onClick={() => onCancel()}
          >
            Отменить
          </TButton>
          <TButton
            className={cn(styles.button, styles.declineButton)}
            $size="small"
            onClick={() => handleDeny()}
          >
            Отказаться
          </TButton>
        </div>
      </div>
    </TModal>
  );
};
