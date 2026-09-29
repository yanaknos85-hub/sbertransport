import { Button as AntdButton } from 'antd';
import React, { FC } from 'react';
import cn from 'classnames';
import { ButtonProps } from 'antd/lib/button';
import styles from './index.module.scss';

export const Button: FC<ButtonProps> = ({ className, ...props }) => (
  <AntdButton className={cn(styles.styledButton, className)} {...props} />
);
