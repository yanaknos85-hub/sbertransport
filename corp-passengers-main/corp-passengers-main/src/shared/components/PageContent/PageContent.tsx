import classNames from 'classnames';
import React, { FC } from 'react';

import styles from './layout.module.scss';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
export const PageContent: FC<any> = ({ children, className }) => (
  <div className={classNames(styles.layout, className)}>{children}</div>
);
