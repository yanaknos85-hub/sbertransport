import Button, { ButtonProps } from 'antd/lib/button';
import { useTranslation } from 'i18n';
import React, { FC } from 'react';
import cn from 'classnames';
import { DownOutlined } from '@ant-design/icons';
import styles from './DefaultSorting.module.scss';

export const DefaultSorting: FC<ButtonProps> = ({ className, ...props }) => {
  const { t } = useTranslation();

  return (
    <Button {...props} className={cn(className, styles.buttonSort)}>
      {t.global.defaultSorting}
      <DownOutlined />
    </Button>
  );
};
