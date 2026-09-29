import React, { FC } from 'react';
import { Link, LinkProps } from 'react-router-dom';
import cn from 'classnames';
import styles from '../../Table.module.scss';

export const CellLink: FC<LinkProps> = ({
  children, className, ...props
}) => (
  <Link {...props} className={cn(styles.link, className)}>
    {children}
  </Link>
);
