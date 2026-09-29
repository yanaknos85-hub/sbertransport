import Button, { ButtonProps } from 'antd/lib/button';
import { useTranslation } from 'i18n';
import React, { FC } from 'react';

import styles from './ExportButton.module.scss';

export const ExportButton: FC<ButtonProps & { caption?: string }> = props => {
  const { t: { Forms: { RegistryXLSModal } } } = useTranslation();

  return (
    <Button
      {...props}
      className={styles.button}
      type="primary"
    >
      {props.caption || RegistryXLSModal.generateRegisterForPayment}
    </Button>
  );
};
