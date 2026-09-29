import { FC } from 'react';

import { Contacts } from 'constants/app.constants';
import Modal from 'components/Modal/Modal';
import styles from './AccountLockout.module.scss';

interface ModalProps {
  visible: boolean;
  onClose: () => void;
}

export const AccountLockoutModal: FC<ModalProps> = props => (
  <Modal
    title={`Ваша учетная запись заблокирована.\nПожалуйста, обратитесь в службу поддержки!`}
    content={(
      <div className={styles.container}>
        <a href={Contacts.linkOutTel} className={styles.link}>
          {Contacts.outTel}
        </a>
      </div>
    )}
    {...props}
  />
);
