import {
  ConfigProvider as UIKitProvider,
  Map2GISContext,
  use2GIS
} from '@sber-sbertransport/ui-kit/src';
import { observer } from 'mobx-react';
import React, { FC, useState } from 'react';
import { QueryCache, ReactQueryCacheProvider } from 'react-query';
import { I18Provider } from 'i18n';
import { BreadcrumbsContext } from 'shared/components/Breadcrumbs';
import AppStore, { AppStoreContext } from 'stores';
import { SettingsContext, RegistrySettingsContext } from 'stores/SettingsContext';
import { OrganizationContext, Props as OrganizationProviderProps } from 'context/Organization.context';

interface Props {
  value: {
    queryCache: QueryCache;
    settings: RegistrySettingsContext;
    organization: OrganizationProviderProps;
    breadcrumbs: BreadcrumbsContext;
    mapContext: ReturnType<typeof use2GIS>;
  };
  children: React.ReactNode;
}

export const AppProvider: FC<Props> = observer(({ value, children }): JSX.Element => {
  const [appStore] = useState(AppStore());

  return (
    <I18Provider>
      <UIKitProvider>
        <Map2GISContext.Provider value={value.mapContext}>
          <AppStoreContext.Provider value={appStore}>
            <ReactQueryCacheProvider queryCache={value.queryCache}>
              <BreadcrumbsContext.Provider value={value.breadcrumbs}>
                <SettingsContext.Provider value={value.settings}>
                  <OrganizationContext.Provider value={value.organization}>
                    {children}
                  </OrganizationContext.Provider>
                </SettingsContext.Provider>
              </BreadcrumbsContext.Provider>
            </ReactQueryCacheProvider>
          </AppStoreContext.Provider>
        </Map2GISContext.Provider>
      </UIKitProvider>
    </I18Provider>
  );
});

export default AppProvider;
