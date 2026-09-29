import React from 'react';
import type { FC } from 'react';
import { Tabs } from 'antd';
import { useLocation } from 'react-router-dom';
import { useHistory } from '@sber-sbertransport/mf-core';
import * as routes from 'constants/constants.routes';
import { TabPane } from 'shared/components/Tab/TabPane';
import { tabRoutes, OneLevelRoute, TwoLevelRoutes } from './constants/BusinessReports.routes';
import { removeIdFromUrl } from 'utils/removeIdFromUrl';

import styles from './styles.module.scss';

const getTab = ({
  title,
  route,
  component,
  key,
}: (OneLevelRoute | TwoLevelRoutes) & { key?: string }) => (
  <TabPane
    theme="card"
    tab={title}
    key={key ?? route}
    className={styles.tabMargins}
  >
    {component}
  </TabPane>
);

export const BusinessReports: FC = () => {
  const history = useHistory();
  const { pathname } = useLocation();

  const defaultActiveKey = removeIdFromUrl(pathname);
  // eslint-disable-next-line no-useless-escape
  const rootMask = new RegExp(`${routes.BUSINESS_REPORTS}/\[\\w\-\]\+`);
  const defaultActiveRootKey = pathname.match(rootMask)?.[0] ?? defaultActiveKey;

  return (
    <div className={styles.container}>
      <Tabs defaultActiveKey={defaultActiveRootKey} onChange={route => history.push(route)}>
        {tabRoutes.map(content => {
          if ('component' in content) {
            return getTab(content);
          }
        })}
      </Tabs>
    </div>
  );
};

export default BusinessReports;
