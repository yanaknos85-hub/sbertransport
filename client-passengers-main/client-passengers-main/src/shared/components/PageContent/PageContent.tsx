import classNames from 'classnames';
import React, { FC } from 'react';

import styles from './layout.module.scss';

export const PageContent: FC<any> = ({ children, className }) => (
  <div className={classNames(styles.layout, className)}>{children}</div>
);
