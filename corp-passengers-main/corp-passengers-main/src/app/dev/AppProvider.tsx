import { ConfigProvider as UIKitProvider, Map2GISProvider } from '@sber-sbertransport/ui-kit/src';
import { observer } from 'mobx-react';
import React, { FC, useState } from 'react';
import { QueryCache, ReactQueryCacheProvider } from 'react-query';
import { I18Provider } from 'i18n';
import AppStore, { AppStoreContext } from 'stores';
import { BreadcrumbsProvider } from 'shared/components/Breadcrumbs';
import { API_2GIS_KEY, API_2GIS_SDO_KEY } from 'constants/constants.geo';

const queryCache = new QueryCache({
  defaultConfig: {
    queries: {
      suspense: true,
      staleTime: Infinity,
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
        <Map2GISProvider APIKey={appStore.configStore.env.IS_SDO ? API_2GIS_SDO_KEY : API_2GIS_KEY}>
          <AppStoreContext.Provider value={appStore}>
            <ReactQueryCacheProvider queryCache={queryCache}>
              <BreadcrumbsProvider>
                {children}
              </BreadcrumbsProvider>
            </ReactQueryCacheProvider>
          </AppStoreContext.Provider>
        </Map2GISProvider>
      </UIKitProvider>
    </I18Provider>
  );
});

export default AppProvider;
