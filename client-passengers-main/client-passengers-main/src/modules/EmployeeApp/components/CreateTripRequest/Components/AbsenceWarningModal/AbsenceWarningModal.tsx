import React from 'react';

import { Button, Modal } from '@sber-sbertransport/ui-kit/src';

import { ReactComponent as CrossIcon } from 'shared/icons/cross-icon.svg';
import exclamationIcon from 'shared/icons/exclamation-icon.png';

import {
  ACCEPT_TEXT, DECLINE_TEXT, EXCLAMATION_ICON_ALT, MODAL_TITLE
} from './constants';
import { AbsenceWarningModalProps } from './types';
import styles from './styles.module.scss';

export const AbsenceWarningModal = ({
  open,
  onAccept,
  onDecline,
}: AbsenceWarningModalProps) => {
  const ModalFooter = () => {
    return (
      <div className={styles.footer}>
        <Button type="text" onClick={onDecline}>
          {DECLINE_TEXT}
        </Button>
        <Button type="primary" onClick={onAccept}>
          {ACCEPT_TEXT}
        </Button>
      </div>
    );
  };

  return (
    <Modal
      open={open}
      centered
      title={MODAL_TITLE}
      className={styles.modal}
      footer={ModalFooter}
      onCancel={onAccept}
      closeIcon={<CrossIcon />}
    >
      <img src={exclamationIcon} alt={EXCLAMATION_ICON_ALT} />
      <p>На выбранную дату и время пассажир в отпуске/на больничном</p>
    </Modal>
  );
};
