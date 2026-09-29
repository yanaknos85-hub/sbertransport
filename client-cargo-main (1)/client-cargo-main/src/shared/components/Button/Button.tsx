import React from 'react';
import { Button as ButtonAntd } from 'antd';
import { ButtonProps } from 'antd/lib/button';
import cn from 'classnames';

import styles from './Button.module.scss';

type TButton = ButtonProps & { className?: string };

export const Button: React.FC<TButton> = ({
  className, size = 'middle', ...props
}) => (
  <ButtonAntd
    className={cn(
      styles.Button,
      {
        [styles[size]]: size,
      },
      [className]
    )}
    {...props}
  />
);
