import { Button } from 'antd';
import React, { FC } from 'react';
import { useTranslation } from 'i18n';
import styles from '../Modal.module.scss';

interface OwnProp { handlerCancel: () => void }

export const UploadResultButton: FC<OwnProp> = ({ handlerCancel }): JSX.Element => {
  const { t } = useTranslation();
  return (
    <div className={styles.showModalButton}>
      <Button className={styles.showButton} onClick={handlerCancel}>
        {t.global.closingConfirmation}
      </Button>
    </div>
  );
};
