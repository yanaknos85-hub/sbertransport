import React, { FC, Suspense } from 'react';
import { useLocation } from 'react-router-dom';
import { useHistory } from '@sber-sbertransport/mf-core';

import { Tab } from 'shared/components/Tab';
import { TabPane } from 'shared/components/Tab/TabPane';
import { tabRoutes } from './constants/customers.routes';
import styles from './Customers.module.scss';

import * as routes from 'constants/constants.routes';
import SpinWrapped from 'shared/components/SpinWrapped/SpinWrapped';
import ErrorBoundary from 'shared/components/ErrorBoundary';

export const Customers: FC = () => {
  const history = useHistory();
  const { pathname } = useLocation();

  // eslint-disable-next-line no-useless-escape
  const rootMask = new RegExp(`${routes.CUSTOMERS}/\[\\w\-\]\+`);
  const defaultActiveRootKey = pathname.match(rootMask)?.[0] ?? pathname;

  return (
    <div className={styles.container}>
      <Tab
        activeKey={defaultActiveRootKey}
        defaultActiveKey={defaultActiveRootKey}
        onChange={route => history.push(route)}
      >
        {tabRoutes.map(({
          title, route, component: Component,
        }) => (
          <TabPane
            tab={title}
            key={route}
          >
            <ErrorBoundary>
              <Suspense fallback={<SpinWrapped />}>
                <Component />
              </Suspense>
            </ErrorBoundary>
          </TabPane>
        ))}
      </Tab>
    </div>
  );
};

export default Customers;
