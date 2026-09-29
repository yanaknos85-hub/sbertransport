import React, { FC, ReactNode } from 'react';
import { Icon } from 'shared/components/Icon';
import cn from 'classnames';
import styles from './ButtonLoadVsp.module.scss';
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

export const ButtonLoadVsp: FC<Props> = ({
  type,
  onClick,
  children,
  className,
  theme = 'primary',
  color = '#909090',
  isLoading = false,
  disabled = false,
}) => {
  const isDisabled = disabled || isLoading;

  return theme === 'primary' ? (
    <button
      type="button"
      className={cn(styles.button, className, {
        [styles.disabled]: isDisabled,
      })}
      onClick={isDisabled ? ignore : onClick}
      disabled={isDisabled}
    >
      {isLoading ? (
        <Spin size="small" />
      ) : (
        <>
          <Icon color={color} type={type} />
          {children}
        </>
      )}
    </button>
  ) : (
    <Button
      className={cn(className, { [styles.disabled]: isDisabled })}
      onClick={isDisabled ? ignore : onClick}
      type="primary"
      disabled={isDisabled}
    >
      {isLoading ? <Spin /> : children}
    </Button>
  );
};
