/* eslint-disable no-unsafe-optional-chaining */
import { Modal } from 'antd';
import { observer } from 'mobx-react';
import React, { FC } from 'react';

import Close from 'shared/components/Images/Close.svg';

import styles from './reloadTripModal.module.scss';
import '../../override.scss';

interface ReloadTripModalProps {
  visible: boolean;
  onCancel: () => void;
  reloadTrip: () => void;
  setVisibleRepeatRouteAndAtriumModal: React.Dispatch<React.SetStateAction<boolean>>;
}

export const ReloadTripModal: FC<ReloadTripModalProps> = observer(
  ({
    visible,
    onCancel,
    reloadTrip,
    setVisibleRepeatRouteAndAtriumModal,
  }): JSX.Element => {
    const repeatRouteAndAtriumModal = () => {
      onCancel();
      setVisibleRepeatRouteAndAtriumModal(true);
    };

    return (
      <div onClick={e => e.stopPropagation()}>
        <Modal
          open={visible}
          onCancel={onCancel}
          className={styles.reloadTripModal}
          footer={false}
        >
          <div className="cardWrapper">
            <div className="header">
              <div className="numberApplication">
                Повторить заявку ?
              </div>
              <div className="cancel" onClick={onCancel}>
                <img alt="Close" src={Close} />
              </div>
            </div>
            <div className={styles.reloadTripModal_wrapper__buttonn}>
              <div className={styles.reloadTripModal_button} onClick={repeatRouteAndAtriumModal}>
                Повторить маршрут и атрибуты
              </div>
              <div className={styles.reloadTripModal_button} onClick={reloadTrip}>
                Повторить только маршрут
              </div>
            </div>
          </div>
        </Modal>
      </div>
    );
  }
);
