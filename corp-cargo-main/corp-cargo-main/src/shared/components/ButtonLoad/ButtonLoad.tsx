import React, { FC, ReactNode } from 'react';
import { Icon } from 'shared/components/Icon';
import cn from 'classnames';
import styles from './ButtonLoad.module.scss';
import { Button } from '../Button/Button';
import { Spin } from 'antd';
import { ignore } from 'utils';

interface Props {
  type: 'export' | 'import';
  onClick?: () => void;
  theme?: 'primary' | 'secondary';
  className?: string;
  children?: ReactNode;
  color?: string;
  isLoading?: boolean;
  disabled?: boolean;
}

export const ButtonLoad: FC<Props> = ({
  type, onClick, children, className, theme = 'primary', color = '#909090', isLoading = false, disabled,
}) => theme === 'primary' ? (
  <button
    type="button"
    className={cn(styles.button, className, { [styles.disabled]: disabled })}
    onClick={isLoading ? ignore : onClick}
    disabled={disabled}
  >
    {isLoading ? <Spin size="small" /> : <Icon color={color} type={type} />}
  </button>
) : (
  <Button
    className={cn(className, { [styles.disabled]: disabled })}
    onClick={onClick}
    type="primary"
    disabled={disabled}
  >
    {children}
  </Button>
);
