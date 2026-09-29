import React, { FC, useMemo, useEffect } from 'react';
import { useHistory, useLocation } from 'react-router-dom';
import { Tabs } from 'antd';

import { IS_DEV } from 'constants/constants.env';
import * as routes from 'constants/constants.routes';
import { OneLevelRoute, TwoLevelRoutes } from './constants/routes';

import { useRole } from 'utils/useRole';
import { TabPane } from 'shared/components/Tab/TabPane';
import { removeIdFromUrl } from 'utils/removeIdFromUrl';

import mfDataLoader from 'mf/MFDataLoader';

import styles from './styles.module.scss';

interface MFRoutes { data: { tabRoutes: (OneLevelRoute | TwoLevelRoutes)[] } }

// Грузим роуты из ремоутов
const MFBootRoutes = {
  platform: mfDataLoader(() => {
    try {
      return import('platform/modules/Registry/tabRoutes');
    } catch {
      return Promise.resolve();
    }
  }) as MFRoutes,
  passengers: mfDataLoader(() => {
    try {
      return import('passengers/modules/Registry/tabRoutes');
    } catch {
      return Promise.resolve();
    }
  }) as MFRoutes,
  cargo: mfDataLoader(() => {
    try {
      return import('cargo/modules/Registry/tabRoutes');
    } catch {
      return Promise.resolve();
    }
  }) as MFRoutes,
  fleet: mfDataLoader(() => {
    try {
      return import('fleet/modules/Registry/tabRoutes');
    } catch {
      return Promise.resolve();
    }
  }) as MFRoutes,
};

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

  const defaultActiveKey = removeIdFromUrl(pathname);

  // eslint-disable-next-line no-useless-escape
  const rootMask = new RegExp(`${routes.REGISTRY}/[\\w-]+`);
  const defaultActiveRootKey = pathname.match(rootMask)?.[0] ?? defaultActiveKey;

  const MFRoutes = useMemo(() => MFBootRoutes, []) as Record<string, MFRoutes>;

  const tabRoutes = [
    ...(MFRoutes.passengers?.data.tabRoutes || []),
    // ...(MFRoutes.platform?.data.tabRoutes || []),
    ...(MFRoutes.cargo?.data.tabRoutes || []),
    ...(MFRoutes.fleet?.data.tabRoutes || []),
  ];

  useEffect(() => {
    if (IS_DEV) {
      // eslint-disable-next-line no-console
      console.log('REGISTRY Routes', MFRoutes);
    }
  }, [MFRoutes]);

  useEffect(() => {
    if (tabRoutes.length) {
      const activeRootTab = tabRoutes.find(tab => tab.route === pathname);
      if (activeRootTab) {
        if ('tabs' in activeRootTab && !!activeRootTab.tabs) history.push(activeRootTab.tabs[0].route);
      }
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tabRoutes, pathname]);

  if (!tabRoutes.length) {
    return null;
  }

  return (
    <div className={styles.container}>
      <Tabs
        defaultActiveKey={defaultActiveRootKey}
        activeKey={defaultActiveRootKey}
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
                  activeKey={defaultActiveKey}
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
