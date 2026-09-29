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
import { removeIdFromUrl } from 'utils/removeIdFromUrl';
// import { removeIdFromUrl } from 'utils/removeIdFromUrl';

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
  // eslint-disable-next-line no-useless-escape
  const rootMask = new RegExp(`${routes.REGISTRY}/\[\\w\-\]\+`);
  const defaultActiveKey = removeIdFromUrl(pathname);
  const defaultActiveRootKey = pathname.match(rootMask)?.[0] ?? defaultActiveKey;

  useEffect(() => {
    const activeRootTab = tabRoutes.find(tab => tab.route === pathname);

    // eslint-disable-next-line no-extra-boolean-cast
    if (!!activeRootTab) {
      if ('tabs' in activeRootTab) history.replace(activeRootTab.tabs[0].route);
    }
  }, [pathname]);

  return (
    <div className={styles.container}>
      <Tabs
        activeKey={defaultActiveRootKey}
        defaultActiveKey={defaultActiveRootKey}
        onChange={route => history.push(route)}
      >
        {tabRoutes.map(content => {
          if ('component' in content) {
            return getTab(content);
          }
          if ('tabs' in content) {
            const filteredTabs = (content.tabs || []).filter(tab =>
              // Что тут происходит???
              // @ts-ignore
              // eslint-disable-next-line @stylistic/implicit-arrow-linebreak
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
