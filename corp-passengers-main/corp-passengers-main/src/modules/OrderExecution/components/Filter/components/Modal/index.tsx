import React, { FC } from 'react';
import classNames from 'classnames';
import { Modal as AntDesignModal } from 'antd';
import { ModalProps as LegacyModalProps } from 'antd/lib/modal';
import styles from './modal.module.scss';
import CloseIcon from '../../../Icon/CloseIcon';

interface ModalProps extends Partial<LegacyModalProps> {
  onClose?: () => void;
}

const Modal: FC<ModalProps> = ({ onClose, ...props }) => (
  <AntDesignModal
    closeIcon={(
      <span onClick={onClose}>
        <CloseIcon />
      </span>
    )}
    className={classNames(styles.filterModal, props.className)}
    {...props}
  />
);
export default Modal;
