import React from 'react';
import { Button as ButtonAntd, ButtonProps } from 'antd-mobile';
import cn from 'classnames';

import styles from './Button.module.scss';

const Button: React.FC<ButtonProps> = ({
  className,
  children,
  disabled,
  size = 'middle',
  color = 'primary',
  ...props
}) => (
  <ButtonAntd
    {...props}
    size={size}
    color={color}
    disabled={disabled}
    className={cn(
      styles.button,
      {
        [styles.disabled]: disabled,
      },
      [className]
    )}
  >
    {children}
  </ButtonAntd>
);

export default Button;
