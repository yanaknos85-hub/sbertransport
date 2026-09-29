import React, { FC, ReactNode } from 'react';
import { useParams } from 'react-router-dom';
import { useHistory as History } from '@sber-sbertransport/mf-core';
import { notification } from 'antd';
import { useUiContext } from 'shared/components/UI';
import { useModalState } from 'shared/hooks/useModal';
import TButton from 'shared/ui/Button/Button';

import { useTakeToWork } from 'modules/Exchange/api/exchange';

import { DetailedViewExchange, Statuses } from '../../../types';
import { AcceptedModal } from '../../Modals/AcceptedModal/AcceptedModal';
import { DeclineModal } from '../../Modals/DeclineModal/DeclineModal';

import styles from './Button.module.scss';

interface Props {
  request: DetailedViewExchange;
}

export const Button: FC<Props> = props => {
  const { request } = props;

  const { type, id: orderId } = useParams<{ type: string; id: string }>();
  const history = History();
  const { isMobile } = useUiContext();
  const [modal, modalActions] = useModalState();

  const [takeToWork] = useTakeToWork();

  const goBack = () => {
    history.goBack();
  };

  const handleTakeToWork = async (id: string) => {
    // взять в работу
    await takeToWork({ id })
      .then(() => {
        modalActions.show();
        goBack();
      })
      .catch(() => {
        notification.error({
          message: 'Создание доставки!',
          description: 'Что-то пошло не так',
        });
      });
  };

  const handleModal = () => {
    modalActions.show();
  };

  let buttonText = '';
  let buttonAction;
  let component: ReactNode;

  switch (type) {
    // доступная заявка
    case 'available':
      buttonText = 'Взять в работу';
      buttonAction = () => handleTakeToWork(orderId);
      component = (
        <AcceptedModal
          visible={modal}
          onCancel={modalActions.hide}
        />
      );
      break;
    // заявка в работе
    case 'non_terminal':
      buttonText = 'Отказаться';
      buttonAction = () => handleModal();
      component = (
        <DeclineModal
          visible={modal}
          onCancel={modalActions.hide}
        />
      );
      break;
    default:
      buttonText = '';
  }

  return (
    <>
      {request.status !== Statuses.CARGO_CANCELED && (
        <div className={styles.wrapper}>
          <TButton
            type="text"
            size="small"
            style={{
              width: isMobile ? 'auto' : '146px',
              padding: isMobile ? '0px 8px' : '0px',
              height: isMobile ? '28px' : '40px',
              borderRadius: '8px',
              marginRight: '8px',
              fontSize: '14px',
              fontWeight: '600',
            }}
            onClick={buttonAction}
          >
            {buttonText}
          </TButton>
        </div>
      )}
      {component}
    </>
  );
};
