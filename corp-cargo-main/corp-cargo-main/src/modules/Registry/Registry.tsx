import React, { useEffect } from 'react';
import type { FC } from 'react';
import { Tabs } from 'antd';
import { useLocation } from 'react-router-dom';
import { useHistory } from '@sber-sbertransport/mf-core';
import * as routes from 'constants/constants.routes';
import { useRole } from 'utils/useRole';
import { TabPane } from 'shared/components/Tab/TabPane';
import { tabRoutes, OneLevelRoute, TwoLevelRoutes } from './constants/Registry.routes';

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

export const Registry: FC = () => {
  const history = useHistory();
  const { pathname } = useLocation();
  const roles = useRole();

  const defaultActiveKey = pathname;
  const rootMask = new RegExp(`${routes.REGISTRY}/\[\\w\-\]\+`);
  const defaultActiveRootKey = pathname.match(rootMask)?.[0] ?? defaultActiveKey;

  useEffect(() => {
    const activeRootTab = tabRoutes.find(tab => tab.route === pathname);
    const isInnerTab = tabRoutes.some(tab => pathname.includes(tab.route));

    if (!!activeRootTab) {
      if ('tabs' in activeRootTab && !!activeRootTab.tabs) history.replace(activeRootTab.tabs[0].route);
    } else if (!isInnerTab) {
      history.replace(tabRoutes[0].route);
    }
  }, [pathname]);

  return (
    <div className={styles.container}>
      <Tabs defaultActiveKey={defaultActiveRootKey} onChange={route => history.push(route)}>
        {tabRoutes.map((content: any) => {
          if ('component' in content) {
            return getTab(content);
          }
          if ('tabs' in content) {
            const filteredTabs = (content.tabs || []).filter(tab =>
              // Что тут происходит???
              // @ts-ignore
              !('allowedRoles' in tab) || roles.some(role => tab.allowedRoles.includes(role))
            );

            return getTab({
              title: content.title,
              route: content.route,
              component: (
                <Tabs
                  defaultActiveKey={defaultActiveKey}
                  onChange={route => history.push(route)}
                  destroyInactiveTabPane
                >
                  {filteredTabs.map(tab => getTab({ ...tab }))}
                </Tabs>
              ),
            });
          }
        })}
      </Tabs>
    </div>
  );
};

export default Registry;
