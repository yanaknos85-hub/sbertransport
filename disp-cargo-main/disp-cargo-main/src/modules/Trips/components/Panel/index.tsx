import React, { FC, HTMLProps } from 'react';
import cn from 'classnames';
import styles from './index.module.scss';

type IPanel = HTMLProps<HTMLDivElement>;

export const Panel: FC<IPanel> = ({ className, ...props }) => (
  <div className={cn(styles.panel, className)} {...props} />
);
