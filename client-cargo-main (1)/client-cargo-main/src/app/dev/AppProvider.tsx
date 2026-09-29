import React, { FC, useState } from 'react';
import { QueryCache, ReactQueryCacheProvider } from 'react-query';
import { ConfigProvider as UIKitProvider, Map2GISProvider } from '@sber-sbertransport/ui-kit/src';
import { observer } from 'mobx-react';
import { BreadcrumbsProvider } from 'shared/components/Breadcrumbs';
import { UiContextProvider } from 'shared/components/UI/UiContext';
import AppStore, { AppStoreContext } from 'stores';

import { API_2GIS_KEY, API_2GIS_SDO_KEY } from 'constants/constants.app';

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
    <UIKitProvider>
      <Map2GISProvider APIKey={appStore.configStore.env.IS_SDO ? API_2GIS_SDO_KEY : API_2GIS_KEY}>
        <AppStoreContext.Provider value={appStore}>
          <ReactQueryCacheProvider queryCache={queryCache}>
            <UiContextProvider>
              <BreadcrumbsProvider>
                {children}
              </BreadcrumbsProvider>
            </UiContextProvider>
          </ReactQueryCacheProvider>
        </AppStoreContext.Provider>
      </Map2GISProvider>
    </UIKitProvider>
  );
});

export default AppProvider;
