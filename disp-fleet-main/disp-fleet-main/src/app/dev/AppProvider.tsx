import { ConfigProvider as UIKitProvider } from '@sber-sbertransport/ui-kit/src';
import { observer } from 'mobx-react';
import React, { FC, useState } from 'react';
import { QueryCache, ReactQueryCacheProvider } from 'react-query';
import { I18Provider } from 'i18n';
import AppStore, { AppStoreContext } from 'ioc';
import { BreadcrumbsProvider } from 'components/Breadcrumbs/Breadcrumbs';

const queryCache = new QueryCache({
  defaultConfig: {
    queries: {
      suspense: true,
      staleTime: Infinity,
      cacheTime: 1000,
    },
    mutations: {
      throwOnError: true,
    },
  },
});

export const AppProvider: FC = observer(({ children }): JSX.Element => {
  const [appStore] = useState(AppStore());

  return (
    <I18Provider>
      <UIKitProvider>
        <AppStoreContext.Provider value={appStore}>
          <ReactQueryCacheProvider queryCache={queryCache}>
            <BreadcrumbsProvider>
              {children}
            </BreadcrumbsProvider>
          </ReactQueryCacheProvider>
        </AppStoreContext.Provider>
      </UIKitProvider>
    </I18Provider>
  );
});

export default AppProvider;
