import React, { FC } from 'react';
import classNames from 'classnames';

import { UserMenu } from 'components/UserMenu/UserMenu';

import styles from './header.module.less';

export const AppHeader: FC = () => (
  <header className={classNames(styles.header, styles.header__client)}>
    <UserMenu />
  </header>
);
