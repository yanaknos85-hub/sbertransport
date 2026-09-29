import React, { FC } from 'react';
import cn from 'classnames';
import styles from './PageLayout.module.scss';

const PageLayout: FC<{ oneColumn?: boolean }> = ({ children, oneColumn }) => (
  <div
    className={cn(styles.pageLayout, {
      [styles.pageLayoutOneColumn]: oneColumn,
    })}
  >
    {children}
  </div>
);

export default PageLayout;
