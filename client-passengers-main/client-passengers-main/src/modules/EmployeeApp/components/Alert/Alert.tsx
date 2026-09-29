import React from 'react';
import type { FC } from 'react';
import { Alert as AlertAntd, AlertProps } from 'antd';
import cn from 'classnames';

import { ReactComponent as WarningIcon } from 'shared/components/Images/warning.svg';
import { ReactComponent as ErrorIcon } from 'shared/components/Images/error.svg';
import styles from './styles.module.scss';

interface Props {
  type: AlertProps['type'];
  message?: AlertProps['message'];
  description: AlertProps['description'];
  showIcon?: AlertProps['showIcon'];
  icon?: AlertProps['icon'];
  className?: string;
}

const Alert: FC<Props> = ({
  type,
  message,
  description,
  showIcon,
  icon,
  className,
}) => (
  <AlertAntd
    className={cn(styles.alert, className)}
    type={type ?? 'info'}
    showIcon={!!showIcon}
    icon={icon
    ?? type === 'warning' ? <WarningIcon />
      : type === 'error' ? <ErrorIcon /> : null}
    message={message}
    description={description}
  />
);

export default Alert;
