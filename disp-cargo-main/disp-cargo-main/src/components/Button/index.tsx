import React, { FC } from 'react';
import { Button as AntdButton } from 'antd';
import { ButtonProps as OriginalProps } from 'antd/lib/button';
import cn from 'classnames';

import { ReactComponent as ControlIcon } from 'assets/icons/control.svg';
import styles from './index.module.scss';

interface ButtonProps extends OriginalProps {
  /** Счетчик фильтров. Отображается только при filter=true */
  counter?: number;
  /** Добавляет иконку фильтров + счетчик counter */
  filter?: boolean;
}

export const Button: FC<ButtonProps> = ({
  className,
  children,
  filter,
  counter,
  type,
  icon,
  ...props
}) => (
  <AntdButton
    className={cn(styles.styledButton, className)}
    type={filter ? 'primary' : type}
    icon={filter ? <ControlIcon /> : icon}
    {...props}
  >
    {children}
    {filter && !!counter && <span className={styles.counter}>{counter}</span>}
  </AntdButton>
);
