import React, { ReactNode, Suspense, useEffect } from 'react';
import { useTranslation } from 'i18n';
import { SpinWrapped } from 'shared/components/SpinWrapped/SpinWrapped';
import withErrorBoundary from 'shared/decorators/withErrorBoundary';
import { TabPane } from 'shared/components/Tab/TabPane';
import { Tab } from 'shared/components/Tab';
import { useHistory, useLocation } from 'react-router-dom';
import { ReferenceBooksTabs } from '../../constants.settings';

import RegistrySettingsContent from './RegistrySettingsContent';

import * as routes from 'constants/constants.routes';

const pageRoute = routes.REFERENCE_BOOKS;
const defaultTab = ReferenceBooksTabs.common;

const getItems = (
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  locales: any
): {
  tabLabel: string;
  tab: string;
  content: ReactNode;
  disabled?: boolean;
}[] => [
  {
    tabLabel: locales.passenger,
    tab: ReferenceBooksTabs.passengers,
    content: <RegistrySettingsContent />,
  },
];

export const RegistrySettings = withErrorBoundary(() => {
  const {
    t: {
      ServiceParamsPage: { RegistrySettings },
    },
  } = useTranslation();
  const items = getItems(RegistrySettings);
  const { push, replace } = useHistory();
  const { pathname: currentPath } = useLocation();

  useEffect(() => {
    if (currentPath === pageRoute) {
      replace(`${currentPath}/${defaultTab}`);
    }
  }, [replace, currentPath]);

  return (
    <Suspense fallback={<SpinWrapped />}>
      <Tab
        defaultActiveKey={defaultTab}
        onChange={route => push(`${pageRoute}/${route}`)}
        destroyInactiveTabPane
      >
        {items.map(({
          tabLabel, tab, content, disabled,
        }) => (
          <TabPane
            tab={tabLabel}
            key={tab}
            disabled={disabled}
          >
            {content}
          </TabPane>
        ))}
      </Tab>
    </Suspense>
  );
});

export default RegistrySettings;
