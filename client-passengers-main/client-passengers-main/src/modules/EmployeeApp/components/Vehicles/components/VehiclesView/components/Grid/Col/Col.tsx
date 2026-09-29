import classNames from 'classnames';
import React, { FC } from 'react';

import styles from './col.module.scss';

export enum Size {
  small = 'small',
  big = 'big',
}

const Col: FC<{ className?: string; size?: Size }> = ({
  className, size = Size.big, children,
}) => (
  <div className={classNames(styles.col, styles[size], className)}>{children}</div>
);

export default Col;
