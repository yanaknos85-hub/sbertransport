import classNames from 'classnames';
import React, { FC } from 'react';

import { UserMenu } from 'modules/EmployeeApp/components/UserMenu/UserMenu';

import styles from './header.module.less';

export const AppHeader: FC = () => (
  <header className={classNames(styles.header, styles.header__client)}>
    <UserMenu />
  </header>
);
