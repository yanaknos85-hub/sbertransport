import Button, { ButtonProps } from 'antd/lib/button';
import { useTranslation } from 'i18n';
import React, { FC } from 'react';
import { Icon } from 'shared/components/Icon';

import styles from './ExportXlsButton.module.scss';

export const ExportXlsButton: FC<ButtonProps & { caption?: string }> = props => {
  const { t: { Forms: { RegistryXLSModal } } } = useTranslation();

  return (
    <Button
      {...props}
      className={styles.button}
      type="primary"
      icon={<Icon type="import" />}
    >
      {props.caption || RegistryXLSModal.defaultButtonCaption}
    </Button>
  );
};
