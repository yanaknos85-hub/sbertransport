import { Modal as AntdModal, ModalProps } from 'antd-mobile';
import cn from 'classnames';

import styles from './Modal.module.scss';

const Modal: React.FC<ModalProps> = ({
  className, closeOnMaskClick = true, showCloseButton = false, ...props
}) => (
  <AntdModal
    closeOnMaskClick={closeOnMaskClick}
    showCloseButton={showCloseButton}
    className={cn(styles.modal, [className])}
    {...props}
  />
);

export default Modal;
