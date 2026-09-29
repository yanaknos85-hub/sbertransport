import Button, { ButtonProps } from 'antd/lib/button';
import React, { FC } from 'react';
import { Icon } from 'shared/components/Icon';
import styles from './ExportButton.module.scss';

export const ExportButton: FC<ButtonProps> = props => (
  <Button
    {...props}
    className={styles.button}
    icon={<Icon type="import" />}
  />
);
