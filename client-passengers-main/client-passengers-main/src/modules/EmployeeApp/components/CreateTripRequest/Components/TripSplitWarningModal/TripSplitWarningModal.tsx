import React from 'react';
import { useHistory } from 'react-router-dom';

import { Button, Modal } from '@sber-sbertransport/ui-kit/src';

import { TRIPS_LIST_FINAL, TRIPS_LIST_PLANNED } from 'constants/constants.routes';
import { TripStatusesFinal, TTripRequestStatuses } from 'modules/EmployeeApp/TripRequestStatuses.constants';
import { ACCEPT_TEXT, DECLINE_TEXT, MODAL_TITLE } from './constants';

import { ReactComponent as CrossIcon } from 'shared/icons/cross-icon.svg';

import { TripSplitWarningModalProps } from './types';

import styles from './styles.module.scss';

export const TripSplitWarningModal = ({
  open,
  humanReadableId,
  id,
  status,
  onAccept,
  onDecline,
}: TripSplitWarningModalProps) => {
  const history = useHistory();

  const handleRedirect = () => {
    const isFinalTrip = TripStatusesFinal.includes(status as TTripRequestStatuses);

    const redirectUrl = isFinalTrip ? TRIPS_LIST_FINAL : TRIPS_LIST_PLANNED;

    history.replace(`${redirectUrl}/${id}`);
  };

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
      Похоже, вы пытаетесь разделить одну поездку на несколько отдельных заказов. Рекомендуем вам отменить заявку
      <br />
      <span className={styles.link} onClick={handleRedirect}>
        {humanReadableId}
      </span>
      , после чего создать одну общую поездку, указав сразу все необходимые адреса
    </Modal>
  );
};
