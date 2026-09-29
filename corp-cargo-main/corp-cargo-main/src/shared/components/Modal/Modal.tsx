import { Modal as ModalAntd } from 'antd';
import React, { FC } from 'react';
import cn from 'classnames';
import { ModalProps } from 'antd/lib/modal';
import styles from './Modal.module.scss';
import { Icon } from '../Icon';

type TModal = ModalProps & { className?: string };

export const Modal: FC<TModal> = ({
  className, children, okButtonProps, cancelButtonProps, ...props
}) => (
  <ModalAntd
    className={cn(styles.Modal, className)}
    closeIcon={<Icon type="closeModal" className={styles.ModalIcon} />}
    okButtonProps={{ className: styles.okButton, ...okButtonProps }}
    cancelButtonProps={{ className: styles.cancelButton, ...cancelButtonProps }}
    {...props}
  >
    {children}
  </ModalAntd>
);
