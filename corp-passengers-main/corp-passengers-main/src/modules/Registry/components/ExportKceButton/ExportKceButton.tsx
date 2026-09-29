import Button, { ButtonProps } from 'antd/lib/button';
import React, { FC } from 'react';
import { Icon } from 'shared/components/Icon';
import styles from './ExportKceButton.module.scss';

export const ExportKceButton: FC<ButtonProps> = props => (
  <Button
    {...props}
    className={styles.button}
    type="primary"
    icon={<Icon type="import" />}
  >
    KCE
  </Button>
);
